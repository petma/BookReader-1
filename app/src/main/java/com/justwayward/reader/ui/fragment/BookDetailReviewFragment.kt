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
package com.justwayward.reader.ui.fragment

import android.os.Bundle

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseRVFragment
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.HotReview
import com.justwayward.reader.bean.support.SelectionEvent
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerBookComponent
import com.justwayward.reader.ui.activity.BookReviewDetailActivity
import com.justwayward.reader.ui.contract.BookDetailReviewContract
import com.justwayward.reader.ui.easyadapter.BookDetailReviewAdapter
import com.justwayward.reader.ui.presenter.BookDetailReviewPresenter

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 书籍详情 书评列表Fragment
 *
 * @author lfh.
 * @date 16/9/7.
 */
class BookDetailReviewFragment : BaseRVFragment<BookDetailReviewPresenter, HotReview.Reviews>(), BookDetailReviewContract.View {

    private var bookId: String? = null

    private var sort = Constant.SortType.DEFAULT
    private val type = Constant.BookType.ALL

    override val layoutResId: Int
        get() = R.layout.common_easy_recyclerview

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerBookComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun initDatas() {
        EventBus.getDefault().register(this)
        bookId = arguments!!.getString(BUNDLE_ID)
    }

    override fun configViews() {
        initAdapter(BookDetailReviewAdapter::class.java, true, true)
        onRefresh()
    }

    override fun showBookDetailReviewList(list: List<HotReview.Reviews>?, isRefresh: Boolean) {
        if (isRefresh) {
            mAdapter!!.clear()
        }
        mAdapter!!.addAll(list)
        if (list != null)
            start = start + list.size
    }

    override fun showError() {
        loaddingError()
    }

    override fun complete() {
        mRecyclerView!!.setRefreshing(false)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun initCategoryList(event: SelectionEvent) {
        if (userVisibleHint) {
            mRecyclerView!!.setRefreshing(true)
            sort = event.sort
            onRefresh()
        }
    }

    override fun onRefresh() {
        super.onRefresh()
        mPresenter!!.getBookDetailReviewList(bookId, sort, 0, limit)
    }

    override fun onLoadMore() {
        super.onLoadMore()
        mPresenter!!.getBookDetailReviewList(sort, type, start, limit)
    }

    override fun onItemClick(position: Int) {
        BookReviewDetailActivity.startActivity(activity!!, mAdapter!!.getItem(position)._id)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    companion object {

        val BUNDLE_ID = "bookId"

        fun newInstance(id: String): BookDetailReviewFragment {
            val fragment = BookDetailReviewFragment()
            val bundle = Bundle()
            bundle.putString(BUNDLE_ID, id)
            fragment.arguments = bundle
            return fragment
        }
    }
}
