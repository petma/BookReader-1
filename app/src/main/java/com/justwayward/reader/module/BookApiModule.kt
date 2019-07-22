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
package com.justwayward.reader.module

import com.justwayward.reader.api.BookApi
import com.justwayward.reader.api.support.HeaderInterceptor
import com.justwayward.reader.api.support.Logger
import com.justwayward.reader.api.support.LoggingInterceptor

import java.util.concurrent.TimeUnit

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient

@Module
class BookApiModule {

    @Provides
    fun provideOkHttpClient(): OkHttpClient {

        val logging = LoggingInterceptor(Logger())
        logging.level = LoggingInterceptor.Level.BODY

        val builder = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
                .connectTimeout((20 * 1000).toLong(), TimeUnit.MILLISECONDS)
                .readTimeout((20 * 1000).toLong(), TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true) // 失败重发
                .addInterceptor(HeaderInterceptor())
                .addInterceptor(logging)
        return builder.build()
    }

    @Provides
    protected fun provideBookService(okHttpClient: OkHttpClient): BookApi {
        return BookApi.getInstance(okHttpClient)
    }
}