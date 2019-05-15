package com.ellison.combo.giftview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.AttrRes;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ellison.combo.LiveGiftBean;
import com.ellison.combo.R;
import com.ellison.combo.utils.LiveGiftAnimationUtil;


/**
 * @author ellison
 */
public class LiveGiftLayout extends FrameLayout implements Handler.Callback {

    private static final String TAG = "LiveGiftLayout";
    private LayoutInflater mInflater;
    private Context mContext;
    /**
     * 连击handler
     */
    private Handler mHandler = new Handler(this);
    /**
     * 检查连击handler
     */
    private Handler comboHandler = new Handler(this);
    /**
     * 礼物展示时间
     */
    public static final int GIFT_DISMISS_TIME = 6000;
    /**
     * 当前动画runnable
     */
    private Runnable mCurrentAnimRunnable;

    RelativeLayout mGiftItemContent;
    ImageView mIvGift, mIvSenderHeader;
    TextView mTvSenderName, mTvSenderInfo;
    TextView mTvGiftNum;

    private LiveGiftBean mGift;
    /**
     * item 显示位置
     */
    private int mIndex = 1;
    /**
     * 礼物连击数
     */
    private int mGiftCount;
    /**
     * 当前播放连击数
     */
    private int mCombo = 1;
    /**
     * 礼物动画正在显示，在这期间可触发连击效果
     */
    private boolean isShowing = false;
    /**
     * 礼物动画结束
     */
    private boolean isEnd = true;
    /**
     * 自定义动画的接口
     */
    private IBaseAnim anim;

    private LeftGiftAnimationStatusListener mGiftAnimationListener;
    private View rootView;

    /**
     * 当前是否被移除
     */
    private boolean mIsRemove;

    public LiveGiftLayout(Context context) {
        this(context, null);
    }

    public LiveGiftLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveGiftLayout(Context context, AttributeSet attributeSet, @AttrRes int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);

        mInflater = LayoutInflater.from(context);
        mContext = context;
        initView();
    }

    private void initView() {
        rootView = mInflater.inflate(R.layout.item_live_voice_gift, null);
        mGiftItemContent = rootView.findViewById(R.id.item_live_voice_gift_content);
        mIvGift = rootView.findViewById(R.id.item_live_voice_gift_iv_gift);
        mTvGiftNum = rootView.findViewById(R.id.item_live_voice_gift_tv_num);
        mIvSenderHeader = rootView.findViewById(R.id.item_live_voice_gift_iv_header);
        mTvSenderName = rootView.findViewById(R.id.item_live_voice_gift_tv_nickname);
        mTvSenderInfo = rootView.findViewById(R.id.item_live_voice_gift_tv_info);

        this.addView(rootView/*, new LayoutParams(DensityUtil.dip2px(260.0F), LayoutParams.WRAP_CONTENT)*/);
    }

    public void firstHideLayout() {
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0f);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(LiveGiftLayout.this, alpha);
        animator.setStartDelay(0);
        animator.setDuration(0);
        animator.start();
    }

    public void hideView() {
        mIvGift.setVisibility(INVISIBLE);
        mTvGiftNum.setVisibility(INVISIBLE);
    }

    public void setGiftViewEndVisibility(boolean hasGift) {
        if (hasGift) {
            LiveGiftLayout.this.setVisibility(View.GONE);
        } else {
            LiveGiftLayout.this.setVisibility(View.INVISIBLE);
        }
    }

    public boolean setGift(LiveGiftBean gift) {
        if (gift == null) {
            return false;
        }
        mGift = gift;

        if (mGift.isCurrentStart()) {
            mGiftCount = gift.getGiftCount() + mGift.getHitCombo();
        } else {
            mGiftCount = gift.getGiftCount();
        }
        if (!TextUtils.isEmpty(gift.getSendUserName())) {
            mTvSenderName.setText(gift.getSendUserName());
        }
        if (!TextUtils.isEmpty(gift.getGiftId())) {
            mTvSenderInfo.setText(gift.getGiftName());
        }
        // 设置头像
        mIvSenderHeader.setImageDrawable(ContextCompat.getDrawable(mContext, gift.getSendUserPic()));
        // 设置名字
        String firstText = "送 ";
        String beforeColor = "#2d2d2d";
        String afterColor = "#ff3a72";

        //创建SpannableStringBuilder，并添加前面文案
        SpannableStringBuilder builder = new SpannableStringBuilder(firstText);
        //设置前面的字体颜色
        builder.setSpan(new ForegroundColorSpan(Color.parseColor(beforeColor)), 0, firstText.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        //追加后面文案
        builder.append(gift.getRecevierUserName());
        //设置后面的字体颜色
        builder.setSpan(new ForegroundColorSpan(Color.parseColor(afterColor)), firstText.length(), builder.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        mTvSenderInfo.setText(builder);

        // 设置礼物图片
        mIvGift.setImageDrawable(ContextCompat.getDrawable(mContext, gift.getGiftPic()));

        mTvGiftNum.setText("x " + (mGiftCount));
        mCombo = mGiftCount;
        return true;
    }

    public LiveGiftBean getGift() {
        return mGift;
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    public void setRemove(boolean isRemove) {
        mIsRemove = isRemove;
    }

    public boolean isRemove() {
        return mIsRemove;
    }

    private class GiftNumAnimaRunnable implements Runnable {
        @Override
        public void run() {
            dismissGiftLayout();
        }
    }

    /**
     * 增加礼物数量,用于连击效果
     *
     * @param count
     */
    public synchronized void setGiftCount(int count) {
        mGiftCount += count;
        mGift.setGiftCount(mGiftCount);
        ++mCombo;

        mTvGiftNum.setText("x " + (mCombo));
        comboAnimation(false);
        removeDismissGiftCallback();
    }

    /**
     * 显示完连击数与动画时,关闭此Item Layout,并通知外部隐藏自身(供内部调用)
     */
    private synchronized void dismissGiftLayout() {
        removeDismissGiftCallback();
        if (mGiftAnimationListener != null) {
            mGiftAnimationListener.dismiss(this);
        }
    }

    /**
     * 移除掉消失的 runnable
     */
    public synchronized void removeDismissGiftCallback() {
        stopCheckGiftCount();
        if (mCurrentAnimRunnable != null) {
            mHandler.removeCallbacks(mCurrentAnimRunnable);
            mCurrentAnimRunnable = null;
        }
    }

    /**
     * 连击结束时回调
     */
    public synchronized void comboEndAnim() {
        if (mHandler != null) {
            if (mCurrentAnimRunnable == null) {
                mCurrentAnimRunnable = new GiftNumAnimaRunnable();
                mHandler.postDelayed(mCurrentAnimRunnable, GIFT_DISMISS_TIME);
            }
        }
    }

    /**
     * 移除掉实时监测
     */
    public void stopCheckGiftCount() {
        comboHandler.removeCallbacksAndMessages(null);
    }


    /**
     * 获取当前显示礼物发送人id
     *
     * @return
     */
    public String getCurrentSendUserId() {
        if (mGift != null) {
            return mGift.getSendUserId();
        }
        return null;
    }

    /**
     * 获取当前显示礼物id
     *
     * @return
     */
    public String getCurrentGiftId() {
        if (mGift != null) {
            return mGift.getGiftId();
        }
        return null;
    }

    public int getGiftCount() {
        return mGiftCount;
    }

    public synchronized void setSendGiftTime(long sendGiftTime) {
        mGift.setSendGiftTime(sendGiftTime);
    }


    /**
     * 设置item显示位置
     *
     * @param mIndex
     */
    public void setIndex(int mIndex) {
        this.mIndex = mIndex;
    }

    /**
     * 获取ite显示位置
     *
     * @return
     */
    public int getIndex() {
        Log.i(TAG, "index : " + mIndex);
        return mIndex;
    }

    public void setGiftAnimationListener(LeftGiftAnimationStatusListener giftAnimationListener) {
        this.mGiftAnimationListener = giftAnimationListener;
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void setCurrentShowStatus(boolean status) {
        mCombo = 0;
        isShowing = status;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void CurrentEndStatus(boolean isEnd) {
        this.isEnd = isEnd;
    }

    public long getSendGiftTime() {
        return mGift.getSendGiftTime();
    }

    public boolean isCurrentStart() {
        return mGift.isCurrentStart();
    }

    public void setCurrentStart(boolean currentStart) {
        mGift.setCurrentStart(currentStart);
    }

    public int getCombo() {
        return mCombo;
    }

    public void clearHandler() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            //这里要置位null，否则当前页面销毁时，正在执行的礼物动画会造成内存泄漏
            mHandler = null;
        }

        mGiftAnimationListener = null;

        if (comboHandler != null) {
            comboHandler.removeCallbacksAndMessages(null);
            //这里要置位null，否则当前页面销毁时，正在执行的礼物动画会造成内存泄漏
            comboHandler = null;
        }
        resetGift();
    }

    public void resetGift() {
        mCurrentAnimRunnable = null;
        mIndex = -1;
        mGiftCount = 0;
        mCombo = 0;
        isShowing = false;
        isEnd = true;
    }

    public AnimatorSet startAnimation(IBaseAnim anim) {
        this.anim = anim;
        if (anim == null) {
            hideView();
            //布局飞入
            ObjectAnimator flyFromLtoR = LiveGiftAnimationUtil.createFlyFromLtoR(mGiftItemContent, -getWidth(), 0, 400L, new OvershootInterpolator());
            flyFromLtoR.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                }
            });

            //礼物飞入
            ObjectAnimator flyFromLtoR2 = LiveGiftAnimationUtil.createFlyFromLtoR(mIvGift, -getWidth(), 0, 500L, new DecelerateInterpolator());
            flyFromLtoR2.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mIvGift.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    comboAnimation(true);
                }
            });

            return LiveGiftAnimationUtil.startAnimation(flyFromLtoR, flyFromLtoR2);
        } else {
            return anim.startAnim(this, rootView);
        }
    }

    public void comboAnimation(boolean isFirst) {
        if (anim == null) {
            if (isFirst) {
                mTvGiftNum.setVisibility(View.VISIBLE);
                mTvGiftNum.setText("x " + mCombo);
                Log.d(TAG, "comboAnimation 连击：" + mCombo);
                comboEndAnim();
            } else {
                //数量增加
                ObjectAnimator scaleGiftNum = LiveGiftAnimationUtil.scaleGiftNum(mTvGiftNum);
                scaleGiftNum.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        comboEndAnim();
                    }
                });
                scaleGiftNum.start();
            }
        } else {
            anim.comboAnim(this, rootView, isFirst);
        }
    }

    /**
     * 动画结束
     *
     * @param anim
     * @return
     */
    public AnimatorSet endAnimation(IBaseAnim anim) {
        if (anim == null) {
            //向上渐变消失
            ObjectAnimator fadeAnimator = LiveGiftAnimationUtil.createFadeAnimator(LiveGiftLayout.this, 0, -100, 500, 0);
            fadeAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    mTvGiftNum.setVisibility(View.INVISIBLE);
                }
            });
            // 复原
            ObjectAnimator fadeAnimator2 = LiveGiftAnimationUtil.createFadeAnimator(LiveGiftLayout.this, 100, 0, 0, 0);

            AnimatorSet animatorSet = LiveGiftAnimationUtil.startAnimation(fadeAnimator, fadeAnimator2);
            return animatorSet;
        } else {
            return anim.endAnim(this, rootView);
        }
    }

    public interface LeftGiftAnimationStatusListener {
        void dismiss(LiveGiftLayout liveGiftLayout);
    }

    @Override
    public String toString() {
        return "LiveGiftLayout{" +
                "mCurrentSendUserId=" + getGift().getSendUserId() +
                " mCurrentGiftId=" + getGift().getGiftId() +
                '}';
    }
}
