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
import com.justwayward.reader.bean.BooksByCats
import com.justwayward.reader.manager.SettingManager
import com.justwayward.reader.view.recyclerview.adapter.BaseViewHolder
import com.justwayward.reader.view.recyclerview.adapter.RecyclerArrayAdapter

/**
 * 二级排行榜 / 二级分类
 *
 * @author yuyh.
 * @date 16/9/3.
 */
class SubCategoryAdapter(context: Context) : RecyclerArrayAdapter<BooksByCats.BooksBean>(context) {

    override fun OnCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return object : BaseViewHolder<BooksByCats.BooksBean>(parent, R.layout.item_sub_category_list) {
            override fun setData(item: BooksByCats.BooksBean) {
                super.setData(item)
                if (!SettingManager.instance!!.isNoneCover) {
                    holder.setRoundImageUrl(R.id.ivSubCateCover, Constant.IMG_BASE_URL + item.cover,
                            R.drawable.cover_default)
                } else {
                    holder.setImageResource(R.id.ivSubCateCover, R.drawable.cover_default)
                }

                holder.setText(R.id.tvSubCateTitle, item.title)
                        .setText(R.id.tvSubCateAuthor, (if (item.author == null) "未知" else item.author) + " | " + if (item.majorCate == null) "未知" else item.majorCate)
                        .setText(R.id.tvSubCateShort, item.shortIntro)
                        .setText(R.id.tvSubCateMsg, String.format(mContext!!.resources.getString(R.string.category_book_msg),
                                item.latelyFollower,
                                if (TextUtils.isEmpty(item.retentionRatio)) "0" else item.retentionRatio))
            }
        }
    }
}
