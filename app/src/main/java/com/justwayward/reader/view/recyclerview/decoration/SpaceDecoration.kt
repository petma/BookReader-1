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

import android.graphics.Rect
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import android.view.Gravity
import android.view.View

import com.justwayward.reader.view.recyclerview.adapter.RecyclerArrayAdapter


class SpaceDecoration(space: Int) : RecyclerView.ItemDecoration() {

    private val halfSpace: Int
    private val headerCount = -1
    private val footerCount = Integer.MAX_VALUE
    private var mPaddingEdgeSide = true
    private var mPaddingStart = true
    private var mPaddingHeaderFooter = false


    init {
        this.halfSpace = space / 2
    }

    fun setPaddingEdgeSide(mPaddingEdgeSide: Boolean) {
        this.mPaddingEdgeSide = mPaddingEdgeSide
    }

    fun setPaddingStart(mPaddingStart: Boolean) {
        this.mPaddingStart = mPaddingStart
    }

    fun setPaddingHeaderFooter(mPaddingHeaderFooter: Boolean) {
        this.mPaddingHeaderFooter = mPaddingHeaderFooter
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        var spanCount = 0
        var orientation = 0
        var spanIndex = 0
        var headerCount = 0
        var footerCount = 0
        if (parent.adapter is RecyclerArrayAdapter<*>) {
            headerCount = (parent.adapter as RecyclerArrayAdapter<*>).headerCount
            footerCount = (parent.adapter as RecyclerArrayAdapter<*>).footerCount
        }

        val layoutManager = parent.layoutManager
        if (layoutManager is StaggeredGridLayoutManager) {
            orientation = layoutManager.orientation
            spanCount = layoutManager.spanCount
            spanIndex = (view.layoutParams as StaggeredGridLayoutManager.LayoutParams).spanIndex
        } else if (layoutManager is GridLayoutManager) {
            orientation = layoutManager.orientation
            spanCount = layoutManager.spanCount
            spanIndex = (view.layoutParams as GridLayoutManager.LayoutParams).spanIndex
        } else if (layoutManager is LinearLayoutManager) {
            orientation = layoutManager.orientation
            spanCount = 1
            spanIndex = 0
        }

        /**
         * 普通Item的尺寸
         */
        if (position >= headerCount && position < parent.adapter!!.itemCount - footerCount) {
            val gravity: Int
            if (spanIndex == 0 && spanCount > 1)
                gravity = Gravity.LEFT
            else if (spanIndex == spanCount - 1 && spanCount > 1)
                gravity = Gravity.RIGHT
            else if (spanCount == 1)
                gravity = Gravity.FILL_HORIZONTAL
            else {
                gravity = Gravity.CENTER
            }
            if (orientation == OrientationHelper.VERTICAL) {
                when (gravity) {
                    Gravity.LEFT -> {
                        if (mPaddingEdgeSide)
                            outRect.left = halfSpace * 2
                        outRect.right = halfSpace
                    }
                    Gravity.RIGHT -> {
                        outRect.left = halfSpace
                        if (mPaddingEdgeSide)
                            outRect.right = halfSpace * 2
                    }
                    Gravity.FILL_HORIZONTAL -> if (mPaddingEdgeSide) {
                        outRect.left = halfSpace * 2
                        outRect.right = halfSpace * 2
                    }
                    Gravity.CENTER -> {
                        outRect.left = halfSpace
                        outRect.right = halfSpace
                    }
                }
                if (position - headerCount < spanCount && mPaddingStart) outRect.top = halfSpace * 2
                outRect.bottom = halfSpace * 2
            } else {
                when (gravity) {
                    Gravity.LEFT -> {
                        if (mPaddingEdgeSide)
                            outRect.bottom = halfSpace * 2
                        outRect.top = halfSpace
                    }
                    Gravity.RIGHT -> {
                        outRect.bottom = halfSpace
                        if (mPaddingEdgeSide)
                            outRect.top = halfSpace * 2
                    }
                    Gravity.FILL_HORIZONTAL -> if (mPaddingEdgeSide) {
                        outRect.left = halfSpace * 2
                        outRect.right = halfSpace * 2
                    }
                    Gravity.CENTER -> {
                        outRect.bottom = halfSpace
                        outRect.top = halfSpace
                    }
                }
                if (position - headerCount < spanCount && mPaddingStart) outRect.left = halfSpace * 2
                outRect.right = halfSpace * 2
            }
        } else {//只有HeaderFooter进到这里
            if (mPaddingHeaderFooter) {//并且需要padding Header&Footer
                if (orientation == OrientationHelper.VERTICAL) {
                    if (mPaddingEdgeSide) {
                        outRect.left = halfSpace * 2
                        outRect.right = halfSpace * 2
                    }
                    outRect.top = halfSpace * 2
                } else {
                    if (mPaddingEdgeSide) {
                        outRect.top = halfSpace * 2
                        outRect.bottom = halfSpace * 2
                    }
                    outRect.left = halfSpace * 2
                }
            }
        }
    }


}