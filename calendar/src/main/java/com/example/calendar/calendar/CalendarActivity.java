package com.example.calendar.calendar;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.calendar.R;

import java.util.Calendar;
import java.util.Date;

public class CalendarActivity extends AppCompatActivity {

    private static final int MAX_VALUE = Integer.MAX_VALUE;

    ViewPager mViewPager;

    TextView mTitleView;

    int mCurIndex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        mTitleView = findViewById(R.id.title);
        mViewPager = findViewById(R.id.viewPager);
        mViewPager.setAdapter(new CalendarPageAdapter(getSupportFragmentManager()));

        mCurIndex = MAX_VALUE / 2;
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurIndex = position - MAX_VALUE / 2;
                setCurrentDate();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setCurrentItem(mCurIndex);
    }

    private void setCurrentDate(){
        int[] ymd = CalendarUtil.getYMD(new Date(System.currentTimeMillis()));
        if(mCurIndex == 0){
            mTitleView.setText(String.format("%s-%s", ymd[0], ymd[1]));
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(System.currentTimeMillis()));
            cal.set(ymd[0], ymd[1] - 1 + mCurIndex, ymd[2]);
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            mTitleView.setText(String.format("%s-%s", year, month));
        }
    }

    public void preMonth(View view){
        mCurIndex--;
        setCurrentDate();
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
    }

    public void nextMonth(View view){
        mCurIndex++;
        setCurrentDate();
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
    }

    public class CalendarPageAdapter extends FragmentPagerAdapter {

        CalendarPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            int realPos = position - getCount() / 2;
            return CalendarFragment.getInstance(realPos);
        }

        @Override
        public int getCount() {
            return MAX_VALUE;
        }
    }
}
