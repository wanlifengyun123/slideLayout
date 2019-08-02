package com.example.newsdemo.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.KeyEvent;

import com.example.newsdemo.R;
import com.example.newsdemo.base.ToastUtils;
import com.example.newsdemo.adapter.MainViewPagerAdapter;
import com.example.newsdemo.base.status.StatusBarUtil;
import com.example.newsdemo.fragment.CountFragment;
import com.example.newsdemo.fragment.HomeFragment;
import com.example.newsdemo.fragment.MineFragment;
import com.example.newsdemo.fragment.VideoFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener{

    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;

    private MainViewPagerAdapter mAdapter;

    protected long mExitTime; //记录第一次点击时的时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        StatusBarUtil.setColor(this, getColor(R.color.colorPrimary));
        init();
    }

    private void init(){
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new HomeFragment());
        fragments.add(new CountFragment());
        fragments.add(new VideoFragment());
        fragments.add(new MineFragment());
        mAdapter = new MainViewPagerAdapter(getSupportFragmentManager(), 0);
        mViewPager.setAdapter(mAdapter);
        mAdapter.setFragments(fragments);
        mTabLayout.addOnTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if(mAdapter.getCount() > 0){
            mViewPager.setCurrentItem(tab.getPosition(), false);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                ToastUtils.show("再按一次退出黑马头条");
                mExitTime = System.currentTimeMillis();
            } else {
                MainActivity.this.finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
