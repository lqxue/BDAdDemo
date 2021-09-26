package com.baidu.mobads.demo.main;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.Toast;

import com.baidu.mobads.CpuAdView;
import com.baidu.mobads.demo.main.mediaExamples.novel.utils.SharedPreUtils;
import com.baidu.mobads.nativecpu.CpuLpFontSize;
import com.baidu.mobads.production.cpu.CPUWebAdRequestParam;

/**
 * icon_cpu
 */
public class CpuAdActivity extends Activity {
    public static final String TAG = "CpuAdActivity";
    
    // 测试id
    private static String DEFAULT_APPSID = "d77e414";

    private CpuLpFontSize mDefaultCpuLpFontSize = CpuLpFontSize.REGULAR;
    private boolean isDarkMode = false;
    private CpuAdView mCpuView;
    
    public enum CpuChannel {
        /**
         * 娱乐频道
         */
        CHANNEL_ENTERTAINMENT(1001),
        /**
         * 体育频道
         */
        CHANNEL_SPORT(1002),
        /**
         * 图片频道
         */
        CHANNEL_PICTURE(1003),
        /**
         * 手机频道
         */
        CHANNEL_MOBILE(1005),
        /**
         * 财经频道
         */
        CHANNEL_FINANCE(1006),
        /**
         * 汽车频道
         */
        CHANNEL_AUTOMOTIVE(1007),
        /**
         * 房产频道
         */
        CHANNEL_HOUSE(1008),
        /**
         * 热点频道
         */
        CHANNEL_HOTSPOT(1021),

        /**
         * 本地频道
         */
        CHANNEL_LOCAL(1080),

        /**
         * 热榜频道
         */
        CHANNEL_HOT(1090);

        private int value;
        
        CpuChannel(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cpu);
        initSpinner();
        Button button = (Button) this.findViewById(R.id.button);
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showSelectedCpuWebPage();
            }
        });
    }

    /**
     *
     *  内容联盟模板渲染，展示频道
     *
     */
    private void showSelectedCpuWebPage() {

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

        CPUWebAdRequestParam cpuWebAdRequestParam = new CPUWebAdRequestParam.Builder()
                .setLpFontSize(mDefaultCpuLpFontSize)
                .setLpDarkMode(isDarkMode)
                .setCityIfLocalChannel("北京")
                .setCustomUserId(outerId)
                .build();

        mCpuView = new CpuAdView(this, getAppsid(), getChannel().getValue(), cpuWebAdRequestParam,
                new CpuAdView.CpuAdViewInternalStatusListener() {

            /**
             *  网页加载错误
             * @param message 错误信息
             */
            @Override
            public void loadDataError(String message) {
                Log.d(TAG, "loadDataError: " + message);
                makeToast("loadDataError: " + message);
            }

            /**
             *  广告点击回调
             */
            @Override
            public void onAdClick() {
                Log.d(TAG, "onAdClick: ");
                makeToast("onAdClick");
            }

            /**
             *  广告展现回调
             * @param impressionAdNums 有效展现数
             */
            @Override
            public void onAdImpression(String impressionAdNums) {
                Log.d(TAG, "impressionAdNums =  " + impressionAdNums);
                makeToast("impressionAdNums =  " + impressionAdNums);
            }

            /**
             *  内容点击回调
             */
            @Override
            public void onContentClick() {
                Log.d(TAG, "onContentClick: ");
                makeToast("onContentClick");
            }

            /**
             *  内容展现回调
             * @param impressionContentNums 内容展现数量
             */
            @Override
            public void onContentImpression(String impressionContentNums) {
                Log.d(TAG, "impressionContentNums =  " + impressionContentNums);
                makeToast("impressionContentNums = " + impressionContentNums);
            }
        });

        final RelativeLayout parentLayout = (RelativeLayout) this.findViewById(R.id.parent_block);
        RelativeLayout.LayoutParams reLayoutParams = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        reLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        parentLayout.addView(mCpuView, reLayoutParams);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 检测广告组件是否需要处理返回按键
        if (mCpuView.onKeyBackDown(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private void makeToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    /**
     * 初始化下拉框
     */
    private void initSpinner() {
        Spinner channelSpinner = (Spinner) this.findViewById(R.id.channel);
        channelSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });
        List<SpinnerItem> list = new ArrayList<SpinnerItem>();
        list.add(new SpinnerItem("娱乐频道", CpuChannel.CHANNEL_ENTERTAINMENT));
        list.add(new SpinnerItem("体育频道", CpuChannel.CHANNEL_SPORT));
        list.add(new SpinnerItem("图片频道", CpuChannel.CHANNEL_PICTURE));
        list.add(new SpinnerItem("手机频道", CpuChannel.CHANNEL_MOBILE));
        list.add(new SpinnerItem("财经频道", CpuChannel.CHANNEL_FINANCE));
        list.add(new SpinnerItem("汽车频道", CpuChannel.CHANNEL_AUTOMOTIVE));
        list.add(new SpinnerItem("房产频道", CpuChannel.CHANNEL_HOUSE));
        list.add(new SpinnerItem("热点频道", CpuChannel.CHANNEL_HOTSPOT));
        list.add(new SpinnerItem("本地频道", CpuChannel.CHANNEL_LOCAL));
        list.add(new SpinnerItem("热榜", CpuChannel.CHANNEL_HOT));
        ArrayAdapter<SpinnerItem> dataAdapter = new ArrayAdapter<SpinnerItem>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        channelSpinner.setAdapter(dataAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCpuView != null) {
            mCpuView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCpuView != null) {
            mCpuView.onPause();
        }
    }

    /**
     * 获取appsid
     * 
     * @return
     */
    private String getAppsid() {
        return DEFAULT_APPSID;
    }

    /**
     * 获取频道
     * 
     * @return
     */
    private CpuChannel getChannel() {
        Spinner channelSpinner = (Spinner) this.findViewById(R.id.channel);
        SpinnerItem selectedItem = (SpinnerItem) channelSpinner.getSelectedItem();
        return selectedItem.getChannel();
    }




    class SpinnerItem extends Object {
        /**
         * 频道名称
         */
        String text;
        /**
         * 频道id
         */
        CpuChannel channel;

        public SpinnerItem(String text, CpuChannel cpuChannel) {
            this.text = text;
            this.channel = cpuChannel;
        }

        @Override
        public String toString() {
            return text;
        }

        CpuChannel getChannel() {
            return channel;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cpu_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cpu_menu_light_mode:
                isDarkMode = false;
                break;
            case R.id.cpu_menu_dark_mode:
                 isDarkMode = true;
                break;
            case R.id.cpu_menu_small:
                mDefaultCpuLpFontSize = CpuLpFontSize.SMALL;
                break;
            case R.id.cpu_menu_middle:
                mDefaultCpuLpFontSize = CpuLpFontSize.REGULAR;
                break;
            case R.id.cpu_menu_big:
                mDefaultCpuLpFontSize = CpuLpFontSize.LARGE;
                break;
            default: break;
        }
        showSelectedCpuWebPage();
        return true;
    }
}
