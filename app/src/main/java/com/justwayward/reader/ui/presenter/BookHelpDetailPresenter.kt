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
import com.justwayward.reader.bean.BookHelp
import com.justwayward.reader.bean.CommentList
import com.justwayward.reader.ui.contract.BookHelpDetailContract
import com.justwayward.reader.utils.LogUtils

import javax.inject.Inject

import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author lfh.
 * @date 16/9/3.
 */
class BookHelpDetailPresenter @Inject
constructor(private val bookApi: BookApi) : RxPresenter<BookHelpDetailContract.View>(), BookHelpDetailContract.Presenter {

    override fun getBookHelpDetail(id: String) {
        val rxSubscription = bookApi.getBookHelpDetail(id).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BookHelp> {
                    override fun onCompleted() {

                    }

                    override fun onError(e: Throwable) {
                        LogUtils.e("getBookHelpDetail:$e")
                    }

                    override fun onNext(bookHelp: BookHelp) {
                        mView!!.showBookHelpDetail(bookHelp)
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

    override fun getBookHelpComments(disscussionId: String, start: Int, limit: Int) {
        val rxSubscription = bookApi.getBookReviewComments(disscussionId, start.toString() + "", limit.toString() + "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<CommentList> {
                    override fun onCompleted() {

                    }

                    override fun onError(e: Throwable) {
                        LogUtils.e("getBookHelpComments:$e")
                    }

                    override fun onNext(list: CommentList) {
                        mView!!.showBookHelpComments(list)
                    }
                })
        addSubscrebe(rxSubscription)
    }

}
