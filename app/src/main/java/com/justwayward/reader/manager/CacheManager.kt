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
package com.justwayward.reader.manager

import android.content.Context
import android.text.TextUtils
import android.util.Log

import com.justwayward.reader.ReaderApplication
import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.BookLists
import com.justwayward.reader.bean.BookMixAToc
import com.justwayward.reader.bean.ChapterRead
import com.justwayward.reader.utils.ACache
import com.justwayward.reader.utils.AppUtils
import com.justwayward.reader.utils.FileUtils
import com.justwayward.reader.utils.LogUtils
import com.justwayward.reader.utils.SharedPreferencesUtil
import com.justwayward.reader.utils.StringUtils
import com.justwayward.reader.utils.ToastUtils

import java.io.File
import java.io.Serializable
import java.util.ArrayList

/**
 * @author yuyh.
 * @date 2016/9/28.
 */
class CacheManager {

    val searchHistory: List<String>?
        get() = SharedPreferencesUtil.instance!!.getObject(searchHistoryKey, List<*>::class.java)

    private val searchHistoryKey: String
        get() = "searchHistory"

    /**
     * 获取我收藏的书单列表
     *
     * @return
     */
    val collectionList: MutableList<BookLists.BookListsBean>?
        get() = ACache.get(
                ReaderApplication.getsInstance()).getAsObject(collectionKey) as ArrayList<BookLists.BookListsBean>

    private val collectionKey: String
        get() = "my_book_lists"

    /**
     * 获取缓存大小
     *
     * @return
     */
    val cacheSize: String
        @Synchronized get() {
            var cacheSize: Long = 0

            try {
                val cacheDir = Constant.BASE_PATH
                cacheSize += FileUtils.getFolderSize(cacheDir)
                if (FileUtils.isSdCardAvailable) {
                    val extCacheDir = AppUtils.appContext!!.getExternalCacheDir()!!.getPath()
                    cacheSize += FileUtils.getFolderSize(extCacheDir)
                }
            } catch (e: Exception) {
                LogUtils.e(e.toString())
            }

            return FileUtils.formatFileSizeToString(cacheSize)
        }

    fun saveSearchHistory(obj: Any) {
        SharedPreferencesUtil.instance!!.putObject(searchHistoryKey, obj)
    }

    fun removeCollection(bookListId: String) {
        val list = collectionList ?: return
        for (bean in list) {
            if (bean != null) {
                if (TextUtils.equals(bean._id, bookListId)) {
                    list.remove(bean)
                    ACache.get(ReaderApplication.getsInstance()).put(collectionKey, list as Serializable)
                    break
                }
            }
        }
    }

    fun addCollection(bean: BookLists.BookListsBean) {
        var list: MutableList<BookLists.BookListsBean>? = collectionList
        if (list == null) {
            list = ArrayList()
        }
        for (data in list) {
            if (data != null) {
                if (TextUtils.equals(data._id, bean._id)) {
                    ToastUtils.showToast("已经收藏过啦")
                    return
                }
            }
        }
        list.add(bean)
        ACache.get(ReaderApplication.getsInstance()).put(collectionKey, list as Serializable?)
        ToastUtils.showToast("收藏成功")
    }

    /**
     * 获取目录缓存
     *
     * @param mContext
     * @param bookId
     * @return
     */
    fun getTocList(mContext: Context, bookId: String): List<BookMixAToc.mixToc.Chapters>? {
        val obj = ACache.get(mContext).getAsObject(getTocListKey(bookId))
        if (obj != null) {
            try {
                val data = obj as BookMixAToc
                val list = data.mixToc!!.chapters
                if (list != null && !list.isEmpty()) {
                    return list
                }
            } catch (e: Exception) {
                ACache.get(mContext).remove(getTocListKey(bookId))
            }

        }
        return null
    }

    fun saveTocList(mContext: Context, bookId: String, data: BookMixAToc) {
        ACache.get(mContext).put(getTocListKey(bookId), data)
    }

    fun removeTocList(mContext: Context, bookId: String) {
        ACache.get(mContext).remove(getTocListKey(bookId))
    }

    private fun getTocListKey(bookId: String): String {
        return "$bookId-bookToc"
    }

    fun getChapterFile(bookId: String, chapter: Int): File? {
        val file = FileUtils.getChapterFile(bookId, chapter)
        return if (file != null && file.length() > 50) file else null
    }

    fun saveChapterFile(bookId: String, chapter: Int, data: ChapterRead.Chapter) {
        val file = FileUtils.getChapterFile(bookId, chapter)
        FileUtils.writeFile(file.absolutePath, StringUtils.formatContent(data.body), false)
    }

    /**
     * 清除缓存
     *
     * @param clearReadPos 是否删除阅读记录
     */
    @Synchronized
    fun clearCache(clearReadPos: Boolean, clearCollect: Boolean) {
        try {
            // 删除内存缓存
            val cacheDir = AppUtils.appContext!!.getCacheDir().getPath()
            FileUtils.deleteFileOrDirectory(File(cacheDir))
            if (FileUtils.isSdCardAvailable) {
                // 删除SD书籍缓存
                FileUtils.deleteFileOrDirectory(File(Constant.PATH_DATA))
            }
            // 删除阅读记录（SharePreference）
            if (clearReadPos) {
                //防止再次弹出性别选择框，sp要重写入保存的性别
                val chooseSex = SettingManager.instance!!.userChooseSex
                SharedPreferencesUtil.instance!!.removeAll()
                SettingManager.instance!!.saveUserChooseSex(chooseSex)
            }
            // 清空书架
            if (clearCollect) {
                CollectionsManager.instance!!.clear()
            }
            // 清除其他缓存
            ACache.get(AppUtils.appContext).clear()
        } catch (e: Exception) {
            LogUtils.e(e.toString())
        }

    }

    companion object {

        private var manager: CacheManager? = null

        val instance: CacheManager
            get() = if (manager == null) manager = CacheManager() else manager
    }

}
