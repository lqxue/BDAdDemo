package com.baidu.mobads.demo.main.mediaExamples.novel.ui.adapter;

import android.view.View;
import android.view.ViewGroup;



public interface IViewHolder<T> {
    View createItemView(ViewGroup parent);
    void initView();
    void onBind(T data, int pos);
    void onClick();
}
