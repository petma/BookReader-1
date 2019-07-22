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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseActivity
import com.justwayward.reader.bean.BookListTags
import com.justwayward.reader.bean.support.TagEvent
import com.justwayward.reader.common.OnRvItemClickListener
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerFindComponent
import com.justwayward.reader.ui.adapter.SubjectTagsAdapter
import com.justwayward.reader.ui.contract.SubjectBookListContract
import com.justwayward.reader.ui.fragment.SubjectFragment
import com.justwayward.reader.ui.presenter.SubjectBookListPresenter
import com.justwayward.reader.utils.ToastUtils
import com.justwayward.reader.view.RVPIndicator
import com.justwayward.reader.view.ReboundScrollView
import com.justwayward.reader.view.SupportDividerItemDecoration

import org.greenrobot.eventbus.EventBus

import java.util.ArrayList
import java.util.Arrays

import javax.inject.Inject

import butterknife.Bind

class SubjectBookListActivity : BaseActivity(), SubjectBookListContract.View, OnRvItemClickListener<String> {

    @Bind(R.id.indicatorSubject)
    internal var mIndicator: RVPIndicator? = null
    @Bind(R.id.viewpagerSubject)
    internal var mViewPager: ViewPager? = null
    @Bind(R.id.rsvTags)
    internal var rsvTags: ReboundScrollView? = null

    @Bind(R.id.rvTags)
    internal var rvTags: RecyclerView? = null
    private var mTagAdapter: SubjectTagsAdapter? = null
    private val mTagList = ArrayList<BookListTags.DataBean>()

    private var mTabContents: MutableList<Fragment>? = null
    private var mAdapter: FragmentPagerAdapter? = null
    private var mDatas: List<String>? = null

    @Inject
    internal var mPresenter: SubjectBookListPresenter? = null

    private var currentTag = ""

    override val layoutId: Int
        get() = R.layout.activity_subject_book_list_tag

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerFindComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun initToolBar() {
        mCommonToolbar!!.setTitle(R.string.subject_book_list)
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
    }

    override fun initDatas() {
        mDatas = Arrays.asList(*resources.getStringArray(R.array.subject_tabs))

        mTabContents = ArrayList()
        mTabContents!!.add(SubjectFragment.newInstance("", 0))
        mTabContents!!.add(SubjectFragment.newInstance("", 1))
        mTabContents!!.add(SubjectFragment.newInstance("", 2))

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
        mIndicator!!.setViewPager(mViewPager, 0)

        rvTags!!.setHasFixedSize(true)
        rvTags!!.layoutManager = LinearLayoutManager(this)
        rvTags!!.addItemDecoration(SupportDividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        mTagAdapter = SubjectTagsAdapter(this, mTagList)
        mTagAdapter!!.setItemClickListener(this)
        rvTags!!.adapter = mTagAdapter

        mPresenter!!.attachView(this)
        mPresenter!!.getBookListTags()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_subject, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_tags) {
            if (isVisible(rsvTags)) {
                hideTagGroup()
            } else {
                showTagGroup()
            }
            return true
        } else if (item.itemId == R.id.menu_my_book_list) {
            startActivity(Intent(this, MyBookListActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (isVisible(rsvTags)) {
            hideTagGroup()
        } else {
            super.onBackPressed()
        }
    }

    override fun showBookListTags(data: BookListTags) {
        mTagList.clear()
        mTagList.addAll(data.data!!)
        mTagAdapter!!.notifyDataSetChanged()
    }

    override fun showError() {

    }

    override fun complete() {}

    override fun onItemClick(view: View, position: Int, data: String) {
        hideTagGroup()
        currentTag = data
        EventBus.getDefault().post(TagEvent(currentTag))
    }

    private fun showTagGroup() {
        if (mTagList.isEmpty()) {
            ToastUtils.showToast(getString(R.string.network_error_tips))
            return
        }
        val mShowAction = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f)
        mShowAction.duration = 400
        rsvTags!!.startAnimation(mShowAction)
        rsvTags!!.visibility = View.VISIBLE
    }

    private fun hideTagGroup() {
        val mHiddenAction = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f)
        mHiddenAction.duration = 400
        rsvTags!!.startAnimation(mHiddenAction)
        rsvTags!!.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mPresenter != null) {
            mPresenter!!.detachView()
        }
    }

    companion object {

        fun startActivity(context: Context) {
            val intent = Intent(context, SubjectBookListActivity::class.java)
            context.startActivity(intent)
        }
    }
}
