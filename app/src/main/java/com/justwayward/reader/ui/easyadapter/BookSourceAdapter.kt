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
package com.justwayward.reader.ui.easyadapter

import android.content.Context
import android.view.ViewGroup

import com.justwayward.reader.R
import com.justwayward.reader.bean.BookSource
import com.justwayward.reader.view.LetterView
import com.justwayward.reader.view.recyclerview.adapter.BaseViewHolder
import com.justwayward.reader.view.recyclerview.adapter.RecyclerArrayAdapter

/**
 * 查询
 *
 * @author yuyh.
 * @date 16/9/3.
 */
class BookSourceAdapter(context: Context) : RecyclerArrayAdapter<BookSource>(context) {

    override fun OnCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return object : BaseViewHolder<BookSource>(parent, R.layout.item_book_source) {
            override fun setData(item: BookSource) {
                holder.setText(R.id.tv_source_title, item.host)
                        .setText(R.id.tv_source_content, item.lastChapter)

                val letterView = holder.getView<LetterView>(R.id.letter_view)
                letterView.setText(item.host)
            }
        }
    }
}
