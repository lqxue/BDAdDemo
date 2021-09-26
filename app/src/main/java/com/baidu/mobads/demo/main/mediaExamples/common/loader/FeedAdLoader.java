package com.baidu.mobads.demo.main.mediaExamples.common.loader;

import android.content.Context;
import android.util.Log;

import com.baidu.mobad.feeds.ArticleInfo;
import com.baidu.mobad.feeds.BaiduNativeManager;
import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobads.demo.main.mediaExamples.common.parser.Parser;
import com.baidu.mobads.demo.main.mediaExamples.news.bean.FeedItem;

import java.util.List;

/**
 * 信息流元素类广告加载器，用于加载资讯类Demo中的信息流广告项
 *
 * 对外提供方法：
 * {@link #getDataLoaded(int)}: 父类方法，从数据队列中获取指定数量的{@link FeedItem}对象用于展现
 *
 * 初始化方法：{@link #FeedAdLoader(Context, String, int, Parser, LoadListener)}
 */
public class FeedAdLoader<E> extends AbstractFeedLoader<E> {
    private static final String TAG = "FeedAdLoader";

    private BaiduNativeManager mAdManager;
    private String mAdPlaceId;
    private Parser<NativeResponse, E> mParser;

    private FeedAdLoader(Context context, String adPlaceId, int threshold,
                         Parser<NativeResponse, E> parser, LoadListener listener) {
        super(context, threshold, listener);
        mAdPlaceId = adPlaceId;
        mParser = parser;
    }

    public static <T> FeedAdLoader<T> getInstance(Context context, String adPlaceId, int threshold,
                                           Parser<NativeResponse, T> parser, LoadListener listener) {
        FeedAdLoader<T> loader = new FeedAdLoader<>(context, adPlaceId, threshold, parser, listener);
        // 初始化BaiduNativeManager
        loader.initManager();
        // 实例化后开始请求内容
        loader.load();
        return loader;
    }

    @Override
    protected void initManager() {
        mAdManager = new BaiduNativeManager(mContext, mAdPlaceId);
    }

    @Override
    protected void loadData() {
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
        mAdManager.loadFeedAd(requestParameters, new BaiduNativeManager.FeedAdListener() {
            @Override
            public void onNativeLoad(List<NativeResponse> nativeResponses) {
                Log.i(TAG, "onNativeLoad:" +
                        (nativeResponses != null ? nativeResponses.size() : null));
                // 预加载广告响应
                offerDataLoaded(nativeResponses, mParser);
            }

            @Override
            public void onNativeFail(NativeErrorCode errorCode) {
                Log.w(TAG, "onNativeFail reason:" + errorCode.name());
                mListener.onLoadException("onNativeFail reason:" + errorCode.name(), errorCode.ordinal());
                checkAndLoad(true);
            }

            @Override
            public void onVideoDownloadSuccess() {

            }

            @Override
            public void onVideoDownloadFailed() {
                Log.w(TAG, "onVideoDownloadFailed");
                mListener.onLoadException("onVideoDownloadFailed", 0);
                checkAndLoad(true);
            }

            @Override
            public void onLpClosed() {

            }
        });
    }
}
