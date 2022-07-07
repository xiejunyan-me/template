package com.jeckonly.core.util;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.Application;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AppUtil {

    private static final Object LOCK = new Object();

    private static SoftReference<List<PackageInfo>> mAppInfoList = null;

    /**
     *  获取 dayInterval天之前————现在的这个时间段内应用的统计信息
     * @param context
     * @param dayInterval 时间天数时间段
     * @return 当版本小于lollipop，返回列表为空。大于或等于lollipop之后的才有数据。当设备大于Android R时，在设备未解锁状态调用该方法，返回null
     */
    public static List<UsageStats> getUsageStats(Context context, int dayInterval) {
        List<UsageStats> usageStats = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            Calendar calendar = Calendar.getInstance();
            long endTime = calendar.getTimeInMillis();
            calendar.add(Calendar.DAY_OF_WEEK, -dayInterval);
            long startTime = calendar.getTimeInMillis();
            usageStats = usm.queryUsageStats(UsageStatsManager.INTERVAL_WEEKLY, startTime, endTime);
        }
        return usageStats;
    }

    /**
     * 获取所有已安装的APP的信息，以一个List表示
     *
     * 如果target Android 11以上，这个方法只会返回对当前应用可见的包的PackageInfo,See <a href="https://developer.android.com/reference/android/content/pm/PackageManager">PackageManager</a>
     * @return
     */
    public static List<PackageInfo> getAllAppInfos(Context context) {

        PackageManager pm = context.getPackageManager();
        // Return a List of all packages that are installed on the device.
        return pm.getInstalledPackages(0);
    }

    /**
     *  通 getAllAppInfos， 但会使用缓存
     *
     * @return
     */
    public static List<PackageInfo> getAllAppInfosByCache(Context context) {
        List<PackageInfo> packages = null;
        if(mAppInfoList != null){
            packages = mAppInfoList.get();
        }
        if(packages != null){
            return packages;
        }
        packages = getAllAppInfos(context);
        mAppInfoList = new SoftReference<>(packages);
        return packages;
    }

    /**
     *
     * @param pi
     * @return 返回true表示是系统APP
     */
    public static boolean isSystemApp(PackageInfo pi) {
        if(pi.applicationInfo == null) return true;
        return (pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;
    }

    /**
     *
     * @param context
     * @param packageName 包名
     * @return 返回true表示是系统APP，不是系统APP和包名不存在都返回false
     */
    public static boolean isSystemApp(Context context, String packageName) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            if(info.applicationInfo == null) return true;
            return (info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取本应用名称 例如youtube
     * @return 返回null表示没找到
     */
    public static String getAppName(Context context) {
        String appName = null;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            appName = context.getString(ai.labelRes);
        } catch (PackageManager.NameNotFoundException e) {
        }
        return appName;
    }

    /**
     * 通过包名获取任意一个应用名称，例如youtube
     * @param context
     * @param packageName
     * @return
     */
    public String getAppNameByPackageName(Context context, String packageName) {
        try {
            PackageInfo info =context.getPackageManager().getPackageInfo(packageName, 0);
            return (String) context.getPackageManager().getApplicationLabel(info.applicationInfo);
        } catch (Throwable ignored) {

        }
        return "";
    }

    /**
     * 获取首次安装时间
     *
     * @return
     */
    public static long getFirstInstallTime(Context context){
        long result = 1;

        try {
            result =context.getPackageManager().getPackageInfo(context.getPackageName(), 0).firstInstallTime;
        } catch (Exception ignored) {

        }

        return result;
    }

    public static String getCurrentProcessName(Context context) {
        String processName = null;
        // firstly, get form ActivityThread
        try {
            Object activityThread = getCurrentActivityThread(context);
            if (activityThread != null) {
                Method getProcessName = activityThread
                        .getClass().getDeclaredMethod("getProcessName");
                getProcessName.setAccessible(true);
                processName = (String) getProcessName.invoke(activityThread);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        // secondly, get from cmdline file
        if (TextUtils.isEmpty(processName)) {
            int pid = android.os.Process.myPid();
            BufferedReader cmdlineReader = null;
            try {
                cmdlineReader = new BufferedReader(new InputStreamReader(
                        new FileInputStream("/proc/" + pid + "/cmdline"),
                        "iso-8859-1"));
                int c;
                StringBuilder tempName = new StringBuilder();
                while ((c = cmdlineReader.read()) > 0) {
                    tempName.append((char) c);
                }
                processName = tempName.toString().trim();
            } catch (Throwable t2) {
                t2.printStackTrace();
            } finally {
                if (cmdlineReader != null) {
                    try {
                        cmdlineReader.close();
                    } catch (IOException ignore) {}
                }
            }
        }
        // lastly, get from ams
        if (TextUtils.isEmpty(processName)) {
            try {
                int pid = android.os.Process.myPid();
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo>
                        infos = am.getRunningAppProcesses();
                if (infos != null && infos.size() > 0) {
                    for (ActivityManager.RunningAppProcessInfo p : infos) {
                        if (p.pid == pid) {
                            processName = p.processName;
                            break;
                        }
                    }
                }
            } catch (Throwable t3) {
                t3.printStackTrace();
            }
        }
        return processName == null ? "" : processName.trim();
    }

    public static Object getCurrentActivityThread(Context context) {
        Object currentActivityThread = null;
        try {
            Class activityThread = Class.forName("android.app.ActivityThread");
            Method m = activityThread.getMethod("currentActivityThread");
            m.setAccessible(true);
            currentActivityThread = m.invoke(null);
            if (currentActivityThread == null && context != null) {
                // In older versions of Android (prior to frameworks/base 66a017b63461a22842)
                // the currentActivityThread was built on thread locals, so we'll need to try
                // even harder
                Application app = (Application) context.getApplicationContext();
                Field mLoadedApk = app.getClass().getField("mLoadedApk");
                mLoadedApk.setAccessible(true);
                Object apk = mLoadedApk.get(app);
                Field mActivityThreadField = apk.getClass().getDeclaredField("mActivityThread");
                mActivityThreadField.setAccessible(true);
                currentActivityThread = mActivityThreadField.get(apk);
            }
        } catch (Exception ignore) {}
        return currentActivityThread;
    }

    public static String getAndroidId (Context context) {
        return Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * 获取versionName
     */
    public static String getAppVersionName(Context context){
        String versionName = "";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi =  pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 获取versionCode
     */
    public static long getAppVersionCode(Context context) {
        long versionCode = 0;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi =  pm.getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = pi.getLongVersionCode();
            }else {
                versionCode = pi.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }


    /**
     * 从apk文件获取apk信息
     *
     * @param context
     * @param apkPath
     * @return
     */
    public static ApplicationInfo getAppInfoFromApk(Context context,String apkPath){
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath,
                PackageManager.GET_ACTIVITIES);
        if(info != null){
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            return appInfo;
        }
        return null;
    }

    public Drawable getAppIconByPackage(Context context, String packageName) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            return info.applicationInfo.loadIcon(context.getPackageManager());
        } catch (Throwable ignored) {

        }
        return null;
    }

    /**
     * 方法描述：判断某一应用是否正在运行
     * Created by cafeting on 2017/2/4.
     * @param context     上下文
     * @param packageName 应用的包名
     * @return true 表示正在运行，false 表示没有运行
     */
    public static boolean isAppRunning(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        if (list.size() <= 0) {
            return false;
        }
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.baseActivity.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取所有应用，包含权限信息
     * Android 11以后失效
     * @return
     */
    public static List<PackageInfo> getPermissionAppListInfos(Context context) {
        PackageManager pm = context.getPackageManager();
        // Return a List of all packages that are installed on the device.
        List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS);
        return packages;
    }

    /**
     * 检查android.permission.PACKAGE_USAGE_STATS是否允许。即使用情况访问权限是否允许。
     * 若没有允许，可参考以下代码进行设置
     * <pre>
     * Intent intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
     * startActivity(intent)}
     * //startActivityForResult(intent, USAGE_STATS_REQUEST_CODE)
     * </pre>
     * @return
     */
    public static boolean checkUsagesStatsPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            mode = appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), context.getPackageName());
            boolean granted = mode == AppOpsManager.MODE_ALLOWED;
            return granted;
        }
        return true;
    }

    /**
     * 重启（目前应用在更改语言）
     * @param activity
     */
    public static void restart(Activity activity) {
        Intent intent = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        System.exit(0);
    }

    /**
     * 根目录
     *
     * @return
     */
    public static String getRootPath(){
        try {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        } catch (Exception exception) {
            exception.printStackTrace();
            return "";
        }
    }

    /**
     * 前往谷歌应用市场
     */
    public static void toMarketReview(@NonNull Context context, String pkgName) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + pkgName));
            intent.setPackage("com.android.vending");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                // 没有google play
                // 通过浏览器打开评分
                intent.setPackage(null);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + pkgName));
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                } else {
                    // 没有浏览器.
                }

            }
        } catch (Exception e) {
        }
    }
}
