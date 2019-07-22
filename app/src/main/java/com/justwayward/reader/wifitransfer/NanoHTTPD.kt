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

import android.util.Log

import com.justwayward.reader.api.BookApi
import com.justwayward.reader.bean.BookMixAToc
import com.justwayward.reader.bean.ChapterRead
import com.justwayward.reader.manager.CacheManager
import com.justwayward.reader.utils.FileUtils

import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter
import java.io.RandomAccessFile
import java.io.UnsupportedEncodingException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.HashMap
import java.util.Locale
import java.util.StringTokenizer
import java.util.TimeZone

import nl.siegmann.epublib.domain.Book
import rx.Observer

abstract class NanoHTTPD(private val hostname: String?, private val myPort: Int) {
    private var myServerSocket: ServerSocket? = null
    private var myThread: Thread? = null
    private var tempFileManagerFactory: TempFileManagerFactory? = null
    private var asyncRunner: AsyncRunner? = null

    /**
     * Constructs an HTTP server on given port.
     */
    constructor(port: Int) : this(null, port) {}

    init {
        this.tempFileManagerFactory = DefaultTempFileManagerFactory()
        this.asyncRunner = DefaultAsyncRunner()
    }

    /**
     * Starts the server
     *
     *
     * Throws an IOException if the socket is already in use
     */
    @Throws(IOException::class)
    fun start() {
        Log.i("NanoHTTPD", "server start")
        myServerSocket = ServerSocket()
        myServerSocket!!.bind(hostname?.let { InetSocketAddress(it, myPort) }
                ?: InetSocketAddress(myPort))

        myThread = Thread(Runnable {
            do {
                try {
                    val finalAccept = myServerSocket!!.accept()
                    Log.i("NanoHTTPD",
                            "accept request from " + finalAccept!!.inetAddress)
                    val inputStream = finalAccept.getInputStream()
                    val outputStream = finalAccept
                            .getOutputStream()
                    val tempFileManager = tempFileManagerFactory!!
                            .create()
                    val session = HTTPSession(
                            tempFileManager, inputStream, outputStream)
                    asyncRunner!!.exec(Runnable {
                        session.run()
                        if (finalAccept != null) {
                            try {
                                finalAccept.close()
                            } catch (ignored: IOException) {
                            }

                        }
                    })
                } catch (e: IOException) {
                }

            } while (!myServerSocket!!.isClosed)
        })
        myThread!!.isDaemon = true
        myThread!!.name = "NanoHttpd Main Listener"
        myThread!!.start()
    }

    /**
     * Stops the server.
     */
    fun stop() {
        Log.i("NanoHTTPD", "server stop")
        try {
            myServerSocket!!.close()
            myThread!!.join()
        } catch (ioe: IOException) {
            ioe.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    fun setTempFileManagerFactory(
            tempFileManagerFactory: TempFileManagerFactory) {
        this.tempFileManagerFactory = tempFileManagerFactory
    }

    fun setAsyncRunner(asyncRunner: AsyncRunner) {
        this.asyncRunner = asyncRunner
    }

    /**
     * Override this to customize the server.
     *
     *
     *
     *
     * (By default, this delegates to serveFile() and allows directory listing.)
     *
     * @param uri    Percent-decoded URI without parameters, for example
     * "/index.cgi"
     * @param method "GET", "POST" etc.
     * @param parms  Parsed, percent decoded parameters from URI and, in case of
     * POST, data.
     * @param header Header entries, percent decoded
     * @return HTTP response, see class Response for details
     */
    abstract fun serve(uri: String?, method: Method,
                       header: Map<String, String>, parms: Map<String, String>,
                       files: Map<String, String>): Response?

    /**
     * Decodes the percent encoding scheme. <br></br>
     * For example: "an+example%20string" -> "an example string"
     */
    @Throws(InterruptedException::class)
    protected fun decodePercent(str: String): String {
        try {
            val sb = StringBuilder()
            var i = 0
            while (i < str.length) {
                val c = str[i]
                when (c) {
                    '+' -> sb.append(' ')
                    '%' -> {
                        sb.append(Integer.parseInt(
                                str.substring(i + 1, i + 3), 16).toChar())
                        i += 2
                    }
                    else -> sb.append(c)
                }
                i++
            }
            return sb.toString()
        } catch (e: Exception) {
            throw InterruptedException()
        }

    }

    protected fun decodeParameters(
            parms: Map<String, String>): Map<String, List<String>> {
        return this.decodeParameters(parms[QUERY_STRING_PARAMETER])
    }

    protected fun decodeParameters(queryString: String?): Map<String, List<String>> {
        val parms = HashMap<String, List<String>>()
        if (queryString != null) {
            val st = StringTokenizer(queryString, "&")
            while (st.hasMoreTokens()) {
                val e = st.nextToken()
                val sep = e.indexOf('=')
                try {
                    val propertyName = if (sep >= 0)
                        decodePercent(
                                e.substring(0, sep)).trim { it <= ' ' }
                    else
                        decodePercent(e)
                                .trim { it <= ' ' }
                    if (!parms.containsKey(propertyName)) {
                        parms[propertyName] = ArrayList()
                    }
                    val propertyValue = if (sep >= 0)
                        decodePercent(e
                                .substring(sep + 1))
                    else
                        null
                    if (propertyValue != null) {
                        parms[propertyName]!!.add(propertyValue)
                    }
                } catch (e1: InterruptedException) {
                    e1.printStackTrace()
                }

            }
        }
        return parms
    }

    enum class Method {
        GET, PUT, POST, DELETE;


        companion object {

            internal fun lookup(method: String?): Method? {
                for (m in Method.values()) {
                    if (m.toString().equals(method!!, ignoreCase = true)) {
                        return m
                    }
                }
                return null
            }
        }
    }

    interface AsyncRunner {
        fun exec(code: Runnable)
    }

    interface TempFileManagerFactory {
        fun create(): TempFileManager
    }

    interface TempFileManager {
        @Throws(Exception::class)
        fun createTempFile(): TempFile

        fun clear()
    }

    interface TempFile {

        val name: String
        @Throws(Exception::class)
        fun open(): OutputStream

        @Throws(Exception::class)
        fun delete()
    }

    /**
     * HTTP response. Return one of these from serve().
     */
    class Response {
        private val bookId: String
        private val bookMix: BookMixAToc.mixToc?

        private val bookApi: BookApi
        /**
         * HTTP status code after processing, e.g. "200 OK", HTTP_OK
         */
        var status: Status? = null
        /**
         * MIME type of content, e.g. "text/html"
         */
        var mimeType: String? = null
        /**
         * Data of the response, may be null.
         */
        var data: InputStream? = null

        /**
         * Headers for the HTTP response. Use addHeader() to add lines.
         */
        var header: MutableMap<String, String>? = HashMap()


        /**
         * start Chapters index
         */
        var start: Int = 0

        /**
         * Default constructor: response = HTTP_OK, mime = MIME_HTML and your
         * supplied message
         */
        constructor(msg: String) : this(Status.OK, MIME_HTML, msg) {}

        /**
         * Basic constructor.
         */
        constructor(status: Status, mimeType: String, data: InputStream) {
            this.status = status
            this.mimeType = mimeType
            this.data = data
        }

        /**
         * Convenience method that makes an InputStream out of given text.
         */
        constructor(status: Status, mimeType: String, txt: String) {
            this.status = status
            this.mimeType = mimeType
            try {
                this.data = ByteArrayInputStream(txt.toByteArray(charset("UTF-8")))
            } catch (uee: UnsupportedEncodingException) {
                uee.printStackTrace()
            }

        }

        constructor(status: Status, mimeType: String, bookId: String, bookMix: BookMixAToc.mixToc, bookApi: BookApi, start: Int) {
            this.status = status
            this.mimeType = mimeType
            this.bookId = bookId
            this.bookMix = bookMix
            this.bookApi = bookApi
            this.start = start
        }

        /**
         * Adds given line to the header.
         */
        fun addHeader(name: String, value: String) {
            header!![name] = value
        }

        /**
         * Sends given response to the socket.
         */
        private fun send(outputStream: OutputStream) {
            val mime = mimeType
            val gmtFrmt = SimpleDateFormat(
                    "E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US)
            gmtFrmt.timeZone = TimeZone.getTimeZone("GMT")

            try {
                if (status == null) {
                    throw Error("sendResponse(): Status can't be null.")
                }
                val pw = PrintWriter(outputStream)
                pw.print("HTTP/1.0 " + status!!.description + " \r\n")

                if (mime != null) {
                    pw.print("Content-Type: $mime\r\n")
                }

                if (header == null || header!!["Date"] == null) {
                    pw.print("Date: " + gmtFrmt.format(Date()) + "\r\n")
                }

                if (header != null) {
                    for (key in header!!.keys) {
                        val value = header!![key]
                        pw.print("$key: $value\r\n")
                    }
                }

                pw.print("\r\n")
                pw.flush()

                sendInputData(outputStream, data)
                //              上传文件
                witBook(pw, outputStream)
                outputStream.flush()
                outputStream.close()
                if (data != null)
                    data!!.close()
            } catch (ioe: IOException) {
                // Couldn't write? No can do.
                ioe.printStackTrace()
            }

        }

        fun witBook(pw: PrintWriter, outputStream: OutputStream) {
            if (this.bookMix == null) {
                return
            }
            val list = this.bookMix.chapters
            for (i in start until list!!.size) {
                val character = list[i]
                val title = character.title
                val fileIndex = CacheManager.instance.getChapterFile(this.bookId, i + 1)
                if (fileIndex != null) {
                    var fis: FileInputStream? = null
                    try {
                        fis = FileInputStream(fileIndex)
                        pw.print("\r\n")
                        pw.print(title)
                        pw.print("\r\n")
                        pw.flush()
                        sendInputData(outputStream, fis)
                        outputStream.flush()
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        if (fis != null) {
                            try {
                                fis.close()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                        }
                    }
                } else {
                    //                      网络请求前先暂停350ms
                    try {
                        Thread.sleep(350)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    Log.i("tag_", "onload: " + character.link)
                    //                  开始加载网页数据
                    bookApi.getChapterRead(character.link).subscribe(object : Observer<ChapterRead> {
                        override fun onCompleted() {
                            //                                call(integer, observer);
                        }

                        override fun onError(e: Throwable) {

                        }

                        override fun onNext(chapterRead: ChapterRead) {
                            Log.i("tag_", "onload end: chapterRead -> $title")
                            pw.print("\r\n")
                            pw.print(title)
                            pw.print("\r\n")
                            pw.print(chapterRead.chapter!!.body)
                            pw.flush()
                            //                          保存当前的文章到本地
                            CacheManager.instance.saveChapterFile(this@Response.bookId, i, chapterRead.chapter)
                        }
                    })
                }
            }
        }


        //        public String getTitle(File file ,int i ,BookMixAToc.mixToc mixToc){
        //            if(mixToc != null && mixToc.chapters != null && i < mixToc.chapters.size() ){
        //                return mixToc.chapters.get(i).title;
        //            }else{
        //                return "第" + FileUtils.getFileNameNotType(file) + "章";
        //            }
        //
        //        }

        @Throws(IOException::class)
        private fun sendInputData(outputStream: OutputStream, data: InputStream?) {
            if (data != null) {
                var pending = data.available() // This is to support
                // partial sends, see
                // serveFile()
                val BUFFER_SIZE = 16 * 1024
                val buff = ByteArray(BUFFER_SIZE)
                while (pending > 0) {
                    val read = data.read(buff, 0,
                            if (pending > BUFFER_SIZE)
                                BUFFER_SIZE
                            else
                                pending)
                    if (read <= 0) {
                        break
                    }
                    outputStream.write(buff, 0, read)
                    pending -= read
                }
            }
        }

        /**
         * Some HTTP response status codes
         */
        enum class Status private constructor(val requestStatus: Int, private val descr: String) {
            OK(200, "OK"), CREATED(201, "Created"), NO_CONTENT(204,
                    "No Content"),
            PARTIAL_CONTENT(206, "Partial Content"), REDIRECT(
                    301, "Moved Permanently"),
            NOT_MODIFIED(304, "Not Modified"), BAD_REQUEST(
                    400, "Bad Request"),
            UNAUTHORIZED(401, "Unauthorized"), FORBIDDEN(
                    403, "Forbidden"),
            NOT_FOUND(404, "Not Found"), RANGE_NOT_SATISFIABLE(
                    416, "Requested Range Not Satisfiable"),
            INTERNAL_ERROR(
                    500, "Internal Server Error");

            val description: String
                get() = "" + this.requestStatus + " " + descr
        }

        companion object {

            fun error(outputStream: OutputStream, error: Status,
                      message: String) {
                Response(error, MIME_PLAINTEXT, message).send(outputStream)
            }
        }
    }

    class DefaultTempFile @Throws(IOException::class)
    constructor(tempdir: String) : TempFile {
        private val file: File
        private val fstream: OutputStream

        override val name: String
            get() = file.absolutePath

        init {
            file = File.createTempFile("NanoHTTPD-", "", File(tempdir))
            fstream = FileOutputStream(file)
        }

        @Throws(Exception::class)
        override fun open(): OutputStream {
            return fstream
        }

        @Throws(Exception::class)
        override fun delete() {
            file.delete()
        }
    }

    /**
     * Handles one session, i.e. parses the HTTP request and returns the
     * response.
     */
    protected inner class HTTPSession(private val tempFileManager: TempFileManager,
                                      private val inputStream: InputStream?, private val outputStream: OutputStream) : Runnable {

        private val tmpBucket: RandomAccessFile?
            @Throws(IOException::class)
            get() {
                try {
                    val tempFile = tempFileManager.createTempFile()
                    return RandomAccessFile(tempFile.name, "rw")
                } catch (e: Exception) {
                    System.err.println("Error: " + e.message)
                }

                return null
            }

        override fun run() {
            try {
                if (inputStream == null) {
                    return
                }

                // Read the first 8192 bytes.
                // The full header should fit in here.
                // Apache's default header limit is 8KB.
                // Do NOT assume that a single read will get the entire header
                // at once!
                var buf = ByteArray(BUFSIZE)
                var splitbyte = 0
                var rlen = 0
                run {
                    var read = inputStream!!.read(buf, 0, BUFSIZE)
                    while (read > 0) {
                        rlen += read
                        splitbyte = findHeaderEnd(buf, rlen)
                        if (splitbyte > 0)
                            break
                        read = inputStream.read(buf, rlen, BUFSIZE - rlen)
                    }
                }

                // Create a BufferedReader for parsing the header.
                val hin = BufferedReader(InputStreamReader(
                        ByteArrayInputStream(buf, 0, rlen)))
                val pre = HashMap<String, String>()
                val parms = HashMap<String, String>()
                val header = HashMap<String, String>()
                val files = HashMap<String, String>()

                // Decode the header into parms and header java properties
                decodeHeader(hin, pre, parms, header)
                val method = Method.lookup(pre["method"])
                if (method == null) {
                    Response.error(outputStream, Response.Status.BAD_REQUEST,
                            "BAD REQUEST: Syntax error.")
                    throw InterruptedException()
                }
                val uri = pre["uri"]
                var size = extractContentLength(header)

                // Write the part of body already read to ByteArrayOutputStream
                // f
                val f = tmpBucket
                if (splitbyte < rlen) {
                    f!!.write(buf, splitbyte, rlen - splitbyte)
                }

                // While Firefox sends on the first read all the data fitting
                // our buffer, Chrome and Opera send only the headers even if
                // there is data for the body. We do some magic here to find
                // out whether we have already consumed part of body, if we
                // have reached the end of the data to be sent or we should
                // expect the first byte of the body at the next read.
                if (splitbyte < rlen) {
                    size -= (rlen - splitbyte + 1).toLong()
                } else if (splitbyte == 0 || size == 0x7FFFFFFFFFFFFFFFL) {
                    size = 0
                }

                // Now read all the body and write it to f
                buf = ByteArray(512)
                while (rlen >= 0 && size > 0) {
                    rlen = inputStream.read(buf, 0, 512)
                    size -= rlen.toLong()
                    if (rlen > 0) {
                        f!!.write(buf, 0, rlen)
                    }
                }

                // Get the raw body as a byte []
                val fbuf = f!!.channel.map(
                        FileChannel.MapMode.READ_ONLY, 0, f.length())
                f.seek(0)

                // Create a BufferedReader for easily reading it as string.
                val bin = FileInputStream(f.fd)
                val `in` = BufferedReader(InputStreamReader(
                        bin))

                // If the method is POST, there may be parameters
                // in data section, too, read it:
                if (Method.POST == method) {
                    var contentType = ""
                    val contentTypeHeader = header["content-type"]

                    var st: StringTokenizer? = null
                    if (contentTypeHeader != null) {
                        st = StringTokenizer(contentTypeHeader, ",; ")
                        if (st.hasMoreTokens()) {
                            contentType = st.nextToken()
                        }
                    }

                    if ("multipart/form-data".equals(contentType, ignoreCase = true)) {
                        // Handle multipart/form-data
                        if (!st!!.hasMoreTokens()) {
                            Response.error(
                                    outputStream,
                                    Response.Status.BAD_REQUEST,
                                    "BAD REQUEST: Content type is multipart/form-data but boundary missing. Usage: GET /example/file.html")
                            throw InterruptedException()
                        }

                        val boundaryStartString = "boundary="
                        val boundaryContentStart = contentTypeHeader!!
                                .indexOf(boundaryStartString) + boundaryStartString.length
                        var boundary = contentTypeHeader.substring(
                                boundaryContentStart,
                                contentTypeHeader.length)
                        if (boundary.startsWith("\"") && boundary.startsWith("\"")) {
                            boundary = boundary.substring(1,
                                    boundary.length - 1)
                        }

                        decodeMultipartData(boundary, fbuf, `in`, parms, files)
                    } else {
                        // Handle application/x-www-form-urlencoded
                        var postLine = ""
                        val pbuf = CharArray(512)
                        var read = `in`.read(pbuf)
                        while (read >= 0 && !postLine.endsWith("\r\n")) {
                            postLine += String(pbuf, 0, read)
                            read = `in`.read(pbuf)
                        }
                        postLine = postLine.trim { it <= ' ' }
                        decodeParms(postLine, parms)
                    }
                }

                if (Method.PUT == method)
                    files["content"] = saveTmpFile(fbuf, 0, fbuf.limit())

                // Ok, now do the serve()
                val r = serve(uri, method, header, parms, files)
                if (r == null) {
                    Response.error(outputStream,
                            Response.Status.INTERNAL_ERROR,
                            "SERVER INTERNAL ERROR: Serve() returned a null response.")
                    throw InterruptedException()
                } else {
                    r.send(outputStream)
                }

                `in`.close()
                inputStream.close()
            } catch (ioe: IOException) {
                try {
                    Response.error(
                            outputStream,
                            Response.Status.INTERNAL_ERROR,
                            "SERVER INTERNAL ERROR: IOException: " + ioe.message)
                    throw InterruptedException()
                } catch (ignored: Throwable) {
                    ignored.printStackTrace()
                }

            } catch (ie: InterruptedException) {
                // Thrown by sendError, ignore and exit the thread.
                ie.printStackTrace()
            } finally {
                tempFileManager.clear()
            }
        }

        private fun extractContentLength(header: Map<String, String>): Long {
            var size = 0x7FFFFFFFFFFFFFFFL
            val contentLength = header["content-length"]
            if (contentLength != null) {
                try {
                    size = Integer.parseInt(contentLength).toLong()
                } catch (ex: NumberFormatException) {
                    ex.printStackTrace()
                }

            }
            return size
        }

        /**
         * Decodes the sent headers and loads the data into Key/value pairs
         */
        @Throws(InterruptedException::class)
        private fun decodeHeader(`in`: BufferedReader, pre: MutableMap<String, String>,
                                 parms: MutableMap<String, String>, header: MutableMap<String, String>) {
            try {
                // Read the request line
                val inLine = `in`.readLine() ?: return

                val st = StringTokenizer(inLine)
                if (!st.hasMoreTokens()) {
                    Response.error(outputStream, Response.Status.BAD_REQUEST,
                            "BAD REQUEST: Syntax error. Usage: GET /example/file.html")
                    throw InterruptedException()
                }

                pre["method"] = st.nextToken()

                if (!st.hasMoreTokens()) {
                    Response.error(outputStream, Response.Status.BAD_REQUEST,
                            "BAD REQUEST: Missing URI. Usage: GET /example/file.html")
                    throw InterruptedException()
                }

                var uri = st.nextToken()

                // Decode parameters from the URI
                val qmi = uri.indexOf('?')
                if (qmi >= 0) {
                    decodeParms(uri.substring(qmi + 1), parms)
                    uri = decodePercent(uri.substring(0, qmi))
                } else {
                    uri = decodePercent(uri)
                }

                // If there's another token, it's protocol version,
                // followed by HTTP headers. Ignore version but parse headers.
                // NOTE: this now forces header names lowercase since they are
                // case insensitive and vary by client.
                if (st.hasMoreTokens()) {
                    var line: String? = `in`.readLine()
                    while (line != null && line.trim { it <= ' ' }.length > 0) {
                        val p = line.indexOf(':')
                        if (p >= 0)
                            header[line.substring(0, p).trim { it <= ' ' }
                                    .toLowerCase()] = line.substring(p + 1)
                                    .trim { it <= ' ' }
                        line = `in`.readLine()
                    }
                }

                pre["uri"] = uri
            } catch (ioe: IOException) {
                Response.error(
                        outputStream,
                        Response.Status.INTERNAL_ERROR,
                        "SERVER INTERNAL ERROR: IOException: " + ioe.message)
                throw InterruptedException()
            }

        }

        /**
         * Decodes the Multipart Body data and put it into Key/Value pairs.
         */
        @Throws(InterruptedException::class)
        private fun decodeMultipartData(boundary: String, fbuf: ByteBuffer,
                                        `in`: BufferedReader, parms: MutableMap<String, String>,
                                        files: MutableMap<String, String>) {
            try {
                val bpositions = getBoundaryPositions(fbuf,
                        boundary.toByteArray())
                var boundarycount = 1
                var mpline: String? = `in`.readLine()
                while (mpline != null) {
                    if (!mpline.contains(boundary)) {
                        Response.error(
                                outputStream,
                                Response.Status.BAD_REQUEST,
                                "BAD REQUEST: Content type is multipart/form-data but next chunk does not start with boundary. Usage: GET /example/file.html")
                        throw InterruptedException()
                    }
                    boundarycount++
                    val item = HashMap<String, String>()
                    mpline = `in`.readLine()
                    while (mpline != null && mpline.trim { it <= ' ' }.length > 0) {
                        val p = mpline.indexOf(':')
                        if (p != -1) {
                            item[mpline.substring(0, p).trim { it <= ' ' }
                                    .toLowerCase()] = mpline.substring(p + 1)
                                    .trim { it <= ' ' }
                        }
                        mpline = `in`.readLine()
                    }
                    if (mpline != null) {
                        val contentDisposition = item["content-disposition"]
                        if (contentDisposition == null) {
                            Response.error(
                                    outputStream,
                                    Response.Status.BAD_REQUEST,
                                    "BAD REQUEST: Content type is multipart/form-data but no content-disposition info found. Usage: GET /example/file.html")
                            throw InterruptedException()
                        }
                        val st = StringTokenizer(contentDisposition, "; ")
                        val disposition = HashMap<String, String>()
                        while (st.hasMoreTokens()) {
                            val token = st.nextToken()
                            val p = token.indexOf('=')
                            if (p != -1) {
                                disposition[token.substring(0, p).trim { it <= ' ' }
                                        .toLowerCase()] = token.substring(p + 1)
                                        .trim { it <= ' ' }
                            }
                        }
                        var pname = disposition["name"]
                        pname = pname!!.substring(1, pname.length - 1)

                        var value: String? = ""
                        if (item["content-type"] == null) {
                            while (mpline != null && !mpline.contains(boundary)) {
                                mpline = `in`.readLine()
                                if (mpline != null) {
                                    val d = mpline.indexOf(boundary)
                                    if (d == -1) {
                                        value += mpline
                                    } else {
                                        value += mpline.substring(0, d - 2)
                                    }
                                }
                            }
                        } else {
                            if (boundarycount > bpositions.size) {
                                Response.error(outputStream,
                                        Response.Status.INTERNAL_ERROR,
                                        "Error processing request")
                                throw InterruptedException()
                            }
                            val offset = stripMultipartHeaders(fbuf,
                                    bpositions[boundarycount - 2])
                            val path = saveTmpFile(fbuf, offset,
                                    bpositions[boundarycount - 1] - offset - 4)
                            files[pname] = path
                            value = disposition["filename"]
                            value = value!!.substring(1, value.length - 1)
                            do {
                                mpline = `in`.readLine()
                            } while (mpline != null && !mpline.contains(boundary))
                        }
                        parms[pname] = value!!
                    }
                }
            } catch (ioe: IOException) {
                Response.error(
                        outputStream,
                        Response.Status.INTERNAL_ERROR,
                        "SERVER INTERNAL ERROR: IOException: " + ioe.message)
                throw InterruptedException()
            }

        }

        /**
         * Find byte index separating header from body. It must be the last byte
         * of the first two sequential new lines.
         */
        private fun findHeaderEnd(buf: ByteArray, rlen: Int): Int {
            var splitbyte = 0
            while (splitbyte + 3 < rlen) {
                if (buf[splitbyte] == '\r'.toByte() && buf[splitbyte + 1] == '\n'.toByte()
                        && buf[splitbyte + 2] == '\r'.toByte()
                        && buf[splitbyte + 3] == '\n'.toByte()) {
                    return splitbyte + 4
                }
                splitbyte++
            }
            return 0
        }

        /**
         * Find the byte positions where multipart boundaries start.
         */
        fun getBoundaryPositions(b: ByteBuffer, boundary: ByteArray): IntArray {
            var matchcount = 0
            var matchbyte = -1
            val matchbytes = ArrayList<Int>()
            run {
                var i = 0
                while (i < b.limit()) {
                    if (b.get(i) == boundary[matchcount]) {
                        if (matchcount == 0)
                            matchbyte = i
                        matchcount++
                        if (matchcount == boundary.size) {
                            matchbytes.add(matchbyte)
                            matchcount = 0
                            matchbyte = -1
                        }
                    } else {
                        i -= matchcount
                        matchcount = 0
                        matchbyte = -1
                    }
                    i++
                }
            }
            val ret = IntArray(matchbytes.size)
            for (i in ret.indices) {
                ret[i] = matchbytes[i]
            }
            return ret
        }

        /**
         * Retrieves the content of a sent file and saves it to a temporary
         * file. The full path to the saved file is returned.
         */
        private fun saveTmpFile(b: ByteBuffer, offset: Int, len: Int): String {
            var path = ""
            if (len > 0) {
                try {
                    val tempFile = tempFileManager.createTempFile()
                    val src = b.duplicate()
                    val dest = FileOutputStream(tempFile.name)
                            .channel
                    src.position(offset).limit(offset + len)
                    dest.write(src.slice())
                    path = tempFile.name
                } catch (e: Exception) { // Catch exception if any
                    System.err.println("Error: " + e.message)
                }

            }
            return path
        }

        /**
         * It returns the offset separating multipart file headers from the
         * file's data.
         */
        private fun stripMultipartHeaders(b: ByteBuffer, offset: Int): Int {
            var i: Int
            i = offset
            while (i < b.limit()) {
                if (b.get(i) == '\r'.toByte() && b.get(++i) == '\n'.toByte()
                        && b.get(++i) == '\r'.toByte() && b.get(++i) == '\n'.toByte()) {
                    break
                }
                i++
            }
            return i + 1
        }

        /**
         * Decodes parameters in percent-encoded URI-format ( e.g.
         * "name=Jack%20Daniels&pass=Single%20Malt" ) and adds them to given
         * Map. NOTE: this doesn't support multiple identical keys due to the
         * simplicity of Map.
         */
        @Throws(InterruptedException::class)
        private fun decodeParms(parms: String?, p: MutableMap<String, String>) {
            if (parms == null) {
                p[QUERY_STRING_PARAMETER] = ""
                return
            }

            p[QUERY_STRING_PARAMETER] = parms
            val st = StringTokenizer(parms, "&")
            try {
                while (st.hasMoreTokens()) {
                    val e = st.nextToken()
                    val sep = e.indexOf('=')
                    if (sep >= 0) {
                        p[decodePercent(e.substring(0, sep)).trim { it <= ' ' }] = decodePercent(e.substring(sep + 1))
                    } else {
                        p[decodePercent(e).trim { it <= ' ' }] = ""
                    }
                }
            } catch (e: InterruptedException) {
                Response.error(outputStream, Response.Status.BAD_REQUEST,
                        "BAD REQUEST: Bad percent-encoding.")
            }

        }

        companion object {
            val BUFSIZE = 8192
        }
    }

    private inner class DefaultTempFileManagerFactory : TempFileManagerFactory {
        override fun create(): TempFileManager {
            return DefaultTempFileManager()
        }
    }

    class DefaultTempFileManager : TempFileManager {
        private val tmpdir: String?
        private val tempFiles: MutableList<TempFile>

        init {
            tmpdir = System.getProperty("java.io.tmpdir")
            tempFiles = ArrayList()
        }

        @Throws(Exception::class)
        override fun createTempFile(): TempFile {
            val tempFile = DefaultTempFile(tmpdir)
            tempFiles.add(tempFile)
            return tempFile
        }

        override fun clear() {
            for (file in tempFiles) {
                try {
                    file.delete()
                } catch (ignored: Exception) {
                }

            }
            tempFiles.clear()
        }
    }

    private inner class DefaultAsyncRunner : AsyncRunner {
        private var requestCount: Long = 0

        override fun exec(code: Runnable) {
            ++requestCount
            val t = Thread(code)
            t.isDaemon = true
            t.name = "NanoHttpd Request Processor (#$requestCount)"
            t.start()
        }
    }

    companion object {
        /*
     * Pseudo-Parameter to use to store the actual query string in the
     * parameters map for later re-processing.
     */
        val QUERY_STRING_PARAMETER = "NanoHttpd.QUERY_STRING"
        /**
         * Common mime types for dynamic content
         */
        val MIME_PLAINTEXT = "text/plain"
        val MIME_HTML = "text/html"
        val MIME_DEFAULT_BINARY = "application/octet-stream"
    }
}
