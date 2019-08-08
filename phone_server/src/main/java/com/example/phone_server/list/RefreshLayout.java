package com.example.phone_server.list;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RefreshLayout extends FrameLayout implements AbsListView.OnScrollListener {

    private static final String TAG = RefreshLayout.class.getSimpleName();

    private View mHeaderView;
    private ListView mContentView;
    private View mFooterView;

    private int mHeaderHeight;
    private int mHeaderWidth;

    private int mContentHeight;
    private int mContentWidth;

    private int mFooterHeight;
    private int mFooterWidth;

    private Scroller mScroller;

    private int mRefreshHeight; // 下拉的最大高度

    private int firstDownTag;

    private float downX;
    private float downY;

    private float upX;
    private float upY;

    private Context mContext;

    public RefreshLayout(@NonNull Context context) {
        this(context, null);
    }

    public RefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mScroller = new Scroller(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        if (childCount != 3) {
            throw new IllegalThreadStateException("Please add listView to RefreshLayout.");
        }
        mHeaderView = getChildAt(0);
        mContentView = (ListView) getChildAt(1);
        mFooterView = getChildAt(2);
        mContentView.setOnScrollListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(mHeaderHeight == 0){
            mHeaderWidth = mHeaderView.getMeasuredWidth();
            mHeaderHeight = mHeaderView.getMeasuredHeight();

            mContentWidth = mContentView.getMeasuredWidth();
            mContentHeight = mContentView.getMeasuredHeight();

            mFooterWidth = mFooterView.getMeasuredWidth();
            mFooterHeight = mFooterView.getMeasuredHeight();

            mRefreshHeight = mHeaderHeight - 5;

            Log.d(TAG, "mHeaderHeight:" + mHeaderHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mHeaderView.layout(0, mHeaderHeight, mHeaderWidth, 0);
        mContentView.layout(0, 0, mContentWidth, mContentHeight);
        mFooterView.layout(0, mContentHeight, mFooterWidth, mContentHeight + mFooterHeight);
    }

    private boolean isIntercept;

    private boolean isTop;

    private boolean isBottom;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final float x = ev.getX();
        final float y = ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isIntercept = false;
                upX = downX = x;
                upY = downY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isTop) {
                    /* 下拉刷新拦截 **/
                    if (upY - y < 0) {
                        isIntercept = true;
                    } else if (y - upY < 0) {
                        isIntercept = false;
                    }
                } else if (isBottom) {
                    /* 上拉加载拦截 **/
                    if (y - downY < 0) {
                        isIntercept = true;
                    } else if (y - downY > 0) {
                        isIntercept = false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                downX = upY = 0;
                downY = upX = 0;
                break;
        }
        return isIntercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = x;
                downY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isTop) {
                    if (firstDownTag == 0) {
                        firstDownTag++;
                        return true;
                    }
                    final float dy = y - downY;
                    int disY = (int) (getScrollY() - dy);
                    Log.d(TAG, "getScrollY:" + getScrollY() +",disY:" + disY + ", downY:" + downY);
                    if (-disY <= 0) {
                        disY = 0;
                    }
                    if (-disY < mHeaderHeight) {
                        scrollTo(0, disY);
                        // mRefreshProgress.setVisibility(INVISIBLE);
                        if (-disY < mRefreshHeight) {
                            Log.d(TAG, "准备起飞");
                        } else {
                            Log.d(TAG, "加速中");
                        }
                    }
                }
                downX = x;
                downY = y;
                break;
            case MotionEvent.ACTION_UP:
                isIntercept = false;
                if (-getScrollY() >= mRefreshHeight) {
                    startRefreshing();
                } else {
                    stopRefreshing();
                }
                break;
        }
        return true;
    }

    private Handler handler = new Handler();

    private void startRefreshing() {
        mScroller.startScroll(getScrollX(), getScrollY(), 0, -mRefreshHeight - getScrollY());
        Log.d(TAG, "起飞咯~");
        invalidate();
        /**
         * 模拟刷新完成，延迟关闭
         */
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopRefreshing();
            }
        }, 2000);
    }


    private void stopRefreshing() {
        mScroller.startScroll(getScrollX(), getScrollY(), 0, -getScrollY());
        /**
         * ListView子项移动到第一个
         */
        mContentView.setSelection(0);
        invalidate();

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem == 0) {
            Log.e("log", "滑到顶部");
            isTop = true;
            isBottom = false;
        }
        if (visibleItemCount + firstVisibleItem == totalItemCount) {
            Log.e("log", "滑到底部");
            isTop = false;
            isBottom = true;
        }
    }
}
