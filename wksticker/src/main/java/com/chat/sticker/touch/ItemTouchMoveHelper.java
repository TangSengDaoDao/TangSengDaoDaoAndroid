package com.chat.sticker.touch;

import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * 滑动触摸辅助
 *
 * @author dengyuhan
 *         created 2018/4/8 14:24
 */
public class ItemTouchMoveHelper {
    private boolean mInterceptEnable;
    private OnItemTouchMoveListener mOnItemTouchMoveListener;

    public boolean onTouchEvent(RecyclerView rv, MotionEvent e) {
        if (!mInterceptEnable) {
            return false;
        }

        //Log.d("ItemTouchMoveHelper", "onTouchEvent----->" + e);

        View childView = rv.findChildViewUnder(e.getX(), e.getY());
        if (childView == null) {
            if (mOnItemTouchMoveListener != null) {
                mOnItemTouchMoveListener.onItemTouchMove(false, null, -1, e);
                return true;
            }
        }

        int childPosition = rv.getChildLayoutPosition(childView);
        if (childPosition < 0) {
            if (mOnItemTouchMoveListener != null) {
                mOnItemTouchMoveListener.onItemTouchMove(false, null, -1, e);
                return true;
            }
        }

        if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_MOVE) {
            if (mOnItemTouchMoveListener != null) {
                mOnItemTouchMoveListener.onItemTouchMove(true, childView, childPosition, e);
                return true;
            }
        } else if (e.getAction() == MotionEvent.ACTION_UP) {
            if (mOnItemTouchMoveListener != null) {
                mOnItemTouchMoveListener.onItemTouchMove(true, childView, childPosition, e);
                return true;
            }
        }
        return false;
    }

    public void setOnItemTouchMoveListener(OnItemTouchMoveListener onItemTouchMoveListener) {
        this.mOnItemTouchMoveListener = onItemTouchMoveListener;
    }

    public void setInterceptEnable(boolean interceptEnable) {
        this.mInterceptEnable = interceptEnable;
    }

    /**
     * 是否在触摸
     *
     * @param event
     * @return
     */
    public static boolean isActionTouch(MotionEvent event) {
        return event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE;
    }

    /**
     * 是否触摸结束
     *
     * @param event
     * @return
     */
    public static boolean isActionEnd(MotionEvent event) {
        return event.getAction() == MotionEvent.ACTION_UP;
    }

}
