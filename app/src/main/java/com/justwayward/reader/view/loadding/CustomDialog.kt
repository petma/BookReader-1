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
package com.justwayward.reader.view.loadding

import android.app.Activity
import android.app.Dialog
import android.content.Context
import androidx.core.content.ContextCompat
import android.view.View
import android.view.ViewGroup

import com.justwayward.reader.R

class CustomDialog @JvmOverloads constructor(context: Context, themeResId: Int = 0) : Dialog(context, themeResId) {
    companion object {

        fun instance(activity: Activity): CustomDialog {
            val v = View.inflate(activity, R.layout.common_progress_view, null) as LoadingView
            v.color = ContextCompat.getColor(activity, R.color.reader_menu_bg_color)
            val dialog = CustomDialog(activity, R.style.loading_dialog)
            dialog.setContentView(v,
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT)
            )
            return dialog
        }
    }
}
