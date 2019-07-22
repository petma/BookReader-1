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
import com.justwayward.reader.bean.BookMixAToc
import com.justwayward.reader.bean.ChapterRead
import com.justwayward.reader.bean.Recommend

/**
 * @author lfh.
 * @date 2016/8/7.
 */
interface BookReadContract {

    interface View : BaseContract.BaseView {
        fun showBookToc(list: List<BookMixAToc.mixToc.Chapters>)

        fun showChapterRead(data: ChapterRead.Chapter, chapter: Int)

        fun netError(chapter: Int) //添加网络处理异常接口
    }

    interface Presenter<T> : BaseContract.BasePresenter<T> {
        fun getBookMixAToc(bookId: String, view: String)

        fun getChapterRead(url: String, chapter: Int)

        /**
         * 合成所有的文件为一个整个text
         * @param recommendBooks
         * @param list
         */
        fun merginAllBook(recommendBooks: Recommend.RecommendBooks, list: List<BookMixAToc.mixToc.Chapters>)
    }

}
