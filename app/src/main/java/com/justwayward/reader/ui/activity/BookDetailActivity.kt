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
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.bumptech.glide.Glide
import com.justwayward.reader.R
import com.justwayward.reader.base.BaseActivity
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.BookDetail
import com.justwayward.reader.bean.BookLists
import com.justwayward.reader.bean.HotReview
import com.justwayward.reader.bean.Recommend
import com.justwayward.reader.bean.RecommendBookList
import com.justwayward.reader.bean.support.RefreshCollectionIconEvent
import com.justwayward.reader.common.OnRvItemClickListener
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerBookComponent
import com.justwayward.reader.manager.CollectionsManager
import com.justwayward.reader.ui.adapter.HotReviewAdapter
import com.justwayward.reader.ui.adapter.RecommendBookListAdapter
import com.justwayward.reader.ui.contract.BookDetailContract
import com.justwayward.reader.ui.presenter.BookDetailPresenter
import com.justwayward.reader.utils.FormatUtils
import com.justwayward.reader.utils.ToastUtils
import com.justwayward.reader.view.DrawableCenterButton
import com.justwayward.reader.view.TagColor
import com.justwayward.reader.view.TagGroup
import com.yuyh.easyadapter.glide.GlideRoundTransform

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.util.ArrayList

import javax.inject.Inject


import butterknife.OnClick
import kotlinx.android.synthetic.main.activity_book_detail.*

/**
 * Created by lfh on 2016/8/6.
 */
class BookDetailActivity : BaseActivity(), BookDetailContract.View, OnRvItemClickListener<Any> {
 
 
 
 

    @Inject
    internal var mPresenter: BookDetailPresenter? = null

    private val tagList = ArrayList<String>()
    private var times = 0

    private var mHotReviewAdapter: HotReviewAdapter? = null
    private val mHotReviewList = ArrayList<HotReview.Reviews>()
    private var mRecommendBookListAdapter: RecommendBookListAdapter? = null
    private val mRecommendBookList = ArrayList<RecommendBookList.RecommendBook>()
    private var bookId: String? = null

    private var collapseLongIntro = true
    private var recommendBooks: Recommend.RecommendBooks? = null
    private var isJoinedCollections = false

    override val layoutId: Int
        get() = R.layout.activity_book_detail

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerBookComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun initToolBar() {
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
        mCommonToolbar!!.setTitle(R.string.book_detail)
    }

    override fun initDatas() {
        bookId = intent.getStringExtra(INTENT_BOOK_ID)
        EventBus.getDefault().register(this)
    }

    override fun configViews() {
        rvHotReview!!.setHasFixedSize(true)
        rvHotReview!!.layoutManager = LinearLayoutManager(this)
        mHotReviewAdapter = HotReviewAdapter(mContext, mHotReviewList, this)
        rvHotReview!!.adapter = mHotReviewAdapter

        rvRecommendBoookList!!.setHasFixedSize(true)
        rvRecommendBoookList!!.layoutManager = LinearLayoutManager(this)
        mRecommendBookListAdapter = RecommendBookListAdapter(mContext, mRecommendBookList, this)
        rvRecommendBoookList!!.adapter = mRecommendBookListAdapter

        tag_group!!.setOnTagClickListener { tag ->
            startActivity(Intent(this@BookDetailActivity, BooksByTagActivity::class.java)
                    .putExtra("tag", tag))
        }

        mPresenter!!.attachView(this)
        mPresenter!!.getBookDetail(bookId!!)
        mPresenter!!.getHotReview(bookId!!)
        mPresenter!!.getRecommendBookList(bookId!!, "3")
    }

    override fun showBookDetail(data: BookDetail) {
        Glide.with(mContext)
                .load(Constant.IMG_BASE_URL + data.cover!!)
                .placeholder(R.drawable.cover_default)
                .transform(GlideRoundTransform(mContext))
                .into(ivBookCover!!)

        tvBookListTitle!!.text = data.title
        tvBookListAuthor!!.text = String.format(getString(R.string.book_detail_author), data.author)
        tvCatgory!!.text = String.format(getString(R.string.book_detail_category), data.cat)
        tvWordCount!!.text = FormatUtils.formatWordCount(data.wordCount)
        tvLatelyUpdate!!.text = FormatUtils.getDescriptionTimeFromDateString(data.updated?:"")
        tvLatelyFollower!!.text = data.latelyFollower.toString()
        tvRetentionRatio!!.text = if (TextUtils.isEmpty(data.retentionRatio))
            "-"
        else
            String.format(getString(R.string.book_detail_retention_ratio),
                    data.retentionRatio)
        tvSerializeWordCount!!.text = if (data.serializeWordCount < 0)
            "-"
        else
            data.serializeWordCount.toString()

        tagList.clear()
        tagList.addAll(data.tags!!)
        times = 0
        showHotWord()

        tvlongIntro!!.text = data.longIntro
        tvCommunity!!.text = String.format(getString(R.string.book_detail_community), data.title)
        tvHelpfulYes!!.text = String.format(getString(R.string.book_detail_post_count), data.postCount)

        recommendBooks = Recommend.RecommendBooks()
        recommendBooks!!.title = data.title
        recommendBooks!!._id = data._id
        recommendBooks!!.cover = data.cover
        recommendBooks!!.lastChapter = data.lastChapter
        recommendBooks!!.updated = data.updated?:""

        refreshCollectionIcon()
    }

    /**
     * 刷新收藏图标
     */
    private fun refreshCollectionIcon() {
        if (CollectionsManager.instance!!.isCollected(recommendBooks!!._id)) {
            initCollection(false)
        } else {
            initCollection(true)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun RefreshCollectionIcon(event: RefreshCollectionIconEvent) {
        refreshCollectionIcon()
    }

    /**
     * 每次显示8个
     */
    private fun showHotWord() {
        val start: Int
        val end: Int
        if (times < tagList.size && times + 8 <= tagList.size) {
            start = times
            end = times + 8
        } else if (times < tagList.size - 1 && times + 8 > tagList.size) {
            start = times
            end = tagList.size - 1
        } else {
            start = 0
            end = if (tagList.size > 8) 8 else tagList.size
        }
        times = end
        if (end - start > 0) {
            val batch = tagList.subList(start, end)
            val colors = TagColor.getRandomColors(batch.size)
            tag_group!!.setTags(colors, *batch.toTypedArray())
        }
    }

    override fun showHotReview(list: List<HotReview.Reviews>) {
        mHotReviewList.clear()
        mHotReviewList.addAll(list)
        mHotReviewAdapter!!.notifyDataSetChanged()
    }

    override fun showRecommendBookList(list: List<RecommendBookList.RecommendBook>) {
        if (!list.isEmpty()) {
            tvRecommendBookList!!.visibility = View.VISIBLE
            mRecommendBookList.clear()
            mRecommendBookList.addAll(list)
            mRecommendBookListAdapter!!.notifyDataSetChanged()
        }
    }

    override fun onItemClick(view: View, position: Int, data: Any) {
        if (data is HotReview.Reviews) {
            BookDiscussionDetailActivity.startActivity(this, data._id?:"")
        } else if (data is RecommendBookList.RecommendBook) {

            val bookLists = BookLists()
            val bookListsBean = bookLists.BookListsBean()
            bookListsBean._id = data.id
            bookListsBean.author = data.author
            bookListsBean.bookCount = data.bookCount
            bookListsBean.collectorCount = data.collectorCount
            bookListsBean.cover = data.cover
            bookListsBean.desc = data.desc
            bookListsBean.title = data.title

            SubjectBookListDetailActivity.startActivity(this, bookListsBean)
        }
    }

    @OnClick(R.id.btnJoinCollection)
    fun onClickJoinCollection() {
        if (!isJoinedCollections) {
            if (recommendBooks != null) {
                CollectionsManager.instance!!.add(recommendBooks!!)
                ToastUtils.showToast(String.format(getString(
                        R.string.book_detail_has_joined_the_book_shelf), recommendBooks!!.title))
                initCollection(false)
            }
        } else {
            CollectionsManager.instance!!.remove(recommendBooks!!._id!!)
            ToastUtils.showToast(String.format(getString(
                    R.string.book_detail_has_remove_the_book_shelf), recommendBooks!!.title))
            initCollection(true)
        }
    }

    private fun initCollection(coll: Boolean) {
        if (coll) {
            btnJoinCollection!!.setText(R.string.book_detail_join_collection)
            val drawable = ContextCompat.getDrawable(this, R.drawable.book_detail_info_add_img)
            drawable!!.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
            btnJoinCollection!!.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.shape_common_btn_solid_normal))
            btnJoinCollection!!.setCompoundDrawables(drawable, null, null, null)
            btnJoinCollection!!.postInvalidate()
            isJoinedCollections = false
        } else {
            btnJoinCollection!!.setText(R.string.book_detail_remove_collection)
            val drawable = ContextCompat.getDrawable(this, R.drawable.book_detail_info_del_img)
            drawable!!.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
            btnJoinCollection!!.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.btn_join_collection_pressed))
            btnJoinCollection!!.setCompoundDrawables(drawable, null, null, null)
            btnJoinCollection!!.postInvalidate()
            isJoinedCollections = true
        }
    }

    @OnClick(R.id.btnRead)
    fun onClickRead() {
        if (recommendBooks == null) return
        ReadActivity.startActivity(this, recommendBooks!!)
    }

    @OnClick(R.id.tvBookListAuthor)
    fun searchByAuthor() {
        val author = tvBookListAuthor!!.text.toString().replace(" ".toRegex(), "")
        SearchByAuthorActivity.startActivity(this, author)
    }

    @OnClick(R.id.tvlongIntro)
    fun collapseLongIntro() {
        if (collapseLongIntro) {
            tvlongIntro!!.maxLines = 20
            collapseLongIntro = false
        } else {
            tvlongIntro!!.maxLines = 4
            collapseLongIntro = true
        }
    }

    @OnClick(R.id.tvMoreReview)
    fun onClickMoreReview() {
        BookDetailCommunityActivity.startActivity(this, bookId!!, tvBookListTitle!!.text.toString(), 1)
    }

    @OnClick(R.id.rlCommunity)
    fun onClickCommunity() {
        BookDetailCommunityActivity.startActivity(this, bookId!!, tvBookListTitle!!.text.toString(), 0)
    }

    override fun showError() {

    }

    override fun complete() {

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        if (mPresenter != null) {
            mPresenter!!.detachView()
        }
    }

    companion object {

        var INTENT_BOOK_ID = "bookId"

        fun startActivity(context: Context, bookId: String) {
            context.startActivity(Intent(context, BookDetailActivity::class.java)
                    .putExtra(INTENT_BOOK_ID, bookId))
        }
    }
}
