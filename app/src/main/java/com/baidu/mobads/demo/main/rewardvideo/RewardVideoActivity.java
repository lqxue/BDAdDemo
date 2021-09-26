package com.baidu.mobads.demo.main.rewardvideo;

import com.baidu.mobads.AdSettings;
import com.baidu.mobads.demo.main.R;
import com.baidu.mobads.demo.main.permission.BasePermissionActivity;
import com.baidu.mobads.rewardvideo.RewardVideoAd;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

/*
1. 激励视频集成参考类：RewardVideoActivity
2. 激励视频广告分在线播放和本地播放两种形式。
    1. 如果采用本地播放则需提前预加载load处理，预加载耗时因广告而异，1秒~5秒之间。
    2. 如果采用在线播放，无需做任何操作，直接show即可。有播放卡顿风险。
3. 单次请求的广告不支持多次展现。下次展现前需要重新预加载视频，不做预加载直接show则在线请求并播放。可以在点击关闭操作后重新预加载新广告。
4. 广告存在有效期，需要一定时间（2小时、非固定值）内展现。如果广告超时未展现，调用show的时候会重新请求广告并在线播放。可以通过isReady判断是否过期。
5. 监听展现失败onAdFailed做异常流程处理。
* */
public class RewardVideoActivity extends BasePermissionActivity implements RewardVideoAd.RewardVideoAdListener {

    public static final String TAG = "RewardVideoActivity";
    // 线上广告位id
    private static final String AD_PLACE_ID = "5925490";
    // 激励视屏产品可以选择是否使用SurfaceView进行渲染视频
    public RewardVideoAd mRewardVideoAd;
    private EditText mAdPlaceIdView;
    // 测试环境的广告位id
    //    private String mAdPlaceId = "2411590";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reward_video);
        AdSettings.setSupportHttps(false);
        initView();
        mRewardVideoAd = new RewardVideoAd(RewardVideoActivity.this,
                AD_PLACE_ID, RewardVideoActivity.this, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onVideoDownloadSuccess() {
        // 视频缓存成功
        // 说明：如果想一定走本地播放，那么收到该回调之后，可以调用show
        Log.i(TAG, "onVideoDownloadSuccess,isReady=" + mRewardVideoAd.isReady());
//        mRewardVideoAd.show();
    }

    @Override
    public void onVideoDownloadFailed() {
        // 视频缓存失败，如果想走本地播放，可以在这儿重新load下一条广告，最好限制load次数（4-5次即可）。
        Log.i(TAG, "onVideoDownloadFailed");
        mRewardVideoAd.load();
    }

    @Override
    public void playCompletion() {
        // 播放完成回调，媒体可以在这儿给用户奖励
        Log.i(TAG, "playCompletion");
    }

    @Override
    public void onAdShow() {
        // 视频开始播放时候的回调
        Log.i(TAG, "onAdShow");

    }

    @Override
    public void onAdClick() {
        // 广告被点击的回调
        Log.i(TAG, "onAdClick");
    }

    @Override
    public void onAdClose(float playScale) {
        // 用户关闭了广告
        // 说明：关闭按钮在mssp上可以动态配置，媒体通过mssp配置，可以选择广告一开始就展示关闭按钮，还是播放结束展示关闭按钮
        // 建议：收到该回调之后，可以重新load下一条广告,最好限制load次数（4-5次即可）
        // playScale[0.0-1.0],1.0表示播放完成，媒体可以按照自己的设计给予奖励
        Log.i(TAG, "onAdClose" + playScale);
        mRewardVideoAd.load();
    }

    @Override
    public void onAdFailed(String arg0) {
        // 广告失败回调 原因：广告内容填充为空；网络原因请求广告超时
        // 建议：收到该回调之后，可以重新load下一条广告，最好限制load次数（4-5次即可）
        Log.i(TAG, "onAdFailed");
        mRewardVideoAd.load();

    }

    private void initView() {
        Button btn1 = this.findViewById(R.id.btn_change_orientation);
        btn1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentOrientation = getResources().getConfiguration().orientation;
                if (currentOrientation == ORIENTATION_PORTRAIT) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                } else if (currentOrientation == ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
            }
        });

        Button btn2 = this.findViewById(R.id.btn_load);
        btn2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mRewardVideoAd.isReady()){
                    mRewardVideoAd.show();
                }else {
                    mRewardVideoAd.load();
                }

            }

        });

        Button btn3 = this.findViewById(R.id.btn_show);
        btn3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRewardVideoAd != null) {
                    mRewardVideoAd.show();
                } else {
                    Toast.makeText(RewardVideoActivity.this,
                            "请成功加载后在进行广告展示！", Toast.LENGTH_LONG).show();
                }
            }

        });

        Button btn4 = this.findViewById(R.id.is_ready);
        btn4.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isReady = mRewardVideoAd != null && mRewardVideoAd.isReady();
                Toast.makeText(RewardVideoActivity.this, "可用广告:" + isReady, Toast.LENGTH_SHORT).show();
            }
        });

        mAdPlaceIdView = findViewById(R.id.edit_apid);
        mAdPlaceIdView.setText(AD_PLACE_ID);
        mAdPlaceIdView.clearFocus();
    }
}