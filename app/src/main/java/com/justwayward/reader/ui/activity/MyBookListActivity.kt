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

import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseRVActivity
import com.justwayward.reader.bean.BookLists
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.manager.CacheManager
import com.justwayward.reader.ui.easyadapter.SubjectBookListAdapter
import com.justwayward.reader.view.recyclerview.adapter.RecyclerArrayAdapter

/**
 * 我的书单
 * Created by lfh on 2016/9/23.
 */
class MyBookListActivity : BaseRVActivity<BookLists.BookListsBean>(), RecyclerArrayAdapter.OnItemLongClickListener {

    override val layoutId: Int
        get() = R.layout.activity_common_recyclerview

    override fun setupActivityComponent(appComponent: AppComponent?) {

    }

    override fun initToolBar() {
        mCommonToolbar!!.setTitle(R.string.subject_book_list_my_book_list)
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
    }

    override fun initDatas() {

    }

    override fun configViews() {
        initAdapter(SubjectBookListAdapter::class.java, true, false)
        mAdapter!!.setOnItemLongClickListener(this)
        onRefresh()
    }

    override fun onItemClick(position: Int) {
        SubjectBookListDetailActivity.startActivity(this, mAdapter!!.getItem(position))
    }

    override fun onItemLongClick(position: Int): Boolean {
        showLongClickDialog(position)
        return false
    }

    /**
     * 显示长按对话框
     *
     * @param position
     */
    private fun showLongClickDialog(position: Int) {
        AlertDialog.Builder(this)
                .setTitle(mAdapter!!.getItem(position).title)
                .setItems(resources.getStringArray(R.array.my_book_list_item_long_click_choice)
                ) { dialog, which ->
                    when (which) {
                        0 -> {
                            //删除
                            CacheManager.instance.removeCollection(mAdapter!!.getItem(position)._id)
                            mAdapter!!.remove(position)
                        }
                        else -> {
                        }
                    }
                    dialog.dismiss()
                }
                .setNegativeButton(null, null)
                .create().show()
    }

    override fun onRefresh() {
        super.onRefresh()
        val data = CacheManager.instance.collectionList
        mAdapter!!.clear()
        mAdapter!!.addAll(data)
        mRecyclerView!!.setRefreshing(false)
    }
}
