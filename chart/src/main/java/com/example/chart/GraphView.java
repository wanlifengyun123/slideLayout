package com.example.chart;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import com.example.chart.util.DisplayUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GraphView extends View {

    private Context mContext;

    private Paint mLinePaint;
    private Paint mTextPaint;
    private Paint mRectPaint;
    private Paint mPathPaint;

    private RectF mRectF; // 矩形大小设置

    private int mWidth;
    private int mHeight;

    private int xPaddingPoint; // x轴左右两边的偏移量
    private int yPaddingPoint; // y轴上下两边的偏移量

    private Path mCubicPath; // 曲线图路径对象
    private List<PointF> mPointPathList; // 记录柱状图的定点坐标
    private List<PointF> fList; // 记录曲线图的定点坐标

    private Path mDstPath; // 路径绘制每段截取出来的路径
    private PathMeasure mPathMeasure; //路径测量类
    private ValueAnimator mValueAnimator;
    private float mCurrentValue;

    private int mCanvasWidth; // 画布的实际宽度
    private int mMinWSpace; // 横坐标最小间距
    private boolean isInitialized = false; // 是否初始化坐标完成
    private boolean isCustMinWSpace; // 是否自定义最小宽度
    private boolean isBezierLine = true; // 是否画曲线还是直线图
    private boolean isPlayAnim; // 是否开启动画

    // 滑动相关
    private Scroller mScroller;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private VelocityTracker velocityTracker;
    private FlingRunnable mFling;

    private int xSize = 15;
    private int ySize = 9;


    public GraphView(Context context) {
        this(context, null);
    }

    public GraphView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mRectF = new RectF();
        mCubicPath = new Path();
        mDstPath = new Path();
        mPathMeasure = new PathMeasure();
        mScroller = new Scroller(context, null, false);
        mFling = new FlingRunnable();
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        initPaint();
        initData();
        initAnim();
    }

    private void initPaint() {
        int color = mContext.getResources().getColor(R.color.colorAccent, null);
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(color);
        mLinePaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(color);
        mTextPaint.setTextSize(20);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRectPaint.setAntiAlias(true);
        mRectPaint.setColor(color);
        mRectPaint.setStyle(Paint.Style.FILL);

        mPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPathPaint.setColor(Color.parseColor("#303F9F"));
        mPathPaint.setStrokeWidth(DisplayUtil.dp2px(3));
        mPathPaint.setStyle(Paint.Style.STROKE);
    }

    private void initData() {
        mPointPathList = new ArrayList<>();
        fList = new ArrayList<>();
        xPaddingPoint = DisplayUtil.dp2px(20);
        yPaddingPoint = DisplayUtil.dp2px(20);
    }

    private void initAnim() {
        mValueAnimator = ValueAnimator.ofFloat(0f, 1f).setDuration(1000);
        mValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mValueAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mCurrentValue = 0f;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mCurrentValue = 1f;
            }
        });
    }

    public void toggleSpace(int space){
        isCustMinWSpace = !isCustMinWSpace;
        if(isCustMinWSpace){
            mMinWSpace = space;
        }
        isInitialized = false;
        toggleAnim();
    }

    public void toggleBezierLine() {
        isBezierLine = !isBezierLine;
        toggleAnim();
    }

    public void toggleAnim() {
        isPlayAnim = true;
        if (mValueAnimator != null) {
            mValueAnimator.start();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mHeight = getHeight();
        mWidth = getWidth();
    }

    private float lastX;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 当数据的长度不足以滑动时，不做滑动处理
        if (mCanvasWidth < mWidth) {
            return true;
        }
        initVelocityTrackerIfNotExists();
        velocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                lastX = event.getX();
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                float curX = event.getX();
                int deltaX = (int) (lastX - curX);
                // 滑动处理
                if (getScrollX() + deltaX < 0) {
                    scrollTo(0, 0);
                    return true;
                } else if (getScrollX() + deltaX > mCanvasWidth - mWidth) {
                    scrollTo(mCanvasWidth - mWidth, 0);
                    return true;
                }
                scrollBy(deltaX, 0);
                lastX = curX;
                break;
            }
            case MotionEvent.ACTION_UP: {
                // 计算当前速度， 1000表示每秒像素数等
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                // 获取横向速度
                int velocityX = (int) velocityTracker.getXVelocity();
                // 速度要大于最小的速度值，才开始滑动
                if (Math.abs(velocityX) > mMinimumVelocity) {
                    int initX = getScrollX();
                    int maxX = mCanvasWidth - mWidth;
                    if (maxX > 0) {
                        // 开始 fling
//                        mScroller.fling(initX, 0, -velocityX,
//                                0, 0, maxX, 0, 0);
                        mFling.start(initX, velocityX, maxX);
                    }
                }
                recycleVelocityTracker();
                break;
            }
        }
        return true;
    }

    private void initVelocityTrackerIfNotExists() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mWidth == 0 || mHeight == 0) {
            return;
        }

        int minHSpace = (mHeight - 2 * yPaddingPoint) / (ySize + 1);
        if(!isCustMinWSpace){
            mMinWSpace = (mWidth - 2 * xPaddingPoint) / (xSize + 1);
        }
        mCanvasWidth = mMinWSpace * (xSize + 1) + 2 * xPaddingPoint;
        float xEndPoint = mCanvasWidth - xPaddingPoint;
        float yEndPoint = mHeight - yPaddingPoint;

        // 画横线
        int lineYSize = ySize + 2;
        for (int i = 0; i < lineYSize; i++) {
            canvas.drawLine(xPaddingPoint, yPaddingPoint + minHSpace * i, xEndPoint, yPaddingPoint + minHSpace * i, mLinePaint);
            // 显示文字横线坐标
            String yName = String.valueOf(i);
            canvas.drawText(yName, (float) (xPaddingPoint * 0.5), yEndPoint - minHSpace * i, mTextPaint);
        }

        // 画竖线
        int lineXSize = xSize + 2;
        if (lineXSize * mMinWSpace < xEndPoint) {
            lineXSize = (int) (xEndPoint / mMinWSpace);
        }
        for (int i = 0; i < lineXSize; i++) {
            canvas.drawLine(xPaddingPoint + mMinWSpace * i, yPaddingPoint, xPaddingPoint + mMinWSpace * i, yEndPoint, mLinePaint);
            // 显示文字竖线坐标
            String yName = String.valueOf(i);
            canvas.drawText(yName, xPaddingPoint + mMinWSpace * i, yEndPoint + (float) (yPaddingPoint * 0.5), mTextPaint);
        }

        float offsetX = (float) (xPaddingPoint + mMinWSpace * 0.75);
        // 画柱状图
        if (!isInitialized) {
            mPointPathList.clear();
            @SuppressLint("DrawAllocation")
            Random random = new Random();
            for (int i = 0; i < xSize; i++) {
                int randomNumber = random.nextInt((mHeight - 2 * yPaddingPoint));
                mRectF.left = offsetX + mMinWSpace * i;
                mRectF.right = (float) (mRectF.left + mMinWSpace * 0.5);
                mRectF.top = yEndPoint - randomNumber;
                mRectF.bottom = yEndPoint;
                //canvas.drawRect(mRectF, mRectPaint);
                // 取圆柱体的x中心点
                mPointPathList.add(new PointF((float) (mRectF.left + mMinWSpace * 0.25), mRectF.top));
            }
            isInitialized = true;
        } else {
            for (int i = 0; i < mPointPathList.size(); i++) {
                float top = (yEndPoint - mPointPathList.get(i).y);
                mRectF.left = offsetX + mMinWSpace * i;
                mRectF.right = (float) (mRectF.left + mMinWSpace * 0.5);
                mRectF.top = yEndPoint - mCurrentValue * top;
                mRectF.bottom = yEndPoint;
                canvas.drawRect(mRectF, mRectPaint);

                // 显示文字竖线坐标
                float p = (top * 100) / (float) (mHeight - 2 * yPaddingPoint);
                String yName = Math.round(p) + "%";
                canvas.drawText(yName, xPaddingPoint + mMinWSpace * (i + 1), mRectF.top - (float) (yPaddingPoint * 0.5), mTextPaint);
                // 取圆柱体的x中心点
            }
        }

        // 画二阶贝塞尔曲线
        mCubicPath.reset();
        mDstPath.reset();
        fList.clear();
        // 获取原点位置
        PointF pointF = new PointF(xPaddingPoint, yEndPoint);
        fList.add(pointF);
        fList.addAll(mPointPathList);
        PointF endPointF = new PointF(xEndPoint, yEndPoint);
        fList.add(endPointF);
        cubicTo();
        drawCubicTo(canvas);
    }

    // 画二阶贝塞尔曲线 isBezierLine ： 是否画曲线还是直线图
    private void cubicTo() {
        mCubicPath.moveTo(fList.get(0).x, fList.get(0).y);
        for (int j = 0; j < fList.size(); j++) {
            PointF pre = fList.get(j);
            PointF next;
            if (isBezierLine) {
                if (j != fList.size() - 1) {
                    next = fList.get(j + 1);
                    float wt = (pre.x + next.x) / 2;
                    PointF p1 = new PointF();
                    PointF p2 = new PointF();
                    p1.y = pre.y;
                    p1.x = wt;
                    p2.y = next.y;
                    p2.x = wt;
                    mCubicPath.cubicTo(p1.x, p1.y, p2.x, p2.y, next.x, next.y);
                }
            } else {
                mCubicPath.lineTo(pre.x, pre.y);
            }
        }
    }

    // isPlayAnim 是否开启动画
    private void drawCubicTo(Canvas canvas) {
        if (isPlayAnim) {
            mPathMeasure.setPath(mCubicPath, false);
            float length = mPathMeasure.getLength();
            //获取当前进度的路径，同时赋值给传入的mDstPath
            mPathMeasure.getSegment(0, mCurrentValue * length, mDstPath, true);
            canvas.drawPath(mDstPath, mPathPaint);
        } else {
            canvas.drawPath(mCubicPath, mPathPaint);
        }
    }

    /**
     * 滚动线程
     */
    private class FlingRunnable implements Runnable {

        private int mInitX;

        void start(int initX,
                   int velocityX,
                   int maxX) {
            this.mInitX = initX;
            // 先停止上一次的滚动
            stop();
            mScroller.fling(initX, 0, -velocityX,
                    0, 0, maxX, 0, 0);
            post(this);
        }

        @Override
        public void run() {
            // 如果已经结束，就不再进行
            if (!mScroller.computeScrollOffset()) {
                return;
            }
            // 计算偏移量
            int currX = mScroller.getCurrX();
            int diffX = mInitX - currX;

            // 用于记录是否超出边界，如果已经超出边界，则不再进行回调，即使滚动还没有完成
            boolean isEnd = false;
            if (diffX != 0) {
                // 超出右边界，进行修正
                if (getScrollX() + diffX >= mCanvasWidth - mWidth) {
                    diffX = (int) (mCanvasWidth - mWidth - getScrollX());
                    isEnd = true;
                }

                // 超出左边界，进行修正
                if (getScrollX() <= 0) {
                    diffX = -getScrollX();
                    isEnd = true;
                }

                if (!mScroller.isFinished()) {
                    scrollBy(diffX, 0);
                }
                mInitX = currX;
            }

            if (!isEnd) {
                post(this);
            }
        }

        /**
         * 进行停止
         */
        void stop() {
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
        }
    }

}
