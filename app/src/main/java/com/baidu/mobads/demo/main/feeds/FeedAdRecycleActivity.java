package com.baidu.mobads.demo.main.feeds;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.baidu.mobad.feeds.ArticleInfo;
import com.baidu.mobad.feeds.BaiduNativeManager;
import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobads.component.XNativeView;
import com.baidu.mobads.demo.main.R;

import java.util.ArrayList;
import java.util.List;

public class FeedAdRecycleActivity extends Activity {
    private static final String TAG = FeedAdRecycleActivity.class.getSimpleName();
    private static final String FEED_VIDEO_AD_PLACE_ID = "2362913";        // 信息流视频

    List<NativeResponse> nrAdList = new ArrayList<NativeResponse>(); // 媒体自渲染广告使用
    private RecycleAdapter mAdapter;
    private RecyclerView mRecycleView;
    private BaiduNativeManager mBaiduNativeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_recycle_view);

        initView();

        /**
         * Step 1. 创建BaiduNative对象，参数分别为： 上下文context，广告位ID
         * 注意：请将adPlaceId替换为自己的广告位ID
         * 注意：信息流广告对象，与广告位id一一对应，同一个对象可以多次发起请求
         */
        mBaiduNativeManager = new BaiduNativeManager(this, FEED_VIDEO_AD_PLACE_ID);

        loadFeedAd();

    }

    private void initView() {
        mRecycleView = findViewById(R.id.container);
        mAdapter = new RecycleAdapter();
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));
        mRecycleView.setAdapter(mAdapter);
    }

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

        mBaiduNativeManager.loadFeedAd(requestParameters, new BaiduNativeManager.FeedAdListener() {
            @Override
            public void onNativeLoad(List<NativeResponse> nativeResponses) {
                Log.i(TAG, "onNativeLoad:" +
                           (nativeResponses != null ? nativeResponses.size() : null));
                // 一个广告只允许展现一次，多次展现、点击只会计入一次
                if (nativeResponses != null && nativeResponses.size() > 0) {
                    nrAdList.addAll(nativeResponses);
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNativeFail(NativeErrorCode errorCode) {
                Log.w(TAG, "onNativeFail reason:" + errorCode.name());
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


    class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder> {

        public RecycleAdapter() {
            setHasStableIds(true);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View item = LayoutInflater.from(viewGroup.getContext()).
                    inflate(viewType == 1 ? R.layout.feed_native_video_item : R.layout.feed_native_listview_ad_row,
                            viewGroup, false);
            return new ViewHolder(item, viewType);
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, int position) {
            final NativeResponse nrAd = nrAdList.get(position);
            AQuery aq = new AQuery(viewHolder.itemView);
            viewHolder.mTitle.setText(nrAd.getTitle());
            viewHolder.mText.setText(nrAd.getDesc());
            viewHolder.mBrandName.setText(nrAd.getBrandName());
            if (viewHolder.mType == 1) {
                viewHolder.mVideo.setNativeItem(nrAd);
            } else {
                aq.id(viewHolder.mImage).image(nrAd.getImageUrl(), false, true);
            }
            aq.id(viewHolder.mIcon).image(nrAd.getIconUrl(), false, true);
            aq.id(viewHolder.mAdLogo).image(nrAd.getAdLogoUrl(), false, true);
            setUnionLogoClick(viewHolder.mAdLogo, nrAd);
            aq.id(viewHolder.mBdLogo).image(nrAd.getBaiduLogoUrl(), false, true);
            setUnionLogoClick(viewHolder.mBdLogo, nrAd);
            // 若为下载类广告，则渲染四个信息字段
            if (isDownloadAd(nrAd)) {
                addDownloadInfo(viewHolder, aq, nrAd);
            } else {
                hideDownloadInfo(viewHolder);
            }
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nrAd.handleClick(viewHolder.itemView);
                }
            });
        }

        @Override
        public int getItemCount() {
            return nrAdList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if ("video".equals(nrAdList.get(position).getAdMaterialType())) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public void onViewAttachedToWindow(ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            int id = (int) holder.getItemId();
            // 调用render，对于设置自动播放的广告会自动播放
            if (holder.mType == 1) {
                holder.mVideo.render();
            }
            // 可见时需要调用recordImpression上报展现曝光
            nrAdList.get(id).recordImpression(holder.itemView);
            if (id == (nrAdList.size() - 1)) {
                loadFeedAd();
            }
        }

        @Override
        public void onViewDetachedFromWindow(ViewHolder holder) {
            super.onViewDetachedFromWindow(holder);
//            if (holder.mType == 1) {
//                holder.mVideo.pause();
//            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView mIcon;
            XNativeView mVideo;
            ImageView mImage;
            TextView mTitle;
            TextView mText;
            TextView mBrandName;
            // 下载类广告信息
            TextView mVersion;
            TextView mPrivacy;
            TextView mPermission;
            TextView mPublisher;
            // Logo
            ImageView mAdLogo;
            ImageView mBdLogo;
            int mType;

            public ViewHolder(View itemView, int type) {
                super(itemView);
                mType = type;
                mIcon = itemView.findViewById(R.id.native_icon_image);
                if (type == 1) {
                    mVideo = itemView.findViewById(R.id.native_main_image);
                    mImage = null;
                } else {
                    mVideo = null;
                    mImage = itemView.findViewById(R.id.native_main_image);
                }
                mText = itemView.findViewById(R.id.native_text);
                mTitle = itemView.findViewById(R.id.native_title);
                mBrandName = itemView.findViewById(R.id.native_brand_name);
                mAdLogo = itemView.findViewById(R.id.native_adlogo);
                mBdLogo = itemView.findViewById(R.id.native_baidulogo);
                // 下载类广告信息
                mVersion = itemView.findViewById(R.id.native_version);
                mPublisher = itemView.findViewById(R.id.native_publisher);
                mPrivacy = itemView.findViewById(R.id.native_privacy);
                mPermission = itemView.findViewById(R.id.native_permission);
            }
        }
    }

    private void addDownloadInfo(final RecycleAdapter.ViewHolder viewHolder, AQuery aq, final NativeResponse nrAd) {
        viewHolder.mBrandName.setVisibility(View.GONE);
        aq.id(R.id.native_brand_name).text(nrAd.getBrandName());
        aq.id(R.id.native_version).text("版本 " + nrAd.getAppVersion());
        aq.id(R.id.native_publisher).text(nrAd.getPublisher());
        if (null != viewHolder.mPrivacy) {
            viewHolder.mPrivacy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nrAd.privacyClick();
                }
            });
        }
        if (null != viewHolder.mPermission) {
            viewHolder.mPermission.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nrAd.permissionClick();
                }
            });
        }
        nrAd.setAdPrivacyListener(new NativeResponse.AdPrivacyListener() {
            @Override
            public void onADPrivacyClick() {
                Log.i(TAG, "onADPrivacyClick: " + nrAd.getTitle());
            }

            @Override
            public void onADPermissionShow() {
                Log.i(TAG, "onADPermissionShow: " + nrAd.getTitle());
            }

            @Override
            public void onADPermissionClose() {
                Log.i(TAG, "onADPermissionClose: " + nrAd.getTitle());
            }
        });
    }

    private void hideDownloadInfo(final RecycleAdapter.ViewHolder viewHolder) {
        viewHolder.mVersion.setVisibility(View.GONE);
        viewHolder.mPublisher.setVisibility(View.GONE);
        viewHolder.mPrivacy.setVisibility(View.GONE);
        viewHolder.mPermission.setVisibility(View.GONE);
        viewHolder.mBrandName.setVisibility(View.VISIBLE);
    }

    // 点击联盟logo打开官网
    private void setUnionLogoClick(ImageView logo, final NativeResponse nrAd) {
        logo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nrAd.unionLogoClick();
            }
        });
    }

    private boolean isDownloadAd(NativeResponse nrAd) {
        return !TextUtils.isEmpty(nrAd.getAppVersion()) && !TextUtils.isEmpty(nrAd.getPublisher())
                && !TextUtils.isEmpty(nrAd.getAppPrivacyLink()) && !TextUtils.isEmpty(nrAd.getAppPermissionLink());
    }
}
