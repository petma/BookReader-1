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
import android.text.TextUtils
import android.view.View

import com.justwayward.reader.R
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.BooksByTag
import com.justwayward.reader.common.OnRvItemClickListener
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter
import com.yuyh.easyadapter.recyclerview.EasyRVHolder

/**
 * @author lfh.
 * @date 16/8/7.
 */
class BooksByTagAdapter(context: Context, list: List<BooksByTag.TagBook>,
                        private val itemClickListener: OnRvItemClickListener<*>) : EasyRVAdapter<BooksByTag.TagBook>(context, list, R.layout.item_tag_book_list) {

    override fun onBindData(holder: EasyRVHolder, position: Int, item: BooksByTag.TagBook) {
        val sbTags = StringBuffer()
        for (tag in item.tags!!) {
            if (!TextUtils.isEmpty(tag)) {
                sbTags.append(tag)
                sbTags.append(" | ")
            }
        }

        holder.setRoundImageUrl(R.id.ivBookCover, Constant.IMG_BASE_URL + item.cover!!, R.drawable.cover_default)
                .setText(R.id.tvBookListTitle, item.title)
                .setText(R.id.tvShortIntro, item.shortIntro)
                .setText(R.id.tvTags, if (item.tags!!.size == 0)
                    ""
                else
                    sbTags.substring(0, sbTags
                            .lastIndexOf(" | ")))

        holder.setOnItemViewClickListener { itemClickListener.onItemClick(holder.getItemView(), position, item) }
    }
}
