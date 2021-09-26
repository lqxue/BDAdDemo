package com.baidu.mobads.demo.main;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.WebView;

import com.bun.miitmdid.interfaces.IIdentifierListener;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.interfaces.IdSupplier;

public class MobadsApplication extends Application {
    private static Context sInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        //集成信通院MSA http://msa-alliance.cn/
        sInstance = this;
        try {
            // 初始化MSA SDK
            // JLibrary.InitEntry(this);
            //获取OAID
            int sdkState = MdidSdkHelper.InitSdk(getApplicationContext(), true, new IIdentifierListener() {
                @Override
                public void OnSupport(boolean b, IdSupplier idSupplier) {
                    if (idSupplier != null) {
                        String oaid = idSupplier.getOAID();
                        Log.e("oaid","oaid=" + oaid);
                    }
                }
            });
            Log.e("mdidsdk","初始化" + sdkState);
        } catch (Throwable tr) {
            tr.printStackTrace();
        }
        // 重要：适配安卓P，如果WebView使用多进程，添加如下代码
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = getProcessName(this);
            // 填入应用自己的包名
            if (!"com.baidu.mobads.demo.main".equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }
    }

    private String getProcessName(Context context) {
        if (context == null) return null;
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == android.os.Process.myPid()) {
                return processInfo.processName;
            }
        }
        return null;
    }
    public static Context getContext(){
        return sInstance;
    }
}
