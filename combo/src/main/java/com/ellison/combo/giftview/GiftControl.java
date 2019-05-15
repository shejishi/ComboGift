package com.ellison.combo.giftview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import com.ellison.combo.LiveGiftBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author ellison
 */
public class GiftControl implements LiveGiftLayout.LeftGiftAnimationStatusListener {

    private static final String TAG = "GiftControl";
    protected Context mContext;
    /**
     * 自定义动画
     */
    private IBaseAnim customAnim;
    /**
     * 是否开启隐藏动画
     */
    private boolean isHideMode;
    /**
     * 当前所有礼物动画布局的显示方式
     */
    private int curDisplayMode = FROM_BOTTOM_TO_TOP;
    /**
     * 由下往上
     */
    public static final int FROM_BOTTOM_TO_TOP = 0;
    /**
     * 由上往下
     */
    public static final int FROM_TOP_TO_BOTTOM = 1;
    /**
     * 礼物队列
     */
    private LinkedList<LiveGiftBean> mGiftQueue;
    /**
     * 保存礼物view
     */
    private LinkedList<LiveGiftLayout> mLiveGiftLayouts;

    /**
     * 添加礼物布局的父容器
     */
    private LinearLayout mGiftLayoutParent;
    /**
     * 最大礼物布局数
     */
    private int mGiftLayoutMaxNums;

    public GiftControl(Context context) {
        mContext = context;
        mGiftQueue = new LinkedList<>();
        mLiveGiftLayouts = new LinkedList<>();
    }

    public GiftControl setCustomAnim(IBaseAnim anim) {
        customAnim = anim;
        return this;
    }

    /**
     * @param giftLayoutParent 存放礼物控件的父容器
     * @param giftMaxNum       礼物控件的数量
     * @return
     */
    public GiftControl setGiftLayout(LinearLayout giftLayoutParent, @NonNull int giftMaxNum) {
        if (giftMaxNum <= 0) {
            throw new IllegalArgumentException("GiftFrameLayout数量必须大于0");
        }
        if (giftLayoutParent.getChildCount() > 0) {
            //如果父容器没有子孩子，就进行添加
            return this;
        }
        mGiftLayoutParent = giftLayoutParent;
        mGiftLayoutMaxNums = giftMaxNum;
        LayoutTransition transition = new LayoutTransition();
        transition.setAnimator(LayoutTransition.CHANGE_APPEARING,/*change_appearing */ transition.getAnimator(LayoutTransition.CHANGE_APPEARING));
        transition.setAnimator(LayoutTransition.APPEARING, transition.getAnimator(LayoutTransition.APPEARING));
        transition.setAnimator(LayoutTransition.DISAPPEARING, transition.getAnimator(LayoutTransition.CHANGE_APPEARING));
        transition.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, transition.getAnimator(LayoutTransition.DISAPPEARING));
        mGiftLayoutParent.setLayoutTransition(transition);

        return this;
    }

    /**
     * 是否开启隐藏动画
     *
     * @param isHideMode
     * @return
     */
    public GiftControl setHideMode(boolean isHideMode) {
        this.isHideMode = isHideMode;
        return this;
    }

    /**
     * 当前所有礼物动画布局的显示方式
     *
     * @param displayMode {@link #FROM_BOTTOM_TO_TOP}、{@link #FROM_TOP_TO_BOTTOM}
     * @return
     */
    public GiftControl setDisplayMode(int displayMode) {
        this.curDisplayMode = displayMode;
        return this;
    }

    public void loadGift(LiveGiftBean gift) {
        loadGift(gift, false, false);
    }

    public void loadGiftFirst(LiveGiftBean giftBean) {
        loadGift(giftBean, false, true);
    }

    /**
     * 加入礼物，具有实时连击效果
     *
     * @param gift
     * @param supportCombo 是否支持实时连击，如果为true：支持，否则不支持
     */
    public synchronized void loadGift(LiveGiftBean gift, boolean supportCombo, boolean isFirst) {
        if (mGiftQueue != null) {
            if (supportCombo) {
                Iterator<LiveGiftLayout> liveGiftLayoutIterator = mLiveGiftLayouts.iterator();
                while (liveGiftLayoutIterator.hasNext()) {
                    // 获取到队列中的view
                    LiveGiftLayout giftLayout = liveGiftLayoutIterator.next();
                    // 判断是否在之前存在
                    if (giftLayout.getCurrentGiftId().equals(gift.getGiftId()) && giftLayout.getCurrentSendUserId().equals(gift.getSendUserId())) {
                        if (giftLayout.isRemove()) {
                            // 去掉 消失的回调  整条数据是没用的了
                            giftLayout.removeDismissGiftCallback();
                            // 被顶出去了
                            liveGiftLayoutIterator.remove();

                            gift.setGiftCount(giftLayout.getGiftCount() + 1);
                            showGift(gift);
                        } else {
                            //连击
                            giftLayout.setGiftCount(gift.getGiftCount());
                            giftLayout.setSendGiftTime(gift.getSendGiftTime());
                        }
                        return;
                    }
                }
                showGift(gift);
            } else {
                addGiftQueue(gift, isFirst);
            }
        }
    }

    private void addGiftQueue(final LiveGiftBean gift, boolean isFirst) {
        String userId = "";
        if (mGiftQueue != null) {
            if (mGiftQueue.size() == 0) {
                Log.d(TAG, "剩余个数：" + mGiftQueue.size() + ", 礼物：" + gift.getGiftId());
                if (isFirst) {
                    // 自己送的放前面
                    mGiftQueue.addFirst(gift);
                } else {
                    // 别人的放后面  不包括自己的
                    if (!userId.equals(gift.getSendUserId())) {
                        mGiftQueue.addLast(gift);
                    }
                }
                showGift();
                return;
            }
            Log.d(TAG, "剩余个数：" + mGiftQueue.size() + ",礼物：" + gift.getGiftId());
            if (isFirst) {
                mGiftQueue.addFirst(gift);
            } else {
                if (!userId.equals(gift.getSendUserId())) {
                    mGiftQueue.addLast(gift);
                }
            }
        }

    }

    private synchronized void showGift(LiveGiftBean giftBean) {
        if (giftBean == null) {
            return;
        }
        LiveGiftLayout giftLayout;
        int childCount = mGiftLayoutParent.getChildCount();
        Log.d(TAG, "showGift: 礼物布局的个数" + childCount);
        if (childCount == mGiftLayoutMaxNums) {
            LiveGiftLayout firstGiftLayout = (LiveGiftLayout) mGiftLayoutParent.getChildAt(0);
            firstGiftLayout.setRemove(true);
            // 移除第一个
            mGiftLayoutParent.removeViewAt(0);
        }
        //没有超过最大的礼物布局数量，可以继续添加礼物布局
        giftLayout = new LiveGiftLayout(mContext);
        giftLayout.setIndex(0);
//            LiveGiftLayout.firstHideLayout();
        giftLayout.setGiftAnimationListener(this);
        if (curDisplayMode == FROM_BOTTOM_TO_TOP) {
            //两个参数分别是layout_width,layout_height
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mGiftLayoutParent.getLayoutParams();
            //这个就是添加其他属性的，这个是在父元素的底部。
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mGiftLayoutParent.addView(giftLayout);
        } else if (curDisplayMode == FROM_TOP_TO_BOTTOM) {
            //两个参数分别是layout_width,layout_height
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mGiftLayoutParent.getLayoutParams();
            //这个就是添加其他属性的，这个是在父元素的底部。
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            mGiftLayoutParent.addView(giftLayout, 0);
        } else {//默认由下往上
            //两个参数分别是layout_width,layout_height
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mGiftLayoutParent.getLayoutParams();
            //这个就是添加其他属性的，这个是在父元素的底部。
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mGiftLayoutParent.addView(giftLayout);
        }
        boolean hasGift = giftLayout.setGift(giftBean);
        if (hasGift) {
            giftLayout.startAnimation(customAnim);
            // 添加到view队列中
            mLiveGiftLayouts.add(giftLayout);
        }
    }

    /**
     * 显示礼物
     * synchronized 阻塞队列
     */
    private synchronized void showGift() {
        if (isEmpty()) {
            return;
        }
        LiveGiftLayout giftLayout;
        int childCount = mGiftLayoutParent.getChildCount();
        Log.d(TAG, "showGift: 礼物布局的个数" + childCount);
        if (childCount == mGiftLayoutMaxNums) {
            LiveGiftLayout firstGiftLayout = (LiveGiftLayout) mGiftLayoutParent.getChildAt(0);
            firstGiftLayout.setRemove(true);
            // 移除第一个
            mGiftLayoutParent.removeViewAt(0);
        }
        //没有超过最大的礼物布局数量，可以继续添加礼物布局
        giftLayout = new LiveGiftLayout(mContext);
        giftLayout.setIndex(0);
//            LiveGiftLayout.firstHideLayout();
        giftLayout.setGiftAnimationListener(this);
        if (curDisplayMode == FROM_BOTTOM_TO_TOP) {
            //两个参数分别是layout_width,layout_height
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mGiftLayoutParent.getLayoutParams();
            //这个就是添加其他属性的，这个是在父元素的底部。
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mGiftLayoutParent.addView(giftLayout);
        } else if (curDisplayMode == FROM_TOP_TO_BOTTOM) {
            //两个参数分别是layout_width,layout_height
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mGiftLayoutParent.getLayoutParams();
            //这个就是添加其他属性的，这个是在父元素的底部。
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            mGiftLayoutParent.addView(giftLayout, 0);
        } else {//默认由下往上
            //两个参数分别是layout_width,layout_height
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mGiftLayoutParent.getLayoutParams();
            //这个就是添加其他属性的，这个是在父元素的底部。
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            mGiftLayoutParent.addView(giftLayout);
        }
        boolean hasGift = giftLayout.setGift(getGift());
        if (hasGift) {
            giftLayout.startAnimation(customAnim);
        }
    }

    /**
     * 取出礼物
     *
     * @return
     */
    private synchronized LiveGiftBean getGift() {
        LiveGiftBean gift = null;
        if (mGiftQueue.size() != 0) {
            gift = mGiftQueue.get(0);
            mGiftQueue.remove(0);
            Log.i(TAG, "getGift---集合个数：" + mGiftQueue.size() + ",送出礼物---" + gift.getGiftId() + ",礼物数X" + gift.getGiftCount());
        }
        return gift;
    }

    /**
     * 通过获取giftId和getSendUserId当前用户giftId礼物总数
     *
     * @param giftId
     * @param userId
     * @return
     */
    public int getCurGiftCountByUserId(String giftId, String userId) {
        int curGiftCount = 0;
        LiveGiftLayout giftLayout;
        LiveGiftBean giftBean;
        for (int i = 0; i < mGiftLayoutParent.getChildCount(); i++) {
            giftLayout = (LiveGiftLayout) mGiftLayoutParent.getChildAt(i);
            giftBean = giftLayout.getGift();
            if (giftBean != null && giftBean.getGiftId().equals(giftId) && giftBean.getSendUserId().equals(userId)) {
                curGiftCount = giftBean.getGiftCount();
            } else {//自己的礼物不正在显示，还在队列中
                Iterator<LiveGiftBean> iterator = mGiftQueue.iterator();
                while (iterator.hasNext()) {
                    giftBean = iterator.next();
                    if (giftBean.getGiftId().equals(giftId) && giftBean.getSendUserId().equals(userId)) {
                        curGiftCount = giftBean.getGiftCount();
                        break;
                    }
                }
            }
        }
        return curGiftCount;
    }

    /**
     * 获取正在展示礼物的个数（即GiftFragmeLayout展示的个数）
     *
     * @return
     */
    public int getShowingGiftLayoutCount() {
        int count = 0;
        LiveGiftLayout giftLayout;
        for (int i = 0; i < mGiftLayoutParent.getChildCount(); i++) {
            giftLayout = (LiveGiftLayout) mGiftLayoutParent.getChildAt(i);
            if (giftLayout.isShowing()) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获取正在展示礼物的个数实例（即GiftFragmeLayout展示的个数实例）
     *
     * @return
     */
    public List<LiveGiftLayout> getShowingGiftLayouts() {
        List<LiveGiftLayout> giftLayoutList = new ArrayList<>();
        LiveGiftLayout giftLayout;
        for (int i = 0; i < mGiftLayoutParent.getChildCount(); i++) {
            giftLayout = (LiveGiftLayout) mGiftLayoutParent.getChildAt(i);
            if (giftLayout.isShowing()) {
                giftLayoutList.add(giftLayout);
            }
        }
        return giftLayoutList;
    }

    @Override
    public void dismiss(LiveGiftLayout giftLayout) {
        if (giftLayout != null) {
            reStartAnimation(giftLayout, giftLayout.getIndex());
        }
    }

    /**
     * 从列表中移除数据
     *
     * @param giftLayout
     */
    private synchronized void removeLayout(LiveGiftLayout giftLayout) {
        if (mLiveGiftLayouts != null) {
            Iterator<LiveGiftLayout> iterator = mLiveGiftLayouts.iterator();
            while (iterator.hasNext()) {
                LiveGiftLayout next = iterator.next();
                if (next.getCurrentGiftId().equals(giftLayout.getCurrentGiftId()) && next.getCurrentSendUserId().equals(giftLayout.getCurrentSendUserId()) && !next.isShowing()) {
                    giftLayout.removeDismissGiftCallback();
                    iterator.remove();
                    Log.i(TAG, "移除数据：" + next.toString() + " 列表个数：" + mLiveGiftLayouts.size());
                    return;
                }
            }
        }
    }

    private void reStartAnimation(final LiveGiftLayout giftLayout, final int index) {
        //动画结束，这时不能触发连击动画
        giftLayout.setCurrentShowStatus(false);
        Log.d(TAG, "reStartAnimation: 动画结束");
        AnimatorSet animatorSet = giftLayout.endAnimation(customAnim);
        if (animatorSet != null) {
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Log.i(TAG, "礼物动画dismiss: index = " + index);
                    //动画完全结束
                    giftLayout.CurrentEndStatus(true);
                    giftLayout.setGiftViewEndVisibility(isEmpty());
                    mGiftLayoutParent.removeView(giftLayout);

                    // 从列表中移除数据
                    removeLayout(giftLayout);
                    showGift();
                }
            });
        }
    }

    public GiftControl reSetGiftLayout(LinearLayout giftLayoutParent, @NonNull int giftLayoutNums) {
        return setGiftLayout(giftLayoutParent, giftLayoutNums);
    }

    /**
     * 清除所有礼物
     */
    public synchronized void cleanAll() {
        if (mGiftQueue != null) {
            mGiftQueue.clear();
        }
        if (mLiveGiftLayouts != null) {
            mLiveGiftLayouts.clear();
        }
        LiveGiftLayout giftLayout;
        for (int i = 0; i < mGiftLayoutParent.getChildCount(); i++) {
            giftLayout = (LiveGiftLayout) mGiftLayoutParent.getChildAt(i);
            if (giftLayout != null) {
                giftLayout.clearHandler();
                giftLayout.firstHideLayout();
            }
        }
        mGiftLayoutParent.removeAllViews();
    }

    /**
     * 礼物是否为空
     *
     * @return
     */
    public synchronized boolean isEmpty() {
        if (mGiftQueue == null || mGiftQueue.size() == 0 || mGiftLayoutParent == null) {
            return true;
        } else {
            return false;
        }
    }
}
