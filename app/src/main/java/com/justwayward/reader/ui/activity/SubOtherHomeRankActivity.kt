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

import android.content.Context
import android.content.Intent

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseRVActivity
import com.justwayward.reader.bean.BooksByCats
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerFindComponent
import com.justwayward.reader.ui.contract.SubRankContract
import com.justwayward.reader.ui.easyadapter.SubCategoryAdapter
import com.justwayward.reader.ui.presenter.SubRankPresenter

import javax.inject.Inject

/**
 * 别人家的排行榜
 * Created by lfh on 2016/10/15.
 */
class SubOtherHomeRankActivity : BaseRVActivity<BooksByCats.BooksBean>(), SubRankContract.View {
    private var id: String? = null
    private var title: String? = null

    @Inject
    internal var mPresenter: SubRankPresenter? = null

    override val layoutId: Int
        get() = R.layout.activity_subject_book_list_detail

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerFindComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun initToolBar() {
        title = intent.getStringExtra(INTENT_TITLE).split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        id = intent.getStringExtra(BUNDLE_ID)

        mCommonToolbar!!.title = title
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
    }

    override fun initDatas() {

    }

    override fun configViews() {
        initAdapter(SubCategoryAdapter::class.java, true, false)
        mPresenter!!.attachView(this)
        onRefresh()
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
        BookDetailActivity.startActivity(this, mAdapter!!.getItem(position)._id)
    }

    override fun onRefresh() {
        super.onRefresh()
        mPresenter!!.getRankList(id)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mPresenter != null)
            mPresenter!!.detachView()
    }

    companion object {

        val BUNDLE_ID = "_id"
        val INTENT_TITLE = "title"

        fun startActivity(context: Context, id: String, title: String) {
            context.startActivity(Intent(context, SubOtherHomeRankActivity::class.java)
                    .putExtra(INTENT_TITLE, title).putExtra(BUNDLE_ID, id))
        }
    }
}
