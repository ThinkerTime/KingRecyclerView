package com.king.kingrecyclerview;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.king.lrecyclerview.CommonRecyclerViewHolder;
import com.king.lrecyclerview.LRecyclerView;
import com.king.lrecyclerview.MyAdapter;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private LRecyclerView mRecyclerView;
    private ArrayList<String> listData;
    private View mHeaderView,mHeaderView01;
    private View mEmptyView;

    private int refreshTime = 0;
    private int times = 0;
    private MyAdapter<String> mMyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (LRecyclerView) findViewById(R.id.recycler);

        //没有数据的时候显示
        mEmptyView = findViewById(R.id.text_empty);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setEmptyView(mEmptyView);

        listData = new ArrayList<String>();
        for (int i = 0; i < 15; i++) {
            listData.add("item" + (1 + listData.size()));
        }

//        mHeaderView = View.inflate(this, R.layout.item_header, null);
        mHeaderView = LayoutInflater.from(this).inflate(R.layout.item_header, mRecyclerView, false);
        mHeaderView01 = LayoutInflater.from(this).inflate(R.layout.item_header_01, mRecyclerView, false);

        mMyAdapter = new MyAdapter<String>(this, listData) {
            @Override
            public int getItemLayoutId(int viewType) {
                return R.layout.item;
            }

            @Override
            public void onBind(CommonRecyclerViewHolder viewHolder, int position, String data) {
                TextView tv = viewHolder.getView(R.id.item_tv);
                tv.setText(data);
            }
        };

        mRecyclerView.addHeaderView(mHeaderView);
        mRecyclerView.addHeaderView(mHeaderView01);

        mRecyclerView.setLoadingListener(new LRecyclerView.LoadingListener() {
            @Override
            public void onRefresh() {
                refreshTime ++;
                times = 0;

                new Handler().postDelayed(new Runnable(){
                    public void run() {
                        listData.clear();
                        for(int i = 0; i < 15 ;i++){
                            listData.add("item" + i + "after " + refreshTime + " times of refresh");
                        }
                        mMyAdapter.notifyDataSetChanged();
                        mRecyclerView.refreshComplete();
                    }

                }, 1000);
            }

            @Override
            public void onLoadMore() {
                if(times < 2){
                    new Handler().postDelayed(new Runnable(){
                        public void run() {
                            for(int i = 0; i < 15 ;i++){
                                listData.add("item" + (1 + listData.size() ) );
                            }
                            mRecyclerView.loadMoreComplete();
                            mMyAdapter.notifyDataSetChanged();
                        }
                    }, 1000);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            for(int i = 0; i < 9 ;i++){
                                listData.add("item" + (1 + listData.size() ) );
                            }
                            mRecyclerView.setNoMore(true);
                            mMyAdapter.notifyDataSetChanged();
                        }
                    }, 1000);
                }
                times ++;

            }
        });

        mRecyclerView.setAdapter(mMyAdapter);
        mRecyclerView.refresh();
    }
}
