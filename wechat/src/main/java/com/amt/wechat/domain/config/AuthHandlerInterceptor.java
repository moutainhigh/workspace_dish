package com.amt.wechat.domain.config;

import com.alibaba.fastjson.JSON;
import com.amt.wechat.common.Constants;
import com.amt.wechat.service.redis.RedisService;
import com.amt.wechat.domain.packet.BizPacket;
import com.amt.wechat.domain.util.DateTimeUtil;
import com.amt.wechat.model.poi.POIUserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Copyright (c) 2018 by CANSHU
 *
 * @author adu Create on 2018-12-29 10:32
 * @version 1.0
 */
@Component("authHandlerInterceptor")
public class AuthHandlerInterceptor implements HandlerInterceptor {
    private static final Logger traceLog = LoggerFactory.getLogger("globalTraceLog");
    private static final Logger logger = LoggerFactory.getLogger(AuthHandlerInterceptor.class);

    private  @Autowired RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String accessToken = request.getParameter(Constants.REQ_PARAM_ACCESSTOKEN);
        if (StringUtils.isEmpty(accessToken)) {
            traceLog(request, accessToken,"");
            return handlerError(response,HttpStatus.UNAUTHORIZED, "access_token is empty!");
        }

        try {
            POIUserData user = redisService.getPOIUser(accessToken);
            if (user == null) {
                traceLog(request, accessToken,"");
                return handlerError(response,HttpStatus.UNAUTHORIZED,"user not found or frozen!");
            }
            if(!check(response,user)){
                return false;
            }

            request.setAttribute(Constants.WECHAT_LOGGED_USER, user);
            traceLog(request, accessToken, user.getId());
            return true;
        } catch (Exception e) {
            return handlerError(response, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    private boolean check(HttpServletResponse response,POIUserData user) throws IOException {
        if (user.getIsAccountNonLocked() != 1) {
            return handlerError(response,HttpStatus.UNAUTHORIZED, "User account is locked");
        }

        if (user.getIsEnabled() != 1) {
            return handlerError(response,HttpStatus.UNAUTHORIZED ,"User is disabled");
        }

        if (user.getIsAccountNonExpired() != 1) {
            return handlerError(response,HttpStatus.UNAUTHORIZED,"User account has expired");
        }

        if (user.getIsCredentialsNonExpired() != 1) {
            return handlerError(response,HttpStatus.UNAUTHORIZED,"User credentials have expired");
        }
        return true;
    }

    private boolean handlerError(HttpServletResponse response,HttpStatus status, String msg) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        BizPacket bizPacket =  BizPacket.error(HttpStatus.UNAUTHORIZED.value(),msg);
        String s = JSON.toJSONString(bizPacket);
        response.getWriter().write(s);
        return false;
    }

    private void traceLog(HttpServletRequest request, String accessToken,String userId) {
        try {
            String addr = request.getRemoteAddr();
            String uri = request.getRequestURI();
            String params = asParams2String(request);
            String token = accessToken == null ? "" : accessToken.trim();
            traceLog.error("createDate={},ip={},reqURI={},accessToken={},userId={},params={}", DateTimeUtil.now(), addr,uri, token,userId, params);
        } catch (Exception e) {
            logger.error("accessToken=" + accessToken + ",e=" + e.getMessage(), e);
        }
    }

    private String asParams2String(HttpServletRequest request) {
        StringBuilder params = new StringBuilder();
        Enumeration<String> pNames = request.getParameterNames();
        while (pNames.hasMoreElements()) {
            String name = pNames.nextElement();
            String value = request.getParameter(name);
            params.append(name).append(":").append(value).append(",");
        }
        if (params.length() >= 2) {
            params.deleteCharAt(params.length() - 1);
        }

        return params.toString();
    }
}