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
 * 评论列表
 */
class CommentList : Base() {


    /**
     * _id : 57bf0be8c2099ef01d9d35d6
     * content : 看过 但不是很喜欢 太压抑 虽然感情戏写的不错 但虐过头了
     * author : {"_id":"55c43916ad75a05059fd23d7",
     * "avatar":"/avatar/5a/1f/5a1fb41215c3e7f9cedb8310ad76d3d8","nickname":"恋旧的人",
     * "type":"normal","lv":8,"gender":"female"}
     * floor : 105
     * likeCount : 0
     * created : 2016-08-25T15:16:56.437Z
     * replyTo : {"_id":"57bf04e65889e74a6f0808bf","floor":104,
     * "author":{"_id":"5794ad7ffda61987396d6216","nickname":"从未改变"}}
     */

    var comments: List<CommentsBean>? = null

    class CommentsBean {
        var _id: String? = null
        var content: String? = null
        /**
         * _id : 55c43916ad75a05059fd23d7
         * avatar : /avatar/5a/1f/5a1fb41215c3e7f9cedb8310ad76d3d8
         * nickname : 恋旧的人
         * type : normal
         * lv : 8
         * gender : female
         */

        var author: AuthorBean? = null
        var floor: Int = 0
        var likeCount: Int = 0
        var created: String? = null
        /**
         * _id : 57bf04e65889e74a6f0808bf
         * floor : 104
         * author : {"_id":"5794ad7ffda61987396d6216","nickname":"从未改变"}
         */

        var replyTo: ReplyToBean? = null

        class AuthorBean {
            var _id: String? = null
            var avatar: String? = null
            var nickname: String? = null
            var type: String? = null
            var lv: Int = 0
            var gender: String? = null
        }

        class ReplyToBean {
            var _id: String? = null
            var floor: Int = 0
            /**
             * _id : 5794ad7ffda61987396d6216
             * nickname : 从未改变
             */

            var author: AuthorBean? = null

            class AuthorBean {
                var _id: String? = null
                var nickname: String? = null
            }
        }
    }
}
