package com.baidu.mobads.demo.main.mediaExamples.common.loader;

import android.content.Context;
import android.util.Log;

import com.baidu.mobads.demo.main.mediaExamples.common.parser.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 抽象的信息流加载器，用于加载Demo中的信息流项
 *
 * 需要实体子类继承实现的方法：
 * {@link #initManager()}: 负责初始化加载器的具体manager
 * {@link #loadData()}: 负责加载请求数据
 */
public abstract class AbstractFeedLoader<E> {
    private static final String TAG = "FeedLoader";

    protected static final int DEFAULT_THRESHOLD = 5;
    protected static int mThreshold;

    protected LinkedBlockingQueue<E> bufferQueue;
    protected Context mContext;
    protected LoadListener mListener;

    protected boolean isLoading = false;
    protected boolean finished = false;

    public AbstractFeedLoader(Context context, int threshold, final LoadListener listener) {
        mContext = context;
        mListener = listener;
        mThreshold = threshold;
        bufferQueue = new LinkedBlockingQueue<E>();
    }

    protected abstract void initManager();

    protected abstract void loadData();

    protected void load() {
        if (!isLoading && !finished) {
            isLoading = true;
            loadData();
        }
    }

    public List<E> getDataLoaded(final int contentNum) {
        int num = Math.min(contentNum, mThreshold);
        List<E> list = new ArrayList<E>();
        for (int i = 0; i < num; i++) {
            E item = bufferQueue.poll();
            if (null != item) {
                list.add(item);
            }
        }
        // 如果数量不足，就发起加载请求
        checkAndLoad(false);
        return list;
    }

    protected <T> void offerDataLoaded(List<T> dataList, Parser<T, E> parser) {
        if (dataList != null && !dataList.isEmpty()) {
            for (T data : dataList) {
                bufferQueue.offer(parser.parseData(data));
            }
        }
        checkAndLoad(true);
    }

    protected synchronized void checkAndLoad(boolean isLoadingFinished) {
        if (isLoadingFinished) {
            isLoading = false;
        }
        if (bufferQueue.size() < mThreshold) {
            Log.i(TAG, "onAdLoaded: size = " + bufferQueue.size() + "loadContent()");
            // 若内容buffer数量不足，就继续请求
            load();
        } else if (mListener != null) {
            mListener.onLoadComplete();
        }
    }

    public void release() {
        finished = true;
        bufferQueue.clear();
        mListener = null;
    }

    public interface LoadListener {

        void onLoadComplete();

        void onLoadException(String msg, int errorCode);
    }
}
