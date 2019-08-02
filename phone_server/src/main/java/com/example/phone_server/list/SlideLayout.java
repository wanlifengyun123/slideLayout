package com.example.phone_server.list;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SlideLayout extends FrameLayout {

    private View mContentView;
    private int mContentWidth;
    private int mContentHeight;

    private View mMenuView;
    private int mMenuWidth;
    private int mMenuHeight;

    private Scroller mScroller;

    private float downX;
    private float downY;

    public SlideLayout(@NonNull Context context) {
        this(context, null);
    }

    public SlideLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        if(childCount == 2){
            mContentView = getChildAt(0);
            mMenuView = getChildAt(1);
        } else {
            throw new IllegalArgumentException("SlideLayout only need contains two child (content and slide).");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mContentView.getMeasuredWidth(), mContentView.getMeasuredHeight());
        mContentWidth = getMeasuredWidth();
        mContentHeight = getMeasuredHeight();
        mMenuWidth = mMenuView.getMeasuredWidth();
        mMenuHeight = mMenuView.getMeasuredHeight();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mContentView.layout(0, 0, mContentWidth, mContentHeight);
        //将menu布局到右侧不可见（屏幕外)
        mMenuView.layout(mContentWidth, 0, mContentWidth + mMenuWidth, mContentHeight);
        if(mMenuView instanceof LinearLayout){
            View childAt = ((LinearLayout) mMenuView).getChildAt(0);
            childAt.setMinimumHeight(mContentHeight);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean intercept = false;
        final float x = event.getX();
        final float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX =  x;
                downY = y;
                if (onSlideChangeListener != null) {
                    onSlideChangeListener.onClick(this);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // 解决Item点击事件的冲突
                final float moveX = Math.abs(x - downX);
                if (moveX > 10f) {
                    //对儿子touch事件进行拦截
                    intercept = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return intercept;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.performClick();
        float x =  event.getX();
        float y =  event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = x;
                downY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                final float dx = (int) (x - downX);
                int disX = (int) (getScrollX() - dx);
                if (disX <= 0) {
                    disX = 0;
                } else if (disX >= mMenuWidth) {
                    disX = mMenuWidth;
                }

                scrollTo(disX, getScrollY());

                // 解决滑动冲突
                final float moveX = Math.abs(x - downX);
                final float moveY = Math.abs(y - downY);
                if (moveX > moveY && moveX > 10f) {
                    //剥夺ListView对touch事件的处理权
                    getParent().requestDisallowInterceptTouchEvent(true);
                }

                downX = x;
                downY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (getScrollX() < mMenuWidth / 2) {
                    closeMenu();
                } else {
                    openMenu();
                }
                break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    public final void openMenu() {
        mScroller.startScroll(getScrollX(), getScrollY(), mMenuWidth - getScrollX(), 0);
        invalidate();
        if (onSlideChangeListener != null) {
            onSlideChangeListener.onMenuOpen(this);
        }
    }

    public final void closeMenu() {
        mScroller.startScroll(getScrollX(), getScrollY(), 0 - getScrollX(), 0);
        invalidate();
        if (onSlideChangeListener != null) {
            onSlideChangeListener.onMenuClose(this);
        }
    }

    // 限制只能有一个menu被打开
    private onSlideChangeListener onSlideChangeListener;

    public interface onSlideChangeListener {
        void onMenuOpen(SlideLayout slideLayout);

        void onMenuClose(SlideLayout slideLayout);

        void onClick(SlideLayout slideLayout);
    }

    public void setOnSlideChangeListener(SlideLayout.onSlideChangeListener onSlideChangeListener) {
        this.onSlideChangeListener = onSlideChangeListener;

    }
}
