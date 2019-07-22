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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.justwayward.reader.R
import com.justwayward.reader.base.BaseRVActivity
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.CommentList
import com.justwayward.reader.bean.Disscussion
import com.justwayward.reader.common.OnRvItemClickListener
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerCommunityComponent
import com.justwayward.reader.ui.adapter.BestCommentListAdapter
import com.justwayward.reader.ui.contract.BookDiscussionDetailContract
import com.justwayward.reader.ui.easyadapter.CommentListAdapter
import com.justwayward.reader.ui.presenter.BookDiscussionDetailPresenter
import com.justwayward.reader.utils.FormatUtils
import com.justwayward.reader.view.BookContentTextView
import com.justwayward.reader.view.SupportDividerItemDecoration
import com.justwayward.reader.view.recyclerview.adapter.RecyclerArrayAdapter
import com.yuyh.easyadapter.glide.GlideCircleTransform

import java.util.ArrayList

import javax.inject.Inject

import butterknife.Bind
import butterknife.ButterKnife

/**
 * 综合讨论区详情
 */
class BookDiscussionDetailActivity : BaseRVActivity<CommentList.CommentsBean>(), BookDiscussionDetailContract.View, OnRvItemClickListener<CommentList.CommentsBean> {

    private var id: String? = null
    private val mBestCommentList = ArrayList<CommentList.CommentsBean>()
    private var mBestCommentListAdapter: BestCommentListAdapter? = null
    private var headerViewHolder: HeaderViewHolder? = null

    @Inject
    internal var mPresenter: BookDiscussionDetailPresenter? = null

    override val layoutId: Int
        get() = R.layout.activity_community_book_discussion_detail

    override fun showError() {

    }

    override fun complete() {

    }

    internal class HeaderViewHolder(view: View) {
        @Bind(R.id.ivBookCover)
        var ivAvatar: ImageView? = null
        @Bind(R.id.tvBookTitle)
        var tvNickName: TextView? = null
        @Bind(R.id.tvTime)
        var tvTime: TextView? = null
        @Bind(R.id.tvTitle)
        var tvTitle: TextView? = null
        @Bind(R.id.tvContent)
        var tvContent: BookContentTextView? = null
        @Bind(R.id.tvBestComments)
        var tvBestComments: TextView? = null
        @Bind(R.id.rvBestComments)
        var rvBestComments: RecyclerView? = null
        @Bind(R.id.tvCommentCount)
        var tvCommentCount: TextView? = null

        init {
            ButterKnife.bind(this, view)   //view绑定
        }
    }

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerCommunityComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun initToolBar() {
        mCommonToolbar!!.title = "详情"
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
    }

    override fun initDatas() {
        id = intent.getStringExtra(INTENT_ID)

        mPresenter!!.attachView(this)
        mPresenter!!.getBookDisscussionDetail(id)
        mPresenter!!.getBestComments(id)
        mPresenter!!.getBookDisscussionComments(id, start, limit)
    }

    override fun configViews() {
        initAdapter(CommentListAdapter::class.java, false, true)

        mAdapter!!.addHeader(object : RecyclerArrayAdapter.ItemView {
            override fun onCreateView(parent: ViewGroup): View {
                return LayoutInflater.from(this@BookDiscussionDetailActivity).inflate(R.layout.header_view_book_discussion_detail, parent, false)
            }

            override fun onBindView(headerView: View) {
                headerViewHolder = HeaderViewHolder(headerView)
            }
        })

    }

    override fun showBookDisscussionDetail(disscussion: Disscussion) {
        Glide.with(mContext)
                .load(Constant.IMG_BASE_URL + disscussion.post!!.author!!.avatar!!)
                .placeholder(R.drawable.avatar_default)
                .transform(GlideCircleTransform(mContext))
                .into(headerViewHolder!!.ivAvatar!!)

        headerViewHolder!!.tvNickName!!.text = disscussion.post!!.author!!.nickname
        headerViewHolder!!.tvTime!!.text = FormatUtils.getDescriptionTimeFromDateString(disscussion.post!!.created)
        headerViewHolder!!.tvTitle!!.text = disscussion.post!!.title
        headerViewHolder!!.tvContent!!.setText(disscussion.post!!.content)
        headerViewHolder!!.tvCommentCount!!.text = String.format(mContext.getString(R.string.comment_comment_count), disscussion.post!!.commentCount)
    }

    override fun showBestComments(list: CommentList) {
        if (list.comments!!.isEmpty()) {
            gone(headerViewHolder!!.tvBestComments, headerViewHolder!!.rvBestComments)
        } else {
            mBestCommentList.addAll(list.comments!!)
            headerViewHolder!!.rvBestComments!!.setHasFixedSize(true)
            headerViewHolder!!.rvBestComments!!.layoutManager = LinearLayoutManager(this)
            headerViewHolder!!.rvBestComments!!.addItemDecoration(SupportDividerItemDecoration(mContext, LinearLayoutManager.VERTICAL, true))
            mBestCommentListAdapter = BestCommentListAdapter(mContext, mBestCommentList)
            mBestCommentListAdapter!!.setOnItemClickListener(this)
            headerViewHolder!!.rvBestComments!!.adapter = mBestCommentListAdapter
            visible(headerViewHolder!!.tvBestComments, headerViewHolder!!.rvBestComments)
        }
    }

    override fun showBookDisscussionComments(list: CommentList) {
        mAdapter!!.addAll(list.comments)
        start = start + list.comments!!.size
    }

    override fun onLoadMore() {
        super.onLoadMore()
        mPresenter!!.getBookDisscussionComments(id, start, limit)
    }

    override fun onItemClick(view: View, position: Int, data: CommentList.CommentsBean) {

    }

    override fun onItemClick(position: Int) {
        val data = mAdapter!!.getItem(position)
    }

    override fun onDestroy() {
        super.onDestroy()
        ButterKnife.unbind(headerViewHolder)
        if (mPresenter != null) {
            mPresenter!!.detachView()
        }
    }

    companion object {

        private val INTENT_ID = "id"

        fun startActivity(context: Context, id: String) {
            context.startActivity(Intent(context, BookDiscussionDetailActivity::class.java)
                    .putExtra(INTENT_ID, id))
        }
    }
}
