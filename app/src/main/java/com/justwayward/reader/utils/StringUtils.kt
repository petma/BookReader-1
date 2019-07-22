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
package com.justwayward.reader.utils

/**
 * Created by lfh on 2016/9/10.
 */
object StringUtils {

    /**
     * Return a String that only has two spaces.
     *
     * @return
     */
    val twoSpaces: String
        get() = "\u3000\u3000"

    fun creatAcacheKey(vararg param: Any): String {
        var key = ""
        for (o in param) {
            key += "-$o"
        }
        return key.replaceFirst("-".toRegex(), "")
    }

    /**
     * 格式化小说内容。
     *
     *
     *  * 小说的开头，缩进2格。在开始位置，加入2格空格。
     *  * 所有的段落，缩进2格。所有的\n,替换为2格空格。
     *
     * @param str
     * @return
     */
    fun formatContent(str: String): String {
        var str = str
        str = str.replace("[ ]*".toRegex(), "")//替换来自服务器上的，特殊空格
        str = str.replace("[ ]*".toRegex(), "")//
        str = str.replace("\n\n", "\n")
        str = str.replace("\n", "\n" + twoSpaces)
        str = twoSpaces + str
        //        str = convertToSBC(str);
        return str
    }

}
