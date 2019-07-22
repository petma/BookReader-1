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
package com.justwayward.reader.view.recyclerview.adapter

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

import com.justwayward.reader.view.recyclerview.EasyRecyclerView


/**
 * Created by Mr.Jude on 2015/8/18.
 */
class DefaultEventDelegate(private val adapter: RecyclerArrayAdapter<*>) : EventDelegate {
    private val footer: EventFooter

    private var onLoadMoreListener: OnLoadMoreListener? = null

    private var hasData = false
    private var isLoadingMore = false

    private var hasMore = false
    private var hasNoMore = false
    private var hasError = false

    private var status = STATUS_INITIAL

    init {
        footer = EventFooter()
        adapter.addFooter(footer)
    }

    fun onMoreViewShowed() {
        log("onMoreViewShowed")
        if (!isLoadingMore && onLoadMoreListener != null) {
            isLoadingMore = true
            onLoadMoreListener!!.onLoadMore()
        }
    }

    fun onErrorViewShowed() {
        resumeLoadMore()
    }

    //-------------------5个状态触发事件-------------------
    override fun addData(length: Int) {
        log("addData$length")
        if (hasMore) {
            if (length == 0) {
                //当添加0个时，认为已结束加载到底
                if (status == STATUS_INITIAL || status == STATUS_MORE) {
                    footer.showNoMore()
                }
            } else {
                //当Error或初始时。添加数据，如果有More则还原。
                if (hasMore && (status == STATUS_INITIAL || status == STATUS_ERROR)) {
                    footer.showMore()
                }
                hasData = true
            }
        } else {
            if (hasNoMore) {
                footer.showNoMore()
                status = STATUS_NOMORE
            }
        }
        isLoadingMore = false
    }

    override fun clear() {
        log("clear")
        hasData = false
        status = STATUS_INITIAL
        footer.hide()
        isLoadingMore = false
    }

    override fun stopLoadMore() {
        log("stopLoadMore")
        footer.showNoMore()
        status = STATUS_NOMORE
        isLoadingMore = false
    }

    override fun pauseLoadMore() {
        log("pauseLoadMore")
        footer.showError()
        status = STATUS_ERROR
        isLoadingMore = false
    }

    override fun resumeLoadMore() {
        isLoadingMore = false
        footer.showMore()
        onMoreViewShowed()
    }

    //-------------------3种View设置-------------------

    override fun setMore(view: View, listener: OnLoadMoreListener) {
        this.footer.setMoreView(view)
        this.onLoadMoreListener = listener
        hasMore = true
        log("setMore")
    }

    override fun setNoMore(view: View) {
        this.footer.setNoMoreView(view)
        hasNoMore = true
        log("setNoMore")
    }

    override fun setErrorMore(view: View) {
        this.footer.setErrorView(view)
        hasError = true
        log("setErrorMore")
    }


    private inner class EventFooter : RecyclerArrayAdapter.ItemView {
        private val container: FrameLayout?
        private var moreView: View? = null
        private var noMoreView: View? = null
        private var errorView: View? = null

        private var flag = Hide

        init {
            container = FrameLayout(adapter.context!!)
            container!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        override fun onCreateView(parent: ViewGroup): View? {
            log("onCreateView")
            return container
        }

        override fun onBindView(headerView: View) {
            log("onBindView")
            when (flag) {
                ShowMore -> onMoreViewShowed()
                ShowError -> onErrorViewShowed()
            }
        }

        fun refreshStatus() {
            if (container != null) {
                if (flag == Hide) {
                    container.visibility = View.GONE
                    return
                }
                if (container.visibility != View.VISIBLE) container.visibility = View.VISIBLE
                var view: View? = null
                when (flag) {
                    ShowMore -> view = moreView
                    ShowError -> view = errorView
                    ShowNoMore -> view = noMoreView
                }
                if (view == null) {
                    hide()
                    return
                }
                if (view.parent == null) container.addView(view)
                for (i in 0 until container.childCount) {
                    if (container.getChildAt(i) === view)
                        view.visibility = View.VISIBLE
                    else
                        container.getChildAt(i).visibility = View.GONE
                }
            }
        }

        fun showError() {
            flag = ShowError
            refreshStatus()
        }

        fun showMore() {
            flag = ShowMore
            refreshStatus()
        }

        fun showNoMore() {
            flag = ShowNoMore
            refreshStatus()
        }

        //初始化
        fun hide() {
            flag = Hide
            refreshStatus()
        }

        fun setMoreView(moreView: View) {
            this.moreView = moreView
        }

        fun setNoMoreView(noMoreView: View) {
            this.noMoreView = noMoreView
        }

        fun setErrorView(errorView: View) {
            this.errorView = errorView
        }

        companion object {
            val Hide = 0
            val ShowMore = 1
            val ShowError = 2
            val ShowNoMore = 3
        }
    }

    companion object {
        private val STATUS_INITIAL = 291
        private val STATUS_MORE = 260
        private val STATUS_NOMORE = 408
        private val STATUS_ERROR = 732

        private fun log(content: String) {
            if (EasyRecyclerView.DEBUG) {
                Log.i(EasyRecyclerView.TAG, content)
            }
        }
    }
}
