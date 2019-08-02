package com.example.newsdemo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.newsdemo.R;
import com.example.newsdemo.adapter.HomeAdapter;
import com.example.newsdemo.base.view.pulltorefresh.PullToRefreshView;
import com.example.newsdemo.base.view.pulltorefresh.WrapRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HomeFragment extends Fragment {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.pull_to_refresh)
    PullToRefreshView mPullToRefreshView;

    @BindView(R.id.recycler_view)
    WrapRecyclerView mRecyclerView;

    private HomeAdapter mAdapter;

    private Unbinder mUnBinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnBinder = ButterKnife.bind(this, view);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(mToolbar);

        mToolbar.setTitle(null);
        mTvTitle.setText(getResources().getString(R.string.tab_home));

        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(manager);
        mAdapter = new HomeAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mPullToRefreshView.setOnRefreshListener(() -> mPullToRefreshView.postDelayed(() -> {
            mPullToRefreshView.setRefreshing(false);
            loadData();
        }, 1000));
    }

    private void loadData(){
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add("Item -- " + i);
        }
        mAdapter.setData(data);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        mUnBinder.unbind();
        super.onDestroyView();
    }
}
