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
package com.justwayward.reader.ui.contract

import com.justwayward.reader.base.BaseContract
import com.justwayward.reader.bean.HotReview

/**
 * @author lfh.
 * @date 2016/9/7.
 */
interface BookDetailReviewContract {

    interface View : BaseContract.BaseView {
        fun showBookDetailReviewList(list: List<HotReview.Reviews>, isRefresh: Boolean)
    }

    interface Presenter<T> : BaseContract.BasePresenter<T> {
        fun getBookDetailReviewList(bookId: String, sort: String, start: Int, limit: Int)
    }
}
