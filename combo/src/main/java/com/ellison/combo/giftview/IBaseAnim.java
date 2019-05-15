package com.ellison.combo.giftview;

import android.animation.AnimatorSet;
import android.view.View;

/**
 * @author Ellison
 */

public interface IBaseAnim {
    /**
     * 开始动画
     *
     * @param liveGiftLayout
     * @param rootView
     * @return
     */
    AnimatorSet startAnim(LiveGiftLayout liveGiftLayout, View rootView);

    /**
     * 连击动画
     *
     * @param liveGiftLayout
     * @param rootView
     * @param isFirst
     * @return
     */
    AnimatorSet comboAnim(LiveGiftLayout liveGiftLayout, View rootView, boolean isFirst);

    /**
     * 结束动画
     *
     * @param liveGiftLayout
     * @param rootView
     * @return
     */
    AnimatorSet endAnim(LiveGiftLayout liveGiftLayout, View rootView);
}
