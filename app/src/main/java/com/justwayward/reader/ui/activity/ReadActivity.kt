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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.ListPopupWindow
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.FrameLayout
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView

import com.justwayward.reader.R
import com.justwayward.reader.ReaderApplication
import com.justwayward.reader.base.BaseActivity
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.BookMixAToc
import com.justwayward.reader.bean.BookSource
import com.justwayward.reader.bean.ChapterRead
import com.justwayward.reader.bean.Recommend
import com.justwayward.reader.bean.support.BookMark
import com.justwayward.reader.bean.support.DownloadMessage
import com.justwayward.reader.bean.support.DownloadProgress
import com.justwayward.reader.bean.support.DownloadQueue
import com.justwayward.reader.bean.support.ReadTheme
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerBookComponent
import com.justwayward.reader.manager.CacheManager
import com.justwayward.reader.manager.CollectionsManager
import com.justwayward.reader.manager.EventManager
import com.justwayward.reader.manager.SettingManager
import com.justwayward.reader.manager.ThemeManager
import com.justwayward.reader.service.DownloadBookService
import com.justwayward.reader.ui.adapter.BookMarkAdapter
import com.justwayward.reader.ui.adapter.TocListAdapter
import com.justwayward.reader.ui.contract.BookReadContract
import com.justwayward.reader.ui.easyadapter.ReadThemeAdapter
import com.justwayward.reader.ui.presenter.BookReadPresenter
import com.justwayward.reader.utils.AppUtils
import com.justwayward.reader.utils.FileUtils
import com.justwayward.reader.utils.FormatUtils
import com.justwayward.reader.utils.LogUtils
import com.justwayward.reader.utils.ScreenUtils
import com.justwayward.reader.utils.SharedPreferencesUtil
import com.justwayward.reader.utils.TTSPlayerUtils
import com.justwayward.reader.utils.ToastUtils
import com.justwayward.reader.view.readview.BaseReadView
import com.justwayward.reader.view.readview.NoAimWidget
import com.justwayward.reader.view.readview.OnReadStateChangeListener
import com.justwayward.reader.view.readview.OverlappedWidget
import com.justwayward.reader.view.readview.PageWidget
import com.sinovoice.hcicloudsdk.android.tts.player.TTSPlayer
import com.sinovoice.hcicloudsdk.common.tts.TtsConfig
import com.sinovoice.hcicloudsdk.player.TTSCommonPlayer

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.io.File
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Collections
import java.util.Date
import java.util.concurrent.TimeUnit

import javax.inject.Inject

import butterknife.Bind
import butterknife.OnClick
import rx.Observable
import rx.functions.Action1

/**
 * Created by lfh on 2016/9/18.
 */
class ReadActivity : BaseActivity(), BookReadContract.View {

    @Bind(R.id.ivBack)
    internal var mIvBack: ImageView? = null
    @Bind(R.id.tvBookReadReading)
    internal var mTvBookReadReading: TextView? = null
    @Bind(R.id.tvBookReadCommunity)
    internal var mTvBookReadCommunity: TextView? = null
    @Bind(R.id.tvBookReadIntroduce)
    internal var mTvBookReadChangeSource: TextView? = null
    @Bind(R.id.tvBookReadSource)
    internal var mTvBookReadSource: TextView? = null

    @Bind(R.id.flReadWidget)
    internal var flReadWidget: FrameLayout? = null

    @Bind(R.id.llBookReadTop)
    internal var mLlBookReadTop: LinearLayout? = null
    @Bind(R.id.tvBookReadTocTitle)
    internal var mTvBookReadTocTitle: TextView? = null
    @Bind(R.id.tvBookReadMode)
    internal var mTvBookReadMode: TextView? = null
    @Bind(R.id.tvBookReadSettings)
    internal var mTvBookReadSettings: TextView? = null
    @Bind(R.id.tvBookReadDownload)
    internal var mTvBookReadDownload: TextView? = null
    @Bind(R.id.tvBookReadToc)
    internal var mTvBookReadToc: TextView? = null
    @Bind(R.id.llBookReadBottom)
    internal var mLlBookReadBottom: LinearLayout? = null
    @Bind(R.id.rlBookReadRoot)
    internal var mRlBookReadRoot: RelativeLayout? = null
    @Bind(R.id.tvDownloadProgress)
    internal var mTvDownloadProgress: TextView? = null

    @Bind(R.id.rlReadAaSet)
    internal var rlReadAaSet: LinearLayout? = null
    @Bind(R.id.ivBrightnessMinus)
    internal var ivBrightnessMinus: ImageView? = null
    @Bind(R.id.seekbarLightness)
    internal var seekbarLightness: SeekBar? = null
    @Bind(R.id.ivBrightnessPlus)
    internal var ivBrightnessPlus: ImageView? = null
    @Bind(R.id.tvFontsizeMinus)
    internal var tvFontsizeMinus: TextView? = null
    @Bind(R.id.seekbarFontSize)
    internal var seekbarFontSize: SeekBar? = null
    @Bind(R.id.tvFontsizePlus)
    internal var tvFontsizePlus: TextView? = null

    @Bind(R.id.rlReadMark)
    internal var rlReadMark: LinearLayout? = null
    @Bind(R.id.tvAddMark)
    internal var tvAddMark: TextView? = null
    @Bind(R.id.lvMark)
    internal var lvMark: ListView? = null

    @Bind(R.id.cbVolume)
    internal var cbVolume: CheckBox? = null
    @Bind(R.id.cbAutoBrightness)
    internal var cbAutoBrightness: CheckBox? = null
    @Bind(R.id.gvTheme)
    internal var gvTheme: GridView? = null

    private var decodeView: View? = null

    @Inject
    internal var mPresenter: BookReadPresenter? = null

    private val mChapterList = ArrayList<BookMixAToc.mixToc.Chapters>()
    private var mTocListPopupWindow: ListPopupWindow? = null
    private var mTocListAdapter: TocListAdapter? = null

    private var mMarkList: List<BookMark>? = null
    private var mMarkAdapter: BookMarkAdapter? = null

    private var currentChapter = 1

    /**
     * 是否开始阅读章节
     */
    private var startRead = false

    /**
     * 朗读 播放器
     */
    private var mTtsPlayer: TTSPlayer? = null
    private var ttsConfig: TtsConfig? = null

    private var mPageWidget: BaseReadView? = null
    private var curTheme = -1
    private var themes: List<ReadTheme>? = null
    private var gvAdapter: ReadThemeAdapter? = null
    private val receiver = Receiver()
    private val intentFilter = IntentFilter()
    private val sdf = SimpleDateFormat("HH:mm")

    private var recommendBooks: Recommend.RecommendBooks? = null
    private var bookId: String? = null

    private var isAutoLightness = false // 记录其他页面是否自动调整亮度
    private var isFromSD = false

    override val layoutId: Int
        get() {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN)
            statusBarColor = ContextCompat.getColor(this, R.color.reader_menu_bg_color)
            return R.layout.activity_read
        }

    /**
     * 时刻监听系统亮度改变事件
     */
    private val Brightness = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            //LogUtils.d("BrightnessOnChange:" + ScreenUtils.getScreenBrightnessInt255());
            if (!ScreenUtils.isAutoBrightness(this@ReadActivity)) {
                seekbarLightness!!.progress = ScreenUtils.screenBrightness
            }
        }
    }

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerBookComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun initToolBar() {}

    override fun initDatas() {
        recommendBooks = intent.getSerializableExtra(INTENT_BEAN) as Recommend.RecommendBooks
        bookId = recommendBooks!!._id
        isFromSD = intent.getBooleanExtra(INTENT_SD, false)

        if (Intent.ACTION_VIEW == intent.action) {
            val filePath = Uri.decode(intent.dataString!!.replace("file://", ""))
            val fileName: String
            if (filePath.lastIndexOf(".") > filePath.lastIndexOf("/")) {
                fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."))
            } else {
                fileName = filePath.substring(filePath.lastIndexOf("/") + 1)
            }

            CollectionsManager.instance!!.remove(fileName)
            // 转存
            val desc = FileUtils.createWifiTranfesFile(fileName)
            FileUtils.fileChannelCopy(File(filePath), desc)
            // 建立
            recommendBooks = Recommend.RecommendBooks()
            recommendBooks!!.isFromSD = true
            recommendBooks!!._id = fileName
            recommendBooks!!.title = fileName

            isFromSD = true
        }
        EventBus.getDefault().register(this)
        showDialog()

        mTvBookReadTocTitle!!.text = recommendBooks!!.title

        mTtsPlayer = TTSPlayerUtils.ttsPlayer
        ttsConfig = TTSPlayerUtils.ttsConfig

        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        intentFilter.addAction(Intent.ACTION_TIME_TICK)

        CollectionsManager.instance!!.setRecentReadingTime(bookId)
        Observable.timer(1000, TimeUnit.MILLISECONDS)
                .subscribe {
                    //延迟1秒刷新书架
                    EventManager.refreshCollectionList()
                }
    }

    override fun configViews() {
        hideStatusBar()
        decodeView = window.decorView
        val params = mLlBookReadTop!!.layoutParams as RelativeLayout.LayoutParams
        params.topMargin = ScreenUtils.getStatusBarHeight(this) - 2
        mLlBookReadTop!!.layoutParams = params

        initTocList()

        initAASet()

        initPagerWidget()

        mPresenter!!.attachView(this)
        // 本地收藏  直接打开
        if (isFromSD) {
            val chapters = BookMixAToc.mixToc.Chapters()
            chapters.title = recommendBooks!!.title
            mChapterList.add(chapters)
            showChapterRead(null, currentChapter)
            //本地书籍隐藏社区、简介、缓存按钮
            gone(mTvBookReadCommunity, mTvBookReadChangeSource, mTvBookReadDownload)
            return
        }
        mPresenter!!.getBookMixAToc(bookId, "chapters")
    }


    private fun initTocList() {
        mTocListAdapter = TocListAdapter(this, mChapterList, bookId, currentChapter)
        mTocListPopupWindow = ListPopupWindow(this)
        mTocListPopupWindow!!.setAdapter(mTocListAdapter)
        mTocListPopupWindow!!.width = ViewGroup.LayoutParams.MATCH_PARENT
        mTocListPopupWindow!!.height = ViewGroup.LayoutParams.WRAP_CONTENT
        mTocListPopupWindow!!.anchorView = mLlBookReadTop
        mTocListPopupWindow!!.setOnItemClickListener { parent, view, position, id ->
            mTocListPopupWindow!!.dismiss()
            currentChapter = position + 1
            mTocListAdapter!!.setCurrentChapter(currentChapter)
            startRead = false
            showDialog()
            readCurrentChapter()
            hideReadBar()
        }
        mTocListPopupWindow!!.setOnDismissListener {
            gone(mTvBookReadTocTitle)
            visible(mTvBookReadReading, mTvBookReadCommunity, mTvBookReadChangeSource)
        }
    }


    private fun initAASet() {
        curTheme = SettingManager.instance!!.readTheme
        ThemeManager.setReaderTheme(curTheme, mRlBookReadRoot)

        seekbarFontSize!!.max = 10
        //int fontSizePx = SettingManager.getInstance().getReadFontSize(bookId);
        val fontSizePx = SettingManager.instance!!.readFontSize
        val progress = ((ScreenUtils.pxToDpInt(fontSizePx.toFloat()) - 12) / 1.7f).toInt()
        seekbarFontSize!!.progress = progress
        seekbarFontSize!!.setOnSeekBarChangeListener(SeekBarChangeListener())

        seekbarLightness!!.max = 100
        seekbarLightness!!.setOnSeekBarChangeListener(SeekBarChangeListener())
        seekbarLightness!!.progress = ScreenUtils.screenBrightness
        isAutoLightness = ScreenUtils.isAutoBrightness(this)


        this.contentResolver.registerContentObserver(Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), true, Brightness)

        if (SettingManager.instance!!.isAutoBrightness) {
            startAutoLightness()
        } else {
            stopAutoLightness()
        }

        cbVolume!!.isChecked = SettingManager.instance!!.isVolumeFlipEnable
        cbVolume!!.setOnCheckedChangeListener(ChechBoxChangeListener())

        cbAutoBrightness!!.isChecked = SettingManager.instance!!.isAutoBrightness
        cbAutoBrightness!!.setOnCheckedChangeListener(ChechBoxChangeListener())

        gvAdapter = ReadThemeAdapter(this, themes = ThemeManager.getReaderThemeData(curTheme), curTheme)
        gvTheme!!.adapter = gvAdapter
        gvTheme!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            if (position < themes!!.size - 1) {
                changedMode(false, position)
            } else {
                changedMode(true, position)
            }
        }
    }

    private fun initPagerWidget() {
        when (SharedPreferencesUtil.instance!!.getInt(Constant.FLIP_STYLE, 0)) {
            0 -> mPageWidget = PageWidget(this, bookId, mChapterList, ReadListener())
            1 -> mPageWidget = OverlappedWidget(this, bookId, mChapterList, ReadListener())
            2 -> mPageWidget = NoAimWidget(this, bookId, mChapterList, ReadListener())
        }

        registerReceiver(receiver, intentFilter)
        if (SharedPreferencesUtil.instance!!.getBoolean(Constant.ISNIGHT, false)) {
            mPageWidget!!.setTextColor(ContextCompat.getColor(this, R.color.chapter_content_night),
                    ContextCompat.getColor(this, R.color.chapter_title_night))
        }
        flReadWidget!!.removeAllViews()
        flReadWidget!!.addView(mPageWidget)
    }

    /**
     * 加载章节列表
     *
     * @param list
     */
    override fun showBookToc(list: List<BookMixAToc.mixToc.Chapters>) {
        mChapterList.clear()
        mChapterList.addAll(list)
        readCurrentChapter()
    }

    /**
     * 获取当前章节。章节文件存在则直接阅读，不存在则请求
     */
    fun readCurrentChapter() {
        if (CacheManager.instance.getChapterFile(bookId, currentChapter) != null) {
            showChapterRead(null, currentChapter)
        } else {
            mPresenter!!.getChapterRead(mChapterList[currentChapter - 1].link, currentChapter)
        }
    }


    fun merginAllChapterToFile() {
        mPresenter!!.merginAllBook(recommendBooks, mChapterList)
    }


    @Synchronized
    override fun showChapterRead(data: ChapterRead.Chapter?, chapter: Int) { // 加载章节内容
        if (data != null) {
            CacheManager.instance.saveChapterFile(bookId, chapter, data)
        }

        if (!startRead) {
            startRead = true
            currentChapter = chapter
            if (!mPageWidget!!.isPrepared) {
                mPageWidget!!.init(curTheme)
            } else {
                mPageWidget!!.jumpToChapter(currentChapter)
            }
            hideDialog()
        }
    }

    override fun netError(chapter: Int) {
        hideDialog()//防止因为网络问题而出现dialog不消失
        if (Math.abs(chapter - currentChapter) <= 1) {
            ToastUtils.showToast(R.string.net_error)
        }
    }

    override fun showError() {
        hideDialog()
    }

    override fun complete() {
        hideDialog()
    }

    @Synchronized
    private fun hideReadBar() {
        gone(mTvDownloadProgress, mLlBookReadBottom, mLlBookReadTop, rlReadAaSet, rlReadMark)
        hideStatusBar()
        decodeView!!.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE
    }

    @Synchronized
    private fun showReadBar() { // 显示工具栏
        visible(mLlBookReadBottom, mLlBookReadTop)
        showStatusBar()
        decodeView!!.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }

    @Synchronized
    private fun toggleReadBar() { // 切换工具栏 隐藏/显示 状态
        if (isVisible(mLlBookReadTop)) {
            hideReadBar()
        } else {
            showReadBar()
        }
    }

    /***************Title Bar */

    @OnClick(R.id.ivBack)
    fun onClickBack() {
        if (mTocListPopupWindow!!.isShowing) {
            mTocListPopupWindow!!.dismiss()
        } else {
            finish()
        }
    }

    @OnClick(R.id.tvBookReadReading)
    fun readBook() {
        gone(rlReadAaSet, rlReadMark)
        ToastUtils.showToast("正在拼命开发中...")
    }


    @OnClick(R.id.tvBookReadCommunity)
    fun onClickCommunity() {
        gone(rlReadAaSet, rlReadMark)
        BookDetailCommunityActivity.startActivity(this, bookId, mTvBookReadTocTitle!!.text.toString(), 0)
    }

    @OnClick(R.id.tvBookReadIntroduce)
    fun onClickIntroduce() {
        gone(rlReadAaSet, rlReadMark)
        BookDetailActivity.startActivity(mContext, bookId)
    }

    @OnClick(R.id.tvBookReadSource)
    fun onClickSource() {
        BookSourceActivity.start(this, bookId, 1)
    }

    /***************Bottom Bar */

    @OnClick(R.id.tvBookReadMode)
    fun onClickChangeMode() { // 日/夜间模式切换
        gone(rlReadAaSet, rlReadMark)

        val isNight = !SharedPreferencesUtil.instance!!.getBoolean(Constant.ISNIGHT, false)
        changedMode(isNight, -1)
    }

    private fun changedMode(isNight: Boolean, position: Int) {
        SharedPreferencesUtil.instance!!.putBoolean(Constant.ISNIGHT, isNight)
        AppCompatDelegate.setDefaultNightMode(if (isNight)
            AppCompatDelegate.MODE_NIGHT_YES
        else
            AppCompatDelegate.MODE_NIGHT_NO)

        if (position >= 0) {
            curTheme = position
        } else {
            curTheme = SettingManager.instance!!.readTheme
        }
        gvAdapter!!.select(curTheme)

        mPageWidget!!.setTheme(if (isNight) ThemeManager.NIGHT else curTheme)
        mPageWidget!!.setTextColor(ContextCompat.getColor(mContext, if (isNight) R.color.chapter_content_night else R.color.chapter_content_day),
                ContextCompat.getColor(mContext, if (isNight) R.color.chapter_title_night else R.color.chapter_title_day))

        mTvBookReadMode!!.text = getString(if (isNight)
            R.string.book_read_mode_day_manual_setting
        else
            R.string.book_read_mode_night_manual_setting)
        val drawable = ContextCompat.getDrawable(this, if (isNight)
            R.drawable.ic_menu_mode_day_manual
        else
            R.drawable.ic_menu_mode_night_manual)
        drawable!!.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
        mTvBookReadMode!!.setCompoundDrawables(null, drawable, null, null)

        ThemeManager.setReaderTheme(curTheme, mRlBookReadRoot)
    }

    @OnClick(R.id.tvBookReadSettings)
    fun setting() {
        if (isVisible(mLlBookReadBottom)) {
            if (isVisible(rlReadAaSet)) {
                gone(rlReadAaSet)
            } else {
                visible(rlReadAaSet)
                gone(rlReadMark)
            }
        }
    }

    @OnClick(R.id.tvBookReadDownload)
    fun downloadBook() {
        gone(rlReadAaSet)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("缓存多少章？")
                .setItems(arrayOf("后面五十章", "后面全部", "全部")) { dialog, which ->
                    when (which) {
                        0 -> DownloadBookService.post(DownloadQueue(bookId, mChapterList, currentChapter + 1, currentChapter + 50))
                        1 -> DownloadBookService.post(DownloadQueue(bookId, mChapterList, currentChapter + 1, mChapterList.size))
                        2 -> DownloadBookService.post(DownloadQueue(bookId, mChapterList, 1, mChapterList.size))
                        else -> {
                        }
                    }
                }
        builder.show()
    }

    @OnClick(R.id.tvBookMark)
    fun onClickMark() {
        if (isVisible(mLlBookReadBottom)) {
            if (isVisible(rlReadMark)) {
                gone(rlReadMark)
            } else {
                gone(rlReadAaSet)

                updateMark()

                visible(rlReadMark)
            }
        }
    }

    @OnClick(R.id.tvBookReadToc)
    fun onClickToc() {
        gone(rlReadAaSet, rlReadMark)
        if (!mTocListPopupWindow!!.isShowing) {
            visible(mTvBookReadTocTitle)
            gone(mTvBookReadReading, mTvBookReadCommunity, mTvBookReadChangeSource)
            mTocListPopupWindow!!.inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
            mTocListPopupWindow!!.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            mTocListPopupWindow!!.show()
            mTocListPopupWindow!!.setSelection(currentChapter - 1)
            mTocListPopupWindow!!.listView!!.isFastScrollEnabled = true
        }
    }

    /***************Setting Menu */

    @OnClick(R.id.ivBrightnessMinus)
    fun brightnessMinus() {
        var curBrightness = SettingManager.instance!!.readBrightness
        if (curBrightness > 5 && !SettingManager.instance!!.isAutoBrightness) {
            seekbarLightness!!.progress = (curBrightness = curBrightness - 2)
            ScreenUtils.saveScreenBrightnessInt255(curBrightness, this@ReadActivity)
        }
    }

    @OnClick(R.id.ivBrightnessPlus)
    fun brightnessPlus() {
        var curBrightness = SettingManager.instance!!.readBrightness
        if (!SettingManager.instance!!.isAutoBrightness) {
            seekbarLightness!!.progress = (curBrightness = curBrightness + 2)
            ScreenUtils.saveScreenBrightnessInt255(curBrightness, this@ReadActivity)
        }
    }

    @OnClick(R.id.tvFontsizeMinus)
    fun fontsizeMinus() {
        calcFontSize(seekbarFontSize!!.progress - 1)
    }

    @OnClick(R.id.tvFontsizePlus)
    fun fontsizePlus() {
        calcFontSize(seekbarFontSize!!.progress + 1)
    }

    @OnClick(R.id.tvClear)
    fun clearBookMark() {
        SettingManager.instance!!.clearBookMarks(bookId)

        updateMark()
    }

    /***************Book Mark */

    @OnClick(R.id.tvAddMark)
    fun addBookMark() {
        val readPos = mPageWidget!!.readPos
        val mark = BookMark()
        mark.chapter = readPos[0]
        mark.startPos = readPos[1]
        mark.endPos = readPos[2]
        if (mark.chapter >= 1 && mark.chapter <= mChapterList.size) {
            mark.title = mChapterList[mark.chapter - 1].title
        }
        mark.desc = mPageWidget!!.headLine
        if (SettingManager.instance!!.addBookMark(bookId, mark)) {
            ToastUtils.showSingleToast("添加书签成功")
            updateMark()
        } else {
            ToastUtils.showSingleToast("书签已存在")
        }
    }

    private fun updateMark() {
        if (mMarkAdapter == null) {
            mMarkAdapter = BookMarkAdapter(this, ArrayList())
            lvMark!!.adapter = mMarkAdapter
            lvMark!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                val mark = mMarkAdapter!!.getData(position)
                if (mark != null) {
                    mPageWidget!!.setPosition(intArrayOf(mark.chapter, mark.startPos, mark.endPos))
                    hideReadBar()
                } else {
                    ToastUtils.showSingleToast("书签无效")
                }
            }
        }
        mMarkAdapter!!.clear()

        mMarkList = SettingManager.instance!!.getBookMarks(bookId)
        if (mMarkList != null && mMarkList!!.size > 0) {
            Collections.reverse(mMarkList)
            mMarkAdapter!!.addAll(mMarkList)
        }
    }

    /***************Event */

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showDownProgress(progress: DownloadProgress) {
        if (bookId == progress.bookId) {
            if (isVisible(mLlBookReadBottom)) { // 如果工具栏显示，则进度条也显示
                visible(mTvDownloadProgress)
                // 如果之前缓存过，就给提示
                mTvDownloadProgress!!.text = progress.message
            } else {
                gone(mTvDownloadProgress)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun downloadMessage(msg: DownloadMessage) {
        if (isVisible(mLlBookReadBottom)) { // 如果工具栏显示，则进度条也显示
            if (bookId == msg.bookId) {
                visible(mTvDownloadProgress)
                mTvDownloadProgress!!.text = msg.message
                if (msg.isComplete) {
                    mTvDownloadProgress!!.postDelayed({ gone(mTvDownloadProgress) }, 2500)
                }
            }
        }
    }

    /**
     * 显示加入书架对话框
     *
     * @param bean
     */
    private fun showJoinBookShelfDialog(bean: Recommend.RecommendBooks?) {
        AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.book_read_add_book))
                .setMessage(getString(R.string.book_read_would_you_like_to_add_this_to_the_book_shelf))
                .setPositiveButton(getString(R.string.book_read_join_the_book_shelf)) { dialog, which ->
                    dialog.dismiss()
                    bean!!.recentReadingTime = FormatUtils.getCurrentTimeString(FormatUtils.FORMAT_DATE_TIME)
                    CollectionsManager.instance!!.add(bean)
                    finish()
                }
                .setNegativeButton(getString(R.string.book_read_not)) { dialog, which ->
                    dialog.dismiss()
                    finish()
                }
                .create()
                .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            1 -> if (resultCode == Activity.RESULT_OK) {
                val bookSource = data!!.getSerializableExtra("source") as BookSource
                bookId = bookSource._id
            }
            else -> {
            }
        }//mPresenter.getBookMixAToc(bookId, "chapters");
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> if (mTocListPopupWindow != null && mTocListPopupWindow!!.isShowing) {
                mTocListPopupWindow!!.dismiss()
                gone(mTvBookReadTocTitle)
                visible(mTvBookReadReading, mTvBookReadCommunity, mTvBookReadChangeSource)
                return true
            } else if (isVisible(rlReadAaSet)) {
                gone(rlReadAaSet)
                return true
            } else if (isVisible(mLlBookReadBottom)) {
                hideReadBar()
                return true
            } else if (!CollectionsManager.instance!!.isCollected(bookId)) {
                showJoinBookShelfDialog(recommendBooks)
                return true
            }
            KeyEvent.KEYCODE_MENU -> {
                toggleReadBar()
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> if (SettingManager.instance!!.isVolumeFlipEnable) {
                return true
            }
            KeyEvent.KEYCODE_VOLUME_UP -> if (SettingManager.instance!!.isVolumeFlipEnable) {
                return true
            }
            else -> {
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (SettingManager.instance!!.isVolumeFlipEnable) {
                mPageWidget!!.nextPage()
                return true// 防止翻页有声音
            }
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (SettingManager.instance!!.isVolumeFlipEnable) {
                mPageWidget!!.prePage()
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (mTtsPlayer!!.playerState == TTSCommonPlayer.PLAYER_STATE_PLAYING)
            mTtsPlayer!!.stop()

        EventManager.refreshCollectionIcon()
        EventManager.refreshCollectionList()
        EventBus.getDefault().unregister(this)

        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {
            LogUtils.e("Receiver not registered")
        }

        if (isAutoLightness) {
            ScreenUtils.startAutoBrightness(this@ReadActivity)
        } else {
            ScreenUtils.stopAutoBrightness(this@ReadActivity)
        }

        if (mPresenter != null) {
            mPresenter!!.detachView()
        }

        // 观察内存泄漏情况
        ReaderApplication.getRefWatcher(this)!!.watch(this)
    }

    private inner class ReadListener : OnReadStateChangeListener {
        override fun onChapterChanged(chapter: Int) {
            LogUtils.i("onChapterChanged:$chapter")
            currentChapter = chapter
            mTocListAdapter!!.setCurrentChapter(currentChapter)
            // 加载前一节 与 后三节
            var i = chapter - 1
            while (i <= chapter + 3 && i <= mChapterList.size) {
                if (i > 0 && i != chapter
                        && CacheManager.instance.getChapterFile(bookId, i) == null) {
                    mPresenter!!.getChapterRead(mChapterList[i - 1].link, i)
                }
                i++
            }
        }

        override fun onPageChanged(chapter: Int, page: Int) {
            LogUtils.i("onPageChanged:$chapter-$page")
        }

        override fun onLoadChapterFailure(chapter: Int) {
            LogUtils.i("onLoadChapterFailure:$chapter")
            startRead = false
            if (CacheManager.instance.getChapterFile(bookId, chapter) == null)
                mPresenter!!.getChapterRead(mChapterList[chapter - 1].link, chapter)
        }

        override fun onCenterClick() {
            LogUtils.i("onCenterClick")
            toggleReadBar()
        }

        override fun onFlip() {
            hideReadBar()
        }
    }

    private inner class SeekBarChangeListener : SeekBar.OnSeekBarChangeListener {

        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            if (seekBar.id == seekbarFontSize!!.id && fromUser) {
                calcFontSize(progress)
            } else if (seekBar.id == seekbarLightness!!.id && fromUser
                    && !SettingManager.instance!!.isAutoBrightness) { // 非自动调节模式下 才可调整屏幕亮度
                ScreenUtils.saveScreenBrightnessInt100(progress, this@ReadActivity)
                //SettingManager.getInstance().saveReadBrightness(progress);
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {

        }
    }

    private inner class ChechBoxChangeListener : CompoundButton.OnCheckedChangeListener {

        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            if (buttonView.id == cbVolume!!.id) {
                SettingManager.instance!!.saveVolumeFlipEnable(isChecked)
            } else if (buttonView.id == cbAutoBrightness!!.id) {
                if (isChecked) {
                    startAutoLightness()
                } else {
                    stopAutoLightness()
                    ScreenUtils.saveScreenBrightnessInt255(ScreenUtils.screenBrightnessInt255, AppUtils.appContext)
                }
            }
        }
    }

    internal inner class Receiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (mPageWidget != null) {
                if (Intent.ACTION_BATTERY_CHANGED == intent.action) {
                    val level = intent.getIntExtra("level", 0)
                    mPageWidget!!.setBattery(100 - level)
                } else if (Intent.ACTION_TIME_TICK == intent.action) {
                    mPageWidget!!.setTime(sdf.format(Date()))
                }
            }
        }
    }

    private fun startAutoLightness() {
        SettingManager.instance!!.saveAutoBrightness(true)
        ScreenUtils.startAutoBrightness(this@ReadActivity)
        seekbarLightness!!.isEnabled = false
    }

    private fun stopAutoLightness() {
        SettingManager.instance!!.saveAutoBrightness(false)
        ScreenUtils.stopAutoBrightness(this@ReadActivity)
        seekbarLightness!!.progress = (ScreenUtils.screenBrightnessInt255 / 255.0f * 100).toInt()
        seekbarLightness!!.isEnabled = true
    }

    private fun calcFontSize(progress: Int) {
        // progress range 1 - 10
        if (progress >= 0 && progress <= 10) {
            seekbarFontSize!!.progress = progress
            mPageWidget!!.setFontSize(ScreenUtils.dpToPxInt(12 + 1.7f * progress))
        }
    }

    companion object {

        val INTENT_BEAN = "recommendBooksBean"
        val INTENT_SD = "isFromSD"

        @JvmOverloads
        fun startActivity(context: Context, recommendBooks: Recommend.RecommendBooks, isFromSD: Boolean = false) {
            context.startActivity(Intent(context, ReadActivity::class.java)
                    .putExtra(INTENT_BEAN, recommendBooks)
                    .putExtra(INTENT_SD, isFromSD))
        }
    }

}//添加收藏需要，所以跳转的时候传递整个实体类
