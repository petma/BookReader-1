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
import com.justwayward.reader.bean.BookLists
import com.justwayward.reader.bean.support.TagEvent
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerFindComponent
import com.justwayward.reader.manager.SettingManager
import com.justwayward.reader.ui.activity.SubjectBookListDetailActivity
import com.justwayward.reader.ui.contract.SubjectFragmentContract
import com.justwayward.reader.ui.easyadapter.SubjectBookListAdapter
import com.justwayward.reader.ui.presenter.SubjectFragmentPresenter

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 主题书单
 *
 * @author yuyh.
 * @date 16/9/1.
 */
class SubjectFragment : BaseRVFragment<SubjectFragmentPresenter, BookLists.BookListsBean>(), SubjectFragmentContract.View {

    var currendTag: String
    var currentTab: Int = 0

    var duration = ""
    var sort = ""

    override val layoutResId: Int
        get() = R.layout.common_easy_recyclerview

    override fun initDatas() {
        EventBus.getDefault().register(this)

        currentTab = arguments!!.getInt(BUNDLE_TAB)
        when (currentTab) {
            0 -> {
                duration = "last-seven-days"
                sort = "collectorCount"
            }
            1 -> {
                duration = "all"
                sort = "created"
            }
            2 -> {
                duration = "all"
                sort = "collectorCount"
            }
            else -> {
                duration = "all"
                sort = "collectorCount"
            }
        }
    }

    override fun configViews() {
        initAdapter(SubjectBookListAdapter::class.java, true, true)
        onRefresh()
    }

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerFindComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun showBookList(bookLists: List<BookLists.BookListsBean>, isRefresh: Boolean) {
        if (isRefresh) {
            mAdapter!!.clear()
            start = 0
        }
        mAdapter!!.addAll(bookLists)
        start = start + bookLists.size
    }

    override fun showError() {
        loaddingError()
    }

    override fun complete() {
        mRecyclerView!!.setRefreshing(false)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun initCategoryList(event: TagEvent) {
        currendTag = event.tag
        if (userVisibleHint) {
            mPresenter!!.getBookLists(duration, sort, 0, limit, currendTag, SettingManager.instance!!.userChooseSex)
        }
    }

    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }

    override fun onItemClick(position: Int) {
        SubjectBookListDetailActivity.startActivity(activity!!, mAdapter!!.getItem(position))
    }

    override fun onRefresh() {
        super.onRefresh()
        mPresenter!!.getBookLists(duration, sort, 0, limit, currendTag, SettingManager.instance!!.userChooseSex)
    }

    override fun onLoadMore() {
        mPresenter!!.getBookLists(duration, sort, start, limit, currendTag, SettingManager.instance!!.userChooseSex)
    }

    companion object {

        val BUNDLE_TAG = "tag"
        val BUNDLE_TAB = "tab"

        fun newInstance(tag: String, tab: Int): SubjectFragment {
            val fragment = SubjectFragment()
            val bundle = Bundle()
            bundle.putString(BUNDLE_TAG, tag)
            bundle.putInt(BUNDLE_TAB, tab)
            fragment.arguments = bundle
            return fragment
        }
    }
}
