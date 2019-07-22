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
package com.justwayward.reader.view.recyclerview

import android.content.Context
import android.content.res.TypedArray
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.TextView

import com.justwayward.reader.R
import com.justwayward.reader.utils.NetworkUtils
import com.justwayward.reader.view.recyclerview.adapter.RecyclerArrayAdapter
import com.justwayward.reader.view.recyclerview.decoration.DividerDecoration
import com.justwayward.reader.view.recyclerview.swipe.OnRefreshListener
import com.justwayward.reader.view.recyclerview.swipe.SwipeRefreshLayout

import java.util.ArrayList


class EasyRecyclerView @JvmOverloads constructor(private val mContext: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : FrameLayout(mContext, attrs, defStyle) {
    var recyclerView: RecyclerView? = null
        protected set
    protected var tipView: TextView
    protected var mProgressView: ViewGroup
    protected var mEmptyView: ViewGroup
    protected var mErrorView: ViewGroup
    private var mProgressId: Int = 0
    private var mEmptyId: Int = 0
    private var mErrorId: Int = 0

    protected var mClipToPadding: Boolean = false
    protected var mPadding: Int = 0
    protected var mPaddingTop: Int = 0
    protected var mPaddingBottom: Int = 0
    protected var mPaddingLeft: Int = 0
    protected var mPaddingRight: Int = 0
    protected var mScrollbarStyle: Int = 0
    protected var mScrollbar: Int = 0

    protected var mInternalOnScrollListener: RecyclerView.OnScrollListener
    protected var mExternalOnScrollListener: RecyclerView.OnScrollListener? = null

    var swipeToRefresh: SwipeRefreshLayout
        protected set
    protected var mRefreshListener: OnRefreshListener? = null

    var decorations: MutableList<RecyclerView.ItemDecoration> = ArrayList()

    val isTipViewVisible: Boolean
        get() = tipView.visibility == View.VISIBLE

    /**
     * @return the recycler adapter
     */
    /**
     * 设置适配器，关闭所有副view。展示recyclerView
     * 适配器有更新，自动关闭所有副view。根据条数判断是否展示EmptyView
     *
     * @param adapter
     */
    var adapter: RecyclerView.Adapter<*>?
        get() = recyclerView!!.adapter
        set(adapter) {
            recyclerView!!.adapter = adapter
            adapter.registerAdapterDataObserver(EasyDataObserver(this))
            showRecycler()
        }


    /**
     * @return inflated error view or null
     */
    var errorView: View?
        get() = if (mErrorView.childCount > 0) mErrorView.getChildAt(0) else null
        set(errorView) {
            mErrorView.removeAllViews()
            mErrorView.addView(errorView)
        }

    /**
     * @return inflated progress view or null
     */
    var progressView: View?
        get() = if (mProgressView.childCount > 0) mProgressView.getChildAt(0) else null
        set(progressView) {
            mProgressView.removeAllViews()
            mProgressView.addView(progressView)
        }


    /**
     * @return inflated empty view or null
     */
    var emptyView: View?
        get() = if (mEmptyView.childCount > 0) mEmptyView.getChildAt(0) else null
        set(emptyView) {
            mEmptyView.removeAllViews()
            mEmptyView.addView(emptyView)
        }

    init {
        if (attrs != null)
            initAttrs(attrs)
        initView()
    }

    protected fun initAttrs(attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.superrecyclerview)
        try {
            mClipToPadding = a.getBoolean(R.styleable.superrecyclerview_recyclerClipToPadding, false)
            mPadding = a.getDimension(R.styleable.superrecyclerview_recyclerPadding, -1.0f).toInt()
            mPaddingTop = a.getDimension(R.styleable.superrecyclerview_recyclerPaddingTop, 0.0f).toInt()
            mPaddingBottom = a.getDimension(R.styleable.superrecyclerview_recyclerPaddingBottom, 0.0f).toInt()
            mPaddingLeft = a.getDimension(R.styleable.superrecyclerview_recyclerPaddingLeft, 0.0f).toInt()
            mPaddingRight = a.getDimension(R.styleable.superrecyclerview_recyclerPaddingRight, 0.0f).toInt()
            mScrollbarStyle = a.getInteger(R.styleable.superrecyclerview_scrollbarStyle, -1)
            mScrollbar = a.getInteger(R.styleable.superrecyclerview_scrollbars, -1)

            mEmptyId = a.getResourceId(R.styleable.superrecyclerview_layout_empty, 0)
            mProgressId = a.getResourceId(R.styleable.superrecyclerview_layout_progress, 0)
            mErrorId = a.getResourceId(R.styleable.superrecyclerview_layout_error, R.layout.common_net_error_view)
        } finally {
            a.recycle()
        }
    }

    private fun initView() {
        if (isInEditMode) {
            return
        }
        //生成主View
        val v = LayoutInflater.from(context).inflate(R.layout.common_recyclerview, this)
        swipeToRefresh = v.findViewById<View>(R.id.ptr_layout) as SwipeRefreshLayout
        swipeToRefresh.isEnabled = false

        mProgressView = v.findViewById<View>(R.id.progress) as ViewGroup
        if (mProgressId != 0) LayoutInflater.from(context).inflate(mProgressId, mProgressView)
        mEmptyView = v.findViewById<View>(R.id.empty) as ViewGroup
        if (mEmptyId != 0) LayoutInflater.from(context).inflate(mEmptyId, mEmptyView)
        mErrorView = v.findViewById<View>(R.id.error) as ViewGroup
        if (mErrorId != 0) LayoutInflater.from(context).inflate(mErrorId, mErrorView)
        initRecyclerView(v)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return swipeToRefresh.dispatchTouchEvent(ev)
    }

    /**
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    fun setRecyclerPadding(left: Int, top: Int, right: Int, bottom: Int) {
        this.mPaddingLeft = left
        this.mPaddingTop = top
        this.mPaddingRight = right
        this.mPaddingBottom = bottom
        recyclerView!!.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom)
    }

    override fun setClipToPadding(isClip: Boolean) {
        recyclerView!!.clipToPadding = isClip
    }

    fun setEmptyView(emptyView: Int) {
        mEmptyView.removeAllViews()
        LayoutInflater.from(context).inflate(emptyView, mEmptyView)
    }

    fun setProgressView(progressView: Int) {
        mProgressView.removeAllViews()
        LayoutInflater.from(context).inflate(progressView, mProgressView)
    }

    fun setErrorView(errorView: Int) {
        mErrorView.removeAllViews()
        LayoutInflater.from(context).inflate(errorView, mErrorView)
    }

    fun scrollToPosition(position: Int) {
        recyclerView!!.scrollToPosition(position)
    }

    /**
     * Implement this method to customize the AbsListView
     */
    protected fun initRecyclerView(view: View) {
        recyclerView = view.findViewById<View>(android.R.id.list) as RecyclerView
        tipView = view.findViewById<View>(R.id.tvTip) as TextView
        setItemAnimator(null)
        if (recyclerView != null) {
            recyclerView!!.setHasFixedSize(true)
            recyclerView!!.clipToPadding = mClipToPadding
            mInternalOnScrollListener = object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (mExternalOnScrollListener != null)
                        mExternalOnScrollListener!!.onScrolled(recyclerView, dx, dy)

                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (mExternalOnScrollListener != null)
                        mExternalOnScrollListener!!.onScrollStateChanged(recyclerView, newState)

                }
            }
            recyclerView!!.addOnScrollListener(mInternalOnScrollListener)

            if (mPadding.toFloat() != -1.0f) {
                recyclerView!!.setPadding(mPadding, mPadding, mPadding, mPadding)
            } else {
                recyclerView!!.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom)
            }
            if (mScrollbarStyle != -1) {
                recyclerView!!.scrollBarStyle = mScrollbarStyle
            }
            when (mScrollbar) {
                0 -> isVerticalScrollBarEnabled = false
                1 -> isHorizontalScrollBarEnabled = false
                2 -> {
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                }
            }
        }
    }

    override fun setVerticalScrollBarEnabled(verticalScrollBarEnabled: Boolean) {
        recyclerView!!.isVerticalScrollBarEnabled = verticalScrollBarEnabled
    }

    override fun setHorizontalScrollBarEnabled(horizontalScrollBarEnabled: Boolean) {
        recyclerView!!.isHorizontalScrollBarEnabled = horizontalScrollBarEnabled
    }

    /**
     * Set the layout manager to the recycler
     *
     * @param manager
     */
    fun setLayoutManager(manager: RecyclerView.LayoutManager) {
        recyclerView!!.layoutManager = manager
    }

    /**
     * Set the ItemDecoration to the recycler
     *
     * @param color
     * @param height
     * @param paddingLeft
     * @param paddingRight
     */
    fun setItemDecoration(color: Int, height: Int, paddingLeft: Int, paddingRight: Int) {
        val itemDecoration = DividerDecoration(color, height, paddingLeft, paddingRight)
        itemDecoration.setDrawLastItem(false)
        decorations.add(itemDecoration)
        recyclerView!!.addItemDecoration(itemDecoration)
    }


    class EasyDataObserver(private val recyclerView: EasyRecyclerView) : AdapterDataObserver() {

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            super.onItemRangeChanged(positionStart, itemCount)
            update()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            update()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            update()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount)
            update()
        }

        override fun onChanged() {
            super.onChanged()
            update()
        }

        //自动更改Container的样式
        private fun update() {
            log("update")
            val count: Int
            if (recyclerView.adapter is RecyclerArrayAdapter<*>) {
                count = (recyclerView.adapter as RecyclerArrayAdapter<*>).count
            } else {
                count = recyclerView.adapter!!.itemCount
            }

            if (count == 0 && !NetworkUtils.isAvailable(recyclerView.context)) {
                recyclerView.showError()
                return
            }

            if (count == 0 && (recyclerView.adapter as RecyclerArrayAdapter<*>).headerCount == 0) {
                log("no data:" + "show empty")
                recyclerView.showEmpty()
            } else {
                log("has data")
                recyclerView.showRecycler()
            }
        }
    }

    /**
     * 设置适配器，关闭所有副view。展示进度条View
     * 适配器有更新，自动关闭所有副view。根据条数判断是否展示EmptyView
     *
     * @param adapter
     */
    fun setAdapterWithProgress(adapter: RecyclerView.Adapter<*>) {
        recyclerView!!.adapter = adapter
        adapter.registerAdapterDataObserver(EasyDataObserver(this))
        //只有Adapter为空时才显示ProgressView
        if (adapter is RecyclerArrayAdapter<*>) {
            if (adapter.count == 0) {
                showProgress()
            } else {
                showRecycler()
            }
        } else {
            if (adapter.itemCount == 0) {
                showProgress()
            } else {
                showRecycler()
            }
        }
    }

    /**
     * Remove the adapter from the recycler
     */
    fun clear() {
        recyclerView!!.adapter = null
    }


    private fun hideAll() {
        mEmptyView.visibility = View.GONE
        mProgressView.visibility = View.GONE
        mErrorView.visibility = View.GONE
        //        mPtrLayout.setRefreshing(false);
        recyclerView!!.visibility = View.INVISIBLE
    }


    fun showError() {
        log("showError")
        if (mErrorView.childCount > 0) {
            hideAll()
            mErrorView.visibility = View.VISIBLE
        } else {
            showRecycler()
        }

    }

    fun showEmpty() {
        log("showEmpty")
        if (mEmptyView.childCount > 0) {
            hideAll()
            mEmptyView.visibility = View.VISIBLE
        } else {
            showRecycler()
        }
    }


    fun showProgress() {
        log("showProgress")
        if (mProgressView.childCount > 0) {
            hideAll()
            mProgressView.visibility = View.VISIBLE
        } else {
            showRecycler()
        }
    }


    fun showRecycler() {
        log("showRecycler")
        hideAll()
        recyclerView!!.visibility = View.VISIBLE
    }

    fun showTipViewAndDelayClose(tip: String) {
        tipView.text = tip
        val mShowAction = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -1.0f, Animation.RELATIVE_TO_SELF, 0.0f)
        mShowAction.duration = 500
        tipView.startAnimation(mShowAction)
        tipView.visibility = View.VISIBLE

        tipView.postDelayed({
            val mHiddenAction = TranslateAnimation(Animation.RELATIVE_TO_SELF,
                    0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                    -1.0f)
            mHiddenAction.duration = 500
            tipView.startAnimation(mHiddenAction)
            tipView.visibility = View.GONE
        }, 2200)
    }

    fun showTipView(tip: String) {
        tipView.text = tip
        val mShowAction = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -1.0f, Animation.RELATIVE_TO_SELF, 0.0f)
        mShowAction.duration = 500
        tipView.startAnimation(mShowAction)
        tipView.visibility = View.VISIBLE
    }

    fun hideTipView(delayMillis: Long) {
        tipView.postDelayed({
            val mHiddenAction = TranslateAnimation(Animation.RELATIVE_TO_SELF,
                    0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                    -1.0f)
            mHiddenAction.duration = 500
            tipView.startAnimation(mHiddenAction)
            tipView.visibility = View.GONE
        }, delayMillis)
    }

    fun setTipViewText(tip: String) {
        if (!isTipViewVisible)
            showTipView(tip)
        else
            tipView.text = tip
    }


    /**
     * Set the listener when refresh is triggered and enable the SwipeRefreshLayout
     *
     * @param listener
     */
    fun setRefreshListener(listener: OnRefreshListener) {
        swipeToRefresh.isEnabled = true
        swipeToRefresh.setOnRefreshListener(listener)
        this.mRefreshListener = listener
    }

    fun setRefreshing(isRefreshing: Boolean) {
        swipeToRefresh.post {
            if (isRefreshing) { // 避免刷新的loadding和progressview 同时显示
                mProgressView.visibility = View.GONE
            }
            swipeToRefresh.isRefreshing = isRefreshing
        }
    }

    fun setRefreshing(isRefreshing: Boolean, isCallbackListener: Boolean) {
        swipeToRefresh.post {
            swipeToRefresh.isRefreshing = isRefreshing
            if (isRefreshing && isCallbackListener && mRefreshListener != null) {
                mRefreshListener!!.onRefresh()
            }
        }
    }

    /**
     * Set the colors for the SwipeRefreshLayout states
     *
     * @param colRes
     */
    fun setRefreshingColorResources(@ColorRes vararg colRes: Int) {
        swipeToRefresh.setColorSchemeResources(*colRes)
    }

    /**
     * Set the colors for the SwipeRefreshLayout states
     *
     * @param col
     */
    fun setRefreshingColor(vararg col: Int) {
        swipeToRefresh.setColorSchemeColors(*col)
    }

    /**
     * Set the scroll listener for the recycler
     *
     * @param listener
     */
    fun setOnScrollListener(listener: RecyclerView.OnScrollListener) {
        mExternalOnScrollListener = listener
    }

    /**
     * Add the onItemTouchListener for the recycler
     *
     * @param listener
     */
    fun addOnItemTouchListener(listener: RecyclerView.OnItemTouchListener) {
        recyclerView!!.addOnItemTouchListener(listener)
    }

    /**
     * Remove the onItemTouchListener for the recycler
     *
     * @param listener
     */
    fun removeOnItemTouchListener(listener: RecyclerView.OnItemTouchListener) {
        recyclerView!!.removeOnItemTouchListener(listener)
    }


    override fun setOnTouchListener(listener: View.OnTouchListener) {
        recyclerView!!.setOnTouchListener(listener)
    }

    fun setItemAnimator(animator: RecyclerView.ItemAnimator?) {
        recyclerView!!.itemAnimator = animator
    }

    fun addItemDecoration(itemDecoration: RecyclerView.ItemDecoration) {
        recyclerView!!.addItemDecoration(itemDecoration)
    }

    fun addItemDecoration(itemDecoration: RecyclerView.ItemDecoration, index: Int) {
        recyclerView!!.addItemDecoration(itemDecoration, index)
    }

    fun removeItemDecoration(itemDecoration: RecyclerView.ItemDecoration) {
        recyclerView!!.removeItemDecoration(itemDecoration)
    }

    fun removeAllItemDecoration() {
        for (decoration in decorations) {
            recyclerView!!.removeItemDecoration(decoration)
        }
    }

    companion object {

        val TAG = "EasyRecyclerView"
        var DEBUG = false

        private fun log(content: String) {
            if (DEBUG) {
                Log.i(TAG, content)
            }
        }
    }

}
