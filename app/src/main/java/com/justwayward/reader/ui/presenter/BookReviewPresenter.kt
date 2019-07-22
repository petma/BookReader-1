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
import com.justwayward.reader.bean.BookReviewList
import com.justwayward.reader.ui.contract.BookReviewContract
import com.justwayward.reader.utils.LogUtils
import com.justwayward.reader.utils.RxUtil
import com.justwayward.reader.utils.StringUtils

import javax.inject.Inject

import rx.Observable
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers

/**
 * @author lfh.
 * @date 16/9/3.
 */
class BookReviewPresenter @Inject
constructor(private val bookApi: BookApi) : RxPresenter<BookReviewContract.View>(), BookReviewContract.Presenter {

    override fun getBookReviewList(sort: String, type: String, distillate: String, start: Int, limit: Int) {
        val key = StringUtils.creatAcacheKey("book-review-list", sort, type, distillate, start, limit)
        val fromNetWork = bookApi.getBookReviewList("all", sort, type, start.toString() + "", limit.toString() + "", distillate)
                .compose(RxUtil.rxCacheListHelper(key))

        //依次检查disk、network
        val rxSubscription = Observable.concat(RxUtil.rxCreateDiskObservable(key, BookReviewList::class.java), fromNetWork)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BookReviewList> {
                    override fun onCompleted() {
                        mView!!.complete()
                    }

                    override fun onError(e: Throwable) {
                        LogUtils.e("onError: $e")
                        mView!!.showError()
                    }

                    override fun onNext(list: BookReviewList) {
                        LogUtils.d("onNext: get data finish")
                        val isRefresh = if (start == 0) true else false
                        mView!!.showBookReviewList(list.reviews, isRefresh)
                    }
                })
        addSubscrebe(rxSubscription)
    }

}
