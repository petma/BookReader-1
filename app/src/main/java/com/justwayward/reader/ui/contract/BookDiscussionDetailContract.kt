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
import com.justwayward.reader.bean.CommentList
import com.justwayward.reader.bean.Disscussion

/**
 * @author yuyh.
 * @date 16/9/2.
 */
interface BookDiscussionDetailContract {

    interface View : BaseContract.BaseView {

        fun showBookDisscussionDetail(disscussion: Disscussion)

        fun showBestComments(list: CommentList)

        fun showBookDisscussionComments(list: CommentList)

    }

    interface Presenter : BaseContract.BasePresenter<View> {

        fun getBookDisscussionDetail(id: String)

        fun getBestComments(disscussionId: String)

        fun getBookDisscussionComments(disscussionId: String, start: Int, limit: Int)

    }

}
