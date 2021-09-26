package com.baidu.mobads.demo.main;

import android.app.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.baidu.mobads.AdSize;
import com.baidu.mobads.InterstitialAd;
import com.baidu.mobads.InterstitialAdListener;
/*
1. 插屏集成参考类：InterstitialAdActivity
2. 创建插屏展示的Parent视图
3. 创建广告实例，插屏广告支持图片、视频物料，根据需求创建不同的广告对象
4. 视频前贴插屏有倒计时，结束后自动关闭
5. 视频暂停插屏无倒计时，可以自定大小，width和height以及传入的parent必须大小合理，不可太小
6. 需要注册listener，调用load后监听onAdReady
7. 广告在回调onAdReady后，才可调用show接口展示插屏广告
8. 注意show成功后需要重新load新的广告，同一条广告不能重复曝光*/
public class InterstitialAdActivity extends Activity implements InterstitialAdListener {
    // 重要：请填上您的广告位ID，代码位错误会导致无法请求到广告
    private static final String YOUR_AD_PLACE_ID = "2403633";
    private static final String YOUR_VIDEO_AD_PLACE_ID = "2058626";

    private InterstitialAd mInterAd;            // 插屏广告实例，支持单例模式
    private String mAdType = "interAd";         // 插屏广告的类型，Demo使用，避免重复创建广告实例
    private EditText mAdPlaceIdView;            // 广告位id
    private boolean isAdForVideo = false;       // 视频插屏广告
    private RelativeLayout mVideoAdLayout;      // 展示视频插屏的布局
    private RelativeLayout.LayoutParams reLayoutParams;
    private boolean isQianTiePianAd = true;     // 前贴片广告 or 暂停页广告

    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interstitial);

        // 默认请求https广告，若需要请求http广告，请设置AdSettings.setSupportHttps为false
        // AdSettings.setSupportHttps(false);

        // 设置appsid
        mAdPlaceIdView = findViewById(R.id.edit_apid);
        mAdPlaceIdView.setText(YOUR_AD_PLACE_ID);

        initAdControlView();

        // 视频插屏广告：初始化展示布局
        final RelativeLayout parentLayout = findViewById(R.id.parent_interstitial);
        mVideoAdLayout = new RelativeLayout(this);
        reLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        reLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        parentLayout.addView(mVideoAdLayout, reLayoutParams);

        // 创建插屏广告实例
        createInterstitialAd();

        mButton = this.findViewById(R.id.btn_interstitial);
        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mInterAd.isAdReady()) {
                    showAd();
                } else {
                    createInterstitialAd();
                    loadAd();
                }
            }
        });

        // 提前加载插屏广告
        loadAd();
    }

    /**
     * 创建广告实例，支持：插屏、前贴片、暂停页
     */
    private void createInterstitialAd() {
        String adPlaceId = mAdPlaceIdView.getText().toString();
        if (isAdForVideo) {
            if (isQianTiePianAd) {
                if (!"qianTiePian".equals(mAdType)) {
                    // 创建前贴片广告
                    mInterAd = new InterstitialAd(this, AdSize.InterstitialForVideoBeforePlay, adPlaceId);
                    mInterAd.setListener(this);
                    mAdType = "qianTiePian";
                }
            } else {
                // 创建暂停页广告
                if (!"zanTingYe".equals(mAdType)) {
                    mInterAd = new InterstitialAd(this, AdSize.InterstitialForVideoPausePlay, adPlaceId);
                    mInterAd.setListener(this);
                    mAdType = "zanTingYe";
                }
            }
        } else {
            // 创建插屏广告
            if (mInterAd == null || !"interAd".equals(mAdType)) {
                mInterAd = new InterstitialAd(this, adPlaceId);
                mInterAd.setListener(this);
                mAdType = "interAd";
            }
        }
    }

    /**
     * 加载广告
     */
    private void loadAd() {
        if (isAdForVideo) {
            int width = getValueById(R.id.edit_width);
            int height = getValueById(R.id.edit_height);
            if (width <= 0 || height <= 0) {
                width = 600;
                height = 500;
            }
            reLayoutParams.width = width;
            reLayoutParams.height = height;
            mInterAd.loadAdForVideoApp(width, height);
        } else {
            mInterAd.loadAd();
        }
    }

    /**
     * 展现广告
     */
    private void showAd() {
        if (isAdForVideo) {
            mInterAd.showAdInParentForVideoApp(this, mVideoAdLayout);
        } else {
            mInterAd.showAd(this);
        }
    }

    @Override
    public void onAdClick(InterstitialAd arg0) {
        Log.i("InterstitialAd", "onAdClick");
    }

    @Override
    public void onAdDismissed() {
        Log.i("InterstitialAd", "onAdDismissed");
        mButton.setText("加载插屏广告");
        if (!isAdForVideo) {
            loadAd();
        }
    }

    @Override
    public void onAdFailed(String arg0) {
        Log.i("InterstitialAd", "onAdFailed");
    }

    @Override
    public void onAdPresent() {
        Log.i("InterstitialAd", "onAdPresent");
    }

    @Override
    public void onAdReady() {
        Log.i("InterstitialAd", "onAdReady");
        mButton.setText("展现插屏广告");
    }

    private void initAdControlView() {
        CheckBox mCheckVideo = findViewById(R.id.check_video_ad);
        RadioGroup videoType = findViewById(R.id.video_ad_type);
        videoType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                isQianTiePianAd = (checkedId == R.id.qian_tie_pian);
            }
        });

        final LinearLayout adSizeEditView = findViewById(R.id.ad_xy_size);

        final RadioButton btn1 = findViewById(R.id.qian_tie_pian);
        final RadioButton btn2 = findViewById(R.id.zan_ting_ye);
        mCheckVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isAdForVideo = isChecked;
                mAdPlaceIdView.setText(isAdForVideo ? YOUR_VIDEO_AD_PLACE_ID : YOUR_AD_PLACE_ID);
                btn1.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
                btn2.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
                adSizeEditView.setVisibility(isAdForVideo ? View.VISIBLE : View.GONE);
            }
        });
    }

    private int getValueById(int viewId) {
        EditText editText = findViewById(viewId);
        String value = editText.getText().toString();
        if (value.length() > 0) {
            try {
                return Integer.valueOf(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
}
