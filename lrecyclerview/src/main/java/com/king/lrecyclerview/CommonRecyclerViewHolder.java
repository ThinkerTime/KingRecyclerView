package com.king.lrecyclerview;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

/**
 * author lrzg on 2017/8/28.
 * 描述：
 */

public class CommonRecyclerViewHolder extends RecyclerView.ViewHolder{
    private SparseArray<View> mViews;//集合类，layout里包含的View,以view的id作为key，value是view对象

    public CommonRecyclerViewHolder(View itemView) {
        super(itemView);
        mViews = new SparseArray<>();
    }

    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = itemView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }
}
