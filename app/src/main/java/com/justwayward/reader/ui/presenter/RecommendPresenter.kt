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

import android.content.Context

import com.justwayward.reader.api.BookApi
import com.justwayward.reader.base.RxPresenter
import com.justwayward.reader.bean.BookMixAToc
import com.justwayward.reader.bean.Recommend
import com.justwayward.reader.manager.SettingManager
import com.justwayward.reader.ui.contract.RecommendContract
import com.justwayward.reader.utils.ACache
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
 * @author yuyh.
 * @date 2016/8/3.
 */
class RecommendPresenter @Inject
constructor(private val mContext: Context, private val bookApi: BookApi) : RxPresenter<RecommendContract.View>(), RecommendContract.Presenter<RecommendContract.View> {

    override fun getRecommendList() {
        val key = StringUtils.creatAcacheKey("recommend-list", SettingManager.instance!!.userChooseSex)
        val fromNetWork = bookApi.getRecommend(SettingManager.instance!!.userChooseSex)
                .compose(RxUtil.rxCacheListHelper(key))

        //依次检查disk、network
        val rxSubscription = Observable.concat(RxUtil.rxCreateDiskObservable(key, Recommend::class.java), fromNetWork)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Recommend> {
                    override fun onNext(recommend: Recommend?) {
                        if (recommend != null) {
                            val list = recommend.books
                            if (list != null && !list.isEmpty() && mView != null) {
                                mView!!.showRecommendList(list)
                            }
                        }
                    }

                    override fun onCompleted() {
                        mView!!.complete()
                    }

                    override fun onError(e: Throwable) {
                        LogUtils.e("getRecommendList", e.toString())
                        mView!!.showError()
                    }
                })
        addSubscrebe(rxSubscription)
    }

    fun getTocList(bookId: String) {
        bookApi.getBookMixAToc(bookId, "chapters").subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BookMixAToc> {
                    override fun onNext(data: BookMixAToc) {
                        ACache.get(mContext).put(bookId + "bookToc", data)
                        val list = data.mixToc!!.chapters
                        if (list != null && !list.isEmpty() && mView != null) {
                            mView!!.showBookToc(bookId, list)
                        }
                    }

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {
                        LogUtils.e("onError: $e")
                        mView!!.showError()
                    }
                })
    }
}
