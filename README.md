# ExpandableRecycleView
只有两级的ExpandableRecycleView，基于https://github.com/hgDendi/ExpandableRecyclerView

补充实现了以下几点：

1、childItem和groupItem的loadMore
2、childItem和groupItem的局部刷新
```
//更新child
 adapter.notifyGroupItem(groupItem);
 //更新group
 adapter.notifyChildItem(groupItem,childItem);
```

