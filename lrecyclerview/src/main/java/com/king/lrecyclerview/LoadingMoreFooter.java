package com.king.lrecyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * author lrzg on 2017/8/29.
 * 描述：加载更多
 */

public class LoadingMoreFooter extends LinearLayout {

    private LinearLayout mContainer;
    private ProgressBar mProgressBar;
    private TextView mStatusTextView;
    public final static int STATE_LOADING = 0;
    public final static int STATE_COMPLETE = 1;
    public final static int STATE_NOMORE = 2;

    private String loadingHint;
    private String noMoreHint;
    private String loadingDoneHint;

    public LoadingMoreFooter(Context context) {
        super(context);
        initView();
    }

    /**
     * @param context
     * @param attrs
     */
    public LoadingMoreFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        mContainer = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.recycler_loading_more_footer, null);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 0);
        this.setLayoutParams(lp);
        this.setPadding(0, 0, 0, 0);

//        addView(mContainer, new LayoutParams(LayoutParams.MATCH_PARENT, 0));
        addView(mContainer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setGravity(Gravity.BOTTOM);

        mProgressBar = (ProgressBar)findViewById(R.id.listview_footer_progressbar);
        mStatusTextView = (TextView) findViewById(R.id.loading_status_textview);
        mStatusTextView.setText("正在加载...");

        loadingHint = (String)getContext().getText(R.string.listview_loading);
        noMoreHint = (String)getContext().getText(R.string.nomore_loading);
        loadingDoneHint = (String)getContext().getText(R.string.loading_done);


        measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void setLoadingHint(String hint) {
        loadingHint = hint;
    }

    public void setNoMoreHint(String hint) {
        noMoreHint = hint;
    }

    public void setLoadingDoneHint(String hint) {
        loadingDoneHint = hint;
    }


    public void  setState(int state) {
        switch(state) {
            case STATE_LOADING:
                mProgressBar.setVisibility(View.VISIBLE);
                mStatusTextView.setText(loadingHint);
                this.setVisibility(View.VISIBLE);
                break;
            case STATE_COMPLETE:
                mStatusTextView.setText(loadingDoneHint);
                this.setVisibility(View.GONE);
                break;
            case STATE_NOMORE:
                mStatusTextView.setText(noMoreHint);
                mProgressBar.setVisibility(View.GONE);
                this.setVisibility(View.VISIBLE);
                break;
        }

    }
}
