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
 * @author yuyh.
 * @date 2016/8/4.
 */
class SearchDetail : Base() {


    /**
     * _id : 55e2850dbda85d9f74e6f73b
     * hasCp : false
     * title : w
     * cat : 奇幻
     * author : w
     * site : faloo
     * cover : /agent/http://img.faloo.com/Novel/166x235/0/71/000071091.jpg
     * shortIntro : 最终之海的传说，可怕的烧烧能力，危险的黑暗能力，还有恐怖的冰冰能力。  看小Down怎样找到去另一个世界的大门。本书绝对会全本，更新时间固定每周六晚 绝对大更。...
     * lastChapter : 抢劫海贼
     * retentionRatio : null
     * latelyFollower : 4
     * wordCount : 5075
     */

    var books: List<SearchBooks>? = null

    class SearchBooks {
        var _id: String
        var hasCp: Boolean = false
        var title: String
        var cat: String? = null
        var author: String
        var site: String? = null
        var cover: String
        var shortIntro: String? = null
        var lastChapter: String? = null
        var retentionRatio: String
        var latelyFollower: Int = 0
        var wordCount: Int = 0

        constructor(_id: String, title: String, author: String, cover: String, retentionRatio: String, latelyFollower: Int) {
            this._id = _id
            this.title = title
            this.author = author
            this.cover = cover
            this.retentionRatio = retentionRatio
            this.latelyFollower = latelyFollower
        }

        constructor() {}
    }
}
