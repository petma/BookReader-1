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
class BooksByTag : Base() {


    /**
     * _id : 559b29837a0f8b65521464a7
     * title : 申公豹传承
     * author : 第九天命
     * shortIntro : 本书主角玉独秀获得应灾劫大道而生的申公豹传承，然后又在无意间融合了一丝诸天劫难本源，有了执掌、引动大劫之力量，为众生带来劫难，可以借助大劫，来加快自己的修炼速，...
     * cover : /agent/http://image.cmfu.com/books/3533952/3533952.jpg
     * cat : 仙侠
     * majorCate : 仙侠
     * minorCate : 洪荒封神
     * latelyFollower : 776
     * retentionRatio : 69.68
     * lastChapter : 第1337章 狐媚子
     * tags : ["洪荒封神","仙侠"]
     */

    var books: List<TagBook>? = null

    class TagBook {
        var _id: String? = null
        var title: String? = null
        var author: String? = null
        var shortIntro: String? = null
        var cover: String? = null
        var site: String? = null
        var cat: String? = null
        var majorCate: String? = null
        var minorCate: String? = null
        var latelyFollower: Int = 0
        var retentionRatio: String? = null
        var lastChapter: String? = null
        var tags: List<String>? = null
    }
}
