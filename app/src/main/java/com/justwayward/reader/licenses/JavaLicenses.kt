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
package com.justwayward.reader.licenses

import java.io.*

/**
 * 统一添加Licenses信息
 */
object JavaLicenses {

    internal var licenseStr = ""

    @JvmStatic
    fun main(args: Array<String>) {

        try {

            val license = File("app/license.txt")

            val read = InputStreamReader(FileInputStream(license), "utf-8")
            val bufferedReader = BufferedReader(read)
            var lineTxt = ""
            while ((lineTxt = bufferedReader.readLine()) != null) {
                licenseStr += lineTxt + "\n"
            }
            read.close()
        } catch (e: Exception) {
            println(e.toString())
        }

        val f = File("app/src/main/java/com/justwayward/reader")
        println(f.absolutePath)
        print(f)
    }

    fun print(f: File?) {

        if (f != null) {
            if (f.isDirectory) {
                val fileArray = f.listFiles()
                if (fileArray != null) {
                    for (i in fileArray.indices) {
                        val file = fileArray[i]

                        if (file.isDirectory) {
                            print(file)
                        } else {
                            addLicense(0, licenseStr, file)
                        }
                    }
                }
            } else {
                addLicense(0, licenseStr, f)
            }
        }
    }

    fun addLicense(skip: Long, str: String, file: File) {
        try {

            val read = InputStreamReader(FileInputStream(file), "utf-8")
            val bufferedReader = BufferedReader(read)
            val lineTxt = bufferedReader.readLine()
            read.close()
            if (lineTxt.startsWith("/**")) {
                return
            }

            val raf = RandomAccessFile(file, "rw")
            if (skip < 0 || skip > raf.length()) {
                println("skip error")
                return
            }
            val b = str.toByteArray()
            raf.setLength(raf.length() + b.size)
            for (i in raf.length() - 1 downTo b.size + skip - 1 + 1) {
                raf.seek(i - b.size)
                val temp = raf.readByte()
                raf.seek(i)
                raf.writeByte(temp.toInt())
            }
            raf.seek(skip)
            raf.write(b)
            raf.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}