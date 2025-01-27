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

import android.content.Context
import android.os.Environment
import android.util.Log

import com.justwayward.reader.ReaderApplication

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

/**
 * Log工具类，可控制Log输出开关、保存Log到文件、过滤输出等级
 *
 * @author yuyh.
 * @date 16/4/9.
 */
object LogUtils {
    private val LOG_SWITCH = true // 日志文件总开关
    private val LOG_TO_FILE = false // 日志写入文件开关
    private val LOG_TAG = "BookReader" // 默认的tag
    private val LOG_TYPE = 'v'// 输入日志类型，v代表输出所有信息,w则只输出警告...
    private val LOG_SAVE_DAYS = 7// sd卡中日志文件的最多保存天数

    private val LOG_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")// 日志的输出格式
    private val FILE_SUFFIX = SimpleDateFormat("yyyy-MM-dd")// 日志文件格式
    private var LOG_FILE_PATH: String? = null // 日志文件保存路径
    private var LOG_FILE_NAME: String? = null// 日志文件保存名称

    private val functionName: String?
        get() {
            val sts = Thread.currentThread().stackTrace ?: return null
            for (st in sts) {
                if (st.isNativeMethod) {
                    continue
                }
                if (st.className == Thread::class.java.name) {
                    continue
                }
                if (st.fileName == "LogUtils.java") {
                    continue
                }
                return ("[" + Thread.currentThread().name + "("
                        + Thread.currentThread().id + "): " + st.fileName
                        + ":" + st.lineNumber + "]")
            }
            return null
        }

    /**
     * 得到LOG_SAVE_DAYS天前的日期
     *
     * @return
     */
    private val dateBefore: Date
        get() {
            val nowtime = Date()
            val now = Calendar.getInstance()
            now.time = nowtime
            now.set(Calendar.DATE, now.get(Calendar.DATE) - LOG_SAVE_DAYS)
            return now.time
        }

    fun init(context: Context) { // 在Application中初始化
        LOG_FILE_PATH = Environment.getExternalStorageDirectory().path + File.separator + ReaderApplication.getsInstance()!!.packageName
        LOG_FILE_NAME = "Log"
    }

    /****************************
     * Warn
     */
    fun w(msg: Any) {
        w(LOG_TAG, msg)
    }

    @JvmOverloads
    fun w(tag: String, msg: Any, tr: Throwable? = null) {
        log(tag, msg.toString(), tr, 'w')
    }

    /***************************
     * Error
     */
    fun e(msg: Any) {
        e(LOG_TAG, msg)
    }

    @JvmOverloads
    fun e(tag: String, msg: Any, tr: Throwable? = null) {
        log(tag, msg.toString(), tr, 'e')
    }

    /***************************
     * Debug
     */
    fun d(msg: Any) {
        d(LOG_TAG, msg)
    }

    @JvmOverloads
    fun d(tag: String, msg: Any, tr: Throwable? = null) {
        log(tag, msg.toString(), tr, 'd')
    }

    /****************************
     * Info
     */
    fun i(msg: Any) {
        i(LOG_TAG, msg)
    }

    @JvmOverloads
    fun i(tag: String, msg: Any, tr: Throwable? = null) {
        log(tag, msg.toString(), tr, 'i')
    }

    /**************************
     * Verbose
     */
    fun v(msg: Any) {
        v(LOG_TAG, msg)
    }

    @JvmOverloads
    fun v(tag: String, msg: Any, tr: Throwable? = null) {
        log(tag, msg.toString(), tr, 'v')
    }

    /**
     * 根据tag, msg和等级，输出日志
     *
     * @param tag
     * @param msg
     * @param level
     */
    private fun log(tag: String, msg: String, tr: Throwable?, level: Char) {
        if (LOG_SWITCH) {
            if ('e' == level && ('e' == LOG_TYPE || 'v' == LOG_TYPE)) { // 输出错误信息
                Log.e(tag, createMessage(msg), tr)
            } else if ('w' == level && ('w' == LOG_TYPE || 'v' == LOG_TYPE)) {
                Log.w(tag, createMessage(msg), tr)
            } else if ('d' == level && ('d' == LOG_TYPE || 'v' == LOG_TYPE)) {
                Log.d(tag, createMessage(msg), tr)
            } else if ('i' == level && ('d' == LOG_TYPE || 'v' == LOG_TYPE)) {
                Log.i(tag, createMessage(msg), tr)
            } else {
                Log.v(tag, createMessage(msg), tr)
            }
            if (LOG_TO_FILE)
                log2File(level.toString(), tag, if (msg + tr!! == null) "" else "\n" + Log.getStackTraceString(tr))
        }
    }

    private fun createMessage(msg: String): String {
        val functionName = functionName
        return if (functionName == null)
            msg
        else
            "$functionName - $msg"
    }

    /**
     * 打开日志文件并写入日志
     *
     * @return
     */
    @Synchronized
    private fun log2File(mylogtype: String, tag: String, text: String) {
        val nowtime = Date()
        val date = FILE_SUFFIX.format(nowtime)
        val dateLogContent = LOG_FORMAT.format(nowtime) + ":" + mylogtype + ":" + tag + ":" + text // 日志输出格式
        val destDir = File(LOG_FILE_PATH)
        if (!destDir.exists()) {
            destDir.mkdirs()
        }
        val file = File(LOG_FILE_PATH, LOG_FILE_NAME!! + date)
        try {
            val filerWriter = FileWriter(file, true)
            val bufWriter = BufferedWriter(filerWriter)
            bufWriter.write(dateLogContent)
            bufWriter.newLine()
            bufWriter.close()
            filerWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    /**
     * 删除指定的日志文件
     */
    fun delFile() {// 删除日志文件
        val needDelFiel = FILE_SUFFIX.format(dateBefore)
        val file = File(LOG_FILE_PATH, needDelFiel + LOG_FILE_NAME!!)
        if (file.exists()) {
            file.delete()
        }
    }
}// 调试信息
