package com.baidu.mobads.demo.main.mediaExamples.novel;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;


import com.baidu.mobad.feeds.ArticleInfo;
import com.baidu.mobad.feeds.BaiduNativeManager;
import com.baidu.mobad.feeds.NativeErrorCode;
import com.baidu.mobad.feeds.NativeResponse;
import com.baidu.mobad.feeds.RequestParameters;
import com.baidu.mobad.feeds.XAdNativeResponse;
import com.baidu.mobads.component.FeedPortraitVideoView;
import com.baidu.mobads.component.IFeedPortraitListener;
import com.baidu.mobads.component.INativeVideoListener;
import com.baidu.mobads.component.XNativeView;
import com.baidu.mobads.demo.main.R;
import com.baidu.mobads.demo.main.mediaExamples.novel.base.BaseMVPActivity;
import com.baidu.mobads.demo.main.mediaExamples.novel.model.bean.BookChapterBean;
import com.baidu.mobads.demo.main.mediaExamples.novel.model.bean.CollBookBean;
import com.baidu.mobads.demo.main.mediaExamples.novel.model.local.BookRepository;
import com.baidu.mobads.demo.main.mediaExamples.novel.model.local.ReadSettingManager;
import com.baidu.mobads.demo.main.mediaExamples.novel.presenter.ReadPresenter;
import com.baidu.mobads.demo.main.mediaExamples.novel.presenter.contract.ReadContract;
import com.baidu.mobads.demo.main.mediaExamples.novel.ui.adapter.CategoryAdapter;
import com.baidu.mobads.demo.main.mediaExamples.novel.ui.dialog.ReadSettingDialog;
import com.baidu.mobads.demo.main.mediaExamples.novel.utils.BrightnessUtils;
import com.baidu.mobads.demo.main.mediaExamples.novel.utils.Constant;
import com.baidu.mobads.demo.main.mediaExamples.novel.utils.RxUtils;
import com.baidu.mobads.demo.main.mediaExamples.novel.utils.ScreenUtils;
import com.baidu.mobads.demo.main.mediaExamples.novel.utils.StringUtils;
import com.baidu.mobads.demo.main.mediaExamples.novel.utils.SystemBarUtils;
import com.baidu.mobads.demo.main.mediaExamples.novel.widget.page.PageLoader;
import com.baidu.mobads.demo.main.mediaExamples.novel.widget.page.PageMode;
import com.baidu.mobads.demo.main.mediaExamples.novel.widget.page.PageStyle;
import com.baidu.mobads.demo.main.mediaExamples.novel.widget.page.PageView;
import com.baidu.mobads.demo.main.mediaExamples.novel.widget.page.TxtChapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;

import static android.support.v4.view.ViewCompat.LAYER_TYPE_SOFTWARE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 *  ###############################################################################################
 *  小说类场景集成百度联盟广告示例说明：
 *  请求广告的时机 ： 在新的一章节刚开始的时候，发起请求 {@link ReadActivity#tryRequestAdOnNewChapter()},去
 *  获得这一章节所需要的广告数据（包含信息流大图、信息流视频、小视频）。
 *  当即将翻到显示广告的页面时，直接从已保存的广告数据中随机取出一个出来，如
 *   {@link ReadActivity#requestBaiDuFeedVideoFrom_ExistedFeedVideoDatas()};
 *
 *   note:信息流视频/小视频播放的时机与广告展现日志发送的时机均在{@link PageView#setPageMode(PageMode pageMode)}
 *   ##############################################################################################
 */


public class ReadActivity extends BaseMVPActivity<ReadContract.Presenter>
        implements ReadContract.View {
    private static final String TAG = "ReadActivity";
    public static final int REQUEST_MORE_SETTING = 1;
    public static final String EXTRA_COLL_BOOK = "extra_coll_book";
    public static final String EXTRA_IS_COLLECTED = "extra_is_collected";

    private final Uri BRIGHTNESS_MODE_URI =
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE);
    private final Uri BRIGHTNESS_URI =
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
    private final Uri BRIGHTNESS_ADJ_URI =
            Settings.System.getUriFor("screen_auto_brightness_adj");

    private static final int WHAT_CATEGORY = 1;
    private static final int WHAT_CHAPTER = 2;


    /**
     *  与广告请求相关：
     */
    private static final String BIG_PIC_AD_PLACE_ID = "2058628";        // 大图+ICON+描述
    private View mAdView; // 放置广告的View

    /**
     *  与小视频广告相关参数
     */
    private static final String AdPlaceId_SMALL_VIDEO = "6164562";

    /**
     *  与 信息流视频 相关参数
     */
    private static final String FEED_VIDEO_AD_PLACE_ID = "2362913";

    /**
     *  mAdView的id要根据请求到的广告类型时刻更改
     */
    public static final int ID_FEED_VIDEO = 0x1052;
    public static final int ID_SMALL_VIDEO = 0x1053;
    public static final int ID_FEED_PIC = 0x1054;


    @BindView(R.id.read_dl_slide)
    DrawerLayout mDlSlide;
    @BindView(R.id.read_abl_top_menu)
    AppBarLayout mAblTopMenu;
    @BindView(R.id.read_pv_page)
    PageView mPvPage;
    @BindView(R.id.read_tv_page_tip)
    TextView mTvPageTip;
    @BindView(R.id.read_ll_bottom_menu)
    LinearLayout mLlBottomMenu;
    @BindView(R.id.read_tv_pre_chapter)
    TextView mTvPreChapter;
    @BindView(R.id.read_sb_chapter_progress)
    SeekBar mSbChapterProgress;
    @BindView(R.id.read_tv_next_chapter)
    TextView mTvNextChapter;
    @BindView(R.id.read_tv_category)
    TextView mTvCategory;
    @BindView(R.id.read_tv_night_mode)
    TextView mTvNightMode;
    @BindView(R.id.read_tv_setting)
    TextView mTvSetting;
    @BindView(R.id.read_iv_category)
    ListView mLvCategory;
    @BindView(R.id.bottom_banner)
    RelativeLayout mBottomBanner;
    @BindView(R.id.native_icon_image)
    ImageView mIconImg;
    @BindView(R.id.native_title)
    TextView mTitleTv;
    @BindView(R.id.native_text)
    TextView mTextTv;
    @BindView(R.id.tv_novel_info)
    TextView mInfoTv;

    private ReadSettingDialog mSettingDialog;
    private  PageLoader mPageLoader;
    private Animation mTopInAnim;
    private Animation mTopOutAnim;
    private Animation mBottomInAnim;
    private Animation mBottomOutAnim;
    private CategoryAdapter mCategoryAdapter;
    private CollBookBean mCollBook;
    private PowerManager.WakeLock mWakeLock;

    // 请求广告的参数
    private RequestParameters requestParameters;
    // 保存每一章节所有信息流视频广告数据的集合
    private List<NativeResponse> mFeedVideoDatas = new ArrayList<>();
    // 保存每一章节所有小视频广告数据的集合
    private List<NativeResponse> mSmallVideoDatas = new ArrayList<>();
    // 保存每一章节左右信息流图片数据的集合
    private List<NativeResponse> mFeedAdImgs = new ArrayList<>();


    private static class MyHandler extends Handler {
        private final WeakReference<ReadActivity> mActivity;
        public MyHandler(ReadActivity readActivity) {
            mActivity = new WeakReference<ReadActivity>(readActivity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            ReadActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case WHAT_CATEGORY:
                        activity.mLvCategory.setSelection(activity.mPageLoader.getChapterPos());
                        break;
                    case WHAT_CHAPTER:
                        activity.mPageLoader.openChapter();
                        break;
                }
            }
        }
    }
    private Handler mHandler = new MyHandler(this);
    // 接收电池信息和时间更新的广播
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                int level = intent.getIntExtra("level", 0);
                mPageLoader.updateBattery(level);
            }
            // 监听分钟的变化
            else if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                mPageLoader.updateTime();
            }
        }
    };
    // 亮度调节监听
    // 由于亮度调节没有 Broadcast 而是直接修改 ContentProvider 的。所以需要创建一个 Observer 来监听 ContentProvider 的变化情况。
    private ContentObserver mBrightObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange);
            // 判断当前是否跟随屏幕亮度，如果不是则返回
            if (selfChange || !mSettingDialog.isBrightFollowSystem()) return;
            // 如果系统亮度改变，则修改当前 Activity 亮度
            if (BRIGHTNESS_MODE_URI.equals(uri)) {
                Log.d(TAG, "亮度模式改变");
            } else if (BRIGHTNESS_URI.equals(uri) && !BrightnessUtils.isAutoBrightness(ReadActivity.this)) {
                Log.d(TAG, "亮度模式为手动模式 值改变");
                BrightnessUtils.setBrightness(ReadActivity.this, BrightnessUtils.getScreenBrightness(ReadActivity.this));
            } else if (BRIGHTNESS_ADJ_URI.equals(uri) && BrightnessUtils.isAutoBrightness(ReadActivity.this)) {
                Log.d(TAG, "亮度模式为自动模式 值改变");
                BrightnessUtils.setDefaultBrightness(ReadActivity.this);
            } else {
                Log.d(TAG, "亮度调整 其他");
            }
        }
    };

    private boolean isCollected = false; // isFromSDCard
    private boolean isNightMode = false;
    private boolean isFullScreen = false;
    private boolean isRegistered = false;
    private String mBookId;

    public static void startActivity(Context context, CollBookBean collBook, boolean isCollected) {
        context.startActivity(new Intent(context, ReadActivity.class)
                .putExtra(EXTRA_IS_COLLECTED, isCollected)
                .putExtra(EXTRA_COLL_BOOK, collBook));
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_read;
    }

    @Override
    protected ReadContract.Presenter bindPresenter() {
        return new ReadPresenter();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mCollBook = getIntent().getParcelableExtra(EXTRA_COLL_BOOK);
        isCollected = getIntent().getBooleanExtra(EXTRA_IS_COLLECTED, false);
        isNightMode = ReadSettingManager.getInstance().isNightMode();
        isFullScreen = ReadSettingManager.getInstance().isFullScreen();
        mBookId = mCollBook.get_id();
    }

    @Override
    protected void setUpToolbar(Toolbar toolbar) {
        super.setUpToolbar(toolbar);
        //设置标题
        toolbar.setTitle(mCollBook.getTitle());
        //半透明化StatusBar
        SystemBarUtils.transparentStatusBar(this);
    }

    @Override
    protected void initWidget() {
        super.initWidget();

        //获取页面加载器
        mPageLoader = mPvPage.getPageLoader(mCollBook);
        //禁止滑动展示DrawerLayout
        mDlSlide.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        //侧边打开后，返回键能够起作用
        mDlSlide.setFocusableInTouchMode(false);
        mSettingDialog = new ReadSettingDialog(this, mPageLoader);

        setUpAdapter();

        //夜间模式按钮的状态
        toggleNightMode();

        //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(mReceiver, intentFilter);

        //设置当前Activity的Brightness
        if (ReadSettingManager.getInstance().isBrightnessAuto()) {
            BrightnessUtils.setDefaultBrightness(this);
        } else {
            BrightnessUtils.setBrightness(this, ReadSettingManager.getInstance().getBrightness());
        }

        //初始化屏幕常亮类
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "myapp:keep bright");

        //隐藏StatusBar
        mPvPage.post(new Runnable() {
            @Override
            public void run() {
                hideSystemBar();
            }
        });
        //初始化TopMenu
        initTopMenu();
        //初始化BottomMenu
        initBottomMenu();


        /**
         * ####################################################################################
         *                                                                                   ##
         *                                                                                   ##
         * Step 1. 创建BaiduNative对象，参数分别为： 上下文context，广告位ID                 ##
         * 注意：请将adPlaceId替换为自己的广告位ID                                           ##
         * 注意：信息流广告对象，与广告位id一一对应，同一个对象可以多次发起请求              ##
         *                                                                                   ##
         *                                                                                   ##
         *  ###################################################################################                                                                                  #
         */


        requestBaiDuUnionAd_bottom();

        final View coverPageView = LayoutInflater.from(this).inflate(R.layout.layout_cover_view, null, false);
        mPvPage.setReaderAdListener(new PageView.ReaderAdListener() {

            @Override
            public View onRequestAd() {
                Random  r = new Random();
                int anInt = r.nextInt(3);
                if (anInt == 0) {
                    // 请求百度联盟Union的信息流大图广告
                    requestBaiDuUnionAdFrom_ExistedFeedImgData();
                } else if (anInt == 1) {
                    // 请求百度联盟Union的小视频广告
                    requestBaiDuUnionVideoFrom_ExistedSmallVideoDatas();
                }
                else {
                    // 请求百度联盟Union的信息流视频广告
                    requestBaiDuFeedVideoFrom_ExistedFeedVideoDatas();
                    // 更换底通的广告
                    requestBaiDuUnionAd_bottom();
                }
                return mAdView;
            }

            @Override
            public View getCoverPageView() {
                return coverPageView;
            }

            /**
             *  小说翻页场景中，已请求到的广告发送展现日志
             *  发送展现的判断机制 位于{@link PageView#computeScroll()}  中
             * @param response
             */
            @Override
            public void onAdExposed(NativeResponse response) {
                    response.registerViewForInteraction(mAdView, new NativeResponse.AdInteractionListener() {
                        @Override
                        public void onAdClick() {
                            Log.d(TAG, "onAdClick: ");
                        }

                        @Override
                        public void onADExposed() {
                            Log.d(TAG, "onADExposed: ");
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
                            Log.d(TAG, "onADStatusChanged: ");
                        }

                        @Override
                        public void onAdUnionClick() {
                            Log.d(TAG, "onAdUnionClick: ");
                        }
                    });
            }

            @Override
            public void playAdVideo(FeedPortraitVideoView feedPortraitVideoView) {
                feedPortraitVideoView.setFeedPortraitListener(new IFeedPortraitListener() {
                    @Override
                    public void playRenderingStart() {
                        Log.d(TAG, "playRenderingStart: ");
                    }

                    @Override
                    public void playPause() {

                    }

                    @Override
                    public void playResume() {

                    }

                    @Override
                    public void playCompletion() {
                        Log.d(TAG, "playCompletion: ");
                    }

                    @Override
                    public void playError() {

                    }
                });
                feedPortraitVideoView.play();
            }

            @Override
            public void playFeedAdVideo(XNativeView xNativeView) {
                xNativeView.setNativeVideoListener(new INativeVideoListener() {
                    @Override
                    public void onRenderingStart() {
                        Log.d(TAG, "onRenderingStart: 信息流视频 ");
                    }

                    @Override
                    public void onPause() {

                    }

                    @Override
                    public void onResume() {

                    }

                    @Override
                    public void onCompletion() {
                        Log.d(TAG, "onCompletion: 信息流视频");

                    }

                    @Override
                    public void onError() {

                    }
                });
                xNativeView.render();
            }


        });


    }

    /**
     * 从已有的信息流视频广告数据集合中取出一条出来
     */
    private void requestBaiDuFeedVideoFrom_ExistedFeedVideoDatas() {
        if (mFeedVideoDatas.size() > 0) {
            Random random = new Random();
            final NativeResponse targetData = mFeedVideoDatas.get(random.nextInt(mFeedVideoDatas.size()));
            /**
             * 加载信息流视频布局
             */
            mAdView = LayoutInflater.from(ReadActivity.this)
                    .inflate(R.layout.novel_feed_horizontal_video, null);

            mAdView.setId(ID_FEED_VIDEO);


           XNativeView video =  mAdView.findViewById(R.id.novel_xnativeView);
           TextView descTv = mAdView.findViewById(R.id.novel_desc);
           ImageView iconImg = mAdView.findViewById(R.id.lly_icon);
           TextView titleTv = mAdView.findViewById(R.id.lly_title);
           Button bt = mAdView.findViewById(R.id.lly_bt);

           Glide.with(ReadActivity.this)
                   .load(targetData.getIconUrl())
                   .into(iconImg);

           descTv.setText(TextUtils.isEmpty(targetData.getDesc()) ?
                           targetData.getTitle() : targetData.getDesc());
           titleTv.setText(targetData.getBrandName());

            /**
             * 设置数据和封面图
             */

            video.setNativeItem(targetData);

            if (isDownloadAd(targetData)) {
               bt.setText("立即下载");
            } else {
                bt.setText("查看详情");
            }
            video.setVideoMute(true);
            mPvPage.setFeedVideoViewData(video);
        }
    }

    /**
     *  从已保存的小视频广告数据中拿出一个
     */
    private void requestBaiDuUnionVideoFrom_ExistedSmallVideoDatas() {
        if (mSmallVideoDatas.size() > 0) {
            Random random = new Random();
            NativeResponse targetData = mSmallVideoDatas.get(random.nextInt(mSmallVideoDatas.size()));

            View inflate = LayoutInflater.from(ReadActivity.this).inflate(R.layout.novel_vertical_video, null);
            /**
             *  note:为了便于复现媒体问题，这里布局采用动态设置padding值，
             *   目前左右留出的padiding值是屏幕宽度的1/6，可以手动修改来复现媒体问题
             */
            int widthPixels = getResources().getDisplayMetrics().widthPixels;
            int heightPixels = getResources().getDisplayMetrics().heightPixels;
            int variablePadding = widthPixels / 6;
            int availableWidth = widthPixels - 2 * variablePadding;
            int availableHeight = availableWidth * 16 / 9;


            int bannerMeasuredHeight = mBottomBanner.getMeasuredHeight();
            int default_paddingTop = ScreenUtils.dpToPx(50);
            int default_paddingBotttom = 0;

            if (availableHeight >= heightPixels - bannerMeasuredHeight - default_paddingTop) {
                /**
                 *  高度超出了屏幕可显示范围（极少出现情况）
                 */
                default_paddingTop = ScreenUtils.dpToPx(20);
                default_paddingBotttom = ScreenUtils.dpToPx(10);
            }

            inflate.setPadding(variablePadding, default_paddingTop, variablePadding, default_paddingBotttom);
            FeedPortraitVideoView feedPortraitVideoView = inflate.findViewById(R.id.novel_feedportraitVideo);


            ImageView iconImg = inflate.findViewById(R.id.rlv_icon);
            TextView titleTv = inflate.findViewById(R.id.rlv_title);
            TextView descTv = inflate.findViewById(R.id.rlv_desc);
            Button bt = inflate.findViewById(R.id.rlv_bt_download);

            Glide.with(ReadActivity.this)
                    .load(targetData.getIconUrl())
                    .into(iconImg);

            titleTv.setText(targetData.getTitle());
            descTv.setText(targetData.getDesc());

            if (isDownloadAd(targetData)) {
                bt.setText("立即下载");
            } else {
                bt.setText("查看详情");
            }

            feedPortraitVideoView.setAdData((XAdNativeResponse) targetData);
            feedPortraitVideoView.setVideoMute(true);
            feedPortraitVideoView.setShowProgress(true);
            mPvPage.setFeedPortraitVideoView(feedPortraitVideoView);
            mAdView = inflate;
            mAdView.setId(ID_SMALL_VIDEO);

        }

    }

    /**
     *  请求广告展示在底通位置，与requestBaiDuUnionAd()类似，
     *   只是显示位置及出现时机不同
     */
    private void requestBaiDuUnionAd_bottom() {
        initRequestAdParams();
        BaiduNativeManager mBaiduNativeManager = new BaiduNativeManager(this, BIG_PIC_AD_PLACE_ID);
        mBaiduNativeManager.loadFeedAd(requestParameters, new BaiduNativeManager.NativeLoadListener() {
            @Override
            public void onNativeLoad(List<NativeResponse> nativeResponses) {
                Log.i(TAG, "onNativeLoad:" +
                        (nativeResponses != null ? nativeResponses.size() : null));
                //  一个广告只允许展现一次，多次展现、点击只会计入一次
                if (nativeResponses != null && nativeResponses.size() > 0) {
                    final NativeResponse response = nativeResponses.get(0);
                    RequestOptions options = new RequestOptions().bitmapTransform(new RoundedCorners(20));
                    Glide.with(ReadActivity.this)
                            .load(response.getIconUrl())
                            .apply(options)
                            .into(mIconImg);
                    mTitleTv.setText(response.getTitle());
                    mTextTv.setText(response.getDesc());

                    /**
                     *  发送广告展现日志
                     */
                    response.registerViewForInteraction(mBottomBanner, new NativeResponse.AdInteractionListener() {
                        @Override
                        public void onAdClick() {
                            Log.d(TAG, "onAdClick: ");
                        }

                        @Override
                        public void onADExposed() {
                            Log.d(TAG, "onADExposed: ");
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
                            Log.d(TAG, "onADStatusChanged: ");
                        }

                        @Override
                        public void onAdUnionClick() {

                        }
                    });
                    mInfoTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            response.handleClick(v);
                        }
                    });
                }
            }

            @Override
            public void onLoadFail(String message, String errorCode) {
                Log.w(TAG, "onLoadFail reason:" + message + "errorCode:" + errorCode);

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

            }
        });

    }

    /**
     *  从已保存的信息流大图数据的集合中
     *   选择一条数据去展示
     *
     */
    private void requestBaiDuUnionAdFrom_ExistedFeedImgData() {
        /**
         *  重新选择一个广告位来实例化BaiduNativeManager, 广告位有 信息流大图 和 小视频
         */
        if (mFeedAdImgs.size() > 0) {
            Random random = new Random();
            final NativeResponse targetAdData = mFeedAdImgs.get(random.nextInt(mFeedAdImgs.size()));

            mAdView = LayoutInflater.from(ReadActivity.this).inflate(R.layout.novel_feee_pic, null);
            mAdView.setId(ID_FEED_PIC);

            LinearLayout l = mAdView.findViewById(R.id.r);

            ImageView mainImg  =  mAdView.findViewById(R.id.r_img);
            TextView descTv = mAdView.findViewById(R.id.r_desc);
            ImageView iconImg = mAdView.findViewById(R.id.l_icon_img);
            TextView titleTv = mAdView.findViewById(R.id.l_title_tv);
            Button bt = mAdView.findViewById(R.id.l_bt);

            Glide.with(ReadActivity.this)
                    .load(targetAdData.getImageUrl())
                    .into(mainImg);

            Glide.with(ReadActivity.this)
                    .load(targetAdData.getIconUrl())
                    .into(iconImg);

            descTv.setText(targetAdData.getDesc().length()
                            > targetAdData.getTitle().length() ?
                             targetAdData.getDesc() : targetAdData.getTitle());

            titleTv.setText(targetAdData.getDesc().length()
                    < targetAdData.getTitle().length() ?
                    targetAdData.getDesc() : targetAdData.getTitle());

            if (isDownloadAd(targetAdData)) {
                bt.setText("立即下载");
            } else {
                bt.setText("查看详情");
            }

            // 将请求的广告数据传递给PageView，作为判断广告发送展现日志的一个因素
            mPvPage.setAdDataForImpression(targetAdData);

            l.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    targetAdData.handleClick(v);
                }
            });

//

        }

    }

    private boolean isDownloadAd(NativeResponse nrAd) {
        return !TextUtils.isEmpty(nrAd.getAppVersion()) && !TextUtils.isEmpty(nrAd.getPublisher())
                && !TextUtils.isEmpty(nrAd.getAppPrivacyLink()) && !TextUtils.isEmpty(nrAd.getAppPermissionLink());
    }

    private void initTopMenu() {
        if (Build.VERSION.SDK_INT >= 19) {
            mAblTopMenu.setPadding(0, ScreenUtils.getStatusBarHeight(), 0, 0);
        }
    }

    private void initBottomMenu() {
        //判断是否全屏
        if (ReadSettingManager.getInstance().isFullScreen()) {
            //还需要设置mBottomMenu的底部高度
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mLlBottomMenu.getLayoutParams();
            params.bottomMargin = ScreenUtils.getNavigationBarHeight();
            mLlBottomMenu.setLayoutParams(params);
        } else {
            //设置mBottomMenu的底部距离
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mLlBottomMenu.getLayoutParams();
            params.bottomMargin = 0;
            mLlBottomMenu.setLayoutParams(params);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d(TAG, "onWindowFocusChanged: " + mAblTopMenu.getMeasuredHeight());
    }

    private void toggleNightMode() {
        if (isNightMode) {
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_read_menu_morning);
            mTvNightMode.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
            mTitleTv.setTextColor(Color.WHITE);
            mTextTv.setTextColor(Color.WHITE);
        } else {
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_read_menu_night);
            mTvNightMode.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
            mTitleTv.setTextColor(Color.BLACK);
            mTextTv.setTextColor(Color.BLACK);
        }
    }

    private void setUpAdapter() {
        mCategoryAdapter = new CategoryAdapter();
        mLvCategory.setAdapter(mCategoryAdapter);
        mLvCategory.setFastScrollEnabled(true);
    }

    // 注册亮度观察者
    private void registerBrightObserver() {
        try {
            if (mBrightObserver != null) {
                if (!isRegistered) {
                    final ContentResolver cr = getContentResolver();
                    cr.unregisterContentObserver(mBrightObserver);
                    cr.registerContentObserver(BRIGHTNESS_MODE_URI, false, mBrightObserver);
                    cr.registerContentObserver(BRIGHTNESS_URI, false, mBrightObserver);
                    cr.registerContentObserver(BRIGHTNESS_ADJ_URI, false, mBrightObserver);
                    isRegistered = true;
                }
            }
        } catch (Throwable throwable) {
        }
    }

    //解注册
    private void unregisterBrightObserver() {
        try {
            if (mBrightObserver != null) {
                if (isRegistered) {
                    getContentResolver().unregisterContentObserver(mBrightObserver);
                    isRegistered = false;
                }
            }
        } catch (Throwable throwable) {
        }
    }

    @Override
    protected void initClick() {
        super.initClick();
        mPageLoader.setOnPageChangeListener(
                new PageLoader.OnPageChangeListener() {

                    @Override
                    public void onChapterChange(int pos) {
                        Log.d("HHHH", "onChapterChange: 章节改变" + pos);
                        mCategoryAdapter.setChapter(pos);
                        /**
                         *   在每一章节 开始的时候做请求，而不是在临近广告页出现的时候
                         *   做请求
                         */
                        tryRequestAdOnNewChapter();
                    }

                    @Override
                    public void requestChapters(List<TxtChapter> requestChapters) {
                    }

                    @Override
                    public void onCategoryFinish(List<TxtChapter> chapters) {
                        for (TxtChapter chapter : chapters) {
                            chapter.setTitle(StringUtils.convertCC(chapter.getTitle(), mPvPage.getContext()));
                        }
                        mCategoryAdapter.refreshItems(chapters);
                    }

                    @Override
                    public void onPageCountChange(int count) {
                        mSbChapterProgress.setMax(Math.max(0, count - 1));
                        mSbChapterProgress.setProgress(0);
                        // 如果处于错误状态，那么就冻结使用
                        if (mPageLoader.getPageStatus() == PageLoader.STATUS_LOADING
                                || mPageLoader.getPageStatus() == PageLoader.STATUS_ERROR) {
                            mSbChapterProgress.setEnabled(false);
                        } else {
                            mSbChapterProgress.setEnabled(true);
                        }
                    }

                    @Override
                    public void onPageChange(final int pos) {
                        mSbChapterProgress.post(new Runnable() {
                            @Override
                            public void run() {
                                mSbChapterProgress.setProgress(pos);
                            }
                        });
                    }

                    @Override
                    public void onPageBgColor(PageStyle pageStyle) {
                        if (isNightMode) {
                            mBottomBanner.setBackgroundColor(getResources().getColor(R.color.black));
                        } else {
                            int bgColor = getResources().getColor(R.color.nb_read_bg_1);
                            if (pageStyle.getBgColor() == PageStyle.BG_0.getBgColor()) {
                                bgColor = getResources().getColor(R.color.nb_read_bg_1);

                            } else if (pageStyle.getBgColor() == PageStyle.BG_1.getBgColor()) {
                                bgColor = getResources().getColor(R.color.nb_read_bg_2);

                            } else if (pageStyle.getBgColor() == PageStyle.BG_2.getBgColor()) {
                                bgColor = getResources().getColor(R.color.nb_read_bg_3);

                            } else if (pageStyle.getBgColor() == PageStyle.BG_3.getBgColor()) {
                                bgColor = getResources().getColor(R.color.nb_read_bg_4);

                            } else if (pageStyle.getBgColor() == PageStyle.BG_4.getBgColor()) {
                                bgColor = getResources().getColor(R.color.nb_read_bg_5);
                            }
                            mBottomBanner.setBackgroundColor(bgColor);
                        }
                    }
                }
        );

        mSbChapterProgress.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (mLlBottomMenu.getVisibility() == VISIBLE) {
                            //显示标题
                            mTvPageTip.setText((progress + 1) + "/" + (mSbChapterProgress.getMax() + 1));
                            mTvPageTip.setVisibility(VISIBLE);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        //进行切换
                        int pagePos = mSbChapterProgress.getProgress();
                        if (pagePos != mPageLoader.getPagePos()) {
                            mPageLoader.skipToPage(pagePos);
                        }
                        //隐藏提示
                        mTvPageTip.setVisibility(GONE);
                    }
                }
        );

        mPvPage.setTouchListener(new PageView.TouchListener() {
            @Override
            public boolean onTouch() {
                return !hideReadMenu();
            }

            @Override
            public void center() {
                toggleMenu(true);
            }

            @Override
            public void prePage() {
            }

            @Override
            public void nextPage() {
            }

            @Override
            public void cancel() {
            }
        });

        mLvCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDlSlide.closeDrawer(Gravity.START);
                mPageLoader.skipToChapter(position);
            }
        });


        mTvCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //移动到指定位置
                if (mCategoryAdapter.getCount() > 0) {
                    mLvCategory.setSelection(mPageLoader.getChapterPos());
                }
                //切换菜单
                toggleMenu(true);
                //打开侧滑动栏
                mDlSlide.openDrawer(Gravity.START);
            }
        });


        mTvSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMenu(false);
                mSettingDialog.show();
            }
        });



        mTvPreChapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPageLoader.skipPreChapter()) {
                    mCategoryAdapter.setChapter(mPageLoader.getChapterPos());
                }
            }
        });


        mTvNextChapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPageLoader.skipNextChapter()) {
                    mCategoryAdapter.setChapter(mPageLoader.getChapterPos());
                }
            }
        });


        mTvNightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNightMode) {
                    isNightMode = false;
                } else {
                    isNightMode = true;
                }
                mPageLoader.setNightMode(isNightMode);
                toggleNightMode();
            }
        });



        mSettingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                hideSystemBar();
            }
        });
    }

    /**
     * 每一章开始的时候， 请求广告
     */
    private void tryRequestAdOnNewChapter() {
        initRequestAdParams();
        initFeedVideoData();
        initSmallVideoData();
        initFeedImg();
    }

    /**
     *  请求信息流图片广告数据
     */
    private void initFeedImg() {
        BaiduNativeManager baiduNativeManager = new BaiduNativeManager(this, BIG_PIC_AD_PLACE_ID);
        baiduNativeManager.loadFeedAd(requestParameters, new BaiduNativeManager.NativeLoadListener() {
            @Override
            public void onLoadFail(String message, String errorCode) {
                Log.d(TAG, "onLoadFail: " + message);
            }

            @Override
            public void onNativeLoad(List<NativeResponse> nativeResponses) {
                //  一个广告只允许展现一次，多次展现、点击只会计入一次
                if (nativeResponses != null && nativeResponses.size() > 0) {
                    mFeedAdImgs.clear();
                    mFeedAdImgs = nativeResponses;
                }
            }

            @Override
            public void onNativeFail(NativeErrorCode errorCode) {

            }

            @Override
            public void onVideoDownloadSuccess() {

            }

            @Override
            public void onVideoDownloadFailed() {

            }

            @Override
            public void onLpClosed() {

            }
        });
    }

    /**
     *  请求小视频广告数据
     */
    private void initSmallVideoData() {
        BaiduNativeManager baiduNativeManager = new BaiduNativeManager(this, AdPlaceId_SMALL_VIDEO);
        baiduNativeManager.loadPortraitVideoAd(requestParameters, new BaiduNativeManager.PortraitVideoAdListener() {
            @Override
            public void onAdClick() {

            }

            @Override
            public void onNativeLoad(List<NativeResponse> nativeResponses) {
                if (nativeResponses != null && nativeResponses.size() > 0) {
                    mSmallVideoDatas.clear();
                    mSmallVideoDatas = nativeResponses;
                }
            }

            @Override
            public void onNativeFail(NativeErrorCode errorCode) {

            }

            @Override
            public void onVideoDownloadSuccess() {

            }

            @Override
            public void onVideoDownloadFailed() {

            }

            @Override
            public void onLpClosed() {

            }
        });
    }

    /**
     * 请求信息流视频广告数据
     */

    private void initFeedVideoData() {
        BaiduNativeManager mBaiduNativeManager = new BaiduNativeManager(this, FEED_VIDEO_AD_PLACE_ID);
        mBaiduNativeManager.loadFeedAd(requestParameters, new BaiduNativeManager.FeedAdListener() {
            @Override
            public void onNativeLoad(List<NativeResponse> nativeResponses) {
                Log.i(TAG, "onNativeLoad:" +
                        (nativeResponses != null ? nativeResponses.size() : null));
                // 一个广告只允许展现一次，多次展现、点击只会计入一次
                if (nativeResponses != null && nativeResponses.size() > 0) {
                    mFeedVideoDatas.clear();
                    mFeedVideoDatas = nativeResponses;
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

    private void initRequestAdParams() {
        requestParameters = new RequestParameters.Builder()
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
    }

    /**
     * 隐藏阅读界面的菜单显示
     *
     * @return 是否隐藏成功
     */
    private boolean hideReadMenu() {
        hideSystemBar();
        if (mAblTopMenu.getVisibility() == VISIBLE) {
            toggleMenu(true);
            return true;
        } else if (mSettingDialog.isShowing()) {
            mSettingDialog.dismiss();
            return true;
        }
        return false;
    }

    private void showSystemBar() {
        //显示
        SystemBarUtils.showUnStableStatusBar(this);
        if (isFullScreen) {
            SystemBarUtils.showUnStableNavBar(this);
        }
    }

    private void hideSystemBar() {
        //隐藏
        SystemBarUtils.hideStableStatusBar(this);
        if (isFullScreen) {
            SystemBarUtils.hideStableNavBar(this);
        }
    }

    /**
     * 切换菜单栏的可视状态
     * 默认是隐藏的
     */
    private void toggleMenu(boolean hideStatusBar) {
        initMenuAnim();

        if (mAblTopMenu.getVisibility() == View.VISIBLE) {
            //关闭
            mAblTopMenu.startAnimation(mTopOutAnim);
            mLlBottomMenu.startAnimation(mBottomOutAnim);
            mAblTopMenu.setVisibility(GONE);
            mLlBottomMenu.setVisibility(GONE);
            mTvPageTip.setVisibility(GONE);

            if (hideStatusBar) {
                hideSystemBar();
            }
        } else {
            mAblTopMenu.setVisibility(View.VISIBLE);
            mLlBottomMenu.setVisibility(View.VISIBLE);
            mAblTopMenu.startAnimation(mTopInAnim);
            mLlBottomMenu.startAnimation(mBottomInAnim);

            showSystemBar();
        }
    }

    //初始化菜单动画
    private void initMenuAnim() {
        if (mTopInAnim != null) return;

        mTopInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_in);
        mTopOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_out);
        mBottomInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_in);
        mBottomOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_out);
        //退出的速度要快
        mTopOutAnim.setDuration(200);
        mBottomOutAnim.setDuration(200);
    }

    @Override
    protected void processLogic() {
        super.processLogic();
        // 如果是已经收藏的，那么就从数据库中获取目录
        if (isCollected) {
            Disposable disposable = BookRepository.getInstance()
                    .getBookChaptersInRx(mBookId)
                    .compose(new SingleTransformer<List<BookChapterBean>, List<BookChapterBean>>() {
                        @Override
                        public SingleSource<List<BookChapterBean>> apply(Single<List<BookChapterBean>> upstream) {
                            return RxUtils.toSimpleSingle(upstream);
                        }
                    })
                    .subscribe(new BiConsumer<List<BookChapterBean>, Throwable>() {
                        @Override
                        public void accept(List<BookChapterBean> bookChapterBeen, Throwable throwable) throws Exception {
                            // 设置 CollBook
                                mPageLoader.getCollBook().setBookChapters(bookChapterBeen);
                                // 刷新章节列表
                                mPageLoader.refreshChapterList();
                                // 如果是网络小说并被标记更新的，则从网络下载目录
                                if (mCollBook.isUpdate() && !mCollBook.isLocal()) {
                                    mPresenter.loadCategory(mBookId);
                                }
                        }
                    });
            addDisposable(disposable);
        }
    }

    @Override
    public void showError() {

    }

    @Override
    public void complete() {

    }

    @Override
    public void showCategory(List<BookChapterBean> bookChapters) {
        mPageLoader.getCollBook().setBookChapters(bookChapters);
        mPageLoader.refreshChapterList();

        // 如果是目录更新的情况，那么就需要存储更新数据
        if (mCollBook.isUpdate() && isCollected) {
            BookRepository.getInstance()
                    .saveBookChaptersWithAsync(bookChapters);
        }
    }

    @Override
    public void finishChapter() {
        if (mPageLoader.getPageStatus() == PageLoader.STATUS_LOADING) {
            mHandler.sendEmptyMessage(WHAT_CHAPTER);
        }
        // 当完成章节的时候，刷新列表
        mCategoryAdapter.notifyDataSetChanged();
    }

    @Override
    public void errorChapter() {
        if (mPageLoader.getPageStatus() == PageLoader.STATUS_LOADING) {
            mPageLoader.chapterError();
        }
    }

    @Override
    public void onBackPressed() {
        if (mAblTopMenu.getVisibility() == View.VISIBLE) {
            // 非全屏下才收缩，全屏下直接退出
            if (!ReadSettingManager.getInstance().isFullScreen()) {
                toggleMenu(true);
                return;
            }
        } else if (mSettingDialog.isShowing()) {
            mSettingDialog.dismiss();
            return;
        } else if (mDlSlide.isDrawerOpen(Gravity.START)) {
            mDlSlide.closeDrawer(Gravity.START);
            return;
        }

        if (!mCollBook.isLocal() && !isCollected
                && !mCollBook.getBookChapters().isEmpty()) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("加入书架")
                    .setMessage("喜欢本书就加入书架吧")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //设置为已收藏
                            isCollected = true;
                            //设置阅读时间
                            mCollBook.setLastRead(StringUtils.
                                    dateConvert(System.currentTimeMillis(), Constant.FORMAT_BOOK_DATE));

                            BookRepository.getInstance()
                                    .saveCollBookWithAsync(mCollBook);

                            exit();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            exit();
                        }
                    })
                    .create();
            alertDialog.show();
        } else {
            exit();
        }
    }

    // 退出
    private void exit() {
        // 返回给BookDetail。
        Intent result = new Intent();
        setResult(Activity.RESULT_OK, result);
        // 退出
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerBrightObserver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWakeLock.acquire();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWakeLock.release();
        if (isCollected) {
            mPageLoader.saveRecord();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterBrightObserver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);

        mHandler.removeMessages(WHAT_CATEGORY);
        mHandler.removeMessages(WHAT_CHAPTER);

        mPageLoader.closeBook();
        mPageLoader = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isVolumeTurnPage = ReadSettingManager
                .getInstance().isVolumeTurnPage();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (isVolumeTurnPage) {
                    return mPageLoader.skipToPrePage();
                }
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (isVolumeTurnPage) {
                    return mPageLoader.skipToNextPage();
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SystemBarUtils.hideStableStatusBar(this);
        if (requestCode == REQUEST_MORE_SETTING) {
            boolean fullScreen = ReadSettingManager.getInstance().isFullScreen();
            if (isFullScreen != fullScreen) {
                isFullScreen = fullScreen;
                // 刷新BottomMenu
                initBottomMenu();
            }

            // 设置显示状态
            if (isFullScreen) {
                SystemBarUtils.hideStableNavBar(this);
            } else {
                SystemBarUtils.showStableNavBar(this);
            }
        }
    }
}
