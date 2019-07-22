/**
 * Copyright 2016 JustWayward Team
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.justwayward.reader.utils

import android.text.TextUtils

import java.text.SimpleDateFormat
import java.util.Date

/**
 * @author yuyh.
 * @date 16/9/2.
 */
object FormatUtils {

    private val ONE_MINUTE = 60000L
    private val ONE_HOUR = 3600000L
    private val ONE_DAY = 86400000L
    private val ONE_WEEK = 604800000L

    private val ONE_SECOND_AGO = "秒前"
    private val ONE_MINUTE_AGO = "分钟前"
    private val ONE_HOUR_AGO = "小时前"
    private val ONE_DAY_AGO = "天前"
    private val ONE_MONTH_AGO = "月前"
    private val ONE_YEAR_AGO = "年前"

    private val sdf = SimpleDateFormat()
    val FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm:ss.SSS"

    /**
     * 获取当前日期的指定格式的字符串
     *
     * @param format 指定的日期时间格式，若为null或""则使用指定的格式"yyyy-MM-dd HH:mm:ss.SSS"
     * @return
     */
    fun getCurrentTimeString(format: String?): String {
        if (format == null || format.trim { it <= ' ' } == "") {
            sdf.applyPattern(FORMAT_DATE_TIME)
        } else {
            sdf.applyPattern(format)
        }
        return sdf.format(Date())
    }

    /**
     * 根据时间字符串获取描述性时间，如3分钟前，1天前
     *
     * @param dateString 时间字符串
     * @return
     */
    fun getDescriptionTimeFromDateString(dateString: String): String {
        if (TextUtils.isEmpty(dateString))
            return ""
        sdf.applyPattern(FORMAT_DATE_TIME)
        try {
            return getDescriptionTimeFromDate(sdf.parse(formatZhuiShuDateString(dateString)))
        } catch (e: Exception) {
            LogUtils.e("getDescriptionTimeFromDateString: $e")
        }

        return ""
    }

    /**
     * 格式化追书神器返回的时间字符串
     *
     * @param dateString 时间字符串
     * @return
     */
    fun formatZhuiShuDateString(dateString: String): String {
        return dateString.replace("T".toRegex(), " ").replace("Z".toRegex(), "")
    }

    /**
     * 根据Date获取描述性时间，如3分钟前，1天前
     *
     * @param date
     * @return
     */
    fun getDescriptionTimeFromDate(date: Date): String {
        val delta = Date().time - date.time
        if (delta < 1L * ONE_MINUTE) {
            val seconds = toSeconds(delta)
            return (if (seconds <= 0) 1 else seconds).toString() + ONE_SECOND_AGO
        }
        if (delta < 45L * ONE_MINUTE) {
            val minutes = toMinutes(delta)
            return (if (minutes <= 0) 1 else minutes).toString() + ONE_MINUTE_AGO
        }
        if (delta < 24L * ONE_HOUR) {
            val hours = toHours(delta)
            return (if (hours <= 0) 1 else hours).toString() + ONE_HOUR_AGO
        }
        if (delta < 48L * ONE_HOUR) {
            return "昨天"
        }
        if (delta < 30L * ONE_DAY) {
            val days = toDays(delta)
            return (if (days <= 0) 1 else days).toString() + ONE_DAY_AGO
        }
        if (delta < 12L * 4L * ONE_WEEK) {
            val months = toMonths(delta)
            return (if (months <= 0) 1 else months).toString() + ONE_MONTH_AGO
        } else {
            val years = toYears(delta)
            return (if (years <= 0) 1 else years).toString() + ONE_YEAR_AGO
        }
    }

    private fun toSeconds(date: Long): Long {
        return date / 1000L
    }

    private fun toMinutes(date: Long): Long {
        return toSeconds(date) / 60L
    }

    private fun toHours(date: Long): Long {
        return toMinutes(date) / 60L
    }

    private fun toDays(date: Long): Long {
        return toHours(date) / 24L
    }

    private fun toMonths(date: Long): Long {
        return toDays(date) / 30L
    }

    private fun toYears(date: Long): Long {
        return toMonths(date) / 365L
    }

    fun formatWordCount(wordCount: Int): String {
        return if (wordCount / 10000 > 0) {
            (wordCount / 10000f + 0.5).toInt().toString() + "万字"
        } else if (wordCount / 1000 > 0) {
            (wordCount / 1000f + 0.5).toInt().toString() + "千字"
        } else {
            wordCount.toString() + "字"
        }
    }
}