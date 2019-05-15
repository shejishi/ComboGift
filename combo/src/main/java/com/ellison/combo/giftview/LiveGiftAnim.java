package com.ellison.combo.giftview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.ellison.combo.R;
import com.ellison.combo.utils.DensityUtil;
import com.ellison.combo.utils.LiveGiftAnimationUtil;


/**
 * @author ellison
 */

public class LiveGiftAnim implements IBaseAnim {

    @Override
    public AnimatorSet startAnim(final LiveGiftLayout giftLayout, View rootView) {
        //礼物飞入
        ObjectAnimator leftToRight1 = LiveGiftAnimationUtil.createFlyFromLtoR(giftLayout, -DensityUtil.dip2px(rootView.getContext(), 260.0F), 0, 500L, new DecelerateInterpolator());
        ObjectAnimator leftToRight2 = LiveGiftAnimationUtil.createFlyFromLtoR(giftLayout, 0, -DensityUtil.dip2px(rootView.getContext(), 10.0F), 60L, new DecelerateInterpolator());
        ObjectAnimator leftToRight3 = LiveGiftAnimationUtil.createFlyFromLtoR(giftLayout, -DensityUtil.dip2px(rootView.getContext(), 10.0F), 0, 60L, new DecelerateInterpolator());
        leftToRight1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                giftLayout.comboAnimation(true);
            }
        });
        return LiveGiftAnimationUtil.startAnimation(leftToRight1, leftToRight2, leftToRight3);
    }

    @Override
    public AnimatorSet comboAnim(final LiveGiftLayout giftLayout, View rootView, boolean isFirst) {
        final TextView anim_num = rootView.findViewById(R.id.item_live_voice_gift_tv_num);
        if (isFirst) {
            anim_num.setVisibility(View.VISIBLE);
//            anim_num.setText("x " + giftLayout.getCombo());
            giftLayout.comboEndAnim();//这里一定要回调该方法，不然连击不会生效
        } else {
            //数量增加
            ObjectAnimator scaleGiftNum = LiveGiftAnimationUtil.scaleGiftNum(anim_num);
            scaleGiftNum.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    giftLayout.comboEndAnim();//这里一定要回调该方法，不然连击不会生效
                }
            });
            scaleGiftNum.start();
        }
        return null;
    }

    @Override
    public AnimatorSet endAnim(final LiveGiftLayout giftLayout, View rootView) {
        //向上渐变消失
//        ObjectAnimator fadeAnimator = LiveGiftAnimationUtil.createFadeAnimator(giftLayout, 0, 0, 1500, 0);
//        // 复原
//        ObjectAnimator fadeAnimator2 = LiveGiftAnimationUtil.createFadeAnimator(giftLayout, 0, 0, 0, 0);
//        AnimatorSet animatorSet = LiveGiftAnimationUtil.startAnimation(fadeAnimator, fadeAnimator2);
//        return animatorSet;
        return testAnim(giftLayout);
    }

    @NonNull
    private AnimatorSet testAnim(LiveGiftLayout giftLayout) {
        PropertyValuesHolder translationY = PropertyValuesHolder.ofFloat("translationY", 0, -DensityUtil.dip2px(giftLayout.getContext(), 50.0F));
        PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0f);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(giftLayout, translationY, alpha);
        animator.setStartDelay(0);
        animator.setDuration(500);

//        translationY = PropertyValuesHolder.ofFloat("translationY", -50, -100);
//        alpha = PropertyValuesHolder.ofFloat("alpha", 0.5f, 0f);
//        ObjectAnimator animator1 = ObjectAnimator.ofPropertyValuesHolder(giftLayout, translationY, alpha);
//        animator1.setStartDelay(0);
//        animator1.setDuration(500);

        // 复原
//        ObjectAnimator fadeAnimator2 = LiveGiftAnimationUtil.createFadeAnimator(LiveGiftLayout, 0, 0, 0, 0);

        AnimatorSet animatorSet = new AnimatorSet();
//        animatorSet.play(animator1).after(animator);
//        animatorSet.play(fadeAnimator2).after(animator1);
        animatorSet.play(animator);
        animatorSet.start();
        return animatorSet;
    }
}
