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
import android.widget.RelativeLayout
import android.widget.TextView

import com.justwayward.reader.R
import com.justwayward.reader.utils.ScreenUtils
import com.yuyh.easyadapter.abslistview.EasyLVAdapter
import com.yuyh.easyadapter.abslistview.EasyLVHolder

/**
 * @author yuyh.
 * @date 16/8/6.
 */
class MinorAdapter(context: Context, list: List<String>) : EasyLVAdapter<String>(context, list, R.layout.item_minor_list) {

    private var current = 0

    override fun convert(holder: EasyLVHolder, position: Int, s: String) {
        holder.setText(R.id.tvMinorItem, s)

        if (current == position) {
            holder.setVisible(R.id.ivMinorChecked, true)
        } else {
            holder.setVisible(R.id.ivMinorChecked, false)
        }

        if (position != 0) { // 子项右移
            val textView = holder.getView<TextView>(R.id.tvMinorItem)
            val params = textView.layoutParams as RelativeLayout.LayoutParams
            params.leftMargin = ScreenUtils.dpToPxInt(25f)
            textView.layoutParams = params
        }
    }

    fun setChecked(position: Int) {
        current = position
        notifyDataSetChanged()
    }
}
