package com.baidu.mobads.demo.main;

import com.baidu.mobads.AdSettings;
import com.baidu.mobads.demo.main.banner.BannerListActivity;
import com.baidu.mobads.demo.main.content.NativeContentAdActivity;
import com.baidu.mobads.demo.main.cpu.CpuVideoActivity;
import com.baidu.mobads.demo.main.cpu.NativeCPUAdActivity;
import com.baidu.mobads.demo.main.feeds.FeedAdActivity;
import com.baidu.mobads.demo.main.feeds.FeedAdRecycleActivity;
import com.baidu.mobads.demo.main.feeds.video.FeedNativeVideoActivity;
import com.baidu.mobads.demo.main.feeds.video.FeedPortraitVideoActivity;
import com.baidu.mobads.demo.main.jssdk.HybridInventoryActivity;
import com.baidu.mobads.demo.main.mediaExamples.MediaExamplesActivity;
import com.baidu.mobads.demo.main.patchvideo.VideoPatchAdActivity;
import com.baidu.mobads.demo.main.permission.BasePermissionActivity;
import com.baidu.mobads.demo.main.fullvideo.FullScreenVideoActivity;
import com.baidu.mobads.demo.main.rewardvideo.RewardVideoActivity;
import com.baidu.mobads.demo.main.search.InsiteActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Demo主界面，广告产品的展示列表
 * SDK 接入文档：https://union.baidu.com/miniappblog/2020/06/16/AndroidSDK/
 *
 * 集成提示：
 * 1. 请参考Demo中AndroidManifest配置相关配置，注意appsid（应用id）
 * 2. 默认使用https，如需更改，调用AdSettings.setSupportHttps(false);
 * 3. 设置SDK可以使用的权限，包含：设备信息、定位、存储、APP LIST。 注意：建议授权SDK读取设备信息，SDK会在应用获得系统权限后自行获取IMEI等设备信息，有助于提升ECPM。
 * 4. 可以参考Demo集成信通院SDK，非强制
 * 5. 接入广告的时候需要配置apid（广告位id），广告的appsid、appid、包名。三者有绑定关系，需要与百青藤后台配置的一致。且注意id前后不能出现空格。
 * 6. 如果您是更新SDK，请检查相关产品API是否变更。
 * 7. 广告业务提示：SDK中单次请求的广告不支持多次展现，多次展现只计费一次。信息流广告需要您手动发送广告的曝光和点击事件给SDK。
 */

public class BaiduSDKDemo extends BasePermissionActivity {

    public static final String TAG = "BaiduSDKDemo";

    private LinearLayout mAdTypeList;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initView();

        // 设置SDK可以使用的权限，包含：设备信息、定位、存储、APP LIST
        // 注意：建议授权SDK读取设备信息，SDK会在应用获得系统权限后自行获取IMEI等设备信息
        //      授权SDK获取设备信息会有助于提升ECPM
        initMobadsPermissions();

        // SDK默认使用https，如需改为http请求，调用如下方法
        AdSettings.setSupportHttps(false);
    }

    @Override
    protected void onDestroy() {        
        Log.i(TAG, "onDestroy");
        
        // 您可以在这里执行您的业务逻辑，比如发统计给服务器，让服务器统计退出概率， 或者APP运行时长.
        
        /**
         * 百度广告联盟建议您在退出APP前做两件事情
         * 
         * 1. 通过BaiduXAdSDKContext.exit()来告知AdSDK，以便AdSDK能够释放资源.
         * 
         * 2. 使用下面两行代码种的任意一行来冷酷无情的强制退出当前进程，以确保App本身资源得到释放。
         *      android.os.Process.killProcess(android.os.Process.myPid());
         *      System.exit(0);
         */
        // baidu-xadsdk will release all resource
        com.baidu.mobads.production.BaiduXAdSDKContext.exit();
        // kill current process
        // android.os.Process.killProcess(android.os.Process.myPid());
        // System.exit(0);
        
        super.onDestroy();
    }

    private void initView() {
        mAdTypeList = findViewById(R.id.item_container);
        LayoutInflater inflater = getLayoutInflater();

        bindButton(inflater, R.mipmap.ic_banner_20_3, "横幅", BannerListActivity.class, true);

        bindButton(inflater, R.mipmap.ic_interstitial, "开屏", RSplashManagerActivity.class, true);

        bindButton(inflater, R.mipmap.ic_interstitial, "插屏", InterstitialAdActivity.class, true);

        bindButton(inflater, R.mipmap.ic_feed, "信息流", FeedAdActivity.class, true);

        bindText(inflater, "");  // 添加空的TextView作为间隔

        bindButton(inflater, R.mipmap.ic_feed_video, "信息流视频", FeedNativeVideoActivity.class, true);

        bindButton(inflater, R.mipmap.ic_feed_video, "信息流视频(Recycle)", FeedAdRecycleActivity.class, true);

        bindButton(inflater, R.mipmap.ic_portrait_video, "小视频", FeedPortraitVideoActivity.class, true);

        bindButton(inflater, R.mipmap.ic_reward_video, "激励视频", RewardVideoActivity.class, true);

        bindButton(inflater, R.mipmap.ic_reward_video, "全屏视频", FullScreenVideoActivity.class, true);

        bindButton(inflater, R.mipmap.ic_video_patch, "视频贴片", VideoPatchAdActivity.class, false);

        bindText(inflater, "");  // 添加空的TextView作为间隔

        bindButton(inflater, R.mipmap.ic_jssdk, "JSSDK", HybridInventoryActivity.class, true);

        bindButton(inflater, R.mipmap.ic_cpu, "内容联盟原生渲染", NativeCPUAdActivity.class, true);

        bindButton(inflater, R.mipmap.ic_cpu, "内容联盟模板渲染", CpuAdActivity.class, false);

        bindButton(inflater, R.mipmap.ic_feed, "内容位", NativeContentAdActivity.class, false);

        bindButton(inflater, R.mipmap.ic_portrait_video, "内容联盟小视频", CpuVideoActivity.class, true);



        bindText(inflater, "");
        bindButton(inflater, R.mipmap.ic_feed, "站内搜索", InsiteActivity.class, false);
        bindButton(inflater, R.mipmap.ic_cpu, "媒体接入示例", MediaExamplesActivity.class, false);
        // sdk version
        bindText(inflater, "");  // 添加空的TextView作为间隔
        bindText(inflater, " v 8.55");
        bindText(inflater, "");  // 添加空的TextView作为间隔
    }

    private void bindButton(LayoutInflater inflater, int iconId, String name, final Class clz, boolean showDivider) {
        View btn = inflater.inflate(R.layout.demo_ad_list_item, null);
        TextView textView = btn.findViewById(R.id.item_name);
        textView.setText(name);
        ImageView icon = btn.findViewById(R.id.left_icon);
        icon.setImageResource(iconId);
        View divider = btn.findViewById(R.id.divider);
        divider.setVisibility(showDivider ? View.VISIBLE : View.GONE);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clz == RSplashManagerActivity.class) {
                    Intent intent = new Intent(BaiduSDKDemo.this, clz);
                    intent.putExtra("need_app_logo", true);
                    intent.putExtra("exit_after_lp", false);
                    startActivity(intent);
                } else {
                    startActivity(new Intent(BaiduSDKDemo.this, clz));
                }
            }
        });
        mAdTypeList.addView(btn);
    }

    private void bindText(LayoutInflater inflater, String text) {
        View item = inflater.inflate(R.layout.demo_ad_list_empty_view, null);
        TextView textView = item.findViewById(R.id.text);
        ImageView sdkIcon = item.findViewById(R.id.sdk_icon);
        if (TextUtils.isEmpty(text)) {
            textView.setVisibility(View.GONE);
            sdkIcon.setVisibility(View.GONE);
        } else {
            textView.setText(text);
        }
        mAdTypeList.addView(item);
    }

}