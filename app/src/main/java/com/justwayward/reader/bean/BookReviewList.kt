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
package com.justwayward.reader.bean

import com.justwayward.reader.bean.base.Base

/**
 * Created by lfh on 2016/9/1.
 * 书评列表
 */
class BookReviewList : Base() {


    /**
     * _id : 57c78c949869613834c1c519
     * title : 【书评两星，主要是不爱看这类的书，没有贬低此书的意思，见谅】
     * book : {"_id":"5086510f9dacd30e3a000026","cover":"/agent/http://image.cmfu
     * .com/books/109222/109222.jpg","title":"无限恐怖","site":"zhuishuvip","type":"khly"}
     * helpful : {"total":-38,"no":45,"yes":7}
     * likeCount : 1
     * state : normal
     * updated : 2016-09-01T13:07:57.530Z
     * created : 2016-09-01T02:04:04.487Z
     */

    var reviews: List<ReviewsBean>? = null

    class ReviewsBean {
        var _id: String? = null
        var title: String? = null
        /**
         * _id : 5086510f9dacd30e3a000026
         * cover : /agent/http://image.cmfu.com/books/109222/109222.jpg
         * title : 无限恐怖
         * site : zhuishuvip
         * type : khly
         */

        var book: BookBean? = null
        /**
         * total : -38
         * no : 45
         * yes : 7
         */

        var helpful: HelpfulBean? = null
        var likeCount: Int = 0
        var state: String? = null
        var updated: String? = null
        var created: String? = null

        class BookBean {
            var _id: String? = null
            var cover: String? = null
            var title: String? = null
            var site: String? = null
            var type: String? = null
        }

        class HelpfulBean {
            var total: Int = 0
            var no: Int = 0
            var yes: Int = 0
        }
    }
}
