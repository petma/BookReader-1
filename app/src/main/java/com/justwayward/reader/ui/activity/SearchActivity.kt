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
import androidx.core.view.MenuItemCompat
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.SearchView
import android.text.TextUtils
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseRVActivity
import com.justwayward.reader.bean.SearchDetail
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerBookComponent
import com.justwayward.reader.manager.CacheManager
import com.justwayward.reader.ui.adapter.AutoCompleteAdapter
import com.justwayward.reader.ui.adapter.SearchHistoryAdapter
import com.justwayward.reader.ui.contract.SearchContract
import com.justwayward.reader.ui.easyadapter.SearchAdapter
import com.justwayward.reader.ui.presenter.SearchPresenter
import com.justwayward.reader.view.TagColor
import com.justwayward.reader.view.TagGroup

import java.util.ArrayList

import javax.inject.Inject

import butterknife.Bind
import butterknife.OnClick

/**
 * Created by Administrator on 2016/8/6.
 */
class SearchActivity : BaseRVActivity<SearchDetail.SearchBooks>(), SearchContract.View {

    @Bind(R.id.tvChangeWords)
    internal var mTvChangeWords: TextView? = null
    @Bind(R.id.tag_group)
    internal var mTagGroup: TagGroup? = null
    @Bind(R.id.rootLayout)
    internal var mRootLayout: LinearLayout? = null
    @Bind(R.id.layoutHotWord)
    internal var mLayoutHotWord: RelativeLayout? = null
    @Bind(R.id.rlHistory)
    internal var rlHistory: RelativeLayout? = null
    @Bind(R.id.tvClear)
    internal var tvClear: TextView? = null
    @Bind(R.id.lvSearchHistory)
    internal var lvSearchHistory: ListView? = null

    @Inject
    internal var mPresenter: SearchPresenter? = null

    private val tagList = ArrayList<String>()
    private var times = 0

    private var mAutoAdapter: AutoCompleteAdapter? = null
    private val mAutoList = ArrayList<String>()

    private var mHisAdapter: SearchHistoryAdapter? = null
    private val mHisList = ArrayList<String>()
    private var key: String? = null
    private var searchMenuItem: MenuItem? = null
    private var searchView: SearchView? = null

    private var mListPopupWindow: ListPopupWindow? = null

    internal var hotIndex = 0

    override val layoutId: Int
        get() = R.layout.activity_search

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerBookComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun initToolBar() {
        mCommonToolbar!!.title = ""
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
    }

    override fun initDatas() {
        key = intent.getStringExtra(INTENT_QUERY)

        mHisAdapter = SearchHistoryAdapter(this, mHisList)
        lvSearchHistory!!.adapter = mHisAdapter
        lvSearchHistory!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id -> search(mHisList[position]) }
        initSearchHistory()
    }

    override fun configViews() {
        initAdapter(SearchAdapter::class.java, false, false)

        initAutoList()

        mTagGroup!!.setOnTagClickListener { tag -> search(tag) }

        mTvChangeWords!!.setOnClickListener { showHotWord() }

        mPresenter!!.attachView(this)
        mPresenter!!.getHotWordList()
    }

    private fun initAutoList() {
        mAutoAdapter = AutoCompleteAdapter(this, mAutoList)
        mListPopupWindow = ListPopupWindow(this)
        mListPopupWindow!!.setAdapter(mAutoAdapter)
        mListPopupWindow!!.width = ViewGroup.LayoutParams.MATCH_PARENT
        mListPopupWindow!!.height = ViewGroup.LayoutParams.WRAP_CONTENT
        mListPopupWindow!!.anchorView = mCommonToolbar
        mListPopupWindow!!.setOnItemClickListener { parent, view, position, id ->
            mListPopupWindow!!.dismiss()
            val tv = view.findViewById<View>(R.id.tvAutoCompleteItem) as TextView
            val str = tv.text.toString()
            search(str)
        }
    }

    @Synchronized
    override fun showHotWordList(list: List<String>) {
        visible(mTvChangeWords)
        tagList.clear()
        tagList.addAll(list)
        times = 0
        showHotWord()
    }

    /**
     * 每次显示8个热搜词
     */
    @Synchronized
    private fun showHotWord() {
        val tagSize = 8
        val tags = arrayOfNulls<String>(tagSize)
        var j = 0
        while (j < tagSize && j < tagList.size) {
            tags[j] = tagList[hotIndex % tagList.size]
            hotIndex++
            j++
        }
        val colors = TagColor.getRandomColors(tagSize)
        mTagGroup!!.setTags(colors, *tags)
    }

    override fun showAutoCompleteList(list: List<String>) {
        mAutoList.clear()
        mAutoList.addAll(list)

        if (!mListPopupWindow!!.isShowing) {
            mListPopupWindow!!.inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED
            mListPopupWindow!!.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            mListPopupWindow!!.show()
        }
        mAutoAdapter!!.notifyDataSetChanged()

    }

    override fun showSearchResultList(list: List<SearchDetail.SearchBooks>) {
        mAdapter!!.clear()
        mAdapter!!.addAll(list)
        mAdapter!!.notifyDataSetChanged()
        initSearchResult()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search, menu)

        searchMenuItem = menu.findItem(R.id.action_search)//在菜单中找到对应控件的item
        searchView = MenuItemCompat.getActionView(searchMenuItem!!) as SearchView
        searchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                key = query
                mPresenter!!.getSearchResultList(query)
                saveSearchHistory(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (TextUtils.isEmpty(newText)) {
                    if (mListPopupWindow!!.isShowing)
                        mListPopupWindow!!.dismiss()
                    initTagGroup()
                } else {
                    mPresenter!!.getAutoCompleteList(newText)
                }
                return false
            }
        })
        search(key) // 外部调用搜索，则打开页面立即进行搜索
        MenuItemCompat.setOnActionExpandListener(searchMenuItem!!,
                object : MenuItemCompat.OnActionExpandListener {
                    //设置打开关闭动作监听
                    override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                        return true
                    }

                    override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                        initTagGroup()
                        return true
                    }
                })
        return true
    }

    /**
     * 保存搜索记录.不重复，最多保存20条
     *
     * @param query
     */
    private fun saveSearchHistory(query: String?) {
        var list: MutableList<String>? = CacheManager.instance.searchHistory
        if (list == null) {
            list = ArrayList()
            list.add(query)
        } else {
            val iterator = list.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next()
                if (TextUtils.equals(query, item)) {
                    iterator.remove()
                }
            }
            list.add(0, query)
        }
        val size = list.size
        if (size > 20) { // 最多保存20条
            for (i in size - 1 downTo 20) {
                list.removeAt(i)
            }
        }
        CacheManager.instance.saveSearchHistory(list)
        initSearchHistory()
    }

    private fun initSearchHistory() {
        val list = CacheManager.instance.searchHistory
        mHisAdapter!!.clear()
        if (list != null && list!!.size > 0) {
            tvClear!!.isEnabled = true
            mHisAdapter!!.addAll(list)
        } else {
            tvClear!!.isEnabled = false
        }
        mHisAdapter!!.notifyDataSetChanged()
    }

    /**
     * 展开SearchView进行查询
     *
     * @param key
     */
    private fun search(key: String?) {
        MenuItemCompat.expandActionView(searchMenuItem!!)
        if (!TextUtils.isEmpty(key)) {
            searchView!!.setQuery(key, true)
            saveSearchHistory(key)
        }
    }

    private fun initSearchResult() {
        gone(mTagGroup, mLayoutHotWord, rlHistory)
        visible(mRecyclerView)
        if (mListPopupWindow!!.isShowing)
            mListPopupWindow!!.dismiss()
    }

    private fun initTagGroup() {
        visible(mTagGroup, mLayoutHotWord, rlHistory)
        gone(mRecyclerView)
        if (mListPopupWindow!!.isShowing)
            mListPopupWindow!!.dismiss()
    }

    override fun onItemClick(position: Int) {
        val data = mAdapter!!.getItem(position)
        BookDetailActivity.startActivity(this, data._id)
    }

    @OnClick(R.id.tvClear)
    fun clearSearchHistory() {
        CacheManager.instance.saveSearchHistory(null)
        initSearchHistory()
    }

    override fun showError() {
        loaddingError()
    }

    override fun complete() {
        mRecyclerView!!.setRefreshing(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mPresenter != null) {
            mPresenter!!.detachView()
        }
    }

    companion object {

        val INTENT_QUERY = "query"

        fun startActivity(context: Context, query: String) {
            context.startActivity(Intent(context, SearchActivity::class.java)
                    .putExtra(INTENT_QUERY, query))
        }
    }
}
