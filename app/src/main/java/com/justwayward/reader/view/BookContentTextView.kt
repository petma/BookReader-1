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

import android.content.Context
import androidx.core.content.ContextCompat
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import android.widget.TextView

import com.justwayward.reader.R
import com.justwayward.reader.ui.activity.SearchActivity
import com.justwayward.reader.utils.LogUtils

/**
 * 识别文字中的书名
 *
 * @author yuyh.
 * @date 16/9/2.
 */
class BookContentTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TextView(context, attrs, defStyleAttr) {

    fun setText(text: String) {
        var text = text
        val startIndex = 0
        while (true) {

            val start = text.indexOf("《")
            val end = text.indexOf("》")
            if (start < 0 || end < 0) {
                append(text.substring(startIndex))
                break
            }

            append(text.substring(startIndex, start))

            val spanableInfo = SpannableString(text.substring(start, end + 1))
            spanableInfo.setSpan(Clickable(spanableInfo.toString()), 0, end + 1 - start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            append(spanableInfo)
            //setMovementMethod()该方法必须调用，否则点击事件不响应
            movementMethod = LinkMovementMethod.getInstance()
            text = text.substring(end + 1)

            LogUtils.e(spanableInfo.toString())
        }
    }

    internal inner class Clickable(private val name: String) : ClickableSpan() {

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = ContextCompat.getColor(context, R.color.light_coffee)
            ds.isUnderlineText = false
        }

        override fun onClick(v: View) {
            SearchActivity.startActivity(context, name.replace("》".toRegex(), "").replace("《".toRegex(), ""))
        }
    }
}
