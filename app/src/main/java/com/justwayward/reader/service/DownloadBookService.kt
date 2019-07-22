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
package com.justwayward.reader.service

import android.app.Service
import android.content.Intent
import android.os.AsyncTask
import android.os.IBinder
import androidx.annotation.MainThread
import android.text.TextUtils

import com.justwayward.reader.R
import com.justwayward.reader.ReaderApplication
import com.justwayward.reader.api.BookApi
import com.justwayward.reader.api.support.Logger
import com.justwayward.reader.api.support.LoggingInterceptor
import com.justwayward.reader.bean.BookMixAToc
import com.justwayward.reader.bean.ChapterRead
import com.justwayward.reader.bean.support.DownloadMessage
import com.justwayward.reader.bean.support.DownloadProgress
import com.justwayward.reader.bean.support.DownloadQueue
import com.justwayward.reader.manager.CacheManager
import com.justwayward.reader.utils.AppUtils
import com.justwayward.reader.utils.LogUtils
import com.justwayward.reader.utils.NetworkUtils

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.util.ArrayList

import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

/**
 * @author yuyh.
 * @date 16/8/13.
 */
class DownloadBookService : Service() {

    var bookApi: BookApi
    protected var mCompositeSubscription: CompositeSubscription? = null

    var isBusy = false // 当前是否有下载任务在进行

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        val logging = LoggingInterceptor(Logger())
        logging.level = LoggingInterceptor.Level.BASIC
        bookApi = ReaderApplication.getsInstance()!!.appComponent!!.readerApi
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        unSubscribe()
        EventBus.getDefault().unregister(this)
    }

    fun post(progress: DownloadProgress) {
        EventBus.getDefault().post(progress)
    }

    private fun post(message: DownloadMessage) {
        EventBus.getDefault().post(message)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @Synchronized
    fun addToDownloadQueue(queue: DownloadQueue) {
        if (!TextUtils.isEmpty(queue.bookId)) {
            var exists = false
            // 判断当前书籍缓存任务是否存在
            for (i in downloadQueues.indices) {
                if (downloadQueues[i].bookId == queue.bookId) {
                    LogUtils.e("addToDownloadQueue:exists")
                    exists = true
                    break
                }
            }
            if (exists) {
                post(DownloadMessage(queue.bookId, "当前缓存任务已存在", false))
                return
            }

            // 添加到下载队列
            downloadQueues.add(queue)
            LogUtils.e("addToDownloadQueue:" + queue.bookId)
            post(DownloadMessage(queue.bookId, "成功加入缓存队列", false))
        }
        // 从队列顺序取出第一条下载
        if (downloadQueues.size > 0 && !isBusy) {
            isBusy = true
            val downloadQueue = downloadQueues[0]
            downloadBook(downloadQueue)
        }
    }


    @Synchronized
    fun downloadBook(downloadQueue: DownloadQueue) {
        val downloadTask = object : AsyncTask<Int, Int, Int>() {

            internal var list: List<BookMixAToc.mixToc.Chapters> = downloadQueue.list
            internal var bookId = downloadQueue.bookId
            internal var start = downloadQueue.start // 起始章节
            internal var end = downloadQueue.end // 结束章节

            protected override fun doInBackground(vararg params: Int): Int? {
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                }

                var failureCount = 0
                var i = start
                while (i <= end && i <= list.size) {
                    if (canceled) {
                        break
                    }
                    // 网络异常，取消下载
                    if (!NetworkUtils.isAvailable(AppUtils.appContext)) {
                        downloadQueue.isCancel = true
                        post(DownloadMessage(bookId, getString(R.string.book_read_download_error), true))
                        failureCount = -1
                        break
                    }
                    if (!downloadQueue.isFinish && !downloadQueue.isCancel) {
                        // 章节文件不存在,则下载，否则跳过
                        if (CacheManager.instance.getChapterFile(bookId, i) == null) {
                            val chapters = list[i - 1]
                            val url = chapters.link
                            val ret = download(url, bookId, chapters.title, i, list.size)
                            if (ret != 1) {
                                failureCount++
                            }
                        } else {
                            post(DownloadProgress(bookId, String.format(
                                    getString(R.string.book_read_alreday_download), list[i - 1].title, i, list.size),
                                    true))
                        }
                    }
                    i++
                }
                try {
                    Thread.sleep(500)
                } catch (e: InterruptedException) {
                }

                return failureCount
            }


            override fun onPostExecute(failureCount: Int?) {
                super.onPostExecute(failureCount)
                downloadQueue.isFinish = true
                if (failureCount > -1) {
                    // 完成通知
                    post(DownloadMessage(bookId,
                            String.format(getString(R.string.book_read_download_complete), failureCount), true))
                }
                // 下载完成，从队列里移除
                downloadQueues.remove(downloadQueue)
                // 释放 空闲状态
                isBusy = false
                if (!canceled) {
                    // post一个空事件，通知继续执行下一个任务
                    post(DownloadQueue())
                } else {
                    downloadQueues.clear()
                }
                canceled = false
                LogUtils.i(bookId + "缓存完成，失败" + failureCount + "章")
            }
        }
        downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    private fun download(url: String, bookId: String, title: String, chapter: Int, chapterSize: Int): Int {

        val result = intArrayOf(-1)

        val subscription = bookApi.getChapterRead(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ChapterRead> {
                    override fun onNext(data: ChapterRead) {
                        if (data.chapter != null) {
                            post(DownloadProgress(bookId, String.format(
                                    getString(R.string.book_read_download_progress), title, chapter, chapterSize),
                                    true))
                            CacheManager.instance.saveChapterFile(bookId, chapter, data.chapter)
                            result[0] = 1
                        } else {
                            result[0] = 0
                        }
                    }

                    override fun onCompleted() {
                        result[0] = 1
                    }

                    override fun onError(e: Throwable) {
                        result[0] = 0
                    }
                })

        addSubscrebe(subscription)

        while (result[0] == -1) {
            try {
                Thread.sleep(350)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }
        return result[0]
    }

    protected fun unSubscribe() {
        if (mCompositeSubscription != null) {
            mCompositeSubscription!!.unsubscribe()
        }
    }

    protected fun addSubscrebe(subscription: Subscription) {
        if (mCompositeSubscription == null) {
            mCompositeSubscription = CompositeSubscription()
        }
        mCompositeSubscription!!.add(subscription)
    }

    companion object {

        var downloadQueues: MutableList<DownloadQueue> = ArrayList()

        var canceled = false

        fun post(downloadQueue: DownloadQueue) {
            EventBus.getDefault().post(downloadQueue)
        }

        fun cancel() {
            canceled = true
        }
    }
}
