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
 * Created by Administrator on 2016/8/15.
 */
class BooksByCats : Base() {
    /**
     * _id : 555abb2d91d0eb814e5db04f
     * title : 全职法师
     * author : 乱
     * shortIntro : 一觉醒来，世界大变。 熟悉的高中传授的是魔法，告诉大家要成为一名出色的魔法师。 居住的都市之外游荡着袭击人类的魔物妖兽，虎视眈眈。
     * 崇尚科学的世界变成了崇尚魔法...
     * cover : /agent/http://image.cmfu.com/books/3489766/3489766.jpg
     * site : zhuishuvip
     * majorCate : 玄幻
     * latelyFollower : 109257
     * latelyFollowerBase : 0
     * minRetentionRatio : 0
     * retentionRatio : 72.88
     * lastChapter : 第1173章 文泰之死
     * tags : ["腹黑","玄幻","异界大陆"]
     */

    var books: List<BooksBean>? = null

    class BooksBean(var _id: String, var cover: String, var title: String, var author: String, var majorCate: String, var shortIntro: String, var latelyFollower: Int, var retentionRatio: String) {
        var site: String? = null
        var latelyFollowerBase: Int = 0
        var minRetentionRatio: String? = null
        var lastChapter: String? = null
        var tags: List<String>? = null
    }
}
