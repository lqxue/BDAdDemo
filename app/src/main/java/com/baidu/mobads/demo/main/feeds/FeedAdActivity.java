package com.baidu.mobads.demo.main.feeds;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.androidquery.AQuery;
import com.baidu.mobad.feeds.ArticleInfo;
import com.baidu.mobad.feeds.BaiduNativeManager;
import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobad.feeds.XAdNativeResponse;
import com.baidu.mobads.component.FeedNativeView;
import com.baidu.mobads.component.StyleParams;
import com.baidu.mobads.demo.main.R;
import com.baidu.mobads.demo.main.feeds.FeedH5.FeedH5ListViewActivity;
import com.baidu.mobads.demo.main.feeds.FeedH5.FeedH5LunBoActivity;
import com.baidu.mobads.demo.main.tools.CustomProgressButton;
import com.baidu.mobads.demo.main.tools.RefreshAndLoadMoreView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
/*
1. 信息流集成参考类：FeedAdActivity
2. 推荐使用BaiduNativeManager集成
    1. 构造请求参数：RequestParameters，非必须。可根据您的需求来创建Parameter
    2. 执行loadFeedAd
    3. 监听onNativeLoad，请求成功会回传List<NativeResponse> ，您可根据NativeResponse中包含的物料内容来创建对应的view
3. 参数回传请联系商务同学，进行相关合作。
4. 下载类广告推荐根据getDownloadStatus获取的下载状态来进行样式渲染。
5. 注意：信息流广告需要您手动发送曝光和点击事件。漏发则无法计费。
    1. 广告数据渲染完毕，View展示的时候使用NativeResponse调用registerViewForInteraction来发送曝光
    2. 绑定view点击事件，发生点击使用NativeResponse调用handleClick来发送点击
6. 智能优选SDK提供的可配置模板类信息流FeedNativeView，支持多种元素格式集合竞价。
7. 智能优选支持自定义视图样式，可以通过StyleParams来配置相关UI参数。包含视图间距、字体颜色、大小、字体、容器背景色。
8. 广告有时间有效期限制，从拉取到使用超过30分钟，将被视为无效广告。可以利用isAdAvailable方法验证。
9. 广告位的返回物料组合配置/返回模板，当APP广告上线后，切勿随意变更，确保前端不会发生崩溃。且前端开发需要对物料进行判空校验，以确定物料是否满足渲染条件，若有不符需要抛弃广告，防止crash。
*/
public class FeedAdActivity extends Activity {
    private static final String TAG = FeedAdActivity.class.getSimpleName();
    private static final String BIG_PIC_AD_PLACE_ID = "2058628";        // 大图+ICON+描述
    private static final String SANTU_AD_PLACE_ID = "5887568";          // 三图
    private static final String H5_LISTVIEW_AD_PLACE_ID = "3143845";    // 三图模版
    private static final String H5_LUNBO_AD_PLACE_ID = "2403624";       // 轮播图+文字
    private static final String FEED_SMART_OPT_AD_PLACE_ID = "6481012"; // 信息流智能优选

    private static final int NATIVE_BIG_PIC = 1;
    private static final int NATIVE_SANTU = 2;
    private static final int NATIVE_SMART_OPT = 5;

    List<NativeResponse> nrAdList = new ArrayList<NativeResponse>(); // 媒体自渲染广告使用
    private NativeAdAdapter mAdapter;
    private RefreshAndLoadMoreView mRefreshLoadView;
    private int mAdPatternType = NATIVE_BIG_PIC;
    private BaiduNativeManager mBaiduNativeManager;
    private boolean isRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_main);
        initView();

        // 默认请求大图广+ICON样式
//        fetchNativeAd(BIG_PIC_AD_PLACE_ID);

        /**
         * Step 1. 创建BaiduNative对象，参数分别为： 上下文context，广告位ID
         * 注意：请将adPlaceId替换为自己的广告位ID
         * 注意：信息流广告对象，与广告位id一一对应，同一个对象可以多次发起请求
         */
        mBaiduNativeManager = new BaiduNativeManager(this, BIG_PIC_AD_PLACE_ID);

        mRefreshLoadView.setRefreshing(true);
        loadFeedAd();

    }

    private void initView() {

        mRefreshLoadView = findViewById(R.id.refresh_container);
        mRefreshLoadView.setLoadAndRefreshListener(new RefreshAndLoadMoreView.LoadAndRefreshListener() {
            @Override
            public void onRefresh() {
                loadFeedAd();
                // fetchNativeAd(getNativeAdPlaceId());
            }

            public void onLoadMore() {
                loadFeedAd();
                // fetchNativeAd(getNativeAdPlaceId());
            }
        });

        ListView list = mRefreshLoadView.getListView();
        mAdapter = new NativeAdAdapter(this);
        list.setAdapter(mAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < nrAdList.size()) {
                    NativeResponse nrAd = nrAdList.get(position);
                    nrAd.handleClick(view);
                    // mRefreshLayout.setRefreshing(false); // 点击取消刷新
                }
            }
        });
    }


//    public void fetchNativeAd(String adPlaceId) {
//        /**
//         * Step 1. 创建BaiduNative对象，参数分别为： 上下文context，广告位ID, BaiduNativeNetworkListener监听（监听广告请求的成功与失败）
//         * 注意：请将adPlaceId替换为自己的广告位ID
//         * 注意：BaiduNative是广告请求的会话，每次请求应该有独立的BaiduNative对象
//         */
//        BaiduNative baidu = new BaiduNative(this, adPlaceId, new BaiduNative.NativeADEventListener() {
//
//            @Override
//            public void onADExposed(NativeResponse response) {
//
//            }
//
//            @Override
//            public void onVideoDownloadSuccess() {
//
//            }
//
//            @Override
//            public void onVideoDownloadFailed() {
//
//            }
//
//            @Override
//            public void onLpClosed() {
//
//            }
//
//            @Override
//            public void onAdClick(NativeResponse response) {
//
//            }
//
//            @Override
//            public void onNativeFail(NativeErrorCode arg0) {
//                Log.w(TAG, "onNativeFail reason:" + arg0.name());
//                mRefreshLoadView.onLoadFinish();
//            }
//
//            @Override
//            public void onNativeLoad(List<NativeResponse> arg0) {
//                // 一个广告只允许展现一次，多次展现、点击只会计入一次
//                if (nativeResponses != null && nativeResponses.size() > 0) {
//                    nrAdList.addAll(nativeResponses);
//                    mAdapter.notifyDataSetChanged();
//                }
//                mRefreshLoadView.onLoadFinish();
//            }
//
//        },8000);
//
//        /**
//         * Step 2. 创建requestParameters对象，并将其传给baidu.makeRequest来请求广告
//         */
//        // 用户点击下载类广告时，是否弹出提示框让用户选择下载与否
////        RequestParameters requestParameters = new RequestParameters.Builder()
////                .downloadAppConfirmPolicy(RequestParameters.DOWNLOAD_APP_CONFIRM_ONLY_MOBILE)
////                .build();
//        // 若与百度进行相关合作，可使用如下接口上报广告的上下文
//        RequestParameters requestParameters = new RequestParameters.Builder()
//                .downloadAppConfirmPolicy(RequestParameters.DOWNLOAD_APP_CONFIRM_ONLY_MOBILE)
//                // 用户维度：用户性别，取值：0-unknown，1-male，2-female
//                .addExtra(ArticleInfo.USER_SEX, "1")
//                // 用户维度：收藏的小说ID，最多五个ID，且不同ID用'/分隔'
//                .addExtra(ArticleInfo.FAVORITE_BOOK, "这是小说的名称1/这是小说的名称2/这是小说的名称3")
//                // 内容维度：小说、文章的名称
//                .addExtra(ArticleInfo.PAGE_TITLE, "测试书名")
//                // 内容维度：小说、文章的ID
//                .addExtra(ArticleInfo.PAGE_ID, "10930484090")
//                // 内容维度：小说分类，一级分类和二级分类用'/'分隔
//                .addExtra(ArticleInfo.CONTENT_CATEGORY, "一级分类/二级分类")
//                // 内容维度：小说、文章的标签，最多10个，且不同标签用'/分隔'
//                .addExtra(ArticleInfo.CONTENT_LABEL, "标签1/标签2/标签3")
//                .build();
//
//        baidu.makeRequest(requestParameters);
//    }

    private void loadFeedAd() {
        // 若与百度进行相关合作，可使用如下接口上报广告的上下文
        RequestParameters requestParameters = new RequestParameters.Builder()
                .downloadAppConfirmPolicy(RequestParameters.DOWNLOAD_APP_CONFIRM_ONLY_MOBILE)
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

        mBaiduNativeManager.loadFeedAd(requestParameters, new BaiduNativeManager.NativeLoadListener() {
            @Override
            public void onNativeLoad(List<NativeResponse> nativeResponses) {
                Log.i(TAG, "onADLoaded:" +
                                        (nativeResponses != null ? nativeResponses.size() : null));
                // 一个广告只允许展现一次，多次展现、点击只会计入一次
                if (nativeResponses != null && nativeResponses.size() > 0) {
                    // 刷新时重制数据
                    if (mRefreshLoadView.isRefreshing()) {
                        nrAdList.clear();
                    }
                    nrAdList.addAll(nativeResponses);
                    mAdapter.notifyDataSetChanged();
                }
                mRefreshLoadView.onLoadFinish();
            }

            @Override
            public void onLoadFail(String message, String errorCode) {
                Log.w(TAG, "onLoadFail reason:" + message + "errorCode:" + errorCode);
                mRefreshLoadView.onLoadFinish();
            }

            @Override
            public void onNativeFail(NativeErrorCode errorCode) {
                // 建议使用onLoadFail回调获取详细的请求失败的原因
                Log.i(TAG, "onNativeFail reason:" + errorCode.name());
            }

            @Override
            public void onVideoDownloadSuccess() {

            }

            @Override
            public void onVideoDownloadFailed() {

            }

            @Override
            public void onLpClosed() {
                Log.i(TAG, "onLpClosed.");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feed_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.feed_patterns) {
            return true;
        }
        switch (id) {
            case R.id.big_pic:  // 大图+ICON+描述
                mAdPatternType = NATIVE_BIG_PIC;
                mRefreshLoadView.setRefreshing(true);
//                fetchNativeAd(BIG_PIC_AD_PLACE_ID);
                mBaiduNativeManager = new BaiduNativeManager(this, BIG_PIC_AD_PLACE_ID);
                loadFeedAd();
                break;
            case R.id.santu:    // 信息流三图
                mAdPatternType = NATIVE_SANTU;
                mRefreshLoadView.setRefreshing(true);
//                fetchNativeAd(SANTU_AD_PLACE_ID);
                mBaiduNativeManager = new BaiduNativeManager(this, SANTU_AD_PLACE_ID);
                loadFeedAd();
                break;
            case R.id.h5_listview:  // 三图模版
                Intent h5List = new Intent(FeedAdActivity.this, FeedH5ListViewActivity.class);
                h5List.putExtra("adPlaceId", H5_LISTVIEW_AD_PLACE_ID);
                startActivity(h5List);
                break;
            case R.id.h5_lunbo:     // 轮播图+文字模版
                Intent h5Lunbo = new Intent(FeedAdActivity.this, FeedH5LunBoActivity.class);
                h5Lunbo.putExtra("adPlaceId", H5_LUNBO_AD_PLACE_ID);
                startActivity(h5Lunbo);
                break;
            case R.id.smart_opt:    // 信息流智能优选
                mAdPatternType = NATIVE_SMART_OPT;
                mRefreshLoadView.setRefreshing(true);
//                fetchNativeAd(FEED_SMART_OPT_AD_PLACE_ID);
                mBaiduNativeManager = new BaiduNativeManager(this, FEED_SMART_OPT_AD_PLACE_ID);
                loadFeedAd();
                break;
            default:
                // nop
        }
        return super.onOptionsItemSelected(item);
    }

    public String getNativeAdPlaceId() {
        String adPlaceID = "";
        switch (mAdPatternType) {
            case NATIVE_BIG_PIC:
                adPlaceID = BIG_PIC_AD_PLACE_ID;
                break;
            case NATIVE_SANTU:
                adPlaceID = SANTU_AD_PLACE_ID;
                break;
            case NATIVE_SMART_OPT:
                adPlaceID = FEED_SMART_OPT_AD_PLACE_ID;
                break;
            default:
                // nop
        }
        return adPlaceID;
    }

    class NativeAdAdapter extends BaseAdapter {
        LayoutInflater inflater;

        public NativeAdAdapter(Context context) {
            super();
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return nrAdList.size();
        }

        @Override
        public NativeResponse getItem(int position) {
            return nrAdList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final NativeResponse nrAd = getItem(position);
            CustomProgressButton progressButton = null;
            switch (mAdPatternType) {
                case NATIVE_BIG_PIC:
                    // 大图广告样式
                    if (convertView == null || ((Integer) convertView.getTag()) != NATIVE_BIG_PIC) {
                        convertView = inflater.inflate(R.layout.feed_native_listview_ad_row, null);
                        convertView.setTag(NATIVE_BIG_PIC);
                    }
                    AQuery aq = new AQuery(convertView);
                    aq.id(R.id.native_icon_image).image(nrAd.getIconUrl(), false, true);
                    aq.id(R.id.native_main_image).image(nrAd.getImageUrl(), false, true);
                    aq.id(R.id.native_text).text(nrAd.getDesc());
                    aq.id(R.id.native_title).text(nrAd.getTitle());
                    // 若为下载类广告，则渲染四个信息字段
                    if (isDownloadAd(nrAd)) {
                        aq.id(R.id.app_name).text(nrAd.getBrandName());
                        progressButton = addDownloadInfo(convertView, aq, nrAd);
                        convertView.findViewById(R.id.native_brand_name).setVisibility(View.GONE);
                    } else {
                        aq.id(R.id.native_brand_name).text(nrAd.getBrandName());
                        hideDownloadInfo(convertView);
                        convertView.findViewById(R.id.native_brand_name).setVisibility(View.VISIBLE);
                    }
                    aq.id(R.id.native_adlogo).image(nrAd.getAdLogoUrl(), false, true);
                    setUnionLogoClick(convertView, R.id.native_adlogo, nrAd);
                    aq.id(R.id.native_baidulogo).image(nrAd.getBaiduLogoUrl(), false, true);
                    setUnionLogoClick(convertView, R.id.native_baidulogo, nrAd);
                    // nrAd.isDownloadApp() ? "下载" : "查看";
                    break;
                case NATIVE_SANTU:
                    // 三图广告样式
                    if (convertView == null || ((Integer) convertView.getTag()) != NATIVE_SANTU) {
                        convertView = inflater.inflate(R.layout.feed_native_santu_item, null);
                        convertView.setTag(NATIVE_SANTU);
                    }
                    AQuery aq1 = new AQuery(convertView);
                    aq1.id(R.id.iv_title).text(nrAd.getTitle());
                    aq1.id(R.id.iv_icon).image(nrAd.getIconUrl());
                    List<String> picUrls = nrAd.getMultiPicUrls();
                    if (picUrls != null && picUrls.size() > 2) {
                        aq1.id(R.id.iv_main1).image(picUrls.get(0));
                        aq1.id(R.id.iv_main2).image(picUrls.get(1));
                        aq1.id(R.id.iv_main3).image(picUrls.get(2));
                    }
                    aq1.id(R.id.iv_desc).text(nrAd.getDesc());
                    aq1.id(R.id.iv_baidulogo).image(nrAd.getBaiduLogoUrl());
                    setUnionLogoClick(convertView, R.id.iv_baidulogo, nrAd);
                    aq1.id(R.id.iv_adlogo).image(nrAd.getAdLogoUrl());
                    setUnionLogoClick(convertView, R.id.iv_adlogo, nrAd);
                    if (isDownloadAd(nrAd)) {
                        aq1.id(R.id.app_name).text(nrAd.getBrandName());
                        progressButton = addDownloadInfo(convertView, aq1, nrAd);
                        convertView.findViewById(R.id.iv_brandname).setVisibility(View.GONE);
                    } else {
                        aq1.id(R.id.iv_brandname).text(nrAd.getBrandName());
                        hideDownloadInfo(convertView);
                        convertView.findViewById(R.id.iv_brandname).setVisibility(View.VISIBLE);
                    }
                    break;
                case NATIVE_SMART_OPT:
                    // 信息流智能优选
                    if (convertView == null || ((Integer) convertView.getTag()) != NATIVE_SMART_OPT) {
                        convertView = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.feed_native_listview_item, null);
                        convertView.setTag(NATIVE_SMART_OPT);
                    } else {
                        ((ViewGroup) convertView).removeAllViews();
                    }
                    final FeedNativeView newAdView = new FeedNativeView(FeedAdActivity.this);
                    if (newAdView.getParent() != null) {
                        ((ViewGroup) newAdView.getParent()).removeView(newAdView);
                    }
                    final NativeResponse ad = (NativeResponse) nrAdList.get(position);
                    newAdView.setAdData((XAdNativeResponse) ad);
                    // 智能优选支持自定义视图样式，可以通过StyleParams来配置相关UI参数。
                    if (position == 2) {
                        // 自定义参数，调整广告效果，需要在setAdData()后调用
                        StyleParams params = new StyleParams.Builder()
                                .setTitleFontColor(getResources().getColor(R.color.blue))
                                .setTitleFontSizeSp(16)
                                .setTitleFontTypeFace(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC))
                                .setImageBackground(getResources().getDrawable(R.drawable.no_ad_icon))
                                .setBrandLeftDp(0)
                                .setBrandFontColor(getResources().getColor(R.color.red))
                                .setBrandFontTypeFace(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC))
                                .build();
                        newAdView.changeViewLayoutParams(params);
                    }
                    ((ViewGroup) convertView).addView(newAdView);
                default:
                    // nop
            }
            final WeakReference<CustomProgressButton> button = new WeakReference<CustomProgressButton>(progressButton);

            // 警告：调用该函数来发送展现，勿漏！
            // recordImpression() 与BaiduNative配套使用
            // nrAd.recordImpression(convertView);

            /**
             * registerViewForInteraction()与BaiduNativeManager配套使用
             * 警告：调用该函数来发送展现，勿漏！
             */
            nrAd.registerViewForInteraction(convertView, new NativeResponse.AdInteractionListener() {
                @Override
                public void onAdClick() {
                    Log.i(TAG, "onAdClick:" + nrAd.getTitle());
                }

                @Override
                public void onADExposed() {
                    Log.i(TAG, "onADExposed:" + nrAd.getTitle());
                }

                /**
                 * 信息流曝光失败回调
                 *
                 * @param reason
                 */
                @Override
                public void onADExposureFailed(int reason) {
                    Log.i(TAG , "onADExposureFailed: " + reason);
                }

                @Override
                public void onADStatusChanged() {
                    if (null != button.get()) {
                        button.get().updateStatus(nrAd);
                    }
                    Log.i(TAG, "onADStatusChanged:" + getBtnText(nrAd));
                }

                @Override
                public void onAdUnionClick() {
                    Log.i(TAG, "onADUnionClick");
                }
            });

            nrAd.setAdPrivacyListener(new NativeResponse.AdPrivacyListener() {
                @Override
                public void onADPermissionShow() {
                    Log.i(TAG, "onADPermissionShow");
                }

                @Override
                public void onADPermissionClose() {
                    Log.i(TAG, "onADPermissionClose");
                }

                @Override
                public void onADPrivacyClick() {
                    Log.i(TAG, "onADPrivacyClick");
                }
            });

            return convertView;
        }
    }

    private CustomProgressButton addDownloadInfo(final View convertView, AQuery aq, final NativeResponse nrAd) {
        convertView.findViewById(R.id.app_download_container).setVisibility(View.VISIBLE);
        aq.id(R.id.native_version).text("版本 " + nrAd.getAppVersion());
        aq.id(R.id.native_publisher).text(nrAd.getPublisher());

        View privacy = convertView.findViewById(R.id.native_privacy);
        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nrAd.privacyClick();
            }
        });

        View permission = convertView.findViewById(R.id.native_permission);
        permission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nrAd.permissionClick();
            }
        });

        CustomProgressButton button = convertView.findViewById(R.id.native_download_button);
        initProgressButton(button, nrAd);

        return button;
    }

    // 点击联盟logo打开官网
    private void setUnionLogoClick(final View convertView, int logoId, final NativeResponse nrAd) {
        View logo = convertView.findViewById(logoId);
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nrAd.unionLogoClick();
            }
        });
    }

    private void hideDownloadInfo(final View convertView) {
        convertView.findViewById(R.id.app_download_container).setVisibility(View.GONE);
    }

    private boolean isDownloadAd(NativeResponse nrAd) {
        return !TextUtils.isEmpty(nrAd.getAppVersion()) && !TextUtils.isEmpty(nrAd.getPublisher())
                && !TextUtils.isEmpty(nrAd.getAppPrivacyLink()) && !TextUtils.isEmpty(nrAd.getAppPermissionLink());
    }

    public void initProgressButton(CustomProgressButton button, NativeResponse nrAd) {
        button.initWithResponse(nrAd);
        button.setTextColor(Color.parseColor("#FFFFFF"));
        // 字体大小适配屏幕
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int textSize = (int) (12 * metrics.scaledDensity + 0.5f);
        button.setTextSize(textSize);
        button.setTypeFace(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC));
        button.setForegroundColor(Color.parseColor("#3388FF"));
        button.setBackgroundColor(Color.parseColor("#D7E6FF"));
    }

    // 下载状态及下载的进度
    private String getBtnText(NativeResponse nrAd) {
        if (nrAd == null) {
            return "";
        }
        if (nrAd.isDownloadApp()) {
            int status = nrAd.getDownloadStatus();
            if (status >= 0 && status <= 100) {
                return "下载中：" + status + "%";
            } else if (status == 101) {
                return "点击安装";
            } else if (status == 102) {
                return "继续下载";
            } else if (status == 103) {
                return "点击启动";
            } else if (status == 104) {
                return "重新下载";
            } else {
                return "点击下载";
            }
        }
        return "查看详情";
    }
}
