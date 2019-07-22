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

import android.content.Intent
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseActivity
import com.justwayward.reader.bean.BooksByTag
import com.justwayward.reader.common.OnRvItemClickListener
import com.justwayward.reader.component.AppComponent

import com.justwayward.reader.ui.adapter.BooksByTagAdapter
import com.justwayward.reader.ui.contract.BooksByTagContract
import com.justwayward.reader.ui.presenter.BooksByTagPresenter
import com.justwayward.reader.view.SupportDividerItemDecoration

import java.util.ArrayList

import javax.inject.Inject


/**
 * Created by Administrator on 2016/8/7.
 */
class BooksByTagActivity : BaseActivity(), BooksByTagContract.View, OnRvItemClickListener<BooksByTag.TagBook> {

    @Bind(R.id.refreshLayout)
    internal var refreshLayout: SwipeRefreshLayout? = null
    @Bind(R.id.recyclerview)
    internal var mRecyclerView: RecyclerView? = null
    private var linearLayoutManager: LinearLayoutManager? = null

    @Inject
    internal var mPresenter: BooksByTagPresenter? = null

    private var mAdapter: BooksByTagAdapter? = null
    private val mList = ArrayList<BooksByTag.TagBook>()

    private var tag: String? = null
    private var current = 0

    override val layoutId: Int
        get() = R.layout.activity_books_by_tag

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerBookComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun initToolBar() {
        mCommonToolbar!!.title = intent.getStringExtra("tag")
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
    }

    override fun initDatas() {
        tag = intent.getStringExtra("tag")
    }

    override fun configViews() {
        refreshLayout!!.setOnRefreshListener(RefreshListener())

        mRecyclerView!!.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(this)
        mRecyclerView!!.layoutManager = linearLayoutManager
        mRecyclerView!!.addItemDecoration(SupportDividerItemDecoration(mContext, LinearLayoutManager.VERTICAL))
        mAdapter = BooksByTagAdapter(mContext, mList, this)
        mRecyclerView!!.adapter = mAdapter
        mRecyclerView!!.addOnScrollListener(RefreshListener())

        mPresenter!!.attachView(this)
        mPresenter!!.getBooksByTag(tag, current.toString() + "", (current + 10).toString() + "")
    }


    override fun showBooksByTag(list: List<BooksByTag.TagBook>, isRefresh: Boolean) {
        if (isRefresh)
            mList.clear()
        mList.addAll(list)
        current = mList.size
        mAdapter!!.notifyDataSetChanged()
    }

    override fun onLoadComplete(isSuccess: Boolean, msg: String) {
        refreshLayout!!.isRefreshing = false
    }

    override fun onItemClick(view: View, position: Int, data: BooksByTag.TagBook) {
        startActivity(Intent(this@BooksByTagActivity, BookDetailActivity::class.java)
                .putExtra("bookId", data._id))
    }

    override fun showError() {

    }

    override fun complete() {
        refreshLayout!!.isRefreshing = false
    }

    private inner class RefreshListener : RecyclerView.OnScrollListener(), SwipeRefreshLayout.OnRefreshListener {

        override fun onRefresh() {
            current = 0
            mPresenter!!.getBooksByTag(tag, current.toString() + "", "10")
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val lastVisibleItemPosition = linearLayoutManager!!.findLastVisibleItemPosition()
            if (lastVisibleItemPosition + 1 == mAdapter!!.itemCount) { // 滑到倒数第二项就加载更多

                val isRefreshing = refreshLayout!!.isRefreshing
                if (isRefreshing) {
                    mAdapter!!.notifyItemRemoved(mAdapter!!.itemCount)
                    return
                }
                mPresenter!!.getBooksByTag(tag, current.toString() + "", "10")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mPresenter != null) {
            mPresenter!!.detachView()
        }
    }
}
