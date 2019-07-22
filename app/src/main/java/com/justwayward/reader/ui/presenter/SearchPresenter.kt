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
import com.justwayward.reader.bean.AutoComplete
import com.justwayward.reader.bean.HotWord
import com.justwayward.reader.bean.SearchDetail
import com.justwayward.reader.ui.contract.SearchContract
import com.justwayward.reader.utils.LogUtils
import com.justwayward.reader.utils.RxUtil
import com.justwayward.reader.utils.StringUtils

import javax.inject.Inject

import rx.Observable
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author lfh.
 * @date 2016/8/6.
 */
class SearchPresenter @Inject
constructor(private val bookApi: BookApi) : RxPresenter<SearchContract.View>(), SearchContract.Presenter<SearchContract.View> {

    override fun getHotWordList() {
        val key = StringUtils.creatAcacheKey("hot-word-list")
        val fromNetWork = bookApi.hotWord
                .compose(RxUtil.rxCacheListHelper(key))

        //依次检查disk、network
        val rxSubscription = Observable.concat(RxUtil.rxCreateDiskObservable(key, HotWord::class.java), fromNetWork)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<HotWord> {
                    override fun onNext(hotWord: HotWord) {
                        val list = hotWord.hotWords
                        if (list != null && !list.isEmpty() && mView != null) {
                            mView!!.showHotWordList(list)
                        }
                    }

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {
                        LogUtils.e("onError: $e")
                    }
                })
        addSubscrebe(rxSubscription)
    }

    override fun getAutoCompleteList(query: String) {
        val rxSubscription = bookApi.getAutoComplete(query).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<AutoComplete> {
                    override fun onNext(autoComplete: AutoComplete) {
                        LogUtils.e("getAutoCompleteList" + autoComplete.keywords!!)
                        val list = autoComplete.keywords
                        if (list != null && !list.isEmpty() && mView != null) {
                            mView!!.showAutoCompleteList(list)
                        }
                    }

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {
                        LogUtils.e(e.toString())
                    }
                })
        addSubscrebe(rxSubscription)
    }

    override fun getSearchResultList(query: String) {
        val rxSubscription = bookApi.getSearchResult(query).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<SearchDetail> {
                    override fun onNext(bean: SearchDetail) {
                        val list = bean.books
                        if (list != null && !list.isEmpty() && mView != null) {
                            mView!!.showSearchResultList(list)
                        }
                    }

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {
                        LogUtils.e(e.toString())
                    }
                })
        addSubscrebe(rxSubscription)
    }
}
