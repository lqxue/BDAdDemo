package com.baidu.mobads.demo.main.feeds.video;

import java.util.List;

import com.androidquery.AQuery;
import com.baidu.mobad.feeds.ArticleInfo;
import com.baidu.mobad.feeds.BaiduNative;
import com.baidu.mobad.feeds.BaiduNativeManager;
import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobad.feeds.XAdNativeResponse;
import com.baidu.mobads.component.FeedPortraitVideoView;
import com.baidu.mobads.component.IFeedPortraitListener;
import com.baidu.mobads.demo.main.R;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
/*
1. 小视频集成参考类：FeedPortraitVideoActivity
2. 竖版沉浸式视频广告，SDK提供视频播放器组件FeedPortraitVideoView，接入基本同信息流
3. FeedPortraitVideoView提供可自定义性更强，提供更多播放器相关API。
4. 注意：小视频需要您手动发送点击事件。漏发则无法计费。
* */
public class FeedPortraitVideoActivity extends Activity {

    private static final String TAG = "FeedPortraitVideo";
    // 代码位id
    private String mAdPlaceId = "7250989";
    /**
     * 提供的视频组件，整个视频区域不可以点击
     */
    private RelativeLayout mVideoRl;
    private RelativeLayout.LayoutParams videoLp;
    private FeedPortraitVideoView mFeedVideoView;
    /** 定义工信部加的那四个字段的相关控件*/
    private LinearLayout appLayout;
    private TextView publisher;
    private TextView appVersion;
    private TextView appPrivacy;
    private TextView appPermission;
    /** 由于只显示一条广告，所以定义一个值，是否为下载广告 */
    private boolean isDownload=false;
    /**
     * 媒体接入的时候，可以把title,icon,desc,adlogol等等设置为可点击的（按自己产品的设计去开发）
     * 这儿只是示例设置点击之后，如何调用相关的api
     */
    private TextView mTitle;
    // 控制音量
    private ImageView mVolume;
    private TextView mBrandName;
    private ImageView mBaiduLogo;
    private ImageView mAdLogo;
    private SwipeRefreshLayout mRefreshLayout;
    // 提供的视频组件默认是有声音播放，媒体可以调用设置静音与否的api；
    private Boolean mIsMute = false;
    private NativeResponse mNativeAd;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 需要在视频播放过程中保持屏幕常亮，所以需要设置这个
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.feed_portrait_video);
        mVideoRl = findViewById(R.id.feed_portrait_video);
        // 初始化logo
        mBaiduLogo = findViewById(R.id.iv_baidulogo);
        mAdLogo = findViewById(R.id.iv_adlogo);
        // 对工信部四个字段进行实例化。
        appLayout = findViewById(R.id.vertical_video_new_layout);
        appVersion = findViewById(R.id.vertical_video_version);
        appPrivacy = findViewById(R.id.vertical_video_privacy);
        appPermission = findViewById(R.id.vertical_video_permission);
        publisher = findViewById(R.id.vertical_video_publisher);
        mFeedVideoView = new FeedPortraitVideoView(this);
        // 【可选配置】是否显示视频播放的进度条
        mFeedVideoView.setShowProgress(true);
        mFeedVideoView.setProgressBackgroundColor(Color.BLACK);
        mFeedVideoView.setProgressBarColor(Color.WHITE);
        mFeedVideoView.setProgressHeightInDp(1);
        videoLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout
                .LayoutParams.MATCH_PARENT);
        mVideoRl.addView(mFeedVideoView, videoLp);
        mFeedVideoView.setFeedPortraitListener(new IFeedPortraitListener() {
            @Override
            public void playCompletion() {
                // 视频播放完成
                Log.i(TAG, "playCompletion==");
            }

            @Override
            public void playError() {
                // 播放错误
                Log.i(TAG, "playError==");
            }

            @Override
            public void playRenderingStart() {
                // 视频开始播放第一帧
                Log.i(TAG, "playRenderingStart==");
                //显示字段
                if(isDownload) {
                    appLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void playPause() {
                Log.i(TAG, "playPause==");
            }

            @Override
            public void playResume() {
                Log.i(TAG, "playResume==");
            }
        });
        mTitle = findViewById(R.id.iv_title);
        mBrandName = findViewById(R.id.iv_brandname);
        mVolume = findViewById(R.id.test_volume);
        mVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsMute = !mIsMute;
                // 调用设置静音的api
                mFeedVideoView.setVideoMute(mIsMute);
                mVolume.setImageResource(mIsMute ? R.mipmap.volume_close : R.mipmap.volume_open);
            }
        });

        /**
         * 说明：媒体划走view两种处理方式：
         * （1）划走时候调用mFeedVideoView.stop()，释放播放器资源，切换到前台之后可以重新设置数据进行播放播放，可以这样调用api：
         if (mFeedVideoView != null) {
         // (XAdNativeResponse) mNativeAd这个广告数据可以是已经播放过的，也可以是已经新请求OK的广告数据
         mFeedVideoView.setAdData((XAdNativeResponse) mNativeAd);
         mFeedVideoView.play();
         }
         */
        // 媒体可以复用同一个组件view，这个组件view接收广告数据（(XAdNativeResponse) mNativeAd）进行重新播放，
        Button stopAndRestart = findViewById(R.id.stop_Restart);
        stopAndRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 释放播放器资源
                mFeedVideoView.stop();
                if (mFeedVideoView != null) {
                    // 传入当前想播放的广告数据,如果是视频就开始准备播放器
                    mFeedVideoView.setAdData((XAdNativeResponse) mNativeAd);
                    // 提供的视频组件默认是有声音播放，调用设置静音的api
                    mFeedVideoView.setVideoMute(mIsMute);
                    // 调用play方法，开始播放
                    mFeedVideoView.play();
                }
            }
        });
        // 测试play()接口使用
        Button btnPlay = findViewById(R.id.video_play);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 测试使用
                if (mFeedVideoView != null) {
                    // 调用play方法，开始播放
                    mFeedVideoView.play();
                }
            }
        });

        mRefreshLayout = findViewById(R.id.refresh_container);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchAd();
            }
        });
        /**
         * 请求广告数据，
         * 备注：如果需要的话，可以一次请求返回多条广告数据（mssp后台可配置）
         */
        fetchAd();

        ImageView backBtn = findViewById(R.id.btn_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 暂停播放的时候需要调用pause,也可由播放组件自动管理
//        if (mFeedVideoView != null) {
//            mFeedVideoView.pause();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 回到前台时需要调用resume,也可由播放组件自动管理
//        if (mFeedVideoView != null) {
//            mFeedVideoView.resume();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 调用stop,释放播放器资源
        if (mFeedVideoView != null) {
            mFeedVideoView.stop();
        }
    }

    private void fetchAd() {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
        int winW = dm.widthPixels;
        int winH = dm.heightPixels;
        /**
         * 配置请求广告的参数
         * @param winW 宽度
         * @param winH 高度
         * @param policy  用户点击下载类广告时，是否弹出提示框让用户选择下载与否
         */
        RequestParameters requestParameters = new RequestParameters.Builder()
                .setWidth(winW)
                .setHeight(winH)
                .downloadAppConfirmPolicy(
                        RequestParameters.DOWNLOAD_APP_CONFIRM_ONLY_MOBILE) // 用户点击下载类广告时，是否弹出提示框让用户选择下载与否
                // 用户维度：用户性别，取值：0-unknown，1-male，2-female
                .addExtra(ArticleInfo.USER_SEX, "1")
                // 用户维度：收藏的小说ID，最多五个ID，且不同ID用'/分隔'
                .addExtra(ArticleInfo.FAVORITE_BOOK, "这是小说的名称1/这是小说的名称2/这是小说的名称3")
                // 内容维度：小说、文章的名称
                .addExtra(ArticleInfo.PAGE_TITLE, "测试书名")
                // 内容维度：小说、文章的ID
                .addExtra(ArticleInfo.PAGE_ID, "10930484090")
                // 内容维度：小说分类，一级分类和二级分类用'/'分隔
                .addExtra(ArticleInfo.CONTENT_CATEGORY, "一级分类/二级分类")
                // 内容维度：小说、文章的标签，最多10个，且不同标签用'/分隔'
                .addExtra(ArticleInfo.CONTENT_LABEL, "标签1/标签2/标签3")
                .build();

        /**
         * @param context 上下文
         * @param mAdPlaceId 广告位id,说明：媒体需要写自己申请好的广告位id
         * @param BaiduNativeNetworkListener 接收广告请求成功和失败的回调
         */
        BaiduNativeManager baiduNativeManager = new BaiduNativeManager(this, mAdPlaceId);
        baiduNativeManager.loadPortraitVideoAd(requestParameters, new BaiduNativeManager.PortraitVideoAdListener() {
            @Override
            public void onAdClick() {
                // 视频发生点击
                Log.i(TAG, "onAdClick：");
                Toast.makeText(FeedPortraitVideoActivity.this, "视频发生点击", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNativeLoad(final List<NativeResponse> nativeResponses) {
                Log.i(TAG, "onADLoaded：");
                mRefreshLayout.setRefreshing(false);
                if (nativeResponses != null && nativeResponses.size() > 0) {
                    // 这里每次都取第一条广告来做展示,模拟多条广告;实际开发过程中需要开发者自己处理
                    mNativeAd = nativeResponses.get(0);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mFeedVideoView != null) {
                                // 传入当前想播放的广告数据,如果是视频就开始准备播放器
                                mFeedVideoView.setAdData((XAdNativeResponse) mNativeAd);
                                // 调用play方法开始播放视屏，若已经播放过，则恢复播放
                                mFeedVideoView.play();
                                // 重置静音按钮的状态，保持与播放器的状态一致
                                mIsMute = false;
                                mVolume.setImageResource(mIsMute
                                        ? R.mipmap.volume_close : R.mipmap.volume_open);
                                // 判断一下是不是下载类广告，如果是就给四个字段赋值
                                if (!TextUtils.isEmpty(mNativeAd.getAppVersion()) &&
                                        !TextUtils.isEmpty(mNativeAd.getAppPermissionLink()) &&
                                        !TextUtils.isEmpty(mNativeAd.getAppPrivacyLink()) &&
                                        !TextUtils.isEmpty(mNativeAd.getPublisher())) {
                                    isDownload = true;
                                    appVersion.setText("版本:"+mNativeAd.getAppVersion());
                                    publisher.setText(mNativeAd.getPublisher());
                                    // 给隐私添加点击事件
                                    appPrivacy.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            mNativeAd.privacyClick();
                                        }
                                    });
                                    // 给权限添加点击事件
                                    appPermission.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            mNativeAd.permissionClick();
                                        }
                                    });
                                    // 给隐私、权限注册事件回调
                                    mNativeAd.setAdPrivacyListener(new NativeResponse.AdPrivacyListener() {
                                        @Override
                                        public void onADPrivacyClick() {
                                            Log.i(TAG, "onADPrivacyClick: " + mNativeAd.getTitle());
                                        }

                                        @Override
                                        public void onADPermissionShow() {
                                            Log.i(TAG, "onADPermissionShow: " + mNativeAd.getTitle());
                                        }

                                        @Override
                                        public void onADPermissionClose() {
                                            Log.i(TAG, "onADPermissionClose: " + mNativeAd.getTitle());
                                        }
                                    });
                                }
                            }
                            mBrandName.setText(mNativeAd.getBrandName());
                            // 渲染logo，注册点击
                            AQuery aq = new AQuery(FeedPortraitVideoActivity.this);
                            aq.id(R.id.iv_baidulogo).image(mNativeAd.getBaiduLogoUrl());
                            setUnionLogoClick(mBaiduLogo, mNativeAd);
                            aq.id(R.id.iv_adlogo).image(mNativeAd.getAdLogoUrl());
                            setUnionLogoClick(mAdLogo, mNativeAd);
                            // 渲染标题，注册点击
                            mTitle.setText(mNativeAd.getTitle());
                            mTitle.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // 触发点击操作时，需要调用该api
                                    mNativeAd.handleClick(mTitle);
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onNativeFail(NativeErrorCode errorCode) {
                Log.i(TAG, "onLoadFail:" + errorCode);
                mRefreshLayout.setRefreshing(false);
                Toast.makeText(FeedPortraitVideoActivity.this, "没有收到视频广告，请检查", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVideoDownloadSuccess() {
                // 视频缓存成功
                Log.i(TAG, "onVideoDownloadSuccess：");
                Toast.makeText(FeedPortraitVideoActivity.this, "视频缓存成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVideoDownloadFailed() {
                // 视频缓存失败
                Log.i(TAG, "onVideoDownloadFailed：");
                Toast.makeText(FeedPortraitVideoActivity.this, "视频缓存失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLpClosed() {
                // 落地页关闭回调
                Log.i(TAG, "onLpClosed");
                Toast.makeText(FeedPortraitVideoActivity.this, "lp页面关闭", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 点击联盟logo打开官网
    private void setUnionLogoClick(ImageView logo, final NativeResponse nrAd) {
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nrAd.unionLogoClick();
            }
        });
    }
}

