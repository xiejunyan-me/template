package com.maosiapps.battery.utils;

import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Date;

import timber.log.Timber;

public class CronUtils {

    /**
     * 根据 Cron表达式和开始时间，得到下次执行时间
     *
     * @param cron
     * @param startDate
     * @return
     */
    public static Date getNextExecuteTime(String cron, Date startDate) {
        try {
            CronExpression cronExpression = new CronExpression(cron);
            return cronExpression.getNextValidTimeAfter(startDate == null ? new Date() : startDate);
        } catch (ParseException e) {
            Timber.d("无效的cron表达式:" + cron + e);
            return startDate == null ? new Date() : startDate;
        }
    }

}

