package com.justwayward.reader.view.chmview

import com.justwayward.reader.utils.LogUtils

import java.io.ByteArrayInputStream
import java.io.Closeable
import java.io.EOFException
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.util.ArrayList
import java.util.Collections
import kotlin.collections.Map.Entry
import java.util.TreeMap

/**
 * CHM 文件结构
 *
 *
 * 1. Header
 * - 1.1 ITSF, 4字节
 * - 1.2 版本信息, 4字节
 * - 1.3 文件头总长度, 4字节
 * - 1.4 固定为1, 4字节
 * - 1.5 时间记录, 4字节
 * - 1.6 windows语言ID标识, 4字节
 * - 1.7 两个GUID, 16字节, 固定为{7C01FD10-7BAA-11D0-9E0C-00A0-C922-E6EC},{7C01FD11-7BAA-11D0-9E0C-00A0-C922-E6EC}
 * - 1.8 两项header section, 每项16字节, 记录着从文件头开始的偏移量和section的长度，各占8个字节
 * - - 1.8.1 header section 0
 * - - - 1.8.1.1 第一双字：0x01fe
 * - - - 1.8.1.2 第三双字为文件大小
 * - - - 1.8.1.3 共占5个双字，其余均为0
 * - - 1.8.2 header section 1
 * - - - 1.8.2.1 第一双字为ITSP
 * - - - 1.8.2.2 第二双字为版本号
 * - - - 1.8.2.3 第三双字为本section长度
 * - - - 1.8.2.4 第四双字为0x0a
 * - - - 1.8.2.5 第五双字值为0x1000，是目录块的大小
 * - - - 1.8.2.6 第六双字是quickref section的“密度”，一般是2
 * - - - 1.8.2.7 第七双字是索引树的深度，1表示没有索引，2表示有一层的PMGI数据块
 * - - - 1.8.2.8 第八双字表示根索引的块号，如果没有索引为-1
 * - - - 1.8.2.9 第九双字是第一个PMGL(listing)的块号
 * - - - 1.8.2.A 第十双字是最后一个PMGL的块号
 * - - - 1.8.2.B 第十一双字是-1
 * - - - 1.8.2.C 第十二双字是目录块的块数
 * - - - 1.8.2.D 第十三双字是windows语言ID标识
 * - 1.9 8个字节的信息，这些在版本2里是没有的
 *
 *
 * 2.
 */
class CHMFile @Throws(IOException::class, DataFormatException::class)
constructor(filepath: String) : Closeable {

    // header info
    private var version: Int = 0    // 3, 2
    private val timestamp: Int
    private val lang: Int    // Windows Language ID
    private val contentOffset: Long
    private var fileLength: Long = 0
    private val chunkSize: Int
    private val quickRef: Int
    private val rootIndexChunkNo: Int
    private val firstPMGLChunkNo: Int
    private val lastPMGLChunkNo: Int
    private val totalChunks: Int

    private val chunkOffset: Long

    internal var fileAccess: RandomAccessFile? = null

    private var entryCache: MutableMap<String, ListingEntry>? = TreeMap()

    // level 1 index, <filename, level 2 chunkNo>
    private val indexTree = ArrayList<Map<String, Int>>()

    private var resources: MutableList<String>? = null

    private var siteMap: String? = null

    private var sections: Array<Section>? = arrayOf(Section())

    private var filepath: String? = null

    init {
        var iTemp: Int
        fileAccess = RandomAccessFile(this.filepath = filepath, "r")

        /**
         * Step 1. CHM header
         */
        var `in` = LEInputStream(createInputStream(0, CHM_HEADER_LENGTH))

        // ITSF
        if (`in`.readUTF8(4) != "ITSF") {
            throw DataFormatException("CHM file should start with \"ITSF\"")
        }

        // version
        if ((version = `in`.read32()) > 3) {
            LogUtils.w("CHM header version unexpected value $version")
        }

        // header length
        val length = `in`.read32()

        // value
        iTemp = `in`.read32() // -1

        // timestamp
        timestamp = `in`.read32() // big-endian DWORD?
        LogUtils.i("CHM timestamp: $timestamp")

        // windows language id
        lang = `in`.read32()
        LogUtils.i("CHM ITSF language: " + WindowsLanguageID.getLocale(lang)!!)

        // GUID
        var strTmp = `in`.readGUID()    //.equals("7C01FD10-7BAA-11D0-9E0C-00A0-C922-E6EC");
        strTmp = `in`.readGUID()    //.equals("7C01FD11-7BAA-11D0-9E0C-00A0-C922-E6EC");

        // header section
        val off0 = `in`.read64()
        val len0 = `in`.read64()
        val off1 = `in`.read64()
        val len1 = `in`.read64()

        // if the header length is really 0x60, read the final QWORD, or the content should be immediate after header section 1
        contentOffset = if (length >= CHM_HEADER_LENGTH) `in`.read64() else off1 + len1
        LogUtils.i("CHM content offset $contentOffset")

        /* Step 1.1 (Optional)  CHM header section 0 */
        `in` = LEInputStream(createInputStream(off0, len0.toInt())) // len0 can't exceed 32-bit
        iTemp = `in`.read32() // 0x01FE;
        iTemp = `in`.read32() // 0;
        if ((fileLength = `in`.read64()) != fileAccess!!.length()) {
            LogUtils.w("CHM file may be corrupted, expect file length $fileLength")
        }
        iTemp = `in`.read32() // 0;
        iTemp = `in`.read32() // 0;

        /**
         * Step 1.2 CHM header section 1: directory index header
         */
        `in` = LEInputStream(createInputStream(off1, CHM_DIRECTORY_HEADER_LENGTH))

        if (`in`.readUTF8(4) != "ITSP") {
            throw DataFormatException("CHM directory header should start with \"ITSP\"")
        }

        iTemp = `in`.read32() // version
        chunkOffset = off1 + `in`.read32() // = 0x54
        iTemp = `in`.read32() // = 0x0a
        chunkSize = `in`.read32()    // 0x1000
        quickRef = 1 + (1 shl `in`.read32())    // = 1 + (1 << quickRefDensity )
        for (i in `in`.read32() downTo 2) { // depth of index tree, 1: no index, 2: one level of PMGI chunks
            indexTree.add(TreeMap())
        }

        rootIndexChunkNo = `in`.read32()    // chunk number of root, -1: none
        firstPMGLChunkNo = `in`.read32()    // chunk number of first PMGL
        lastPMGLChunkNo = `in`.read32()     // chunk number of last PMGL

        iTemp = `in`.read32() // = -1
        totalChunks = `in`.read32() // chunk counts
        val lang2 = `in`.read32() // language code
        LogUtils.i("CHM ITSP language " + WindowsLanguageID.getLocale(lang2)!!)

        strTmp = `in`.readGUID() //.equals("5D02926A-212E-11D0-9DF9-00A0-C922-E6EC"))
        iTemp = `in`.read32() // = x54
        iTemp = `in`.read32() // = -1
        iTemp = `in`.read32() // = -1
        iTemp = `in`.read32() // = -1

        if ((chunkSize * totalChunks + CHM_DIRECTORY_HEADER_LENGTH).toLong() != len1) {
            throw DataFormatException("CHM directory list chunks size mismatch")
        }

        /**
         * Step 2. CHM name list: content sections
         */
        `in` = LEInputStream(
                getResourceAsStream("::DataSpace/NameList"))

        iTemp = `in`.read16() // length in 16-bit-word, = in.length() / 2
        sections = arrayOfNulls(`in`.read16())
        for (i in sections!!.indices) {
            val name = `in`.readUTF16(`in`.read16() shl 1)
            if ("Uncompressed" == name) {
                sections[i] = Section()
            } else if ("MSCompressed" == name) {
                sections[i] = LZXCSection()
            } else {
                throw DataFormatException("Unknown content section $name")
            }
            iTemp = `in`.read16() // = null
        }
    }

    /**
     * Read len bytes from file beginning from offset. Since it's really a
     * ByteArrayInputStream, close() operation is optional
     */
    @Synchronized
    @Throws(IOException::class)
    private fun createInputStream(offset: Long, len: Int): InputStream {
        fileAccess!!.seek(offset)
        val b = ByteArray(len) // TODO performance?
        fileAccess!!.readFully(b)
        return ByteArrayInputStream(b)
    }

    /**
     * Resovle entry by name, using cache and index
     */
    @Throws(IOException::class)
    private fun resolveEntry(name: String): ListingEntry {
        //        if (rootIndexChunkNo < 0 && resources == null) // no index
        //        {
        list() // force cache fill
        //}
        val entry = entryCache!![name]
        if (entry != null) {
            return entry
        }

        //error
        //        if (rootIndexChunkNo >= 0 && resources == null) {
        //            entry = resolveIndexedEntry(name, rootIndexChunkNo, 0);
        //        }
        //
        //        if (entry == null) {// ugly
        //            entry = resolveIndexedEntry(name.toLowerCase(), rootIndexChunkNo, 0);
        //            LogUtils.w("Resolved using lowercase name " + name);
        //        }

        if (entry == null) {
            throw FileNotFoundException("$filepath#$name")
        }

        return entry
    }

    /**
     * listing chunks have filename/offset entries sorted by filename
     * alphabetically index chunks have filename/listingchunk# entries,
     * specifying the first filename of each listing chunk. NOTE: this code will
     * crack when there is no index at all (rootIndexChunkNo == -1), so at
     * processDirectoryIndex() method, we have already cached all resource
     * names. however, this code will still crack, when resolving a not-at-all
     * existing resource.
     */
    @Synchronized
    @Throws(IOException::class)
    private fun resolveIndexedEntry(name: String, chunkNo: Int, level: Int): ListingEntry? {
        var chunkNo = chunkNo

        if (chunkNo < 0) {
            throw IllegalArgumentException("chunkNo < 0")
        }

        if (level < indexTree.size) {    // no more than indexTreeDepth
            // process the index chunk
            val index = indexTree[level]

            if (index.isEmpty()) {    // load it from the file
                val `in` = LEInputStream(
                        createInputStream(chunkOffset + rootIndexChunkNo * chunkSize, chunkSize))
                if (`in`.readUTF8(4) != "PMGI") {
                    throw DataFormatException("Index Chunk magic mismatch, should be 'PMGI'")
                }
                val freeSpace = `in`.read32() // Length of free space and/or quickref area at end of directory chunk
                // directory index entries, sorted by filename (case insensitive)
                while (`in`.available() > freeSpace) {
                    index.put(`in`.readUTF8(`in`.readENC()), `in`.readENC())
                }
                LogUtils.i("Index L$level$indexTree")
            }

            chunkNo = -1
            var lastKey = ""
            for ((key, value) in index) {
                if (name.compareTo(key) < 0) {
                    if (level + 1 == indexTree.size // it's the last index
                            && entryCache!!.containsKey(lastKey))
                    // if the first entry is cached
                    {
                        return entryCache!![name] // it should be in the cache, too
                    }
                    break // we found its chunk, break anyway
                }
                lastKey = key
                chunkNo = value
            }
            return resolveIndexedEntry(name, chunkNo, level + 1)
        } else { // process the listing chunk, and cache entries in the whole chunk
            val `in` = LEInputStream(
                    createInputStream(chunkOffset + chunkNo * chunkSize, chunkSize))
            if (`in`.readUTF8(4) != "PMGL") {
                throw DataFormatException("Listing Chunk magic mismatch, should be 'PMGL'")
            }
            val freeSpace = `in`.read32() // Length of free space and/or quickref area at end of directory chunk
            `in`.read32() // = 0;
            `in`.read32() // previousChunk #
            `in`.read32() // nextChunk #
            while (`in`.available() > freeSpace) {
                val entry = ListingEntry(`in`)
                entryCache!![entry.name] = entry
            }
            /* The quickref area is written backwards from the end of the chunk. One quickref entry
             * exists for every n entries in the file,
			 * where n is calculated as 1 + (1 << quickref density). So for density = 2, n = 5.
				chunkSize-0002: WORD     Number of entries in the chunk
				chunkSize-0004: WORD     Offset of entry n from entry 0
				chunkSize-0008: WORD     Offset of entry 2n from entry 0
				chunkSize-000C: WORD     Offset of entry 3n from entry 0
					LogUtils.i("resources.size() = " + resources.size());
					if ( (in.available() & 1) >0 ) // align to word
						in.skip(1);
					while (in.available() > 0)
						LogUtils.i("chunk " + i + ": " + in.read16());
             */
            return entryCache!![name]
        }
    }

    /**
     * Get an InputStream object for the named resource in the CHM.
     */
    @Throws(IOException::class)
    fun getResourceAsStream(name: String?): InputStream? {
        var name = name
        name = name!!.toLowerCase()
        if (name == null || name.length == 0) {
            name = getSiteMap()
            if (name == null) return null
        }
        val entry = resolveEntry(name) ?: throw FileNotFoundException("$filepath#$name")
        val section = sections!![entry.section]
        return section.resolveInputStream(entry.offset, entry.length)
    }

    /**
     * Get the name of the resources in the CHM. Caches perform better when
     * iterate the CHM using order of this returned list.
     *
     * @see .resolveIndexedEntry
     * chunk will be read twice, one in resolveIndexEntry, one here, fix it!
     */
    @Synchronized
    @Throws(IOException::class)
    fun list(): List<String>? {
        if (resources == null) {
            // find resources in all listing chunks
            resources = ArrayList()
            for (i in firstPMGLChunkNo until totalChunks) {
                val `in` = LEInputStream(
                        createInputStream(chunkOffset + i * chunkSize, chunkSize))
                if (`in`.readUTF8(4) != "PMGL") {
                    continue
                    //throw new DataFormatException("Listing Chunk magic mismatch, should be 'PMGL'");
                }
                val freeSpace = `in`.read32() // Length of free space and/or quickref area at end of directory chunk
                `in`.read32() // = 0;
                `in`.read32() // previousChunk #
                `in`.read32() // nextChunk #
                while (`in`.available() > freeSpace) {
                    val entry = ListingEntry(`in`)
                    entryCache!![entry.name] = entry
                    if (entry.name[0] == '/') {
                        resources!!.add(entry.name)
                        if (entry.name.endsWith(".hhc")) { // .hhc entry is the navigation file
                            siteMap = entry.name
                            LogUtils.i("CHM sitemap " + siteMap!!)
                        }
                    }
                }
            }
            resources = Collections.unmodifiableList(resources) // protect the list, since the reference will be
        }

        return resources
    }

    /**
     * The sitemap file, usually the .hhc file.
     */
    @Throws(IOException::class)
    fun getSiteMap(): String? {
        if (resources == null) {
            list()
        }
        return siteMap
    }

    /**
     * After close, the object can not be used any more.
     */
    @Throws(IOException::class)
    override fun close() {
        entryCache = null
        sections = null
        resources = null
        if (fileAccess != null) {
            fileAccess!!.close()
            fileAccess = null
        }
    }

    @Throws(IOException::class)
    protected override fun finalize() {
        close()
    }

    internal open inner class Section {

        @Throws(IOException::class)
        open fun resolveInputStream(off: Long, len: Int): InputStream {
            return createInputStream(contentOffset + off, len)
        }
    }

    internal inner class LZXCSection @Throws(IOException::class, DataFormatException::class)
    constructor() : Section() {

        var compressedLength: Long = 0
        var uncompressedLength: Long = 0
        var blockSize: Int = 0
        var resetInterval: Int = 0
        var addressTable: LongArray
        var windowSize: Int = 0
        var sectionOffset: Long = 0

        var cachedBlocks: LRUCache<Int, Array<ByteArray>>

        init {
            // control data
            var `in` = LEInputStream(
                    getResourceAsStream("::DataSpace/Storage/MSCompressed/ControlData"))
            `in`.read32() // words following LZXC
            if (`in`.readUTF8(4) != "LZXC") {
                throw DataFormatException("Must be in LZX Compression")
            }

            `in`.read32() // <=2, version
            resetInterval = `in`.read32() // huffman reset interval for blocks
            windowSize = `in`.read32() * 0x8000    // usu. 0x10, windows size in 0x8000-byte blocks
            val cacheSize = `in`.read32()    // unknown, 0, 1, 2
            LogUtils.i("LZX cache size $cacheSize")
            cachedBlocks = LRUCache(1 + cacheSize shl 2)
            `in`.read32() // = 0

            // reset table
            `in` = LEInputStream(
                    getResourceAsStream("::DataSpace/Storage/MSCompressed/Transform/" + "{7FC28940-9D31-11D0-9B27-00A0C91E9C7C}/InstanceData/ResetTable"))
            val version = `in`.read32()
            if (version != 2) {
                LogUtils.w("LZXC version unknown $version")
            }
            addressTable = LongArray(`in`.read32())
            `in`.read32() // = 8; size of table entry
            `in`.read32() // = 0x28, header length
            uncompressedLength = `in`.read64()
            compressedLength = `in`.read64()
            blockSize = `in`.read64().toInt() // 0x8000, do not support blockSize larger than 32-bit integer
            for (i in addressTable.indices) {
                addressTable[i] = `in`.read64()
            }
            // init cache
            //			cachedBlocks = new byte[resetInterval][blockSize];
            //			cachedResetBlockNo = -1;

            val entry = entryCache!!["::DataSpace/Storage/MSCompressed/Content".toLowerCase()]
                    ?: throw DataFormatException("LZXC missing content")
            if (compressedLength != entry.length.toLong()) {
                throw DataFormatException("LZXC content corrupted")
            }
            sectionOffset = contentOffset + entry.offset
        }

        @Throws(IOException::class)
        override fun resolveInputStream(off: Long, len: Int): InputStream {
            // the input stream !
            return object : InputStream() {

                internal var startBlockNo = (off / blockSize).toInt()
                internal var startOffset = (off % blockSize).toInt()
                internal var endBlockNo = ((off + len) / blockSize).toInt()
                internal var endOffset = ((off + len) % blockSize).toInt()
                // actually start at reset intervals
                internal var blockNo = startBlockNo - startBlockNo % resetInterval

                internal var inflater: Inflater? = Inflater(windowSize)

                internal var buf: ByteArray? = null
                internal var pos: Int = 0
                internal var bytesLeft: Int = 0

                @Throws(IOException::class)
                override fun available(): Int {
                    return bytesLeft // not non-blocking available
                }

                @Throws(IOException::class)
                override fun close() {
                    inflater = null
                }

                /**
                 * Read the blockNo block, called when bytesLeft == 0
                 */
                @Throws(IOException::class)
                private fun readBlock() {
                    if (blockNo > endBlockNo) {
                        throw EOFException()
                    }

                    val cachedNo = blockNo / resetInterval
                    synchronized(cachedBlocks) {
                        var cache = cachedBlocks.get(cachedNo)
                        if (cache == null) {
                            if ((cache = cachedBlocks.prune()) == null)
                            // try reuse old caches
                            {
                                cache = Array(resetInterval) { ByteArray(blockSize) }
                            }
                            val resetBlockNo = blockNo - blockNo % resetInterval
                            var i = 0
                            while (i < cache!!.size && resetBlockNo + i < addressTable.size) {
                                val blockNo = resetBlockNo + i
                                val len = (if (blockNo + 1 < addressTable.size)
                                    addressTable[blockNo + 1] - addressTable[blockNo]
                                else
                                    compressedLength - addressTable[blockNo]).toInt()
                                LogUtils.i("readBlock " + blockNo + ": " + (sectionOffset + addressTable[blockNo]) + "+ " + len)
                                inflater!!.inflate(i == 0, // reset flag
                                        createInputStream(sectionOffset + addressTable[blockNo], len),
                                        cache[i]) // here is the heart
                                i++
                            }
                            cachedBlocks.put(cachedNo, cache)
                        }
                        if (buf == null)
                        // allocate the buffer
                        {
                            buf = ByteArray(blockSize)
                        }
                        System.arraycopy(cache[blockNo % cache.size], 0, buf, 0, buf!!.size)
                    }

                    // the start block has special pos value
                    pos = if (blockNo == startBlockNo) startOffset else 0
                    // the end block has special length
                    bytesLeft = if (blockNo < startBlockNo)
                        0
                    else
                        if (blockNo < endBlockNo) blockSize else endOffset
                    bytesLeft -= pos

                    blockNo++
                }

                @Throws(IOException::class, DataFormatException::class)
                override fun read(b: ByteArray, off: Int, len: Int): Int {

                    if (bytesLeft <= 0 && blockNo > endBlockNo) {
                        return -1    // no more data
                    }

                    while (bytesLeft <= 0) {
                        readBlock() // re-charge
                    }
                    val togo = Math.min(bytesLeft, len)
                    System.arraycopy(buf!!, pos, b, off, togo)
                    pos += togo
                    bytesLeft -= togo

                    return togo
                }

                @Throws(IOException::class)
                override fun read(): Int {
                    val b = ByteArray(1)
                    return if (read(b) == 1) b[0] and 0xff else -1
                }

                @Throws(IOException::class)
                override fun skip(n: Long): Long {
                    LogUtils.w("LZX skip happens: $pos+ $n")
                    pos += n.toInt()    // TODO n chould be negative, so do boundary checks!
                    return n
                }
            }
        }
    }

    internal inner class ListingEntry @Throws(IOException::class)
    constructor(`in`: LEInputStream) {

        var name: String
        var section: Int = 0
        var offset: Long = 0
        var length: Int = 0

        init {
            name = `in`.readUTF8(`in`.readENC()).toLowerCase()
            section = `in`.readENC()
            offset = `in`.readENC().toLong()
            length = `in`.readENC()
        }

        override fun toString(): String {
            return "$name @$section: $offset + $length"
        }
    }

    companion object {

        val CHM_HEADER_LENGTH = 0x60

        val CHM_DIRECTORY_HEADER_LENGTH = 0x54

        @Throws(Exception::class)
        @JvmStatic
        fun main(argv: Array<String>) {
            if (argv.size == 0) {
                System.err.println("usage: java " + CHMFile::class.java.name + " <chm file name> (file)*")
                System.exit(1)
            }

            val chm = CHMFile(argv[0])
            if (argv.size == 1) {
                for (file in chm.list()!!) {
                    println(file)
                }
            } else {
                val buf = ByteArray(1024)
                for (i in 1 until argv.size) {
                    val `in` = chm.getResourceAsStream(argv[i])
                    var c = 0
                    while ((c = `in`!!.read(buf)) >= 0) {
                        print(String(buf, 0, c))
                    }
                }
            }
            chm.close()
        }
    }
}