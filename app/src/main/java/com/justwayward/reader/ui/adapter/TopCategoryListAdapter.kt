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
import com.justwayward.reader.bean.CategoryList
import com.justwayward.reader.common.OnRvItemClickListener
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter
import com.yuyh.easyadapter.recyclerview.EasyRVHolder

/**
 * @author lfh.
 * @date 16/8/30.
 */
class TopCategoryListAdapter(context: Context, list: List<CategoryList.MaleBean>, private val itemClickListener: OnRvItemClickListener<*>) : EasyRVAdapter<CategoryList.MaleBean>(context, list, R.layout.item_top_category_list) {

    override fun onBindData(holder: EasyRVHolder, position: Int, item: CategoryList.MaleBean) {
        holder.setText(R.id.tvName, item.name)
                .setText(R.id.tvBookCount, String.format(mContext.getString(R.string
                        .category_book_count), item.bookCount))

        holder.setOnItemViewClickListener { itemClickListener.onItemClick(holder.getItemView(), position, item) }
    }

}
