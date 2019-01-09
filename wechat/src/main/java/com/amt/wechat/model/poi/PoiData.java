package com.amt.wechat.model.poi;

import com.alibaba.fastjson.JSON;

/**
 * Copyright (c) 2019 by CANSHU
 *
 *  门店数据
 *
 * @author adu Create on 2019-01-03 17:21
 * @version 1.0
 */
public class PoiData extends PoiBasicData {
    private static final long serialVersionUID = 98370870191036587L;

    /**
     * 门店帐户名
     */
    private String accountName;

    /**
     *
     */
    private String accountPassword;

    /**
     * 美团门店id
     */
    private String mtAppAuthToken;

    /**
     * 饿了么门店Id
     */
    private String eleShopId;

    /**
     * 最后更新时间
     */
    private String updTime;



    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountPassword() {
        return accountPassword;
    }

    public void setAccountPassword(String accountPassword) {
        this.accountPassword = accountPassword;
    }

    public String getMtAppAuthToken() {
        return mtAppAuthToken;
    }

    public void setMtAppAuthToken(String mtAppAuthToken) {
        this.mtAppAuthToken = mtAppAuthToken;
    }

    public String getEleShopId() {
        return eleShopId;
    }

    public void setEleShopId(String eleShopId) {
        this.eleShopId = eleShopId;
    }


    public String getUpdTime() {
        return updTime;
    }

    public void setUpdTime(String updTime) {
        this.updTime = updTime;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}