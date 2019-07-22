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
import com.justwayward.reader.bean.support.FindBean
import com.justwayward.reader.common.OnRvItemClickListener
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter
import com.yuyh.easyadapter.recyclerview.EasyRVHolder

/**
 * @author lfh.
 * @date 16/8/16.
 */
class FindAdapter(context: Context, list: List<FindBean>, private val itemClickListener: OnRvItemClickListener<*>) : EasyRVAdapter<FindBean>(context, list, R.layout.item_find) {

    override fun onBindData(holder: EasyRVHolder, position: Int, item: FindBean) {

        holder.setText(R.id.tvTitle, item.title)
        holder.setImageResource(R.id.ivIcon, item.iconResId)

        holder.setOnItemViewClickListener { itemClickListener.onItemClick(holder.getItemView(), position, item) }
    }
}
