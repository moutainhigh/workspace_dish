package com.amt.wechat.controller.auth;

import com.amt.wechat.domain.packet.BizPacket;
import com.amt.wechat.domain.util.WechatUtil;
import com.amt.wechat.form.basic.WeichatLoginForm;
import com.amt.wechat.model.poi.PoiUserData;
import com.amt.wechat.service.poi.PoiUserService;
import com.amt.wechat.service.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
public class LoginController{

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private @Resource PoiUserService poiUserService;
    private  @Autowired RedisService redisService;

    /**
     * @param code 授权码
     * @param encryptedData 包括敏感数据在内的完整用户信息的加密数据
     * @param iv 加密算法的初始向量
     * @return
     */
    @RequestMapping(value = "/wechat/login",method = {RequestMethod.POST,RequestMethod.GET},produces = {"application/json","text/html"})
    public BizPacket weichatLogin(String code, String  encryptedData, String iv,String inviterId){
        if(code == null || code.trim().length() ==0){
            return BizPacket.error(HttpStatus.BAD_REQUEST.value(),"参数code不正确!");
        }
        if(encryptedData == null || encryptedData.trim().length() == 0){
            return BizPacket.error(HttpStatus.BAD_REQUEST.value(),"参数encryptedData不正确!");
        }
        if(iv == null || iv.trim().length() == 0){
           return BizPacket.error(HttpStatus.BAD_REQUEST.value(),"参数iv不正确!");
        }

        if(!StringUtils.isEmpty(inviterId)){
            if(inviterId.trim().length() != 32){
                return BizPacket.error(HttpStatus.BAD_REQUEST.value(),"参数inviterId非法(32位)!");
            }
        }


        logger.info("code={},encryptedData={},iv={},inviterId={}",code,encryptedData,iv,inviterId);

        BizPacket packet =  poiUserService.weichatLogin(code,encryptedData,iv,inviterId);
        return packet;
    }


    /**
     * accessToken登录
     *
     * @param accessToken
     * @return
     */
    @RequestMapping(value = "/wechat/login/token",method = {RequestMethod.POST,RequestMethod.GET},produces = {"application/json","text/html"})
    public BizPacket accessTokenLogin(String accessToken){
        if (StringUtils.isEmpty(accessToken)) {
            return BizPacket.error(HttpStatus.UNAUTHORIZED.value(),"access_token is empty!");
        }

        try {
            PoiUserData user = redisService.getPoiUser(accessToken);
            if (user == null) {
                return BizPacket.error(HttpStatus.UNAUTHORIZED.value(), "user not found or frozen!");
            }

            BizPacket packet = WechatUtil.check(user);
            if(packet.getCode() != HttpStatus.OK.value()){
                return packet;
            }
            WeichatLoginForm form =  poiUserService.buildResponse(user);

            return BizPacket.success(form);

        }catch (Exception ex){
            logger.error(ex.getMessage(),ex);
            return BizPacket.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),ex.getMessage());
        }
    }

    @RequestMapping(value = "/wechat/test",method = {RequestMethod.POST,RequestMethod.GET},produces = {"application/json","text/html"})
    public BizPacket test(String code,String  encryptedData,String iv){
        if(code != null && code.trim().length() != 0) {
            logger.info("code={},encryptedData={},iv={}",code,encryptedData,iv);
            return BizPacket.success("参数有值!code=" + code+",encryptedData="+encryptedData);
        }
        return BizPacket.error_param_null("code参数为空!");
    }

    @RequestMapping(value = "/wechat/login/mobile",method = {RequestMethod.POST,RequestMethod.GET})
    public BizPacket login4mobile(@RequestParam("mobile") String mobile){
        try {
            return poiUserService.login4mobile(mobile.trim());
        } catch (IOException e) {
            return BizPacket.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),e.getMessage());
        }
    }
}