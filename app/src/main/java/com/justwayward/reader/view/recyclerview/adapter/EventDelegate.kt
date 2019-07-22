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
package com.justwayward.reader.view.recyclerview.adapter

import android.view.View

/**
 * Created by Mr.Jude on 2015/8/18.
 */
interface EventDelegate {
    fun addData(length: Int)
    fun clear()

    fun stopLoadMore()
    fun pauseLoadMore()
    fun resumeLoadMore()

    fun setMore(view: View, listener: OnLoadMoreListener)
    fun setNoMore(view: View)
    fun setErrorMore(view: View)
}
