package com.king.lrecyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

/**
 * author lrzg on 2017/8/28.
 * 描述：
 */

public abstract class MyAdapter<T> extends RecyclerView.Adapter<CommonRecyclerViewHolder>{

    public List<T> mDataList;
    public Context mContext;
    protected LayoutInflater mLayoutInflater;

    public MyAdapter(Context context, List<T> datas) {
        this.mContext = context;
        this.mDataList = datas;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public CommonRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CommonRecyclerViewHolder(mLayoutInflater.inflate(getItemLayoutId(viewType), parent, false));
    }

    @Override
    public void onBindViewHolder(CommonRecyclerViewHolder holder, int position) {
        final T data = mDataList.get(position);
        onBind(holder, position,data);
    }
    @Override
    public int getItemCount() {
        return mDataList.size();
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
