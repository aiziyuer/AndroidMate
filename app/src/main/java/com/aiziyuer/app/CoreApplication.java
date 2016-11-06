package com.aiziyuer.app;

import android.app.Application;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.util.Log;

import com.aiziyuer.app.auth.FingerprintHelper;
import com.aiziyuer.app.common.AppConstant;
import com.facebook.stetho.Stetho;
import com.orm.SugarContext;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;

import static android.hardware.fingerprint.FingerprintManager.*;
import static com.aiziyuer.app.auth.FingerprintHelper.*;

/**
 * 应用程序的入口
 */

public class CoreApplication extends Application {

    private SharedPreferences globalSettings;

    private static final String TAG = "CoreApplication";

    private static CoreApplication app;
    private FingerprintHelper helper;
    private AbstractAuthenticationCallback callback;

    public static CoreApplication getInstance() {
        return app;
    }

    @Override
    public void onCreate() {
        try {
            super.onCreate();

            // 初始化全局调试开关
            Stetho.initializeWithDefaults(this);
            SugarContext.init(this);

            helper = new FingerprintHelper(this);

            globalSettings = getSharedPreferences(AppConstant
                    .GLOBAL_SHARED_PREFERENCE, 0);

            // 判断是否系统中已经有了加密用的AES密钥
            if (!globalSettings.contains(AppConstant.PRIVATE_KEY)) {
                callback = new CoreAuthenticationCallback("my key");

                helper.generateKey(); // 里面会生成一个新的AES密钥并且存储在AndroidKeyStore里面, 需要指纹解锁后才可以访问
                helper.setPurpose(KeyProperties.PURPOSE_ENCRYPT);
                helper.authenticate(callback);

            }

            // TODO 测试

            app = this;
        } catch (Exception e) {
            System.exit(0);
        }
    }


    private class CoreAuthenticationCallback extends FingerprintHelper
            .AbstractAuthenticationCallback {

        boolean isFirst = true;

        CoreAuthenticationCallback(String originalData) {
            super(originalData);
        }

        @Override
        protected void onAuthenticationSucceeded(String value) {

            Log.i(TAG, "onAuthenticationSucceeded, value: " + value + ".");

            // 考虑把密钥放到全局配置里
            System.out.println(value);

            helper.stopAuthenticate();

            if (isFirst) {
                setOriginalData(value);
                helper.setPurpose(KeyProperties.PURPOSE_DECRYPT);
                helper.authenticate(callback);
                helper.stopAuthenticate();
                isFirst = false;
            }
        }

        @Override
        protected void onAuthenticationFail() {
            Log.e(TAG, "onAuthenticationFail");
            helper.stopAuthenticate();
        }
    }


}
