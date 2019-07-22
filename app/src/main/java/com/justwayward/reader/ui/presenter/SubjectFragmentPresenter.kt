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
import com.justwayward.reader.bean.BookLists
import com.justwayward.reader.ui.contract.SubjectFragmentContract
import com.justwayward.reader.utils.LogUtils
import com.justwayward.reader.utils.RxUtil
import com.justwayward.reader.utils.StringUtils
import com.justwayward.reader.utils.ToastUtils

import javax.inject.Inject

import rx.Observable
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers

/**
 * @author yuyh.
 * @date 2016/8/31.
 */
class SubjectFragmentPresenter @Inject
constructor(private val bookApi: BookApi) : RxPresenter<SubjectFragmentContract.View>(), SubjectFragmentContract.Presenter<SubjectFragmentContract.View> {

    override fun getBookLists(duration: String, sort: String, start: Int, limit: Int, tag: String, gender: String) {
        val key = StringUtils.creatAcacheKey("book-lists", duration, sort, start.toString() + "", limit.toString() + "", tag, gender)
        val fromNetWork = bookApi.getBookLists(duration, sort, start.toString() + "", limit.toString() + "", tag, gender)
                .compose(RxUtil.rxCacheListHelper(key))

        //依次检查disk、network
        val rxSubscription = Observable.concat(RxUtil.rxCreateDiskObservable(key, BookLists::class.java), fromNetWork)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BookLists> {
                    override fun onCompleted() {
                        mView!!.complete()
                    }

                    override fun onError(e: Throwable) {
                        LogUtils.e("getBookLists:$e")
                        mView!!.showError()
                    }

                    override fun onNext(tags: BookLists) {
                        mView!!.showBookList(tags.bookLists, if (start == 0) true else false)
                        if (tags.bookLists == null || tags.bookLists!!.size <= 0) {
                            ToastUtils.showSingleToast("暂无相关书单")
                        }
                    }
                })
        addSubscrebe(rxSubscription)
    }
}
