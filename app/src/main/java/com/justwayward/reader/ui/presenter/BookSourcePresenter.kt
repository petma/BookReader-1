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
import com.justwayward.reader.bean.BookSource
import com.justwayward.reader.ui.contract.BookSourceContract
import com.justwayward.reader.utils.LogUtils

import javax.inject.Inject

import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author yuyh.
 * @date 2016/9/8.
 */
class BookSourcePresenter @Inject
constructor(private val bookApi: BookApi) : RxPresenter<BookSourceContract.View>(), BookSourceContract.Presenter {

    override fun getBookSource(viewSummary: String, book: String) {
        val rxSubscription = bookApi.getBookSource(viewSummary, book).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<List<BookSource>> {
                    override fun onNext(data: List<BookSource>?) {
                        if (data != null && mView != null) {
                            mView!!.showBookSource(data)
                        }
                    }

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {
                        LogUtils.e("onError: $e")
                    }
                })
        addSubscrebe(rxSubscription)
    }
}
