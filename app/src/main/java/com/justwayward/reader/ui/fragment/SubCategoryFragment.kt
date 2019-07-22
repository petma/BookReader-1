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
import com.justwayward.reader.bean.BooksByCats
import com.justwayward.reader.bean.support.SubEvent
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerFindComponent
import com.justwayward.reader.ui.activity.BookDetailActivity
import com.justwayward.reader.ui.contract.SubCategoryFragmentContract
import com.justwayward.reader.ui.easyadapter.SubCategoryAdapter
import com.justwayward.reader.ui.presenter.SubCategoryFragmentPresenter

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 二级分类
 *
 * @author yuyh.
 * @date 16/9/1.
 */
class SubCategoryFragment : BaseRVFragment<SubCategoryFragmentPresenter, BooksByCats.BooksBean>(), SubCategoryFragmentContract.View {

    private var major: String? = ""
    private var minor: String? = ""
    private var gender: String? = ""
    private var type: String? = ""

    override val layoutResId: Int
        get() = R.layout.common_easy_recyclerview

    override fun initDatas() {
        EventBus.getDefault().register(this)
        major = arguments!!.getString(BUNDLE_MAJOR)
        gender = arguments!!.getString(BUNDLE_GENDER)
        minor = arguments!!.getString(BUNDLE_MINOR)
        type = arguments!!.getString(BUNDLE_TYPE)
    }

    override fun configViews() {
        initAdapter(SubCategoryAdapter::class.java, true, true)
        onRefresh()
    }

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerFindComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun showCategoryList(data: BooksByCats, isRefresh: Boolean) {
        if (isRefresh) {
            start = 0
            mAdapter!!.clear()
        }
        mAdapter!!.addAll(data.books)
        start += data.books!!.size
    }

    override fun showError() {
        loaddingError()
    }

    override fun complete() {
        mRecyclerView!!.setRefreshing(false)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun initCategoryList(event: SubEvent) {
        minor = event.minor
        val type = event.type
        if (this.type == type) {
            onRefresh()
        }
    }

    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }

    override fun onItemClick(position: Int) {
        val data = mAdapter!!.getItem(position)
        BookDetailActivity.startActivity(activity!!, data._id)
    }

    override fun onRefresh() {
        super.onRefresh()
        mPresenter!!.getCategoryList(gender, major, minor, this.type, 0, limit)
    }

    override fun onLoadMore() {
        mPresenter!!.getCategoryList(gender, major, minor, this.type, start, limit)
    }

    companion object {

        val BUNDLE_MAJOR = "major"
        val BUNDLE_MINOR = "minor"
        val BUNDLE_GENDER = "gender"
        val BUNDLE_TYPE = "type"

        fun newInstance(major: String, minor: String, gender: String,
                        @Constant.CateType type: String): SubCategoryFragment {
            val fragment = SubCategoryFragment()
            val bundle = Bundle()
            bundle.putString(BUNDLE_MAJOR, major)
            bundle.putString(BUNDLE_GENDER, gender)
            bundle.putString(BUNDLE_MINOR, minor)
            bundle.putString(BUNDLE_TYPE, type)
            fragment.arguments = bundle
            return fragment
        }
    }
}
