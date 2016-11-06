package com.aiziyuer.app.common;

/**
 * 静态常量类
 */

public class AppConstant {

    /** 私钥 */
    public static final String PRIVATE_KEY = "PRIVATE_KEY";

    /** 公钥 */
    public static final String PUBLIC_KEY = "PUBLIC_KEY";

    /** 全局的上下文配置 */
    public static final String GLOBAL_SHARED_PREFERENCE = "GLOBAL_SHARED_PREFERENCE ";

    /////////--------配置项 开始 ------------//////////
    /** 全局配置项中的IV键名 */
    public static final String IV_KEY_NAME = "IV_KEY_NAME";

    /** 保存在AndroidKeystore中的可以密钥名字*/
    public static final String ANDROID_KEYSTORE_AES_KEY_NAME = "com.aiziyuer.app.fingerprint_authentication_key";

    /////////--------配置项 结束 ------------//////////

    /** KeyStore类型 */
    public static final String ANDROID_KEYSTORE_TYPE = "AndroidKeyStore";

}
