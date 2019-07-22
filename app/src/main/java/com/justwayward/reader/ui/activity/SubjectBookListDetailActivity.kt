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
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.justwayward.reader.R
import com.justwayward.reader.base.BaseRVActivity
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.BookListDetail
import com.justwayward.reader.bean.BookLists
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerFindComponent
import com.justwayward.reader.manager.CacheManager
import com.justwayward.reader.ui.contract.SubjectBookListDetailContract
import com.justwayward.reader.ui.easyadapter.SubjectBookListDetailBooksAdapter
import com.justwayward.reader.ui.presenter.SubjectBookListDetailPresenter
import com.justwayward.reader.view.recyclerview.adapter.RecyclerArrayAdapter
import com.yuyh.easyadapter.glide.GlideCircleTransform

import java.util.ArrayList

import javax.inject.Inject

import butterknife.Bind
import butterknife.ButterKnife

/**
 * 书单详情
 */
class SubjectBookListDetailActivity : BaseRVActivity<BookListDetail.BookListBean.BooksBean>(), SubjectBookListDetailContract.View {

    private var headerViewHolder: HeaderViewHolder? = null

    private val mAllBooks = ArrayList<BookListDetail.BookListBean.BooksBean>()

    private var start = 0
    private val limit = 20

    @Inject
    internal var mPresenter: SubjectBookListDetailPresenter? = null

    private var bookListsBean: BookLists.BookListsBean? = null

    override val layoutId: Int
        get() = R.layout.activity_subject_book_list_detail

    internal class HeaderViewHolder(view: View) {
        @Bind(R.id.tvBookListTitle)
        var tvBookListTitle: TextView? = null
        @Bind(R.id.tvBookListDesc)
        var tvBookListDesc: TextView? = null
        @Bind(R.id.ivAuthorAvatar)
        var ivAuthorAvatar: ImageView? = null
        @Bind(R.id.tvBookListAuthor)
        var tvBookListAuthor: TextView? = null
        @Bind(R.id.btnShare)
        var btnShare: TextView? = null

        init {
            ButterKnife.bind(this, view)
        }
    }

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerFindComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun initToolBar() {
        mCommonToolbar!!.setTitle(R.string.subject_book_list_detail)
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
    }

    override fun initDatas() {
        bookListsBean = intent.getSerializableExtra(INTENT_BEAN) as BookLists.BookListsBean
    }

    override fun configViews() {
        initAdapter(SubjectBookListDetailBooksAdapter::class.java, false, true)
        mRecyclerView!!.removeAllItemDecoration()
        mAdapter!!.addHeader(object : RecyclerArrayAdapter.ItemView {
            override fun onCreateView(parent: ViewGroup): View {
                return LayoutInflater.from(mContext).inflate(R.layout.header_view_book_list_detail, parent, false)
            }

            override fun onBindView(headerView: View) {
                headerViewHolder = HeaderViewHolder(headerView)
            }
        })

        mPresenter!!.attachView(this)
        mPresenter!!.getBookListDetail(bookListsBean!!._id)
    }

    override fun showBookListDetail(data: BookListDetail) {
        headerViewHolder!!.tvBookListTitle!!.text = data.bookList!!.title
        headerViewHolder!!.tvBookListDesc!!.text = data.bookList!!.desc
        headerViewHolder!!.tvBookListAuthor!!.text = data.bookList!!.author!!.nickname

        Glide.with(mContext)
                .load(Constant.IMG_BASE_URL + data.bookList!!.author!!.avatar!!)
                .placeholder(R.drawable.avatar_default)
                .transform(GlideCircleTransform(mContext))
                .into(headerViewHolder!!.ivAuthorAvatar!!)

        val list = data.bookList!!.books
        mAllBooks.clear()
        mAllBooks.addAll(list!!)
        mAdapter!!.clear()
        loadNextPage()
    }

    private fun loadNextPage() {
        if (start < mAllBooks.size) {
            if (mAllBooks.size - start > limit) {
                mAdapter!!.addAll(mAllBooks.subList(start, start + limit))
            } else {
                mAdapter!!.addAll(mAllBooks.subList(start, mAllBooks.size))
            }
            start += limit
        } else {
            mAdapter!!.addAll(ArrayList())
        }
    }

    override fun showError() {

    }

    override fun complete() {

    }

    override fun onItemClick(position: Int) {
        BookDetailActivity.startActivity(this, mAdapter!!.getItem(position).book!!._id)
    }

    override fun onRefresh() {
        mPresenter!!.getBookListDetail(bookListsBean!!._id)
    }

    override fun onLoadMore() {
        mRecyclerView!!.postDelayed({ loadNextPage() }, 500)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_subject_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_collect) {
            CacheManager.instance.addCollection(bookListsBean)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        ButterKnife.unbind(headerViewHolder)
        if (mPresenter != null) {
            mPresenter!!.detachView()
        }
    }

    companion object {

        val INTENT_BEAN = "bookListsBean"

        fun startActivity(context: Context, bookListsBean: BookLists.BookListsBean) {
            context.startActivity(Intent(context, SubjectBookListDetailActivity::class.java)
                    .putExtra(INTENT_BEAN, bookListsBean))
        }
    }
}
