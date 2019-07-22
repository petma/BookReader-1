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
import com.justwayward.reader.view.recyclerview.EasyRecyclerView
import com.justwayward.reader.view.recyclerview.adapter.OnLoadMoreListener
import com.justwayward.reader.view.recyclerview.adapter.RecyclerArrayAdapter
import com.justwayward.reader.view.recyclerview.swipe.OnRefreshListener

import java.lang.reflect.Constructor

import javax.inject.Inject


/**
 * @author lfh.
 * @date 16/9/3.
 */
abstract class BaseRVFragment<T1 : BaseContract.BasePresenter<*>, T2> : BaseFragment(), OnLoadMoreListener, OnRefreshListener, RecyclerArrayAdapter.OnItemClickListener {

    @Inject
    protected var mPresenter: T1? = null

    @Bind(R.id.recyclerview)
    protected var mRecyclerView: EasyRecyclerView? = null
    protected var mAdapter: RecyclerArrayAdapter<T2>? = null

    protected var start = 0
    protected var limit = 20

    /**
     * [此方法不可再重写]
     */
    override fun attachView() {
        if (mPresenter != null)
            mPresenter!!.attachView(this)
    }

    protected fun initAdapter(refreshable: Boolean, loadmoreable: Boolean) {
        if (mRecyclerView != null) {
            mRecyclerView!!.setLayoutManager(LinearLayoutManager(supportActivity))
            mRecyclerView!!.setItemDecoration(ContextCompat.getColor(activity!!, R.color.common_divider_narrow), 1, 0, 0)
            mRecyclerView!!.setAdapterWithProgress(mAdapter)
        }

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
    }

    protected fun initAdapter(clazz: Class<out RecyclerArrayAdapter<T2>>, refreshable: Boolean, loadmoreable: Boolean) {
        mAdapter = createInstance(clazz) as RecyclerArrayAdapter<T2>?
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

    override fun onLoadMore() {}

    override fun onRefresh() {
        mRecyclerView!!.setRefreshing(true)
    }

    protected fun loaddingError() {
        if (mAdapter!!.count < 1) { // 说明缓存也没有加载，那就显示errorview，如果有缓存，即使刷新失败也不显示error
            mAdapter!!.clear()
        }
        mAdapter!!.pauseMore()
        mRecyclerView!!.setRefreshing(false)
        mRecyclerView!!.showTipViewAndDelayClose("似乎没有网络哦")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mPresenter != null)
            mPresenter!!.detachView()
    }
}
