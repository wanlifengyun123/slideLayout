package com.example.chart.util;

import android.annotation.SuppressLint;
import android.content.Context;

public class DisplayUtil {

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    public static void init(Context context){
        mContext = context;
    }


    /**
     * 将dp转换成px
     *
     * @param dpValue dpValue
     * @return int
     */
    public static int dp2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 将像素转换成dp
     *
     * @param pxValue pxValue
     * @return int
     */
    public static int px2dp(float pxValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

}
