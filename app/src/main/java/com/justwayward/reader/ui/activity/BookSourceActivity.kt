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

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AlertDialog

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseRVActivity
import com.justwayward.reader.bean.BookSource
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.component.DaggerBookComponent
import com.justwayward.reader.ui.contract.BookSourceContract
import com.justwayward.reader.ui.easyadapter.BookSourceAdapter
import com.justwayward.reader.ui.presenter.BookSourcePresenter

import javax.inject.Inject

/**
 * @author yuyh.
 * @date 2016/9/8.
 */
class BookSourceActivity : BaseRVActivity<BookSource>(), BookSourceContract.View {

    @Inject
    internal var mPresenter: BookSourcePresenter? = null

    private var bookId = ""

    override val layoutId: Int
        get() = R.layout.activity_common_recyclerview

    override fun setupActivityComponent(appComponent: AppComponent?) {
        DaggerBookComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun initToolBar() {
        bookId = intent.getStringExtra(INTENT_BOOK_ID)
        mCommonToolbar!!.title = "选择来源"
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
    }

    override fun initDatas() {
        initAdapter(BookSourceAdapter::class.java, false, false)
    }

    override fun configViews() {
        mPresenter!!.attachView(this)
        mPresenter!!.getBookSource("summary", bookId)

        AlertDialog.Builder(this)
                .setMessage("换源功能暂未实现，后续更新...")
                .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }.create().show()
    }

    override fun onItemClick(position: Int) {
        val data = mAdapter!!.getItem(position)
        val intent = Intent()
        intent.putExtra("source", data)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }


    override fun showBookSource(list: List<BookSource>) {
        mAdapter!!.clear()
        mAdapter!!.addAll(list)
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

        val INTENT_BOOK_ID = "bookId"

        fun start(activity: Activity, bookId: String, reqId: Int) {
            activity.startActivityForResult(Intent(activity, BookSourceActivity::class.java)
                    .putExtra(INTENT_BOOK_ID, bookId), reqId)
        }
    }

}
