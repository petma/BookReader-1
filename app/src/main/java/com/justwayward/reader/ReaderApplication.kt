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
package com.justwayward.reader

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

import com.justwayward.reader.base.Constant
import com.justwayward.reader.base.CrashHandler
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerAppComponent
import com.justwayward.reader.module.AppModule
import com.justwayward.reader.module.BookApiModule
import com.justwayward.reader.utils.AppUtils
import com.justwayward.reader.utils.FileUtils
import com.justwayward.reader.utils.LogUtils
import com.justwayward.reader.utils.SharedPreferencesUtil
import com.sinovoice.hcicloudsdk.api.HciCloudSys
import com.sinovoice.hcicloudsdk.common.HciErrorCode
import com.sinovoice.hcicloudsdk.common.InitParam
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher

/**
 * @author yuyh.
 * @date 2016/8/3.
 */
class ReaderApplication : Application() {
    var appComponent: AppComponent? = null
        private set

    private var refWatcher: RefWatcher? = null

    override fun onCreate() {
        super.onCreate()
        refWatcher = LeakCanary.install(this)
        sInstance = this
        initCompoent()
        AppUtils.init(this)
        CrashHandler.instance.init(this)
        initPrefs()
        initNightMode()
        //initHciCloud();
    }

    private fun initCompoent() {
        appComponent = DaggerAppComponent.builder()
                .bookApiModule(BookApiModule())
                .appModule(AppModule(this))
                .build()
    }

    /**
     * 初始化SharedPreference
     */
    protected fun initPrefs() {
        SharedPreferencesUtil.init(applicationContext, packageName + "_preference", Context.MODE_MULTI_PROCESS)
    }

    protected fun initNightMode() {
        val isNight = SharedPreferencesUtil.instance!!.getBoolean(Constant.ISNIGHT, false)
        LogUtils.d("isNight=$isNight")
        if (isNight) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    protected fun initHciCloud() {
        val initparam = InitParam()
        val authDirPath = filesDir.absolutePath
        initparam.addParam(InitParam.AuthParam.PARAM_KEY_AUTH_PATH, authDirPath)
        initparam.addParam(InitParam.AuthParam.PARAM_KEY_AUTO_CLOUD_AUTH, "no")
        initparam.addParam(InitParam.AuthParam.PARAM_KEY_CLOUD_URL, "test.api.hcicloud.com:8888")
        initparam.addParam(InitParam.AuthParam.PARAM_KEY_DEVELOPER_KEY, "0a5e69f8fb1c019b2d87a17acf200889")
        initparam.addParam(InitParam.AuthParam.PARAM_KEY_APP_KEY, "0d5d5466")
        val logDirPath = FileUtils.createRootPath(this) + "/hcicloud"
        FileUtils.createDir(logDirPath)
        initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_FILE_PATH, logDirPath)
        initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_FILE_COUNT, "5")
        initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_FILE_SIZE, "1024")
        initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_LEVEL, "5")
        val errCode = HciCloudSys.hciInit(initparam.stringConfig, this)
        if (errCode != HciErrorCode.HCI_ERR_NONE) {
            LogUtils.e("HciCloud初始化失败$errCode")
            return
        }
        LogUtils.e("HciCloud初始化成功")
    }

    companion object {

        private var sInstance: ReaderApplication? = null

        fun getRefWatcher(context: Context): RefWatcher? {
            val application = context.applicationContext as ReaderApplication
            return application.refWatcher
        }

        fun getsInstance(): ReaderApplication? {
            return sInstance
        }
    }
}
