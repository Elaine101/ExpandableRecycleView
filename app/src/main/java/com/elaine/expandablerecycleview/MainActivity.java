package com.elaine.expandablerecycleview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.elaine.expandablerecycleview.base.BaseExpandableRecyclerViewAdapter;
import com.elaine.expandablerecycleview.base.ViewProducer;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SampleGroupBean mCurrentSample;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<SampleGroupBean> list = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            final List<SampleChildBean> childList = new ArrayList<>(i);
            for (int j = 0; j < i; j++) {
                childList.add(new SampleChildBean("child " + i));
            }
            list.add(new SampleGroupBean(childList, "group " + i));
        }

        final RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final SampleAdapter adapter = new SampleAdapter(list);
        adapter.setEmptyViewProducer(new ViewProducer() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
                return new DefaultEmptyViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.empty, parent, false)
                );
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder) {

            }
        });
        adapter.setHeaderViewProducer(new ViewProducer() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
                return new DefaultEmptyViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.header, parent, false)
                );
            }
            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder) {
            }
        }, false);
        //允许加载更多child
        adapter.setEnableLoadMoreChild(true);
        adapter.setLoadMoreListener(new BaseExpandableRecyclerViewAdapter.LoadMoreListener() {

            @Override
            public void onLoadChildrenMore() {
                final List<SampleChildBean> childList = new ArrayList<>();
                if (mCurrentSample==null){
                    return;
                }
                if (mCurrentSample.getChildCount()<20){
                    for (int i = 0; i < 5; i++) {
                        childList.add(new SampleChildBean("child loadMore" + i));
                    }
                    adapter.addChildItemList(mCurrentSample,childList);
                }else {
                    adapter.setEnableLoadMoreChild(false);
                }
            }

            @Override
            public void onLoadGroupMore() {

            }
        });
        adapter.setOnclickListener(new BaseExpandableRecyclerViewAdapter.ExpandableRecyclerViewOnClickListener<SampleGroupBean, SampleChildBean>() {
            @Override
            public boolean onGroupLongClicked(SampleGroupBean groupItem) {
                groupItem.setmName("更改了名字"+groupItem.getName());
                adapter.notifyGroupItem(groupItem);
                return true;
            }

            @Override
            public boolean onInterceptGroupExpandEvent(SampleGroupBean groupItem, boolean isExpand) {
                return false;
            }

            @Override
            public void onGroupClicked(SampleGroupBean groupItem) {
                mCurrentSample = groupItem;
                Log.e("TAG", "onGroupClicked: "+mCurrentSample.getChildCount() );
                adapter.setEnableLoadMoreChild(true);
            }

            @Override
            public void onChildClicked(SampleGroupBean groupItem, SampleChildBean childItem) {
                childItem.setmName("更新了名字"+childItem.getName());
                adapter.notifyChildItem(groupItem,childItem);
            }
        });
        recyclerView.setAdapter(adapter);
    }
}
