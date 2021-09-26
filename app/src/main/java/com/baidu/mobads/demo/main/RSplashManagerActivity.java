package com.baidu.mobads.demo.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobads.SplashAd;
import com.baidu.mobads.SplashLpCloseListener;

import java.util.HashMap;

/**
 * 开屏完整示例
 * 1. 快捷接入请参考RSplashActivity，完整示例请参考RSplashManagerActivity
 * 2. 开屏广告需嵌入应用启动页Activity中。
 * 3. 开屏广告支持自定义跳过按钮，需在百青藤平台配置广告位设置。
 * 4. 设置开屏广告请求参数，非必选。
 * 5. 设置开屏listener
 * 6. 实例化开屏广告对象，canClick建议为true，否则影响填充
 * 7. 请求开屏广告，可直接loadAndShow（实时请求并展示），也可拆分load和show，分开延时展示。
 * 根据工信部的规定，不再默认申请权限，而是主动弹框由用户授权使用。
 * 如果是Android6.0以下的机器, 或者targetSDKVersion < 23，默认在安装时获得了所有权限，可以直接调用SDK
 */
public class RSplashManagerActivity extends Activity {

    public static final String TAG = RSplashManagerActivity.class.getSimpleName();
    private SplashAd splashAd;
    private RelativeLayout adsParent;
    private TextView stateTextView;
    private boolean needAppLogo = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_manager);
        adsParent = this.findViewById(R.id.adsRl);
        stateTextView = this.findViewById(R.id.stateTextView);

        initView();
    }

    private void initView() {

        // 1. 设置开屏广告请求参数，图片宽高单位dp 非必选
        final RequestParameters parameters = new RequestParameters.Builder()
                .setHeight(640)
                .setWidth(360)
                .build();
        final String adPlaceId = "2058622"; // 重要：请填上您的广告位ID，代码位错误会导致无法请求到广告
//        AdSettings.setSupportHttps(false); // 自由选择使用http或者https请求，默认https
//        SplashAd.setBitmapDisplayMode(BitmapDisplayMode.DISPLAY_MODE_FIT_XY);// 根据需求自由设置开屏图片拉伸方式
        // 设置视频广告最大缓存占用空间(15MB~100MB),默认30MB,单位MB
        // SplashAd.setMaxVideoCacheCapacityMb(30);
        Intent intent = getIntent();

        if (intent != null) {
            needAppLogo = intent.getBooleanExtra("need_app_logo", true);
        }

        // 2. 设置开屏listener
        final SplashLpCloseListener listener = new SplashLpCloseListener() {
            @Override
            public void onLpClosed() {
                Log.i(TAG, "onLpClosed");
                Toast.makeText(RSplashManagerActivity.this,"LpClosed",Toast.LENGTH_SHORT).show();
                // 落地页关闭后关闭广告，并跳转到应用的主页
                destorySplash();
            }

            @Override
            public void onAdDismissed() {
                Log.i(TAG , "onAdDismissed:");
                destorySplash();
            }

            @Override
            public void onADLoaded() {
                Log.i(TAG, "onADLoaded");
                stateTextView.setText("请求成功");
                HashMap ext = splashAd.getExtData();
                Log.i("ext_data", ext.toString());
            }

            @Override
            public void onAdFailed(String arg0) {
                Log.i(TAG, arg0);
                destorySplash();
                stateTextView.setText("请求失败");
            }

            @Override
            public void onAdPresent() {
                if (needAppLogo) {
                    findViewById(R.id.appLogo).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.appLogo).setVisibility(View.GONE);
                }
                adsParent.setVisibility(View.VISIBLE);
                stateTextView.setText("展示成功");
                Log.i(TAG, "onAdPresent");
            }

            @Override
            public void onAdClick() {
                Log.i(TAG, "onAdClick");
            }
        };

        // 3. 初始化开屏实例，请求开屏广告
        Button btn1 = this.findViewById(R.id.loadAndShow);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 实例化开屏广告的构造函数
                adsParent.setVisibility(View.VISIBLE);
                splashAd = new SplashAd(RSplashManagerActivity.this, adsParent, listener, adPlaceId, true,
                        parameters);

            }
        });

        Button btn2 = this.findViewById(R.id.load);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 实例化开屏广告的构造函数
                 * fetchAd：是否自动请求广告, 设置为true则自动loadAndshow，无需再主动load和show
                 * 设置为false则仅初始化开屏广告对象，需要手动调用load请求广告，并调用show展示广告
                 **/
                splashAd = new SplashAd(RSplashManagerActivity.this, adsParent, listener, adPlaceId, true,
                        parameters, 4200, false, true);

                splashAd.load();
            }
        });

        Button btn3 = this.findViewById(R.id.load_bg);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 实例化开屏广告的构造函数
                 * fetchAd：是否自动请求广告, 设置为true则自动loadAndshow，无需再主动load和show
                 * 设置为false则仅初始化开屏广告对象，需要手动调用load请求广告，并调用show展示广告
                 * SplashAd的初始化和load操作，可以置于子线程中进行
                 **/
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        splashAd = new SplashAd(RSplashManagerActivity.this, adsParent, listener, adPlaceId, true,
                                parameters, 4200, false, true);
                        splashAd.load();
                    }
                }).start();
            }
        });

        Button btn4 = this.findViewById(R.id.show);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 单条广告多次show只计入一次，请注意
                if (adsParent != null) {
                    if (splashAd != null) {
                        adsParent.setVisibility(View.VISIBLE);
                        splashAd.show();
                    } else {
                        stateTextView.setText("请检查开屏对象是否存在异常");
                        Log.i(TAG, "请检查开屏对象是否存在异常");
                        return;
                    }
                }
            }
        });
    }

    private void destorySplash() {
        adsParent.setVisibility(View.INVISIBLE);
        findViewById(R.id.appLogo).setVisibility(View.INVISIBLE);
        if (splashAd != null) {
            splashAd.destroy();
            splashAd = null;
        }

        adsParent.removeAllViews();
        stateTextView.setText("等待请求");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (splashAd != null) {
            splashAd.destroy();
            splashAd = null;
        }
        this.finish();
    }
}
