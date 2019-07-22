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

import android.content.Context
import android.os.Environment

import com.justwayward.reader.base.Constant

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.channels.FileChannel
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Comparator
import java.util.Enumeration
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * @author yuyh.
 * @date 16/4/9.
 */
class FileUtils {

    /**
     * 递归获取所有文件
     *
     * @param root
     * @param ext  指定扩展名
     */
    @Synchronized
    private fun getAllFiles(root: File, ext: String) {
        val list = ArrayList<File>()
        val files = root.listFiles()
        if (files != null) {
            for (f in files) {
                if (f.isDirectory) {
                    getAllFiles(f, ext)
                } else {
                    if (f.name.endsWith(ext) && f.length() > 50)
                        list.add(f)
                }
            }
        }
    }

    companion object {

        fun getChapterPath(bookId: String, chapter: Int): String {
            return Constant.PATH_TXT + bookId + File.separator + chapter + ".txt"
        }

        fun getChapterFile(bookId: String, chapter: Int): File {
            val file = File(getChapterPath(bookId, chapter))
            if (!file.exists())
                createFile(file)
            return file
        }

        fun getBookDir(bookId: String): File {
            return File(Constant.PATH_TXT + bookId)
        }

        fun getMerginBook(bookName: String): String {
            return Constant.PATH_TXT + bookName
        }

        fun createWifiTempFile(): File {
            val src = Constant.PATH_DATA + "/" + System.currentTimeMillis()
            val file = File(src)
            if (!file.exists())
                createFile(file)
            return file
        }

        fun getBookDirFiles(bookId: String): Array<File>? {
            val dir = getBookDir(bookId)
            if (dir.exists()) {
                val files = dir.listFiles()
                try {
                    Arrays.sort(files) { lhs, rhs ->
                        val lPage = Integer.parseInt(getFileNameNotType(lhs))
                        val rPage = Integer.parseInt(getFileNameNotType(rhs))
                        Integer.compare(lPage, rPage)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return files
            }
            return null
        }

        fun getFileNameNotType(file: File): String {
            val path = file.path
            val separatorIndex = path.lastIndexOf(File.separator)
            val lastTypeIndex = path.lastIndexOf(".")
            return if (separatorIndex < 0) path.substring(0, lastTypeIndex) else path.substring(separatorIndex + 1, lastTypeIndex)
        }


        /**
         * 获取Wifi传书保存文件
         *
         * @param fileName
         * @return
         */
        fun createWifiTranfesFile(fileName: String): File {
            LogUtils.i("wifi trans save $fileName")
            // 取文件名作为文件夹（bookid）
            val absPath = Constant.PATH_TXT + "/" + fileName + "/1.txt"

            val file = File(absPath)
            if (!file.exists())
                createFile(file)
            return file
        }

        fun getEpubFolderPath(epubFileName: String): String {
            return Constant.PATH_EPUB + "/" + epubFileName
        }

        fun getPathOPF(unzipDir: String): String? {
            var mPathOPF = ""
            try {
                val br = BufferedReader(InputStreamReader(FileInputStream("$unzipDir/META-INF/container.xml"), "UTF-8"))
                var line: String
                while ((line = br.readLine()) != null) {
                    if (line.contains("full-path")) {
                        val start = line.indexOf("full-path")
                        val start2 = line.indexOf('\"', start)
                        val stop2 = line.indexOf('\"', start2 + 1)
                        if (start2 > -1 && stop2 > start2) {
                            mPathOPF = line.substring(start2 + 1, stop2).trim { it <= ' ' }
                            break
                        }
                    }
                }
                br.close()

                if (!mPathOPF.contains("/")) {
                    return null
                }

                val last = mPathOPF.lastIndexOf('/')
                if (last > -1) {
                    mPathOPF = mPathOPF.substring(0, last)
                }

                return mPathOPF
            } catch (e: NullPointerException) {
                LogUtils.e(e.toString())
            } catch (e: IOException) {
                LogUtils.e(e.toString())
            }

            return mPathOPF
        }

        fun checkOPFInRootDirectory(unzipDir: String): Boolean {
            var mPathOPF = ""
            var status = false
            try {
                val br = BufferedReader(InputStreamReader(FileInputStream("$unzipDir/META-INF/container.xml"), "UTF-8"))
                var line: String
                while ((line = br.readLine()) != null) {
                    if (line.contains("full-path")) {
                        val start = line.indexOf("full-path")
                        val start2 = line.indexOf('\"', start)
                        val stop2 = line.indexOf('\"', start2 + 1)
                        if (start2 > -1 && stop2 > start2) {
                            mPathOPF = line.substring(start2 + 1, stop2).trim { it <= ' ' }
                            break
                        }
                    }
                }
                br.close()

                if (!mPathOPF.contains("/")) {
                    status = true
                } else {
                    status = false
                }
            } catch (e: NullPointerException) {
                LogUtils.e(e.toString())
            } catch (e: IOException) {
                LogUtils.e(e.toString())
            }

            return status
        }

        @Throws(IOException::class)
        fun unzipFile(inputZip: String, destinationDirectory: String) {

            val buffer = 2048
            val zipFiles = ArrayList<String>()
            val sourceZipFile = File(inputZip)
            val unzipDirectory = File(destinationDirectory)

            createDir(unzipDirectory.absolutePath)

            val zipFile: ZipFile
            zipFile = ZipFile(sourceZipFile, ZipFile.OPEN_READ)
            val zipFileEntries = zipFile.entries()

            while (zipFileEntries.hasMoreElements()) {

                val entry = zipFileEntries.nextElement() as ZipEntry
                val currentEntry = entry.name
                val destFile = File(unzipDirectory, currentEntry)

                if (currentEntry.endsWith(Constant.SUFFIX_ZIP)) {
                    zipFiles.add(destFile.absolutePath)
                }

                val destinationParent = destFile.parentFile
                createDir(destinationParent.absolutePath)

                if (!entry.isDirectory) {

                    if (destFile != null && destFile.exists()) {
                        LogUtils.i(destFile.toString() + "已存在")
                        continue
                    }

                    val `is` = BufferedInputStream(zipFile.getInputStream(entry))
                    var currentByte: Int
                    // buffer for writing file
                    val data = ByteArray(buffer)

                    val fos = FileOutputStream(destFile)
                    val dest = BufferedOutputStream(fos, buffer)

                    while ((currentByte = `is`.read(data, 0, buffer)) != -1) {
                        dest.write(data, 0, currentByte)
                    }
                    dest.flush()
                    dest.close()
                    `is`.close()
                }
            }
            zipFile.close()

            val iter = zipFiles.iterator()
            while (iter.hasNext()) {
                val zipName = iter.next()
                unzipFile(zipName, destinationDirectory + File.separatorChar
                        + zipName.substring(0, zipName.lastIndexOf(Constant.SUFFIX_ZIP)))
            }
        }

        /**
         * 读取Assets文件
         *
         * @param fileName
         * @return
         */
        fun readAssets(fileName: String?): ByteArray? {
            if (fileName == null || fileName.length <= 0) {
                return null
            }
            var buffer: ByteArray? = null
            try {
                val fin = AppUtils.appContext!!.getAssets().open("uploader$fileName")
                val length = fin.available()
                buffer = ByteArray(length)
                fin.read(buffer)
                fin.close()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                return buffer
            }
        }

        /**
         * 创建根缓存目录
         *
         * @return
         */
        fun createRootPath(context: Context): String {
            var cacheRootPath = ""
            if (isSdCardAvailable) {
                // /sdcard/Android/data/<application package>/cache
                cacheRootPath = context.externalCacheDir!!.path
            } else {
                // /data/data/<application package>/cache
                cacheRootPath = context.cacheDir.path
            }
            return cacheRootPath
        }

        val isSdCardAvailable: Boolean
            get() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

        /**
         * 递归创建文件夹
         *
         * @param dirPath
         * @return 创建失败返回""
         */
        fun createDir(dirPath: String): String {
            try {
                val file = File(dirPath)
                if (file.parentFile.exists()) {
                    LogUtils.i("----- 创建文件夹" + file.absolutePath)
                    file.mkdir()
                    return file.absolutePath
                } else {
                    createDir(file.parentFile.absolutePath)
                    LogUtils.i("----- 创建文件夹" + file.absolutePath)
                    file.mkdir()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return dirPath
        }

        /**
         * 递归创建文件夹
         *
         * @param file
         * @return 创建失败返回""
         */
        fun createFile(file: File): String {
            try {
                if (file.parentFile.exists()) {
                    LogUtils.i("----- 创建文件" + file.absolutePath)
                    file.createNewFile()
                    return file.absolutePath
                } else {
                    createDir(file.parentFile.absolutePath)
                    file.createNewFile()
                    LogUtils.i("----- 创建文件" + file.absolutePath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return ""
        }

        /**
         * 将内容写入文件
         *
         * @param filePath eg:/mnt/sdcard/demo.txt
         * @param content  内容
         * @param isAppend 是否追加
         */
        fun writeFile(filePath: String, content: String, isAppend: Boolean) {
            LogUtils.i("save:$filePath")
            try {
                val fout = FileOutputStream(filePath, isAppend)
                val bytes = content.toByteArray()
                fout.write(bytes)
                fout.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        fun writeFile(filePathAndName: String, fileContent: String) {
            try {
                val outstream = FileOutputStream(filePathAndName)
                val out = OutputStreamWriter(outstream)
                out.write(fileContent)
                out.close()
            } catch (e: java.io.IOException) {
                e.printStackTrace()
            }

        }

        /**
         * 获取Raw下的文件内容
         *
         * @param context
         * @param resId
         * @return 文件内容
         */
        fun getFileFromRaw(context: Context?, resId: Int): String? {
            if (context == null) {
                return null
            }

            val s = StringBuilder()
            try {
                val `in` = InputStreamReader(context.resources.openRawResource(resId))
                val br = BufferedReader(`in`)
                var line: String
                while ((line = br.readLine()) != null) {
                    s.append(line)
                }
                return s.toString()
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }

        }

        fun getBytesFromFile(f: File?): ByteArray? {
            if (f == null) {
                return null
            }
            try {
                val stream = FileInputStream(f)
                val out = ByteArrayOutputStream(1000)
                val b = ByteArray(1000)
                var n: Int
                while ((n = stream.read(b)) != -1) {
                    out.write(b, 0, n)
                }
                stream.close()
                out.close()
                return out.toByteArray()
            } catch (e: IOException) {
            }

            return null
        }

        /**
         * 文件拷贝
         *
         * @param src  源文件
         * @param desc 目的文件
         */
        fun fileChannelCopy(src: File, desc: File) {
            //createFile(src);
            createFile(desc)
            var fi: FileInputStream? = null
            var fo: FileOutputStream? = null
            try {
                fi = FileInputStream(src)
                fo = FileOutputStream(desc)
                val `in` = fi.channel//得到对应的文件通道
                val out = fo.channel//得到对应的文件通道
                `in`.transferTo(0, `in`.size(), out)//连接两个通道，并且从in通道读取，然后写入out通道
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    fo?.close()
                    fi?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }

        /**
         * 转换文件大小
         *
         * @param fileLen 单位B
         * @return
         */
        fun formatFileSizeToString(fileLen: Long): String {
            val df = DecimalFormat("0.00")
            var fileSizeString = ""
            if (fileLen < 1024) {
                fileSizeString = df.format(fileLen.toDouble()) + "B"
            } else if (fileLen < 1048576) {
                fileSizeString = df.format(fileLen.toDouble() / 1024) + "K"
            } else if (fileLen < 1073741824) {
                fileSizeString = df.format(fileLen.toDouble() / 1048576) + "M"
            } else {
                fileSizeString = df.format(fileLen.toDouble() / 1073741824) + "G"
            }
            return fileSizeString
        }

        /**
         * 删除指定文件
         *
         * @param file
         * @return
         * @throws IOException
         */
        @Throws(IOException::class)
        fun deleteFile(file: File): Boolean {
            return deleteFileOrDirectory(file)
        }

        /**
         * 删除指定文件，如果是文件夹，则递归删除
         *
         * @param file
         * @return
         * @throws IOException
         */
        @Throws(IOException::class)
        fun deleteFileOrDirectory(file: File?): Boolean {
            try {
                if (file != null && file.isFile) {
                    return file.delete()
                }
                if (file != null && file.isDirectory) {
                    val childFiles = file.listFiles()
                    // 删除空文件夹
                    if (childFiles == null || childFiles.size == 0) {
                        return file.delete()
                    }
                    // 递归删除文件夹下的子文件
                    for (i in childFiles.indices) {
                        deleteFileOrDirectory(childFiles[i])
                    }
                    return file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return false
        }

        /**
         * 获取文件夹大小
         *
         * @return
         * @throws Exception
         */
        @Throws(Exception::class)
        fun getFolderSize(dir: String): Long {
            val file = File(dir)
            var size: Long = 0
            try {
                val fileList = file.listFiles()
                for (i in fileList.indices) {
                    // 如果下面还有文件
                    if (fileList[i].isDirectory) {
                        size = size + getFolderSize(fileList[i].absolutePath)
                    } else {
                        size = size + fileList[i].length()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return size
        }

        /***
         * 获取文件扩展名
         *
         * @param filename 文件名
         * @return
         */
        fun getExtensionName(filename: String?): String? {
            if (filename != null && filename.length > 0) {
                val dot = filename.lastIndexOf('.')
                if (dot > -1 && dot < filename.length - 1) {
                    return filename.substring(dot + 1)
                }
            }
            return filename
        }

        /**
         * 获取文件内容
         *
         * @param path
         * @return
         */
        fun getFileOutputString(path: String, charset: String): String? {
            try {
                val file = File(path)
                val bufferedReader = BufferedReader(InputStreamReader(FileInputStream(file), charset), 8192)
                val sb = StringBuilder()
                var line: String? = null
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append("\n").append(line)
                }
                bufferedReader.close()
                return sb.toString()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }

        fun getCharset(fileName: String): String {
            var bis: BufferedInputStream? = null
            var charset = "GBK"
            val first3Bytes = ByteArray(3)
            try {
                var checked = false
                bis = BufferedInputStream(FileInputStream(fileName))
                bis.mark(0)
                var read = bis.read(first3Bytes, 0, 3)
                if (read == -1)
                    return charset
                if (first3Bytes[0] == 0xFF.toByte() && first3Bytes[1] == 0xFE.toByte()) {
                    charset = "UTF-16LE"
                    checked = true
                } else if (first3Bytes[0] == 0xFE.toByte() && first3Bytes[1] == 0xFF.toByte()) {
                    charset = "UTF-16BE"
                    checked = true
                } else if (first3Bytes[0] == 0xEF.toByte()
                        && first3Bytes[1] == 0xBB.toByte()
                        && first3Bytes[2] == 0xBF.toByte()) {
                    charset = "UTF-8"
                    checked = true
                }
                bis.mark(0)
                if (!checked) {
                    while ((read = bis.read()) != -1) {
                        if (read >= 0xF0)
                            break
                        if (0x80 <= read && read <= 0xBF)
                        // 单独出现BF以下的，也算是GBK
                            break
                        if (0xC0 <= read && read <= 0xDF) {
                            read = bis.read()
                            if (0x80 <= read && read <= 0xBF)
                            // 双字节 (0xC0 - 0xDF)
                            // (0x80 - 0xBF),也可能在GB编码内
                                continue
                            else
                                break
                        } else if (0xE0 <= read && read <= 0xEF) {// 也有可能出错，但是几率较小
                            read = bis.read()
                            if (0x80 <= read && read <= 0xBF) {
                                read = bis.read()
                                if (0x80 <= read && read <= 0xBF) {
                                    charset = "UTF-8"
                                    break
                                } else
                                    break
                            } else
                                break
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (bis != null) {
                    try {
                        bis.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }

            return charset
        }

        @Throws(IOException::class)
        fun getCharset1(fileName: String): String {
            val bin = BufferedInputStream(FileInputStream(fileName))
            val p = (bin.read() shl 8) + bin.read()

            val code: String
            when (p) {
                0xefbb -> code = "UTF-8"
                0xfffe -> code = "Unicode"
                0xfeff -> code = "UTF-16BE"
                else -> code = "GBK"
            }
            return code
        }

        fun saveWifiTxt(src: String, desc: String) {
            val LINE_END = "\n".toByteArray()
            try {
                val isr = InputStreamReader(FileInputStream(src), getCharset(src))
                val br = BufferedReader(isr)

                val fout = FileOutputStream(desc, true)
                var temp: String
                while ((temp = br.readLine()) != null) {
                    val bytes = temp.toByteArray()
                    fout.write(bytes)
                    fout.write(LINE_END)
                }
                br.close()
                fout.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
}