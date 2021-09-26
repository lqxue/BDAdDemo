package com.baidu.mobads.demo.main.mediaExamples.novel.utils;

import android.widget.Toast;

import com.baidu.mobads.demo.main.MobadsApplication;


public class ToastUtils {

    public static void show(String msg){
        Toast.makeText(MobadsApplication.getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
