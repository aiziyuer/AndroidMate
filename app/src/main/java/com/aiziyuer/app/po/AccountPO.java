package com.aiziyuer.app.po;

import com.orm.SugarRecord;
import com.orm.dsl.Table;

import lombok.Data;

/**
 * 账号信息
 */
@Data
@Table(name = "account")
public class AccountPO extends SugarRecord {

    private String siteName;

    private String siteUrl;

    private String userName;

    private String userPasswd;

}
