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
import com.justwayward.reader.bean.BooksByTag
import com.justwayward.reader.bean.SearchDetail
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerBookComponent
import com.justwayward.reader.ui.contract.SearchByAuthorContract
import com.justwayward.reader.ui.easyadapter.SearchAdapter
import com.justwayward.reader.ui.presenter.SearchByAuthorPresenter

import java.util.ArrayList

import javax.inject.Inject

/**
 * @author yuyh.
 * @date 2016/9/8.
 */
class SearchByAuthorActivity : BaseRVActivity<SearchDetail.SearchBooks>(), SearchByAuthorContract.View {

    @Inject
    internal var mPresenter: SearchByAuthorPresenter? = null

    private var author = ""

    override val layoutId: Int
        get() = R.layout.activity_common_recyclerview

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerBookComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun initToolBar() {
        author = intent.getStringExtra(INTENT_AUTHOR)
        mCommonToolbar!!.title = author
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
    }

    override fun initDatas() {
        initAdapter(SearchAdapter::class.java, false, false)
    }

    override fun configViews() {
        mPresenter!!.attachView(this)
        mPresenter!!.getSearchResultList(author)
    }

    override fun onItemClick(position: Int) {
        val data = mAdapter!!.getItem(position)
        BookDetailActivity.startActivity(this, data._id)
    }

    override fun showSearchResultList(list: List<BooksByTag.TagBook>) {
        val mList = ArrayList<SearchDetail.SearchBooks>()
        for (book in list) {
            mList.add(SearchDetail.SearchBooks(book._id, book.title, book.author, book.cover, book.retentionRatio, book.latelyFollower))
        }
        mAdapter!!.clear()
        mAdapter!!.addAll(mList)
    }

    override fun showError() {
        loaddingError()
    }

    override fun complete() {
        mRecyclerView!!.setRefreshing(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mPresenter != null) {
            mPresenter!!.detachView()
        }
    }

    companion object {

        val INTENT_AUTHOR = "author"

        fun startActivity(context: Context, author: String) {
            context.startActivity(Intent(context, SearchByAuthorActivity::class.java)
                    .putExtra(INTENT_AUTHOR, author))
        }
    }
}
