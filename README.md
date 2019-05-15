>产品需求：
    1、 有暴击，暴击的时长为5秒
    2、 有数据需要无限往里面添加，主播需要实时看到送礼物的情况
    3、 当礼物被后面的数据顶上去时，如果有暴击，重新进入，然后叠加
    3、当动画进入后才显示后面的数据`(暂未实现)`

###### 实现效果：
![礼物暴击效果](https://upload-images.jianshu.io/upload_images/2158207-5974e4e3bc45c076.gif?imageMogr2/auto-orient/strip)

大致的效果图如上，一般直播项目中使用的礼物框架都是类似的实现方式，不过有些像 `*爱网`的动画是有一个队列的形式，而且没有暴击，当用户赠送的礼物过多时，会出现礼物不断的出现在上面；还有像`*鱼直播App`实现的暴击和上面的类似，不过当礼物推上去之后是不会重新暴击的；我们的产品暴击时间比较长，目前的实现方式参考了市面上多个`APP`的形式。

下面，我们对该需求进行拆分：

## 一、对象创建
简单的分析需求后，我们大致可以将该动画分为三个对象：
```
1、 礼物管理类，
    
2、 礼物布局类

3、 动画类
```

##### 礼物管理类

该对象中，应该保存所有的礼物布局对象，控制其添加、删除；

##### 礼物布局类
布局当然是显示当前礼物的数据：赠送人的头像、名字、接收人的名字、礼物的图片、暴击的数量等，礼物布局还有一个隐藏的功能是自身的暴击时间和隐藏的时间控制；当然，这两个时间是一样的，当有暴击的时候，取消隐藏的时间，然后暴击结束后又开始隐藏时间的倒计时。

##### 动画类
控制布局的显示、隐藏、暴击的动画

## 二、 礼物管理 `GiftControl.java`

##### 2.1 添加动态参数

首先，我们需要创建一个`GiftControl`，添加大致的框架方法，不需要设置为单例类，因为单例引用可能导致界面的内存泄漏，所以我们直接设置构造方法传`Context`进入即可：
```
public class GiftControl {
  public GiftControl(Context context) {
      mContext = context;
  }
}
```
然后，作为礼物布局的存放管理者，还需要有一个布局容器来添加，所以，还需要从页面中传入一个`ViewGroup`到里面，布局容器中还需要有一个礼物的数量，比如我上面最大的显示数量为2，这些都可以动态控制，所以增加一个方法设置这两个参数：
```
public class GiftControl {
  public GiftControl(Context context) {
      mContext = context;
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
      transition.setAnimator(LayoutTransition.CHANGE_APPEARING,
                transition.getAnimator(LayoutTransition.CHANGE_APPEARING));
      transition.setAnimator(LayoutTransition.APPEARING, 
                transition.getAnimator(LayoutTransition.APPEARING));
      transition.setAnimator(LayoutTransition.DISAPPEARING, 
                transition.getAnimator(LayoutTransition.CHANGE_APPEARING));
      transition.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, 
                transition.getAnimator(LayoutTransition.DISAPPEARING));
      mGiftLayoutParent.setLayoutTransition(transition);
      return this;
  }
}
```

上面，我们添加了两个参数设置，还给布局添加了 `LayoutTransition`动画，关于`LayoutTransition`动画的使用，官网对其进行了详细的解释使用：
[https://developer.android.com/reference/android/animation/LayoutTransition](https://developer.android.com/reference/android/animation/LayoutTransition)

##### 2.2 添加礼物数据、显示礼物布局
添加礼物
```
public class GiftControl {
  public GiftControl(Context context) {
      mContext = context;
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
      transition.setAnimator(LayoutTransition.CHANGE_APPEARING,
                transition.getAnimator(LayoutTransition.CHANGE_APPEARING));
      transition.setAnimator(LayoutTransition.APPEARING, 
                transition.getAnimator(LayoutTransition.APPEARING));
      transition.setAnimator(LayoutTransition.DISAPPEARING, 
                transition.getAnimator(LayoutTransition.CHANGE_APPEARING));
      transition.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, 
                transition.getAnimator(LayoutTransition.DISAPPEARING));
      mGiftLayoutParent.setLayoutTransition(transition);
      return this;
  }
  
  /**
   * 添加礼物数据
   */
  public synchronized void loadGift(LiveGiftBean gift) {
      showGift(gift);
  }

    private synchronized void showGift(LiveGiftBean giftBean) {
        if (giftBean == null) {
            return;
        }
        LiveGiftLayout giftLayout;
        int childCount = mGiftLayoutParent.getChildCount();
        Log.d(TAG, "showGift: 礼物布局的个数" + childCount);
       
        //没有超过最大的礼物布局数量，可以继续添加礼物布局
        giftLayout = new LiveGiftLayout(mContext);
        giftLayout.setIndex(0);
        //两个参数分别是layout_width,layout_height
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mGiftLayoutParent.getLayoutParams();
        //这个就是添加其他属性的，这个是在父元素的底部。
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mGiftLayoutParent.addView(giftLayout);
        boolean hasGift = giftLayout.setGift(giftBean);
        if (hasGift) {
            giftLayout.startAnimation(customAnim);
            // 添加到view队列中
            mLiveGiftLayouts.add(giftLayout);
        }
    }
}
```
添加数据，直接调用显示数据的方法，在里面，我们新建布局类，然后外层是一个`RelativeLayout`将其规则设置为底部，因为上面是自底向上动画；

好了，动画管理类，简单的框架就是这样，后面就只需要往方法上叠加逻辑就好了。

## 三、 动画布局类 `LiveGiftLayout.java`
布局类是一个自定义的组合`View`,布局是比较简单的，根据创建自定义`View`的方式来即可：
```
public class LiveGiftLayout extends FrameLayout {
    private LayoutInflater mInflater;
    private Context mContext;

    RelativeLayout mGiftItemContent;
    ImageView mIvGift, mIvSenderHeader;
    TextView mTvSenderName, mTvSenderInfo;
    TextView mTvGiftNum;

    private View rootView;

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

        this.addView(rootView);
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
}
```
上面，我们创建了一个自定义组合`View`，因为在`GiftControl`中显示`layout`的时候，调用了`setGift()`方法，所以我们在里面创建这个方法，目的是设置里面控件数据。

到现在，我们可以添加数据到控制类中，然后显示数据到`ViewGroup`中，图片的话，自己脑补一下吧😊

上篇文章中，我们创建了礼物动画类，其中的功能为：
> 1、添加礼物
2、 添加礼物布局类，
3、显示礼物布局

礼物布局，它的作用为：
> 1、创建布局后显示其中的数据

今天的文章主要是完善上面的类的功能：
```
1、暴击
2、无限数据添加
3、礼物面板被顶上去之后，在暴击时间内需要重新回到暴击面板中
```

在前一篇文章中，`GiftControl`类中，添加礼物后，开始了一个进入礼物面板的动画：
```
  public AnimatorSet startAnimation(IBaseAnim anim) {
    this.anim = anim;
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
    }
```

从这里开始，动画结束后，就有一个暴击倒计时，这个我们有多种实现方式，这里我们使用`Handler`实现：
```

private Handler mHandler = new Handler(this);  // 连击hander
private Runnable mCurrentAnimRunnable;  // 连击检测runnable

/**
 * 连击结束时回调
 */
public void comboEndAnim() {
    if (mHandler != null) {
        if (mCurrentAnimRunnable == null) {
            mCurrentAnimRunnable = new GiftNumAnimaRunnable();
            mHandler.postDelayed(mCurrentAnimRunnable, GIFT_DISMISS_TIME);
        }
    }
}

// 动画连接runnable
private class GiftNumAnimaRunnable implements Runnable {
    @Override
    public void run() {
        dismissGiftLayout();
    }
}
```

上面，当连击结束之后，就使用`Handler`开始`postDelayed`一个延时的方法来显示礼物消息。在这期间，如果有新的礼物进来，就判断当前是否在显示，所以需要在`GiftControl`的`loadGift()`方法中完善：
```

// 保存礼物view
private LinkedList<LiveGiftLayout> mLiveGiftLayouts;
/**
 * 加入礼物，具有实时连击效果
 *
 * @param gift
 * @param supportCombo 是否支持实时连击，如果为true：支持，否则不支持
 */
public synchronized void loadGift(LiveGiftBean gift, boolean supportCombo, 
                                boolean isFirst) {
       Iterator<LiveGiftLayout> liveGiftLayoutIterator 
                        = mLiveGiftLayouts.iterator();
        while (liveGiftLayoutIterator.hasNext()) {
            // 获取到队列中的view
            LiveGiftLayout giftLayout = liveGiftLayoutIterator.next();
            // 判断是否在之前存在
            if (giftLayout.getCurrentGiftId().equals(gift.getGiftId()) && 
                  giftLayout.getCurrentSendUserId().equals(gift.getSendUserId())) {
                if (giftLayout.isRemove()) {
                    // 去掉 消失的回调  整条数据是没用的了
                    giftLayout.removeDismissGiftCallback();
                    // 被顶出去了
                    liveGiftLayoutIterator.remove();
                    gift.setGiftCount(giftLayout.getGiftCount() + 1);
                    showGift(gift);
                } else {
                    //连击
                    giftLayout.updateGiftCount(gift.getGiftCount());
                    giftLayout.setSendGiftTime(gift.getSendGiftTime());
                }
                return;
            }
        }
        showGift(gift);
    }
}
```

上面的注释，首先，我们需要有一个 `mLiveGiftLayouts`变量来保存所有添加进的的`layout`，添加进来之后，在下一次加入新的数据就遍历其中所有的数据，然后来判断是否可以暴击，如果是暴击的话，就把数量给累加进入然后重新设置值；

在暴击的时候，需要移除掉`handler`的`GiftNumAnimaRunnable`，不然在`GIFT_DISMISS_TIME`之后，就会自动移除，所以每次暴击开始就要移除掉消失的`runnable`，在暴击动画（数组变大的动画）结束后，重新开始倒计时！
```
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

public void comboAnimation(boolean isFirst) {
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
}
```
不是第一次就走了下面的逻辑，数字变大的动画结束之后，回调到了暴击动画结束的方法；

到此，主要的逻辑就写完了，还是很简单的。

代码上传到了[https://github.com/shejishi/ComboGift](https://github.com/shejishi/ComboGift)喜欢的给个Star❤~





















