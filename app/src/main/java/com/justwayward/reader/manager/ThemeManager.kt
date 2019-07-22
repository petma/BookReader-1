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
package com.justwayward.reader.manager

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.ContextCompat
import android.view.View

import com.justwayward.reader.R
import com.justwayward.reader.bean.support.ReadTheme
import com.justwayward.reader.utils.AppUtils
import com.justwayward.reader.utils.ScreenUtils

import java.util.ArrayList

/**
 * @author yuyh.
 * @date 2016/9/23.
 */
object ThemeManager {

    val NORMAL = 0
    val YELLOW = 1
    val GREEN = 2
    val LEATHER = 3
    val GRAY = 4
    val NIGHT = 5

    fun setReaderTheme(theme: Int, view: View) {
        when (theme) {
            NORMAL -> view.setBackgroundResource(R.drawable.theme_white_bg)
            YELLOW -> view.setBackgroundResource(R.drawable.theme_yellow_bg)
            GREEN -> view.setBackgroundResource(R.drawable.theme_green_bg)
            LEATHER -> view.setBackgroundResource(R.drawable.theme_leather_bg)
            GRAY -> view.setBackgroundResource(R.drawable.theme_gray_bg)
            NIGHT -> view.setBackgroundResource(R.drawable.theme_night_bg)
            else -> {
            }
        }
    }

    fun getThemeDrawable(theme: Int): Bitmap {
        var bmp = Bitmap.createBitmap(ScreenUtils.screenWidth, ScreenUtils.screenHeight, Bitmap.Config.ARGB_8888)
        when (theme) {
            NORMAL -> bmp.eraseColor(ContextCompat.getColor(AppUtils.appContext!!, R.color.read_theme_white))
            YELLOW -> bmp.eraseColor(ContextCompat.getColor(AppUtils.appContext!!, R.color.read_theme_yellow))
            GREEN -> bmp.eraseColor(ContextCompat.getColor(AppUtils.appContext!!, R.color.read_theme_green))
            LEATHER -> bmp = BitmapFactory.decodeResource(AppUtils.appContext!!.getResources(), R.drawable.theme_leather_bg)
            GRAY -> bmp.eraseColor(ContextCompat.getColor(AppUtils.appContext!!, R.color.read_theme_gray))
            NIGHT -> bmp.eraseColor(ContextCompat.getColor(AppUtils.appContext!!, R.color.read_theme_night))
            else -> {
            }
        }
        return bmp
    }

    fun getReaderThemeData(curTheme: Int): List<ReadTheme> {
        val themes = intArrayOf(NORMAL, YELLOW, GREEN, LEATHER, GRAY, NIGHT)
        val list = ArrayList<ReadTheme>()
        var theme: ReadTheme
        for (i in themes.indices) {
            theme = ReadTheme()
            theme.theme = themes[i]
            list.add(theme)
        }
        return list
    }

}
