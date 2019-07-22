/**
 * Copyright (c) 2016, smuyyh@gmail.com All Rights Reserved.
 * #                                                   #
 * #                       _oo0oo_                     #
 * #                      o8888888o                    #
 * #                      88" . "88                    #
 * #                      (| -_- |)                    #
 * #                      0\  =  /0                    #
 * #                    ___/`---'\___                  #
 * #                  .' \\|     |# '.                 #
 * #                 / \\|||  :  |||# \                #
 * #                / _||||| -:- |||||- \              #
 * #               |   | \\\  -  #/ |   |              #
 * #               | \_|  ''\---/''  |_/ |             #
 * #               \  .-\__  '-'  ___/-. /             #
 * #             ___'. .'  /--.--\  `. .'___           #
 * #          ."" '<  `.___\_<|>_/___.' >' "".         #
 * #         | | :  `- \`.;`\ _ /`;.`/ - ` : | |       #
 * #         \  \ `_.   \_ __\ /__ _/   .-` /  /       #
 * #     =====`-.____`.___ \_____/___.-`___.-'=====    #
 * #                       `=---='                     #
 * #     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   #
 * #                                                   #
 * #               佛祖保佑         永无BUG             #
 * #                                                   #
 */
package com.justwayward.reader.ui.activity

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.menu.MenuBuilder
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView

import com.google.gson.Gson
import com.justwayward.reader.R
import com.justwayward.reader.base.BaseActivity
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.user.TencentLoginResult
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerMainComponent
import com.justwayward.reader.manager.EventManager
import com.justwayward.reader.manager.SettingManager
import com.justwayward.reader.service.DownloadBookService
import com.justwayward.reader.ui.contract.MainContract
import com.justwayward.reader.ui.fragment.CommunityFragment
import com.justwayward.reader.ui.fragment.FindFragment
import com.justwayward.reader.ui.fragment.RecommendFragment
import com.justwayward.reader.ui.presenter.MainActivityPresenter
import com.justwayward.reader.utils.LogUtils
import com.justwayward.reader.utils.SharedPreferencesUtil
import com.justwayward.reader.utils.ToastUtils
import com.justwayward.reader.view.GenderPopupWindow
import com.justwayward.reader.view.LoginPopupWindow
import com.justwayward.reader.view.RVPIndicator
import com.tencent.connect.common.Constants
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError

import org.json.JSONObject

import java.lang.reflect.Method
import java.util.ArrayList
import java.util.Arrays

import javax.inject.Inject

import butterknife.Bind

/**
 * https://github.com/JustWayward/BookReader
 */
class MainActivity : BaseActivity(), MainContract.View, LoginPopupWindow.LoginTypeListener {

    @Bind(R.id.indicator)
    internal var mIndicator: RVPIndicator? = null
    @Bind(R.id.viewpager)
    internal var mViewPager: ViewPager? = null

    private var mTabContents: MutableList<Fragment>? = null
    private var mAdapter: FragmentPagerAdapter? = null
    private var mDatas: List<String>? = null

    @Inject
    internal var mPresenter: MainActivityPresenter? = null

    // 退出时间
    private var currentBackPressedTime: Long = 0

    private var popupWindow: LoginPopupWindow? = null
    var loginListener: IUiListener? = null
    private var genderPopupWindow: GenderPopupWindow? = null

    override val layoutId: Int
        get() = R.layout.activity_main

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerMainComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun initToolBar() {
        mCommonToolbar!!.setLogo(R.mipmap.logo)
        title = ""
    }

    fun pullSyncBookShelf() {
        mPresenter!!.syncBookShelf()
    }

    override fun initDatas() {
        startService(Intent(this, DownloadBookService::class.java))

        mTencent = Tencent.createInstance("1105670298", this@MainActivity)

        mDatas = Arrays.asList(*resources.getStringArray(R.array.home_tabs))
        mTabContents = ArrayList()
        mTabContents!!.add(RecommendFragment())
        mTabContents!!.add(CommunityFragment())
        mTabContents!!.add(FindFragment())

        mAdapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getCount(): Int {
                return mTabContents!!.size
            }

            override fun getItem(position: Int): Fragment {
                return mTabContents!![position]
            }
        }
    }

    override fun configViews() {
        mIndicator!!.setTabItemTitles(mDatas)
        mViewPager!!.adapter = mAdapter
        mViewPager!!.offscreenPageLimit = 3
        mIndicator!!.setViewPager(mViewPager, 0)

        mPresenter!!.attachView(this)

        mIndicator!!.postDelayed({
            if (!SettingManager.instance!!.isUserChooseSex && (genderPopupWindow == null || !genderPopupWindow!!.isShowing)) {
                showChooseSexPopupWindow()
            } else {
                showDialog()
                mPresenter!!.syncBookShelf()
            }
        }, 500)
    }

    fun showChooseSexPopupWindow() {
        if (genderPopupWindow == null) {
            genderPopupWindow = GenderPopupWindow(this@MainActivity)
        }
        if (!SettingManager.instance!!.isUserChooseSex && !genderPopupWindow!!.isShowing) {
            genderPopupWindow!!.showAtLocation(mCommonToolbar, Gravity.CENTER, 0, 0)
        }
    }

    fun setCurrentItem(position: Int) {
        mViewPager!!.currentItem = position
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_search -> startActivity(Intent(this@MainActivity, SearchActivity::class.java))
            R.id.action_login -> {
                if (popupWindow == null) {
                    popupWindow = LoginPopupWindow(this)
                    popupWindow!!.setLoginTypeListener(this)
                }
                popupWindow!!.showAtLocation(mCommonToolbar, Gravity.CENTER, 0, 0)
            }
            R.id.action_my_message -> {
                if (popupWindow == null) {
                    popupWindow = LoginPopupWindow(this)
                    popupWindow!!.setLoginTypeListener(this)
                }
                popupWindow!!.showAtLocation(mCommonToolbar, Gravity.CENTER, 0, 0)
            }
            R.id.action_sync_bookshelf -> {
                showDialog()
                mPresenter!!.syncBookShelf()
            }
            R.id.action_scan_local_book -> ScanLocalBookActivity.startActivity(this)
            R.id.action_wifi_book -> WifiBookActivity.startActivity(this)
            R.id.action_feedback -> FeedbackActivity.startActivity(this)
            R.id.action_night_mode -> {
                if (SharedPreferencesUtil.instance!!.getBoolean(Constant.ISNIGHT, false)) {
                    SharedPreferencesUtil.instance!!.putBoolean(Constant.ISNIGHT, false)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                } else {
                    SharedPreferencesUtil.instance!!.putBoolean(Constant.ISNIGHT, true)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                recreate()
            }
            R.id.action_settings -> SettingActivity.startActivity(this)
            else -> {
            }
        }/* if (popupWindow == null) {
                    popupWindow = new LoginPopupWindow(this);
                    popupWindow.setLoginTypeListener(this);
                }
                popupWindow.showAtLocation(mCommonToolbar, Gravity.CENTER, 0, 0);*/
        return super.onOptionsItemSelected(item)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - currentBackPressedTime > BACK_PRESSED_INTERVAL) {
                currentBackPressedTime = System.currentTimeMillis()
                ToastUtils.showToast(getString(R.string.exit_tips))
                return true
            } else {
                finish() // 退出
            }
        } else if (event.keyCode == KeyEvent.KEYCODE_MENU) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    /**
     * 显示item中的图片；
     *
     * @param view
     * @param menu
     * @return
     */
    override fun onPrepareOptionsPanel(view: View, menu: Menu?): Boolean {
        if (menu != null) {
            if (menu.javaClass == MenuBuilder::class.java) {
                try {
                    val m = menu.javaClass.getDeclaredMethod("setOptionalIconsVisible", java.lang.Boolean.TYPE)
                    m.isAccessible = true
                    m.invoke(menu, true)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
        return super.onPrepareOptionsPanel(view, menu)
    }

    override fun loginSuccess() {
        ToastUtils.showSingleToast("登陆成功")
    }

    override fun syncBookShelfCompleted() {
        dismissDialog()
        EventManager.refreshCollectionList()
    }

    override fun onLogin(view: ImageView, type: String) {
        if (type == "QQ") {
            if (!mTencent.isSessionValid) {
                if (loginListener == null) loginListener = BaseUIListener()
                mTencent.login(this, "all", loginListener)
            }
        }
        //4f45e920ff5d1a0e29d997986cd97181
    }

    override fun showError() {
        ToastUtils.showSingleToast("同步异常")
        dismissDialog()
    }

    override fun complete() {

    }


    inner class BaseUIListener : IUiListener {

        override fun onComplete(o: Any) {
            val jsonObject = o as JSONObject
            val json = jsonObject.toString()
            val gson = Gson()
            val result = gson.fromJson(json, TencentLoginResult::class.java)
            LogUtils.e(result.toString())
            mPresenter!!.login(result.openid, result.access_token, "QQ")
        }

        override fun onError(uiError: UiError) {}

        override fun onCancel() {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_LOGIN || requestCode == Constants.REQUEST_APPBAR) {
            Tencent.onActivityResultData(requestCode, resultCode, data, loginListener)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        DownloadBookService.cancel()
        stopService(Intent(this, DownloadBookService::class.java))
        if (mPresenter != null) {
            mPresenter!!.detachView()
        }
    }

    companion object {
        // 退出间隔
        private val BACK_PRESSED_INTERVAL = 2000
        var mTencent: Tencent
    }
}