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
package com.justwayward.reader.bean.support

import com.justwayward.reader.bean.BookMixAToc

import java.io.Serializable

/**
 * 下载队列实体
 *
 * @author yuyh.
 * @date 16/8/13.
 */
class DownloadQueue : Serializable {

    var bookId: String

    var list: List<BookMixAToc.mixToc.Chapters>

    var start: Int = 0

    var end: Int = 0

    /**
     * 是否已经开始下载
     */
    var isStartDownload = false

    /**
     * 是否中断下载
     */
    var isCancel = false

    /**
     * 是否下载完成
     */
    var isFinish = false

    constructor(bookId: String, list: List<BookMixAToc.mixToc.Chapters>, start: Int, end: Int) {
        this.bookId = bookId
        this.list = list
        this.start = start
        this.end = end
    }

    /**
     * 空事件。表示通知继续执行下一条任务
     */
    constructor() {}
}
