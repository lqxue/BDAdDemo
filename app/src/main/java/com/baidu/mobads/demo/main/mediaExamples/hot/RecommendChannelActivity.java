package com.baidu.mobads.demo.main.mediaExamples.hot;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobads.demo.main.R;
import com.baidu.mobads.demo.main.cpu.NativeCPUView;
import com.baidu.mobads.demo.main.mediaExamples.novel.utils.SharedPreUtils;
import com.baidu.mobads.demo.main.tools.RefreshAndLoadMoreView;
import com.baidu.mobads.nativecpu.CPUAdRequest;
import com.baidu.mobads.nativecpu.CpuLpFontSize;
import com.baidu.mobads.nativecpu.IBasicCPUData;
import com.baidu.mobads.nativecpu.NativeCPUManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *  点击热榜后再次请求相关热榜内容时，
 *  需要将频道id 设置为 1022 （推荐频道）及listScene = 15 ,keywords为热榜标题
 *  {@link RecommendChannelActivity#loadAd(int pageIndex)}
 */
public class RecommendChannelActivity extends Activity implements NativeCPUManager.CPUAdListener {
    private static final String TAG = "RecommendChannel";
    private final String YOUR_APP_ID = "c0da1ec4"; // 双引号中填写自己的APPSID
    private int mChannelId = 1022; // 推荐频道
    private int mPageIndex = 1;
    private List<IBasicCPUData> nrAdList = new ArrayList<IBasicCPUData>();
    private NativeCPUManager mCpuManager;
    private RefreshAndLoadMoreView mRefreshLoadView;
    private ListView listView;
    private MyAdapter adapter;
    private String hotWord;
    private EditText contentEt;
    private TextView searchTv;
    private boolean isResearch ;
    private boolean isdark;
    private LinearLayout llbg;
    private LinearLayout mLinearRecommendContainer;
    private int textSize;
    private ImageView text_deleteIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommand_channel);

        hotWord = getIntent().getStringExtra("hotWord");
        isdark = getIntent().getBooleanExtra("isdark", false);
        textSize = getIntent().getIntExtra("textSize", 18);


        llbg = findViewById(R.id.llbg);
        contentEt = findViewById(R.id.recom_ed);
        searchTv = findViewById(R.id.recom_search);
        text_deleteIcon = findViewById(R.id.hot_text_delete);
        text_deleteIcon.setVisibility(View.GONE);
        text_deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentEt.setText("");
                text_deleteIcon.setVisibility(View.GONE);
            }
        });
        mLinearRecommendContainer = findViewById(R.id.linear_recommend_container);

        contentEt.setHint(hotWord);
        mLinearRecommendContainer.setVisibility(View.GONE);
        llbg.setBackgroundColor(isdark ? Color.BLACK : Color.WHITE);

        searchTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hotWord = contentEt.getText().toString().trim();
                if (TextUtils.isEmpty(hotWord)) {
                    hotWord = contentEt.getHint().toString().trim();
                }
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                isResearch = true;
                mRefreshLoadView.setRefreshing(true);
                loadAd(mPageIndex++);
            }
        });

        contentEt.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                hotWord = contentEt.getText().toString().trim();
                if (TextUtils.isEmpty(hotWord)) {
                    hotWord = contentEt.getHint().toString().trim();
                }
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

                    contentEt.setText(hotWord);
                    isResearch = true;
                    mRefreshLoadView.setRefreshing(true);
                    loadAd(mPageIndex++);
                    return true;
                }
                return false;
            }
        });

        contentEt.addTextChangedListener(new TextWatcher() {
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



        mRefreshLoadView = findViewById(R.id.native_list_view);
        mRefreshLoadView.setLoadAndRefreshListener(new RefreshAndLoadMoreView.LoadAndRefreshListener() {
            @Override
            public void onRefresh() {
                loadAd(++mPageIndex);
            }

            @Override
            public void onLoadMore() {
                loadAd(++mPageIndex);
            }
        });
        listView = mRefreshLoadView.getListView();

        listView.setCacheColorHint(Color.WHITE);

        adapter = new MyAdapter(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                IBasicCPUData nrAd = nrAdList.get(position);
                nrAd.handleClick(view);
            }
        });
        listView.setAdapter(adapter);

        /**
         * Step 1. NativeCPUManager，参数分别为： 上下文context（必须为Activity），appsid, 认证token, CPUAdListener（监听广告请求的成功与失败）
         * 注意：请将YOUR_AD_PLACE_ID，YOUR_AD_TOKEN替换为自己的ID和TOKEN
         * 建议提前初始化
         */
        mCpuManager = new NativeCPUManager(RecommendChannelActivity.this, YOUR_APP_ID, this);
        mCpuManager.setLpDarkMode(isdark);
        switch (textSize) {
            case 13:
                mCpuManager.setLpFontSize(CpuLpFontSize.SMALL);
                break;
            case 18:
                mCpuManager.setLpFontSize(CpuLpFontSize.REGULAR);
                break;
            case 23:
                mCpuManager.setLpFontSize(CpuLpFontSize.LARGE);
                break;
        }


        loadAd(mPageIndex);


    }

    private void loadAd(int pageIndex) {
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

        // 从热榜进入的推荐频道后，需要设置listScene = 15 及 热词
        builder.setListScene(15);
        builder.setKeyWords(hotWord);

        mCpuManager.setRequestParameter(builder.build());
        mCpuManager.setRequestTimeoutMillis(10 * 1000); // 如果不设置，则默认10s请求超时
        mCpuManager.loadAd(pageIndex, mChannelId, true);

    }

    @Override
    public void onAdLoaded(List<IBasicCPUData> list) {
        mLinearRecommendContainer.setVisibility(View.VISIBLE);
        if (mRefreshLoadView.isRefreshing() || isResearch) {
            nrAdList.clear();
            isResearch = false;
        }
        if (list != null && list.size() > 0) {
            nrAdList.addAll(list);
            if (nrAdList.size() == list.size()) {
                adapter.notifyDataSetChanged();
            }
            makeToast("Load ad success!");
        }

        mLinearRecommendContainer.requestFocus();
        text_deleteIcon.setVisibility(View.GONE);
        mRefreshLoadView.onLoadFinish();

    }

    @Override
    public void onAdError(String msg, int errorCode) {
        mRefreshLoadView.onLoadFinish();
        text_deleteIcon.setVisibility(View.GONE);
        Log.w(TAG, "onAdError reason:" + msg);
        makeToast("onAdError reason:" + msg);
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

    }

    @Override
    public void onVideoDownloadFailed() {

    }


    @Override
    public void onAdStatusChanged(String appPackageName) {
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

    private void makeToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    class MyAdapter extends BaseAdapter {
        LayoutInflater inflater;
        AQuery aq;
        public MyAdapter(Context context) {
            super();
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            aq = new AQuery(context);
        }

        @Override
        public int getCount() {
            return nrAdList.size();
        }
        @Override
        public IBasicCPUData getItem(int position) {
            return nrAdList.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            IBasicCPUData nrAd = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.feed_native_listview_item, null);
            } else {
                ((ViewGroup) convertView).removeAllViews();
            }
            final NativeCPUView cpuView = new NativeCPUView(RecommendChannelActivity.this);
            cpuView.setAttribute(isdark ? Color.BLACK : Color.WHITE, textSize);
            if (cpuView.getParent() != null) {
                ((ViewGroup) cpuView.getParent()).removeView(cpuView);
            }
            cpuView.setItemData(nrAd, aq);
            ((ViewGroup) convertView).addView(cpuView);
            // 展现时需要调用onImpression上报展现
            nrAd.onImpression(convertView);
            return convertView;
        }

    }
}