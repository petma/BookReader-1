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
import android.graphics.Bitmap
import android.graphics.RectF
import android.graphics.pdf.PdfRenderer
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.justwayward.reader.R

import java.lang.ref.WeakReference

import uk.co.senab.photoview.PhotoViewAttacher

class PDFPagerAdapter(context: Context, pdfPath: String) : BasePDFPagerAdapter(context, pdfPath), PhotoViewAttacher.OnMatrixChangedListener {

    internal var attachers: SparseArray<WeakReference<PhotoViewAttacher>>? = null
    internal var scale = PdfScale()
    internal var pageClickListener: OnPageClickListener? = null

    init {
        attachers = SparseArray()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val v = inflater.inflate(R.layout.view_pdf_page, container, false)
        val iv = v.findViewById<View>(R.id.imageView) as ImageView

        if (renderer == null || count < position) {
            return v
        }

        val page = getPDFPage(renderer, position)

        val bitmap = bitmapContainer!!.get(position)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()

        val attacher = PhotoViewAttacher(iv)
        attacher.setScale(scale.scale, scale.centerX, scale.centerY, true)
        attacher.setOnMatrixChangeListener(this)

        attachers!!.put(position, WeakReference(attacher))

        iv.setImageBitmap(bitmap)
        attacher.onPhotoTapListener = PhotoViewAttacher.OnPhotoTapListener { view, x, y ->
            if (pageClickListener != null) {
                pageClickListener!!.onPageTap(view, x, y)
            }
        }
        attacher.update()
        container.addView(v, 0)

        return v
    }

    override fun close() {
        super.close()
        if (attachers != null) {
            attachers!!.clear()
            attachers = null
        }
    }

    override fun onMatrixChanged(rect: RectF) {
        if (scale.scale != PdfScale.DEFAULT_SCALE) {
            //            scale.setCenterX(rect.centerX());
            //            scale.setCenterY(rect.centerY());
        }
    }

    class Builder(internal var context: Context) {
        internal var pdfPath = ""
        internal var scale = DEFAULT_SCALE
        internal var centerX = 0f
        internal var centerY = 0f
        internal var offScreenSize = BasePDFPagerAdapter.DEFAULT_OFFSCREENSIZE
        internal var renderQuality = BasePDFPagerAdapter.DEFAULT_QUALITY
        internal var pageClickListener: OnPageClickListener

        fun setScale(scale: Float): Builder {
            this.scale = scale
            return this
        }

        fun setScale(scale: PdfScale): Builder {
            this.scale = scale.scale
            this.centerX = scale.centerX
            this.centerY = scale.centerY
            return this
        }

        fun setCenterX(centerX: Float): Builder {
            this.centerX = centerX
            return this
        }

        fun setCenterY(centerY: Float): Builder {
            this.centerY = centerY
            return this
        }

        fun setRenderQuality(renderQuality: Float): Builder {
            this.renderQuality = renderQuality
            return this
        }

        fun setOffScreenSize(offScreenSize: Int): Builder {
            this.offScreenSize = offScreenSize
            return this
        }

        fun setPdfPath(path: String): Builder {
            this.pdfPath = path
            return this
        }

        fun setOnPageClickListener(listener: OnPageClickListener?): Builder {
            if (listener != null) {
                pageClickListener = listener
            }
            return this
        }

        fun create(): PDFPagerAdapter {
            val adapter = PDFPagerAdapter(context, pdfPath)
            adapter.scale.scale = scale
            adapter.scale.centerX = centerX
            adapter.scale.centerY = centerY
            adapter.offScreenSize = offScreenSize
            adapter.renderQuality = renderQuality
            adapter.pageClickListener = pageClickListener
            return adapter
        }
    }

    companion object {

        private val DEFAULT_SCALE = 1f
    }
}
