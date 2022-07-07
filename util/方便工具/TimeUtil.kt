package com.android.aschat.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by NINGMEI(shicaid) on 2022/2/14.
 * Intent: 时间工具类
 */
object TimeUtil {

    const val DATE_TIME_PATTERN1 = "yyyy-MM-dd HH:mm:ss"

    const val DATE_TIME_PATTERN2 = "yyyy-MM-dd HH:mm"

    const val DATE_TIME_PATTERN3 = "yyyy-MM-dd"

    const val DATE_TIME_PATTERN4 = "MM月dd日 HH:mm:ss"

    const val DATE_TIME_PATTERN5 = "M月dd"

    const val DATE_TIME_PATTERN6 = "dd"

    const val DATE_TIME_PATTERN7 = "yyyy-MM-dd'T'HH:mm:ss'Z'"

    const val DATE_TIME_PATTERN8 = "MMM d,yyyy"

    private val dateFormat: SimpleDateFormat = SimpleDateFormat()

    /**
     * 格式话指定时间
     *
     * @param format
     * 格式
     * @param date
     * 指定时间
     * @return
     */
    fun getDateStr(format: String, date: Date?): String {
        if(date == null){
            return ""
        }
        dateFormat.applyPattern(format)
        return dateFormat.format(date)
    }

    /**
     * 格式话指定时间
     *
     * @param format
     * 格式
     * @param timestamp
     * 时间戳
     * @return
     */
    fun getDateStr(format: String, timestamp: Long): String {
        return if (timestamp <= 0) {
            ""
        } else getDateStr(format, Date(timestamp))
    }

    /**
     * 格式化当前时间返回
     *
     * @param format
     * @return
     */
    fun getDateStrNow(format: String): String {
        return getDateStr(format, System.currentTimeMillis())
    }

    /**
     * 把时间字符串从一种格式转变为另一种格式
     */
    fun formatStringToFormatString(timeString: String, oldFormat: String, newFormat: String): String{
        dateFormat.applyPattern(oldFormat)
        val date: Date? = dateFormat.parse(timeString)
        date?.let {
            dateFormat.applyPattern(newFormat)
            return dateFormat.format(date)
        }
        return ""
    }


    /**
     * 查询前/未来pastIndex天的日期
     *
     * @param pastIndex 正数：前pastIndex天
     * 负数：未来v天
     * @return
     */
    fun getPastCal(pastIndex: Int): Calendar {
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - pastIndex)
        return calendar
    }

    /**
     * 和当天相比，preTime是否是同一天
     *
     * @param preTime
     * @return
     */
    fun isSameDay(preTime: Long): Boolean {
        val lastTimeCalendar = Calendar.getInstance()
        lastTimeCalendar.time = Date(preTime)
        return Calendar.getInstance()[Calendar.DAY_OF_YEAR] == lastTimeCalendar[Calendar.DAY_OF_YEAR]
    }

    /**
     * 时长转换为 mm:ss形式
     */
    fun formatDuration(duration: Long): String{
        val secondTotal = duration / 1000
        val min = duration / 60000
        val second = secondTotal - (60 * min)
        return if(min > 0) {
            String.format("%d:%02d", min,second)
        }else {
            String.format("00:%02d",second)
        }

    }

    /**
     * 和preTime相比，现在是X天之后
     * @param preTime
     * @return 返回-1表示非法
     */
    fun dayAfter(preTime: Long): Int {
        if (preTime <= 0) return -1
        val lastTimeCalendar = Calendar.getInstance()
        lastTimeCalendar.time = Date(preTime)
        val yearsInterval = Calendar.getInstance()[Calendar.YEAR] - lastTimeCalendar[Calendar.YEAR]
        var interval = 0
        if (yearsInterval == 0) {
            //是同一年的日来比较
            interval =
                Calendar.getInstance()[Calendar.DAY_OF_YEAR] - lastTimeCalendar[Calendar.DAY_OF_YEAR]
        } else if (yearsInterval == 1) {
            //preTime是上一年，现在是今年
            interval =
                365 - lastTimeCalendar[Calendar.DAY_OF_YEAR] + Calendar.getInstance()[Calendar.DAY_OF_YEAR]
        }
        return interval
    }

    /**
     * 和preTime相比，现在是否已是X分钟之后
     * @param preTime
     * @return 如果已是minute分钟之后，返回true，否则，返回false
     */
    fun isXMinuteAfter(preTime: Long, minute: Long): Boolean {
        val milliseconds = Calendar.getInstance().timeInMillis - preTime
        return milliseconds / 1000 / 60 > minute
    }

    fun getYearMonthDay(timeStamp: Long): IntArray {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeStamp
        val y = calendar.get(Calendar.YEAR)
        val m = calendar.get(Calendar.MONTH)
        val d = calendar.get(Calendar.DAY_OF_MONTH)
        return IntArray(3).apply {
            this[0] = y
            this[1] = m
            this[2] = d
        }
    }

    /**
     * 格式化 毫秒数: 00时00分00秒
     */
    fun formatMillisSecond2(ms: Long): String {

        val ss = 1000L
        val mi = ss * 60
        val hh = mi * 60
        val dd = hh * 24

        val day: Long = ms / dd
        val hour: Long = (ms - day * dd) / hh
        val minute: Long = (ms - day * dd - hour * hh) / mi
        val second: Long = (ms - day * dd - hour * hh - minute * mi) / ss
        val milliSecond: Long = ms - day * dd - hour * hh - minute * mi - second * ss
        return "$hour:$minute:$second"
    }
}