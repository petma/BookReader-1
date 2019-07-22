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


import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.widget.SwitchCompat
import android.widget.CompoundButton
import android.widget.TextView

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseActivity
import com.justwayward.reader.base.Constant
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerMainComponent
import com.justwayward.reader.manager.CacheManager
import com.justwayward.reader.manager.EventManager
import com.justwayward.reader.manager.SettingManager
import com.justwayward.reader.utils.SharedPreferencesUtil

import butterknife.Bind
import butterknife.OnClick

/**
 * Created by xiaoshu on 2016/10/8.
 */
class SettingActivity : BaseActivity() {

    @Bind(R.id.mTvSort)
    internal var mTvSort: TextView? = null
    @Bind(R.id.tvFlipStyle)
    internal var mTvFlipStyle: TextView? = null
    @Bind(R.id.tvCacheSize)
    internal var mTvCacheSize: TextView? = null
    @Bind(R.id.noneCoverCompat)
    internal var noneCoverCompat: SwitchCompat? = null


    override val layoutId: Int
        get() = R.layout.activity_setting

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerMainComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun initToolBar() {
        mCommonToolbar!!.title = "设置"
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
    }

    override fun initDatas() {
        Thread(Runnable {
            val cachesize = CacheManager.instance.cacheSize
            runOnUiThread { mTvCacheSize!!.setText(cachesize) }
        }).start()
        mTvSort!!.text = resources.getStringArray(R.array.setting_dialog_sort_choice)[if (SharedPreferencesUtil.instance!!.getBoolean(Constant.ISBYUPDATESORT, true)) 0 else 1]
        mTvFlipStyle!!.text = resources.getStringArray(R.array.setting_dialog_style_choice)[SharedPreferencesUtil.instance!!.getInt(Constant.FLIP_STYLE, 0)]
    }


    override fun configViews() {
        noneCoverCompat!!.isChecked = SettingManager.instance!!.isNoneCover
        noneCoverCompat!!.setOnCheckedChangeListener { buttonView, isChecked -> SettingManager.instance!!.saveNoneCover(isChecked) }
    }

    @OnClick(R.id.bookshelfSort)
    fun onClickBookShelfSort() {
        AlertDialog.Builder(mContext)
                .setTitle("书架排序方式")
                .setSingleChoiceItems(resources.getStringArray(R.array.setting_dialog_sort_choice),
                        if (SharedPreferencesUtil.instance!!.getBoolean(Constant.ISBYUPDATESORT, true)) 0 else 1
                ) { dialog, which ->
                    mTvSort!!.text = resources.getStringArray(R.array.setting_dialog_sort_choice)[which]
                    SharedPreferencesUtil.instance!!.putBoolean(Constant.ISBYUPDATESORT, which == 0)
                    EventManager.refreshCollectionList()
                    dialog.dismiss()
                }
                .create().show()
    }

    @OnClick(R.id.rlFlipStyle)
    fun onClickFlipStyle() {
        AlertDialog.Builder(mContext)
                .setTitle("阅读页翻页效果")
                .setSingleChoiceItems(resources.getStringArray(R.array.setting_dialog_style_choice),
                        SharedPreferencesUtil.instance!!.getInt(Constant.FLIP_STYLE, 0)
                ) { dialog, which ->
                    mTvFlipStyle!!.text = resources.getStringArray(R.array.setting_dialog_style_choice)[which]
                    SharedPreferencesUtil.instance!!.putInt(Constant.FLIP_STYLE, which)
                    dialog.dismiss()
                }
                .create().show()
    }

    @OnClick(R.id.feedBack)
    fun feedBack() {
        FeedbackActivity.startActivity(this)
    }

    @OnClick(R.id.cleanCache)
    fun onClickCleanCache() {
        //默认不勾选清空书架列表，防手抖！！
        val selected = booleanArrayOf(true, false)
        AlertDialog.Builder(mContext)
                .setTitle("清除缓存")
                .setCancelable(true)
                .setMultiChoiceItems(arrayOf("删除阅读记录", "清空书架列表"), selected) { dialog, which, isChecked -> selected[which] = isChecked }
                .setPositiveButton("确定") { dialog, which ->
                    Thread(Runnable {
                        CacheManager.instance.clearCache(selected[0], selected[1])
                        val cacheSize = CacheManager.instance.cacheSize
                        runOnUiThread {
                            mTvCacheSize!!.setText(cacheSize)
                            EventManager.refreshCollectionList()
                        }
                    }).start()
                    dialog.dismiss()
                }
                .setNegativeButton("取消") { dialog, which -> dialog.dismiss() }
                .create().show()
    }

    companion object {

        fun startActivity(context: Context) {
            context.startActivity(Intent(context, SettingActivity::class.java))
        }
    }
}
