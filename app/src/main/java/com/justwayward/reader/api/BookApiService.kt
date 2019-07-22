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

import com.justwayward.reader.bean.AutoComplete
import com.justwayward.reader.bean.BookDetail
import com.justwayward.reader.bean.BookHelpList
import com.justwayward.reader.bean.BookListDetail
import com.justwayward.reader.bean.BookListTags
import com.justwayward.reader.bean.BookLists
import com.justwayward.reader.bean.BookMixAToc
import com.justwayward.reader.bean.BookRead
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
import com.justwayward.reader.bean.BookHelp
import com.justwayward.reader.bean.HotReview
import com.justwayward.reader.bean.HotWord
import com.justwayward.reader.bean.user.Following
import com.justwayward.reader.bean.user.Login
import com.justwayward.reader.bean.PostCount
import com.justwayward.reader.bean.RankingList
import com.justwayward.reader.bean.Rankings
import com.justwayward.reader.bean.Recommend
import com.justwayward.reader.bean.RecommendBookList
import com.justwayward.reader.bean.SearchDetail
import com.justwayward.reader.bean.user.LoginReq

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import rx.Observable

/**
 * https://github.com/JustWayward/BookReader
 *
 * @author yuyh.
 * @date 2016/8/3.
 */
interface BookApiService {

    @get:GET("/book/hot-word")
    val hotWord: Observable<HotWord>

    /**
     * 获取所有排行榜
     *
     * @return
     */
    @get:GET("/ranking/gender")
    val ranking: Observable<RankingList>

    /**
     * 获取主题书单标签列表
     *
     * @return
     */
    @get:GET("/book-list/tagType")
    val bookListTags: Observable<BookListTags>

    /**
     * 获取分类
     *
     * @return
     */
    @get:GET("/cats/lv2/statistics")
    val categoryList: Observable<CategoryList>

    /**
     * 获取二级分类
     *
     * @return
     */
    @get:GET("/cats/lv2")
    val categoryListLv2: Observable<CategoryListLv2>

    @GET("/book/recommend")
    fun getRecomend(@Query("gender") gender: String): Observable<Recommend>

    /**
     * 获取正版源(若有) 与 盗版源
     * @param view
     * @param book
     * @return
     */
    @GET("/atoc")
    fun getABookSource(@Query("view") view: String, @Query("book") book: String): Observable<List<BookSource>>

    /**
     * 只能获取正版源
     *
     * @param view
     * @param book
     * @return
     */
    @GET("/btoc")
    fun getBBookSource(@Query("view") view: String, @Query("book") book: String): Observable<List<BookSource>>

    @GET("/mix-atoc/{bookId}")
    fun getBookMixAToc(@Path("bookId") bookId: String, @Query("view") view: String): Observable<BookMixAToc>

    @GET("/mix-toc/{bookId}")
    fun getBookRead(@Path("bookId") bookId: String): Observable<BookRead>

    @GET("/btoc/{bookId}")
    fun getBookBToc(@Path("bookId") bookId: String, @Query("view") view: String): Observable<BookMixAToc>

    @GET("http://chapter2.zhuishushenqi.com/chapter/{url}")
    fun getChapterRead(@Path("url") url: String): Observable<ChapterRead>

    @GET("/post/post-count-by-book")
    fun postCountByBook(@Query("bookId") bookId: String): Observable<PostCount>

    @GET("/post/total-count")
    fun postTotalCount(@Query("books") bookId: String): Observable<PostCount>

    /**
     * 关键字自动补全
     *
     * @param query
     * @return
     */
    @GET("/book/auto-complete")
    fun autoComplete(@Query("query") query: String): Observable<AutoComplete>

    /**
     * 书籍查询
     *
     * @param query
     * @return
     */
    @GET("/book/fuzzy-search")
    fun searchBooks(@Query("query") query: String): Observable<SearchDetail>

    /**
     * 通过作者查询书名
     *
     * @param author
     * @return
     */
    @GET("/book/accurate-search")
    fun searchBooksByAuthor(@Query("author") author: String): Observable<BooksByTag>

    /**
     * 热门评论
     *
     * @param book
     * @return
     */
    @GET("/post/review/best-by-book")
    fun getHotReview(@Query("book") book: String): Observable<HotReview>

    @GET("/book-list/{bookId}/recommend")
    fun getRecommendBookList(@Path("bookId") bookId: String, @Query("limit") limit: String): Observable<RecommendBookList>

    @GET("/book/{bookId}")
    fun getBookDetail(@Path("bookId") bookId: String): Observable<BookDetail>

    @GET("/book/by-tags")
    fun getBooksByTag(@Query("tags") tags: String, @Query("start") start: String, @Query("limit") limit: String): Observable<BooksByTag>

    /**
     * 获取单一排行榜
     * 周榜：rankingId->_id
     * 月榜：rankingId->monthRank
     * 总榜：rankingId->totalRank
     *
     * @return
     */
    @GET("/ranking/{rankingId}")
    fun getRanking(@Path("rankingId") rankingId: String): Observable<Rankings>

    /**
     * 获取主题书单列表
     * 本周最热：duration=last-seven-days&sort=collectorCount
     * 最新发布：duration=all&sort=created
     * 最多收藏：duration=all&sort=collectorCount
     *
     * @param tag    都市、古代、架空、重生、玄幻、网游
     * @param gender male、female
     * @param limit  20
     * @return
     */
    @GET("/book-list")
    fun getBookLists(@Query("duration") duration: String, @Query("sort") sort: String, @Query("start") start: String, @Query("limit") limit: String, @Query("tag") tag: String, @Query("gender") gender: String): Observable<BookLists>

    /**
     * 获取书单详情
     *
     * @return
     */
    @GET("/book-list/{bookListId}")
    fun getBookListDetail(@Path("bookListId") bookListId: String): Observable<BookListDetail>

    /**
     * 按分类获取书籍列表
     *
     * @param gender male、female
     * @param type   hot(热门)、new(新书)、reputation(好评)、over(完结)
     * @param major  玄幻
     * @param minor  东方玄幻、异界大陆、异界争霸、远古神话
     * @param limit  50
     * @return
     */
    @GET("/book/by-categories")
    fun getBooksByCats(@Query("gender") gender: String, @Query("type") type: String, @Query("major") major: String, @Query("minor") minor: String, @Query("start") start: Int, @Query("limit") limit: Int): Observable<BooksByCats>


    /**
     * 获取综合讨论区帖子列表
     * 全部、默认排序  http://api.zhuishushenqi.com/post/by-block?block=ramble&duration=all&sort=updated&type=all&start=0&limit=20&distillate=
     * 精品、默认排序  http://api.zhuishushenqi.com/post/by-block?block=ramble&duration=all&sort=updated&type=all&start=0&limit=20&distillate=true
     *
     * @param block      ramble:综合讨论区
     * original：原创区
     * @param duration   all
     * @param sort       updated(默认排序)
     * created(最新发布)
     * comment-count(最多评论)
     * @param type       all
     * @param start      0
     * @param limit      20
     * @param distillate true(精品)
     * @return
     */
    @GET("/post/by-block")
    fun getBookDisscussionList(@Query("block") block: String, @Query("duration") duration: String, @Query("sort") sort: String, @Query("type") type: String, @Query("start") start: String, @Query("limit") limit: String, @Query("distillate") distillate: String): Observable<DiscussionList>

    /**
     * 获取综合讨论区帖子详情
     *
     * @param disscussionId->_id
     * @return
     */
    @GET("/post/{disscussionId}")
    fun getBookDisscussionDetail(@Path("disscussionId") disscussionId: String): Observable<Disscussion>

    /**
     * 获取神评论列表(综合讨论区、书评区、书荒区皆为同一接口)
     *
     * @param disscussionId->_id
     * @return
     */
    @GET("/post/{disscussionId}/comment/best")
    fun getBestComments(@Path("disscussionId") disscussionId: String): Observable<CommentList>

    /**
     * 获取综合讨论区帖子详情内的评论列表
     *
     * @param disscussionId->_id
     * @param start              0
     * @param limit              30
     * @return
     */
    @GET("/post/{disscussionId}/comment")
    fun getBookDisscussionComments(@Path("disscussionId") disscussionId: String, @Query("start") start: String, @Query("limit") limit: String): Observable<CommentList>

    /**
     * 获取书评区帖子列表
     * 全部、全部类型、默认排序  http://api.zhuishushenqi.com/post/review?duration=all&sort=updated&type=all&start=0&limit=20&distillate=
     * 精品、玄幻奇幻、默认排序  http://api.zhuishushenqi.com/post/review?duration=all&sort=updated&type=xhqh&start=0&limit=20&distillate=true
     *
     * @param duration   all
     * @param sort       updated(默认排序)
     * created(最新发布)
     * helpful(最有用的)
     * comment-count(最多评论)
     * @param type       all(全部类型)、xhqh(玄幻奇幻)、dsyn(都市异能)...
     * @param start      0
     * @param limit      20
     * @param distillate true(精品) 、空字符（全部）
     * @return
     */
    @GET("/post/review")
    fun getBookReviewList(@Query("duration") duration: String, @Query("sort") sort: String, @Query("type") type: String, @Query("start") start: String, @Query("limit") limit: String, @Query("distillate") distillate: String): Observable<BookReviewList>

    /**
     * 获取书评区帖子详情
     *
     * @param bookReviewId->_id
     * @return
     */
    @GET("/post/review/{bookReviewId}")
    fun getBookReviewDetail(@Path("bookReviewId") bookReviewId: String): Observable<BookReview>

    /**
     * 获取书评区、书荒区帖子详情内的评论列表
     *
     * @param bookReviewId->_id
     * @param start             0
     * @param limit             30
     * @return
     */
    @GET("/post/review/{bookReviewId}/comment")
    fun getBookReviewComments(@Path("bookReviewId") bookReviewId: String, @Query("start") start: String, @Query("limit") limit: String): Observable<CommentList>

    /**
     * 获取书荒区帖子列表
     * 全部、默认排序  http://api.zhuishushenqi.com/post/help?duration=all&sort=updated&start=0&limit=20&distillate=
     * 精品、默认排序  http://api.zhuishushenqi.com/post/help?duration=all&sort=updated&start=0&limit=20&distillate=true
     *
     * @param duration   all
     * @param sort       updated(默认排序)
     * created(最新发布)
     * comment-count(最多评论)
     * @param start      0
     * @param limit      20
     * @param distillate true(精品) 、空字符（全部）
     * @return
     */
    @GET("/post/help")
    fun getBookHelpList(@Query("duration") duration: String, @Query("sort") sort: String, @Query("start") start: String, @Query("limit") limit: String, @Query("distillate") distillate: String): Observable<BookHelpList>

    /**
     * 获取书荒区帖子详情
     *
     * @param helpId->_id
     * @return
     */
    @GET("/post/help/{helpId}")
    fun getBookHelpDetail(@Path("helpId") helpId: String): Observable<BookHelp>

    /**
     * 第三方登陆
     *
     * @param platform_uid
     * @param platform_token
     * @param platform_code  “QQ”
     * @return
     */
    @POST("/user/login")
    fun login(@Body loginReq: LoginReq): Observable<Login>

    @GET("/user/followings/{userid}")
    fun getFollowings(@Path("userid") userId: String): Observable<Following>

    /**
     * 获取书籍详情讨论列表
     *
     * @param book  bookId
     * @param sort  updated(默认排序)
     * created(最新发布)
     * comment-count(最多评论)
     * @param type  normal
     * vote
     * @param start 0
     * @param limit 20
     * @return
     */
    @GET("/post/by-book")
    fun getBookDetailDisscussionList(@Query("book") book: String, @Query("sort") sort: String, @Query("type") type: String, @Query("start") start: String, @Query("limit") limit: String): Observable<DiscussionList>

    /**
     * 获取书籍详情书评列表
     *
     * @param book  bookId
     * @param sort  updated(默认排序)
     * created(最新发布)
     * helpful(最有用的)
     * comment-count(最多评论)
     * @param start 0
     * @param limit 20
     * @return
     */
    @GET("/post/review/by-book")
    fun getBookDetailReviewList(@Query("book") book: String, @Query("sort") sort: String, @Query("start") start: String, @Query("limit") limit: String): Observable<HotReview>

    @GET("/post/original")
    fun getBookOriginalList(@Query("block") block: String, @Query("duration") duration: String, @Query("sort") sort: String, @Query("type") type: String, @Query("start") start: String, @Query("limit") limit: String, @Query("distillate") distillate: String): Observable<DiscussionList>
}