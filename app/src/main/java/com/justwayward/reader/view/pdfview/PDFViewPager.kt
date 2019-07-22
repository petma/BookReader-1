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
package com.justwayward.reader.view.pdfview

import android.content.Context
import androidx.viewpager.widget.ViewPager
import android.view.MotionEvent
import android.view.View

class PDFViewPager(protected var context: Context, pdfPath: String) : ViewPager(context) {

    private val clickListener = OnPageClickListener { view, x, y ->
        var item = currentItem
        val total = childCount

        if (x < 0.33f && item > 0) {
            item -= 1
            currentItem = item
        } else if (x >= 0.67f && item < total - 1) {
            item += 1
            currentItem = item
        }
    }

    init {
        init(pdfPath)
    }

    protected fun init(pdfPath: String) {
        isClickable = true
        initAdapter(context, pdfPath)
    }

    protected fun initAdapter(context: Context, pdfPath: String) {
        adapter = PDFPagerAdapter.Builder(context)
                .setPdfPath(pdfPath)
                .setOffScreenSize(offscreenPageLimit)
                .setOnPageClickListener(clickListener)
                .create()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        try {
            return super.onInterceptTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return false
        }

    }
}
