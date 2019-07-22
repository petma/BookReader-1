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
import android.text.TextUtils
import android.view.ViewGroup

import com.justwayward.reader.R
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.SearchDetail
import com.justwayward.reader.view.recyclerview.adapter.BaseViewHolder
import com.justwayward.reader.view.recyclerview.adapter.RecyclerArrayAdapter

/**
 * 查询
 *
 * @author yuyh.
 * @date 16/9/3.
 */
class SearchAdapter(context: Context) : RecyclerArrayAdapter<SearchDetail.SearchBooks>(context) {

    override fun OnCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return object : BaseViewHolder<SearchDetail.SearchBooks>(parent, R.layout.item_search_result_list) {
            override fun setData(item: SearchDetail.SearchBooks) {
                holder.setRoundImageUrl(R.id.ivBookCover, Constant.IMG_BASE_URL + item.cover, R.drawable.cover_default)
                        .setText(R.id.tvBookListTitle, item.title)
                        .setText(R.id.tvLatelyFollower, String.format(mContext!!.getString(R.string.search_result_lately_follower), item.latelyFollower))
                        .setText(R.id.tvRetentionRatio, if (TextUtils.isEmpty(item.retentionRatio))
                            String.format(mContext!!.getString(R.string.search_result_retention_ratio), "0")
                        else
                            String.format(mContext!!.getString(R.string.search_result_retention_ratio), item.retentionRatio))
                        .setText(R.id.tvBookListAuthor, String.format(mContext!!.getString(R.string.search_result_author), item.author))
            }
        }
    }
}
