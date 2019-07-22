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
package com.justwayward.reader.utils

import android.content.Context
import android.widget.Toast

/**
 * Toast工具类，解决多个Toast同时出现的问题
 *
 * @author yuyh.
 * @date 16/4/9.
 */
object ToastUtils {

    private var mToast: Toast? = null
    private val context = AppUtils.appContext

    /********************** 非连续弹出的Toast  */
    fun showSingleToast(resId: Int) { //R.string.**
        getSingleToast(resId, Toast.LENGTH_SHORT)!!.show()
    }

    fun showSingleToast(text: String) {
        getSingleToast(text, Toast.LENGTH_SHORT)!!.show()
    }

    fun showSingleLongToast(resId: Int) {
        getSingleToast(resId, Toast.LENGTH_LONG)!!.show()
    }

    fun showSingleLongToast(text: String) {
        getSingleToast(text, Toast.LENGTH_LONG)!!.show()
    }

    /*********************** 连续弹出的Toast  */
    fun showToast(resId: Int) {
        getToast(resId, Toast.LENGTH_SHORT).show()
    }

    fun showToast(text: String) {
        getToast(text, Toast.LENGTH_SHORT).show()
    }

    fun showLongToast(resId: Int) {
        getToast(resId, Toast.LENGTH_LONG).show()
    }

    fun showLongToast(text: String) {
        getToast(text, Toast.LENGTH_LONG).show()
    }

    fun getSingleToast(resId: Int, duration: Int): Toast? { // 连续调用不会连续弹出，只是替换文本
        return getSingleToast(context!!.getResources().getText(resId).toString(), duration)
    }

    fun getSingleToast(text: String, duration: Int): Toast? {
        if (mToast == null) {
            mToast = Toast.makeText(context, text, duration)
        } else {
            mToast!!.setText(text)
        }
        return mToast
    }

    fun getToast(resId: Int, duration: Int): Toast { // 连续调用会连续弹出
        return getToast(context!!.getResources().getText(resId).toString(), duration)
    }

    fun getToast(text: String, duration: Int): Toast {
        return Toast.makeText(context, text, duration)
    }
}
