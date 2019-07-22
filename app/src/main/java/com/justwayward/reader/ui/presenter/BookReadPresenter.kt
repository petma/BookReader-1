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
package com.justwayward.reader.ui.presenter

import android.content.Context

import com.justwayward.reader.api.BookApi
import com.justwayward.reader.base.RxPresenter
import com.justwayward.reader.bean.BookMixAToc
import com.justwayward.reader.bean.ChapterRead
import com.justwayward.reader.bean.Recommend
import com.justwayward.reader.bean.support.DownloadProgress
import com.justwayward.reader.ui.contract.BookReadContract
import com.justwayward.reader.utils.FileUtils
import com.justwayward.reader.utils.LogUtils
import com.justwayward.reader.utils.RxUtil
import com.justwayward.reader.utils.StringUtils

import java.io.File

import javax.inject.Inject

import rx.Observable
import rx.Observer
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Func1
import rx.internal.util.ObserverSubscriber
import rx.observables.SyncOnSubscribe
import rx.schedulers.Schedulers

/**
 * @author lfh.
 * @date 2016/8/7.
 */
class BookReadPresenter @Inject
constructor(private val mContext: Context, private val bookApi: BookApi) : RxPresenter<BookReadContract.View>(), BookReadContract.Presenter<BookReadContract.View> {

    override fun getBookMixAToc(bookId: String, viewChapters: String) {
        val key = StringUtils.creatAcacheKey("book-toc", bookId, viewChapters)
        val fromNetWork = bookApi.getBookMixAToc(bookId, viewChapters)
                .map { data -> data.mixToc }
                .compose(RxUtil.rxCacheListHelper(key))

        //依次检查disk、network
        val rxSubscription = Observable
                .concat(RxUtil.rxCreateDiskObservable(key, BookMixAToc.mixToc::class.java), fromNetWork)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BookMixAToc.mixToc> {
                    override fun onNext(data: BookMixAToc.mixToc) {
                        val list = data.chapters
                        if (list != null && !list.isEmpty() && mView != null) {
                            mView!!.showBookToc(list)
                        }
                    }

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {
                        LogUtils.e("onError: $e")
                        mView!!.netError(0)
                    }
                })
        addSubscrebe(rxSubscription)
    }

    override fun getChapterRead(url: String, chapter: Int) {
        val rxSubscription = bookApi.getChapterRead(url).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<ChapterRead> {
                    override fun onNext(data: ChapterRead) {
                        if (data.chapter != null && mView != null) {
                            mView!!.showChapterRead(data.chapter, chapter)
                        } else {
                            mView!!.netError(chapter)
                        }
                    }

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {
                        LogUtils.e("onError: $e")
                        mView!!.netError(chapter)
                    }
                })
        addSubscrebe(rxSubscription)
    }

    override fun merginAllBook(recommendBooks: Recommend.RecommendBooks, list: List<BookMixAToc.mixToc.Chapters>) {

        val fileName = recommendBooks.title
        //        File file = FileUtils.getMerginBook(fileName + ".txt");
        //        Subscription rxSubscription = new ObserverSubscriber<Object>
        //                (){};


        Observable.create(Observable.OnSubscribe<String> { }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : Observer<String> {
                    override fun onCompleted() {

                    }

                    override fun onError(e: Throwable) {

                    }

                    override fun onNext(o: String) {

                    }
                })//);


        //        DownloadProgress progress

    }
}