package com.example.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class PColumn extends View {

    private Context mContext;

    private Paint mPaint;

    public PColumn(Context context) {
        this(context, null);
    }

    public PColumn(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PColumn(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initPaint();
    }

    private void initPaint(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        int color = mContext.getResources().getColor(R.color.colorPrimary);
        mPaint.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }
}
