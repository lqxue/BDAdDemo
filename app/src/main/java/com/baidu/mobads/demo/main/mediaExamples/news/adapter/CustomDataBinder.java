package com.baidu.mobads.demo.main.mediaExamples.news.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobads.demo.main.R;
import com.baidu.mobads.demo.main.mediaExamples.news.bean.FeedItem;
import com.baidu.mobads.demo.main.mediaExamples.news.utils.FeedParseHelper;
import com.baidu.mobads.demo.main.tools.CustomProgressButton;

import java.lang.ref.WeakReference;

import static com.baidu.mobads.demo.main.mediaExamples.news.adapter.NewsListAdapter.AD_FEED_PIG_PIC;
import static com.baidu.mobads.demo.main.mediaExamples.news.adapter.NewsListAdapter.AD_FEED_TRI_PIC;
import static com.baidu.mobads.demo.main.mediaExamples.news.adapter.NewsListAdapter.AD_FEED_VIDEO;
import static com.baidu.mobads.demo.main.mediaExamples.news.adapter.NewsListAdapter.CONTENT_BIG_PIC;
import static com.baidu.mobads.demo.main.mediaExamples.news.adapter.NewsListAdapter.CONTENT_TRI_PIC;
import static com.baidu.mobads.demo.main.mediaExamples.news.adapter.NewsListAdapter.CONTENT_VIDEO;

public class CustomDataBinder {
    private View mItemView;
    private AQuery mAq;

    public CustomDataBinder(View itemView) {
        mItemView = itemView;
        mAq = new AQuery(itemView);
    }

    public void bindContentViews(FeedItem item, Holders.FeedContentViewHolder contentViewHolder) {
        contentViewHolder.mTitle.setText(item.getTitle());
        contentViewHolder.mAuthor.setText(item.getAuthor());
        contentViewHolder.mVideoPlay.setVisibility(View.GONE);
        String bottomDesc = item.getBottomDesc();
        switch (item.getItemType()) {
            case CONTENT_BIG_PIC:
                mAq.id(R.id.image_big_pic).image(item.getmLeftImageUrl());
                bottomDesc = FeedParseHelper.getTransformedDateString(bottomDesc);
                break;
            case CONTENT_TRI_PIC:
                mAq.id(R.id.image_left).image(item.getmLeftImageUrl());
                mAq.id(R.id.image_mid).image(item.getmMidImageUrl());
                mAq.id(R.id.image_right).image(item.getmRightImageUrl());
                bottomDesc = FeedParseHelper.getTransformedDateString(bottomDesc);
                break;
            case CONTENT_VIDEO:
                mAq.id(R.id.image_big_pic).image(item.getmLeftImageUrl());
                contentViewHolder.mVideoPlay.setVisibility(View.VISIBLE);
                bottomDesc = FeedParseHelper.getFormatPlayCounts(Integer.parseInt(bottomDesc));
                break;
            default:
        }
        contentViewHolder.mBottomDesc.setText(bottomDesc);
    }

    public void bindAdViews(FeedItem item, Holders.FeedAdViewHolder adViewHolder) {
        final NativeResponse nrAd = item.getNrAd();
        CustomProgressButton downloadButton = null;  // 下载类广告的进度按钮
        adViewHolder.nrAd = nrAd;
        // 为AdView的各个组件加载广告数据
        mAq.id(adViewHolder.mTitle).text(nrAd.getTitle());
        mAq.id(adViewHolder.mText).text(nrAd.getDesc());
        mAq.id(adViewHolder.mBrandName).text(nrAd.getBrandName());
        mAq.id(adViewHolder.mIcon).image(nrAd.getIconUrl(), false, true);
        mAq.id(adViewHolder.mAdLogo).image(nrAd.getAdLogoUrl(), false, true);
        mAq.id(adViewHolder.mBdLogo).image(nrAd.getBaiduLogoUrl(), false, true);
        // 设置百香果logo的点击跳转事件
        View.OnClickListener unionLogoClicked = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nrAd.unionLogoClick();
            }
        };
        mAq.id(adViewHolder.mAdLogo).clicked(unionLogoClicked);
        mAq.id(adViewHolder.mBdLogo).clicked(unionLogoClicked);
        // 加载广告的图片资源
        // 此处仅举例三类广告：1.大图+ICON+描述 2.三图并排 3.使用NativeView的视频广告
        switch (item.getItemType()) {
            case AD_FEED_PIG_PIC:
                mAq.id(((Holders.FeedPicAdViewHolder) adViewHolder).mImage)
                        .image(nrAd.getImageUrl(), false, true);
                break;
            case AD_FEED_TRI_PIC:
                for (int i = 0; i < 3; i++) {
                    mAq.id(((Holders.FeedTriPicAdViewHolder) adViewHolder).mImageList.get(i))
                            .image(nrAd.getMultiPicUrls().get(i), false, true);
                }
                break;
            case AD_FEED_VIDEO:
                ((Holders.FeedVideoAdViewHolder) adViewHolder).mVideo.setNativeItem(nrAd);
        }
        // 若为下载类广告，展示隐私权限信息
        if (FeedParseHelper.isDownloadAd(nrAd)) {
            adViewHolder.mAppVersion.setText("版本 " + nrAd.getAppVersion());
            adViewHolder.mAppPublisher.setText(nrAd.getPublisher());
            adViewHolder.mPrivacyLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nrAd.privacyClick();
                }
            });
            adViewHolder.mPermissionLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nrAd.permissionClick();
                }
            });
            if (item.getItemType() != AD_FEED_VIDEO) {
                // 非视频广告需要展示 隐私权限信息的展示卡片
                adViewHolder.mDownLoadInfoContainer.setVisibility(View.VISIBLE);
                adViewHolder.mBrandName.setVisibility(View.GONE);

                TextView adAppName = adViewHolder.itemView.findViewById(R.id.app_name);
                adAppName.setText(nrAd.getBrandName());
                adAppName.setVisibility(View.VISIBLE);
                downloadButton = adViewHolder.itemView.findViewById(R.id.native_download_button);
                initProgressButton(downloadButton, nrAd);
            } else {
                // 视频广告需要展示 权限隐私信息字段
                adViewHolder.mAppVersion.setVisibility(View.VISIBLE);
                adViewHolder.mAppPublisher.setVisibility(View.VISIBLE);
                adViewHolder.mPrivacyLink.setVisibility(View.VISIBLE);
                adViewHolder.mPermissionLink.setVisibility(View.VISIBLE);
            }
        } else {
            if (item.getItemType() != AD_FEED_VIDEO) {
                // 非视频广告需要隐藏 隐私权限信息的展示卡片
                adViewHolder.mDownLoadInfoContainer.setVisibility(View.GONE);
                adViewHolder.mBrandName.setVisibility(View.VISIBLE);
            } else {
                // 视频广告需要隐藏 权限隐私信息字段
                adViewHolder.mAppVersion.setVisibility(View.GONE);
                adViewHolder.mAppPublisher.setVisibility(View.GONE);
                adViewHolder.mPrivacyLink.setVisibility(View.GONE);
                adViewHolder.mPermissionLink.setVisibility(View.GONE);
            }
        }
        final WeakReference<CustomProgressButton> wButton = new WeakReference<CustomProgressButton>(downloadButton);
        // 为广告视图设置点击
        adViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nrAd.handleClick(mItemView);
            }
        });
    }

    private void initProgressButton(CustomProgressButton button, NativeResponse nrAd) {
        button.initWithResponse(nrAd);
        button.setTextColor(Color.parseColor("#FFFFFF"));
        // 字体大小适配屏幕
        DisplayMetrics metrics = button.getContext().getResources().getDisplayMetrics();
        int textSize = (int) (12 * metrics.scaledDensity + 0.5f);
        button.setTextSize(textSize);
        button.setTypeFace(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD_ITALIC));
        button.setForegroundColor(Color.parseColor("#3388FF"));
        button.setBackgroundColor(Color.parseColor("#D7E6FF"));
    }
}
