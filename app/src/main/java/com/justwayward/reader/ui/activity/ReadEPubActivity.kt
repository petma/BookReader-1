package com.justwayward.reader.ui.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import androidx.appcompat.widget.ListPopupWindow
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.AdapterView
import android.widget.PopupWindow
import android.widget.TextView

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseActivity
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.BookMixAToc
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.ui.adapter.EPubReaderAdapter
import com.justwayward.reader.ui.adapter.TocListAdapter
import com.justwayward.reader.utils.FileUtils
import com.justwayward.reader.view.epubview.DirectionalViewpager
import com.justwayward.reader.view.epubview.ReaderCallback

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList

import butterknife.Bind
import butterknife.OnClick
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.domain.SpineReference
import nl.siegmann.epublib.domain.TOCReference
import nl.siegmann.epublib.epub.EpubReader

class ReadEPubActivity : BaseActivity(), ReaderCallback {

    @Bind(R.id.epubViewPager)
    internal var viewpager: DirectionalViewpager? = null

    @Bind(R.id.toolbar_menu)
    internal var ivMenu: View? = null
    @Bind(R.id.toolbar_title)
    internal var tvTitle: TextView? = null

    private var mAdapter: EPubReaderAdapter? = null

    private var mFileName: String? = null
    private var mFilePath: String? = null

    private var mBook: Book? = null
    private var mTocReferences: ArrayList<TOCReference>? = null
    private var mSpineReferences: List<SpineReference>? = null
    var mIsSmilParsed = false

    private val mChapterList = ArrayList<BookMixAToc.mixToc.Chapters>()
    private var mTocListPopupWindow: ListPopupWindow? = null
    private var mTocListAdapter: TocListAdapter? = null

    private var mIsActionBarVisible = true
    private var currentChapter: Int = 0

    override val layoutId: Int
        get() = R.layout.activity_read_epub

    override fun setupActivityComponent(appComponent: AppComponent?) {

    }

    override fun initToolBar() {

        mCommonToolbar!!.viewTreeObserver
                .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                            mCommonToolbar!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        } else {
                            mCommonToolbar!!.viewTreeObserver.removeGlobalOnLayoutListener(this)
                        }
                        hideToolBarIfVisible()
                    }
                })

        showDialog()
    }

    override fun initDatas() {
        mFilePath = Uri.decode(intent.dataString!!.replace("file://", ""))
        mFileName = mFilePath!!.substring(mFilePath!!.lastIndexOf("/") + 1, mFilePath!!.lastIndexOf("."))
    }

    override fun configViews() {

        object : AsyncTask<Void, Void, Void>() {

            override fun doInBackground(vararg params: Void): Void? {
                loadBook()
                return null
            }

            override fun onPostExecute(aVoid: Void) {
                initPager()

                initTocList()
            }
        }.execute()

    }

    private fun loadBook() {

        try {
            // 打开书籍
            val reader = EpubReader()
            val `is` = FileInputStream(mFilePath)
            mBook = reader.readEpub(`is`)

            mTocReferences = mBook!!.tableOfContents.tocReferences as ArrayList<TOCReference>
            mSpineReferences = mBook!!.spine.spineReferences

            setSpineReferenceTitle()

            // 解压epub至缓存目录
            FileUtils.unzipFile(mFilePath, Constant.PATH_EPUB + "/" + mFileName)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun initPager() {
        viewpager!!.setOnPageChangeListener(object : DirectionalViewpager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                mTocListAdapter!!.setCurrentChapter(position + 1)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        if (mBook != null && mSpineReferences != null && mTocReferences != null) {

            mAdapter = EPubReaderAdapter(supportFragmentManager,
                    mSpineReferences, mBook, mFileName, mIsSmilParsed)
            viewpager!!.adapter = mAdapter
        }

        hideDialog()
    }

    private fun initTocList() {
        mTocListAdapter = TocListAdapter(this, mChapterList, "", 1)
        mTocListAdapter!!.setEpub(true)
        mTocListPopupWindow = ListPopupWindow(this)
        mTocListPopupWindow!!.setAdapter(mTocListAdapter)
        mTocListPopupWindow!!.width = ViewGroup.LayoutParams.MATCH_PARENT
        mTocListPopupWindow!!.height = ViewGroup.LayoutParams.WRAP_CONTENT
        mTocListPopupWindow!!.anchorView = mCommonToolbar
        mTocListPopupWindow!!.setOnItemClickListener { parent, view, position, id ->
            mTocListPopupWindow!!.dismiss()
            currentChapter = position + 1
            mTocListAdapter!!.setCurrentChapter(currentChapter)
            viewpager!!.currentItem = position
        }
        mTocListPopupWindow!!.setOnDismissListener { toolbarAnimateHide() }
    }

    private fun setSpineReferenceTitle() {
        val srSize = mSpineReferences!!.size
        val trSize = mTocReferences!!.size
        for (j in 0 until srSize) {
            val href = mSpineReferences!![j].resource.href
            for (i in 0 until trSize) {
                if (mTocReferences!![i].resource.href.equals(href, ignoreCase = true)) {
                    mSpineReferences!![j].resource.title = mTocReferences!![i].title
                    break
                } else {
                    mSpineReferences!![j].resource.title = ""
                }
            }
        }

        for (i in 0 until trSize) {
            val resource = mTocReferences!![i].resource
            if (resource != null) {
                mChapterList.add(BookMixAToc.mixToc.Chapters(resource.title, resource.href))
            }
        }
    }

    override fun getPageHref(position: Int): String {
        var pageHref = mTocReferences!![position].resource.href
        val opfpath = FileUtils.getPathOPF(FileUtils.getEpubFolderPath(mFileName))
        if (FileUtils.checkOPFInRootDirectory(FileUtils.getEpubFolderPath(mFileName))) {
            pageHref = FileUtils.getEpubFolderPath(mFileName) + "/" + pageHref
        } else {
            pageHref = FileUtils.getEpubFolderPath(mFileName) + "/" + opfpath + "/" + pageHref
        }
        return pageHref
    }

    override fun toggleToolBarVisible() {
        if (mIsActionBarVisible) {
            toolbarAnimateHide()
        } else {
            toolbarAnimateShow(1)
        }
    }

    override fun hideToolBarIfVisible() {
        if (mIsActionBarVisible) {
            toolbarAnimateHide()
        }
    }

    private fun toolbarAnimateShow(verticalOffset: Int) {
        showStatusBar()
        mCommonToolbar!!.animate()
                .translationY(0f)
                .setInterpolator(LinearInterpolator())
                .setDuration(180)
                .setListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationStart(animation: Animator) {
                        toolbarSetElevation((if (verticalOffset == 0) 0 else 1).toFloat())
                    }
                })

        Handler().postDelayed({
            runOnUiThread {
                if (mIsActionBarVisible) {
                    toolbarAnimateHide()
                }
            }
        }, 10000)

        mIsActionBarVisible = true
    }

    private fun toolbarAnimateHide() {
        if (mIsActionBarVisible) {
            mCommonToolbar!!.animate()
                    .translationY((-mCommonToolbar!!.height).toFloat())
                    .setInterpolator(LinearInterpolator())
                    .setDuration(180)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            toolbarSetElevation(0f)
                            hideStatusBar()
                            if (mTocListPopupWindow != null && mTocListPopupWindow!!.isShowing) {
                                mTocListPopupWindow!!.dismiss()
                            }
                        }
                    })
            mIsActionBarVisible = false
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun toolbarSetElevation(elevation: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCommonToolbar!!.elevation = elevation
        }
    }

    @OnClick(R.id.toolbar_menu)
    fun showMenu() {
        if (!mTocListPopupWindow!!.isShowing) {
            mTocListPopupWindow!!.inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
            mTocListPopupWindow!!.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            mTocListPopupWindow!!.show()
            mTocListPopupWindow!!.setSelection(currentChapter - 1)
            mTocListPopupWindow!!.listView!!.isFastScrollEnabled = true
        }
    }

    override fun onBackPressed() {
        if (mTocListPopupWindow != null && mTocListPopupWindow!!.isShowing) {
            mTocListPopupWindow!!.dismiss()
        } else {
            super.onBackPressed()
        }
    }

    companion object {

        fun start(context: Context, filePath: String) {
            val intent = Intent(context, ReadEPubActivity::class.java)
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.fromFile(File(filePath))
            context.startActivity(intent)
        }
    }
}
