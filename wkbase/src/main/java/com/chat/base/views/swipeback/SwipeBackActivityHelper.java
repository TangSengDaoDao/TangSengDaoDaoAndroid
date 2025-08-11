package com.chat.base.views.swipeback;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;

import com.chat.base.R;

/**
 * @author Yrom
 */
public class SwipeBackActivityHelper {
    private final Activity mActivity;

    private SwipeBackLayout mSwipeBackLayout;

    public SwipeBackActivityHelper(Activity activity) {
        mActivity = activity;
    }

    @SuppressWarnings("deprecation")
    public void onActivityCreate() {
        // Android 15 (API 35) 特殊处理：避免设置透明背景导致的残影问题
        if (Build.VERSION.SDK_INT >= 35) {
            // 为Android 15设置不透明的白色背景，避免滑动返回时的残影
            mActivity.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            // 不设置DecorView背景为null，保持系统默认
        } else {
            // Android 15以下版本保持原有逻辑
            mActivity.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mActivity.getWindow().getDecorView().setBackgroundDrawable(null);
        }
        
        mSwipeBackLayout = (SwipeBackLayout) LayoutInflater.from(mActivity).inflate(R.layout.wk_swipeback_layout, null);
        mSwipeBackLayout.addSwipeListener(new SwipeBackLayout.SwipeListener() {
            @Override
            public void onScrollStateChange(int state, float scrollPercent) {
            }

            @Override
            public void onEdgeTouch(int edgeFlag) {
                // Android 15 不转换为透明Activity，避免残影
                if (Build.VERSION.SDK_INT < 35) {
                    Utils.convertActivityToTranslucent(mActivity);
                }
            }

            @Override
            public void onScrollOverThreshold() {

            }
        });
    }

    public void onPostCreate() {
        mSwipeBackLayout.attachToActivity(mActivity);
    }

    public <T extends View> T findViewById(int id) {
        if (mSwipeBackLayout != null) {
            return mSwipeBackLayout.findViewById(id);
        }
        return null;
    }

    public SwipeBackLayout getSwipeBackLayout() {
        return mSwipeBackLayout;
    }
}
