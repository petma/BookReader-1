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

import com.justwayward.reader.base.Constant

/**
 * @author yuyh.
 * @date 16/9/2.
 */
class SelectionEvent {

    var distillate: String

    var type: String

    var sort: String

    constructor(@Constant.Distillate distillate: String,
                @Constant.BookType type: String,
                @Constant.SortType sort: String) {
        this.distillate = distillate
        this.type = type
        this.sort = sort
    }

    constructor(@Constant.SortType sort: String) {
        this.sort = sort
    }
}
