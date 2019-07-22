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
import androidx.appcompat.widget.ListPopupWindow
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseActivity
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.CategoryListLv2
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerFindComponent
import com.justwayward.reader.manager.EventManager
import com.justwayward.reader.ui.adapter.MinorAdapter
import com.justwayward.reader.ui.contract.SubCategoryActivityContract
import com.justwayward.reader.ui.fragment.SubCategoryFragment
import com.justwayward.reader.ui.presenter.SubCategoryActivityPresenter
import com.justwayward.reader.view.RVPIndicator

import java.util.ArrayList
import java.util.Arrays

import javax.inject.Inject

import butterknife.Bind

/**
 * @author yuyh.
 * @date 2016/8/31.
 */
class SubCategoryListActivity : BaseActivity(), SubCategoryActivityContract.View {
    private var cate = ""
    private var gender = ""

    private var currentMinor = ""

    @Bind(R.id.indicatorSub)
    internal var mIndicator: RVPIndicator? = null
    @Bind(R.id.viewpagerSub)
    internal var mViewPager: ViewPager? = null

    @Inject
    internal var mPresenter: SubCategoryActivityPresenter? = null

    private var mTabContents: MutableList<Fragment>? = null
    private var mAdapter: FragmentPagerAdapter? = null
    private var mDatas: List<String>? = null

    private val mMinors = ArrayList<String>()
    private var mListPopupWindow: ListPopupWindow? = null
    private var minorAdapter: MinorAdapter? = null
    private val types = arrayOf(Constant.CateType.NEW, Constant.CateType.HOT, Constant.CateType.REPUTATION, Constant.CateType.OVER)

    private var menuItem: MenuItem? = null

    override val layoutId: Int
        get() = R.layout.activity_sub_category_list

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerFindComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun initToolBar() {
        cate = intent.getStringExtra(INTENT_CATE_NAME)
        if (menuItem != null) {
            menuItem!!.title = cate
        }
        gender = intent.getStringExtra(INTENT_GENDER)
        mCommonToolbar!!.title = cate
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
    }

    override fun initDatas() {
        mDatas = Arrays.asList(*resources.getStringArray(R.array.sub_tabs))

        mPresenter!!.attachView(this)
        mPresenter!!.getCategoryListLv2()

        mTabContents = ArrayList()
        mTabContents!!.add(SubCategoryFragment.newInstance(cate, "", gender, Constant.CateType.NEW))
        mTabContents!!.add(SubCategoryFragment.newInstance(cate, "", gender, Constant.CateType.HOT))
        mTabContents!!.add(SubCategoryFragment.newInstance(cate, "", gender, Constant.CateType.REPUTATION))
        mTabContents!!.add(SubCategoryFragment.newInstance(cate, "", gender, Constant.CateType.OVER))

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
        mViewPager!!.offscreenPageLimit = 4
        mIndicator!!.setViewPager(mViewPager, 0)
        mViewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                EventManager.refreshSubCategory(currentMinor, types[position])
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }

    override fun showCategoryList(data: CategoryListLv2) {
        mMinors.clear()
        mMinors.add(cate)
        if (gender == Constant.Gender.MALE) {
            for (bean in data.male!!) {
                if (cate == bean.major) {
                    mMinors.addAll(bean.mins!!)
                    break
                }
            }
        } else {
            for (bean in data.female!!) {
                if (cate == bean.major) {
                    mMinors.addAll(bean.mins!!)
                    break
                }
            }
        }
        minorAdapter = MinorAdapter(this, mMinors)
        minorAdapter!!.setChecked(0)
        currentMinor = ""
        EventManager.refreshSubCategory(currentMinor, Constant.CateType.NEW)
    }

    override fun showError() {

    }

    override fun complete() {}

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_sub_category, menu)
        menuItem = menu.findItem(R.id.menu_major)
        if (!TextUtils.isEmpty(cate)) {
            menuItem!!.title = cate
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_major) {
            showMinorPopupWindow()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showMinorPopupWindow() {
        if (mMinors.size > 0 && minorAdapter != null) {
            if (mListPopupWindow == null) {
                mListPopupWindow = ListPopupWindow(this)
                mListPopupWindow!!.setAdapter(minorAdapter)
                mListPopupWindow!!.width = ViewGroup.LayoutParams.MATCH_PARENT
                mListPopupWindow!!.height = ViewGroup.LayoutParams.WRAP_CONTENT
                mListPopupWindow!!.anchorView = mCommonToolbar
                mListPopupWindow!!.isModal = true
                mListPopupWindow!!.setOnItemClickListener { parent, view, position, id ->
                    minorAdapter!!.setChecked(position)
                    if (position > 0) {
                        currentMinor = mMinors[position]
                    } else {
                        currentMinor = ""
                    }
                    val current = mViewPager!!.currentItem
                    EventManager.refreshSubCategory(currentMinor, types[current])
                    mListPopupWindow!!.dismiss()
                    mCommonToolbar!!.title = mMinors[position]
                }
            }
            mListPopupWindow!!.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mPresenter != null) {
            mPresenter!!.detachView()
        }
    }

    companion object {

        val INTENT_CATE_NAME = "name"
        val INTENT_GENDER = "gender"

        fun startActivity(context: Context, name: String, @Constant.Gender gender: String) {
            val intent = Intent(context, SubCategoryListActivity::class.java)
            intent.putExtra(INTENT_CATE_NAME, name)
            intent.putExtra(INTENT_GENDER, gender)
            context.startActivity(intent)
        }
    }
}
