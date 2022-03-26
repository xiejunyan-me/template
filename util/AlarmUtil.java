package com.maosiapps.battery.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.maosiapps.battery.bean.push.PushStrategyEntity;

import java.util.Calendar;
import java.util.Date;

public class AlarmUtil {
    /**
     * 在 startTime + interval 这个绝对时间发送含action的intent
     * @param context
     * @param action
     * @param startTime
     * @param interval
     */
    public static void setRTCAlarm(Context context, String action, long startTime, long interval) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(action);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // >= Android 6，建议增加PendingIntent可变性标志位提升安全性
            pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        }else {
            // < Android 6, PendingIntent都是可变的
            pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime + interval, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, startTime + interval, pendingIntent);
        }
    }

    /**
     * 在 startTime这个绝对时间戳执行一次
     * @param context
     * @param action
     * @param startTime
     */
    public static void setRTCAlarm(Context context, String action, long startTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(action);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // >= Android 6，建议增加PendingIntent可变性标志位提升安全性
            pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        }else {
            // < Android 6, PendingIntent都是可变的
            pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, startTime, pendingIntent);
        }
    }

    /**
     * 取消一个指定action的定时器
     * @param context
     * @param action
     */
    public static void cancelAlarm(Context context, String action) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(action);
        // 取消PendingIntent时flag标志位无意义
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    /**
     * 返回下一次执行的时间戳
     * @param pushStrategyEntity
     * @return
     */
    public static long getNextTimeStamp(PushStrategyEntity pushStrategyEntity) {
        // 如new Date() = 2020-05-30 16:50:00 ，Cron = "0 54 0/1 * * ?",表示每小时的54分钟时会执行一次
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                0, 0, 5);
        Date beginOfDate = calendar.getTime();
        Date nextExecuteTime = CronUtils.getNextExecuteTime(pushStrategyEntity.getTime(), beginOfDate);
        long timestamp = beginOfDate.getTime();
        if (nextExecuteTime !=null ) {
            timestamp = nextExecuteTime.getTime();
        }
        return timestamp;
    }
}
