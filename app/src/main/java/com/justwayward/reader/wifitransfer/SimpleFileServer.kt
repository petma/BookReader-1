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
package com.justwayward.reader.wifitransfer

import android.text.TextUtils

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.justwayward.reader.ReaderApplication
import com.justwayward.reader.api.BookApi
import com.justwayward.reader.bean.BookMixAToc
import com.justwayward.reader.bean.Recommend
import com.justwayward.reader.bean.user.HtmlBook
import com.justwayward.reader.manager.CollectionsManager
import com.justwayward.reader.manager.EventManager
import com.justwayward.reader.utils.ACache
import com.justwayward.reader.utils.FileUtils
import com.justwayward.reader.utils.LogUtils
import com.justwayward.reader.utils.RxUtil
import com.justwayward.reader.utils.StringUtils

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.ArrayList
import java.util.Arrays

import rx.Observable
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Func1

/**
 * Wifi传书 服务端
 *
 * @author yuyh.
 * @date 2016/10/10.
 */
class SimpleFileServer(port: Int) : NanoHTTPD(port) {
    private var bookApi: BookApi? = null

    override fun serve(uri: String?, method: NanoHTTPD.Method,
                       header: Map<String, String>, parms: Map<String, String>,
                       files: Map<String, String>): NanoHTTPD.Response? {
        var uri = uri
        if (NanoHTTPD.Method.GET == method) {
            try {
                uri = String(uri!!.toByteArray(charset("ISO-8859-1")), "UTF-8")
                LogUtils.d("uri= $uri")
            } catch (e: UnsupportedEncodingException) {
                LogUtils.w("URL参数编码转换错误：$e")
            }

            //return new Response(HtmlConst.HTML_STRING);
            if (uri!!.contains("index.html") || uri == "/") {
                return NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, "text/html", String(FileUtils.readAssets("/index.html")))
            } else if (uri.startsWith("/files/") && uri.endsWith(".txt")) {
                val name = parms["name"]
                val start = parms["start"]
                var startIndex = 0
                try {
                    startIndex = Integer.parseInt(start!!)
                } catch (ex: NumberFormatException) {
                    ex.printStackTrace()
                }

                var printName: String
                try {
                    printName = URLDecoder.decode(parms["name"], "utf-8")
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                    printName = "tt"
                }

                val bookid = uri.substring(7, uri.lastIndexOf("."))
                LogUtils.d("-->uri= $uri;name:$printName;start:$start")
                //先加载章节列表
                val resp = NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_DEFAULT_BINARY, bookid, getBookMixAToc(bookid, "chapters"), bookApi, startIndex)
                resp.addHeader("content-disposition", "attachment;filename=" + printName + "_" + startIndex + ".txt")//URLEncoder.encode(name + ".txt", "utf-8"));
                return resp
            } else if (uri.startsWith("/files")) {
                val collectionList = CollectionsManager.instance!!.collectionList
                val htmlBooks = ArrayList<HtmlBook>()
                for (recommendBooks in collectionList!!) {
                    htmlBooks.add(HtmlBook(recommendBooks.title, recommendBooks._id, recommendBooks.lastChapter))
                }
                return NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, "text/json", Gson().toJson(htmlBooks))
            } else {
                // 获取文件类型
                val type = Defaults.extensions[uri.substring(uri.lastIndexOf(".") + 1)]
                if (TextUtils.isEmpty(type))
                    return NanoHTTPD.Response("")
                // 读取文件
                val b = FileUtils.readAssets(uri)
                return if (b == null || b.size < 1) NanoHTTPD.Response("") else NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, type, ByteArrayInputStream(b))
                // 响应
            }
        } else {
            // 读取文件
            for (s in files.keys) {
                try {
                    val fis = FileInputStream(files[s])
                    var fileName = parms["newfile"]
                    if (fileName!!.lastIndexOf(".") > 0)
                        fileName = fileName.substring(0, fileName.lastIndexOf("."))

                    // 创建临时文件保存
                    val outputFile = FileUtils.createWifiTempFile()
                    LogUtils.i("--" + outputFile.absolutePath)
                    val fos = FileOutputStream(outputFile)
                    val buffer = ByteArray(1024)
                    while (true) {
                        val byteRead = fis.read(buffer)
                        if (byteRead == -1) {
                            break
                        }
                        fos.write(buffer, 0, byteRead)
                    }
                    fos.close()

                    // 创建目标文件
                    val desc = FileUtils.createWifiTranfesFile(fileName)
                    LogUtils.i("--" + desc.absolutePath)

                    FileUtils.fileChannelCopy(outputFile, desc)

                    // 添加到收藏
                    addToCollection(fileName)

                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            return NanoHTTPD.Response("")
        }
    }

    /**
     * 添加到收藏
     *
     * @param fileName
     */
    private fun addToCollection(fileName: String) {
        val books = Recommend.RecommendBooks()
        books.isFromSD = true

        books._id = fileName
        books.title = fileName

        //Looper.prepare();
        if (CollectionsManager.instance!!.add(books)) {
            //ToastUtils.showToast(String.format(AppUtils.getAppContext().getString(
            //R.string.book_detail_has_joined_the_book_shelf), books.title));
            EventManager.refreshCollectionList()
        } else {
            //ToastUtils.showSingleToast("书籍已存在");
        }
        //Looper.loop();
    }


    fun getBookMixAToc(bookId: String, viewChapters: String): BookMixAToc.mixToc? {

        val mixToc = BookMixAToc()
        val key = StringUtils.creatAcacheKey("book-toc", bookId, viewChapters)
        val json = ACache.get(ReaderApplication.getsInstance()).getAsString(key)
        if (json != null) {
            return Gson().fromJson(json, BookMixAToc.mixToc::class.java)
        } else {
            bookApi!!.getBookMixAToc(bookId, viewChapters).subscribe(object : Observer<BookMixAToc> {
                override fun onNext(data: BookMixAToc) {
                    mixToc.mixToc = data.mixToc
                    ACache.get(ReaderApplication.getsInstance()).put(key, data.mixToc)
                }

                override fun onCompleted() {}

                override fun onError(e: Throwable) {
                    LogUtils.e("onError: $e")
                    //                        mView.netError(0);
                }
            })
        }

        return mixToc.mixToc
    }

    fun setBookApi(mBookApi: BookApi) {
        this.bookApi = mBookApi
    }

    companion object {

        private var server: SimpleFileServer? = null

        val instance: SimpleFileServer
            get() {
                if (server == null) {
                    server = SimpleFileServer(Defaults.port)
                }
                return server
            }
    }
}
