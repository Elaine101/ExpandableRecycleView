/**
 * BaseExpandableRecyclerViewAdapter
 * https://github.com/hgDendi/ExpandableRecyclerView
 * <p>
 * Copyright (c) 2017 hg.dendi
 * <p>
 * MIT License
 * https://rem.mit-license.org/
 * <p>
 * email: elaine10lyl@163.com
 * Date: 2019-06-09
 */

package com.elaine.expandablerecycleview.base;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public abstract class BaseExpandableRecyclerViewAdapter
        <GroupBean extends BaseExpandableRecyclerViewAdapter.BaseGroupBean<ChildBean>,
                ChildBean,
                GroupViewHolder extends BaseExpandableRecyclerViewAdapter.BaseGroupViewHolder,
                ChildViewHolder extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "BaseExpandableRecyclerV";

    private static final Object EXPAND_PAYLOAD = new Object();

    private static final int TYPE_EMPTY = ViewProducer.VIEW_TYPE_EMPTY;
    protected static final int TYPE_HEADER = ViewProducer.VIEW_TYPE_HEADER;
    private static final int TYPE_GROUP = ViewProducer.VIEW_TYPE_EMPTY >> 2;
    private static final int TYPE_CHILD = ViewProducer.VIEW_TYPE_EMPTY >> 3;
    protected static final int TYPE_MASK = TYPE_GROUP | TYPE_CHILD | TYPE_EMPTY | TYPE_HEADER;

    private Set<GroupBean> mExpandGroupSet;
    private ExpandableRecyclerViewOnClickListener<GroupBean, ChildBean> mOnClickListener;

    private boolean mIsEmpty;
    protected boolean mShowHeaderViewWhenEmpty;
    private ViewProducer mEmptyViewProducer;
    protected ViewProducer mHeaderViewProducer;


    protected LoadMoreListener mLoadMoreListener;
    protected boolean enableLoadMoreChild;
    protected boolean enableLoadMoreGroup;

    public BaseExpandableRecyclerViewAdapter() {
        mExpandGroupSet = new HashSet<>();
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                // after notifyDataSetChange(),clear outdated list
                List<GroupBean> retainItem = new ArrayList<>();
                for (int i = 0; i < getGroupCount(); i++) {
                    GroupBean groupBean = getGroupItem(i);
                    if (mExpandGroupSet.contains(groupBean)) {
                        retainItem.add(groupBean);
                    }
                }
                mExpandGroupSet.clear();
                mExpandGroupSet.addAll(retainItem);
            }
        });
    }

    /**
     * get group count
     *
     * @return group count
     */
    abstract public int getGroupCount();

    /**
     * get groupItem related to GroupCount
     *
     * @param groupIndex the index of group item in group list
     * @return related GroupBean
     */
    abstract public GroupBean getGroupItem(int groupIndex);

    protected int getGroupType(GroupBean groupBean) {
        return 0;
    }

    /**
     * create {@link GroupViewHolder} for group item
     *
     * @param parent
     * @return
     */
    abstract public GroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int groupViewType);

    /**
     * bind {@link GroupViewHolder}
     *
     * @param holder
     * @param groupBean
     * @param isExpand
     */
    abstract public void onBindGroupViewHolder(GroupViewHolder holder, GroupBean groupBean, boolean isExpand);

    /**
     * bind {@link GroupViewHolder} with payload , used to invalidate partially
     *
     * @param holder
     * @param groupBean
     * @param isExpand
     * @param payload
     */
    protected void onBindGroupViewHolder(GroupViewHolder holder, GroupBean groupBean, boolean isExpand, List<Object> payload) {
        onBindGroupViewHolder(holder, groupBean, isExpand);
    }

    protected int getChildType(GroupBean groupBean, ChildBean childBean) {
        return 0;
    }

    /**
     * create {@link ChildViewHolder} for child item
     *
     * @param parent
     * @return
     */
    abstract public ChildViewHolder onCreateChildViewHolder(ViewGroup parent, int childViewType);

    /**
     * bind {@link ChildViewHolder}
     *
     * @param holder
     * @param groupBean
     * @param childBean
     */
    abstract public void onBindChildViewHolder(ChildViewHolder holder, GroupBean groupBean, ChildBean childBean);


    /**
     * bind {@link ChildViewHolder} with payload , used to invalidate partially
     *
     * @param holder
     * @param groupBean
     * @param childBean
     * @param payload
     */
    protected void onBindChildViewHolder(ChildViewHolder holder, GroupBean groupBean, ChildBean childBean, List<Object> payload) {
        onBindChildViewHolder(holder, groupBean, childBean);
        if ((groupBean.getChildIndex(childBean)==groupBean.getChildCount()-1)){
            //when child is the last one in group's child list ,get more child
            if (enableLoadMoreChild&&mLoadMoreListener!=null){
                mLoadMoreListener.onLoadChildrenMore();
            }
            mLoadMoreListener.onLoadChildrenMore();
            Log.e(TAG, "onLoadChildrenMore: " );
        }
    }


    public void setEmptyViewProducer(ViewProducer emptyViewProducer) {
        if (mEmptyViewProducer != emptyViewProducer) {
            mEmptyViewProducer = emptyViewProducer;
            if (mIsEmpty) {
                notifyDataSetChanged();
            }
        }
    }


    public void setHeaderViewProducer(ViewProducer headerViewProducer, boolean showWhenEmpty) {
        mShowHeaderViewWhenEmpty = showWhenEmpty;
        if (mHeaderViewProducer != headerViewProducer) {
            mHeaderViewProducer = headerViewProducer;
            notifyDataSetChanged();
        }
    }

    public final void setOnclickListener(ExpandableRecyclerViewOnClickListener<GroupBean, ChildBean> listener) {
        mOnClickListener = listener;
    }

    public final boolean isGroupExpanding(GroupBean groupBean) {
        return mExpandGroupSet.contains(groupBean);
    }

    /**
     * update  childItem
     * @param groupBean
     * @param childBean
     */
    public void notifyChildItem(GroupBean groupBean,ChildBean childBean){
        int groupPosition =getAdapterPosition(getGroupIndex(groupBean));
        int childIndex = groupBean.getChildIndex(childBean);
        notifyItemChanged(groupPosition+childIndex+1,EXPAND_PAYLOAD);
    }

    public void addChildItemList(GroupBean groupBean,List<ChildBean> childBeanList ){
        groupBean.addChildBean(childBeanList);
        //if you don't use handle ，will crash cause by IllegalStateException
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });

    }

    /**
     * uodate groupItem;
     * @param groupBean
     */
    public void notifyGroupItem(GroupBean groupBean){
        notifyItemChanged(getAdapterPosition(getGroupIndex(groupBean)));
    }

    public final boolean expandGroup(GroupBean groupBean) {
        if (groupBean.isExpandable() && !isGroupExpanding(groupBean)) {
            mExpandGroupSet.add(groupBean);
            final int position = getAdapterPosition(getGroupIndex(groupBean));
            notifyItemRangeInserted(position + 1, groupBean.getChildCount());
            notifyItemChanged(position, EXPAND_PAYLOAD);
            return true;
        }
        return false;
    }

    public final void foldAll() {
        Iterator<GroupBean> iter = mExpandGroupSet.iterator();
        while (iter.hasNext()) {
            GroupBean groupBean = iter.next();
            final int position = getAdapterPosition(getGroupIndex(groupBean));
            notifyItemRangeRemoved(position + 1, groupBean.getChildCount());
            notifyItemChanged(position, EXPAND_PAYLOAD);
            iter.remove();
        }
    }

    public final boolean foldGroup(GroupBean groupBean) {
        if (mExpandGroupSet.remove(groupBean)) {
            final int position = getAdapterPosition(getGroupIndex(groupBean));
            notifyItemRangeRemoved(position + 1, groupBean.getChildCount());
            notifyItemChanged(position, EXPAND_PAYLOAD);
            return true;
        }
        return false;
    }

    @Override
    public final int getItemCount() {
        int result = getGroupCount();
        if (result == 0 && mEmptyViewProducer != null) {
            mIsEmpty = true;
            return mHeaderViewProducer != null && mShowHeaderViewWhenEmpty ? 2 : 1;
        }
        mIsEmpty = false;
        for (GroupBean groupBean : mExpandGroupSet) {
            if (getGroupIndex(groupBean) < 0) {
                Log.e(TAG, "invalid index in expandgroupList : " + groupBean);
                continue;
            }
            result += groupBean.getChildCount();
        }
        if (mHeaderViewProducer != null) {
            result++;
        }
        return result;
    }

    public final int getAdapterPosition(int groupIndex) {
        int result = groupIndex;
        for (GroupBean groupBean : mExpandGroupSet) {
            if (getGroupIndex(groupBean) >= 0 && getGroupIndex(groupBean) < groupIndex) {
                result += groupBean.getChildCount();
            }
        }
        if (mHeaderViewProducer != null) {
            result++;
        }
        return result;
    }

    public final int getGroupIndex(@NonNull GroupBean groupBean) {
        for (int i = 0; i < getGroupCount(); i++) {
            if (groupBean.equals(getGroupItem(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public  int getItemViewType(int position) {
        if (mIsEmpty) {
            return position == 0 && mShowHeaderViewWhenEmpty && mHeaderViewProducer != null ? TYPE_HEADER : TYPE_EMPTY;
        }
        if (position == 0 && mHeaderViewProducer != null) {
            return TYPE_HEADER;
        }
        int[] coord = translateToDoubleIndex(position);
        GroupBean groupBean = getGroupItem(coord[0]);
        if (coord[1] < 0) {
            int groupType = getGroupType(groupBean);
            if ((groupType & TYPE_MASK) == 0) {
                return groupType | TYPE_GROUP;
            } else {
                throw new IllegalStateException(
                        String.format(Locale.getDefault(), "GroupType [%d] conflits with MASK [%d]", groupType, TYPE_MASK));
            }
        } else {
            int childType = getChildType(groupBean, groupBean.getChildAt(coord[1]));
            if ((childType & TYPE_MASK) == 0) {
                return childType | TYPE_CHILD;
            } else {
                throw new IllegalStateException(
                        String.format(Locale.getDefault(), "ChildType [%d] conflits with MASK [%d]", childType, TYPE_MASK));
            }
        }
    }


    @Override
    public  RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType & TYPE_MASK) {
            case TYPE_EMPTY:
                return mEmptyViewProducer.onCreateViewHolder(parent);
            case TYPE_HEADER:
                return mHeaderViewProducer.onCreateViewHolder(parent);
            case TYPE_CHILD:
                return onCreateChildViewHolder(parent, viewType ^ TYPE_CHILD);
            case TYPE_GROUP:
                return onCreateGroupViewHolder(parent, viewType ^ TYPE_GROUP);
            default:
                throw new IllegalStateException(
                        String.format(Locale.getDefault(), "Illegal view type : viewType[%d]", viewType));
        }
    }

    @Override
    public final void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        onBindViewHolder(holder, position, null);
    }

    @Override
    public  void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        switch (holder.getItemViewType() & TYPE_MASK) {
            case TYPE_EMPTY:
                mEmptyViewProducer.onBindViewHolder(holder);
                break;
            case TYPE_HEADER:
                mHeaderViewProducer.onBindViewHolder(holder);
                break;
            case TYPE_CHILD:
                final int[] childCoord = translateToDoubleIndex(position);
                GroupBean groupBean = getGroupItem(childCoord[0]);
                bindChildViewHolder((ChildViewHolder) holder, groupBean, groupBean.getChildAt(childCoord[1]), payloads);
                break;
            case TYPE_GROUP:
                bindGroupViewHolder((GroupViewHolder) holder, getGroupItem(translateToDoubleIndex(position)[0]), payloads);
                break;
            default:
                throw new IllegalStateException(
                        String.format(Locale.getDefault(), "Illegal view type : position [%d] ,itemViewType[%d]", position, holder.getItemViewType()));
        }
    }

    protected void bindGroupViewHolder(final GroupViewHolder holder, final GroupBean groupBean, List<Object> payload) {
        if (payload != null && payload.size() != 0) {
            if (payload.contains(EXPAND_PAYLOAD)) {
                holder.onExpandStatusChanged(BaseExpandableRecyclerViewAdapter.this, isGroupExpanding(groupBean));
                if (payload.size() == 1) {
                    return;
                }
            }
            onBindGroupViewHolder(holder, groupBean, isGroupExpanding(groupBean), payload);
            return;
        }
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnClickListener != null) {
                    return mOnClickListener.onGroupLongClicked(groupBean);
                }
                return false;
            }
        });
        if (!groupBean.isExpandable()) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnClickListener != null) {
                        mOnClickListener.onGroupClicked(groupBean);
                    }
                }
            });
        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean isExpand = mExpandGroupSet.contains(groupBean);
                    if (mOnClickListener == null || !mOnClickListener.onInterceptGroupExpandEvent(groupBean, isExpand)) {
                        final int adapterPosition = holder.getAdapterPosition();
                        holder.onExpandStatusChanged(BaseExpandableRecyclerViewAdapter.this, !isExpand);
                        if (isExpand) {
                            mExpandGroupSet.remove(groupBean);
                            notifyItemRangeRemoved(adapterPosition + 1, groupBean.getChildCount());
                        } else {
                            mExpandGroupSet.add(groupBean);
                            Log.e(TAG, "onClick: " );
                            notifyItemRangeInserted(adapterPosition + 1, groupBean.getChildCount());
                        }
                        mOnClickListener.onGroupClicked(groupBean);
                    }
                }
            });
        }
        onBindGroupViewHolder(holder, groupBean, isGroupExpanding(groupBean));
    }

    protected void bindChildViewHolder(ChildViewHolder holder, final GroupBean groupBean, final ChildBean childBean, List<Object> payload) {
        onBindChildViewHolder(holder, groupBean, childBean, payload);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickListener != null) {
                    mOnClickListener.onChildClicked(groupBean, childBean);
                }
            }
        });
    }

    /**
     * position translation
     * from adapterPosition to group-child coord
     *
     * @param adapterPosition adapterPosition
     * @return int[]{groupIndex,childIndex}
     */
    protected final int[] translateToDoubleIndex(int adapterPosition) {
        if (mHeaderViewProducer != null) {
            adapterPosition--;
        }
        final int[] result = new int[]{-1, -1};
        final int groupCount = getGroupCount();
        int adaptePositionCursor = 0;
        for (int groupCursor = 0; groupCursor < groupCount; groupCursor++) {
            if (adaptePositionCursor == adapterPosition) {
                result[0] = groupCursor;
                result[1] = -1;
                break;
            }
            GroupBean groupBean = getGroupItem(groupCursor);
            if (mExpandGroupSet.contains(groupBean)) {
                final int childCount = groupBean.getChildCount();
                final int offset = adapterPosition - adaptePositionCursor;
                if (childCount >= offset) {
                    result[0] = groupCursor;
                    result[1] = offset - 1;
                    break;
                }
                adaptePositionCursor += childCount;
            }
            adaptePositionCursor++;
        }
        return result;
    }


    public interface BaseGroupBean<ChildBean> {
        /**
         * get num of children
         *
         * @return
         */
        int getChildCount();

        /**
         * get child at childIndex
         *
         * @param childIndex integer between [0,{@link #getChildCount()})
         * @return
         */
        ChildBean getChildAt(int childIndex);

        /**
         * whether this BaseGroupBean is expandable
         *
         * @return
         */
        boolean isExpandable();

        /**
         * get childIndex By childBean
         * @param childBean
         * @return
         */
        int getChildIndex(ChildBean childBean);

        /**
         * add childList to group
         * @param childBeanList
         */
        void addChildBean(List<ChildBean> childBeanList);

    }

    public static abstract class BaseGroupViewHolder extends RecyclerView.ViewHolder {
        public BaseGroupViewHolder(View itemView) {
            super(itemView);
        }

        /**
         * optimize for partial invalidate,
         * when switching fold status.
         * Default implementation is update the whole {android.support.v7.widget.RecyclerView.ViewHolder#itemView}.
         * <p>
         * Warning:If the itemView is invisible , the callback will not be called.
         *
         * @param relatedAdapter
         * @param isExpanding
         */
        protected abstract void onExpandStatusChanged(RecyclerView.Adapter relatedAdapter, boolean isExpanding);
    }

    public void setEnableLoadMoreChild(boolean enableLoadMoreChild) {
        this.enableLoadMoreChild = enableLoadMoreChild;
    }

    public void setEnableLoadMoreGroup(boolean enableLoadMoreGroup) {
        this.enableLoadMoreGroup = enableLoadMoreGroup;
    }

    public interface ExpandableRecyclerViewOnClickListener<GroupBean extends BaseGroupBean, ChildBean> {

        /**
         * called when group item is long clicked
         *
         * @param groupItem
         * @return
         */
        boolean onGroupLongClicked(GroupBean groupItem);

        /**
         * called when an expandable group item is clicked
         *
         * @param groupItem
         * @param isExpand
         * @return whether intercept the click event
         */
        boolean onInterceptGroupExpandEvent(GroupBean groupItem, boolean isExpand);

        /**
         * called when an unexpandable group item is clicked
         *
         * @param groupItem
         */
        void onGroupClicked(GroupBean groupItem);

        /**
         * called when child is clicked
         *
         * @param groupItem
         * @param childItem
         */
        void onChildClicked(GroupBean groupItem, ChildBean childItem);
    }

    public interface LoadMoreListener {
        /**
         * load More children
         * @param  :group position
         */
        void onLoadChildrenMore();

        /**
         * load More group data
         */
        void onLoadGroupMore();
    }

    public void setLoadMoreListener(LoadMoreListener listener) {
        this.mLoadMoreListener = listener;
    }

}

