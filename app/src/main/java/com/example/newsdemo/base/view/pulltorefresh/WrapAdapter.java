package com.example.newsdemo.base.view.pulltorefresh;

import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;

public class WrapAdapter<T extends RecyclerView.Adapter> extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final T mRealAdapter;
    private boolean isStaggeredGrid;
    private static final int BASE_HEADER_VIEW_TYPE = -1 << 10;
    private ArrayList<FixedViewInfo> mHeaderViewInfos = new ArrayList<>();

    public class FixedViewInfo {
        public View view;
        public int viewType;
    }

    WrapAdapter(T adapter) {
        super();
        mRealAdapter = adapter;
    }

    T getWrappedAdapter() {
        return mRealAdapter;
    }

    void addHeaderView(View view) {
        if (null == view) {
            throw new IllegalArgumentException("the view to add must not be null!");
        }
        final FixedViewInfo info = new FixedViewInfo();
        info.view = view;
        info.viewType = BASE_HEADER_VIEW_TYPE + mHeaderViewInfos.size();
        mHeaderViewInfos.add(info);
        notifyDataSetChanged();
    }

    public void adjustSpanSize(RecyclerView recycler) {
        if (recycler.getLayoutManager() instanceof GridLayoutManager) {
            final GridLayoutManager layoutManager = (GridLayoutManager)
                    recycler.getLayoutManager();
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    boolean isHeaderOrFooter = isHeaderPosition(position);
                    return isHeaderOrFooter ? layoutManager.getSpanCount() : 1;
                }
            });
        }
        if (recycler.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            this.isStaggeredGrid = true;
        }
    }

    private boolean isHeader(int viewType) {
        return viewType >= BASE_HEADER_VIEW_TYPE
                && viewType < (BASE_HEADER_VIEW_TYPE + mHeaderViewInfos.size());
    }

    private boolean isHeaderPosition(int position) {
        return position < mHeaderViewInfos.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup,
                                                      int viewType) {
        if (isHeader(viewType)) {
            int whichHeader = Math.abs(viewType - BASE_HEADER_VIEW_TYPE);
            View headerView = mHeaderViewInfos.get(whichHeader).view;
            return createHeaderFooterViewHolder(headerView);
        } else {
            return mRealAdapter.onCreateViewHolder(viewGroup, viewType);
        }
    }

    private RecyclerView.ViewHolder createHeaderFooterViewHolder(View view) {
        if (isStaggeredGrid) {
            StaggeredGridLayoutManager.LayoutParams params = new
                    StaggeredGridLayoutManager.LayoutParams(
                    StaggeredGridLayoutManager.LayoutParams.MATCH_PARENT,
                    StaggeredGridLayoutManager.LayoutParams.WRAP_CONTENT);
            params.setFullSpan(true);
            view.setLayoutParams(params);
        }
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (position < mHeaderViewInfos.size()) {
        } else if (position < mHeaderViewInfos.size() + mRealAdapter.getItemCount()) {
            mRealAdapter.onBindViewHolder(viewHolder,
                    position - mHeaderViewInfos.size());
        }
    }

    @Override
    public int getItemCount() {
        return mHeaderViewInfos.size() + mRealAdapter.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderPosition(position)) {
            return mHeaderViewInfos.get(position).viewType;
        } else {
            return mRealAdapter.getItemViewType(position - mHeaderViewInfos.size());
        }
    }
}
