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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseActivity
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerFindComponent
import com.justwayward.reader.ui.fragment.SubRankFragment
import com.justwayward.reader.view.RVPIndicator

import java.util.ArrayList
import java.util.Arrays

import butterknife.Bind

/**
 * @author yuyh.
 * @date 16/9/1.
 */
class SubRankActivity : BaseActivity() {

    private var week: String? = null
    private var month: String? = null
    private var all: String? = null
    private var title: String? = null

    @Bind(R.id.indicatorSubRank)
    internal var mIndicator: RVPIndicator? = null
    @Bind(R.id.viewpagerSubRank)
    internal var mViewPager: ViewPager? = null

    private var mTabContents: MutableList<Fragment>? = null
    private var mAdapter: FragmentPagerAdapter? = null
    private var mDatas: List<String>? = null


    override val layoutId: Int
        get() = R.layout.activity_sub_rank

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerFindComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun initToolBar() {
        week = intent.getStringExtra(INTENT_WEEK)
        month = intent.getStringExtra(INTENT_MONTH)
        all = intent.getStringExtra(INTENT_ALL)

        title = intent.getStringExtra(INTENT_TITLE).split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        mCommonToolbar!!.title = title
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
    }

    override fun initDatas() {
        mDatas = Arrays.asList(*resources.getStringArray(R.array.sub_rank_tabs))

        mTabContents = ArrayList()
        mTabContents!!.add(SubRankFragment.newInstance(week))
        mTabContents!!.add(SubRankFragment.newInstance(month))
        mTabContents!!.add(SubRankFragment.newInstance(all))

        mAdapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getCount(): Int {
                return mTabContents!!.size
            }

            override fun getItem(position: Int): Fragment {
                return mTabContents!![position]
            }
        }
    }

    override fun configViews() {
        mIndicator!!.setTabItemTitles(mDatas)
        mViewPager!!.adapter = mAdapter
        mViewPager!!.offscreenPageLimit = 3
        mIndicator!!.setViewPager(mViewPager, 0)
    }

    companion object {

        val INTENT_WEEK = "_id"
        val INTENT_MONTH = "month"
        val INTENT_ALL = "all"
        val INTENT_TITLE = "title"

        fun startActivity(context: Context, week: String, month: String, all: String, title: String) {
            context.startActivity(Intent(context, SubRankActivity::class.java)
                    .putExtra(INTENT_WEEK, week)
                    .putExtra(INTENT_MONTH, month)
                    .putExtra(INTENT_ALL, all)
                    .putExtra(INTENT_TITLE, title))
        }
    }
}
