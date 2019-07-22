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
 * Created by lfh on 2016/8/15.
 */
class BookLists : Base() {

    /**
     * _id : 57a83783c9b799011623ecff
     * title : 【追书盘点】男频类型文（六）体育类竞技文
     * author : 追书白小生
     * desc : 在体育竞技的赛场上！
     * 运动员们，因为一个坚定的信念，而不断前行，努力，付出。
     * 他们的目标只有一个：升级！
     * 当冠军，收小弟，在体育的大道上，走向人生的巅峰！
     *
     * 本次就让我们来盘点一下，那些正值火热的体育类竞技文吧。
     * 【排名不分先后】
     * gender : male
     * collectorCount : 2713
     * cover : /agent/http://image.cmfu.com/books/3623405/3623405.jpg
     * bookCount : 20
     */

    var bookLists: List<BookListsBean>? = null

    inner class BookListsBean : Serializable {
        var _id: String? = null
        var title: String? = null
        var author: String? = null
        var desc: String? = null
        var gender: String? = null
        var collectorCount: Int = 0
        var cover: String? = null
        var bookCount: Int = 0
    }
}
