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
 * 综合讨论
 */
class DiscussionList : Base() {


    /**
     * _id : 57c564e9818f568675624358
     * title : 【活动预告】9月2-4日三天两觉《惊悚乐园》签名书抢楼
     * author : {"_id":"52f840b982cfcc3a74031693",
     * "avatar":"/avatar/56/a9/56a96462a50ca99f9cf83440899e46f3","nickname":"追书首席打杂",
     * "type":"official","lv":9,"gender":"male"}
     * type : vote
     * likeCount : 651
     * block : ramble
     * state : hot
     * updated : 2016-09-01T12:30:52.286Z
     * created : 2016-08-30T10:50:17.927Z
     * commentCount : 5366
     * voteCount : 6337
     */

    var posts: List<PostsBean>? = null

    class PostsBean {
        var _id: String? = null
        var title: String? = null
        /**
         * _id : 52f840b982cfcc3a74031693
         * avatar : /avatar/56/a9/56a96462a50ca99f9cf83440899e46f3
         * nickname : 追书首席打杂
         * type : official
         * lv : 9
         * gender : male
         */

        var author: AuthorBean? = null
        var type: String? = null
        var likeCount: Int = 0
        var block: String? = null
        var state: String? = null
        var updated: String? = null
        var created: String? = null
        var commentCount: Int = 0
        var voteCount: Int = 0

        class AuthorBean {
            var _id: String? = null
            var avatar: String? = null
            var nickname: String? = null
            var type: String? = null
            var lv: Int = 0
            var gender: String? = null
        }
    }
}
