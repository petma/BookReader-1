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

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseFragment
import com.justwayward.reader.bean.support.FindBean
import com.justwayward.reader.common.OnRvItemClickListener
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.ui.activity.BookDiscussionActivity
import com.justwayward.reader.ui.activity.BookReviewActivity
import com.justwayward.reader.ui.activity.BookHelpActivity
import com.justwayward.reader.ui.activity.GirlBookDiscussionActivity
import com.justwayward.reader.ui.adapter.FindAdapter
import com.justwayward.reader.view.SupportDividerItemDecoration

import java.util.ArrayList

import butterknife.Bind

class CommunityFragment : BaseFragment(), OnRvItemClickListener<FindBean> {

    @Bind(R.id.recyclerview)
    internal var mRecyclerView: RecyclerView? = null

    private var mAdapter: FindAdapter? = null
    private val mList = ArrayList<FindBean>()

    override val layoutResId: Int
        get() = R.layout.fragment_find

    override fun initDatas() {
        mList.clear()
        mList.add(FindBean("综合讨论区", R.drawable.discuss_section))
        mList.add(FindBean("书评区", R.drawable.comment_section))
        mList.add(FindBean("书荒互助区", R.drawable.helper_section))
        mList.add(FindBean("女生区", R.drawable.girl_section))
        mList.add(FindBean("原创区", R.drawable.yuanchuang))
    }

    override fun configViews() {
        mRecyclerView!!.setHasFixedSize(true)
        mRecyclerView!!.layoutManager = LinearLayoutManager(getActivity())
        mRecyclerView!!.addItemDecoration(SupportDividerItemDecoration(mContext, LinearLayoutManager.VERTICAL, true))

        mAdapter = FindAdapter(mContext, mList, this)
        mRecyclerView!!.adapter = mAdapter

    }

    override fun setupActivityComponent(appComponent: AppComponent?) {

    }

    override fun attachView() {

    }

    override fun onItemClick(view: View, position: Int, data: FindBean) {
        when (position) {
            0 -> BookDiscussionActivity.startActivity(activity!!, true)
            1 -> BookReviewActivity.startActivity(activity!!)
            2 -> BookHelpActivity.startActivity(activity!!)
            3 -> GirlBookDiscussionActivity.startActivity(activity!!)
            4 -> BookDiscussionActivity.startActivity(activity!!, false)
            else -> {
            }
        }
    }

}
