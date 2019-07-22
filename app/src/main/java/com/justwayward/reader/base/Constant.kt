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
package com.justwayward.reader.base

import android.graphics.Color
import androidx.annotation.StringDef

import com.justwayward.reader.utils.AppUtils
import com.justwayward.reader.utils.FileUtils

import org.apache.commons.collections4.map.HashedMap

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.ArrayList

/**
 * @author yuyh.
 * @date 16/8/5.
 */
object Constant {

    val IMG_BASE_URL = "http://statics.zhuishushenqi.com"

    val API_BASE_URL = "http://api.zhuishushenqi.com"

    var PATH_DATA = FileUtils.createRootPath(AppUtils.appContext) + "/cache"

    var PATH_COLLECT = FileUtils.createRootPath(AppUtils.appContext) + "/collect"

    var PATH_TXT = "$PATH_DATA/book/"

    var PATH_EPUB = "$PATH_DATA/epub"

    var PATH_CHM = "$PATH_DATA/chm"


    var BASE_PATH = AppUtils.appContext!!.getCacheDir().getPath()

    val ISNIGHT = "isNight"

    val ISBYUPDATESORT = "isByUpdateSort"
    val FLIP_STYLE = "flipStyle"

    val SUFFIX_TXT = ".txt"
    val SUFFIX_PDF = ".pdf"
    val SUFFIX_EPUB = ".epub"
    val SUFFIX_ZIP = ".zip"
    val SUFFIX_CHM = ".chm"

    val tagColors = intArrayOf(Color.parseColor("#90C5F0"), Color.parseColor("#91CED5"), Color.parseColor("#F88F55"), Color.parseColor("#C0AFD0"), Color.parseColor("#E78F8F"), Color.parseColor("#67CCB7"), Color.parseColor("#F6BC7E"))

    var sortTypeList: List<String> = object : ArrayList<String>() {
        init {
            add(SortType.DEFAULT)
            add(SortType.CREATED)
            add(SortType.COMMENT_COUNT)
            add(SortType.HELPFUL)
        }
    }

    var bookTypeList: List<String> = object : ArrayList<String>() {
        init {
            add(BookType.ALL)
            add(BookType.XHQH)
            add(BookType.WXXX)
            add(BookType.DSYN)
            add(BookType.LSJS)
            add(BookType.YXJJ)
            add(BookType.KHLY)
            add(BookType.CYJK)
            add(BookType.HMZC)
            add(BookType.XDYQ)
            add(BookType.GDYQ)
            add(BookType.HXYQ)
            add(BookType.DMTR)
        }
    }

    var bookType: Map<String, String> = object : HashedMap<String, String>() {
        init {
            put("qt", "其他")
            put(BookType.XHQH, "玄幻奇幻")
            put(BookType.WXXX, "武侠仙侠")
            put(BookType.DSYN, "都市异能")
            put(BookType.LSJS, "历史军事")
            put(BookType.YXJJ, "游戏竞技")
            put(BookType.KHLY, "科幻灵异")
            put(BookType.CYJK, "穿越架空")
            put(BookType.HMZC, "豪门总裁")
            put(BookType.XDYQ, "现代言情")
            put(BookType.GDYQ, "古代言情")
            put(BookType.HXYQ, "幻想言情")
            put(BookType.DMTR, "耽美同人")
        }
    }

    @StringDef(Gender.MALE, Gender.FEMALE)
    @Retention(RetentionPolicy.SOURCE)
    annotation class Gender {
        companion object {
            val MALE = "male"

            val FEMALE = "female"
        }
    }

    @StringDef(CateType.HOT, CateType.NEW, CateType.REPUTATION, CateType.OVER)
    @Retention(RetentionPolicy.SOURCE)
    annotation class CateType {
        companion object {
            val HOT = "hot"

            val NEW = "new"

            val REPUTATION = "reputation"

            val OVER = "over"
        }
    }

    @StringDef(Distillate.ALL, Distillate.DISTILLATE)
    @Retention(RetentionPolicy.SOURCE)
    annotation class Distillate {
        companion object {
            val ALL = ""

            val DISTILLATE = "true"
        }
    }

    @StringDef(SortType.DEFAULT, SortType.COMMENT_COUNT, SortType.CREATED, SortType.HELPFUL)
    @Retention(RetentionPolicy.SOURCE)
    annotation class SortType {
        companion object {
            val DEFAULT = "updated"

            val CREATED = "created"

            val HELPFUL = "helpful"

            val COMMENT_COUNT = "comment-count"
        }
    }

    @StringDef(BookType.ALL, BookType.XHQH, BookType.WXXX, BookType.DSYN, BookType.LSJS, BookType.YXJJ, BookType.KHLY, BookType.CYJK, BookType.HMZC, BookType.XDYQ, BookType.GDYQ, BookType.HXYQ, BookType.DMTR)
    @Retention(RetentionPolicy.SOURCE)
    annotation class BookType {
        companion object {
            val ALL = "all"

            val XHQH = "xhqh"

            val WXXX = "wxxx"

            val DSYN = "dsyn"

            val LSJS = "lsjs"

            val YXJJ = "yxjj"

            val KHLY = "khly"

            val CYJK = "cyjk"

            val HMZC = "hmzc"

            val XDYQ = "xdyq"

            val GDYQ = "gdyq"

            val HXYQ = "hxyq"

            val DMTR = "dmtr"
        }
    }
}
