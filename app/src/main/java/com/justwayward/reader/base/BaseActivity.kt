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

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.view.WindowManager

import com.justwayward.reader.R
import com.justwayward.reader.ReaderApplication
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.utils.SharedPreferencesUtil
import com.justwayward.reader.utils.StatusBarCompat
import com.justwayward.reader.view.loadding.CustomDialog

import butterknife.ButterKnife

abstract class BaseActivity : AppCompatActivity() {

    var mCommonToolbar: Toolbar? = null

    protected var mContext: Context
    protected var statusBarColor = 0
    protected var statusBarView: View? = null
    private var mNowMode: Boolean = false
    private var dialog: CustomDialog? = null//进度条

    abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)
        if (statusBarColor == 0) {
            statusBarView = StatusBarCompat.compat(this, ContextCompat.getColor(this, R.color.colorPrimaryDark))
        } else if (statusBarColor != -1) {
            statusBarView = StatusBarCompat.compat(this, statusBarColor)
        }
        transparent19and20()
        mContext = this
        ButterKnife.bind(this)
        setupActivityComponent(ReaderApplication.getsInstance()!!.appComponent)
        mCommonToolbar = ButterKnife.findById(this, R.id.common_toolbar)
        if (mCommonToolbar != null) {
            initToolBar()
            setSupportActionBar(mCommonToolbar)
        }
        initDatas()
        configViews()
        mNowMode = SharedPreferencesUtil.instance!!.getBoolean(Constant.ISNIGHT)
    }

    protected fun transparent19and20() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //透明状态栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun toolbarSetElevation(elevation: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCommonToolbar!!.elevation = elevation
        }
    }

    override fun onResume() {
        super.onResume()
        if (SharedPreferencesUtil.instance!!.getBoolean(Constant.ISNIGHT, false) != mNowMode) {
            if (SharedPreferencesUtil.instance!!.getBoolean(Constant.ISNIGHT, false)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            recreate()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ButterKnife.unbind(this)
        dismissDialog()
    }

    protected abstract fun setupActivityComponent(appComponent: AppComponent?)

    abstract fun initToolBar()

    abstract fun initDatas()

    /**
     * 对各种控件进行设置、适配、填充数据
     */
    abstract fun configViews()

    protected fun gone(vararg views: View) {
        if (views != null && views.size > 0) {
            for (view in views) {
                if (view != null) {
                    view.visibility = View.GONE
                }
            }
        }
    }

    protected fun visible(vararg views: View) {
        if (views != null && views.size > 0) {
            for (view in views) {
                if (view != null) {
                    view.visibility = View.VISIBLE
                }
            }
        }

    }

    protected fun isVisible(view: View): Boolean {
        return view.visibility == View.VISIBLE
    }

    // dialog
    fun getDialog(): CustomDialog {
        if (dialog == null) {
            dialog = CustomDialog.instance(this)
            dialog!!.setCancelable(true)
        }
        return dialog
    }

    fun hideDialog() {
        if (dialog != null)
            dialog!!.hide()
    }

    fun showDialog() {
        getDialog().show()
    }

    fun dismissDialog() {
        if (dialog != null) {
            dialog!!.dismiss()
            dialog = null
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    protected fun hideStatusBar() {
        val attrs = window.attributes
        attrs.flags = attrs.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
        window.attributes = attrs
        if (statusBarView != null) {
            statusBarView!!.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    protected fun showStatusBar() {
        val attrs = window.attributes
        attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
        window.attributes = attrs
        if (statusBarView != null) {
            statusBarView!!.setBackgroundColor(statusBarColor)
        }
    }

}
