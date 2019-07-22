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
import com.justwayward.reader.bean.BooksByCats
import com.justwayward.reader.bean.Rankings
import com.justwayward.reader.ui.contract.SubRankContract
import com.justwayward.reader.utils.LogUtils

import java.util.ArrayList

import javax.inject.Inject

import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author yuyh.
 * @date 2016/8/31.
 */
class SubRankPresenter @Inject
constructor(private val bookApi: BookApi) : RxPresenter<SubRankContract.View>(), SubRankContract.Presenter<SubRankContract.View> {

    override fun getRankList(id: String) {
        val rxSubscription = bookApi.getRanking(id).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Rankings> {
                    override fun onCompleted() {
                        mView!!.complete()
                    }

                    override fun onError(e: Throwable) {
                        LogUtils.e("getRankList:$e")
                        mView!!.showError()
                    }

                    override fun onNext(ranking: Rankings) {
                        val books = ranking.ranking!!.books

                        val cats = BooksByCats()
                        cats.books = ArrayList()
                        for (bean in books!!) {
                            cats.books!!.add(BooksByCats.BooksBean(bean._id, bean.cover, bean.title, bean.author, bean.cat, bean.shortIntro, bean.latelyFollower, bean.retentionRatio))
                        }
                        mView!!.showRankList(cats)
                    }
                })
        addSubscrebe(rxSubscription)
    }

}
