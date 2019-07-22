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
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.BookListDetail
import com.justwayward.reader.manager.SettingManager
import com.justwayward.reader.view.recyclerview.adapter.BaseViewHolder
import com.justwayward.reader.view.recyclerview.adapter.RecyclerArrayAdapter

/**
 * @author yuyh.
 * @date 16/9/4.
 */
class SubjectBookListDetailBooksAdapter(context: Context) : RecyclerArrayAdapter<BookListDetail.BookListBean.BooksBean>(context) {

    override fun OnCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return object : BaseViewHolder<BookListDetail.BookListBean.BooksBean>(parent, R.layout.item_subject_book_list_detail) {
            override fun setData(item: BookListDetail.BookListBean.BooksBean) {
                if (!SettingManager.instance!!.isNoneCover) {
                    holder.setRoundImageUrl(R.id.ivBookCover, Constant.IMG_BASE_URL + item.book!!.cover!!,
                            R.drawable.cover_default)
                } else {
                    holder.setImageResource(R.id.ivBookCover, R.drawable.cover_default)
                }

                holder.setText(R.id.tvBookListTitle, item.book!!.title)
                        .setText(R.id.tvBookAuthor, item.book!!.author)
                        .setText(R.id.tvBookLatelyFollower, String.format(mContext!!.resources.getString(R.string.subject_book_list_detail_book_lately_follower),
                                item.book!!.latelyFollower))
                        .setText(R.id.tvBookWordCount, String.format(mContext!!.resources.getString(R.string.subject_book_list_detail_book_word_count),
                                item.book!!.wordCount / 10000))
                        .setText(R.id.tvBookDetail, item.book!!.longIntro)
            }
        }
    }
}
