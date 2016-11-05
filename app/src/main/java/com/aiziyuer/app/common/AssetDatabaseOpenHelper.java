package com.aiziyuer.app.common;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * 数据库辅助类, 用于打开数据库
 */

public class AssetDatabaseOpenHelper extends SQLiteAssetHelper {

    private static final int DATABASE_VERSION = 1;

    public AssetDatabaseOpenHelper(Context context, String name) {
        super(context, name, null, DATABASE_VERSION);
    }

}
