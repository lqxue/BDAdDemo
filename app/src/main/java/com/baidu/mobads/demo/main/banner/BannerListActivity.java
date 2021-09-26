package com.baidu.mobads.demo.main.banner;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.baidu.mobads.AdSettings;
import com.baidu.mobads.AdView;
import com.baidu.mobads.AdViewListener;
import com.baidu.mobads.AppActivity;
import com.baidu.mobads.demo.main.R;

import org.json.JSONObject;

/*
1. 横幅集成参考类：BannerListActivity
2. 推荐您将Banner的宽高比固定为20：3以获得最佳的广告展示效果
3. Banner 给开发者的回调（AdViewListener）全部执行在主线程中（异步回调）
4. 横幅会自动刷新，刷新间隔30秒
5. 尽量复用广告实例，不要实例化过多的广告实例，当广告实例不再使用时务必调用destory方法进行资源释放
6. 百青藤平台配置广告位配置尺寸和前端渲染尺寸需要严格保持一致。
* */
public class BannerListActivity extends Activity {

    public static final String TAG = "BannerListActivity";

    private static final String AD_PLACE_ID_20_3 = "2015351";
    private static final String AD_PLACE_ID_7_3 = "3536891";
    private static final String AD_PLACE_ID_3_2 = "3536888";
    private static final String AD_PLACE_ID_2_1 = "3536896";

    private View mNoDataView;
    private RelativeLayout mRlAdContainer;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.banner_main);

        mNoDataView = getLayoutInflater().inflate(R.layout.no_ad_view, null);

        mRlAdContainer = findViewById(R.id.ad_container);

        // 默认请求https广告，若需要请求http广告，请设置AdSettings.setSupportHttps为false
//         AdSettings.setSupportHttps(false);

        // 代码设置AppSid，此函数必须在AdView实例化前调用
        // AdView.setAppSid("debug");

        // 设置'广告着陆页'动作栏的颜色主题
        // 目前开放了七大主题：黑色、蓝色、咖啡色、绿色、藏青色、红色、白色(默认) 主题
        AppActivity
                .setActionBarColorTheme(AppActivity.ActionBarColorTheme.ACTION_BAR_BLUE_THEME);
        // 另外，也可设置动作栏中单个元素的颜色, 颜色参数为四段制，0xFF(透明度, 一般填FF)DE(红)DA(绿)DB(蓝)
        // AppActivity.getActionBarColorTheme().set[Background|Title|Progress|Close]Color(0xFFDEDADB);

        // 重要：请填上您的广告位ID，代码位错误会导致无法请求到广告
        // 创建广告View，并添加至接界面布局中
        bindBannerView(mRlAdContainer, AD_PLACE_ID_20_3, 20, 3);

    }

    public void onClick(View view) {
        // 计算横幅广告的长宽比，以及设置广告位id
        int scaledWidth = 20;
        int scaledHeight = 3;
        String adPlaceId = AD_PLACE_ID_20_3;
        switch (view.getId()) {
            case R.id.btn_show1:
                break;
            case R.id.btn_show2:
                adPlaceId = AD_PLACE_ID_7_3;
                scaledWidth = 7;
                scaledHeight = 3;
                break;
            case R.id.btn_show3:
                adPlaceId = AD_PLACE_ID_3_2;
                scaledWidth = 3;
                scaledHeight = 2;
                break;
            case R.id.btn_show4:
                adPlaceId = AD_PLACE_ID_2_1;
                scaledWidth = 2;
                scaledHeight = 1;
                break;
            default:
                // nop
        }
        mRlAdContainer.removeAllViews();
        // 重要：请填上您的广告位ID，代码位错误会导致无法请求到广告
        // 创建广告View，并添加至接界面布局中
        bindBannerView(mRlAdContainer, adPlaceId, scaledWidth, scaledHeight);
    }

    /**
     * 创建横幅广告的View，并添加至接界面布局中
     * 注意：只有将AdView添加到布局中后，才会有广告返回
      */
    private void bindBannerView(final RelativeLayout yourOriginalLayout,
                                String adPlaceId, int scaleWidth, int scaleHeight) {
        AdView adView = new AdView(this, adPlaceId);
        // 设置监听器
        adView.setListener(new AdViewListener() {
            @Override
            public void onAdSwitch() {
                Log.w(TAG, "onAdSwitch");
            }

            @Override
            public void onAdShow(JSONObject info) {
                // 广告已经渲染出来
                Log.w(TAG, "onAdShow " + info.toString());
            }

            @Override
            public void onAdReady(AdView adView) {
                // 资源已经缓存完毕，还没有渲染出来
                Log.w(TAG, "onAdReady " + adView);
            }

            @Override
            public void onAdFailed(String reason) {
                Log.w("", "onAdFailed " + reason);
                RelativeLayout.LayoutParams rllp = new RelativeLayout
                        .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rllp.addRule(RelativeLayout.CENTER_IN_PARENT);
                yourOriginalLayout.addView(mNoDataView, rllp);
            }

            @Override
            public void onAdClick(JSONObject info) {
                // Log.w(TAG, "onAdClick " + info.toString());
            }

            @Override
            public void onAdClose(JSONObject arg0) {
                Log.w(TAG, "onAdClose");
            }
        });

        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
        int winW = dm.widthPixels;
        int winH = dm.heightPixels;
        int width = Math.min(winW, winH);
        int height = width * scaleHeight / scaleWidth;
        // 将adView添加到父控件中(注：该父控件不一定为您的根控件，只要该控件能通过addView能添加广告视图即可)
        RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(width, height);
        rllp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        yourOriginalLayout.addView(adView, rllp);
    }

}