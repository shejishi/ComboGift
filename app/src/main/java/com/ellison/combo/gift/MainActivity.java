package com.ellison.combo.gift;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.ellison.combo.LiveGiftBean;
import com.ellison.combo.giftview.GiftControl;
import com.ellison.combo.giftview.LiveGiftAnim;

public class MainActivity extends AppCompatActivity {

    private GiftControl mGiftControl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGiftControl = new GiftControl(this);
        mGiftControl.setGiftLayout((LinearLayout) findViewById(R.id.live_gift_layout_content), 2)
                .setHideMode(false)
                .setCustomAnim(new LiveGiftAnim());
    }

    public void send1(View view) {
        LiveGiftBean giftBean = new LiveGiftBean();
        giftBean.setGiftId("1")
                .setGiftName("大傻逼")
                .setGiftCount(1)
                .setGiftPic(R.drawable.gift1)
                .setSendUserId("1")
                .setSendUserName("小傻逼")
                .setRecevierUserName("1——1")
                .setSendUserPic(R.drawable.header11)
                .setSendGiftTime(System.currentTimeMillis())
                .setCurrentStart(false)
                .setGiftCount(1)
                .setEffectUrl("");
        mGiftControl.loadGift(giftBean, true, false);
    }

    public void send2(View view) {
        LiveGiftBean giftBean = new LiveGiftBean();
        giftBean.setGiftId("2")
                .setGiftName("笨鸟")
                .setGiftCount(1)
                .setGiftPic(R.drawable.gift2)
                .setSendUserId("2")
                .setSendUserName("婷婷")
                .setRecevierUserName("小肥牛")
                .setSendUserPic(R.drawable.header22)
                .setSendGiftTime(System.currentTimeMillis())
                .setCurrentStart(false)
                .setGiftCount(1)
                .setEffectUrl("");
        mGiftControl.loadGift(giftBean, true, false);

    }

    public void send3(View view) {
        LiveGiftBean giftBean = new LiveGiftBean();
        giftBean.setGiftId("3")
                .setGiftName("飞机")
                .setGiftCount(1)
                .setGiftPic(R.drawable.gift3)
                .setSendUserId("3")
                .setSendUserName("设计师")
                .setRecevierUserName("哈哈哈")
                .setSendUserPic(R.drawable.header33)
                .setSendGiftTime(System.currentTimeMillis())
                .setCurrentStart(false)
                .setGiftCount(1)
                .setEffectUrl("");
        mGiftControl.loadGift(giftBean, true, false);
    }
}
