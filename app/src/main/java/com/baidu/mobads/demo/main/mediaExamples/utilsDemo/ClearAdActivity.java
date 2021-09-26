package com.baidu.mobads.demo.main.mediaExamples.utilsDemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobads.demo.main.R;
import com.baidu.mobads.demo.main.cpu.NativeCPUView;
import com.baidu.mobads.nativecpu.CPUAdRequest;
import com.baidu.mobads.nativecpu.IBasicCPUData;
import com.baidu.mobads.nativecpu.NativeCPUManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 这个是媒体接入示例中工具类式的清理结束的
 * 半屏广告接入示例。
 * 是内容联盟的接入示例、
 * 需要注意的点为：1.发送点击日志2.发送展现日志。关于点击展现日志的发送在ClearAdapter类中。
 */
public class ClearAdActivity extends Activity {
    /** 打印日志所需要的变量 */
    private static final String TAG = ClearAdActivity.class.getSimpleName();
    /** APPSID */
    private final String YOUR_APP_ID = "c0da1ec4"; // 双引号中填写自己的APPSID
    // 内容联盟有频道ID，不同的ID代表不同的频道
    // 娱乐频道-------1001
    // 体育频道-------1002
    // 财经频道-------1006
    // 汽车频道-------1007
    // 时尚频道-------1009
    // 文化频道-------1011
    // 科技频道-------1013
    private int mChannelId = 1001; // 默认娱乐频道
    /** 用于显示广告列表的RecyclerView */
    private RecyclerView mRecyclerView;
    /** 广告RecyclerView的适配器 */
    private ClearAdapter mClearAdapter;
    /** 储存广告数据的list */
    private List<IBasicCPUData> nrAdList = new ArrayList<IBasicCPUData>();
    /** 用于请求广告 */
    private NativeCPUManager mCpuManager;
    /** 显示广告的大view */
    RelativeLayout mAdView;
    /** 广告的退出按钮 */
    ImageView mExsit;
    /**
     * 实现滑动效果所需要的数据
     * */
    /** 广告部分的高 */
    private int mAdViewHeight = 0;
    /** 广告部分的宽 */
    private int mAdViewWdith = 0;
    /** 开始时的y坐标 */
    private int startY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_utils_demo_clear);
        // 初始化各个控件
        init();
        // 根据屏幕改变尺寸
        setViewSize();
        // 初始化用户内容联盟请求的类
        mCpuManager = new NativeCPUManager(ClearAdActivity.this, YOUR_APP_ID, new NativeCPUManager.CPUAdListener() {
            // 进行了各种接口的回调
            // 请求广告成功
            @Override
            public void onAdLoaded(List<IBasicCPUData> list) {
                if (list != null && list.size() > 0) {
                    // 如果请求成功了的话，把请求成功的数据集合取出来
                    nrAdList = list;
                    // 弹出请求成功的文字
                    Toast.makeText(ClearAdActivity.this, "请求成功", Toast.LENGTH_SHORT).show();
                    // 给适配器赋值,使recyclerview展示出来
                    mClearAdapter = new ClearAdapter(ClearAdActivity.this, list);
                    mRecyclerView.setAdapter(mClearAdapter);
                    LinearLayoutManager manager = new LinearLayoutManager(ClearAdActivity.this);
                    mRecyclerView.setLayoutManager(manager);
                }
            }

            @Override
            public void onAdError(String msg, int errorCode) {
                Log.w(TAG, "onAdError reason:" + msg);
            }

            @Override
            public void onContentClick() {
                Log.i(TAG, "onContentClick");
            }

            @Override
            public void onAdClick() {
                Log.i(TAG, "onAdClick");
            }

            @Override
            public void onAdImpression() {

            }

            @Override
            public void onContentImpression() {

            }

            @Override
            public void onVideoDownloadSuccess() {
                // 预留接口
            }

            @Override
            public void onVideoDownloadFailed() {
                // 预留接口
            }

            @Override
            public void onAdStatusChanged(final String appPackageName) {
                if (!TextUtils.isEmpty(appPackageName) && nrAdList != null) {
                    int size = nrAdList.size();
                    for (int pos = 0; pos < size; pos++) {
                        IBasicCPUData nrAd = nrAdList.get(pos);
                        if (nrAd != null && nrAd.isDownloadApp()) {
                            if (appPackageName.equals(nrAd.getAppPackageName())) {
                                // 如有需要，在此处可以更新下载类广告的下载进度
                            }
                        }
                    }
                }
            }
        });
        // 进行请求
        loadCpuAd();
        // 添加退出按钮的点击事件
        initExitButton();

    }

    /** 初始化各个组件 */
    private void init() {
        /** 显示广告的大view*/
        mAdView = findViewById(R.id.demo_utils_clear_ad_view);
        /** 广告的退出按钮*/
        mExsit = findViewById(R.id.demo_utils_clear_exist);
        /** 显示广告的recyclerview控件*/
        mRecyclerView = findViewById(R.id.demo_utils_clear_recycler_view);
    }

    // 请求广告的方法
    private void loadCpuAd() {
        // 构建请求参数

        CPUAdRequest.Builder builder = new CPUAdRequest.Builder();
        builder.setDownloadAppConfirmPolicy(RequestParameters.DOWNLOAD_APP_CONFIRM_ONLY_MOBILE);
        builder.setCustomUserId("123456789abcdefg"); // 当无法获得设备IMEI,OAID,ANDROIDID信息时，通过此字段获取内容 + 广告
        mCpuManager.setRequestParameter(builder.build());
        mCpuManager.setRequestTimeoutMillis(10 * 1000); // 如果不设置，则默认5s请求超时

        //调用请求接口，请求广告
        // 参数，第一个为页数，这里我们默认写第一页，根据自己的需求进行调整
        // 参数，第二个为频道的id，不同频道有不同的id，也是根据自己需要进行调整
        // 第三个参数，为是否显示广告，设为true
        mCpuManager.loadAd(1, mChannelId, true);
    }

    /** 添加退出按钮的点击事件 */
    private void initExitButton() {
        if (mExsit != null) {
            mExsit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
    }

    /***
     *改变布局的宽高，是为了更好的适配
     */
    private void setViewSize() {
        // 取得屏幕的宽高
        Display display = getWindowManager().getDefaultDisplay();

        Point point = new Point();
        display.getSize(point);
        // 屏幕的宽
        int windowWidth = point.x;
        // 广告的宽为屏幕的宽
        mAdViewWdith = windowWidth;
        /** 显示广告的大view*/
        ViewGroup.LayoutParams mAdViewParams = mAdView.getLayoutParams();
        // 改变宽
        mAdViewParams.width = windowWidth;
        // 改变高
        mAdViewParams.height = 0;
        // 赋值
        mAdView.setLayoutParams(mAdViewParams);
        // 确定广告的最大高度
        setScreenSize();
    }

    // 改变点击事件，实现滑动效果
    @Override
    public boolean dispatchTouchEvent(final MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 记录开始的高
                startY = (int) ev.getY();
                return super.dispatchTouchEvent(ev); //传递事件 比如能够用来子view的点击事件等
            case MotionEvent.ACTION_UP:
                int y = (int) ev.getY();
                int dy = startY - y;
                // 只有当dy比0的时候才是像上滑动
                // 只有没有展示的时候才进行滑动的展示
                // 只有广告加载出来才能向上滑动展现广告
                if (dy > 0 && mAdView.getHeight() == 0 && nrAdList != null && nrAdList.size() > 0) {
                    // 将上滑产看更多的字样与图片隐藏起来
                    TextView topWord = findViewById(R.id.demo_utils_clear_word_top);
                    ImageView topArrow = findViewById(R.id.demo_utils_clear_arrow_top);
                    topWord.setVisibility(View.GONE);
                    topArrow.setVisibility(View.GONE);
                    // 这边赋一个值是因为动画没法从0变为一个数，先给定一个1才可以进行动画。
                    ViewGroup.LayoutParams mAdViewParams = mAdView.getLayoutParams();
                    // 改变高
                    mAdViewParams.height = 1;
                    // 赋值
                    mAdView.setLayoutParams(mAdViewParams);
                    // 运行动画
                    Animation animation = new ViewSizeChangeAnimation(mAdView, mAdViewHeight, mAdViewWdith);
                    animation.setDuration(500);
                    animation.setInterpolator(new LinearInterpolator());
                    mAdView.startAnimation(animation);

                }
                return super.dispatchTouchEvent(ev);
        }

        return super.dispatchTouchEvent(ev);
    }

    public void setScreenSize() {
        mAdView.post(new Runnable() {
            @Override
            public void run() {
                View view = getWindow().findViewById(Window.ID_ANDROID_CONTENT);
                mAdViewHeight = view.getHeight() * 67 / 88;
            }
        });
    }
}
