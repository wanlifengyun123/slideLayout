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
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

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

    private int mRectWidth; // 矩形柱体的宽度

    private Path mCubicPath; // 曲线图路径对象
    private List<PointF> mPointPathList; // 记录柱状图的定点坐标
    private List<PointF> fList; // 记录曲线图的定点坐标

    private Path mDstPath; // 路径绘制每段截取出来的路径
    private PathMeasure mPathMeasure; //路径测量类
    private ValueAnimator mValueAnimator;
    private float mCurrentValue;

    private boolean isInitialized = false;

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
        mRectWidth = DisplayUtil.dp2px(15);
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

    public void start() {
        if (mValueAnimator != null) {
            mValueAnimator.start();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = getWidth();
        mHeight = getHeight();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mWidth == 0 || mHeight == 0) {
            return;
        }
        float yEndPoint = mHeight - yPaddingPoint;
        float xEndPoint = mWidth - xPaddingPoint;
        // 画横线
        canvas.drawLine(xPaddingPoint, yEndPoint, xEndPoint, yEndPoint, mLinePaint);
        // 画竖线
        canvas.drawLine(xPaddingPoint, yEndPoint, xPaddingPoint, yPaddingPoint, mLinePaint);
        // 显示竖坐标文字
        for (int i = 0; i < 5; i++) {
            String yName = String.valueOf(i);
            canvas.drawText(yName, (float) (xPaddingPoint * 0.5), yEndPoint - (yEndPoint / 5) * i, mTextPaint);
            canvas.drawLine(xPaddingPoint, yEndPoint - (yEndPoint / 5) * i, xEndPoint, yEndPoint - (yEndPoint / 5) * i, mLinePaint);
        }
        // 显示横坐标文字,
        float startX = (float) (xPaddingPoint + mRectWidth); //显示开始的x坐标, 开始位置距y轴mRectWidth
        float offsetX = (float) (mRectWidth * 0.5); // 文字相对于柱状图的中心位置偏移量
        for (int i = 0; i < 15; i++) {
            String yName = String.valueOf(i);
            if (i == 0) {
                // 第一个坐标位于 开始坐标 +  xPaddingPoint的一半
                canvas.drawText(yName, startX + offsetX, yEndPoint + (float) (yPaddingPoint * 0.5), mTextPaint);
            } else {
                canvas.drawText(yName, startX + offsetX + (xEndPoint / 15) * i, yEndPoint + (float) (yPaddingPoint * 0.5), mTextPaint);
            }
        }
        // 画柱状图
        if (!isInitialized) {
            @SuppressLint("DrawAllocation")
            Random random = new Random();
            mPointPathList.clear();
            for (int i = 0; i < 15; i++) {
                int randomNumber = random.nextInt((mHeight - 2 * yPaddingPoint));
                if (i == 0) {
                    mRectF.left = startX;
                } else {
                    mRectF.left = startX + (xEndPoint / 15) * i;
                }
                mRectF.right = mRectF.left + mRectWidth;
                mRectF.top = yEndPoint - randomNumber;
                mRectF.bottom = yEndPoint;
                canvas.drawRect(mRectF, mRectPaint);
                // 取圆柱体的x中心点
                mPointPathList.add(new PointF((float) (mRectF.left + mRectWidth * 0.5), mRectF.top));
            }
            isInitialized = true;
        } else {
            for (int i = 0; i < mPointPathList.size(); i++) {
                if (i == 0) {
                    mRectF.left = startX;
                } else {
                    mRectF.left = startX + (xEndPoint / 15) * i;
                }
                mRectF.left = startX + (xEndPoint / 15) * i;
                mRectF.right = mRectF.left + mRectWidth;
                // 因添加原点位置， 所以每次 i+1
                mRectF.top = yEndPoint - mCurrentValue * (yEndPoint - mPointPathList.get(i).y);
                mRectF.bottom = yEndPoint;
                canvas.drawRect(mRectF, mRectPaint);
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
        cubicTo(true);
        mPathPaint.setStyle(Paint.Style.FILL);
        mCubicPath.lineTo(fList.get(fList.size() - 1).x, yEndPoint);
        mCubicPath.close();
        drawCubicTo(canvas, true);
    }

    // 画二阶贝塞尔曲线 isBezierLine ： 是否画曲线还是直线图
    private void cubicTo(boolean isBezierLine) {
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
    private void drawCubicTo(Canvas canvas, boolean isPlayAnim) {
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

}