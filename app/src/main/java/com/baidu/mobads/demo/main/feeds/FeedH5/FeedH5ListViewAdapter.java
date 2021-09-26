package com.baidu.mobads.demo.main.feeds.FeedH5;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobads.BaiduNativeAdPlacement;
import com.baidu.mobads.BaiduNativeH5AdView;
import com.baidu.mobads.BaiduNativeH5AdViewManager;
import com.baidu.mobads.demo.main.R;

import static com.baidu.mobads.demo.main.feeds.FeedH5.FeedH5ListViewActivity.INTERVAL_BETWEEN_AD;

public class FeedH5ListViewAdapter extends BaseAdapter implements OnClickListener {

    private Context context;

    private List<Object> mList;
    private static final int TYPE_AD = 0;
    private static final int TYPE_DATA = 1;
    private static final int TYPE_MAX_COUNT = TYPE_DATA + 1;

    public FeedH5ListViewAdapter(Context context, List<Object> list) {
        this.context = context;
        mList = list;
    }

    public int getCount() {
        return mList.size();
    }

    public Object getItem(int position) {
        return mList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (position % INTERVAL_BETWEEN_AD == 0) {
            return TYPE_AD;
        } else {
            return TYPE_DATA;
        }
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }

    class ViewHolder {
        TextView time;
        TextView doSomething;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Object data = mList.get(position);
        ViewHolder holder = null;
        int type = getItemViewType(position);

        if (type == TYPE_AD) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.feed_h5_item_placement, null);
            } else {
                ((ViewGroup) convertView).removeAllViews();
            }

            final BaiduNativeH5AdView newAdView = BaiduNativeH5AdViewManager.getInstance()
                    .getBaiduNativeH5AdView(context, (BaiduNativeAdPlacement) data, R.drawable.no_ad_icon);
            if (newAdView.getParent() != null) {
                ((ViewGroup) newAdView.getParent()).removeView(newAdView);
            }
            newAdView.setEventListener(new BaiduNativeH5AdView.BaiduNativeH5EventListner() {
                @Override
                public void onAdClick() {
                    ;
                }

                @Override
                public void onAdFail(String arg0) {
                    ;
                }

                @Override
                public void onAdShow() {
                    ;
                }

                @Override
                public void onAdDataLoaded() {
                    // TODO Auto-generated method stub
                    
                }
            });
            // 模板宽高比需要和http://mssp.baidu.com/上代码位的设置相同
            double scale = 1.0 * 2 / 1;
            int width = context.getResources().getDisplayMetrics().widthPixels
                    - (int) (this.context.getResources().getDimension(R.dimen.activity_horizontal_margin) * 2);
            int height = (int) (width / scale);
            RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(width, height);

            newAdView.setLayoutParams(rllp);
            RequestParameters requestParameters =
                    new RequestParameters.Builder().setWidth(width).setHeight(height).build();
            
            newAdView.makeRequest(requestParameters);
            newAdView.recordImpression();
            ((ViewGroup) convertView).addView(newAdView);
        } else {

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.feed_h5_item, null);
                holder = new ViewHolder();
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.doSomething = (TextView) convertView.findViewById(R.id.doSomething);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            FeedH5ContentInfo info = (FeedH5ContentInfo) data;
            holder.time.setText(info.mTime);
            holder.doSomething.setText(info.mDoSomething);
        }

        return convertView;
    }


    @Override
    public void onClick(View v) {
        int destination = (Integer) v.getTag();
        switch (destination) {
            case 0:
                startActivityMethod("TestActivityNo1");
                break;
            case 1:
                startActivityMethod("TestActivityNo2");
                break;
            default:
                break;
        }
    }

    private void startActivityMethod(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        context.startActivity(intent);
    }

}
