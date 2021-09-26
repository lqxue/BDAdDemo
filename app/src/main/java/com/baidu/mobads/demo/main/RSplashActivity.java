package com.baidu.mobads.demo.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobads.AdSettings;
import com.baidu.mobads.SplashAd;
import com.baidu.mobads.SplashLpCloseListener;

import java.util.HashMap;

/**
 * 实时开屏，广告实时请求并且立即展现。实时开屏接入请看该类
 */
public class RSplashActivity extends Activity {

    private TextView mSplashHolder;
    private SplashAd splashAd;
    private boolean canJumpImmediately = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        // 调用获取版本号的方法,下方的值为版本号
        Double SDKVersion= AdSettings.getSDKVersion();
        // 打印出版本号的值
        Log.e("RSplashActivity","广告SDK的版本号为："+SDKVersion);
        mSplashHolder = findViewById(R.id.splash_holder);

        fetchSplashAD();
    }

    private void fetchSplashAD() {
        // 默认请求https广告，若需要请求http广告，请设置AdSettings.setSupportHttps为false
//         AdSettings.setSupportHttps(false);
        RelativeLayout adsParent = (RelativeLayout) this.findViewById(R.id.adsRl);

        SplashLpCloseListener listener = new SplashLpCloseListener() {
            @Override
            public void onLpClosed() {
                Log.i("RSplashActivity", "lp页面关闭");
                Toast.makeText(RSplashActivity.this,"lp页面关闭",Toast.LENGTH_SHORT).show();
                jump();
            }

            @Override
            public void onAdDismissed() {
                Log.i("RSplashActivity", "onAdDismissed");
                jump(); // 跳转至您的应用主界面
            }

            @Override
            public void onADLoaded() {
                Log.i("RSplashActivity", "onADLoaded");
                HashMap ext = splashAd.getExtData();
                Log.i("ext_data", ext.toString());
            }

            @Override
            public void onAdFailed(String arg0) {
                Log.i("RSplashActivity", arg0);
                jump();
            }

            @Override
            public void onAdPresent() {
                Log.i("RSplashActivity", "onAdPresent");
                mSplashHolder.setVisibility(View.GONE);
            }

            @Override
            public void onAdClick() {
                Log.i("RSplashActivity", "onAdClick");
                // 设置开屏可接受点击时，该回调可用
            }
        };

        String adPlaceId = "2058622"; // 重要：请填上您的广告位ID，代码位错误会导致无法请求到广告
        splashAd = new SplashAd(this, adsParent, listener, adPlaceId, true,
                null, 4200, true, true);

        // 等比缩小放大，裁剪边缘部分
//        SplashAd.setBitmapDisplayMode(BitmapDisplayMode.DISPLAY_MODE_CENTER_CROP);
    }

    @Override
    protected void onPause() {
        super.onPause();
        canJumpImmediately = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        canJumpImmediately = true;
    }

    private void jump() {

        if (canJumpImmediately) {
            this.startActivity(new Intent(RSplashActivity.this, BaiduSDKDemo.class));
            this.finish();
        }
    }
}
