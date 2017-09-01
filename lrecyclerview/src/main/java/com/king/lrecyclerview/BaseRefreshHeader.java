package com.king.lrecyclerview;

/**
 * author lrzg on 2017/8/29.
 * 描述：下拉加载接口
 */

public interface BaseRefreshHeader {

    int STATE_NORMAL = 0;
    int STATE_RELEASE_TO_REFRESH = 1;
    int STATE_REFRESHING = 2;
    int STATE_DONE = 3;

    /**
     * 移动下拉加载视图
     * @param delta
     */
    void onMove(float delta);

    boolean releaseAction();

    /**
     * 刷新完成
     */
    void refreshComplete();

}
