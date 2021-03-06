package com.wmt.wechat.service.poi;

import com.wmt.commons.domain.packet.BizPacket;
import com.wmt.commons.enums.PayWay;
import com.wmt.wechat.model.poi.PoiMemberRDData;
import com.wmt.wechat.model.poi.PoiUserData;

import java.util.Map;

/**
 * Copyright (c) 2019 by CANSHU
 *
 *  店铺会员业务
 * @author adu Create on 2019-01-25 11:27
 * @version 1.0
 */
public interface PoiMemberService {
    /**
     * 会员数据获取
     * @return
     */
    public BizPacket memberDataFetch(PoiUserData userData);


    /**
     * 是否会员新手
     * @param poiId
     * @return true:是新手,false:不是新手
     */
    public boolean memberNewbie(String poiId);

    /**
     * 指定店铺是否会员
     * @param poiId
     * @return
     */
    public boolean isMember(String poiId);



    public BizPacket memberBoughtRD(PoiUserData userData,String orderId);

    /**
     * 会员卡购买记录
     * @param index
     * @param pageSize
     * @return
     */
    public BizPacket memberBoughtRDList(PoiUserData userData,int index,int pageSize);


    /**
     * 购买会员成功,包括:
     * 1)余额付款成功;
     * 2)微信付款成功;
     *
     * @param rd
     * @param payUserId 实际支付人(poiUserId)
     * @return
     */
    public BizPacket onMemberBoughtSucc(PoiMemberRDData rd, String endTime, String payUserId);

    /**
     * 会员购买支付前的预览
     * @param userData
     * @param memberCardId
     * @param feeRenew
     * @return
     */
    public BizPacket memberBuyPreview(PoiUserData userData,int memberCardId,int feeRenew);

    /**
     * 会员购买支付确认
     * @param userData
     * @param orderId
     * @param payWay
     * @return
     * @throws Exception
     */
    public BizPacket memberBuyConfirm(PoiUserData userData, String orderId, PayWay payWay)throws Exception;
    public BizPacket memberBuyCallback(Map<String,String> wechatPayCallbackParams);


    /**
     * 设置是否自动续期
     * @param userData
     * @param feeRenew
     * @return
     */
    public BizPacket feeRenewSet(PoiUserData userData,int feeRenew);


    /**
     * 会员自动续期取消
     *
     * @param userData
     * @return
     */
    public BizPacket autoFeeRenewCencel(PoiUserData userData);



    public BizPacket memberFeedback(PoiUserData userData,String svcQty,String suggestText);
}
