/*
watermark参数说明：

        参数	类型	说明
        watermark	OBJECT	数据水印
        appid	String	敏感数据归属appid，开发者可校验此参数与自身appid是否一致
        timestamp	DateInt	敏感数据获取的时间戳, 开发者可以用于数据时效性校验
*/

package com.amt.wechat.service.poi.impl;

import com.alibaba.fastjson.JSONObject;
import com.amt.wechat.common.POI_USER_ROLE;
import com.amt.wechat.dao.RedisDao;
import com.amt.wechat.dao.poi.POIUserDAO;
import com.amt.wechat.domain.PhoneData;
import com.amt.wechat.domain.id.Generator;
import com.amt.wechat.domain.packet.BizPacket;
import com.amt.wechat.domain.util.DateTimeUtil;
import com.amt.wechat.domain.util.WeichatUtil;
import com.amt.wechat.form.WeichatLoginForm;
import com.amt.wechat.model.poi.POIUserData;
import com.amt.wechat.service.poi.IPOIUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Copyright (c) 2018 by CANSHU
 *
 * @author adu Create on 2018-12-17 10:40
 * @version 1.0
 */
@Service("poiUserService")
public class POIUserServiceImpl implements IPOIUserService {

    private static Logger logger = LoggerFactory.getLogger(POIUserServiceImpl.class);
    private @Resource POIUserDAO poiUserDAO;
    private @Resource RedisDao redisDao;

    @Override
    public BizPacket weichatLogin(String code, String encryptedData, String iv, PhoneData phoneData) {
        JSONObject sessionKeyAndOpenid = WeichatUtil.getSessionKeyOrOpenId(code);
        if(sessionKeyAndOpenid == null){
            return BizPacket.error(HttpStatus.BAD_REQUEST.value(),"登录凭证校验失败!");
        }

        logger.info("用户[sessionKey_openid={},code={}]正在从微信登录!",sessionKeyAndOpenid.toString(),code);

        String sessionKey = sessionKeyAndOpenid.getString("session_key");
        String openid = sessionKeyAndOpenid.getString("openid");

        try {
            POIUserData userData =  poiUserDAO.getPOIUserData(openid,phoneData.getPurePhoneNumber());
            if(userData == null){
                JSONObject userJson =  WeichatUtil.getUserInfo(encryptedData,sessionKey,iv);
                logger.info("创建微信用户!userJson={}",userJson);

                userData = createUser(sessionKeyAndOpenid,userJson,phoneData);
                poiUserDAO.addPOIUser(userData);

            }else{

                JSONObject userJson =  WeichatUtil.getUserInfo(encryptedData,sessionKey,iv);
                logger.info("更新微信用户!userJson={}",userJson);
                updateUser(userData,sessionKeyAndOpenid,userJson);
                poiUserDAO.updatePOIUser(userData);
            }

            redisDao.addPOIUser(userData);

            long now = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
            WeichatLoginForm form = new WeichatLoginForm(now);
            form.setAuthToken(userData.getAuthToken());
            form.setNickName(userData.getNickName());
            form.setAvatarUrl(userData.getAvatarUrl());

            return BizPacket.success(form);
        } catch (IOException e) {
            logger.error(phoneData+",e="+e.getMessage(),e);
            return BizPacket.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),e.getMessage());
        }
    }


    private POIUserData createUser(JSONObject sessionKeyAndOpenid,JSONObject userJson,PhoneData phoneData){
        POIUserData data = new POIUserData();
        data.setId(Generator.generate());
        data.setcTime(DateTimeUtil.now());
        data.setuTime(data.getcTime());
        data.setOpenid(sessionKeyAndOpenid.getString("openid"));
        data.setIsAccountNonExpired(1);
        data.setIsAccountNonLocked(1);
        data.setIsCredentialsNonExpired(1);
        data.setIsEnabled(1);
        data.setIsMaster(0);

        data.setRoles(POI_USER_ROLE.USER.toString());
        data.setPassword("");
        data.setMobile(phoneData.getPurePhoneNumber());
        data.setCountryCode(phoneData.getCountryCode());

        updateUser(data,sessionKeyAndOpenid,userJson);
        return data;
    }


    private void updateUser(POIUserData data,JSONObject sessionKeyAndOpenid,JSONObject userJson){
        data.setAuthToken(Generator.generate());

        // unionid 不一定存在!
        String unionid = sessionKeyAndOpenid.getString("unionid");
        if(unionid != null && unionid.trim().length() != 0) {
            data.setUnionid(unionid);
        }else{
            data.setUnionid("");
        }

        if(userJson == null){
            return;
        }
        Integer gender = userJson.getInteger("gender");
        data.setGender(gender == null ?0:gender);

        String province = userJson.getString("province");
        data.setProvince(province == null?"":province);

        String city = userJson.getString("city");
        data.setCity(city == null?"":city);

        String nickName = userJson.getString("nickName");
        data.setNickName(nickName == null?"":nickName);

        String avatarUrl = userJson.getString("avatarUrl");
        data.setAvatarUrl(avatarUrl == null ?"":avatarUrl);
    }

    @Override
    public BizPacket weichatLogin4Phone(HttpSession session,String code, String  encryptedData, String iv){
        JSONObject sessionKeyAndOpenid = WeichatUtil.getSessionKeyOrOpenId(code);
        if(sessionKeyAndOpenid == null){
            return BizPacket.error(HttpStatus.BAD_REQUEST.value(),"登录凭证校验失败!");
        }

        PhoneData phoneData = WeichatUtil.getPhoneData(encryptedData,  sessionKeyAndOpenid.getString("session_key"),iv);
        if(phoneData != null){
            session.setAttribute(PhoneData.SESSION_PHONE,phoneData.getPurePhoneNumber());
            session.setAttribute(PhoneData.SESSION_PHONE_CC,phoneData.getCountryCode());
            return BizPacket.success();
        }
        return BizPacket.error(HttpStatus.REQUEST_TIMEOUT.value(),"请求超时,获取失败!");
    }

    @Override
    public UserDetails loadUserByUsername(String mobile) throws UsernameNotFoundException {
        logger.info("请求登录用户名-mobile={}", mobile);

        POIUserData userData= poiUserDAO.getPOIUserDataByMobile(mobile);
        if(userData == null){
            throw new UsernameNotFoundException("用户[" + mobile + "]不存在!");
        }

        logger.info("用户‘{}’的password={}", mobile,userData.getPassword());

        // 参数分别是：用户名，密码，用户权限 TODO
        User user = new User(userData.getMobile(), userData.getPassword(), AuthorityUtils.commaSeparatedStringToAuthorityList(userData.getRoles()));
        return user;
    }
}