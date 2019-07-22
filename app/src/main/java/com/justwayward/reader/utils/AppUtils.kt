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
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Handler
import android.os.Looper

object AppUtils {

    var appContext: Context? = null
        private set
    private var mUiThread: Thread? = null

    private val sHandler = Handler(Looper.getMainLooper())

    val assets: AssetManager
        get() = appContext!!.assets

    val resource: Resources
        get() = appContext!!.resources

    val isUIThread: Boolean
        get() = Thread.currentThread() === mUiThread

    fun init(context: Context) {
        appContext = context
        mUiThread = Thread.currentThread()
    }

    fun runOnUI(r: Runnable) {
        sHandler.post(r)
    }

    fun runOnUIDelayed(r: Runnable, delayMills: Long) {
        sHandler.postDelayed(r, delayMills)
    }

    fun removeRunnable(r: Runnable?) {
        if (r == null) {
            sHandler.removeCallbacksAndMessages(null)
        } else {
            sHandler.removeCallbacks(r)
        }
    }
}
