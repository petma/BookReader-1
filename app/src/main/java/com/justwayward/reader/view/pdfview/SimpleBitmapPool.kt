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

import android.graphics.Bitmap
import android.graphics.Color

class SimpleBitmapPool(params: PdfRendererParams) : BitmapContainer {

    internal var bitmaps: Array<Bitmap>

    private val poolSize: Int

    private val width: Int

    private val height: Int

    private val config: Bitmap.Config

    init {
        this.poolSize = getPoolSize(params.offScreenSize)
        this.width = params.width
        this.height = params.height
        this.config = params.config
        this.bitmaps = arrayOfNulls(poolSize)
    }

    private fun getPoolSize(offScreenSize: Int): Int {
        return offScreenSize * 2 + 1
    }

    fun getBitmap(position: Int): Bitmap {
        val index = getIndexFromPosition(position)
        if (bitmaps[index] == null) {
            createBitmapAtIndex(index)
        }

        bitmaps[index].eraseColor(Color.TRANSPARENT)

        return bitmaps[index]
    }

    override fun get(position: Int): Bitmap {
        return getBitmap(position)
    }

    override fun remove(position: Int) {
        bitmaps[position].recycle()
        bitmaps[position] = null
    }

    override fun clear() {
        recycleAll()
    }

    protected fun recycleAll() {
        for (i in 0 until poolSize) {
            if (bitmaps[i] != null) {
                bitmaps[i].recycle()
                bitmaps[i] = null
            }
        }
    }

    protected fun createBitmapAtIndex(index: Int) {
        val bitmap = Bitmap.createBitmap(width, height, config)
        bitmaps[index] = bitmap
    }

    protected fun getIndexFromPosition(position: Int): Int {
        return position % poolSize
    }
}