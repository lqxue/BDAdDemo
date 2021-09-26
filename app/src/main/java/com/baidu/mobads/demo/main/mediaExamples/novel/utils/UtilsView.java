package com.baidu.mobads.demo.main.mediaExamples.novel.utils;

import android.view.View;
import android.view.ViewGroup;


public class UtilsView {

    public static void removeParent(View view) {
        try {
            if (view == null) {
                return;
            }
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent == null) {
                return;
            }
            parent.removeView(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
