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
package com.justwayward.reader.base

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View

import com.justwayward.reader.R
import com.justwayward.reader.utils.NetworkUtils
import com.justwayward.reader.view.recyclerview.EasyRecyclerView
import com.justwayward.reader.view.recyclerview.adapter.OnLoadMoreListener
import com.justwayward.reader.view.recyclerview.adapter.RecyclerArrayAdapter
import com.justwayward.reader.view.recyclerview.swipe.OnRefreshListener

import java.lang.reflect.Constructor

import butterknife.Bind

/**
 * @author yuyh.
 * @date 16/9/3.
 */
abstract class BaseRVActivity<T> : BaseActivity(), OnLoadMoreListener, OnRefreshListener, RecyclerArrayAdapter.OnItemClickListener {

    @Bind(R.id.recyclerview)
    protected var mRecyclerView: EasyRecyclerView? = null
    protected var mAdapter: RecyclerArrayAdapter<T>? = null

    protected var start = 0
    protected var limit = 20

    protected fun initAdapter(refreshable: Boolean, loadmoreable: Boolean) {
        if (mAdapter != null) {
            mAdapter!!.setOnItemClickListener(this)
            mAdapter!!.setError(R.layout.common_error_view).setOnClickListener { mAdapter!!.resumeMore() }
            if (loadmoreable) {
                mAdapter!!.setMore(R.layout.common_more_view, this)
                mAdapter!!.setNoMore(R.layout.common_nomore_view)
            }
            if (refreshable && mRecyclerView != null) {
                mRecyclerView!!.setRefreshListener(this)
            }
        }

        if (mRecyclerView != null) {
            mRecyclerView!!.setLayoutManager(LinearLayoutManager(this))
            mRecyclerView!!.setItemDecoration(ContextCompat.getColor(this, R.color.common_divider_narrow), 1, 0, 0)
            mRecyclerView!!.setAdapterWithProgress(mAdapter)
        }
    }

    protected fun initAdapter(clazz: Class<out RecyclerArrayAdapter<T>>, refreshable: Boolean, loadmoreable: Boolean) {
        mAdapter = createInstance(clazz) as RecyclerArrayAdapter<*>?
        initAdapter(refreshable, loadmoreable)
    }

    fun createInstance(cls: Class<*>): Any? {
        var obj: Any?
        try {
            val c1 = cls.getDeclaredConstructor(Context::class.java)
            c1.isAccessible = true
            obj = c1.newInstance(mContext)
        } catch (e: Exception) {
            obj = null
        }

        return obj
    }

    override fun onLoadMore() {
        if (!NetworkUtils.isConnected(applicationContext)) {
            mAdapter!!.pauseMore()
            return
        }
    }

    override fun onRefresh() {
        start = 0
        if (!NetworkUtils.isConnected(applicationContext)) {
            mAdapter!!.pauseMore()
            return
        }
    }

    protected fun loaddingError() {
        mAdapter!!.clear()
        mAdapter!!.pauseMore()
        mRecyclerView!!.setRefreshing(false)
    }
}
