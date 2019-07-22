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
package com.justwayward.reader.view.recyclerview.decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import android.view.View

import com.justwayward.reader.view.recyclerview.adapter.RecyclerArrayAdapter


class DividerDecoration : RecyclerView.ItemDecoration {
    private var mColorDrawable: ColorDrawable? = null
    private var mHeight: Int = 0
    private val mPaddingLeft: Int
    private val mPaddingRight: Int
    private var mDrawLastItem = true
    private var mDrawHeaderFooter = false

    constructor(color: Int, height: Int) {
        this.mColorDrawable = ColorDrawable(color)
        this.mHeight = height
    }

    constructor(color: Int, height: Int, paddingLeft: Int, paddingRight: Int) {
        this.mColorDrawable = ColorDrawable(color)
        this.mHeight = height
        this.mPaddingLeft = paddingLeft
        this.mPaddingRight = paddingRight
    }

    fun setDrawLastItem(mDrawLastItem: Boolean) {
        this.mDrawLastItem = mDrawLastItem
    }

    fun setDrawHeaderFooter(mDrawHeaderFooter: Boolean) {
        this.mDrawHeaderFooter = mDrawHeaderFooter
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        var orientation = 0
        var headerCount = 0
        var footerCount = 0
        if (parent.adapter is RecyclerArrayAdapter<*>) {
            headerCount = (parent.adapter as RecyclerArrayAdapter<*>).headerCount
            footerCount = (parent.adapter as RecyclerArrayAdapter<*>).footerCount
        }

        val layoutManager = parent.layoutManager
        if (layoutManager is StaggeredGridLayoutManager) {
            orientation = layoutManager.orientation
        } else if (layoutManager is GridLayoutManager) {
            orientation = layoutManager.orientation
        } else if (layoutManager is LinearLayoutManager) {
            orientation = layoutManager.orientation
        }

        if (position >= headerCount && position < parent.adapter!!.itemCount - footerCount || mDrawHeaderFooter) {
            if (orientation == OrientationHelper.VERTICAL) {
                outRect.bottom = mHeight
            } else {
                outRect.right = mHeight
            }
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {


        var orientation = 0
        var headerCount = 0
        var footerCount = 0
        val dataCount: Int

        if (parent.adapter is RecyclerArrayAdapter<*>) {
            headerCount = (parent.adapter as RecyclerArrayAdapter<*>).headerCount
            footerCount = (parent.adapter as RecyclerArrayAdapter<*>).footerCount
            dataCount = (parent.adapter as RecyclerArrayAdapter<*>).count
        } else {
            dataCount = parent.adapter!!.itemCount
        }
        val dataStartPosition = headerCount
        val dataEndPosition = headerCount + dataCount


        val layoutManager = parent.layoutManager
        if (layoutManager is StaggeredGridLayoutManager) {
            orientation = layoutManager.orientation
        } else if (layoutManager is GridLayoutManager) {
            orientation = layoutManager.orientation
        } else if (layoutManager is LinearLayoutManager) {
            orientation = layoutManager.orientation
        }
        val start: Int
        val end: Int
        if (orientation == OrientationHelper.VERTICAL) {
            start = parent.paddingLeft + mPaddingLeft
            end = parent.width - parent.paddingRight - mPaddingRight
        } else {
            start = parent.paddingTop + mPaddingLeft
            end = parent.height - parent.paddingBottom - mPaddingRight
        }

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)

            if (position >= dataStartPosition && position < dataEndPosition - 1//数据项除了最后一项

                    || position == dataEndPosition - 1 && mDrawLastItem//数据项最后一项

                    || !(position >= dataStartPosition && position < dataEndPosition) && mDrawHeaderFooter//header&footer且可绘制
            ) {

                if (orientation == OrientationHelper.VERTICAL) {
                    val params = child.layoutParams as RecyclerView.LayoutParams
                    val top = child.bottom + params.bottomMargin
                    val bottom = top + mHeight
                    mColorDrawable!!.setBounds(start, top, end, bottom)
                    mColorDrawable!!.draw(c)
                } else {
                    val params = child.layoutParams as RecyclerView.LayoutParams
                    val left = child.right + params.rightMargin
                    val right = left + mHeight
                    mColorDrawable!!.setBounds(left, start, right, end)
                    mColorDrawable!!.draw(c)
                }
            }
        }
    }
}