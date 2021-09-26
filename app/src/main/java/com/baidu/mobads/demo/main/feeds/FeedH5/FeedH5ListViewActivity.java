package com.baidu.mobads.demo.main.feeds.FeedH5;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ListView;

import com.baidu.mobads.BaiduNativeAdPlacement;
import com.baidu.mobads.demo.main.R;

public class FeedH5ListViewActivity extends Activity {

    public static int INTERVAL_BETWEEN_AD = 1; // 可以手动设置广告的间隔
    private ListView mListView;
    private FeedH5ListViewAdapter mListViewAdapter;
    private List<Object> mList;
    private int sessionid;
    // 重要：请填上您的广告位ID，代码位错误会导致无法请求到广告
    private String YOUR_AD_PLACE_ID = "3143845"; // 点击广告后不会播放视频
    // private String adPlaceId = "2015351"; // 点击广告后会跳转到详情页播放视频
    // private String adPlaceId = "4319858"; // 点击广告后会跳转到详情页播放视频

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_h5_listview);

        Intent intent = getIntent();
        if (intent != null) {
            String adPlaceId = intent.getStringExtra("adPlaceId");
            if (!TextUtils.isEmpty(adPlaceId)) {
                YOUR_AD_PLACE_ID = adPlaceId;
            }
        }

        mList = new ArrayList<Object>();
        sessionid = Math.abs(new Random().nextInt(Integer.MAX_VALUE)) + 1;
        
        queryContetForListView();
    }

    private void queryContetForListView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i <= 9; i++) {
                    if (i % INTERVAL_BETWEEN_AD == 0) {
                        BaiduNativeAdPlacement placement = new BaiduNativeAdPlacement();
//                        placement.setSessionId(sessionid); // 设置页面id，进入页面重新生成，从1开始正整数，可选
//                        placement.setPositionId(i + 1); // 设置广告在页面的楼层，从1开始的正整数，可选
                        placement.setApId(YOUR_AD_PLACE_ID);
                        mList.add(placement);
                    } else {
                        mList.add(new FeedH5ContentInfo(i, "内容标题-" + i, "内容摘要-" + i));
                    }
                }
                onAllDataReady();
            }
        }).start();
    }

    private void onAllDataReady() {
        // 可以在这里对广告进行重排序
        FeedH5ListViewActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListView = (ListView) FeedH5ListViewActivity.this.findViewById(R.id.native_list_view);
                mListViewAdapter = new FeedH5ListViewAdapter(FeedH5ListViewActivity.this, mList);
                mListView.setAdapter(mListViewAdapter);
            }
        });
    }

}
