package com.wmt.wechat.controller.wechat.base;

import com.wmt.wechat.common.Constants;
import com.wmt.wechat.model.poi.PoiUserData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Copyright (c) 2018 by CANSHU
 *
 * @author adu Create on 2018-12-29 10:18
 * @version 1.0
 */
@Component("wechatBaseController")
public class BaseController {

    private @Autowired   HttpServletRequest request;

    protected PoiUserData getUser(){
        PoiUserData user = (PoiUserData)request.getAttribute(Constants.WECHAT_LOGGED_USER);
        return user;
    }
}