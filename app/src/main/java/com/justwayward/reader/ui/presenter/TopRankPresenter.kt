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
import com.justwayward.reader.bean.RankingList
import com.justwayward.reader.ui.contract.TopRankContract
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
 * @date 16/9/1.
 */
class TopRankPresenter @Inject
constructor(private val bookApi: BookApi) : RxPresenter<TopRankContract.View>(), TopRankContract.Presenter<TopRankContract.View> {

    override fun getRankList() {
        val key = StringUtils.creatAcacheKey("book-ranking-list")
        val fromNetWork = bookApi.ranking
                .compose(RxUtil.rxCacheBeanHelper(key))

        //依次检查disk、network
        val rxSubscription = Observable.concat(RxUtil.rxCreateDiskObservable(key, RankingList::class.java), fromNetWork)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<RankingList> {
                    override fun onNext(data: RankingList?) {
                        if (data != null && mView != null) {
                            mView!!.showRankList(data)
                        }
                    }

                    override fun onCompleted() {
                        mView!!.complete()
                    }

                    override fun onError(e: Throwable) {
                        LogUtils.e("getRankList:$e")
                        mView!!.complete()
                    }
                })
        addSubscrebe(rxSubscription)
    }

}
