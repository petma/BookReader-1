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

import android.annotation.TargetApi
import android.app.ActivityManager
import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.SystemClock
import android.telephony.TelephonyManager
import android.text.format.Formatter
import android.util.DisplayMetrics

/**
 * @author yuyh.
 * @date 16/4/9.
 */
object DeviceUtils {

    private val TAG = DeviceUtils::class.java.simpleName
    private val CMCC_ISP = "46000"//中国移动
    private val CMCC2_ISP = "46002"//中国移动
    private val CU_ISP = "46001"//中国联通
    private val CT_ISP = "46003"//中国电信

    /**
     * 获取设备的系统版本号
     */
    val deviceSDK: Int
        get() = Build.VERSION.SDK_INT

    /**
     * 获取设备的型号
     */
    val deviceName: String
        get() = Build.MODEL

    /**
     * 获取 开机时间
     */
    val bootTimeString: String
        get() {
            val ut = SystemClock.elapsedRealtime() / 1000
            val h = (ut / 3600).toInt()
            val m = (ut / 60 % 60).toInt()
            LogUtils.i(TAG, "$h:$m")
            return "$h:$m"
        }

    fun getIMSI(context: Context): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.subscriberId
    }

    fun getIMEI(context: Context): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return telephonyManager.deviceId
    }

    /**
     * 获取手机网络运营商类型
     *
     * @param context
     * @return
     */
    fun getPhoneISP(context: Context?): String {
        if (context == null) {
            return ""
        }
        val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var teleCompany = ""
        val np = manager.networkOperator

        if (np != null) {
            if (np == CMCC_ISP || np == CMCC2_ISP) {
                teleCompany = "中国移动"
            } else if (np.startsWith(CU_ISP)) {
                teleCompany = "中国联通"
            } else if (np.startsWith(CT_ISP)) {
                teleCompany = "中国电信"
            }
        }
        return teleCompany
    }

    /**
     * 获取屏幕信息
     *
     * @param context
     * @return
     */
    fun getDisplayMetrics(context: Context): DisplayMetrics {
        return context.resources.displayMetrics
    }

    /**
     * 获取/打印屏幕信息
     *
     * @param context
     * @return
     */
    fun printDisplayInfo(context: Context): DisplayMetrics {
        val dm = getDisplayMetrics(context)
        val sb = StringBuilder()
        sb.append("\ndensity         :").append(dm.density)
        sb.append("\ndensityDpi      :").append(dm.densityDpi)
        sb.append("\nheightPixels    :").append(dm.heightPixels)
        sb.append("\nwidthPixels     :").append(dm.widthPixels)
        sb.append("\nscaledDensity   :").append(dm.scaledDensity)
        sb.append("\nxdpi            :").append(dm.xdpi)
        sb.append("\nydpi            :").append(dm.ydpi)
        LogUtils.i(TAG, sb.toString())
        return dm
    }

    /**
     * 获取系统当前可用内存大小
     *
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    fun getAvailMemory(context: Context): String {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        return Formatter.formatFileSize(context, mi.availMem)// 将获取的内存大小规格化
    }


    /**
     * 获取 MAC 地址
     * 须配置android.permission.ACCESS_WIFI_STATE权限
     */
    fun getMacAddress(context: Context): String {
        //wifi mac地址
        val wifi = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifi.connectionInfo
        val mac = info.macAddress
        LogUtils.i(TAG, " MAC：$mac")
        return mac
    }
}
