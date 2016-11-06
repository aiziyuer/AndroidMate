package com.aiziyuer.app.auth;

/**
 * 指纹识别辅助类
 */

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.aiziyuer.app.common.AppConstant;

import java.io.UnsupportedEncodingException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import lombok.Getter;
import lombok.Setter;

/**
 * 指纹识别辅助类
 */
public class FingerprintHelper extends FingerprintManager.AuthenticationCallback {

    /** 全局配置 */
    private final SharedPreferences globalSettings;

    private static final String TAG = "FingerprintHelper ";

    private FingerprintManager manager;
    private CancellationSignal mCancellationSignal;

    /** 认证处理类 */
    @Setter
    private AbstractAuthenticationCallback callback;

    /** 密钥管理 */
    private LocalAndroidKeyStore mLocalAndroidKeyStore;

    /** 认证的目的, 加密/解密 */
    @Setter
    private int purpose = KeyProperties.PURPOSE_ENCRYPT;

    public FingerprintHelper(Context context) {
        manager = context.getSystemService(FingerprintManager.class);
        mLocalAndroidKeyStore = new LocalAndroidKeyStore();
        globalSettings = context.getSharedPreferences(AppConstant
                .GLOBAL_SHARED_PREFERENCE, 0);

        // TODO 考虑把下面的校验抽到其他地方去
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            // Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(context, "checkSelfPermission", Toast.LENGTH_LONG).show();
            throw new RuntimeException("checkSelfPermission error");
        }

        // 检查手机是不是带了指纹识别功能
        if (!manager.isHardwareDetected()) {
            String logContext = "no fingerprint hardware detected.";
            Log.e(TAG, logContext);
            Toast.makeText(context, logContext, Toast.LENGTH_LONG).show();
            throw new RuntimeException(logContext);
        }

        // 检查手机内是否设置了指纹
        if (!manager.hasEnrolledFingerprints()) {
            String logContext = "no fingerprint created.";
            Log.e(TAG, logContext);
            Toast.makeText(context.getApplicationContext(), logContext, Toast.LENGTH_LONG).show();
            throw new RuntimeException(logContext);
        }
    }

    public void generateKey() {
        //在keystore中生成加密密钥
        mLocalAndroidKeyStore.generateKey(AppConstant.ANDROID_KEYSTORE_AES_KEY_NAME);
        purpose = KeyProperties.PURPOSE_ENCRYPT;
    }

    /***
     * 认证入口
     */
    public boolean authenticate(AbstractAuthenticationCallback callback) {
        this.callback = callback;

        String iv = globalSettings.getString(AppConstant.IV_KEY_NAME, null);
        // 解密是不允许iv为空
        if (purpose == KeyProperties.PURPOSE_DECRYPT && TextUtils.isEmpty(iv)) {
            Log.e(TAG, "IV is null.");
            return false;
        }

        try {
            FingerprintManager.CryptoObject object =
                    purpose == KeyProperties.PURPOSE_DECRYPT ? mLocalAndroidKeyStore.getCryptoObject
                            (Cipher.DECRYPT_MODE, Base64
                                    .decode
                                            (iv, Base64.URL_SAFE)) : mLocalAndroidKeyStore
                            .getCryptoObject(Cipher
                                    .ENCRYPT_MODE, null);

            // 对象不允许为空
            if (object == null) {
                Log.e(TAG, "CryptoObject is null.");
                return false;
            }

            mCancellationSignal = new CancellationSignal();
            manager.authenticate(object, mCancellationSignal, 0, this, null);
            return true;
        } catch (SecurityException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void stopAuthenticate() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
        callback = null;
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        if (callback == null) {
            return;
        }
        if (result.getCryptoObject() == null) {
            callback.onAuthenticationFail();
            return;
        }
        Cipher cipher = result.getCryptoObject().getCipher();
        String data = callback.getOriginalData(); // 需要处理的子串
        //取出secret key并返回
        if (TextUtils.isEmpty(data)) {
            callback.onAuthenticationFail();
            return;
        }
        switch (purpose) {
            case KeyProperties.PURPOSE_DECRYPT:
                //解密模式
                try {
                    byte[] decrypted = cipher.doFinal(Base64.decode(data.getBytes("UTF-8"),
                            Base64.URL_SAFE));
                    callback.onAuthenticationSucceeded(new String(Base64.decode(decrypted, Base64
                            .URL_SAFE)));
                } catch (BadPaddingException | IllegalBlockSizeException |
                        UnsupportedEncodingException e) {
                    e.printStackTrace();
                    callback.onAuthenticationFail();
                }
                break;
            default:
                //加密模式
                //将前面生成的data包装成secret key，存入沙盒
                try {
                    // 传入的参数需要先使用Base64进行加码
                    byte[] encrypted = cipher.doFinal(Base64.encode(data.getBytes("UTF-8"), Base64
                            .URL_SAFE));
                    String se = Base64.encodeToString(encrypted, Base64.URL_SAFE);
                    String siv = Base64.encodeToString(cipher.getIV(), Base64.URL_SAFE);
                    // 更新配置中的IV值
                    globalSettings.edit().putString(AppConstant.IV_KEY_NAME, siv).apply();
                    callback.onAuthenticationSucceeded(se);
                } catch (BadPaddingException | IllegalBlockSizeException |
                        UnsupportedEncodingException e) {
                    Log.e(TAG, "encrypt error.", e);
                    callback.onAuthenticationFail();
                }
        }
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        if (callback != null) {
            callback.onAuthenticationFail();
        }
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
    }

    @Override
    public void onAuthenticationFailed() {
    }

    /**
     * 抽象的认证回调函数
     * 定义了一些必须要实现的接口简化认证的过程
     */
    public static abstract class AbstractAuthenticationCallback {

        /** 需要处理的数据 */
        @Getter
        @Setter
        private String originalData;

        public AbstractAuthenticationCallback(String originalData) {
            this.originalData = originalData;
        }

        /**
         * 成功后的回调
         *
         * @param value 原始串经过处理后的子串
         */
        protected abstract void onAuthenticationSucceeded(String value);

        /**
         * 认证失败
         */
        protected abstract void onAuthenticationFail();
    }
}
