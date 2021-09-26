package com.baidu.mobads.demo.main.tools;

import java.lang.reflect.Method;

import android.content.Context;
import android.graphics.Rect;
import android.os.PowerManager;
import android.view.View;

public class VisibilityChecker {
    // 1：Invisible，3：outsideWindow，4：screenOff，6：tooSmall
    public static final int SHOW_STATE_SCREEN_OFF = 4;
    public static final int SHOW_STATE_NOT_VISIBLE = 1;
    public static final int SHOW_STATE_NOT_ENOUGH_BIG = 6;
    public static final int SHOW_STATE_LOWER_THAN_MIN_SHOWPERCENT = 3;
    public static final int SHOW_STATE_SHOW = 0;

    /**
     * Whether the view is at least certain % visible
     */
    public static boolean isVisible(final View view, final int minPercentageViewed) {
        if (view == null || view.getVisibility() != View.VISIBLE
                || view.getParent() == null) {
            return false;
        }
        Rect mClipRect = new Rect();

        if (!view.getGlobalVisibleRect(mClipRect)) {
            // Not visible
            return false;
        }

        final long visibleViewArea = (long) mClipRect.height()
                * mClipRect.width();
        final long totalViewArea = (long) view.getHeight() * view.getWidth();

        if (totalViewArea <= 0) {
            return false;
        }

        return 100 * visibleViewArea >= minPercentageViewed * totalViewArea;
    }
    /**
     * Returns whether the screen is currently on. Only indicates whether the
     * screen is on. The screen could be either bright or dim.
     * 
     * Returns: whether the screen is on (bright or dim).
     * 
     * @throws Exception
     */
    public static boolean isScreenOn(Context context) throws Exception {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        Method isScreenOnMethod = pm.getClass().getMethod("isScreenOn");
        boolean isScreenOn = (Boolean) isScreenOnMethod.invoke(pm);
        return isScreenOn;
    }

    /**
     * Returns the visibility of this view and all of its ancestors and check
     * whether view is on the tree
     * 
     * @return True if this view and all of its ancestors are VISIBLE
     */
    protected static boolean isAdViewShown(View view) {
        if (view != null && view.isShown()) {
            return true;
        } else {
            return false;
        }
    }

    protected static boolean isNotEnoughBig(View view) {
        // 目前：宽高都要大于15 (担心文字链广告的情况)
        return view.getWidth() > 15 && view.getHeight() > 15;
    }

    public static int getViewState(View view) throws Exception {
        int showState = SHOW_STATE_SHOW;
        if (!VisibilityChecker.isScreenOn(view.getContext())) {
            showState = SHOW_STATE_SCREEN_OFF;
        } else if (!VisibilityChecker.isAdViewShown(view)) {
            showState = SHOW_STATE_NOT_VISIBLE;
        } else if (!VisibilityChecker.isNotEnoughBig(view)) {
            showState = SHOW_STATE_NOT_ENOUGH_BIG;
        } else if (!VisibilityChecker.isVisible(view, 50)) {
            showState = SHOW_STATE_LOWER_THAN_MIN_SHOWPERCENT;
        }
        return showState;
    }
}
