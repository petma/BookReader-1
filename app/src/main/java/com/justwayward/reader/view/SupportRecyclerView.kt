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
package com.justwayward.reader.view

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.view.View

import com.justwayward.reader.utils.LogUtils


/**
 * 支持emptyView
 *
 * @author yuyh.
 * @date 16/6/10.
 */
class SupportRecyclerView : RecyclerView {
    private var emptyView: View? = null

    private val emptyObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            LogUtils.i("adapter changed")
            val adapter = adapter
            if (adapter != null && emptyView != null) {
                if (adapter.itemCount == 0) {
                    LogUtils.i("adapter visible")
                    emptyView!!.visibility = View.VISIBLE
                    this@SupportRecyclerView.visibility = View.GONE
                } else {
                    LogUtils.i("adapter gone")
                    emptyView!!.visibility = View.GONE
                    this@SupportRecyclerView.visibility = View.VISIBLE
                }
            }

        }
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    override fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        val oldAdapter = getAdapter()
        if (oldAdapter != null && emptyObserver != null) {
            oldAdapter.unregisterAdapterDataObserver(emptyObserver)
        }
        super.setAdapter(adapter)

        adapter?.registerAdapterDataObserver(emptyObserver)
        emptyObserver.onChanged()
    }

    /**
     * set view when no content item
     *
     * @param emptyView visiable view when items is empty
     */
    fun setEmptyView(emptyView: View) {
        this.emptyView = emptyView
    }
}