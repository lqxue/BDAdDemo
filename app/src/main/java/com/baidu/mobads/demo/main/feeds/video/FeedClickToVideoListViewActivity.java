package com.baidu.mobads.demo.main.feeds.video;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.baidu.mobads.BaiduNativeAdPlacement;
import com.baidu.mobads.demo.main.R;
import com.baidu.mobads.demo.main.feeds.FeedH5.FeedH5ContentInfo;

public class FeedClickToVideoListViewActivity extends Activity {

    private ListView mListView;
    private FeedClickToVideoListViewAdapter mListViewAdapter;
    private List<Object> mList;
    private int sessionid;
    // 重要：请填上您的广告位ID，代码位错误会导致无法请求到广告
    private String adPlaceId = "4319858"; // 点击广告后会跳转到详情页播放视频

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_h5_listview);

        mList = new ArrayList<Object>();
        sessionid = Math.abs(new Random().nextInt(Integer.MAX_VALUE)) + 1;
        
        queryContetForListView();
    }

    private void queryContetForListView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i <= 15; i++) {
                    if (i % 3 == 0) {
                        BaiduNativeAdPlacement placement = new BaiduNativeAdPlacement();
//                        placement.setSessionId(sessionid); // 设置页面id，进入页面重新生成，从1开始正整数，可选
//                        placement.setPositionId(i + 1); // 设置广告在页面的楼层，从1开始的正整数，可选
                        placement.setApId(adPlaceId);
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
        FeedClickToVideoListViewActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListView = (ListView) FeedClickToVideoListViewActivity.this.findViewById(R.id.native_list_view);
                mListViewAdapter = new FeedClickToVideoListViewAdapter(FeedClickToVideoListViewActivity.this, mList);
                mListView.setAdapter(mListViewAdapter);
            }
        });
    }

}
