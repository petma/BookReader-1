package com.justwayward.reader.ui.fragment

import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseFragment
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.ui.activity.ReadEPubActivity
import com.justwayward.reader.view.epubview.ObservableWebView
import com.justwayward.reader.view.epubview.VerticalSeekbar

import butterknife.Bind
import nl.siegmann.epublib.domain.Book

/**
 * @author yuyh.
 * @date 2016/12/14.
 */
class EPubReaderFragment : BaseFragment() {

    @Bind(R.id.scrollSeekbar)
    internal var mScrollSeekbar: VerticalSeekbar? = null
    @Bind(R.id.contentWebView)
    internal var mWebview: ObservableWebView? = null

    private var mPosition = -1
    private var mBook: Book? = null
    private var mEpubFileName: String? = null
    private var mIsSmilAvailable: Boolean = false

    private var mScrollY: Int = 0

    private var activity: ReadEPubActivity? = null

    private var mFadeInAnimation: Animation? = null
    private var mFadeOutAnimation: Animation? = null
    private val mHandler = Handler()

    override val layoutResId: Int
        get() = R.layout.fragment_epub_reader

    private val mHideSeekbarRunnable = Runnable { fadeoutSeekbarIfVisible() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        if (state != null && state.containsKey(BUNDLE_POSITION)
                && state.containsKey(BUNDLE_BOOK)) {
            mPosition = state.getInt(BUNDLE_POSITION)
            mBook = state.getSerializable(BUNDLE_BOOK) as Book
            mEpubFileName = state.getString(BUNDLE_EPUB_FILE_NAME)
            mIsSmilAvailable = state.getBoolean(BUNDLE_IS_SMIL_AVAILABLE)
        }
        return super.onCreateView(inflater, container, state)
    }

    override fun setupActivityComponent(appComponent: AppComponent?) {

    }

    override fun attachView() {

    }

    override fun initDatas() {
        activity = getActivity() as ReadEPubActivity?

        mPosition = arguments!!.getInt(BUNDLE_POSITION)
        mBook = arguments!!.getSerializable(BUNDLE_BOOK) as Book
        mEpubFileName = arguments!!.getString(BUNDLE_EPUB_FILE_NAME)
        mIsSmilAvailable = arguments!!.getBoolean(BUNDLE_IS_SMIL_AVAILABLE)
    }

    override fun configViews() {
        initAnimations()

        initSeekbar()

        initWebView()
    }

    private fun initSeekbar() {

        mScrollSeekbar!!.setFragment(this)
        if (mScrollSeekbar!!.progressDrawable != null)
            mScrollSeekbar!!.progressDrawable
                    .setColorFilter(ContextCompat.getColor(mContext!!, R.color.colorAccent),
                            PorterDuff.Mode.SRC_IN)

        mScrollSeekbar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mWebview!!.postDelayed({ mWebview!!.scrollY = (mWebview!!.contentHeight.toFloat() * mWebview!!.scale * progress.toFloat() / seekBar.max).toInt() }, 200)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

    }

    private fun initWebView() {

        mWebview!!.setFragment(this)
        mWebview!!.settings.javaScriptEnabled = true
        mWebview!!.isVerticalScrollBarEnabled = false
        mWebview!!.settings.allowFileAccess = true
        mWebview!!.isHorizontalScrollBarEnabled = false
        mWebview!!.addJavascriptInterface(this, "Highlight")

        mWebview!!.setScrollListener { percent ->
            if (mWebview!!.scrollY != 0) {
                mScrollY = mWebview!!.scrollY
            }

            val height = Math.floor((mWebview!!.contentHeight * mWebview!!.scale).toDouble()).toInt()
            val webViewHeight = mWebview!!.measuredHeight
            mScrollSeekbar!!.max = height - webViewHeight

            mScrollSeekbar!!.progress = percent
        }

        mWebview!!.settings.defaultTextEncodingName = "utf-8"

        val herf = activity!!.getPageHref(mPosition)

        mWebview!!.loadUrl("file://$herf")
    }

    private fun initAnimations() {
        mFadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein)
        mFadeInAnimation!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                mScrollSeekbar!!.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation) {}

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        mFadeOutAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fadeout)
        mFadeOutAnimation!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                mScrollSeekbar!!.visibility = View.INVISIBLE
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
    }

    fun fadeInSeekbarIfInvisible() {
        if (mScrollSeekbar != null && !isVisible(mScrollSeekbar)) {
            mScrollSeekbar!!.startAnimation(mFadeInAnimation)
        }
    }

    fun fadeoutSeekbarIfVisible() {
        if (mScrollSeekbar != null && isVisible(mScrollSeekbar)) {
            mScrollSeekbar!!.startAnimation(mFadeOutAnimation)
        }
    }

    fun removeCallback() {
        mHandler.removeCallbacks(mHideSeekbarRunnable)
    }

    fun startCallback() {
        mHandler.postDelayed(mHideSeekbarRunnable, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    companion object {

        private val BUNDLE_POSITION = "position"
        private val BUNDLE_BOOK = "book"
        private val BUNDLE_EPUB_FILE_NAME = "filename"
        private val BUNDLE_IS_SMIL_AVAILABLE = "smilavailable"

        fun newInstance(position: Int, book: Book, epubFileName: String, isSmilAvailable: Boolean): Fragment {
            val fragment = EPubReaderFragment()
            val args = Bundle()
            args.putInt(BUNDLE_POSITION, position)
            args.putSerializable(BUNDLE_BOOK, book)
            args.putString(BUNDLE_EPUB_FILE_NAME, epubFileName)
            args.putSerializable(BUNDLE_IS_SMIL_AVAILABLE, isSmilAvailable)
            fragment.arguments = args
            return fragment
        }
    }
}
