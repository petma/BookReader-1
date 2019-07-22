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
package com.justwayward.reader.utils

import android.text.TextUtils

import com.google.gson.Gson
import com.justwayward.reader.ReaderApplication

import java.lang.reflect.Field

import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action0
import rx.functions.Action1
import rx.functions.Func1
import rx.schedulers.Schedulers

object RxUtil {

    /**
     * 统一线程处理
     *
     * @param <T>
     * @return
    </T> */
    fun <T> rxSchedulerHelper(): Observable.Transformer<T, T> {    //compose简化线程
        return Observable.Transformer { observable ->
            observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun <T> rxCreateDiskObservable(key: String, clazz: Class<T>): Observable<*> {
        return Observable.create(Observable.OnSubscribe<String> { subscriber ->
            LogUtils.d("get data from disk: key==$key")
            val json = ACache.get(ReaderApplication.getsInstance()).getAsString(key)
            LogUtils.d("get data from disk finish , json==" + json!!)
            if (!TextUtils.isEmpty(json)) {
                subscriber.onNext(json)
            }
            subscriber.onCompleted()
        })
                .map { s -> Gson().fromJson(s, clazz) }
                .subscribeOn(Schedulers.io())
    }

    fun <T> rxCacheListHelper(key: String): Observable.Transformer<T, T> {
        return Observable.Transformer { observable ->
            observable
                    .subscribeOn(Schedulers.io())//指定doOnNext执行线程是新线程
                    .doOnNext { data ->
                        Schedulers.io().createWorker().schedule(Action0 {
                            LogUtils.d("get data from network finish ,start cache...")
                            //通过反射获取List,再判空决定是否缓存
                            if (data == null)
                                return@Action0
                            val clazz = data.javaClass
                            val fields = clazz.fields
                            for (field in fields) {
                                val className = field.type.simpleName
                                // 得到属性值
                                if (className.equals("List", ignoreCase = true)) {
                                    try {
                                        val list = field.get(data) as List<*>
                                        LogUtils.d("list==$list")
                                        if (list != null && !list.isEmpty()) {
                                            ACache.get(ReaderApplication.getsInstance())
                                                    .put(key, Gson().toJson(data, clazz))
                                            LogUtils.d("cache finish")
                                        }
                                    } catch (e: IllegalAccessException) {
                                        e.printStackTrace()
                                    }

                                }
                            }
                        })
                    }
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun <T> rxCacheBeanHelper(key: String): Observable.Transformer<T, T> {
        return Observable.Transformer { observable ->
            observable
                    .subscribeOn(Schedulers.io())//指定doOnNext执行线程是新线程
                    .doOnNext { data ->
                        Schedulers.io().createWorker().schedule {
                            LogUtils.d("get data from network finish ,start cache...")
                            ACache.get(ReaderApplication.getsInstance())
                                    .put(key, Gson().toJson(data, data.javaClass))
                            LogUtils.d("cache finish")
                        }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }
}
