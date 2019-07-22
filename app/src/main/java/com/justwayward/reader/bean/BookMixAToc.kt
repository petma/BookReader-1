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

import java.io.Serializable

/**
 * @author lfh.
 * @date 2016/8/7.
 */
class BookMixAToc : Base() {

    /**
     * _id:577e528e2160421a02d7380d
     * name:优质书源
     * link:http://vip.zhuishushenqi.com/toc/577e528e2160421a02d7380d
     */
    var mixToc: mixToc? = null

    class mixToc : Serializable {
        var _id: String? = null
        var book: String? = null
        var chaptersUpdated: String? = null
        /**
         * title : 第一章 死在万花丛中
         * link : http://vip.zhuishushenqi.com/chapter/577e5290260289ff64a29213?cv=1467896464908
         * id : 577e5290260289ff64a29213
         * currency : 15
         * unreadble : false
         * isVip : false
         */

        var chapters: List<Chapters>? = null

        class Chapters : Serializable {
            var title: String
            var link: String
            var id: String? = null
            var currency: Int = 0
            var unreadble: Boolean = false
            var isVip: Boolean = false

            constructor() {}

            constructor(title: String, link: String) {
                this.title = title
                this.link = link
            }
        }
    }

}
