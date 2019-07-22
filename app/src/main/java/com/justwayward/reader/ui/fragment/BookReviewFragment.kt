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

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseRVFragment
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.BookReviewList
import com.justwayward.reader.bean.support.SelectionEvent
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerCommunityComponent
import com.justwayward.reader.ui.activity.BookReviewDetailActivity
import com.justwayward.reader.ui.contract.BookReviewContract
import com.justwayward.reader.ui.easyadapter.BookReviewAdapter
import com.justwayward.reader.ui.presenter.BookReviewPresenter

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 书评区Fragment
 *
 * @author lfh.
 * @date 16/9/3.
 */
class BookReviewFragment : BaseRVFragment<BookReviewPresenter, BookReviewList.ReviewsBean>(), BookReviewContract.View {

    private var sort = Constant.SortType.DEFAULT
    private var type = Constant.BookType.ALL
    private var distillate = Constant.Distillate.ALL

    override val layoutResId: Int
        get() = R.layout.common_easy_recyclerview

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerCommunityComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun initDatas() {
        EventBus.getDefault().register(this)
    }

    override fun configViews() {
        initAdapter(BookReviewAdapter::class.java, true, true)
        onRefresh()
    }

    override fun showBookReviewList(list: List<BookReviewList.ReviewsBean>, isRefresh: Boolean) {
        if (isRefresh) {
            mAdapter!!.clear()
        }
        mAdapter!!.addAll(list)
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
        mRecyclerView!!.setRefreshing(true)
        sort = event.sort
        type = event.type
        distillate = event.distillate
        start = 0
        mPresenter!!.getBookReviewList(sort, type, distillate, start, limit)
    }

    override fun onRefresh() {
        super.onRefresh()
        mPresenter!!.getBookReviewList(sort, type, distillate, start, limit)
    }

    override fun onLoadMore() {
        mPresenter!!.getBookReviewList(sort, type, distillate, start, limit)
    }

    override fun onItemClick(position: Int) {
        val data = mAdapter!!.getItem(position)
        BookReviewDetailActivity.startActivity(activity!!, data._id)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }
}
