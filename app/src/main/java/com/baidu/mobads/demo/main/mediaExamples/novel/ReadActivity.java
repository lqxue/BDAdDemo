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
 *  ??????????????????????????????????????????????????????
 *  ????????????????????? ??? ??????????????????????????????????????????????????? {@link ReadActivity#tryRequestAdOnNewChapter()},???
 *  ??????????????????????????????????????????????????????????????????????????????????????????????????????
 *  ????????????????????????????????????????????????????????????????????????????????????????????????????????????
 *   {@link ReadActivity#requestBaiDuFeedVideoFrom_ExistedFeedVideoDatas()};
 *
 *   note:???????????????/??????????????????????????????????????????????????????????????????{@link PageView#setPageMode(PageMode pageMode)}
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
     *  ????????????????????????
     */
    private static final String BIG_PIC_AD_PLACE_ID = "2058628";        // ??????+ICON+??????
    private View mAdView; // ???????????????View

    /**
     *  ??????????????????????????????
     */
    private static final String AdPlaceId_SMALL_VIDEO = "6164562";

    /**
     *  ??? ??????????????? ????????????
     */
    private static final String FEED_VIDEO_AD_PLACE_ID = "2362913";

    /**
     *  mAdView???id?????????????????????????????????????????????
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

    // ?????????????????????
    private RequestParameters requestParameters;
    // ????????????????????????????????????????????????????????????
    private List<NativeResponse> mFeedVideoDatas = new ArrayList<>();
    // ??????????????????????????????????????????????????????
    private List<NativeResponse> mSmallVideoDatas = new ArrayList<>();
    // ??????????????????????????????????????????????????????
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
    // ??????????????????????????????????????????
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                int level = intent.getIntExtra("level", 0);
                mPageLoader.updateBattery(level);
            }
            // ?????????????????????
            else if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                mPageLoader.updateTime();
            }
        }
    };
    // ??????????????????
    // ???????????????????????? Broadcast ?????????????????? ContentProvider ?????????????????????????????? Observer ????????? ContentProvider ??????????????????
    private ContentObserver mBrightObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange);
            // ????????????????????????????????????????????????????????????
            if (selfChange || !mSettingDialog.isBrightFollowSystem()) return;
            // ?????????????????????????????????????????? Activity ??????
            if (BRIGHTNESS_MODE_URI.equals(uri)) {
                Log.d(TAG, "??????????????????");
            } else if (BRIGHTNESS_URI.equals(uri) && !BrightnessUtils.isAutoBrightness(ReadActivity.this)) {
                Log.d(TAG, "??????????????????????????? ?????????");
                BrightnessUtils.setBrightness(ReadActivity.this, BrightnessUtils.getScreenBrightness(ReadActivity.this));
            } else if (BRIGHTNESS_ADJ_URI.equals(uri) && BrightnessUtils.isAutoBrightness(ReadActivity.this)) {
                Log.d(TAG, "??????????????????????????? ?????????");
                BrightnessUtils.setDefaultBrightness(ReadActivity.this);
            } else {
                Log.d(TAG, "???????????? ??????");
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
        //????????????
        toolbar.setTitle(mCollBook.getTitle());
        //????????????StatusBar
        SystemBarUtils.transparentStatusBar(this);
    }

    @Override
    protected void initWidget() {
        super.initWidget();

        //?????????????????????
        mPageLoader = mPvPage.getPageLoader(mCollBook);
        //??????????????????DrawerLayout
        mDlSlide.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        //??????????????????????????????????????????
        mDlSlide.setFocusableInTouchMode(false);
        mSettingDialog = new ReadSettingDialog(this, mPageLoader);

        setUpAdapter();

        //???????????????????????????
        toggleNightMode();

        //????????????
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(mReceiver, intentFilter);

        //????????????Activity???Brightness
        if (ReadSettingManager.getInstance().isBrightnessAuto()) {
            BrightnessUtils.setDefaultBrightness(this);
        } else {
            BrightnessUtils.setBrightness(this, ReadSettingManager.getInstance().getBrightness());
        }

        //????????????????????????
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "myapp:keep bright");

        //??????StatusBar
        mPvPage.post(new Runnable() {
            @Override
            public void run() {
                hideSystemBar();
            }
        });
        //?????????TopMenu
        initTopMenu();
        //?????????BottomMenu
        initBottomMenu();


        /**
         * ####################################################################################
         *                                                                                   ##
         *                                                                                   ##
         * Step 1. ??????BaiduNative??????????????????????????? ?????????context????????????ID                 ##
         * ???????????????adPlaceId???????????????????????????ID                                           ##
         * ?????????????????????????????????????????????id??????????????????????????????????????????????????????              ##
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
                    // ??????????????????Union????????????????????????
                    requestBaiDuUnionAdFrom_ExistedFeedImgData();
                } else if (anInt == 1) {
                    // ??????????????????Union??????????????????
                    requestBaiDuUnionVideoFrom_ExistedSmallVideoDatas();
                }
                else {
                    // ??????????????????Union????????????????????????
                    requestBaiDuFeedVideoFrom_ExistedFeedVideoDatas();
                    // ?????????????????????
                    requestBaiDuUnionAd_bottom();
                }
                return mAdView;
            }

            @Override
            public View getCoverPageView() {
                return coverPageView;
            }

            /**
             *  ???????????????????????????????????????????????????????????????
             *  ??????????????????????????? ??????{@link PageView#computeScroll()}  ???
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
                         * ???????????????????????????
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
                        Log.d(TAG, "onRenderingStart: ??????????????? ");
                    }

                    @Override
                    public void onPause() {

                    }

                    @Override
                    public void onResume() {

                    }

                    @Override
                    public void onCompletion() {
                        Log.d(TAG, "onCompletion: ???????????????");

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
     * ??????????????????????????????????????????????????????????????????
     */
    private void requestBaiDuFeedVideoFrom_ExistedFeedVideoDatas() {
        if (mFeedVideoDatas.size() > 0) {
            Random random = new Random();
            final NativeResponse targetData = mFeedVideoDatas.get(random.nextInt(mFeedVideoDatas.size()));
            /**
             * ???????????????????????????
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
             * ????????????????????????
             */

            video.setNativeItem(targetData);

            if (isDownloadAd(targetData)) {
               bt.setText("????????????");
            } else {
                bt.setText("????????????");
            }
            video.setVideoMute(true);
            mPvPage.setFeedVideoViewData(video);
        }
    }

    /**
     *  ???????????????????????????????????????????????????
     */
    private void requestBaiDuUnionVideoFrom_ExistedSmallVideoDatas() {
        if (mSmallVideoDatas.size() > 0) {
            Random random = new Random();
            NativeResponse targetData = mSmallVideoDatas.get(random.nextInt(mSmallVideoDatas.size()));

            View inflate = LayoutInflater.from(ReadActivity.this).inflate(R.layout.novel_vertical_video, null);
            /**
             *  note:???????????????????????????????????????????????????????????????padding??????
             *   ?????????????????????padiding?????????????????????1/6??????????????????????????????????????????
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
                 *  ????????????????????????????????????????????????????????????
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
                bt.setText("????????????");
            } else {
                bt.setText("????????????");
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
     *  ???????????????????????????????????????requestBaiDuUnionAd()?????????
     *   ???????????????????????????????????????
     */
    private void requestBaiDuUnionAd_bottom() {
        initRequestAdParams();
        BaiduNativeManager mBaiduNativeManager = new BaiduNativeManager(this, BIG_PIC_AD_PLACE_ID);
        mBaiduNativeManager.loadFeedAd(requestParameters, new BaiduNativeManager.NativeLoadListener() {
            @Override
            public void onNativeLoad(List<NativeResponse> nativeResponses) {
                Log.i(TAG, "onNativeLoad:" +
                        (nativeResponses != null ? nativeResponses.size() : null));
                //  ???????????????????????????????????????????????????????????????????????????
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
                     *  ????????????????????????
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
                         * ???????????????????????????
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
                // ????????????onLoadFail??????????????????????????????????????????
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
     *  ????????????????????????????????????????????????
     *   ???????????????????????????
     *
     */
    private void requestBaiDuUnionAdFrom_ExistedFeedImgData() {
        /**
         *  ???????????????????????????????????????BaiduNativeManager, ???????????? ??????????????? ??? ?????????
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
                bt.setText("????????????");
            } else {
                bt.setText("????????????");
            }

            // ?????????????????????????????????PageView??????????????????????????????????????????????????????
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
        //??????????????????
        if (ReadSettingManager.getInstance().isFullScreen()) {
            //???????????????mBottomMenu???????????????
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mLlBottomMenu.getLayoutParams();
            params.bottomMargin = ScreenUtils.getNavigationBarHeight();
            mLlBottomMenu.setLayoutParams(params);
        } else {
            //??????mBottomMenu???????????????
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

    // ?????????????????????
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

    //?????????
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
                        Log.d("HHHH", "onChapterChange: ????????????" + pos);
                        mCategoryAdapter.setChapter(pos);
                        /**
                         *   ??????????????? ?????????????????????????????????????????????????????????????????????
                         *   ?????????
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
                        // ????????????????????????????????????????????????
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
                            //????????????
                            mTvPageTip.setText((progress + 1) + "/" + (mSbChapterProgress.getMax() + 1));
                            mTvPageTip.setVisibility(VISIBLE);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        //????????????
                        int pagePos = mSbChapterProgress.getProgress();
                        if (pagePos != mPageLoader.getPagePos()) {
                            mPageLoader.skipToPage(pagePos);
                        }
                        //????????????
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
                //?????????????????????
                if (mCategoryAdapter.getCount() > 0) {
                    mLvCategory.setSelection(mPageLoader.getChapterPos());
                }
                //????????????
                toggleMenu(true);
                //??????????????????
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
     * ??????????????????????????? ????????????
     */
    private void tryRequestAdOnNewChapter() {
        initRequestAdParams();
        initFeedVideoData();
        initSmallVideoData();
        initFeedImg();
    }

    /**
     *  ?????????????????????????????????
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
                //  ???????????????????????????????????????????????????????????????????????????
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
     *  ???????????????????????????
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
     * ?????????????????????????????????
     */

    private void initFeedVideoData() {
        BaiduNativeManager mBaiduNativeManager = new BaiduNativeManager(this, FEED_VIDEO_AD_PLACE_ID);
        mBaiduNativeManager.loadFeedAd(requestParameters, new BaiduNativeManager.FeedAdListener() {
            @Override
            public void onNativeLoad(List<NativeResponse> nativeResponses) {
                Log.i(TAG, "onNativeLoad:" +
                        (nativeResponses != null ? nativeResponses.size() : null));
                // ???????????????????????????????????????????????????????????????????????????
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
                // ???????????????????????????????????????0-unknown???1-male???2-female
                .addExtra(ArticleInfo.USER_SEX, "1")
                // ??????????????????????????????ID???????????????ID????????????ID???'/??????'
                .addExtra(ArticleInfo.FAVORITE_BOOK, "?????????????????????1/?????????????????????2/?????????????????????3")
                // ???????????????????????????????????????
                .addExtra(ArticleInfo.PAGE_TITLE, "????????????")
                // ?????????????????????????????????ID
                .addExtra(ArticleInfo.PAGE_ID, "10930484090")
                // ????????????????????????????????????????????????????????????'/'??????
                .addExtra(ArticleInfo.CONTENT_CATEGORY, "????????????/????????????")
                // ????????????????????????????????????????????????10????????????????????????'/??????'
                .addExtra(ArticleInfo.CONTENT_LABEL, "??????1/??????2/??????3")
                .build();
    }

    /**
     * ?????????????????????????????????
     *
     * @return ??????????????????
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
        //??????
        SystemBarUtils.showUnStableStatusBar(this);
        if (isFullScreen) {
            SystemBarUtils.showUnStableNavBar(this);
        }
    }

    private void hideSystemBar() {
        //??????
        SystemBarUtils.hideStableStatusBar(this);
        if (isFullScreen) {
            SystemBarUtils.hideStableNavBar(this);
        }
    }

    /**
     * ??????????????????????????????
     * ??????????????????
     */
    private void toggleMenu(boolean hideStatusBar) {
        initMenuAnim();

        if (mAblTopMenu.getVisibility() == View.VISIBLE) {
            //??????
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

    //?????????????????????
    private void initMenuAnim() {
        if (mTopInAnim != null) return;

        mTopInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_in);
        mTopOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_out);
        mBottomInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_in);
        mBottomOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_out);
        //?????????????????????
        mTopOutAnim.setDuration(200);
        mBottomOutAnim.setDuration(200);
    }

    @Override
    protected void processLogic() {
        super.processLogic();
        // ???????????????????????????????????????????????????????????????
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
                            // ?????? CollBook
                                mPageLoader.getCollBook().setBookChapters(bookChapterBeen);
                                // ??????????????????
                                mPageLoader.refreshChapterList();
                                // ?????????????????????????????????????????????????????????????????????
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

        // ??????????????????????????????????????????????????????????????????
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
        // ???????????????????????????????????????
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
            // ?????????????????????????????????????????????
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
                    .setTitle("????????????")
                    .setMessage("??????????????????????????????")
                    .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //??????????????????
                            isCollected = true;
                            //??????????????????
                            mCollBook.setLastRead(StringUtils.
                                    dateConvert(System.currentTimeMillis(), Constant.FORMAT_BOOK_DATE));

                            BookRepository.getInstance()
                                    .saveCollBookWithAsync(mCollBook);

                            exit();
                        }
                    })
                    .setNegativeButton("??????", new DialogInterface.OnClickListener() {
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

    // ??????
    private void exit() {
        // ?????????BookDetail???
        Intent result = new Intent();
        setResult(Activity.RESULT_OK, result);
        // ??????
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
                // ??????BottomMenu
                initBottomMenu();
            }

            // ??????????????????
            if (isFullScreen) {
                SystemBarUtils.hideStableNavBar(this);
            } else {
                SystemBarUtils.showStableNavBar(this);
            }
        }
    }
}
