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
package com.justwayward.reader.ui.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AlertDialog
import android.text.TextUtils
import android.view.View
import android.widget.TextView

import com.justwayward.reader.R
import com.justwayward.reader.api.BookApi
import com.justwayward.reader.base.BaseActivity
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerMainComponent
import com.justwayward.reader.utils.NetworkUtils
import com.justwayward.reader.wifitransfer.Defaults
import com.justwayward.reader.wifitransfer.ServerRunner

import javax.inject.Inject

import butterknife.Bind
import butterknife.OnClick

/**
 * Created by xiaoshu on 2016/10/9.
 */
class WifiBookActivity : BaseActivity() {

    @Bind(R.id.mTvWifiName)
    internal var mTvWifiName: TextView? = null
    @Bind(R.id.mTvWifiIp)
    internal var mTvWifiIp: TextView? = null

    @Bind(R.id.tvRetry)
    internal var tvRetry: TextView? = null

    @Inject
    internal var mBookApi: BookApi? = null

    override val layoutId: Int
        get() = R.layout.activity_wifi_book

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerMainComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun initToolBar() {
        mCommonToolbar!!.title = "WiFi传书"
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
    }

    override fun initDatas() {
        val wifiname = NetworkUtils.getConnectWifiSsid(mContext)
        if (!TextUtils.isEmpty(wifiname)) {
            mTvWifiName!!.text = wifiname!!.replace("\"", "")
        } else {
            mTvWifiName!!.text = "Unknow"
        }

        val wifiIp = NetworkUtils.getConnectWifiIp(mContext)
        if (!TextUtils.isEmpty(wifiIp)) {
            tvRetry!!.visibility = View.GONE
            mTvWifiIp!!.text = "http://" + NetworkUtils.getConnectWifiIp(mContext) + ":" + Defaults.port
            // 启动wifi传书服务器
            ServerRunner.startServer(mBookApi)
        } else {
            mTvWifiIp!!.text = "请开启Wifi并重试"
            tvRetry!!.visibility = View.VISIBLE
        }
    }


    override fun configViews() {

    }

    @OnClick(R.id.tvRetry)
    fun retry() {
        initDatas()
    }

    override fun onBackPressed() {
        if (ServerRunner.serverIsRunning) {
            AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("确定要关闭？Wifi传书将会中断！")
                    .setPositiveButton("确定") { dialog, which ->
                        dialog.dismiss()
                        finish()
                    }.setNegativeButton("取消") { dialog, which -> dialog.dismiss() }.create().show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ServerRunner.stopServer()
    }

    companion object {

        fun startActivity(context: Context) {
            context.startActivity(Intent(context, WifiBookActivity::class.java))
        }
    }


}
