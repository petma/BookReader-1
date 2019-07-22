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

import android.text.TextUtils

import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.Recommend
import com.justwayward.reader.ui.presenter.MainActivityPresenter
import com.justwayward.reader.utils.ACache
import com.justwayward.reader.utils.AppUtils
import com.justwayward.reader.utils.FileUtils
import com.justwayward.reader.utils.FormatUtils
import com.justwayward.reader.utils.LogUtils
import com.justwayward.reader.utils.SharedPreferencesUtil

import java.io.File
import java.io.IOException
import java.io.Serializable
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

/**
 * 收藏列表管理
 * Created by lfh on 2016/9/22.
 */
class CollectionsManager private constructor() {

    /**
     * 获取收藏列表
     *
     * @return
     */
    val collectionList: MutableList<Recommend.RecommendBooks>?
        get() = ACache.get(File(Constant.PATH_COLLECT)).getAsObject("collection") as ArrayList<Recommend.RecommendBooks>

    /**
     * 按排序方式获取收藏列表
     *
     * @return
     */
    val collectionListBySort: List<Recommend.RecommendBooks>?
        get() {
            val list = collectionList
            if (list == null) {
                return null
            } else {
                if (SharedPreferencesUtil.instance!!.getBoolean(Constant.ISBYUPDATESORT, true)) {
                    Collections.sort(list, LatelyUpdateTimeComparator())
                } else {
                    Collections.sort(list, RecentReadingTimeComparator())
                }
                return list
            }
        }

    fun putCollectionList(list: List<Recommend.RecommendBooks>) {
        ACache.get(File(Constant.PATH_COLLECT)).put("collection", list as Serializable)
    }

    /**
     * 移除单个收藏
     *
     * @param bookId
     */
    fun remove(bookId: String) {
        val list = collectionList ?: return
        for (bean in list) {
            if (TextUtils.equals(bean._id, bookId)) {
                list.remove(bean)
                putCollectionList(list)
                break
            }
        }
        EventManager.refreshCollectionList()
    }

    /**
     * 是否已收藏
     *
     * @param bookId
     * @return
     */
    fun isCollected(bookId: String?): Boolean {
        val list = collectionList
        if (list == null || list.isEmpty()) {
            return false
        }
        for (bean in list) {
            if (bean._id == bookId) {
                return true
            }
        }
        return false
    }

    /**
     * 是否已置顶
     *
     * @param bookId
     * @return
     */
    fun isTop(bookId: String): Boolean {
        val list = collectionList
        if (list == null || list.isEmpty()) {
            return false
        }
        for (bean in list) {
            if (bean._id == bookId) {
                if (bean.isTop)
                    return true
            }
        }
        return false
    }

    /**
     * 移除多个收藏
     *
     * @param removeList
     */
    fun removeSome(removeList: List<Recommend.RecommendBooks>, removeCache: Boolean) {
        val list = collectionList ?: return
        if (removeCache) {
            for (book in removeList) {
                try {
                    // 移除章节文件
                    FileUtils.deleteFileOrDirectory(FileUtils.getBookDir(book._id))
                    // 移除目录缓存
                    CacheManager.instance.removeTocList(AppUtils.appContext, book._id)
                    // 移除阅读进度
                    SettingManager.instance!!.removeReadProgress(book._id)
                } catch (e: IOException) {
                    LogUtils.e(e.toString())
                }

            }
        }
        list.removeAll(removeList)
        putCollectionList(list)
    }

    /**
     * 加入收藏
     *
     * @param bean
     */
    fun add(bean: Recommend.RecommendBooks): Boolean {
        if (isCollected(bean._id)) {
            return false
        }
        var list: MutableList<Recommend.RecommendBooks>? = collectionList
        if (list == null) {
            list = ArrayList()
        }
        list.add(bean)
        putCollectionList(list)
        EventManager.refreshCollectionList()
        return true
    }

    /**
     * 置顶收藏、取消置顶
     *
     * @param bookId
     */
    fun top(bookId: String, isTop: Boolean) {
        val list = collectionList ?: return
        for (bean in list) {
            if (TextUtils.equals(bean._id, bookId)) {
                bean.isTop = isTop
                list.remove(bean)
                list.add(0, bean)
                putCollectionList(list)
                break
            }
        }
        EventManager.refreshCollectionList()
    }

    /**
     * 设置最新章节和更新时间
     *
     * @param bookId
     */
    @Synchronized
    fun setLastChapterAndLatelyUpdate(bookId: String, lastChapter: String, latelyUpdate: String) {
        val list = collectionList ?: return
        for (bean in list) {
            if (TextUtils.equals(bean._id, bookId)) {
                if (bean.lastChapter != lastChapter) {
                    MainActivityPresenter.isLastSyncUpdateed = true
                }
                bean.lastChapter = lastChapter
                bean.updated = latelyUpdate
                list.remove(bean)
                list.add(bean)
                putCollectionList(list)
                break
            }
        }
    }

    /**
     * 设置最近阅读时间
     *
     * @param bookId
     */
    fun setRecentReadingTime(bookId: String) {
        val list = collectionList ?: return
        for (bean in list) {
            if (TextUtils.equals(bean._id, bookId)) {
                bean.recentReadingTime = FormatUtils.getCurrentTimeString(FormatUtils.FORMAT_DATE_TIME)
                list.remove(bean)
                list.add(bean)
                putCollectionList(list)
                break
            }
        }
    }

    fun clear() {
        try {
            FileUtils.deleteFileOrDirectory(File(Constant.PATH_COLLECT))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    /**
     * 自定义比较器：按最近阅读时间来排序，置顶优先，降序
     */
    internal class RecentReadingTimeComparator : Comparator<*> {
        override fun compare(object1: Any, object2: Any): Int {
            val p1 = object1 as Recommend.RecommendBooks
            val p2 = object2 as Recommend.RecommendBooks
            return if (p1.isTop && p2.isTop || !p1.isTop && !p2.isTop) {
                p2.recentReadingTime.compareTo(p1.recentReadingTime)
            } else {
                if (p1.isTop) -1 else 1
            }
        }
    }

    /**
     * 自定义比较器：按更新时间来排序，置顶优先，降序
     */
    internal class LatelyUpdateTimeComparator : Comparator<*> {
        override fun compare(object1: Any, object2: Any): Int {
            val p1 = object1 as Recommend.RecommendBooks
            val p2 = object2 as Recommend.RecommendBooks
            return if (p1.isTop && p2.isTop || !p1.isTop && !p2.isTop) {
                p2.updated.compareTo(p1.updated)
            } else {
                if (p1.isTop) -1 else 1
            }
        }
    }

    companion object {

        @Volatile
        private var singleton: CollectionsManager? = null

        val instance: CollectionsManager?
            get() {
                if (singleton == null) {
                    synchronized(CollectionsManager::class.java) {
                        if (singleton == null) {
                            singleton = CollectionsManager()
                        }
                    }
                }
                return singleton
            }
    }

}