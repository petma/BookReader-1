package com.justwayward.reader.view.epubview

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MotionEvent
import android.view.View
import android.webkit.WebView

import com.justwayward.reader.ui.activity.ReadEPubActivity
import com.justwayward.reader.ui.fragment.EPubReaderFragment

/**
 * @author yuyh.
 * @date 2016/12/13.
 */
class ObservableWebView : WebView {

    private var mActivityCallback: ReaderCallback? = null
    private var fragment: EPubReaderFragment? = null

    private val MOVE_THRESHOLD_DP: Float
    private var mMoveOccured = false

    private var mDownPosX: Float = 0.toFloat()
    private var mDownPosY: Float = 0.toFloat()

    private var mScrollListener: ScrollListener? = null
    private var mSizeChangedListener: SizeChangedListener? = null

    val contentHeightVal: Int
        get() = Math.floor((contentHeight * scale).toDouble()).toInt()

    val webviewHeight: Int
        get() = this.measuredHeight

    interface ScrollListener {
        fun onScrollChange(percent: Int)
    }

    interface SizeChangedListener {
        fun onSizeChanged(height: Int)
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {

        MOVE_THRESHOLD_DP = 20 * resources.displayMetrics.density
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet,
                defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    fun setScrollListener(listener: ScrollListener) {
        mScrollListener = listener
    }

    fun setSizeChangedListener(listener: SizeChangedListener) {
        mSizeChangedListener = listener
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        mActivityCallback = context as ReadEPubActivity
        mActivityCallback!!.hideToolBarIfVisible()
        if (mScrollListener != null)
            mScrollListener!!.onScrollChange(t)
        super.onScrollChanged(l, t, oldl, oldt)
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        super.onSizeChanged(w, h, ow, oh)
        if (mSizeChangedListener != null) {
            mSizeChangedListener!!.onSizeChanged(h)
        }
    }

    override fun startActionMode(callback: ActionMode.Callback, type: Int): ActionMode {
        return this.dummyActionMode()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {


        if (mActivityCallback == null)
            mActivityCallback = context as ReadEPubActivity

        val action = event.action
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mMoveOccured = false
                mDownPosX = event.x
                mDownPosY = event.y
                fragment!!.removeCallback()
            }
            MotionEvent.ACTION_UP -> {
                if (!mMoveOccured) {
                    mActivityCallback!!.toggleToolBarVisible()
                }

                fragment!!.startCallback()
            }
            MotionEvent.ACTION_MOVE -> if (Math.abs(event.x - mDownPosX) > MOVE_THRESHOLD_DP || Math.abs(event.y - mDownPosY) > MOVE_THRESHOLD_DP) {
                mMoveOccured = true
                fragment!!.fadeInSeekbarIfInvisible()
            }
        }

        return super.dispatchTouchEvent(event)
    }

    override fun startActionMode(callback: ActionMode.Callback): ActionMode {
        return this.dummyActionMode()
    }

    fun dummyActionMode(): ActionMode {
        return object : ActionMode() {
            override fun setTitle(title: CharSequence) {}

            override fun setTitle(resId: Int) {}

            override fun setSubtitle(subtitle: CharSequence) {}

            override fun setSubtitle(resId: Int) {}

            override fun setCustomView(view: View) {}

            override fun invalidate() {}

            override fun finish() {}

            override fun getMenu(): Menu? {
                return null
            }

            override fun getTitle(): CharSequence? {
                return null
            }

            override fun getSubtitle(): CharSequence? {
                return null
            }

            override fun getCustomView(): View? {
                return null
            }

            override fun getMenuInflater(): MenuInflater? {
                return null
            }
        }
    }

    fun setFragment(fragment: EPubReaderFragment) {
        this.fragment = fragment
    }
}