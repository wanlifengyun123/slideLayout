package com.example.newsdemo.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    public static void init(Context context){
        mContext = context.getApplicationContext();
    }

    public static void show(String text){
        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
    }

    public static void show(int textId){
        Toast.makeText(mContext, mContext.getResources().getString(textId), Toast.LENGTH_SHORT).show();
    }

    public static void show(CharSequence text){
        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
    }
}
