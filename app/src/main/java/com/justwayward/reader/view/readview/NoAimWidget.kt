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
package com.justwayward.reader.view.readview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Region
import android.graphics.drawable.GradientDrawable

import com.justwayward.reader.bean.BookMixAToc
import com.justwayward.reader.manager.SettingManager
import com.justwayward.reader.manager.ThemeManager

/**
 * @author zths.
 * @date 2017/08/03.
 */
class NoAimWidget(context: Context, bookId: String, chaptersList: List<BookMixAToc.mixToc.Chapters>, listener: OnReadStateChangeListener) : OverlappedWidget(context, bookId, chaptersList, listener) {

    override fun startAnimation() {
        startAnimation(700)
    }

    fun startAnimation(duration: Int) {
        val dx: Int
        if (actiondownX > mScreenWidth / 2) {
            dx = (-(mScreenWidth + touch_down)).toInt()
            mScroller.startScroll((mScreenWidth + touch_down).toInt(), mTouch.y.toInt(), dx, 0, duration)
        } else {
            dx = (mScreenWidth - touch_down).toInt()
            mScroller.startScroll(touch_down.toInt(), mTouch.y.toInt(), dx, 0, duration)
        }
    }

}
