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
import com.justwayward.reader.bean.BooksByCats
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerFindComponent
import com.justwayward.reader.ui.activity.BookDetailActivity
import com.justwayward.reader.ui.contract.SubRankContract
import com.justwayward.reader.ui.easyadapter.SubCategoryAdapter
import com.justwayward.reader.ui.presenter.SubRankPresenter

/**
 * 二级排行榜
 *
 * @author yuyh.
 * @date 16/9/1.
 */
class SubRankFragment : BaseRVFragment<SubRankPresenter, BooksByCats.BooksBean>(), SubRankContract.View {

    private var id: String? = null

    override val layoutResId: Int
        get() = R.layout.common_easy_recyclerview

    override fun initDatas() {
        id = arguments!!.getString(BUNDLE_ID)
    }

    override fun configViews() {
        initAdapter(SubCategoryAdapter::class.java, true, false)

        onRefresh()
    }

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerFindComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun showRankList(data: BooksByCats) {
        mAdapter!!.clear()
        mAdapter!!.addAll(data.books)
    }

    override fun showError() {
        loaddingError()
    }

    override fun complete() {
        mRecyclerView!!.setRefreshing(false)
    }

    override fun onItemClick(position: Int) {
        BookDetailActivity.startActivity(activity!!, mAdapter!!.getItem(position)._id)
    }

    override fun onRefresh() {
        super.onRefresh()
        mPresenter!!.getRankList(id)
    }

    companion object {

        val BUNDLE_ID = "_id"

        fun newInstance(id: String): SubRankFragment {
            val fragment = SubRankFragment()
            val bundle = Bundle()
            bundle.putString(BUNDLE_ID, id)
            fragment.arguments = bundle
            return fragment
        }
    }

}
