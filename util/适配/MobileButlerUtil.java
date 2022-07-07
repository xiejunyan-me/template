package com.jeckonly.core.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.lang.reflect.Method;
import java.util.List;

public class MobileButlerUtil {

    // 判断机型


    public static boolean isHuawei() {
        if (Build.BRAND == null) {
            return false;
        } else {
            return Build.BRAND.equalsIgnoreCase("huawei") || Build.BRAND.equalsIgnoreCase("honor");
        }
    }

    public static boolean isXiaomi() {
        return Build.BRAND != null && (Build.BRAND.equalsIgnoreCase("xiaomi") ||
                Build.BRAND.equalsIgnoreCase("redmi"));
    }

    public static boolean isOPPO() {
        return Build.BRAND != null && Build.BRAND.equalsIgnoreCase("oppo");
    }

    public static boolean isVIVO() {
        return Build.BRAND != null && Build.BRAND.equalsIgnoreCase("vivo");
    }

    public static boolean isMeizu() {
        return Build.BRAND != null && Build.BRAND.equalsIgnoreCase("meizu");
    }

    public static boolean isSamsung() {
        return Build.BRAND != null && Build.BRAND.equalsIgnoreCase("samsung");
    }

    public static boolean isLeTV() {
        return Build.BRAND != null && Build.BRAND.equalsIgnoreCase("letv");
    }

    public static boolean isSmartisan() {
        return Build.BRAND != null && Build.BRAND.equalsIgnoreCase("smartisan");
    }

    public static boolean isGoogle() {
        return Build.BRAND != null && Build.BRAND.equalsIgnoreCase("pixel ");
    }


    // 前往自启动设置界面


    public static void goHuaweiAutoStartSetting(Context context) {
        try {
            showActivity(context,"com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");
        } catch (Exception e) {
            showActivity(context,"com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.bootstart.BootStartActivity");
        }
    }

    public static void goXiaomiAutoStartSetting(Context context) {
        try {
            showActivity(context,"com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity");
        } catch (Exception e) {
            Intent intent = new Intent();
            intent.setAction("miui.intent.action.OP_AUTO_START");
            intent.addCategory("android.intent.category.DEFAULT");
            if (!(context instanceof Activity)) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public static void goOPPOAutoStartSetting(Context context) {
        try {
            showActivity(context, "com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity");
        } catch (Exception e) {
            try {
                showActivity(context, "com.coloros.phonemanager");
            } catch (Exception e1) {
                try {
                    showActivity(context, "com.oppo.safe");
                } catch (Exception e2) {
                    try {
                        showActivity(context, "com.coloros.oppoguardelf");
                    } catch (Exception e3) {
                        showActivity(context, "com.coloros.safecenter");
                    }
                }
            }
        }
    }

    public static void goVIVOAutoStartSetting(Context context) {
        try {
            showActivity(context, "com.coloros.oppoguardelf", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity");
        } catch (Exception e) {
            showActivity(context, "com.iqoo.secure");
        }
    }

    public static void goMeizuAutoStartSetting(Context context) {
        try {
            showActivity(context,"com.meizu.safe", "com.meizu.safe.permission.SmartBGActivity");
        } catch (Exception e) {
            showActivity(context,"com.meizu.safe");
        }
    }

    public static void goSamsungAutoStartSetting(Context context) {
        try {
            showActivity(context,"com.samsung.android.sm", "com.samsung.android.sm.app.dashboard.SmartManagerDashBoardActivity");
        } catch (Exception e) {
            try {
                showActivity(context,"com.samsung.android.sm_cn");
            } catch (Exception e1) {
                showActivity(context,"com.samsung.android.sm");
            }
        }
    }

    public static void goLetvAutoStartSetting(Context context) {
        try {
            showActivity(context, "com.letv.android.letvsafe",
                    "com.letv.android.letvsafe.AutobootManageActivity");
        } catch (Exception e) {
            Intent intent = new Intent();
            intent.setAction("com.letv.android.permissionautoboot");
            if (!(context instanceof Activity)) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public static void goSmartisanAutoStartSetting(Context context) {
        showActivity(context, "com.smartisanos.security");
    }

    public static void goDefaultAutoStarSetting(Context context) {
        Intent intent = getDefaultSettingIntent(context);
        if(!(context instanceof Activity)){
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * 是否有自启动管理
     */
    public static boolean isNeedAutoStart() {
        return isHuawei() ||
                isXiaomi() ||
                isOPPO() ||
                isVIVO() ||
                isMeizu() ||
                isSamsung() ||
                isLeTV() ||
                isSmartisan();
    }

    /**
     * 跳转设置自启动页面
     */
    public static void toAutoStarSetting(Context context) {
        try {
            if (isHuawei()) {
                goHuaweiAutoStartSetting(context);
            } else if (isXiaomi()) {
                goXiaomiAutoStartSetting(context);
            } else if (isOPPO()) {
                goOPPOAutoStartSetting(context);
            } else if (isVIVO()) {
                goVIVOAutoStartSetting(context);
            } else if (isMeizu()) {
                goMeizuAutoStartSetting(context);
            } else if (isSamsung()) {
                goSamsungAutoStartSetting(context);
            } else if (isLeTV()) {
                goLetvAutoStartSetting(context);
            } else if (isSmartisan()) {
                goSmartisanAutoStartSetting(context);
            } else {
                goDefaultAutoStarSetting(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
            goDefaultAutoStarSetting(context);
        }
    }

    /**
     * 前往通知设置
     */
    public static Intent getNotifySettingIntent(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, context.getApplicationInfo().uid);
            return intent;
        } else {
            return getDefaultSettingIntent(context);
        }
    }

    /**
     * 应用详情设置页
     *
     * @return
     */
    public static Intent getDefaultSettingIntent(Context context) {
        return getDefaultSettingIntent(context.getPackageName());
    }

    /**
     * 应用详情设置页
     *
     * @return
     */
    public static Intent getDefaultSettingIntent(String pkgName) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package",pkgName, null));
        return intent;
    }

    /**
     * 跳转到MIUI应用权限设置页面
     */
    public static void goToXiaomiPermissionsEditorActivity(Context context, Integer requestCode) {
        try {
            // MIUI 8
            Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            intent.putExtra("extra_pkgname", context.getPackageName());
            if(!(context instanceof Activity)){
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            if (context instanceof Activity && requestCode != null) {
                ((Activity) context).startActivityForResult(intent, requestCode);
            } else {
                context.startActivity(intent);
            }
        } catch (Exception e) {
            try {
                // MIUI 5/6/7
                Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                intent.putExtra("extra_pkgname", context.getPackageName());
                if(!(context instanceof Activity)){
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                if (context instanceof Activity && requestCode != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ((Activity) context).startActivityForResult(intent, requestCode);
                } else {
                    context.startActivity(intent);
                }
            } catch (Exception e1) {
                // 否则跳转到应用详情
                Intent intent = getDefaultSettingIntent(context);
                if(!(context instanceof Activity)){
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                if (context instanceof Activity && requestCode != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ((Activity) context).startActivityForResult(intent, requestCode);
                } else {
                    context.startActivity(intent);
                }
            }
        }
    }

    /**
     * 跳转到指定应用的首页
     */
    public static void showActivity(Context context, @NonNull String packageName) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (!(context instanceof Activity)) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 跳转到指定应用的指定页面
     */
    public static void showActivity(Context context, @NonNull String packageName, @NonNull String activityDir) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, activityDir));
        if (!(context instanceof Activity)) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 是否有电池优化白名单
     */
    public static boolean isHaveBatteryOptSetting() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * 跳转设置电池优化白名单
     */
    public static void toBatteryOptSetting(Context context) {
        requestIgnoreBatteryOptimizations(context);
    }

    /**
     * 申请加入电池优化白名单
     *
     * @param context
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static void requestIgnoreBatteryOptimizations(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            if (!(context instanceof Activity)) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 跳转通知栏管理权限设置界面
     *
     * @return
     */
    public static Intent getNotificationAccessSettingIntent(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        } catch (ActivityNotFoundException e) {
            try {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.Settings$NotificationAccessSettingsActivity");
                intent.setComponent(cn);
                intent.putExtra(":settings:show_fragment", "NotificationAccessSettings");
                return intent;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }

    /**
     * usb调试
     *
     * @return
     */
    public static Intent getUsbDebugSettingIntent(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
        return intent;
    }

    public static Intent getAccessibilityIntent(Context context){
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        return intent;
    }

    /**
     * 跳转到设置页面无障碍服务开启自定义辅助功能服务
     *
     * @param context
     */
    public static void jumpToSettingPage(Context context) {
        Intent intent = getAccessibilityIntent(context);
        if (!(context instanceof Activity)) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 跳转到电池优化白名单界面
     * @param context
     */
    public static void jumpToBatteryOptSettingPage(Context context) {
      toBatteryOptSetting(context);
    }

    /**
     * 跳转悬浮窗设置界面Intent
     *
     * @return
     */
    public static Intent getFloatSettingIntent(Context context){
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        return intent;
    }

    /**
     * 判断自定义辅助功能服务是否开启
     *
     * @param context
     * @param className
     * @return
     */
    public static boolean isAccessibilitySettingsOn(Context context, String className) {
        if (context == null) {
            return false;
        }
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager != null) {
                List<ActivityManager.RunningServiceInfo> runningServices =
                        activityManager.getRunningServices(100);// 获取正在运行的服务列表
                if (runningServices.size() < 0) {
                    return false;
                }
                for (int i = 0; i < runningServices.size(); i++) {
                    ComponentName service = runningServices.get(i).service;
                    if (service.getClassName().equals(className)) {
                        return true;
                    }
                }
            }
        }catch (Exception e){}
        return false;
    }


    /**
     * 判断xiaomi后台弹出界面 false未开启 true开启
     * @return
     */
    public static boolean isXiaomiBgPopAllowed(Context context) {
        AppOpsManager ops = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        try {
            int op = 10021;
            Method method = ops.getClass().getMethod("checkOpNoThrow", new Class[]{int.class, int.class, String.class});
            Integer result = (Integer) method.invoke(ops, op, android.os.Process.myUid(), context.getPackageName());
            return result == AppOpsManager.MODE_ALLOWED;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 小米后台锁屏检测方法
     * @return
     */
    public static boolean isXiaomiShowLockView(Context context) {
        AppOpsManager ops = null;
        ops = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        try {
            int op = 10020; // >= 23
            // ops.checkOpNoThrow(op, uid, packageName)
            Method method = ops.getClass().getMethod("checkOpNoThrow",
                    int.class, int.class, String.class);
            Integer result = (Integer) method.invoke(ops, op,  android.os.Process.myUid(), context.getPackageName());

            return result == AppOpsManager.MODE_ALLOWED;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Android11的存储权限的适配
     * @param context
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    public static Intent getAndroid11ExternalIntent(Context context) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION );
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        return intent;
    }

    /**
     * 判断外部存储权限是否被满足，适配11
     * @param context
     * @return
     */
    public static boolean checkExternalStorageInAllAndroid(Context context) {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }
}
