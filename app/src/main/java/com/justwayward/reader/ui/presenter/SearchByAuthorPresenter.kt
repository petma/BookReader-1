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
import com.justwayward.reader.ui.contract.SearchByAuthorContract
import com.justwayward.reader.utils.LogUtils
import com.justwayward.reader.utils.RxUtil
import com.justwayward.reader.utils.StringUtils

import javax.inject.Inject

import rx.Observable
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers

/**
 * @author yuyh.
 * @date 2016/9/8.
 */
class SearchByAuthorPresenter @Inject
constructor(private val bookApi: BookApi) : RxPresenter<SearchByAuthorContract.View>(), SearchByAuthorContract.Presenter {

    override fun getSearchResultList(author: String) {
        val key = StringUtils.creatAcacheKey("search-by-author", author)
        val fromNetWork = bookApi.searchBooksByAuthor(author)
                .compose(RxUtil.rxCacheListHelper(key))

        //依次检查disk、network
        val rxSubscription = Observable.concat(RxUtil.rxCreateDiskObservable(key, BooksByTag::class.java), fromNetWork)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BooksByTag> {
                    override fun onNext(booksByTag: BooksByTag) {
                        if (mView != null)
                            mView!!.showSearchResultList(booksByTag.books)
                    }

                    override fun onCompleted() {
                        LogUtils.i("complete")
                        mView!!.complete()
                    }

                    override fun onError(e: Throwable) {
                        LogUtils.e("getSearchResultList:$e")
                        if (mView != null)
                            mView!!.showError()
                    }
                })
        addSubscrebe(rxSubscription)
    }

}
