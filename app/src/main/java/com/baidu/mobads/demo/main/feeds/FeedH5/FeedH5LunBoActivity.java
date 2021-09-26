package com.baidu.mobads.demo.main.feeds.FeedH5;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.mobad.feeds.BaiduNative;
import com.baidu.mobad.feeds.BaiduNative.BaiduNativeNetworkListener;
import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobad.feeds.NativeResponse.MaterialType;
import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobads.demo.main.R;

public class FeedH5LunBoActivity extends Activity {

    private static final String TAG = "HTMLFeedLunBoActivity";
    private static String YOUR_AD_PLACE_ID = "2403624"; // 双引号中填写自己的广告位ID
    List<NativeResponse> nrAdList = new ArrayList<NativeResponse>();
    private NativeResponse mNrAd;
    LinearLayout adsParent;
    private float density;
    
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_h5_lunbo);
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        density = metric.density;

        adsParent = (LinearLayout) findViewById(R.id.adsRl);

        Intent intent = getIntent();
        if (intent != null) {
            String adPlaceId = intent.getStringExtra("adPlaceId");
            if (!TextUtils.isEmpty(adPlaceId)) {
                YOUR_AD_PLACE_ID = adPlaceId;
            }
        }

        fetchAd(this);
    }

    public void fetchAd(Activity activity) {
        /**
         * Step 1. 创建BaiduNative对象，参数分别为：
         * 上下文context，广告位ID, BaiduNativeNetworkListener监听（监听广告请求的成功与失败）
         * 注意：请将YOUR_AD_PALCE_ID替换为自己的广告位ID
         */
        BaiduNative baidu = new BaiduNative(activity, YOUR_AD_PLACE_ID, 
                new BaiduNativeNetworkListener() {

                    @Override
                    public void onNativeFail(NativeErrorCode arg0) {
                        Toast.makeText(FeedH5LunBoActivity.this, "没有收到轮播模板广告，请检查", Toast.LENGTH_LONG).show();
                        adsParent.addView(getLayoutInflater().inflate(R.layout.no_ad_view, null));
                    }

                    @Override
                    public void onNativeLoad(List<NativeResponse> arg0) {
                        if (arg0 != null && arg0.size() > 0) {
                            nrAdList = arg0;
      
                            mNrAd = nrAdList.get(0);

                            if (mNrAd.getMaterialType() == MaterialType.HTML) {
                                Toast.makeText(FeedH5LunBoActivity.this, "收到轮播模板广告.", Toast.LENGTH_LONG).show();

                                myHandler.sendEmptyMessage(SHOW_HTML);
                            } else {
                                Toast.makeText(FeedH5LunBoActivity.this,
                                        "收到广告,但不是模板广告,请检查", Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    }

                });

        /**
         * Step 2. 创建requestParameters对象，并将其传给baidu.makeRequest来请求广告
         */
        RequestParameters requestParameters = new RequestParameters.Builder()
                .setWidth((int) (360 * density))
                .setHeight((int) (250 * density))
                .downloadAppConfirmPolicy(
                        RequestParameters.DOWNLOAD_APP_CONFIRM_ONLY_MOBILE) // 用户点击下载类广告时，是否弹出提示框让用户选择下载与否
                .build();

        baidu.makeRequest(requestParameters);
    }

    private static int SHOW_HTML = 0;
    Handler myHandler = new Handler() {  
        public void handleMessage(Message msg) {
            if (msg.what == SHOW_HTML) {
                WebView webView = mNrAd.getWebView();
                adsParent.addView(webView);
            }
            super.handleMessage(msg);
        }   
    };

}

