package com.example.phone_server;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.phone_server.list.SlideLayout;

public class SimpleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh);

        ListView listView = findViewById(R.id.lv_contact);

        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 10;
            }

            @SuppressLint("DefaultLocale")
            @Override
            public String getItem(int position) {
                return String.format("item - %d", position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            SlideLayout mSlideLayout;

            @SuppressLint("DefaultLocale")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final ViewHolder holder;
                if(convertView == null){
                    holder = new ViewHolder();
                    convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slide_list, parent, false);
                    holder.slideLayout = convertView.findViewById(R.id.slide_layout);
                    holder.textView = convertView.findViewById(R.id.item_name);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.textView.setText(String.format("item - %d", position));
                holder.slideLayout.setOnSlideChangeListener(new SlideLayout.onSlideChangeListener() {
                    @Override
                    public void onMenuOpen(SlideLayout slideLayout) {
                        mSlideLayout = slideLayout;
                    }

                    @Override
                    public void onMenuClose(SlideLayout slideLayout) {
                        if(mSlideLayout != null){
                            mSlideLayout = null;
                        }
                    }

                    @Override
                    public void onClick(SlideLayout slideLayout) {
                        if(mSlideLayout != null){
                            mSlideLayout.closeMenu();
                        }
                    }
                });
                return convertView;
            }

            class ViewHolder {
                SlideLayout slideLayout;
                TextView textView;
            }
        });
    }
}
