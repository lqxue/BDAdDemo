package com.baidu.mobads.demo.main.feeds.video;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.baidu.mobads.demo.main.R;

public class FeedVideoDemoActivity extends Activity {

    public static final String TAG = FeedVideoDemoActivity.class.getSimpleName();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_video_main);

        Button btn1 = (Button) this.findViewById(R.id.feed_video_btn1);
        btn1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击广告跳转到新页面播放视频
                startActivity(new Intent(FeedVideoDemoActivity.this, FeedClickToVideoListViewActivity.class));
            }

        });
        Button btn2 = (Button) this.findViewById(R.id.feed_video_btn2);
        btn2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // sdk提供视频url等，app自己渲染视频
                startActivity(new Intent(FeedVideoDemoActivity.this, FeedNativeVideoActivity.class));
            }

        });
    }

}
