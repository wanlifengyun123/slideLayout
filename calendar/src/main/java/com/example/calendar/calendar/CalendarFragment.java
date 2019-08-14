package com.example.calendar.calendar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendar.R;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarFragment extends Fragment {

    private static final String REAL_POS = "real_pos";

    private List<CalendarBean> mCalendarBeans;

    private ImageView imageView;

    static CalendarFragment getInstance(int pos) {
        CalendarFragment fragment = new CalendarFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(REAL_POS, pos);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recycler, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            int pos = getArguments().getInt(REAL_POS);
            mCalendarBeans = getCurrentCal(pos);
        }
        RecyclerView mRecyclerView = view.findViewById(R.id.recycleView);
        mRecyclerView.setNestedScrollingEnabled(false); // 禁止滑动
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 7);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(new CalendarRecyclerAdapter());

    }

    private List<CalendarBean> getCurrentCal(int pos){
        int[] ymd = CalendarUtil.getYMD(new Date(System.currentTimeMillis()));
        if(pos == 0){
            return CalendarFactory.getMonthOfDayList(ymd[0], ymd[1]);
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(System.currentTimeMillis()));
            cal.set(ymd[0], ymd[1] + pos - 1, ymd[2]);
            int y = cal.get(Calendar.YEAR);
            int m = cal.get(Calendar.MONTH) + 1;
            return CalendarFactory.getMonthOfDayList(y, m);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(imageView != null){
            imageView.setBackgroundResource(R.drawable.item_shape_transparent);
        }
    }

    class CalendarRecyclerAdapter extends RecyclerView.Adapter {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(getContext()).inflate(R.layout.item_calendar, parent, false);
            return new CalendarViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            final CalendarViewHolder holder = (CalendarViewHolder) viewHolder;
            CalendarBean calendarBean = mCalendarBeans.get(position);
            holder.tvDate.setText(String.valueOf(calendarBean.day));
            holder.tvChainDate.setText(calendarBean.chinaDay);
            // mothFlag 0是当月，-1是月前，1是月后
            if (calendarBean.mothFlag != 0) {
                holder.tvDate.setTextColor(getResources().getColor(R.color.gray, null));
            } else {
                if(calendarBean.isToday){
                    holder.tvDate.setTextColor(getResources().getColor(R.color.gray, null));
                } else {
                    holder.tvDate.setTextColor(getResources().getColor(R.color.black, null));
                }
            }
            holder.tvChainDate.setTextColor(getResources().getColor(R.color.gray, null));
            if(calendarBean.isToday){
                holder.ivBack.setBackgroundResource(R.drawable.item_shape_red);
            } else {
                holder.ivBack.setBackgroundResource(R.drawable.item_shape_transparent);
            }
            if(calendarBean.mothFlag == 0){
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(imageView != null){
                            imageView.setBackgroundResource(R.drawable.item_shape_transparent);
                        }
                        imageView = holder.ivBack;
                        holder.ivBack.setBackgroundResource(R.drawable.item_shape_blue);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mCalendarBeans == null ? 1 : mCalendarBeans.size();
        }

        class CalendarViewHolder extends RecyclerView.ViewHolder {

            TextView tvDate;
            TextView tvChainDate;
            ImageView ivBack;

            CalendarViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.dateTv);
                tvChainDate = itemView.findViewById(R.id.chinaDateTv);
                ivBack = itemView.findViewById(R.id.background);
            }
        }
    }
}
