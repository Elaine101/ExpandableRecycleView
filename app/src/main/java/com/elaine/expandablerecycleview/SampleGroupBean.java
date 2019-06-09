package com.elaine.expandablerecycleview;

import android.support.annotation.NonNull;

import com.elaine.expandablerecycleview.base.BaseExpandableRecyclerViewAdapter;

import java.util.List;

class SampleGroupBean implements BaseExpandableRecyclerViewAdapter.BaseGroupBean<SampleChildBean> {

    private List<SampleChildBean> mList;
    private String mName;

    SampleGroupBean(@NonNull List<SampleChildBean> list, @NonNull String name) {
        mList = list;
        mName = name;
    }

    @Override
    public int getChildCount() {
        return mList.size();
    }

    @Override
    public boolean isExpandable() {
        return getChildCount() > 0;
    }

    @Override
    public int getChildIndex(SampleChildBean sampleChildBean) {
        return mList.indexOf(sampleChildBean);
    }

    @Override
    public void addChildBean(List<SampleChildBean> sampleChildBeans) {
        mList.addAll(sampleChildBeans);
    }

    public String getName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    @Override
    public SampleChildBean getChildAt(int index) {
        return mList.size() <= index ? null : mList.get(index);
    }
}
