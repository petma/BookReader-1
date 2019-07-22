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

import com.justwayward.reader.api.BookApi
import com.justwayward.reader.base.RxPresenter
import com.justwayward.reader.bean.BookReview
import com.justwayward.reader.bean.CommentList
import com.justwayward.reader.ui.contract.BookReviewDetailContract
import com.justwayward.reader.utils.LogUtils

import javax.inject.Inject

import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author yuyh.
 * @date 16/9/2.
 */
class BookReviewDetailPresenter @Inject
constructor(private val bookApi: BookApi) : RxPresenter<BookReviewDetailContract.View>(), BookReviewDetailContract.Presenter {

    override fun getBookReviewDetail(id: String) {
        val rxSubscription = bookApi.getBookReviewDetail(id).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BookReview> {
                    override fun onCompleted() {

                    }

                    override fun onError(e: Throwable) {
                        LogUtils.e("getBookReviewDetail:$e")
                    }

                    override fun onNext(data: BookReview) {
                        mView!!.showBookReviewDetail(data)
                    }
                })
        addSubscrebe(rxSubscription)
    }

    override fun getBestComments(bookReviewId: String) {
        val rxSubscription = bookApi.getBestComments(bookReviewId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<CommentList> {
                    override fun onCompleted() {

                    }

                    override fun onError(e: Throwable) {
                        LogUtils.e("getBestComments:$e")
                    }

                    override fun onNext(list: CommentList) {
                        mView!!.showBestComments(list)
                    }
                })
        addSubscrebe(rxSubscription)
    }

    override fun getBookReviewComments(bookReviewId: String, start: Int, limit: Int) {
        val rxSubscription = bookApi.getBookReviewComments(bookReviewId, start.toString() + "", limit.toString() + "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<CommentList> {
                    override fun onCompleted() {

                    }

                    override fun onError(e: Throwable) {
                        LogUtils.e("getBookReviewComments:$e")
                    }

                    override fun onNext(list: CommentList) {
                        mView!!.showBookReviewComments(list)
                    }
                })
        addSubscrebe(rxSubscription)
    }

}
