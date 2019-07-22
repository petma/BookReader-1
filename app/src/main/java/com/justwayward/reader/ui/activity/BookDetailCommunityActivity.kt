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
import android.content.DialogInterface
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AlertDialog
import android.view.Menu
import android.view.MenuItem

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseActivity
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.support.SelectionEvent
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.ui.fragment.BookDetailDiscussionFragment
import com.justwayward.reader.ui.fragment.BookDetailReviewFragment
import com.justwayward.reader.view.RVPIndicator

import org.greenrobot.eventbus.EventBus

import java.util.ArrayList
import java.util.Arrays

import butterknife.Bind

/**
 * 书籍详情 社区
 */
class BookDetailCommunityActivity : BaseActivity() {

    private var bookId: String? = null
    private var title: String? = null
    private var index: Int = 0

    @Bind(R.id.indicatorSubRank)
    internal var mIndicator: RVPIndicator? = null
    @Bind(R.id.viewpagerSubRank)
    internal var mViewPager: ViewPager? = null

    private var mTabContents: MutableList<Fragment>? = null
    private var mAdapter: FragmentPagerAdapter? = null
    private var mDatas: List<String>? = null

    private var dialog: AlertDialog? = null
    private val select = intArrayOf(0, 0)

    override val layoutId: Int
        get() = R.layout.activity_book_detail_community

    override fun setupActivityComponent(appComponent: AppComponent?) {

    }

    override fun initToolBar() {
        bookId = intent.getStringExtra(INTENT_ID)
        title = intent.getStringExtra(INTENT_TITLE)
        index = intent.getIntExtra(INTENT_INDEX, 0)
        mCommonToolbar!!.title = title
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
    }

    override fun initDatas() {
        mDatas = Arrays.asList(*resources.getStringArray(R.array.bookdetail_community_tabs))

        mTabContents = ArrayList()
        mTabContents!!.add(BookDetailDiscussionFragment.newInstance(bookId))
        mTabContents!!.add(BookDetailReviewFragment.newInstance(bookId))

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
        mViewPager!!.offscreenPageLimit = 2
        mIndicator!!.setViewPager(mViewPager, index)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_community, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_sort) {
            showSortDialog()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showSortDialog() {
        val checked = select[mViewPager!!.currentItem]
        dialog = AlertDialog.Builder(this)
                .setTitle("排序")
                .setSingleChoiceItems(arrayOf("默认排序", "最新发布", "最多评论"),
                        checked) { dialog, which ->
                    if (select[mViewPager!!.currentItem] != which) {
                        select[mViewPager!!.currentItem] = which
                        when (which) {
                            0 -> EventBus.getDefault().post(SelectionEvent(Constant.SortType.DEFAULT))
                            1 -> EventBus.getDefault().post(SelectionEvent(Constant.SortType.CREATED))
                            2 -> EventBus.getDefault().post(SelectionEvent(Constant.SortType.COMMENT_COUNT))
                            else -> {
                            }
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("取消", null)
                .create()
        dialog!!.show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {

        val INTENT_ID = "bookId"
        val INTENT_TITLE = "title"
        val INTENT_INDEX = "index"

        fun startActivity(context: Context, bookId: String, title: String, index: Int) {
            context.startActivity(Intent(context, BookDetailCommunityActivity::class.java)
                    .putExtra(INTENT_ID, bookId)
                    .putExtra(INTENT_TITLE, title)
                    .putExtra(INTENT_INDEX, index))
        }
    }
}
