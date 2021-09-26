package com.baidu.mobads.demo.main.content;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.baidu.mobads.demo.main.R;
import com.baidu.mobads.demo.main.tools.RefreshAndLoadMoreView;

import java.util.ArrayList;
import java.util.List;

public class NativeContentAdActivity extends Activity {

    private static final String TAG = NativeContentAdActivity.class.getSimpleName();
    private static final String DEMO_AD_PLACE_ID = "7160687";

    private BaiduNativeManager mBaiduNativeManager;
    private RefreshAndLoadMoreView mRefreshLoadView;
    private ContentAdAdapter mAdapter;
    List<NativeResponse> mDataList = new ArrayList<NativeResponse>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_main);

        initView();

        /*
         * Step 1. 创建BaiduNativeManager对象，参数分别为： 上下文context，广告位ID
         * 注意：请将adPlaceId替换为自己的广告位ID
         * 注意：信息流广告对象，与广告位id一一对应，同一个对象可以多次发起请求
         */
        mBaiduNativeManager = new BaiduNativeManager(this, DEMO_AD_PLACE_ID);

        // 请求广告时, 设置下拉刷新组件, 开始刷新
        mRefreshLoadView.setRefreshing(true);

        /*
         * Step 2. 使用BaiduNativeManager对象请求内容位广告
         * 注意：请求时可以通过请求参数上报上下文
         */
        loadContentAd();
    }

    private void initView() {
        mRefreshLoadView = findViewById(R.id.refresh_container);
        mRefreshLoadView.setLoadAndRefreshListener(new RefreshAndLoadMoreView.LoadAndRefreshListener() {
            @Override
            public void onRefresh() {
                loadContentAd();
            }

            public void onLoadMore() {
                loadContentAd();
            }
        });

        ListView list = mRefreshLoadView.getListView();
        mAdapter = new ContentAdAdapter(this);
        list.setAdapter(mAdapter);

        // 设置广告&内容的点击事件
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < mDataList.size()) {
                    NativeResponse nrAd = mDataList.get(position);
                    // 当广告&内容发生击后行为，需要调用handleClick()触发广告行为
                    nrAd.handleClick(view);
                }
            }
        });
    }

    // 请求内容位广告数据
    private void loadContentAd() {
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

        mBaiduNativeManager.loadContentAd(requestParameters, new BaiduNativeManager.FeedAdListener() {
            @Override
            public void onNativeLoad(List<NativeResponse> nativeResponses) {
                Log.i(TAG, "onNativeLoad:" +
                           (nativeResponses != null ? nativeResponses.size() : null));
                // 一个广告只允许展现一次，多次展现、点击只会计入一次
                if (nativeResponses != null && nativeResponses.size() > 0) {
                    // 刷新时重置数据
                    if (mRefreshLoadView.isRefreshing()) {
                        mDataList.clear();
                    }
                    mDataList.addAll(nativeResponses);
                    mAdapter.notifyDataSetChanged();
                }
                mRefreshLoadView.onLoadFinish();
            }

            @Override
            public void onNativeFail(NativeErrorCode errorCode) {
                Log.w(TAG, "onNativeFail reason:" + errorCode.name());
                mRefreshLoadView.onLoadFinish();
            }

            @Override
            public void onVideoDownloadSuccess() {
                Log.i(TAG, "onVideoDownloadSuccess.");
            }

            @Override
            public void onVideoDownloadFailed() {
                Log.i(TAG, "onVideoDownloadFailed.");
            }

            @Override
            public void onLpClosed() {
                Log.i(TAG, "onLpClosed");
            }
        });
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

    class ContentAdAdapter extends BaseAdapter {

        LayoutInflater inflater;

        ContentAdAdapter(Context context) {
            super();
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public NativeResponse getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.cpu_native_list_item, null);
            }

            final NativeResponse content = mDataList.get(position);

            AQuery aq = new AQuery(convertView);
            aq.id(R.id.native_icon_image).image(content.getIconUrl(), false, true);
            aq.id(R.id.image_big_pic).image(content.getImageUrl(), false, true);
            // aq.id(R.id.native_author).text(content.getBrandName());
            aq.id(R.id.native_title).text(content.getTitle());
            aq.id(R.id.comment_num).text(content.getBrandName());
            aq.id(R.id.update_time).text(content.getDesc());

            /*
             * Step 3. 展示广告，调用registerViewForInteraction()方法上报广告曝光，以及注册相关回调方法；
             */
            content.registerViewForInteraction(convertView, new NativeResponse.AdInteractionListener() {
                @Override
                public void onAdClick() {
                    Log.i(TAG, "onAdClick:" + content.getTitle());
                }

                @Override
                public void onADExposed() {
                    Log.i(TAG, "onADExposed:" + content.getTitle());
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
                    Log.i(TAG, "onADStatusChanged:" + getBtnText(content));
                }

                @Override
                public void onAdUnionClick() {
                    Log.i(TAG, "onADUnionClick");
                }
            });
            return convertView;
        }
    }
}
