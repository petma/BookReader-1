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
import com.justwayward.reader.bean.HotReview
import com.justwayward.reader.common.OnRvItemClickListener
import com.justwayward.reader.view.XLHRatingBar
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter
import com.yuyh.easyadapter.recyclerview.EasyRVHolder

/**
 * @author lfh.
 * @date 16/8/6.
 */
class HotReviewAdapter(context: Context, list: List<HotReview.Reviews>, private val itemClickListener: OnRvItemClickListener<*>) : EasyRVAdapter<HotReview.Reviews>(context, list, R.layout.item_book_detai_hot_review_list) {

    override fun onBindData(holder: EasyRVHolder, position: Int, item: HotReview.Reviews) {
        holder.setCircleImageUrl(R.id.ivBookCover, Constant.IMG_BASE_URL + item.author!!.avatar!!, R.drawable.avatar_default)
                .setText(R.id.tvBookTitle, item.author!!.nickname)
                .setText(R.id.tvBookType, String.format(mContext.getString(R.string
                        .book_detail_user_lv), item.author!!.lv))
                .setText(R.id.tvTitle, item.title)
                .setText(R.id.tvContent, item.content.toString())
                .setText(R.id.tvHelpfulYes, item.helpful!!.yes.toString())
        val ratingBar = holder.getView<XLHRatingBar>(R.id.rating)
        ratingBar.countSelected = item.rating
        holder.setOnItemViewClickListener { itemClickListener.onItemClick(holder.getItemView(), position, item) }
    }

}