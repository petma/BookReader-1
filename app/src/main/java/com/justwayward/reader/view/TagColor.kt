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
package com.justwayward.reader.view

import android.graphics.Color

import com.justwayward.reader.base.Constant

import java.util.ArrayList

/**
 * @author yuyh.
 * @date 16/8/7.
 */
class TagColor {

    var borderColor = Color.parseColor("#49C120")
    var backgroundColor = Color.parseColor("#49C120")
    var textColor = Color.WHITE

    companion object {

        fun getRandomColors(size: Int): List<TagColor> {

            val list = ArrayList<TagColor>()
            for (i in 0 until size) {
                val color = TagColor()
                color.backgroundColor = Constant.tagColors[i % Constant.tagColors.size]
                color.borderColor = color.backgroundColor
                list.add(color)
            }
            return list
        }
    }
}
