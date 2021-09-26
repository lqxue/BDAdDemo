package com.baidu.mobads.demo.main.search;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import com.androidquery.AQuery;
import com.baidu.mobad.feeds.ArticleInfo;
import com.baidu.mobad.feeds.BaiduNative;
import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobads.demo.main.R;

import java.util.ArrayList;
import java.util.List;

public class InsiteActivity extends Activity implements AdapterView.OnItemClickListener {

    private static final String TAG = InsiteActivity.class.getSimpleName();
    private ListView mListView;
    private List<NativeResponse> nrAdList = new ArrayList<NativeResponse>();
    private String YOUR_AD_PLACE_ID = "7099861";
    private SearchAdAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insite);

        final SearchView searchView = findViewById(R.id.search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                nrAdList.clear();
                mAdapter.notifyDataSetChanged();
                if (TextUtils.isEmpty(query)) {
                    return true;
                }
                BaiduNative baiduNative = new BaiduNative(InsiteActivity.this, YOUR_AD_PLACE_ID,
                        mListener, true, 8000, "insite");

                RequestParameters requestParameters = new RequestParameters.Builder()
                        .downloadAppConfirmPolicy(RequestParameters.DOWNLOAD_APP_CONFIRM_ONLY_MOBILE)
                        .addExtra(ArticleInfo.QUERY_WORD, query).build();
                baiduNative.makeRequest(requestParameters);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mListView = (ListView) findViewById(R.id.searchResult);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < nrAdList.size()) {
                    NativeResponse nrAd = nrAdList.get(position);
                    nrAd.handleClick(view);
                    // mRefreshLayout.setRefreshing(false); // 点击取消刷新
                }
            }
        });
        mAdapter = new SearchAdAdapter(this);
        mListView.setAdapter(mAdapter);
    }

    class SearchAdAdapter extends BaseAdapter {
        LayoutInflater inflater;

        public SearchAdAdapter(Context context) {
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

            if (nrAd.getStyleType() == 28 || nrAd.getStyleType() == 29 || nrAd.getStyleType() == 30) {
                //大图
                convertView = inflater.inflate(R.layout.search_listview_single, null);
                AQuery aq = new AQuery(convertView);
                aq.id(R.id.search_main_image).image(nrAd.getImageUrl(), false, true);
                aq.id(R.id.search_text).text(nrAd.getDesc());
                aq.id(R.id.search_title).text(nrAd.getTitle());
            } else if (nrAd.getStyleType() == 33 || nrAd.getStyleType() == 34) {
                //左右图
                convertView = inflater.inflate(R.layout.search_native_rightpic, null);
                AQuery aq1 = new AQuery(convertView);
                aq1.id(R.id.search_title).text(nrAd.getTitle());
                aq1.id(R.id.search_main_image).image(nrAd.getImageUrl(), false, true);
            } else if (nrAd.getStyleType() == 35 || nrAd.getStyleType() == 36) {
                //三图
                convertView = inflater.inflate(R.layout.search_native_threepic, null);
                AQuery aq1 = new AQuery(convertView);
                aq1.id(R.id.search_title).text(nrAd.getTitle());
                List<String> picUrls = nrAd.getMultiPicUrls();
                if (picUrls != null && picUrls.size() > 2) {
                    aq1.id(R.id.s_main1).image(picUrls.get(0));
                    aq1.id(R.id.s_main2).image(picUrls.get(1));
                    aq1.id(R.id.s_main3).image(picUrls.get(2));
                }
                aq1.id(R.id.search_desc).text(nrAd.getDesc());

            }


            // 警告：调用该函数来发送展现，勿漏！
            // recordImpression() 与BaiduNative配套使用
            nrAd.recordImpression(convertView);
            return convertView;
        }
    }

    private BaiduNative.BaiduNativeNetworkListener mListener = new BaiduNative.BaiduNativeNetworkListener() {
        @Override
        public void onNativeLoad(List<NativeResponse> nativeResponses) {
            Log.i(TAG, "ddloaded");
            nrAdList.addAll(nativeResponses);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onNativeFail(NativeErrorCode errorCode) {
            Log.i(TAG, "ddloaded fail" + errorCode);
            nrAdList.clear();
            mAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        NativeResponse ad = (NativeResponse) nrAdList.get(position);
        if (ad != null) {
            // 调用sdk提供的点击api
            ad.handleClick(view);
        }
    }
}
