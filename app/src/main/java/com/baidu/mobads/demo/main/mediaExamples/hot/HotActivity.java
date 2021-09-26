package com.baidu.mobads.demo.main.mediaExamples.hot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobads.demo.main.R;
import com.baidu.mobads.demo.main.mediaExamples.novel.utils.SharedPreUtils;
import com.baidu.mobads.demo.main.tools.RefreshAndLoadMoreView;
import com.baidu.mobads.nativecpu.CPUAdRequest;
import com.baidu.mobads.nativecpu.IBasicCPUData;
import com.baidu.mobads.nativecpu.NativeCPUManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *  Demo接入 内容联盟热榜频道 示例
 *  注意点：热榜频道需要设置listScene = 19 {@link HotActivity#loadAd(int pageIndex)}
 */
public class HotActivity extends Activity implements NativeCPUManager.CPUAdListener, HotAdapter.OnClickCallBack {

    private static final String TAG = "HotActivity";
    private final String YOUR_APP_ID = "c0da1ec4"; // 双引号中填写自己的APPSID
    private int mChannelId = 1090; // 热榜频道
    private NativeCPUManager mCpuManager;
    private int mPageIndex = 1;
    private HotAdapter mHotAdapter;

    private RefreshAndLoadMoreView refreshAndLoadMoreView;
    private ListView listView;
    private EditText editText;
    private TextView textView;
    private ImageView text_deleteIcon;

    private TimeHandler mTimeHandler;
    private volatile Thread thread;
    private volatile boolean isRunning = true;
    private List<IBasicCPUData> nrAdList = new ArrayList<IBasicCPUData>();
    private boolean isDark;

    private LinearLayout mllHotContainer;
    private int textSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cpu_hotlist);

        isDark = getIntent().getBooleanExtra("isdark", false);
        textSize = getIntent().getIntExtra("textSize", 18);

        editText = findViewById(R.id.hot_et);
        textView = findViewById(R.id.hot_search);
        text_deleteIcon = findViewById(R.id.hot_text_delete);
        text_deleteIcon.setVisibility(View.GONE);

        text_deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
                text_deleteIcon.setVisibility(View.GONE);
            }
        });

        ShapeDrawable shapeDrawable = new ShapeDrawable();
        float[] outerRadii = new float[]{40, 40, 40, 40, 0, 0, 0, 0};
        RoundRectShape roundRectShape = new RoundRectShape(outerRadii, null, null);
        shapeDrawable.setShape(roundRectShape);
        shapeDrawable.getPaint().setColor(isDark ? Color.BLACK : Color.WHITE);


        mllHotContainer = findViewById(R.id.ll_hotContainer);
        mllHotContainer.setBackground(shapeDrawable);
        refreshAndLoadMoreView = findViewById(R.id.hot_lv);
        refreshAndLoadMoreView.setCanRefresh(false);
        refreshAndLoadMoreView.setLoadAndRefreshListener(new RefreshAndLoadMoreView.LoadAndRefreshListener() {
            @Override
            public void onRefresh() {

            }

            @Override
            public void onLoadMore() {
                loadAd(++mPageIndex);
            }
        });

        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                    String keyword = editText.getText().toString().trim();
                    if (TextUtils.isEmpty(keyword)) {
                        keyword = editText.getHint().toString().trim();
                    }

                    Intent intent = new Intent(HotActivity.this, RecommendChannelActivity.class);
                    intent.putExtra("hotWord", keyword);
                    intent.putExtra("isdark",isDark);
                    intent.putExtra("textSize", textSize);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    text_deleteIcon.setVisibility(View.VISIBLE);
                } else {
                    text_deleteIcon.setVisibility(View.GONE);
                }
            }
        });

        listView = refreshAndLoadMoreView.getListView();


        mTimeHandler = new TimeHandler(this);


        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HotActivity.this, RecommendChannelActivity.class);

                String keyword = editText.getText().toString().trim();
                if (TextUtils.isEmpty(keyword)) {
                    keyword = editText.getHint().toString().trim();
                }

                intent.putExtra("hotWord", keyword);
                intent.putExtra("isdark", isDark);
                intent.putExtra("textSize", textSize);
                startActivity(intent);
            }
        });

        /**
         * Step 1. NativeCPUManager，参数分别为： 上下文context（必须为Activity），appsid, 认证token, CPUAdListener（监听广告请求的成功与失败）
         * 注意：请将YOUR_AD_PLACE_ID，YOUR_AD_TOKEN替换为自己的ID和TOKEN
         * 建议提前初始化
         */
        mCpuManager = new NativeCPUManager(HotActivity.this, YOUR_APP_ID, this);

        mHotAdapter = new HotAdapter(this, isDark, textSize);
        mHotAdapter.setOnClickCallBack(this);
        listView.setAdapter(mHotAdapter);
        loadAd(mPageIndex);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
        editText.setText("");
        text_deleteIcon.setVisibility(View.GONE);

        mllHotContainer.requestFocus();

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    if (mHotAdapter != null) {
                        String randomHotKey = mHotAdapter.getRandomHotKey();
                        Message message = Message.obtain();
                        message.obj = randomHotKey;
                        mTimeHandler.sendMessage(message);
                    }
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        thread = null;
                        System.gc();
                    }
                }
            }
        });

        thread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;
        thread.interrupt();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        thread = null;
    }

    public void loadAd(int pageIndex) {
        /**
         * Step2：构建请求参数
         */
        CPUAdRequest.Builder builder = new CPUAdRequest.Builder();
        builder.setDownloadAppConfirmPolicy(RequestParameters.DOWNLOAD_APP_CONFIRM_ONLY_MOBILE);

        /**
         *  注意构建参数时，setCustomUserId 为必选项，
         *  传入的outerId是为了更好的保证能够获取到广告和内容
         *  outerId的格式要求： 包含数字与字母的16位 任意字符串
         */

        /**
         *  推荐的outerId获取方式：
         */
        SharedPreUtils sharedPreUtils = SharedPreUtils.getInstance();
        String outerId = sharedPreUtils.getString(SharedPreUtils.OUTER_ID);
        if (TextUtils.isEmpty(outerId)) {
            outerId = UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0,16);
            sharedPreUtils.putString(SharedPreUtils.OUTER_ID, outerId);
        }

        // 当无法获得设备IMEI,OAID,ANDROIDID信息时，通过此字段获取内容 + 广告
        builder.setCustomUserId(outerId);

        // 热榜频道需要设置listScene = 19
        builder.setListScene(19);

        mCpuManager.setRequestParameter(builder.build());
        mCpuManager.setRequestTimeoutMillis(10 * 1000); // 如果不设置，则默认10s请求超时

        /**
         * Step3：调用请求接口，请求广告
         */

        mCpuManager.loadAd(pageIndex, mChannelId, true);

    }

    @Override
    public void onClick(IBasicCPUData iBasicCPUData, View view) {
        String hotWord = iBasicCPUData.getHotWord();
        Intent intent = new Intent(HotActivity.this, RecommendChannelActivity.class);
        intent.putExtra("hotWord", hotWord);
        intent.putExtra("isdark", isDark);
        intent.putExtra("textSize", textSize);
        startActivity(intent);

    }


    @Override
    public void onAdLoaded(List<IBasicCPUData> list) {
        if (refreshAndLoadMoreView.isRefreshing()) {
            nrAdList.clear();
        }
        if (list != null && list.size() > 0) {
            nrAdList.addAll(list);
            mHotAdapter.addHotData(nrAdList);
        }
        editText.setHint(mHotAdapter.getRandomHotKey());
        refreshAndLoadMoreView.onLoadFinish();
    }

    @Override
    public void onAdError(String msg, int errorCode) {

    }

    @Override
    public void onContentClick() {

    }

    @Override
    public void onAdClick() {

    }

    @Override
    public void onAdImpression() {

    }

    @Override
    public void onContentImpression() {

    }

    @Override
    public void onVideoDownloadSuccess() {

    }

    @Override
    public void onVideoDownloadFailed() {

    }

    @Override
    public void onAdStatusChanged(String appPackageName) {

    }

    static class TimeHandler extends Handler {
        private WeakReference<HotActivity> weakReference;
        public TimeHandler(HotActivity activity) {
            weakReference = new WeakReference<HotActivity>(activity);

        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            HotActivity hotActivity = weakReference.get();
            String value = (String) msg.obj;
            if (hotActivity != null) {
                hotActivity.editText.setHint(value);
            }
        }
    }

}