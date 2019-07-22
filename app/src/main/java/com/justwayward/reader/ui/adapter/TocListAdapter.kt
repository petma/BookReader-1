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
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import android.widget.TextView

import com.justwayward.reader.R
import com.justwayward.reader.bean.BookMixAToc
import com.justwayward.reader.utils.FileUtils
import com.yuyh.easyadapter.abslistview.EasyLVAdapter
import com.yuyh.easyadapter.abslistview.EasyLVHolder

/**
 * @author lfh.
 * @date 16/8/11.
 */
class TocListAdapter(context: Context, list: List<BookMixAToc.mixToc.Chapters>, private val bookId: String, private var currentChapter: Int) : EasyLVAdapter<BookMixAToc.mixToc.Chapters>(context, list, R.layout.item_book_read_toc_list) {

    private var isEpub = false

    override fun convert(holder: EasyLVHolder, position: Int, chapters: BookMixAToc.mixToc.Chapters) {
        val tvTocItem = holder.getView<TextView>(R.id.tvTocItem)
        tvTocItem.text = chapters.title
        val drawable: Drawable?
        if (currentChapter == position + 1) {
            tvTocItem.setTextColor(ContextCompat.getColor(mContext, R.color.light_red))
            drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_toc_item_activated)
        } else if (isEpub || FileUtils.getChapterFile(bookId, position + 1).length() > 10) {
            tvTocItem.setTextColor(ContextCompat.getColor(mContext, R.color.light_black))
            drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_toc_item_download)
        } else {
            tvTocItem.setTextColor(ContextCompat.getColor(mContext, R.color.light_black))
            drawable = ContextCompat.getDrawable(mContext, R.drawable.ic_toc_item_normal)
        }
        drawable!!.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
        tvTocItem.setCompoundDrawables(drawable, null, null, null)
    }

    fun setCurrentChapter(chapter: Int) {
        currentChapter = chapter
        notifyDataSetChanged()
    }

    fun setEpub(isEpub: Boolean) {
        this.isEpub = isEpub
    }
}
