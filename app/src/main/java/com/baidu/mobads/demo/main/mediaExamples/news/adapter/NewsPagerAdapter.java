package com.baidu.mobads.demo.main.mediaExamples.news.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.mobads.demo.main.mediaExamples.news.NewsListFragment;

import java.util.List;

public class NewsPagerAdapter extends FragmentPagerAdapter {

    private List<NewsListFragment> mFragments;
    private int currentItemPosition;
    private Context mContext;

    public NewsPagerAdapter(Context context, FragmentManager fm, List<NewsListFragment> fragments) {
        super(fm);
        mContext = context;
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int i) {
        return mFragments.get(i);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.setPrimaryItem(container, position, object);
        currentItemPosition = position;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragments.get(position).getTitle();
    }

    public int getCurrentItemPosition() {
        return currentItemPosition;
    }

    public TextView loadTabView(int position, ViewGroup rootView) {
        TextView tab = new TextView(mContext);
        tab.setGravity(Gravity.CENTER);
        tab.setText(getPageTitle(position));
        tab.setTextColor(Color.GRAY);
        tab.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tab.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.NORMAL));
        return tab;
    }
}
