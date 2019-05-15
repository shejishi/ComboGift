package com.ellison.combo;

import android.support.annotation.DrawableRes;
import android.text.TextUtils;

/**
 * @author ellison
 */
public class LiveGiftBean {

    //礼物的id
    private String giftId;
    //礼物的名字
    private String giftName;
    //一次发送礼物的数量
    private int giftCount;
    //礼物的图片
    private @DrawableRes int giftPic;
    //礼物的价格
    private String giftPrice;
    //发送者的id
    private String sendUserId;
    //发送者的名字
    private String sendUserName;
    //发送者的头像
    private @DrawableRes int sendUserPic;
    // 接受者的名字
    private String recevierUserName;
    //上一次要连击的礼物数
    private int hitCombo;
    //发送礼物的时间
    private Long sendGiftTime;
    //是否从当前数开始连击
    private boolean currentStart;
    //礼物特效路径，有路径就播放特效礼物，烟花等前期没有配置，使用以前逻辑
    private String effectUrl;

    private boolean isShowing;  // 是否正在显示

    public LiveGiftBean() {
    }

    public String getEffectUrl() {
        return effectUrl;
    }

    public LiveGiftBean setEffectUrl(String effectUrl) {
        this.effectUrl = effectUrl;
        return this;
    }

    public String getGiftId() {
        return giftId;
    }

    public LiveGiftBean setGiftId(String giftId) {
        this.giftId = giftId;
        return this;
    }

    public String getGiftName() {
        return giftName;
    }

    public LiveGiftBean setGiftName(String giftName) {
        this.giftName = giftName;
        return this;
    }

    public int getGiftCount() {
        return giftCount;
    }

    public LiveGiftBean setGiftCount(int giftCount) {
        this.giftCount = giftCount;
        return this;
    }

    public String getSendUserId() {
        return sendUserId;
    }

    public LiveGiftBean setSendUserId(String sendUserId) {
        this.sendUserId = sendUserId;
        return this;
    }

    public String getSendUserName() {
        return sendUserName;
    }

    public LiveGiftBean setSendUserName(String sendUserName) {
        this.sendUserName = sendUserName;
        return this;
    }

    public int getSendUserPic() {
        return sendUserPic;
    }

    public LiveGiftBean setSendUserPic(int sendUserPic) {
        this.sendUserPic = sendUserPic;
        return this;
    }

    public String getRecevierUserName() {
        return TextUtils.isEmpty(recevierUserName) ? "" : recevierUserName;
    }

    public LiveGiftBean setRecevierUserName(String recevierUserName) {
        this.recevierUserName = recevierUserName;
        return this;
    }

    public int getGiftPic() {
        return giftPic;
    }

    public LiveGiftBean setGiftPic(int giftPic) {
        this.giftPic = giftPic;
        return this;
    }

    public String getGiftPrice() {
        return giftPrice;
    }

    public LiveGiftBean setGiftPrice(String giftPrice) {
        this.giftPrice = giftPrice;
        return this;
    }

    public int getHitCombo() {
        return hitCombo;
    }

    public LiveGiftBean setHitCombo(int hitCombo) {
        this.hitCombo = hitCombo;
        return this;
    }

    public Long getSendGiftTime() {
        return sendGiftTime;
    }

    public LiveGiftBean setSendGiftTime(Long sendGiftTime) {
        this.sendGiftTime = sendGiftTime;
        return this;
    }

    public boolean isCurrentStart() {
        return currentStart;
    }

    public LiveGiftBean setCurrentStart(boolean currentStart) {
        this.currentStart = currentStart;
        return this;
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void setShowing(boolean showing) {
        isShowing = showing;
    }
}
