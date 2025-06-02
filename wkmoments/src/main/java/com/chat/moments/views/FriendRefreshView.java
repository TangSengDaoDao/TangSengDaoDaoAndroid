package com.chat.moments.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chat.base.config.WKConfig;
import com.chat.base.entity.BottomSheetItem;
import com.chat.base.ui.Theme;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.WKDialogUtils;
import com.chat.moments.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

//仿微信朋友圈列表页下拉刷新控件
public class FriendRefreshView extends ViewGroup implements OnDetectScrollListener {
    private ImageView backIv;
    private ImageView cameraIv;
    private View titleView;
    private TextView titleTv;
    private TextView updateBgTv;
    //圆形指示器
    private ImageView mRainbowView;
    private ImageView momentBgIv;
    private RecyclerView mContentView;

    //控件宽，高
    private int sWidth;
    private int sHeight;

    private ViewDragHelper mDragHelper;

    //contentView的当前top属性
    private int currentTop;
    //listView首个item
    private int firstItem;
    private boolean bScrollDown = false;

    private boolean bDraging = false;

    //圆形加载指示器最大top
    private final int rainbowMaxTop = 180;
    //圆形加载指示器刷新时的top
    private final int rainbowStickyTop = 180;
    //圆形加载指示器初始top
    private final int rainbowStartTop = -120;
    //圆形加载指示器的半径
    private final int rainbowRadius = 100;
    private int rainbowTop = -120;
    //圆形加载指示器旋转的角度
    private int rainbowRotateAngle = 0;

    private boolean bViewHelperSettling = false;

    //刷新接口listener
    private OnRefreshListener mRefreshLisenter;
    int sh;
    private AbsListView.OnScrollListener onScrollListener;
    private final OnDetectScrollListener onDetectScrollListener;
    private View mHeadViw;

    public enum State {
        NORMAL,
        REFRESHING,
        DRAGING
    }

    //控件当前状态
    private State mState = State.NORMAL;

    public FriendRefreshView(Context context) {
        this(context, null);
    }

    public FriendRefreshView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FriendRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        sh = AndroidUtilities.getScreenHeight() / 3;
        initHandler();
        initDragHelper();
        initListView();
        initRainbowView();
        setBackgroundColor(ContextCompat.getColor(context, R.color.color303030));
        onDetectScrollListener = this;
    }

    public RecyclerView getmContentView() {
        return mContentView;
    }

    public View getmHeadViw() {
        return mHeadViw;
    }

    /**
     * 初始化handler，当ViewDragHelper释放了mContentView时，
     * 我们通过循环发送消息刷新mRainbowView的位置和角度
     */
    @SuppressLint("HandlerLeak")
    private void initHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(@NotNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        if (rainbowTop > rainbowStartTop) {
                            rainbowTop -= 10;
                            requestLayout();
                            mHandler.sendEmptyMessageDelayed(0, 15);
                        }
                        break;
                    case 1:
                        if (rainbowTop <= rainbowStickyTop) {
                            if (rainbowTop < rainbowStickyTop) {
                                rainbowTop += 10;
                                if (rainbowTop > rainbowStickyTop) {
                                    rainbowTop = rainbowStickyTop;
                                }
                            }
                            mRainbowView.setRotation(rainbowRotateAngle -= 10);
                        } else {
                            mRainbowView.setRotation(rainbowRotateAngle += 10);
                        }

                        requestLayout();

                        mHandler.sendEmptyMessageDelayed(1, 15);
                        break;
                }
            }
        };
    }

    /**
     * 初始化mDragHelper，我们处理拖动的核心类
     */
    private void initDragHelper() {
        mDragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(@NotNull View view, int i) {
                return view == mContentView && !bViewHelperSettling;
            }

            @Override
            public int clampViewPositionHorizontal(@NotNull View child, int left, int dx) {
                return 0;
            }

            @Override
            public int clampViewPositionVertical(@NotNull View child, int top, int dy) {
                //限制最大下拉屏幕的三分之一高度

                if (top > sh) top = sh;
                return top;
            }

            @Override
            public void onViewPositionChanged(@NotNull View changedView, int left, int top, int dx, int dy) {
                super.onViewPositionChanged(changedView, left, top, dx, dy);
                if (changedView == mContentView) {
                    int lastContentTop = currentTop;
                    if (top >= 0) {
                        currentTop = top;
                    } else {
                        top = 0;
                    }
                    int lastTop = rainbowTop;
                    int rTop = top + rainbowStartTop;
                    if (rTop >= rainbowMaxTop) {
                        if (!isRefreshing()) {
                            rainbowRotateAngle += (currentTop - lastContentTop) * 2;
                            rTop = rainbowMaxTop;
                            rainbowTop = rTop;
                            mRainbowView.setRotation(rainbowRotateAngle);
                        } else {
                            rTop = rainbowMaxTop;
                            rainbowTop = rTop;
                        }

                    } else {
                        if (isRefreshing()) {
                            rainbowTop = rainbowStickyTop;
                        } else {
                            rainbowTop = rTop;
                            rainbowRotateAngle += (rainbowTop - lastTop) * 3;
                            mRainbowView.setRotation(rainbowRotateAngle);
                        }
                    }

                    requestLayout();

                }
            }

            @Override
            public void onViewReleased(@NotNull View releasedChild, float xvel, float yvel) {
                super.onViewReleased(releasedChild, xvel, yvel);
                mDragHelper.settleCapturedViewAt(0, 0);
                ViewCompat.postInvalidateOnAnimation(FriendRefreshView.this);
                //如果手势释放时，拖动的距离大于rainbowStickyTop，开始刷新
                if (currentTop >= rainbowStickyTop) {
                    startRefresh();
                }

            }
        });


    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
            bViewHelperSettling = true;
        } else {
            bViewHelperSettling = false;
        }
    }

    /**
     * 我们invoke 方法shouldIntercept来判断是否需要拦截事件，
     * 拦截事件是为了将事件传递给mDragHelper来处理，我们这里只有当mContentView滑动到顶部
     * 且mContentView没有处于滑动状态时才触发拦截。
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mDragHelper.shouldInterceptTouchEvent(ev);
        return shouldIntercept();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                mLastMotionY = 0;
                bDraging = false;
                bScrollDown = false;
                rainbowRotateAngle = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                int index = event.getActionIndex();// MotionEventCompat.getActionIndex(event);
                int pointerId = event.getPointerId(index);// MotionEventCompat.getPointerId(event, index);
                if (shouldIntercept()) {
                    mDragHelper.captureChildView(mContentView, pointerId);
                }
                break;
        }
        return true;
    }

    /**
     * 判断是否需要拦截触摸事件
     *
     * @return boolean
     */
    private boolean shouldIntercept() {
        if (bDraging) return true;
        int childCount = mContentView.getChildCount();
        if (childCount > 0) {
            View firstChild = mContentView.getChildAt(0);
            return firstChild.getTop() >= 0
                    && firstItem == 0 && currentTop == 0
                    && bScrollDown;
        } else {
            return true;
        }
    }

    /**
     * 判断mContentView是否处于顶部
     *
     * @return boolean
     */
    private boolean checkIsTop() {
        int childCount = mContentView.getChildCount();
        if (childCount > 0) {
            View firstChild = mContentView.getChildAt(0);
            return firstChild.getTop() >= 0
                    && firstItem == 0 && currentTop == 0;
        } else {
            return false;
        }
    }

    private void initRainbowView() {
        mRainbowView = new ImageView(getContext());
        mRainbowView.setImageResource(R.mipmap.rainbow_ic);
        addView(mRainbowView);
    }

    /**
     * 初始化listView，我们创建了istView for you，所有你要做的
     * 就是调用setAdapter，绑定你自定义的adapter
     */
    int mDistanceY = 0;

    private void initListView() {
        mContentView = new FriendRefreshListView(getContext());
        mHeadViw = LayoutInflater.from(getContext()).inflate(R.layout.list_head_layout, null);
        updateBgTv = mHeadViw.findViewById(R.id.updateBgTv);
        momentBgIv = mHeadViw.findViewById(R.id.momentBgIv);
        mHeadViw.findViewById(R.id.topLayout).setOnClickListener(view -> {
            //查看自己的朋友圈才能修改封面
            if (TextUtils.isEmpty(uid) || uid.equals(WKConfig.getInstance().getUid())) {
                List<BottomSheetItem> list = new ArrayList<>();
                list.add(new BottomSheetItem(getContext().getString(R.string.change_conver), R.mipmap.qr_gallery, () -> iConverClick.onChangeBg()));
                WKDialogUtils.getInstance().showBottomSheet(getContext(), getContext().getString(R.string.moment_cover), false, list);
            }
        });
        boolean isDarkModel = Theme.getDarkModeStatus(getContext());
        this.addView(mContentView);
        mContentView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NotNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                //控制tab透明度
                assert layoutManager != null;
                if (layoutManager.findFirstVisibleItemPosition() == 0) {
                    mDistanceY = -recyclerView.getChildAt(0).getTop();
                    //完全变色的高度
                    int changeHeight = AndroidUtilities.dp(150);
                    //当滑动的距离 <= toolbar高度的时候，改变Toolbar背景色的透明度，达到渐变的效果
                    if (mDistanceY <= changeHeight) {
                        float scale = (float) mDistanceY / changeHeight;
                        float alpha = scale * 255;
                        titleView.setBackgroundColor(Color.argb((int) alpha, isDarkModel ? 17 : 246, isDarkModel ? 17 : 246, isDarkModel ? 17 : 246));
                        backIv.setImageResource(R.mipmap.ic_ab_back);
                        Theme.setColorFilter(getContext(), backIv, R.color.white);
                        titleTv.setVisibility(INVISIBLE);

                        if (TextUtils.isEmpty(uid)) {
                            cameraIv.setVisibility(VISIBLE);
                            Theme.setColorFilter(getContext(), cameraIv, R.color.white);
                            cameraIv.setImageResource(R.mipmap.floating_camera);
                        } else {
                            if (uid.equals(WKConfig.getInstance().getUid())) {
                                cameraIv.setVisibility(VISIBLE);
                                Theme.setColorFilter(getContext(), cameraIv, R.color.white);
                                cameraIv.setImageResource(R.mipmap.floating_message);
                            } else {
                                cameraIv.setVisibility(GONE);
                            }
                        }
                    } else {
                        titleTv.setVisibility(VISIBLE);
                        Theme.setColorFilter(getContext(), backIv, isDarkModel ? R.color.white : R.color.black);
                        if (TextUtils.isEmpty(uid)) {
                            Theme.setColorFilter(getContext(), cameraIv, isDarkModel ? R.color.white : R.color.black);
                            cameraIv.setVisibility(VISIBLE);
                        } else {
                            if (uid.equals(WKConfig.getInstance().getUid())) {
                                cameraIv.setVisibility(VISIBLE);
                                Theme.setColorFilter(getContext(), cameraIv, isDarkModel ? R.color.white : R.color.black);
                                cameraIv.setImageResource(R.mipmap.floating_message);
                            } else {
                                cameraIv.setVisibility(GONE);
                            }
                        }
                        //将标题栏的颜色设置为完全不透明状态
                        titleView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.homeColor));
                    }
                } else {
                    titleTv.setVisibility(VISIBLE);
                    Theme.setColorFilter(getContext(), backIv, isDarkModel ? R.color.white : R.color.black);
                    if (TextUtils.isEmpty(uid)) {
                        Theme.setColorFilter(getContext(), cameraIv, isDarkModel ? R.color.white : R.color.black);
                    } else {
                        if (uid.equals(WKConfig.getInstance().getUid())) {
                            cameraIv.setImageResource(R.mipmap.floating_message);
                            Theme.setColorFilter(getContext(), cameraIv, isDarkModel ? R.color.white : R.color.black);
                        } else cameraIv.setVisibility(GONE);
                    }
                    titleView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.homeColor));
                }
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        sWidth = MeasureSpec.getSize(widthMeasureSpec);
        sHeight = MeasureSpec.getSize(heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        LayoutParams contentParams = (LayoutParams) mContentView.getLayoutParams();
        contentParams.left = 0;
        contentParams.top = 0;
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        LayoutParams contentParams = (LayoutParams) mContentView.getLayoutParams();
        mContentView.layout(contentParams.left, currentTop,
                contentParams.left + sWidth, currentTop + sHeight);

        mRainbowView.layout(rainbowRadius, rainbowTop,
                rainbowRadius * 2, rainbowTop + rainbowRadius);
    }

    @Override
    public void onUpScrolling() {
        bScrollDown = false;
    }

    @Override
    public void onDownScrolling() {
        bScrollDown = true;
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {

        int left = 0;
        int top = 0;

        LayoutParams(Context arg0, AttributeSet arg1) {
            super(arg0, arg1);
        }

        LayoutParams(int arg0, int arg1) {
            super(arg0, arg1);
        }

        LayoutParams(android.view.ViewGroup.LayoutParams arg0) {
            super(arg0);
        }

    }

    @Override
    public android.view.ViewGroup.LayoutParams generateLayoutParams(
            AttributeSet attrs) {
        return new FriendRefreshView.LayoutParams(getContext(), attrs);
    }

    @Override
    protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected android.view.ViewGroup.LayoutParams generateLayoutParams(
            android.view.ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof FriendRefreshView.LayoutParams;
    }

    private float mLastMotionX;
    private float mLastMotionY;

    /**
     * 对ListView的触摸事件进行判断，是否处于滑动状态
     */
    private class FriendRefreshListView extends RecyclerView {


        public FriendRefreshListView(Context context) {
            this(context, null);
        }

        public FriendRefreshListView(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public FriendRefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            setBackgroundColor(ContextCompat.getColor(context, R.color.layoutColor));
        }

        /*当前活动的点Id,有效的点的Id*/
        protected int mActivePointerId = INVALID_POINTER;

        /*无效的点*/
        private static final int INVALID_POINTER = -1;

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            final int action = ev.getActionMasked();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    int index = ev.getActionIndex();//MotionEventCompat.getActionIndex(ev);
                    mActivePointerId = ev.getPointerId(index);// MotionEventCompat.getPointerId(ev, index);
                    if (mActivePointerId == INVALID_POINTER)
                        break;
                    mLastMotionX = ev.getX();
                    mLastMotionY = ev.getY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    int indexMove = ev.getActionIndex();//MotionEventCompat.getActionIndex(ev);
                    mActivePointerId = ev.getPointerId(indexMove);// MotionEventCompat.getPointerId(ev, indexMove);
                    if (mActivePointerId == INVALID_POINTER) {

                    } else {
                        final float y = ev.getY();
                        float dy = y - mLastMotionY;
                        if (checkIsTop() && dy >= 1.0f) {
                            bScrollDown = true;
                            bDraging = true;
                        } else {
                            bScrollDown = false;
                            bDraging = false;
                        }
                        mLastMotionX = y;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    mLastMotionY = 0;
                    break;
            }
            return super.onTouchEvent(ev);
        }
    }

    public boolean isRefreshing() {
        return mState == State.REFRESHING;
    }


    Handler mHandler;

    public void startRefresh() {
        if (!isRefreshing()) {
            mHandler.removeMessages(0);
            mHandler.removeMessages(1);
            mHandler.sendEmptyMessage(1);
            mState = State.REFRESHING;
            invokeListener();
        }

    }

    private void invokeListener() {
        if (mRefreshLisenter != null) {
            mRefreshLisenter.onRefresh();
        }
    }

    public void stopRefresh() {
        mHandler.removeMessages(1);
        mHandler.sendEmptyMessage(0);
        mState = State.NORMAL;
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mRefreshLisenter = listener;
    }

    public void setTitleViews(TextView titleTv, ImageView backIv, ImageView cameraIv, View titleView) {
        this.titleView = titleView;
        this.backIv = backIv;
        this.cameraIv = cameraIv;
        this.titleTv = titleTv;
    }

    private String uid;

    public void setUid(String uid) {
//        if (TextUtils.isEmpty(uid)) uid = WKConfig.getInstance().getUid();
        this.uid = uid;
        if (!TextUtils.isEmpty(uid) && !uid.equals(WKConfig.getInstance().getUid())) {
            updateBgTv.setVisibility(GONE);
        }
    }

    public ImageView getMomentBgIv() {
        return momentBgIv;
    }

    public void hideOrShowUpdateBgTv(boolean isShow) {
        updateBgTv.setVisibility(isShow ? VISIBLE : GONE);
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    private IConverClick iConverClick;

    public void setOnConverClick(IConverClick iConverClick) {
        this.iConverClick = iConverClick;
    }

    public interface IConverClick {
        void onChangeBg();
    }
}
