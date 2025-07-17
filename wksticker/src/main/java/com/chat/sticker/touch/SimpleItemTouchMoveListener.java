package com.chat.sticker.touch;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 普通的滑动触摸监听
 *
 * @author dengyuhan
 *         created 2018/4/8 14:22
 */
public abstract class SimpleItemTouchMoveListener extends RecyclerView.SimpleOnItemTouchListener implements OnItemTouchMoveListener {
    private final ItemTouchMoveHelper mItemTouchMoveHelper;

    public SimpleItemTouchMoveListener() {
        mItemTouchMoveHelper = new ItemTouchMoveHelper();
        mItemTouchMoveHelper.setOnItemTouchMoveListener(this);
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        return onInterceptEnable();
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        mItemTouchMoveHelper.setInterceptEnable(onInterceptEnable());
        mItemTouchMoveHelper.onTouchEvent(rv, e);
    }

    @Override
    public void onItemTouchMove(boolean isTouchChild, View childView, int childPosition, MotionEvent event) {

    }

    public abstract boolean onInterceptEnable();
}
