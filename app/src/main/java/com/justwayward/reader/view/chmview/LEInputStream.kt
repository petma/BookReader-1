package com.justwayward.reader.view.chmview

import java.io.EOFException
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

class LEInputStream(`in`: InputStream) : FilterInputStream(`in`) {

    /**
     * 16-bit little endian unsinged integer
     */
    @Throws(IOException::class)
    fun read16(): Int {
        val b1 = read()
        val b2 = read()
        if (b1 or b2 < 0)
            throw EOFException()
        return (b1 shl 0) + (b2 shl 8)
    }

    /**
     * 32-bit little endian integer
     */
    @Throws(IOException::class)
    fun read32(): Int {
        val b1 = read()
        val b2 = read()
        val b3 = read()
        val b4 = read()
        if (b1 or b2 or b3 or b4 < 0)
            throw EOFException()
        return (b1 shl 0) + (b2 shl 8) + (b3 shl 16) + (b4 shl 24)
    }

    /**
     * Encoded little endian integer
     */
    @Throws(IOException::class)
    fun readENC(): Int {
        var r = 0
        while (true) {
            val b = read()
            if (b < 0) throw EOFException()
            r = (r shl 7) + (b and 0x7f)
            if (b and 0x80 == 0)
                return r
        }
    }

    /**
     * 64-bit little endian integer
     */
    @Throws(IOException::class)
    fun read64(): Long {
        val b1 = read()
        val b2 = read()
        val b3 = read()
        val b4 = read()
        val b5 = read()
        val b6 = read()
        val b7 = read()
        val b8 = read()
        if (b1 or b2 or b3 or b4 or b5 or b6 or b7 or b8 < 0)
            throw EOFException()
        return ((b1 shl 0) + (b2 shl 8) + (b3 shl 16) + (b4 shl 24)
                + (b5 shl 32) + (b6 shl 40) + (b7 shl 48) + (b8 shl 56)).toLong()
    }

    @Throws(IOException::class)
    fun readUTF8(len: Int): String {
        val buf = ByteArray(len)
        readFully(buf)
        return String(buf, 0, len, "UTF-8")
    }

    @Throws(IOException::class)
    fun readUTF16(len: Int): String {
        val buf = ByteArray(len)
        readFully(buf)
        return String(buf, 0, len, "UTF-16LE")
    }

    @Throws(IOException::class)
    fun readGUID(): String {
        return toHexString(read32(), 8) +
                "-" + toHexString(read16(), 4) + "-" + toHexString(read16(), 4) +
                "-" + toHexString(read(), 2) + toHexString(read(), 2) +
                "-" + toHexString(read(), 2) + toHexString(read(), 2) +
                "-" + toHexString(read(), 2) + toHexString(read(), 2) +
                "-" + toHexString(read(), 2) + toHexString(read(), 2)
    }

    private fun toHexString(v: Int, len: Int): String {
        val b = StringBuffer()
        for (i in 0 until len)
            b.append('0')
        b.append(java.lang.Long.toHexString(v.toLong()))
        return b.substring(b.length - len).toUpperCase()
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun readFully(b: ByteArray, off: Int = 0, len: Int = b.size) {
        var n = 0
        while (n < len) {
            val count = read(b, off + n, len - n)
            if (count < 0)
                throw EOFException()
            n += count
        }
    }
}
