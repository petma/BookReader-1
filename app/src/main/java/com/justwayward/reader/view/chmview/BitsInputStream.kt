package com.justwayward.reader.view.chmview

import java.io.EOFException
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

class BitsInputStream(`in`: InputStream) : FilterInputStream(`in`) {

    internal var bitbuf: Int = 0
    internal var bitsLeft: Int = 0

    /**
     * Read 32-bit little endian int instead of a byte!
     */
    @Throws(IOException::class)
    fun read32LE(): Int {
        return `in`.read() + (`in`.read() shl 8) + (`in`.read() shl 16) + (`in`.read() shl 24)
    }

    /**
     * flush n bytes, and reset bitbuf, bitsLeft
     * often used to align the byte array
     * NOTE: n may be negative integer, e.g. -2
     */
    @Throws(IOException::class)
    fun skip(n: Int) {
        bitbuf = 0
        bitsLeft = 0
        super.skip(n.toLong())
    }

    /**
     * Make sure there are at least n (<=16) bits in the buffer,
     * otherwise, read a 16-bit little-endian word from the byte array.
     * returns bitsLeft;
     */
    @Throws(IOException::class)
    fun ensure(n: Int): Int {
        while (bitsLeft < n) {
            // read in two bytes
            val b1 = `in`.read()
            val b2 = `in`.read()
            if (b1 or b2 < 0)
                break

            bitbuf = bitbuf or (b1 or (b2 shl 8) shl BUFFER_BITS - 16 - bitsLeft)
            bitsLeft += 16
        }
        return bitsLeft
    }

    /**
     * Read no more than 16 bits big endian, bits are arranged as
     * <pre>
     * 00000000 00000000 00000000 00000000, bitsLeft = 0;
     * ensure(1);
     * aaaaaaaa 00000000 00000000 00000000, bitsLeft = 8;
     * read(3) = 00000aaa;
     * aaaaa000 00000000 00000000 00000000, bitsLeft = 5;
     * ensure(16);
     * aaaaabbb bbbbbccc ccccc000 00000000, bitsLeft = 21;
     * read(8) = aaaaabbb;
     * bbbbbccc ccccc000 00000000 00000000, bitsLeft = 13;
    </pre> *
     */
    @Throws(IOException::class)
    fun readLE(n: Int): Int {
        val ret = peek(n)
        bitbuf = bitbuf shl n
        bitsLeft -= n
        return ret
    }

    /**
     * Peek n bits, may raise EOFException.
     */
    @Throws(IOException::class)
    fun peek(n: Int): Int {
        if (ensure(n) < n)
            throw EOFException()
        return bitbuf shr BUFFER_BITS - n and UNSIGNED_MASK[n]
    }

    /**
     * Peek no more than n bits, so there is no EOFException.
     */
    @Throws(IOException::class)
    fun peekUnder(n: Int): Int {
        ensure(n)
        return bitbuf shr BUFFER_BITS - n and UNSIGNED_MASK[n]
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

    /**
     * return binary string of bitbuf
     */
    override fun toString(): String {
        var s = "00000000000000000000000000000000" + java.lang.Long.toBinaryString(bitbuf.toLong())
        s = s.substring(s.length - 32)
        return (s.substring(0, 8) + " " + s.substring(8, 16)
                + " " + s.substring(16, 24) + " " + s.substring(24, 32)
                + " " + bitsLeft)
    }

    companion object {

        internal val BUFFER_BITS = 32

        internal val UNSIGNED_MASK = intArrayOf(0, 0x01, 0x03, 0x07, 0x0f, 0x01f, 0x3f, 0x7f, 0xff, 0x1ff, 0x3ff, 0x7ff, 0xfff, 0x1fff, 0x3fff, 0x7fff, 0xffff)
    }
}