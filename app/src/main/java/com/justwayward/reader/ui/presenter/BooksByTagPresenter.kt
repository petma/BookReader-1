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
import com.justwayward.reader.bean.BooksByTag
import com.justwayward.reader.ui.contract.BooksByTagContract
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
 * @date 2016/8/7.
 */
class BooksByTagPresenter @Inject
constructor(private val bookApi: BookApi) : RxPresenter<BooksByTagContract.View>(), BooksByTagContract.Presenter<BooksByTagContract.View> {

    private var isLoading = false

    override fun getBooksByTag(tags: String, start: String, limit: String) {
        if (!isLoading) {
            isLoading = true
            val key = StringUtils.creatAcacheKey("books-by-tag", tags, start, limit)
            val fromNetWork = bookApi.getBooksByTag(tags, start, limit)
                    .compose(RxUtil.rxCacheListHelper(key))

            //依次检查disk、network
            val rxSubscription = Observable.concat(RxUtil.rxCreateDiskObservable(key, BooksByTag::class.java), fromNetWork)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<BooksByTag> {
                        override fun onNext(data: BooksByTag?) {
                            if (data != null) {
                                val list = data.books
                                if (list != null && !list.isEmpty() && mView != null) {
                                    val isRefresh = if (start == "0") true else false
                                    mView!!.showBooksByTag(list, isRefresh)
                                }
                            }
                        }

                        override fun onCompleted() {
                            isLoading = false
                            mView!!.onLoadComplete(true, "")
                        }

                        override fun onError(e: Throwable) {
                            LogUtils.e(e.toString())
                            isLoading = false
                            mView!!.onLoadComplete(false, e.toString())
                        }
                    })
            addSubscrebe(rxSubscription)
        }
    }
}
