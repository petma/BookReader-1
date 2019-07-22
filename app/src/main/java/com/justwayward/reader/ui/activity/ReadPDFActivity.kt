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
package com.justwayward.reader.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.LinearLayout

import com.justwayward.reader.R
import com.justwayward.reader.base.BaseActivity
import com.justwayward.reader.component.AppComponent
import com.justwayward.reader.view.pdfview.PDFViewPager

import java.io.File

import butterknife.Bind

class ReadPDFActivity : BaseActivity() {

    @Bind(R.id.llPdfRoot)
    internal var llPdfRoot: LinearLayout? = null
    private val startX = 0
    private val startY = 0

    override val layoutId: Int
        get() = R.layout.activity_read_pdf

    override fun setupActivityComponent(appComponent: AppComponent?) {

    }

    override fun initToolBar() {
        val filePath = Uri.decode(intent.dataString!!.replace("file://", ""))
        val fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."))
        mCommonToolbar!!.title = fileName
        mCommonToolbar!!.setNavigationIcon(R.drawable.ab_back)
    }

    override fun initDatas() {
        if (Intent.ACTION_VIEW == intent.action) {
            val filePath = Uri.decode(intent.dataString!!.replace("file://", ""))

            val pdfViewPager = PDFViewPager(this, filePath)
            llPdfRoot!!.addView(pdfViewPager)
        }
    }

    override fun configViews() {

    }

    companion object {

        fun start(context: Context, filePath: String) {
            val intent = Intent(context, ReadPDFActivity::class.java)
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.fromFile(File(filePath))
            context.startActivity(intent)
        }
    }
}
