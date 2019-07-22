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
package com.justwayward.reader.base

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.os.Looper

import com.justwayward.reader.service.DownloadBookService
import com.justwayward.reader.utils.FileUtils
import com.justwayward.reader.utils.LogUtils
import com.justwayward.reader.utils.ToastUtils

import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.lang.Thread.UncaughtExceptionHandler
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.Date
import java.util.HashMap

/**
 * 全局异常捕获，当程序发生Uncaught异常的时候,由该类来接管程序,并记录发送错误报告.
 *
 * @author yuyh.
 * @date 2016/10/18.
 */
class CrashHandler private constructor() : UncaughtExceptionHandler {

    //系统默认的UncaughtException处理类
    private var mDefaultHandler: UncaughtExceptionHandler? = null
    //程序的Context对象
    private var mContext: Context? = null
    //用来存储设备信息和异常信息
    private val infos = HashMap<String, String>()

    /**
     * 初始化
     *
     * @param context
     */
    fun init(context: Context) {
        mContext = context
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    override fun uncaughtException(thread: Thread, ex: Throwable) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler!!.uncaughtException(thread, ex)
        } else {
            DownloadBookService.cancel() // 取消任务
            LogUtils.i("取消下载任务")
            Thread(Runnable {
                Looper.prepare()
                ToastUtils.showSingleToast("哎呀，程序发生异常啦...")
                Looper.loop()
            }).start()

            try {
                Thread.sleep(3000)
            } catch (e: InterruptedException) {
                LogUtils.e("CrashHandler.InterruptedException--->$e")
            }

            //退出程序
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(1)
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private fun handleException(ex: Throwable?): Boolean {
        if (ex == null) {
            return false
        }
        //收集设备参数信息
        collectDeviceInfo(mContext)
        //保存日志文件
        saveCrashInfo2File(ex)
        return true
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    fun collectDeviceInfo(ctx: Context?) {
        try {
            val pm = ctx!!.packageManager
            val pi = pm.getPackageInfo(ctx.packageName, PackageManager.GET_ACTIVITIES)
            if (pi != null) {
                val versionName = if (pi.versionName == null) "null" else pi.versionName
                val versionCode = pi.versionCode.toString() + ""
                infos["versionName"] = versionName
                infos["versionCode"] = versionCode
            }
        } catch (e: NameNotFoundException) {
            LogUtils.e("CrashHandleran.NameNotFoundException---> error occured when collect package info", e)
        }

        val fields = Build::class.java.declaredFields
        for (field in fields) {
            try {
                field.isAccessible = true
                infos[field.name] = field.get(null).toString()
            } catch (e: Exception) {
                LogUtils.e("CrashHandler.NameNotFoundException---> an error occured when collect crash info", e)
            }

        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称
     */
    private fun saveCrashInfo2File(ex: Throwable): String? {

        val sb = StringBuffer()
        sb.append("---------------------sta--------------------------")
        for ((key, value) in infos) {
            sb.append("$key=$value\n")
        }

        val writer = StringWriter()
        val printWriter = PrintWriter(writer)
        ex.printStackTrace(printWriter)
        var cause: Throwable? = ex.cause
        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        printWriter.close()
        val result = writer.toString()
        sb.append(result)
        sb.append("--------------------end---------------------------")
        LogUtils.e(sb.toString())
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val fileName = format.format(Date()) + ".log"
        val file = File(FileUtils.createRootPath(mContext) + "/log/" + fileName)
        FileUtils.createFile(file)
        FileUtils.writeFile(file.absolutePath, sb.toString())
        // uploadCrashMessage(sb.toString());
        return null
    }

    companion object {
        //CrashHandler实例
        private var INSTANCE: CrashHandler? = null

        /**
         * 获取CrashHandler实例 ,单例模式
         */
        val instance: CrashHandler
            get() {
                if (INSTANCE == null)
                    INSTANCE = CrashHandler()
                return INSTANCE
            }
    }
}
