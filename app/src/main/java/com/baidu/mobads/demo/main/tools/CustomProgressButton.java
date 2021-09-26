package com.baidu.mobads.demo.main.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.baidu.mobad.feeds.NativeResponse;


public class CustomProgressButton extends View {

    private static final String TAG = CustomProgressButton.class.getSimpleName();
    private int mProgress = -1;
    private Paint mPaint;
    /** 画边框的画笔 */
    private Paint strokePaint;
    /** 边框画笔的颜色,这里默认为蓝色 */
    private int stokeColor = Color.parseColor("#3388FF");
    /** 边框画笔的宽 */
    private int stokeWidth = 3;
    /** 是否有边框 */
    private boolean isStoke = false;
    private String mText;
    private float mTextSize = 10;
    private int mTextColor = Color.WHITE;
    private Typeface mTypeFace;
    private int mForegroundColor;
    private int mBackgroundColor;
    private int mMaxProgress = 100;
    private float mCorner = 12.0F; // 圆角的弧度
    private String mPackageName = "";
    private PorterDuffXfermode mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    /** 响应 */
    private NativeResponse nrAd;

    public CustomProgressButton(Context context) {
        super(context);
        initPaint();
    }

    public CustomProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public CustomProgressButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    public void setMax(int max) {
        if (max > 0) {
            mMaxProgress = max;
        }
    }

    // 必须调用以初始化数据
    public void initWithResponse(NativeResponse nrAd) {
        this.nrAd = nrAd;
        updateStatus(nrAd);
    }

    public void setForegroundColor(int color) {
        mForegroundColor = color;
    }

    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
    }

    public void setCornerRadius(int radius) {
        mCorner = radius;
    }

    public void setPackageName(String pk) {
        mPackageName = pk;
    }

    public void setText(String text) {
        mText = text;
    }

    public void setStroke(boolean isStoke) {
        this.isStoke = isStoke;
    }

    public void setTextColor(int color) {
        mTextColor = color;
    }

    public void setTextSize(int size) {
        mTextSize = size;
    }

    public void setTypeFace(Typeface typeFace) {
        mTypeFace = typeFace;
    }

    public void setProgress(int progress) {
        if (progress > mMaxProgress) {
            return;
        }
        mProgress = progress;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mProgress < 0 || mProgress >= mMaxProgress) {
            // 不带进度的情况
            mPaint.setColor(this.mForegroundColor);
            myDrawRoundRect(canvas, 0, 0, getWidth(), getHeight(), mCorner, mPaint);
            // 按同样的大小绘制一个边框
            if (isStoke) {
                // 如果设置了就会有边框
                myDrawRoundRect(canvas, 1, 1, getWidth() - 1,
                        getHeight() - 1, mCorner, strokePaint);
            }
            drawTextInCenter(canvas, mText, mPaint, mTextColor, mTextSize, mTypeFace);
        } else {
            // 绘制背景
            drawProgressBackground(canvas);
            // 绘制文字
            if (!TextUtils.isEmpty(mText)) {
                drawProgressText(canvas);
            }
        }
    }

    private void drawProgressBackground(Canvas canvas) {
        Bitmap bgBuffer = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas bgCanvas = new Canvas(bgBuffer);
        // 绘制底色bitmap
        mPaint.setColor(this.mBackgroundColor);
        myDrawRoundRect(bgCanvas, 0, 0, getWidth(), getHeight(), mCorner, mPaint);
        // 绘制涂层
        drawBitmapWithXfermode(bgCanvas, mPaint, mForegroundColor);
        // 绘制背景至canvas
        canvas.drawBitmap(bgBuffer, 0, 0, null);
        // 回收数据
        if (!bgBuffer.isRecycled()) {
            bgBuffer.recycle();
        }

    }

    private void drawProgressText(Canvas canvas) {
        Bitmap textBuffer = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas textCanvas = new Canvas(textBuffer);
        // 绘制文字bitmap
        drawTextInCenter(textCanvas, mText, mPaint, mForegroundColor, mTextSize, mTypeFace);
        // 绘制涂层
        drawBitmapWithXfermode(textCanvas, mPaint, mTextColor);
        // 绘制文字至canvas
        canvas.drawBitmap(textBuffer, 0, 0, null);
        // 回收数据
        if (!textBuffer.isRecycled()) {
            textBuffer.recycle();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 拦截事件
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                // 获取下载状态
                int status = nrAd.getDownloadStatus();
                if (0 < status && status < 101) {
                    // 暂停下载
                    nrAd.pauseAppDownload();
                    updateStatus(nrAd);
                } else {
                    // 调用performClick以免与OnClick冲突
                    performClick();
                    // 处理点击，恢复下载
                    nrAd.handleClick(this);
                    updateStatus(nrAd);
                    return false;
                }
            default:
        }
        return true;
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(3);
        // 添加画笔绘制边框
        strokePaint = new Paint();
        strokePaint.setAntiAlias(true);
        strokePaint.setColor(stokeColor);
        strokePaint.setStrokeWidth(stokeWidth);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);

    }

    private void drawTextInCenter(Canvas canvas, String text, Paint paint,
                                  int color, float size, Typeface typeface) {
        if (canvas != null && paint != null && !TextUtils.isEmpty(text)) {
            paint.setColor(color);
            paint.setTextSize(size);
            if (typeface != null) {
                paint.setTypeface(typeface);
            }
            Paint.FontMetrics fm = paint.getFontMetrics();
            float textCenterVerticalBaselineY = (float) (getHeight() / 2) - fm.descent + (fm.descent - fm.ascent) / 2;
            canvas.drawText(text, (getMeasuredWidth() - paint.measureText(text)) / 2,
                    textCenterVerticalBaselineY, paint);
        }
    }


    private void drawBitmapWithXfermode(Canvas bitmapCanvas, Paint paint, int color) {
        // 设置混合模式
        paint.setXfermode(mPorterDuffXfermode);
        paint.setColor(color);
        // 绘制涂层
        myDrawRoundRect(bitmapCanvas, 0, 0, getWidth() * mProgress / mMaxProgress, getHeight(), 0, paint);
        // 清除混合模式
        paint.setXfermode(null);
    }

    private void myDrawRoundRect(Canvas canvas, int left, int top, int right, int bottom,
                                 float corner, Paint paint) {
        paint.setAntiAlias(true);
        if (Build.VERSION.SDK_INT >= 21) {
            canvas.drawRoundRect(left, top, right, bottom, corner, corner, paint);
        } else {
            RectF rectF = new RectF(left, top, right, bottom);
            canvas.drawRoundRect(rectF, corner, corner, paint);
        }
    }

    public void updateStatus(NativeResponse nrAd) {
        int status = nrAd.getDownloadStatus();
        if (status < 0) {
            mProgress = mMaxProgress;
            if (nrAd.isDownloadApp()) {
                mText = "立即下载";
            } else {
                mText = "去看看";
            }
        } else if (status < 101) {
            mText = status + "%";
            mProgress = status;
        } else if (status == 101) {
            mProgress = mMaxProgress;
            if (nrAd.isDownloadApp()) {
                mText = "点击安装";
            } else {
                mText = "去看看";
            }
        } else if (status == 102) {
            mText = "继续下载";
        } else if (status == 104) {
            mText = "重新下载";
            mProgress = mMaxProgress;
        }
        postInvalidate();
    }
}
