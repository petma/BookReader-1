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
import android.view.View
import android.widget.ExpandableListView

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseActivity
import com.justwayward.reader.bean.RankingList
import com.justwayward.reader.common.OnRvItemClickListener
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerFindComponent
import com.justwayward.reader.ui.adapter.TopRankAdapter
import com.justwayward.reader.ui.contract.TopRankContract
import com.justwayward.reader.ui.presenter.TopRankPresenter

import java.util.ArrayList

import javax.inject.Inject

import butterknife.Bind

class TopRankActivity : BaseActivity(), TopRankContract.View {

    @Bind(R.id.elvFeMale)
    internal var elvFeMale: ExpandableListView? = null
    @Bind(R.id.elvMale)
    internal var elvMale: ExpandableListView? = null

    private val maleGroups = ArrayList<RankingList.MaleBean>()
    private val maleChilds = ArrayList<List<RankingList.MaleBean>>()
    private var maleAdapter: TopRankAdapter? = null

    private val femaleGroups = ArrayList<RankingList.MaleBean>()
    private val femaleChilds = ArrayList<List<RankingList.MaleBean>>()
    private var femaleAdapter: TopRankAdapter? = null

    @Inject
    internal var mPresenter: TopRankPresenter? = null

    override val layoutId: Int
        get() = R.layout.activity_top_rank

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerFindComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun initToolBar() {
        mCommonToolbar!!.title = "排行榜"
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
    }

    override fun initDatas() {
        maleAdapter = TopRankAdapter(this, maleGroups, maleChilds)
        femaleAdapter = TopRankAdapter(this, femaleGroups, femaleChilds)
        maleAdapter!!.setItemClickListener(ClickListener())
        femaleAdapter!!.setItemClickListener(ClickListener())
    }

    override fun configViews() {
        showDialog()
        elvMale!!.setAdapter(maleAdapter)
        elvFeMale!!.setAdapter(femaleAdapter)

        mPresenter!!.attachView(this)
        mPresenter!!.getRankList()
    }

    override fun showRankList(rankingList: RankingList) {
        maleGroups.clear()
        femaleGroups.clear()
        updateMale(rankingList)
        updateFemale(rankingList)
    }

    private fun updateMale(rankingList: RankingList) {
        val list = rankingList.male
        val collapse = ArrayList<RankingList.MaleBean>()
        for (bean in list!!) {
            if (bean.collapse) { // 折叠
                collapse.add(bean)
            } else {
                maleGroups.add(bean)
                maleChilds.add(ArrayList())
            }
        }
        if (collapse.size > 0) {
            maleGroups.add(RankingList.MaleBean("别人家的排行榜"))
            maleChilds.add(collapse)
        }
        maleAdapter!!.notifyDataSetChanged()
    }

    private fun updateFemale(rankingList: RankingList) {
        val list = rankingList.female
        val collapse = ArrayList<RankingList.MaleBean>()
        for (bean in list!!) {
            if (bean.collapse) { // 折叠
                collapse.add(bean)
            } else {
                femaleGroups.add(bean)
                femaleChilds.add(ArrayList())
            }
        }
        if (collapse.size > 0) {
            femaleGroups.add(RankingList.MaleBean("别人家的排行榜"))
            femaleChilds.add(collapse)
        }
        femaleAdapter!!.notifyDataSetChanged()
    }

    override fun showError() {

    }

    override fun complete() {
        dismissDialog()
    }

    internal inner class ClickListener : OnRvItemClickListener<RankingList.MaleBean> {

        override fun onItemClick(view: View, position: Int, data: RankingList.MaleBean) {
            if (data.monthRank == null) {
                SubOtherHomeRankActivity.startActivity(mContext, data._id, data.title)
            } else {
                SubRankActivity.startActivity(mContext, data._id, data.monthRank, data.totalRank, data.title)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mPresenter != null) {
            mPresenter!!.detachView()
        }
    }

    companion object {

        fun startActivity(context: Context) {
            val intent = Intent(context, TopRankActivity::class.java)
            context.startActivity(intent)
        }
    }
}
