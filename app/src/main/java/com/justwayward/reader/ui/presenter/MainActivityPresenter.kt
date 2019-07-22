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
import com.justwayward.reader.bean.BookMixAToc
import com.justwayward.reader.bean.Recommend
import com.justwayward.reader.bean.user.Login
import com.justwayward.reader.manager.CollectionsManager
import com.justwayward.reader.ui.contract.MainContract
import com.justwayward.reader.utils.LogUtils
import com.justwayward.reader.utils.ToastUtils

import java.util.ArrayList

import javax.inject.Inject

import rx.Observable
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Func1
import rx.schedulers.Schedulers

/**
 * @author yuyh.
 * @date 2016/8/3.
 */
class MainActivityPresenter @Inject
constructor(private val bookApi: BookApi) : RxPresenter<MainContract.View>(), MainContract.Presenter<MainContract.View> {

    override fun login(uid: String, token: String, platform: String) {
        val rxSubscription = bookApi.login(uid, token, platform).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Login> {
                    override fun onNext(data: Login?) {
                        if (data != null && mView != null && data.ok) {
                            mView!!.loginSuccess()
                            LogUtils.e(data.user!!.toString())
                        }
                    }

                    override fun onCompleted() {}

                    override fun onError(e: Throwable) {
                        LogUtils.e("login$e")
                    }
                })
        addSubscrebe(rxSubscription)
    }

    override fun syncBookShelf() {
        val list = CollectionsManager.instance!!.collectionList
        val observables = ArrayList<Observable<BookMixAToc.mixToc>>()
        if (list != null && !list!!.isEmpty()) {
            for (bean in list!!) {
                if (!bean.isFromSD) {
                    val fromNetWork = bookApi.getBookMixAToc(bean._id, "chapters")
                            .map { data -> data.mixToc }//                    .compose(RxUtil.<BookMixAToc.mixToc>rxCacheListHelper(
                    //                            StringUtils.creatAcacheKey("book-toc", bean._id, "chapters")))
                    observables.add(fromNetWork)
                }
            }
        } else {
            ToastUtils.showSingleToast("书架空空如也...")
            mView!!.syncBookShelfCompleted()
            return
        }
        isLastSyncUpdateed = false
        val rxSubscription = Observable.mergeDelayError<mixToc>(observables)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<BookMixAToc.mixToc> {
                    override fun onNext(data: BookMixAToc.mixToc) {
                        val lastChapter = data.chapters!![data.chapters!!.size - 1].title
                        CollectionsManager.instance!!.setLastChapterAndLatelyUpdate(data.book, lastChapter, data.chaptersUpdated)
                    }

                    override fun onCompleted() {
                        mView!!.syncBookShelfCompleted()
                        if (isLastSyncUpdateed) {
                            ToastUtils.showSingleToast("小説已更新")
                        } else {
                            ToastUtils.showSingleToast("你追的小説沒有更新")
                        }

                    }

                    override fun onError(e: Throwable) {
                        LogUtils.e("onError: $e")
                        mView!!.showError()
                    }
                })
        addSubscrebe(rxSubscription)
    }

    companion object {
        var isLastSyncUpdateed = false
    }
}
