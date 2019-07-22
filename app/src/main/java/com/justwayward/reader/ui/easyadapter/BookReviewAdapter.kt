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
import com.justwayward.reader.bean.BookReviewList
import com.justwayward.reader.manager.SettingManager
import com.justwayward.reader.utils.FormatUtils
import com.justwayward.reader.view.recyclerview.adapter.BaseViewHolder
import com.justwayward.reader.view.recyclerview.adapter.RecyclerArrayAdapter

/**
 * @author lfh.
 * @date 16/9/3.
 */
class BookReviewAdapter(context: Context) : RecyclerArrayAdapter<BookReviewList.ReviewsBean>(context) {

    override fun OnCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return object : BaseViewHolder<BookReviewList.ReviewsBean>(parent, R.layout.item_community_book_review_list) {
            override fun setData(item: BookReviewList.ReviewsBean) {
                if (!SettingManager.instance!!.isNoneCover) {
                    holder.setRoundImageUrl(R.id.ivBookCover, Constant.IMG_BASE_URL + item.book!!.cover!!,
                            R.drawable.cover_default)
                } else {
                    holder.setImageResource(R.id.ivBookCover, R.drawable.cover_default)
                }

                holder.setText(R.id.tvBookTitle, item.book!!.title)
                        .setText(R.id.tvBookType, String.format(mContext!!.getString(R.string.book_review_book_type), Constant.bookType[item.book!!.type]))
                        .setText(R.id.tvTitle, item.title)
                        .setText(R.id.tvHelpfulYes, String.format(mContext!!.getString(R.string.book_review_helpful_yes), item.helpful!!.yes))

                if (TextUtils.equals(item.state, "hot")) {
                    holder.setVisible(R.id.tvHot, true)
                    holder.setVisible(R.id.tvTime, false)
                    holder.setVisible(R.id.tvDistillate, false)
                } else if (TextUtils.equals(item.state, "distillate")) {
                    holder.setVisible(R.id.tvDistillate, true)
                    holder.setVisible(R.id.tvHot, false)
                    holder.setVisible(R.id.tvTime, false)
                } else {
                    holder.setVisible(R.id.tvTime, true)
                    holder.setVisible(R.id.tvHot, false)
                    holder.setVisible(R.id.tvDistillate, false)
                    holder.setText(R.id.tvTime, FormatUtils.getDescriptionTimeFromDateString(item.created))
                }
            }
        }
    }
}
