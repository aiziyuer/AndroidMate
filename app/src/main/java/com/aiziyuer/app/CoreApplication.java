package com.aiziyuer.app;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.orm.SugarContext;

/**
 * 应用程序的入口
 */

public class CoreApplication extends Application {

    private static CoreApplication app;

    public static CoreApplication getInstance() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化全局调试开关
        Stetho.initializeWithDefaults(this);
        SugarContext.init(this);

        app = this;
    }

}
