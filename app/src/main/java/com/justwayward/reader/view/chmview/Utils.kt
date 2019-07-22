package com.justwayward.reader.view.chmview

import android.util.Log

import org.ccil.cowan.tagsoup.jaxp.SAXParserImpl
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap

object Utils {
    var chm: CHMFile? = null

    @Throws(IOException::class)
    fun domparse(filePath: String, extractPath: String, md5: String): ArrayList<String> {
        val listSite = ArrayList<String>()
        listSite.add(md5)
        //        Document doc = Jsoup.parse(chm.getResourceAsStream(""), "UTF-8", "");
        //        Elements listObject = doc.getElementsByTag("object");
        //        for (Element object : listObject) {
        //            Elements listParam = object.getElementsByTag("param");
        //            if (listParam.size() > 0) {
        //                String name = "", local = "";
        //                for (Element param : listParam) {
        //                    if (param.attributes().getIgnoreCase("name").equalsIgnoreCase("name")) {
        //                        name = param.attributes().getIgnoreCase("value");
        //                    } else if (param.attributes().getIgnoreCase("name").equalsIgnoreCase("local")) {
        //                        local = param.attributes().getIgnoreCase("value");
        //                    }
        //                }
        //                listSite.add(local);
        //                object.parent().prepend("<a href=\"" + local + "\">" + name + "</a>");
        //                object.remove();
        //            }
        //        }
        //        try {
        //            FileOutputStream fosHTMLMap =  new FileOutputStream(extractPath + "/" +md5);
        //            fosHTMLMap.write(doc.outerHtml().getBytes());
        //            fosHTMLMap.close();
        //
        //            FileOutputStream fosListSite =  new FileOutputStream(extractPath + "/site_map_" +md5);
        //            for(String str: listSite) {
        //                fosListSite.write((str+";").getBytes());
        //            }
        //            fosListSite.close();
        //            Log.e("Utils", "write ok " + "/site_map_" +md5);
        //        } catch (IOException e) {
        //            e.printStackTrace();
        //            Log.e("Utils", "write ok sitemap error");
        //        }
        ///////////////////////////////////////////////////
        try {

            val fosHTMLMap = FileOutputStream("$extractPath/$md5")
            val fosListSite = FileOutputStream("$extractPath/site_map_$md5")
            try {
                fosListSite.write("$md5;".toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (chm!!.getResourceAsStream("") != null) {
                SAXParserImpl.newInstance(null).parse(
                        chm!!.getResourceAsStream(""),
                        object : DefaultHandler() {

                            internal var url = MyUrl()
                            internal var myMap = HashMap<String, String>()
                            internal var count = 0

                            internal inner class MyUrl {
                                var status = 0
                                var name: String? = null
                                var local: String? = null

                                override fun toString(): String {
                                    return if (status == 1)
                                        "<a href=\"#\">$name</a>"
                                    else
                                        "<a href=\"$local\">$name</a>"
                                }
                            }

                            @Throws(SAXException::class)
                            override fun startElement(uri: String, localName: String, qName: String,
                                                      attributes: Attributes) {

                                if (qName == "param") {
                                    count++
                                    for (i in 0 until attributes.length) {
                                        myMap[attributes.getQName(i).toLowerCase()] = attributes.getValue(i).toLowerCase()
                                    }
                                    if (myMap["name"] == "name" && myMap["value"] != null) {
                                        url.name = myMap["value"]
                                        url.status = 1
                                    } else if (myMap["name"] == "local" && myMap["value"] != null) {
                                        url.local = myMap["value"]
                                        url.status = 2
                                        listSite.add(url.local!!.replace("%20".toRegex(), " "))
                                        try {
                                            fosListSite.write((url.local!!.replace("%20".toRegex(), " ") + ";").toByteArray())
                                        } catch (e: IOException) {
                                            e.printStackTrace()
                                        }

                                    }

                                    if (url.status == 2) {
                                        url.status = 0
                                        try {
                                            fosHTMLMap.write(url.toString().toByteArray())
                                        } catch (e: IOException) {
                                            e.printStackTrace()
                                        }

                                    }
                                } else {
                                    if (url.status == 1) {
                                        try {
                                            fosHTMLMap.write(url.toString().toByteArray())
                                            url.status = 0
                                        } catch (e: IOException) {
                                            e.printStackTrace()
                                        }

                                    }
                                }

                                if (qName != "object" && qName != "param")
                                    try {
                                        fosHTMLMap.write("<$qName>".toByteArray())
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }


                            }

                            @Throws(SAXException::class)
                            override fun endElement(uri: String, localName: String,
                                                    qName: String) {
                                if (qName != "object" && qName != "param")
                                    try {
                                        fosHTMLMap.write("</$qName>".toByteArray())
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }

                            }
                        }
                )
            } else {
                fosHTMLMap.write("<HTML> <BODY> <UL>".toByteArray())
                for (fileName in chm!!.list()!!) {
                    fileName = fileName.substring(1)
                    if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
                        fosListSite.write("$fileName;".toByteArray())
                        fosHTMLMap.write("<li><a href=\"$fileName\">$fileName</a></li>".toByteArray())
                        listSite.add(fileName)
                    }
                }
                fosHTMLMap.write("</UL> </BODY> </HTML>".toByteArray())
            }
            fosHTMLMap.close()
            fosListSite.close()
        } catch (e: SAXException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        ///////////////////////////////////////////////////


        return listSite
    }

    fun getListSite(extractPath: String, md5: String): ArrayList<String>? {
        val listSite = ArrayList<String>()

        val reval = StringBuilder()
        try {
            val `in` = FileInputStream("$extractPath/site_map_$md5")
            val buf = ByteArray(1024)
            var c = 0
            while ((c = `in`.read(buf)) >= 0) {
                reval.append(String(buf, 0, c))
            }
            `in`.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }

        val arrSite = reval.toString().split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        Collections.addAll(listSite, *arrSite)
        return listSite
    }

    fun getBookmark(extractPath: String, md5: String): ArrayList<String> {
        val listBookMark = ArrayList<String>()
        val reval = StringBuilder()
        try {
            val `in` = FileInputStream("$extractPath/bookmark_$md5")
            val buf = ByteArray(1024)
            var c = 0
            while ((c = `in`.read(buf)) >= 0) {
                reval.append(String(buf, 0, c))
            }
            `in`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val arrSite = reval.toString().split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (str in arrSite) {
            if (str.length > 0) {
                listBookMark.add(str)
            }
        }
        return listBookMark
    }

    fun getHistory(extractPath: String, md5: String): Int {
        val reval = StringBuilder()
        try {
            val `in` = FileInputStream("$extractPath/history_$md5")
            val buf = ByteArray(1024)
            var c = 0
            while ((c = `in`.read(buf)) >= 0) {
                reval.append(String(buf, 0, c))
            }
            `in`.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return 1
        }

        try {
            return Integer.parseInt(reval.toString())
        } catch (e: Exception) {
            return 0
        }

    }


    fun saveBookmark(extractPath: String, md5: String, listBookmark: ArrayList<String>) {
        try {
            val fos = FileOutputStream("$extractPath/bookmark_$md5", false)
            for (str in listBookmark) {
                fos.write("$str;".toByteArray())
            }
            fos.close()
        } catch (ignored: IOException) {
        }

    }

    fun saveHistory(extractPath: String, md5: String, index: Int) {
        try {
            val fos = FileOutputStream("$extractPath/history_$md5", false)
            fos.write(("" + index).toByteArray())
            fos.close()
        } catch (ignored: IOException) {
        }

    }


    private fun getSiteMap(filePath: String): String {
        val reval = StringBuilder()
        try {
            if (chm == null) {
                chm = CHMFile(filePath)
            }
            val buf = ByteArray(1024)
            val `in` = chm!!.getResourceAsStream("")
            var c = 0
            while ((c = `in`!!.read(buf)) >= 0) {
                reval.append(String(buf, 0, c))
            }
            //            chm.close();
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }

        return reval.toString()
    }

    fun extract(filePath: String, pathExtract: String): Boolean {
        try {
            if (chm == null) {
                chm = CHMFile(filePath)
            }
            val filePathTemp = File(pathExtract)
            if (!filePathTemp.exists()) {
                if (!filePathTemp.mkdirs()) throw IOException()
            }
            //            for (String file : chm.list()) {
            //                String temp = pathExtract + file;
            //                String tempName = temp.substring(temp.lastIndexOf("/") + 1);
            //                String tempPath = temp.substring(0, temp.lastIndexOf("/"));
            //                File filePathTemp = new File(tempPath);
            //                if (!filePathTemp.exists()) {
            //                    if (!filePathTemp.mkdirs()) throw new IOException();
            //                }
            //                if (tempName.length() > 0) {
            //                    FileOutputStream fos = null;
            //                    try {
            //                        fos = new FileOutputStream(temp);
            //                        byte[] buf = new byte[1024];
            //                        InputStream in = chm.getResourceAsStream(file);
            //                        int c;
            //                        while ((c = in.read(buf)) >= 0) {
            //                            fos.write(buf, 0, c);
            //                        }
            //                    } catch (IOException e) {
            //                        Log.d("Error extract file: ", file);
            //                        e.printStackTrace();
            //                    } finally {
            //                        if (fos != null) fos.close();
            //                    }
            //                }
            //            }
            //            chm.close();
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

        return true
    }

    fun checkSum(path: String): String {
        var checksum: String? = null
        try {
            val fis = FileInputStream(path)
            val md = MessageDigest.getInstance("MD5")

            //Using MessageDigest update() method to provide input
            val buffer = ByteArray(8192)
            var numOfBytesRead: Int
            while ((numOfBytesRead = fis.read(buffer)) > 0) {
                md.update(buffer, 0, numOfBytesRead)
            }
            val hash = md.digest()
            checksum = BigInteger(1, hash).toString(16) //don't use this, truncates leading zero
        } catch (ignored: IOException) {
        } catch (ignored: NoSuchAlgorithmException) {
        }

        assert(checksum != null)
        return checksum!!.trim { it <= ' ' }
    }

    fun extractSpecificFile(filePath: String, pathExtractFile: String, insideFileName: String): Boolean {
        try {
            if (chm == null) {
                chm = CHMFile(filePath)
            }
            if (File(pathExtractFile).exists()) return true
            val path = pathExtractFile.substring(0, pathExtractFile.lastIndexOf("/"))
            val filePathTemp = File(path)
            if (!filePathTemp.exists()) {
                if (!filePathTemp.mkdirs()) throw IOException()
            }
            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(pathExtractFile)
                val buf = ByteArray(1024)
                val `in` = chm!!.getResourceAsStream(insideFileName)
                var c: Int
                while ((c = `in`!!.read(buf)) >= 0) {
                    fos.write(buf, 0, c)
                }
            } catch (e: IOException) {
                Log.d("Error extract file: ", insideFileName)
                e.printStackTrace()
            } finally {
                fos?.close()
            }

        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

        return true
    }
}
