package com.baidu.mobads.demo.main.mediaExamples.news.utils;

import android.text.TextUtils;

import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobads.demo.main.mediaExamples.news.adapter.NewsListAdapter;
import com.baidu.mobads.demo.main.mediaExamples.news.bean.FeedItem;
import com.baidu.mobads.nativecpu.IBasicCPUData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FeedParseHelper {
    private final static long TIME_SECOND_YEAR = 365 * 24 * 60 * 60;
    private final static long TIME_SECOND_MONTH = 30 * 24 * 60 * 60;
    private final static long TIME_SECOND_DAY = 24 * 60 * 60;
    private final static long TIME_SECOND_HOUR = 60 * 60;
    private final static long TIME_SECOND_MINUTE = 60;

    public static String getTransformedDateString(String updateTime) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
            Date date = simpleDateFormat.parse(updateTime);
            if (date == null) {
                return updateTime;
            }
            long updateTimeMilli = date.getTime();
            long timeNowMilli = System.currentTimeMillis();
            if (timeNowMilli < updateTimeMilli) {
                return updateTime;
            } else {
                long gapSecond = (timeNowMilli - updateTimeMilli) / 1000;
                if (gapSecond  < TIME_SECOND_MINUTE) {
                    return "刚刚";
                } else if (gapSecond < TIME_SECOND_HOUR) {
                    int minute = (int) (gapSecond / TIME_SECOND_MINUTE);
                    return minute + "分钟前";
                } else if (gapSecond < TIME_SECOND_DAY) {
                    int hour = (int) (gapSecond / TIME_SECOND_HOUR);
                    return hour + "小时前";
                } else if (gapSecond < TIME_SECOND_MONTH) {
                    int day = (int) (gapSecond / TIME_SECOND_DAY);
                    return day + "天前";
                } else if (gapSecond < TIME_SECOND_YEAR) {
                    int month = (int) (gapSecond / TIME_SECOND_MONTH);
                    return month + "月前";
                } else {
                    int year = (int) (gapSecond / TIME_SECOND_YEAR);
                    return year + "年前";
                }
            }
        } catch (Throwable tr) {
            return updateTime;
        }
    }

    /**
     * 获得格式化的播放数
     * @param playCounts 播放数
     * @return 格式化后的字符串
     */
    public static String getFormatPlayCounts(int playCounts) {
        StringBuilder sb = new StringBuilder("播放: ");
        if (playCounts < 0) {
            sb.append(0);
        } else if (playCounts < 10000) {
            sb.append(playCounts);
        } else {
            sb.append(playCounts / 10000);
            int remain = playCounts % 10000;
            if (remain > 0) {
                sb.append(".").append(remain / 1000);
            }
            sb.append("万");
        }
        return sb.toString();
    }

    /**
     * 解析广告响应数据，组装为FeedItem对象
     * @param response 广告响应数据
     * @param id 对象ID，用于在RecyclerView中区分
     * @return FeedItem对象
     */
    public static FeedItem parseItemFromResponse(NativeResponse response, int id) {
        String imageUrl = response.getImageUrl();
        String videoUrl = response.getVideoUrl();
        List<String> smallImageList = response.getMultiPicUrls();
        int itemType;
        if (null != smallImageList && 2 < smallImageList.size()) {
            // 三图
            itemType = NewsListAdapter.AD_FEED_TRI_PIC;
        } else if (!TextUtils.isEmpty(videoUrl)) {
            // 视频
            itemType = NewsListAdapter.AD_FEED_VIDEO;
        } else if (!TextUtils.isEmpty(imageUrl))  {
            // 大图
            itemType = NewsListAdapter.AD_FEED_PIG_PIC;
        } else {
            throw new IllegalArgumentException("Unsupported AD type.");
        }
        return new FeedItem(id, itemType, response);
    }

    /**
     * 解析内容联盟内容数据，组装为FeedItem对象
     * @param data 内容联盟内容数据
     * @param id 对象ID，用于在RecyclerView中区分
     * @return FeedItem对象
     */
    public static FeedItem parseItemFromCPUData(IBasicCPUData data, int id) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("title", data.getTitle());
        // 设置底部字段
        if ("video".equalsIgnoreCase(data.getType())) {
            params.put("bottom_desc", data.getPlayCounts() + "");
        } else {
            params.put("bottom_desc", data.getUpdateTime());
        }
        params.put("author", data.getAuthor());
        // 设置图片参数
        List<String> imageList = data.getImageUrls();
        List<String> smallImageList = data.getSmallImageUrls();
        int itemType;
        if (smallImageList != null && smallImageList.size() > 2) {
            // 三图
            itemType = NewsListAdapter.CONTENT_TRI_PIC;
            params.put("left_image", smallImageList.get(0));
            params.put("mid_image", smallImageList.get(1));
            params.put("right_image", smallImageList.get(2));
        } else if (imageList != null && imageList.size() > 0) {
            // 大图
            itemType = NewsListAdapter.CONTENT_BIG_PIC;
            params.put("left_image", imageList.get(0));
        } else {
            // 视频缩略图
            itemType = NewsListAdapter.CONTENT_VIDEO;
            params.put("left_image", data.getThumbUrl());
        }
        return new FeedItem(id, itemType, params);
    }

    /**
     * 判断是否为下载类广告，需求四个下载类信息字段均不为空
     * @param nrAd 信息流广告元素
     * @return 是否为下载类广告
     */
    public static boolean isDownloadAd(NativeResponse nrAd) {
        return !TextUtils.isEmpty(nrAd.getAppVersion()) && !TextUtils.isEmpty(nrAd.getPublisher())
                && !TextUtils.isEmpty(nrAd.getAppPermissionLink()) && !TextUtils.isEmpty(nrAd.getAppPrivacyLink());
    }

}
