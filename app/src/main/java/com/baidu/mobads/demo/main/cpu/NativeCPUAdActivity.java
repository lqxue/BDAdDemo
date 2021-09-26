package com.baidu.mobads.demo.main.cpu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobads.AppActivity;
import com.baidu.mobads.demo.main.R;
import com.baidu.mobads.demo.main.mediaExamples.hot.HotActivity;
import com.baidu.mobads.demo.main.mediaExamples.novel.utils.SharedPreUtils;
import com.baidu.mobads.demo.main.tools.RefreshAndLoadMoreView;
import com.baidu.mobads.nativecpu.CPUAdRequest;
import com.baidu.mobads.nativecpu.CpuLpFontSize;
import com.baidu.mobads.nativecpu.NativeCPUManager;
import com.baidu.mobads.nativecpu.IBasicCPUData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
1. 集成参考类：NativeCPUAdActivity
2. 原生渲染的内容联盟，请求广告成功后，返回的广告列表包含内容数据+广告。
3. 如果需要在锁屏场景下展示广告落地页，需要设置AppActivity.canLpShowWhenLocked(boolean canShow);默认为 false，广告展现前全局设置即可
4. 注意：内容联盟原生渲染需要您手动发送广告曝光和广告点击事件。漏发则无法计费。
* */
public class NativeCPUAdActivity extends Activity implements NativeCPUManager.CPUAdListener {
    private static final String TAG = NativeCPUAdActivity.class.getSimpleName();
    private final String YOUR_APP_ID = "c0da1ec4"; // 双引号中填写自己的APPSID
    private View cpuDataContainer;
    private Button showAd;
    private Button loadAd;
    private int mChannelId = 1001; // 默认娱乐频道
    private int mPageIndex = 1;
    private List<IBasicCPUData> nrAdList = new ArrayList<IBasicCPUData>();
    private NativeCPUManager mCpuManager;
    private RefreshAndLoadMoreView mRefreshLoadView;
    private ListView listView;
    private MyAdapter adapter;
    private boolean isDark;
    /**
     *  13sp: 小号字体
     *  18sp: 中号字体
     *  23sp: 大号字体
     */
    private int mDefaultTextSize;
    private int mDefaultBgColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cpu_native_list);
        initAdListView();
        initSpinner();
        loadAd = findViewById(R.id.btn_load);
        loadAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mChannelId == 1090) {
                    Intent intent = new Intent(NativeCPUAdActivity.this, HotActivity.class);
                    intent.putExtra("isdark",isDark);
                    intent.putExtra("textSize", mDefaultTextSize);
                    startActivity(intent);
                } else {
                    loadAd(mPageIndex);
                }
            }
        });
        showAd = findViewById(R.id.btn_show);
        showAd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAdList();
            }
        });
        showAd.setEnabled(false);
        // 设置详情也的actionbar颜色
        AppActivity.setActionBarColorTheme(AppActivity.ActionBarColorTheme.ACTION_BAR_GREEN_THEME);
        /**
         * Step 1. NativeCPUManager，参数分别为： 上下文context（必须为Activity），appsid, 认证token, CPUAdListener（监听广告请求的成功与失败）
         * 注意：请将YOUR_AD_PLACE_ID，YOUR_AD_TOKEN替换为自己的ID和TOKEN
         * 建议提前初始化
         */
        mCpuManager = new NativeCPUManager(NativeCPUAdActivity.this, YOUR_APP_ID, this);

        /**
         * 可选设置: 设置暗黑模式或调整内容详情页的字体大小
         */
        mCpuManager.setLpFontSize(CpuLpFontSize.REGULAR);
        mCpuManager.setLpDarkMode(false);
        mDefaultBgColor = Color.WHITE;
        mDefaultTextSize = 18;
    }

    private void initAdListView() {
        cpuDataContainer = findViewById(R.id.cpuDataContainer);
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

        cpuDataContainer.setVisibility(View.GONE);
    }

    private void initSpinner() {
        // 频道类目
        Spinner channelSpinner = (Spinner) this.findViewById(R.id.channel);
        channelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mChannelId = ((SpinnerItem) parent.getItemAtPosition(position)).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });
        List<SpinnerItem> list2 = new ArrayList<SpinnerItem>();
        list2.add(new SpinnerItem("娱乐频道", 1001));
        list2.add(new SpinnerItem("体育频道", 1002));
        list2.add(new SpinnerItem("财经频道", 1006));
        list2.add(new SpinnerItem("汽车频道", 1007));
        list2.add(new SpinnerItem("时尚频道", 1009));
        list2.add(new SpinnerItem("文化频道", 1036));
        list2.add(new SpinnerItem("科技频道", 1013));
        list2.add(new SpinnerItem("推荐频道", 1022));
        list2.add(new SpinnerItem("视频频道", 1057));
        list2.add(new SpinnerItem("图集频道", 1068));
        list2.add(new SpinnerItem("本地频道", 1080));
        list2.add(new SpinnerItem("热榜", 1090));
        ArrayAdapter<SpinnerItem> dataAdapter2 = new ArrayAdapter<SpinnerItem>(this,
                android.R.layout.simple_spinner_item, list2);
        dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        channelSpinner.setAdapter(dataAdapter2);
    }

    public void showAdList() {
        cpuDataContainer.setVisibility(View.VISIBLE);
        listView.setAdapter(adapter);
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

        // 如果媒体选择了本地频道，可以传入城市名字，否则会根据ip地址推送内容
        if (mChannelId == 1080) {
            // 城市名字建议传入 "XXX市" 或 "XXX县"
            builder.setCityIfLocalChannel("北京市");
        }
        if (mChannelId == 1090) {
            builder.setListScene(19);
        }



        mCpuManager.setRequestParameter(builder.build());
        mCpuManager.setRequestTimeoutMillis(5 * 1000); // 如果不设置，则默认5s请求超时

        /**
         * Step3：调用请求接口，请求广告
         */
        makeToast("Start loadAd!");
        mCpuManager.loadAd(pageIndex, mChannelId, true);

        showAd.setEnabled(false);
    }


    /**
     * 请求广告成功，返回广告列表
     * @param list 广告+内容数据
     */
    @Override
    public void onAdLoaded(List<IBasicCPUData> list) {
        if (mRefreshLoadView.isRefreshing()) {
            nrAdList.clear();
        }
        if (list != null && list.size() > 0) {
            nrAdList.addAll(list);
            showAd.setEnabled(true);
            if (nrAdList.size() == list.size()) {
                adapter.notifyDataSetChanged();
            }
            makeToast("Load ad success!");
        }
        mRefreshLoadView.onLoadFinish();
    }

    @Override
    public void onAdError(String msg, int errorCode) {
        mRefreshLoadView.onLoadFinish();
        Log.w(TAG, "onAdError reason:" + msg);
        Log.w(TAG, "onAdError errorCode:"  + errorCode);
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
        Log.i(TAG, "onAdImpression");
    }

    @Override
    public void onContentImpression() {
        Log.i(TAG, "onContentImpression");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cpu_menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cpu_menu_light_mode:
                isDark = false;
                mDefaultBgColor = Color.WHITE;
                mCpuManager.setLpDarkMode(false);
                cpuDataContainer.setBackgroundColor(Color.WHITE);
                adapter.setStyleParam(mDefaultBgColor, mDefaultTextSize);
                break;
            case R.id.cpu_menu_dark_mode:
                isDark = true;
                mDefaultBgColor = Color.BLACK;
                mCpuManager.setLpDarkMode(true);
                cpuDataContainer.setBackgroundColor(Color.BLACK);
                adapter.setStyleParam(mDefaultBgColor, mDefaultTextSize);
                break;
            case R.id.cpu_menu_small:
                mDefaultTextSize = 13;
                mCpuManager.setLpFontSize(CpuLpFontSize.SMALL);
                adapter.setStyleParam(mDefaultBgColor, mDefaultTextSize);
                break;
            case R.id.cpu_menu_middle:
                mDefaultTextSize = 18;
                mCpuManager.setLpFontSize(CpuLpFontSize.REGULAR);
                adapter.setStyleParam(mDefaultBgColor, mDefaultTextSize);
                break;
            case R.id.cpu_menu_big:
                mDefaultTextSize = 23;
                mCpuManager.setLpFontSize(CpuLpFontSize.LARGE);
                adapter.setStyleParam(mDefaultBgColor, mDefaultTextSize);
                break;
            default: break;
        }
        return true;
    }

    class MyAdapter extends BaseAdapter {
        LayoutInflater inflater;
        AQuery aq;
        private int bg = Color.WHITE;
        private int textSize = 18;
        public MyAdapter(Context context) {
            super();
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            aq = new AQuery(context);
        }

        public void setStyleParam(int bgColor, int wordSize) {
            bg = bgColor;
            textSize = wordSize;
            notifyDataSetChanged();
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
            final NativeCPUView cpuView = new NativeCPUView(NativeCPUAdActivity.this);
            cpuView.setAttribute(bg, textSize);
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

    private void makeToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    class SpinnerItem extends Object {
        /**
         * 名称
         */
        String mName;
        /**
         * id
         */
        int mId;

        public SpinnerItem(String name, int id) {
            mName = name;
            mId = id;
        }

        @Override
        public String toString() {
            return mName;
        }

        int getId() {
            return mId;
        }

    }
}
