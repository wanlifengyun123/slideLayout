package com.example.chart;

import android.content.Context;

public class DisplayUtil {

    static Context mContext;

    public static void init(Context context){
        mContext = context;
    }


    /**
     * 单位转换: dp -> px
     *
     * @param dp
     * @return
     */
    public static int dp2px( float dp) {
        return (int) (mContext.getResources().getDisplayMetrics().density * dp + 0.5);
    }

}
