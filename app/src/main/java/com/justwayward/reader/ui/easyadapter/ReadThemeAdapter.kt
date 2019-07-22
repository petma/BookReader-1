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

import com.justwayward.reader.R
import com.justwayward.reader.bean.support.ReadTheme
import com.justwayward.reader.manager.ThemeManager
import com.justwayward.reader.utils.LogUtils
import com.yuyh.easyadapter.abslistview.EasyLVAdapter
import com.yuyh.easyadapter.abslistview.EasyLVHolder

/**
 * @author yuyh.
 * @date 2016/9/23.
 */
class ReadThemeAdapter(context: Context, list: List<ReadTheme>, selected: Int) : EasyLVAdapter<ReadTheme>(context, list, R.layout.item_read_theme) {

    private var selected = 0

    init {
        this.selected = selected
    }

    override fun convert(holder: EasyLVHolder, position: Int, readTheme: ReadTheme?) {
        if (readTheme != null) {
            ThemeManager.setReaderTheme(readTheme.theme, holder.getView(R.id.ivThemeBg))
            if (selected == position) {
                holder.setVisible(R.id.ivSelected, true)
            } else {
                holder.setVisible(R.id.ivSelected, false)
            }
        }
    }

    fun select(position: Int) {
        selected = position
        LogUtils.i("curtheme=$selected")
        notifyDataSetChanged()
    }
}
