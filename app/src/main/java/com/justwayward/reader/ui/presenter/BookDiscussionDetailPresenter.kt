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
import com.justwayward.reader.bean.CommentList
import com.justwayward.reader.bean.Disscussion
import com.justwayward.reader.ui.contract.BookDiscussionDetailContract
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
class BookDiscussionDetailPresenter @Inject
constructor(private val bookApi: BookApi) : RxPresenter<BookDiscussionDetailContract.View>(), BookDiscussionDetailContract.Presenter {

    override fun getBookDisscussionDetail(id: String) {
        val rxSubscription = bookApi.getBookDisscussionDetail(id).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Disscussion> {
                    override fun onCompleted() {

                    }

                    override fun onError(e: Throwable) {
                        LogUtils.e("getBookDisscussionDetail:$e")
                    }

                    override fun onNext(disscussion: Disscussion) {
                        mView!!.showBookDisscussionDetail(disscussion)
                    }
                })
        addSubscrebe(rxSubscription)
    }

    override fun getBestComments(disscussionId: String) {
        val rxSubscription = bookApi.getBestComments(disscussionId)
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

    override fun getBookDisscussionComments(disscussionId: String, start: Int, limit: Int) {
        val rxSubscription = bookApi.getBookDisscussionComments(disscussionId, start.toString() + "", limit.toString() + "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<CommentList> {
                    override fun onCompleted() {

                    }

                    override fun onError(e: Throwable) {
                        LogUtils.e("getBookDisscussionComments:$e")
                    }

                    override fun onNext(list: CommentList) {
                        mView!!.showBookDisscussionComments(list)
                    }
                })
        addSubscrebe(rxSubscription)
    }

}
