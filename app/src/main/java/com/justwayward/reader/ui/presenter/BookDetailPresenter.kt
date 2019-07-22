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

import android.util.Log

import com.justwayward.reader.api.BookApi
import com.justwayward.reader.base.RxPresenter
import com.justwayward.reader.bean.BookDetail
import com.justwayward.reader.bean.HotReview
import com.justwayward.reader.bean.RecommendBookList
import com.justwayward.reader.ui.contract.BookDetailContract
import com.justwayward.reader.utils.LogUtils

import javax.inject.Inject

import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author lfh.
 * @date 2016/8/6.
 */
class BookDetailPresenter @Inject
constructor(private val bookApi: BookApi) : RxPresenter<BookDetailContract.View>(), BookDetailContract.Presenter<BookDetailContract.View> {

    override fun getBookDetail(bookId: String) {
        val rxSubscription = bookApi.getBookDetail(bookId).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BookDetail> {
                    override fun onNext(data: BookDetail?) {
                        if (data != null && mView != null) {
                            mView!!.showBookDetail(data)
                        }
                    }

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {
                        Log.e(TAG, "onError: $e")
                    }
                })
        addSubscrebe(rxSubscription)
    }

    override fun getHotReview(book: String) {
        val rxSubscription = bookApi.getHotReview(book).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<HotReview> {
                    override fun onNext(data: HotReview) {
                        val list = data.reviews
                        if (list != null && !list.isEmpty() && mView != null) {
                            mView!!.showHotReview(list)
                        }
                    }

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {}
                })
        addSubscrebe(rxSubscription)
    }

    override fun getRecommendBookList(bookId: String, limit: String) {
        val rxSubscription = bookApi.getRecommendBookList(bookId, limit).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<RecommendBookList> {
                    override fun onNext(data: RecommendBookList) {
                        LogUtils.i(data.booklists)
                        val list = data.booklists
                        if (list != null && !list.isEmpty() && mView != null) {
                            mView!!.showRecommendBookList(list)
                        }
                    }

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {
                        LogUtils.e("+++$e")
                    }
                })
        addSubscrebe(rxSubscription)
    }

    companion object {

        private val TAG = "BookDetailPresenter"
    }

}
