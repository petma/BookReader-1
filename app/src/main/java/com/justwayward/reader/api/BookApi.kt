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
package com.justwayward.reader.api

import com.justwayward.reader.base.Constant
import com.justwayward.reader.bean.AutoComplete
import com.justwayward.reader.bean.BookDetail
import com.justwayward.reader.bean.BookHelp
import com.justwayward.reader.bean.BookHelpList
import com.justwayward.reader.bean.BookListDetail
import com.justwayward.reader.bean.BookListTags
import com.justwayward.reader.bean.BookLists
import com.justwayward.reader.bean.BookMixAToc
import com.justwayward.reader.bean.BookReview
import com.justwayward.reader.bean.BookReviewList
import com.justwayward.reader.bean.BookSource
import com.justwayward.reader.bean.BooksByCats
import com.justwayward.reader.bean.BooksByTag
import com.justwayward.reader.bean.CategoryList
import com.justwayward.reader.bean.CategoryListLv2
import com.justwayward.reader.bean.ChapterRead
import com.justwayward.reader.bean.CommentList
import com.justwayward.reader.bean.DiscussionList
import com.justwayward.reader.bean.Disscussion
import com.justwayward.reader.bean.HotReview
import com.justwayward.reader.bean.HotWord
import com.justwayward.reader.bean.RankingList
import com.justwayward.reader.bean.Rankings
import com.justwayward.reader.bean.Recommend
import com.justwayward.reader.bean.RecommendBookList
import com.justwayward.reader.bean.SearchDetail
import com.justwayward.reader.bean.user.Login
import com.justwayward.reader.bean.user.LoginReq

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Query
import rx.Observable

/**
 * https://github.com/JustWayward/BookReader
 *
 * @author yuyh.
 * @date 2016/8/3.
 */
class BookApi(okHttpClient: OkHttpClient) {

    private val service: BookApiService

    val hotWord: Observable<HotWord>
        get() = service.hotWord

    val ranking: Observable<RankingList>
        get() = service.ranking

    val bookListTags: Observable<BookListTags>
        get() = service.bookListTags

    val categoryList: Observable<CategoryList>
        @Synchronized get() = service.categoryList

    val categoryListLv2: Observable<CategoryListLv2>
        get() = service.categoryListLv2

    init {
        val retrofit = Retrofit.Builder()
                .baseUrl(Constant.API_BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()) // 添加Rx适配器
                .addConverterFactory(GsonConverterFactory.create()) // 添加Gson转换器
                .client(okHttpClient)
                .build()
        service = retrofit.create(BookApiService::class.java)
    }

    fun getRecommend(gender: String): Observable<Recommend> {
        return service.getRecomend(gender)
    }

    fun getAutoComplete(query: String): Observable<AutoComplete> {
        return service.autoComplete(query)
    }

    fun getSearchResult(query: String): Observable<SearchDetail> {
        return service.searchBooks(query)
    }

    fun searchBooksByAuthor(author: String): Observable<BooksByTag> {
        return service.searchBooksByAuthor(author)
    }

    fun getBookDetail(bookId: String): Observable<BookDetail> {
        return service.getBookDetail(bookId)
    }

    fun getHotReview(book: String): Observable<HotReview> {
        return service.getHotReview(book)
    }

    fun getRecommendBookList(bookId: String, limit: String): Observable<RecommendBookList> {
        return service.getRecommendBookList(bookId, limit)
    }

    fun getBooksByTag(tags: String, start: String, limit: String): Observable<BooksByTag> {
        return service.getBooksByTag(tags, start, limit)
    }

    fun getBookMixAToc(bookId: String, view: String): Observable<BookMixAToc> {
        return service.getBookMixAToc(bookId, view)
    }

    @Synchronized
    fun getChapterRead(url: String): Observable<ChapterRead> {
        return service.getChapterRead(url)
    }

    @Synchronized
    fun getBookSource(view: String, book: String): Observable<List<BookSource>> {
        return service.getABookSource(view, book)
    }

    fun getRanking(rankingId: String): Observable<Rankings> {
        return service.getRanking(rankingId)
    }

    fun getBookLists(duration: String, sort: String, start: String, limit: String, tag: String, gender: String): Observable<BookLists> {
        return service.getBookLists(duration, sort, start, limit, tag, gender)
    }

    fun getBookListDetail(bookListId: String): Observable<BookListDetail> {
        return service.getBookListDetail(bookListId)
    }

    fun getBooksByCats(gender: String, type: String, major: String, minor: String, start: Int, @Query("limit") limit: Int): Observable<BooksByCats> {
        return service.getBooksByCats(gender, type, major, minor, start, limit)
    }

    fun getBookDisscussionList(block: String, duration: String, sort: String, type: String, start: String, limit: String, distillate: String): Observable<DiscussionList> {
        return service.getBookDisscussionList(block, duration, sort, type, start, limit, distillate)
    }

    fun getBookDisscussionDetail(disscussionId: String): Observable<Disscussion> {
        return service.getBookDisscussionDetail(disscussionId)
    }

    fun getBestComments(disscussionId: String): Observable<CommentList> {
        return service.getBestComments(disscussionId)
    }

    fun getBookDisscussionComments(disscussionId: String, start: String, limit: String): Observable<CommentList> {
        return service.getBookDisscussionComments(disscussionId, start, limit)
    }

    fun getBookReviewList(duration: String, sort: String, type: String, start: String, limit: String, distillate: String): Observable<BookReviewList> {
        return service.getBookReviewList(duration, sort, type, start, limit, distillate)
    }

    fun getBookReviewDetail(bookReviewId: String): Observable<BookReview> {
        return service.getBookReviewDetail(bookReviewId)
    }

    fun getBookReviewComments(bookReviewId: String, start: String, limit: String): Observable<CommentList> {
        return service.getBookReviewComments(bookReviewId, start, limit)
    }

    fun getBookHelpList(duration: String, sort: String, start: String, limit: String, distillate: String): Observable<BookHelpList> {
        return service.getBookHelpList(duration, sort, start, limit, distillate)
    }

    fun getBookHelpDetail(helpId: String): Observable<BookHelp> {
        return service.getBookHelpDetail(helpId)
    }

    fun login(platform_uid: String, platform_token: String, platform_code: String): Observable<Login> {
        val loginReq = LoginReq()
        loginReq.platform_code = platform_code
        loginReq.platform_token = platform_token
        loginReq.platform_uid = platform_uid
        return service.login(loginReq)
    }

    fun getBookDetailDisscussionList(book: String, sort: String, type: String, start: String, limit: String): Observable<DiscussionList> {
        return service.getBookDetailDisscussionList(book, sort, type, start, limit)
    }

    fun getBookDetailReviewList(book: String, sort: String, start: String, limit: String): Observable<HotReview> {
        return service.getBookDetailReviewList(book, sort, start, limit)
    }

    fun getGirlBookDisscussionList(block: String, duration: String, sort: String, type: String, start: String, limit: String, distillate: String): Observable<DiscussionList> {
        return service.getBookDisscussionList(block, duration, sort, type, start, limit, distillate)
    }

    companion object {

        var instance: BookApi? = null

        fun getInstance(okHttpClient: OkHttpClient): BookApi {
            if (instance == null)
                instance = BookApi(okHttpClient)
            return instance
        }
    }

}
