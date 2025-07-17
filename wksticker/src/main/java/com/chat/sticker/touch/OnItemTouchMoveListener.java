package com.chat.sticker.touch;

import android.view.MotionEvent;
import android.view.View;

public interface OnItemTouchMoveListener {

    /**
     * 滑动监听的回调
     *
     * @param isTouchChild  true表示触摸坐标在childView之内,false表示在childView之外,并且childView与childPosition值无效
     * @param childView 触摸的子View
     * @param childPosition 触摸的Item位置
     * @param event 触摸信息
     */
    void onItemTouchMove(boolean isTouchChild, View childView, int childPosition, MotionEvent event);
}
