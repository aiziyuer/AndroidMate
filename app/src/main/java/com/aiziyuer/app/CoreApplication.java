package com.aiziyuer.app;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.aiziyuer.app.common.AppConstant;
import com.facebook.stetho.Stetho;
import com.mtramin.rxfingerprint.RxFingerprint;
import com.orm.SugarContext;

import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

/**
 * 应用程序的入口
 */

public class CoreApplication extends Application {

    private SharedPreferences globalSettings;

    private static final String TAG = "CoreApplication";

    private Disposable fingerprintDisposable = Disposables.empty();

    private static CoreApplication app;

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

            if (RxFingerprint.isUnavailable(this)) {
                return;
            }

            String stringToEncrypt = "Hello world";
            fingerprintDisposable = RxFingerprint.encrypt(this, stringToEncrypt)
                    .subscribe(fingerprintEncryptionResult -> {
                        switch (fingerprintEncryptionResult.getResult()) {
                            case FAILED:
                                Log.i(TAG, "Fingerprint not recognized, try " +
                                        "again!");
                                break;
                            case HELP:
                                Log.i(TAG, fingerprintEncryptionResult
                                        .getMessage());
                                break;
                            case AUTHENTICATED:
                                String encrypted =
                                        fingerprintEncryptionResult
                                                .getEncrypted();
                                Log.d(TAG, encrypted);
                                // 验证成功后就关闭
                                fingerprintDisposable.dispose();

                                // 解密
                                fingerprintDisposable = RxFingerprint.decrypt(this,
                                        encrypted)
                                        .subscribe(fingerprintDecryptionResult -> {
                                            switch (fingerprintDecryptionResult.getResult()) {
                                                case FAILED:
                                                    Log.i(TAG, "Fingerprint not recognized, try " +
                                                            "again!");
                                                    break;
                                                case HELP:
                                                    Log.i(TAG, fingerprintEncryptionResult
                                                            .getMessage());
                                                    break;
                                                case AUTHENTICATED:
                                                    String decrypted =
                                                            fingerprintDecryptionResult
                                                                    .getDecrypted();
                                                    Log.d(TAG, decrypted);
                                                    fingerprintDisposable.dispose();
                                                    break;
                                            }
                                        }, throwable -> {
                                            if (RxFingerprint.keyInvalidated(throwable)) {
                                                // 需要重新验证
                                                // TODO
                                            }
                                            Log.e("ERROR", "decrypt", throwable);
                                        });

                                break;
                        }
                    }, throwable -> {
                        if (RxFingerprint.keyInvalidated(throwable)) {
                            // 需要重新验证
                            // TODO
                        }
                        Log.e("ERROR", "encrypt", throwable);
                    });

            app = this;
        } catch (Exception e) {
            System.exit(0);
            fingerprintDisposable.dispose();
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        fingerprintDisposable.dispose();
    }
}
