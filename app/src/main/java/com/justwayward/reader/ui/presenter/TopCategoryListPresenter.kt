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
import com.justwayward.reader.bean.CategoryList
import com.justwayward.reader.ui.contract.TopCategoryListContract
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
 * @date 2016/8/30.
 */
class TopCategoryListPresenter @Inject
constructor(private val bookApi: BookApi) : RxPresenter<TopCategoryListContract.View>(), TopCategoryListContract.Presenter<TopCategoryListContract.View> {

    override fun getCategoryList() {
        val key = StringUtils.creatAcacheKey("book-category-list")
        val fromNetWork = bookApi.categoryList
                .compose(RxUtil.rxCacheBeanHelper(key))

        //依次检查disk、network
        val rxSubscription = Observable.concat(RxUtil.rxCreateDiskObservable(key, CategoryList::class.java), fromNetWork)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<CategoryList> {
                    override fun onNext(data: CategoryList?) {
                        if (data != null && mView != null) {
                            mView!!.showCategoryList(data)
                        }
                    }

                    override fun onCompleted() {
                        LogUtils.i("complete")
                        mView!!.complete()
                    }

                    override fun onError(e: Throwable) {
                        LogUtils.e(e.toString())
                        mView!!.complete()
                    }
                })
        addSubscrebe(rxSubscription)
    }
}
