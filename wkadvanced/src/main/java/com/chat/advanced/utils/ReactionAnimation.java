package com.chat.advanced.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import com.chat.advanced.R;
import com.chat.base.config.WKConfig;
import com.chat.base.msg.ChatAdapter;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.WKReader;
import com.chat.base.views.ViewSizeChangeAnimation;
import com.xinbida.wukongim.entity.WKMsg;
import com.xinbida.wukongim.entity.WKMsgReaction;

import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RLottieImageView;

import java.util.Objects;

public class ReactionAnimation {
    static RLottieImageView imageView;
    static RLottieImageView bigIv;
    static ViewGroup mRootView;

    static int[] getReactionLocation(ChatAdapter chatAdapter, WKMsg wkMsg) {
        int index = 0;
        for (int i = 0, size = Objects.requireNonNull(chatAdapter).getData().size(); i < size; i++) {
            if (chatAdapter.getData().get(i).wkMsg.clientMsgNO.equals(wkMsg.clientMsgNO)) {
                index = i;
                break;
            }
        }
        int[] location = new int[2];
        View view = chatAdapter.getViewByPosition(index, R.id.reactionsView);
        if (view != null) {
            if (view.getVisibility() == View.GONE)
                view.setVisibility(View.VISIBLE);
            view.getLocationOnScreen(location);
        }
        return location;
    }

    public static void show(Context context, ChatAdapter chatAdapter, String json, int[] location, WKMsg msg) {
        boolean isAdded = false;
        if (WKReader.isNotEmpty(msg.reactionList)) {
            for (WKMsgReaction reaction : msg.reactionList) {
                if (reaction.emoji.equals(json) && reaction.uid.equals(WKConfig.getInstance().getUid())) {
                    isAdded = true;
                    break;
                }
            }
        }
        if (isAdded) return;
        if (bigIv != null && bigIv.isPlaying()) {
            stop();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int[] reactionLocation = getReactionLocation(chatAdapter, msg);
                imageView = new RLottieImageView(context);
                bigIv = new RLottieImageView(context);
                mRootView = ((Activity) context).findViewById(android.R.id.content);
                RLottieDrawable drawable = new RLottieDrawable(context, ReactionStickerUtils.getReactionStickerLittle(json), AndroidUtilities.dp(35), AndroidUtilities.dp(35), false, null);
                imageView.setAutoRepeat(false);
                imageView.setAnimation(drawable);
                imageView.playAnimation();

                mRootView.addView(imageView);
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView.getLayoutParams();
                params.width = AndroidUtilities.dp(35);
                params.height = AndroidUtilities.dp(35);
                imageView.setLayoutParams(params);

                imageView.setX(location[0]);
                imageView.setY(location[1]);

                Animation animation1 = new ViewSizeChangeAnimation(imageView, AndroidUtilities.dp(120), AndroidUtilities.dp(120));
                animation1.setDuration(500);

                //平易坐标
                float x = location[0];
                float y = location[1];

                float targetX = reactionLocation[0] + AndroidUtilities.dp(3);
                float targetY = reactionLocation[1];
                ValueAnimator valueAnimator = new ValueAnimator();
                valueAnimator.setDuration(500);
                valueAnimator.setObjectValues(new PointF(x, y));
                valueAnimator.setInterpolator(new LinearInterpolator());
                //首先判断 目标点在上还是在下
                final boolean flagX = (x - targetX) > 0;
                final boolean flagY = (y - targetY) > 0;

                valueAnimator.setEvaluator((TypeEvaluator<PointF>) (fraction, startValue, endValue) -> {

                    PointF point = new PointF();
                    //这里是需要倒着来  最后要到达200 200 这个点

                    float fractionNeed = 1 - fraction;

                    if (flagX) {
                        float vX = x - targetX;
                        point.x = vX * fractionNeed + targetX;
                    } else {
                        float vX = targetX - x;
                        point.x = x + vX * fraction;
                    }

                    if (flagY) {
                        float vY = y - targetY;
                        point.y = vY * fractionNeed + targetY;
                    } else {
                        float vY = targetY - y;
                        point.y = y + vY * fraction;
                    }
                    return point;

                });
                valueAnimator.start();
                imageView.startAnimation(animation1);
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        RLottieDrawable drawable = new RLottieDrawable(context, ReactionStickerUtils.getReactionStickerBig(json), json, AndroidUtilities.dp(135), AndroidUtilities.dp(135), false, null);
                        bigIv.setAutoRepeat(false);
                        bigIv.setAnimation(drawable);
                        bigIv.playAnimation();
                        drawable.setOnFinishCallback(() -> {
                            imageView.stopAnimation();
                            bigIv.stopAnimation();
                            chatAdapter.getConversationContext().getChatActivity().runOnUiThread(() -> {
                                imageView.clearAnimation();
                                Animation animation2 = new ViewSizeChangeAnimation(imageView, AndroidUtilities.dp(25), AndroidUtilities.dp(25));
                                animation2.setDuration(200);
                                animation2.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation2) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation2) {
                                        mRootView.removeView(imageView);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation2) {

                                    }
                                });
                                imageView.startAnimation(animation2);
                                mRootView.removeView(bigIv);
                            });
                        }, drawable.getFramesCount() - 2);

                        mRootView.addView(bigIv);
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) bigIv.getLayoutParams();
                        params.width = AndroidUtilities.dp(250);
                        params.height = AndroidUtilities.dp(250);
                        bigIv.setLayoutParams(params);
                        bigIv.setX(targetX - AndroidUtilities.dp(100));
                        bigIv.setY(targetY);
                        bigIv.playAnimation();
                    }
                });
                valueAnimator.addUpdateListener(animation -> {
                    PointF point = (PointF) animation.getAnimatedValue();
                    float vX = point.x;
                    float vY = point.y;
                    //说明vx 最大值就是view原坐标
                    if (flagX) {
                        if (vX <= x && vX >= targetX) {
                            imageView.setX(vX);
                        }
                    } else {//说明vx 最小值就是view原坐标
                        if (vX >= x && vX <= targetX) {
                            imageView.setX(vX);
                        }
                    }

                    //说明vY 最大值就是view原坐标
                    if (flagY) {
                        if (vY <= y && vY >= targetY) {
                            imageView.setY(vY);
                        }
                    } else {//说明vx 最小值就是view原坐标
                        if (vY >= y && vY <= targetY) {
                            imageView.setY(vY);
                        }
                    }

                });
            }
        }, 300);

    }

    public static void stop() {
        if (mRootView != null) {
            if (bigIv != null) {
                mRootView.removeView(bigIv);
                bigIv.stopAnimation();
            }
            if (imageView != null) {
                mRootView.removeView(imageView);
                imageView.stopAnimation();
            }

        }
    }
}
