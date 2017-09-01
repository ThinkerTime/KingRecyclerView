package com.king.lrecyclerview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * author lrzg on 2017/8/28.
 * 描述：
 */

public class LRecyclerView  extends RecyclerView {
    private float mLastY = -1;
    private static final float DRAG_RATE = 3;
    private WrapAdapter mWrapAdapter;

    private List<Integer> mHeaderViewTypes = new ArrayList<>();//每个header必须有不同的type,不然滚动的时候顺序会变化
    private List<View> mHeaderViews = new ArrayList<>();

    private static final int TYPE_NORMAL = 1;
    private static final int TYPE_REFRESH_HEADER = 10000;//设置一个很大的数字,尽可能避免和用户的adapter冲突  下拉刷新
    private static final int TYPE_FOOTER = 10001; //上拉加载
    private static final int HEADER_INIT_INDEX = 10002; //头部


    private boolean pullRefreshEnabled = true;//上拉刷新功能
    private boolean loadingMoreEnabled = true;//下拉加载更多
    private boolean isLoadingData = false;//是否在加载数据
    private boolean isNoMore = false;//没有更多

    private ArrowRefreshHeader mRefreshHeader;
    private View mFootView;
    //adapter没有数据的时候显示,类似于listView的emptyView
    private View mEmptyView;

    private LoadingListener mLoadingListener;

    private final RecyclerView.AdapterDataObserver mDataObserver = new DataObserver();

    public LRecyclerView(Context context) {
        this(context, null);
    }

    public LRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if (pullRefreshEnabled) {
            mRefreshHeader = new ArrowRefreshHeader(getContext());
        }

        LoadingMoreFooter footView = new LoadingMoreFooter(getContext());
        mFootView = footView;
        mFootView.setVisibility(GONE);
    }

    /**
     * 必须在setAdapter之前添加
     *
     * @param view header
     */
    public void addHeaderView(View view) {
        mHeaderViewTypes.add(HEADER_INIT_INDEX + mHeaderViews.size());
        mHeaderViews.add(view);

        if (mWrapAdapter != null) {
            mWrapAdapter.notifyDataSetChanged();
        }
    }

    public void setEmptyView(View emptyView) {
        this.mEmptyView = emptyView;
        mDataObserver.onChanged();
    }
    public View getEmptyView() {
        return mEmptyView;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mWrapAdapter = new WrapAdapter(adapter);
        super.setAdapter(mWrapAdapter);
        adapter.registerAdapterDataObserver(mDataObserver);
        mDataObserver.onChanged();
    }


    private class WrapAdapter extends RecyclerView.Adapter<ViewHolder> {
        private RecyclerView.Adapter adapter;

        public WrapAdapter(RecyclerView.Adapter adapter) {
            this.adapter = adapter;
        }

        public int getHeadersCount() {
            return mHeaderViews.size();
        }

        public boolean isHeader(int position) {
            return position >= 1 && position < mHeaderViews.size() + 1;
//            return position >= 0 && position < mHeaderViews.size();
        }

        public boolean isRefreshHeader(int position) {
            return position == 0;
        }

        public boolean isFooter(int position) {
            if (loadingMoreEnabled) {
                return position == getItemCount() - 1;
            } else {
                return false;
            }
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.i("data", "viewType:" + viewType);


            //addRefreshHeader
            if (viewType == TYPE_REFRESH_HEADER) {
                return new CommonRecyclerViewHolder(mRefreshHeader);
            }

            //addHeaderView
            if (mHeaderViewTypes.contains(viewType)) {
                return new CommonRecyclerViewHolder(getHeaderViewByType(viewType));
            }

            if (viewType == TYPE_FOOTER) {
                return new CommonRecyclerViewHolder(mFootView);
            }

            return adapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (isHeader(position) || isRefreshHeader(position)) {
                return;
            }
            int curPosition = position - (getHeadersCount() + 1);
            int adapterCount;
            if (adapter != null) {
                adapterCount = adapter.getItemCount();
                if (curPosition < adapterCount) {
                    adapter.onBindViewHolder(holder, curPosition);
                }
            }
//            adapter.onBindViewHolder(holder, position);
        }

        @Override
        public int getItemCount() {
            if (getHeadersCount() > 0) {
                return getHeadersCount() + adapter.getItemCount() + 2;
            } else {
                return adapter.getItemCount();
            }

        }

        @Override
        public int getItemViewType(int position) {
            Log.i("data", "position:" + position);
//            int adjPosition = position - (getHeadersCount() + 1);
            int curPosition = position - getHeadersCount();

            if (isRefreshHeader(position)) {
                return TYPE_REFRESH_HEADER;
            } else if (isHeader(position)) {
                return mHeaderViewTypes.get(position - 1);
            } else if (isFooter(position)) {
                return TYPE_FOOTER;
            } else {
                return super.getItemViewType(curPosition);
            }

        }
    }

    //根据header的ViewType判断是哪个header
    private View getHeaderViewByType(int itemType) {
        Log.e("data", "itemType:" + itemType);
        Log.e("data", "flg:" + isHeaderType(itemType));
        if (!isHeaderType(itemType)) {
            return null;
        }
        return mHeaderViews.get(itemType - HEADER_INIT_INDEX);
    }

    //判断一个type是否为HeaderType
    private boolean isHeaderType(int itemViewType) {
        return mHeaderViews.size() > 0 && mHeaderViewTypes.contains(itemViewType);
    }

    private boolean isOnTop() {
        if (mRefreshHeader.getParent() != null) {
            return true;
        } else {
            return false;
        }
    }

    //处理下拉加载
    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        if (state == RecyclerView.SCROLL_STATE_IDLE && mLoadingListener != null && !isLoadingData && loadingMoreEnabled) {
            LayoutManager layoutManager = getLayoutManager();
            int lastVisibleItemPosition;
            if (layoutManager instanceof GridLayoutManager) {
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
                ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(into);
                lastVisibleItemPosition = findMax(into);
            } else {
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            }
            if (layoutManager.getChildCount() > 0 && lastVisibleItemPosition >= layoutManager.getItemCount() - 1 && layoutManager.getItemCount() > layoutManager.getChildCount() && !isNoMore && mRefreshHeader.getState() < ArrowRefreshHeader.STATE_REFRESHING) {
                isLoadingData = true;
                mFootView.setVisibility(View.VISIBLE);
                if (mFootView instanceof LoadingMoreFooter) {
                    ((LoadingMoreFooter) mFootView).setState(LoadingMoreFooter.STATE_LOADING);
                } else {
                    mFootView.setVisibility(View.VISIBLE);
                }
                mLoadingListener.onLoadMore();

            }
        }

    }

    //处理上啦刷新
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (mLastY == -1) {
            mLastY = ev.getRawY();
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                mLastY = ev.getRawY();
                if (isOnTop() && pullRefreshEnabled) {
                    Log.d("data", "deltaY#" + deltaY);
                    mRefreshHeader.onMove(deltaY / DRAG_RATE);
                    if (mRefreshHeader.getVisibleHeight() > 0 && mRefreshHeader.getState() < ArrowRefreshHeader.STATE_REFRESHING) {
                        return false;
                    }
                }
                break;
            default:
                mLastY = -1; // reset
                if (isOnTop() && pullRefreshEnabled) {
                    if (mRefreshHeader.releaseAction()) {
                        if (mLoadingListener != null) {
                            mLoadingListener.onRefresh();
                        }
                    }
                }
                break;
        }

        return super.onTouchEvent(ev);
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    /**
     * 刷新完成
     */
    public void refreshComplete() {
        mRefreshHeader.refreshComplete();
        setNoMore(false);
    }

    /**
     * 加载更多完成而且有数据
     */
    public void loadMoreComplete() {
        isLoadingData = false;
        if (mFootView instanceof LoadingMoreFooter) {
            ((LoadingMoreFooter) mFootView).setState(LoadingMoreFooter.STATE_COMPLETE);
        } else {
            mFootView.setVisibility(View.GONE);
        }
    }
    /**
     * 加载更多完成而且没有数据
     */
    public void setNoMore(boolean noMore){
        isLoadingData = false;
        isNoMore = noMore;
        if (mFootView instanceof LoadingMoreFooter) {
            ((LoadingMoreFooter) mFootView).setState(isNoMore ? LoadingMoreFooter.STATE_NOMORE:LoadingMoreFooter.STATE_COMPLETE);
        } else {
            mFootView.setVisibility(View.GONE);
        }
    }

    /**
     * 进入刷新
     */
    public void refresh() {
        if (pullRefreshEnabled && mLoadingListener != null) {
            mRefreshHeader.setState(ArrowRefreshHeader.STATE_REFRESHING);
            mLoadingListener.onRefresh();
        }
    }

    private class DataObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            if (mWrapAdapter != null) {
                mWrapAdapter.notifyDataSetChanged();
            }
            if (mWrapAdapter != null && mEmptyView != null) {
                int emptyCount = 1 + mWrapAdapter.getHeadersCount();
                if (loadingMoreEnabled) {
                    emptyCount++;
                }
                if (mWrapAdapter.getItemCount() == emptyCount) {
                    mEmptyView.setVisibility(View.VISIBLE);
                    LRecyclerView.this.setVisibility(View.GONE);
                } else {
                    mEmptyView.setVisibility(View.GONE);
                    LRecyclerView.this.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mWrapAdapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mWrapAdapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mWrapAdapter.notifyItemMoved(fromPosition, toPosition);
        }
    }

    ;


    public void setLoadingListener(LoadingListener listener) {
        mLoadingListener = listener;
    }

    public interface LoadingListener {

        void onRefresh();

        void onLoadMore();
    }
}
