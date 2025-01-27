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
import android.view.View

import com.justwayward.reader.R
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.RecommendBookList
import com.justwayward.reader.common.OnRvItemClickListener
import com.justwayward.reader.manager.SettingManager
import com.justwayward.reader.utils.NoDoubleClickListener
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter
import com.yuyh.easyadapter.recyclerview.EasyRVHolder

/**
 * @author lfh.
 * @date 16/8/7.
 */
class RecommendBookListAdapter(context: Context, list: List<RecommendBookList.RecommendBook>,
                               private val itemClickListener: OnRvItemClickListener<*>) : EasyRVAdapter<RecommendBookList.RecommendBook>(context, list, R.layout.item_book_detail_recommend_book_list) {

    override fun onBindData(holder: EasyRVHolder, position: Int, item: RecommendBookList.RecommendBook) {
        if (!SettingManager.instance!!.isNoneCover) {
            holder.setRoundImageUrl(R.id.ivBookListCover, Constant.IMG_BASE_URL + item.cover!!, R.drawable.cover_default)
        }

        holder.setText(R.id.tvBookListTitle, item.title)
                .setText(R.id.tvBookAuthor, item.author)
                .setText(R.id.tvBookListTitle, item.title)
                .setText(R.id.tvBookListDesc, item.desc)
                .setText(R.id.tvBookCount, String.format(mContext.getString(R.string
                        .book_detail_recommend_book_list_book_count), item.bookCount))
                .setText(R.id.tvCollectorCount, String.format(mContext.getString(R.string
                        .book_detail_recommend_book_list_collector_count), item.collectorCount))
        holder.setOnItemViewClickListener(object : NoDoubleClickListener() {
            override fun onNoDoubleClick(view: View) {
                itemClickListener.onItemClick(holder.getItemView(), position, item)
            }
        })
    }

}