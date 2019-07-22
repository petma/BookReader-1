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

import android.content.Intent

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseFragment
import com.justwayward.reader.bean.support.FindBean
import com.justwayward.reader.common.OnRvItemClickListener
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.ui.activity.SubjectBookListActivity
import com.justwayward.reader.ui.activity.TopCategoryListActivity
import com.justwayward.reader.ui.activity.TopRankActivity
import com.justwayward.reader.ui.adapter.FindAdapter
import com.justwayward.reader.view.SupportDividerItemDecoration

import java.util.ArrayList

import butterknife.Bind

/**
 * 发现
 *
 * @author yuyh.
 * @date 16/9/1.
 */
class FindFragment : BaseFragment(), OnRvItemClickListener<FindBean> {

    @Bind(R.id.recyclerview)
    internal var mRecyclerView: RecyclerView? = null

    private var mAdapter: FindAdapter? = null
    private val mList = ArrayList<FindBean>()

    override val layoutResId: Int
        get() = R.layout.fragment_find

    override fun initDatas() {
        mList.clear()
        mList.add(FindBean("排行榜", R.drawable.home_find_rank))
        mList.add(FindBean("主题书单", R.drawable.home_find_topic))
        mList.add(FindBean("分类", R.drawable.home_find_category))
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
            0 -> TopRankActivity.startActivity(activity)
            1 -> SubjectBookListActivity.startActivity(activity)
            2 -> startActivity(Intent(activity, TopCategoryListActivity::class.java))
            else -> {
            }
        }
    }

}
