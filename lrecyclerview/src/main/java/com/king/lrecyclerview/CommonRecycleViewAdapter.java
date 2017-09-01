package com.king.lrecyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * author lrzg on 2017/8/28.
 * 描述：
 */

public abstract class CommonRecycleViewAdapter<T> extends RecyclerView.Adapter<CommonRecyclerViewHolder>{

    public List<T> mDataList;
    public Context mContext;
    protected LayoutInflater mLayoutInflater;
    private List<View> mHeaderViews = new ArrayList<>();

    private List<Integer> mHeaderViewTypes = new ArrayList<>();//每个header必须有不同的type,不然滚动的时候顺序会变化
    private List<Integer> mFooterViewTypes = new ArrayList<>();

    private static final int TYPE_NORMAL = 1;
    private static final int TYPE_REFRESH_HEADER = 10000;//设置一个很大的数字,尽可能避免和用户的adapter冲突  下拉刷新
    private static final int HEADER_INIT_INDEX = 10002;


    private ArrowRefreshHeader mRefreshHeader;
    private boolean pullRefreshEnabled = true;//上拉刷新功能

    public CommonRecycleViewAdapter(Context context){
        this.mContext = context;
    }

    public CommonRecycleViewAdapter(Context context, List<T> datas) {
        this.mContext = context;
        this.mDataList = datas;
        mLayoutInflater = LayoutInflater.from(mContext);
        init();
    }

    private void init() {
        if (pullRefreshEnabled) {
            mRefreshHeader = new ArrowRefreshHeader(mContext);
        }
    }


    @Override
    public CommonRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Log.i("data","viewType:"+viewType);
        if (viewType == TYPE_REFRESH_HEADER) {
            return new CommonRecyclerViewHolder(mRefreshHeader);
        }

        if(viewType == HEADER_INIT_INDEX){
            return new CommonRecyclerViewHolder(mHeaderViews.get(0));
        }
        return getViewHolder(parent, viewType);
    }

    @Override
    public int getItemCount() {
        return getDataItemCount() + mHeaderViews.size() + 1;
    }

    public int getDataItemCount() {
        return mDataList.size();
    }

    @Override
    public void onBindViewHolder(CommonRecyclerViewHolder holder, int position) {
//        if(!isHeader(position)){
        if(!isRefreshHeader(position) && !isHeader(position)){
            position = position - (getHeadersCount() + 1);
            final T data = mDataList.get(position);
            onBind(holder, position,data);
        }
//        int adjPosition = position - (getHeadersCount() + 1);


    }

    public CommonRecyclerViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new CommonRecyclerViewHolder(mLayoutInflater.inflate(getItemLayoutId(viewType), parent, false));
    }

    /**
     * 必须在setAdapter之前添加
     *
     * @param view header
     */
    public void addHeaderView(View view) {
        mHeaderViewTypes.add(HEADER_INIT_INDEX + mHeaderViews.size());
        mHeaderViews.add(view);
    }

    //判断一个type是否为HeaderType
    private boolean isHeaderType(int itemViewType) {
        return  mHeaderViews.size() > 0 &&  mHeaderViewTypes.contains(itemViewType);
    }
    public boolean isHeader(int position) {
//        return mHeaderViews.size() > 0 && position < mHeaderViews.size();
        return  mHeaderViews.size() > 0 && position >= 1 && position < mHeaderViews.size() + 1;
    }

    public boolean isRefreshHeader(int position) {
        return position == 0;
    }

    @Override
    public int getItemViewType(int position) {
        int adjPosition = position - (getHeadersCount() + 1);

        Log.e("data","position:"+position);
        if (isRefreshHeader(position)) {
            return TYPE_REFRESH_HEADER;
        }else if(isHeader(position)){
            position = position - getHeadersCount();
            return mHeaderViewTypes.get(position);
        }else {
            return getItemType(adjPosition);
        }

//        if (isHeader(position)) {
//            return mHeaderViewTypes.get(position);
//        }

//        return getItemType(getRealPosition(position));
    }

    public int getItemType(int position) {
        return TYPE_NORMAL;
    }

    public int getRealPosition(int position) {
        return position - mHeaderViews.size();
    }
    public int getHeadersCount() {
        return mHeaderViews.size();
    }

    /**
     * item 布局id
     *
     * @param viewType item type
     * @return item 布局id
     */
    abstract public int getItemLayoutId(int viewType);
    /**
     * 绑定数据
     *
     * @param viewHolder holder
     * @param position   pos
     * @param data       数据源
     */
    public abstract void onBind(CommonRecyclerViewHolder viewHolder, int position, T data);


}
