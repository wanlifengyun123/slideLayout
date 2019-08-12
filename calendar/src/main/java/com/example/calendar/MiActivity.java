package com.example.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.calendar.calendar.CaledarAdapter;
import com.example.calendar.calendar.CalendarBean;
import com.example.calendar.calendar.CalendarDateView;
import com.example.calendar.calendar.CalendarUtil;
import com.example.calendar.calendar.CalendarView;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Objects;

public class MiActivity extends AppCompatActivity {

    private TextView mTitleTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        //设置是否有返回箭头
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        //设置返回键可用
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mTitleTv = findViewById(R.id.title);
        mTitleTv.setText("");

        init();
    }

    private void init(){
        CalendarDateView mCalendarDateView = findViewById(R.id.calendarDateView);
        mCalendarDateView.setAdapter(new CaledarAdapter() {
            @Override
            public View getView(View convertView, ViewGroup parentView, CalendarBean bean) {
                //判断convertView为null，可以有效利用view的回收重用，左右滑动的效率高
                if (convertView == null) {
                    convertView = LayoutInflater.from(parentView.getContext()).inflate(R.layout.item_xiaomi, null);
                }

                TextView chinaText = (TextView) convertView.findViewById(R.id.chinaText);
                TextView text = (TextView) convertView.findViewById(R.id.text);

                text.setText("" + bean.day);
                //mothFlag 0是当月，-1是月前，1是月后
                if (bean.mothFlag != 0) {
                    text.setTextColor(0xff9299a1);
                } else {
                    text.setTextColor(0xff444444);
                }
                chinaText.setText(bean.chinaDay);

                return convertView;
            }
        });

        mCalendarDateView.setOnItemClickListener(new CalendarView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, CalendarBean bean) {
                mTitleTv.setText(MessageFormat.format("{0}/{1}/{2}", bean.year, bean.moth, bean.day));
            }
        });
        int[] data = CalendarUtil.getYMD(new Date());
        mTitleTv.setText(data[0] + "/" + data[1] + "/" + data[2]);
    }
}
