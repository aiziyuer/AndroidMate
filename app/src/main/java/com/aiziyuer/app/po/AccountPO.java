package com.aiziyuer.app.po;

import android.support.annotation.CallSuper;

import com.orm.SugarRecord;
import com.orm.dsl.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 账号信息
 */
@Data
@Table(name = "account")
@EqualsAndHashCode(callSuper = true)
public class AccountPO extends SugarRecord {

    private String siteName;

    private String siteUrl;

    private String userName;

    private String userPasswd;

}
