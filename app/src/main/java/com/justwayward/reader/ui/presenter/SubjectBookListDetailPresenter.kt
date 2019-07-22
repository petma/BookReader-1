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
import com.justwayward.reader.bean.BookListDetail
import com.justwayward.reader.ui.contract.SubjectBookListDetailContract
import com.justwayward.reader.utils.LogUtils

import javax.inject.Inject

import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author lfh.
 * @date 2016/8/31.
 */
class SubjectBookListDetailPresenter @Inject
constructor(private val bookApi: BookApi) : RxPresenter<SubjectBookListDetailContract.View>(), SubjectBookListDetailContract.Presenter<SubjectBookListDetailContract.View> {

    override fun getBookListDetail(bookListId: String) {
        val rxSubscription = bookApi.getBookListDetail(bookListId).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BookListDetail> {
                    override fun onCompleted() {
                        mView!!.complete()
                    }

                    override fun onError(e: Throwable) {
                        LogUtils.e("getBookListDetail:$e")
                        mView!!.complete()
                    }

                    override fun onNext(data: BookListDetail) {
                        mView!!.showBookListDetail(data)
                    }
                })
        addSubscrebe(rxSubscription)
    }

}
