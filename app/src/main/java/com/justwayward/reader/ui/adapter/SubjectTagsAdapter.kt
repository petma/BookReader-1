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
package com.justwayward.reader.ui.adapter

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View

import com.justwayward.reader.R
import com.justwayward.reader.bean.BookListTags
import com.justwayward.reader.common.OnRvItemClickListener
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter
import com.yuyh.easyadapter.recyclerview.EasyRVHolder

/**
 * @author yuyh.
 * @date 2016/8/31.
 */
class SubjectTagsAdapter(context: Context, list: List<BookListTags.DataBean>) : EasyRVAdapter<BookListTags.DataBean>(context, list, R.layout.item_subject_tags_list) {

    private var listener: OnRvItemClickListener<*>? = null

    override fun onBindData(viewHolder: EasyRVHolder, position: Int, item: BookListTags.DataBean) {
        val rvTagsItem = viewHolder.getView<RecyclerView>(R.id.rvTagsItem)
        rvTagsItem.setHasFixedSize(true)
        rvTagsItem.layoutManager = GridLayoutManager(mContext, 4)
        val adapter = TagsItemAdapter(mContext, item.tags)
        rvTagsItem.adapter = adapter

        viewHolder.setText(R.id.tvTagGroupName, item.name)
    }

    internal inner class TagsItemAdapter(context: Context, list: List<String>) : EasyRVAdapter<String>(context, list, R.layout.item_subject_tag_list) {

        override fun onBindData(viewHolder: EasyRVHolder, position: Int, item: String) {
            viewHolder.setText(R.id.tvTagName, item)
            viewHolder.getItemView().setOnClickListener { v ->
                if (listener != null) {
                    listener!!.onItemClick(v, position, item)
                }
            }
        }
    }

    fun setItemClickListener(listener: OnRvItemClickListener<String>) {
        this.listener = listener
    }
}
