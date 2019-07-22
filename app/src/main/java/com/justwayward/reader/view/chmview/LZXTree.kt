package com.justwayward.reader.view.chmview

import java.io.IOException

class LZXTree internal constructor(internal var bits: Int, internal var max_symbol: Int) {

    internal var symbols: IntArray
    internal var lens: ByteArray

    init {

        symbols = IntArray((1 shl bits) + (max_symbol shl 1))
        lens = ByteArray(max_symbol + LZX_LENTABLE_SAFETY)
    }

    /**
     * This function was coded by David Tritscher. It builds a fast huffman
     * decoding table out of just a canonical huffman code lengths table.
     */
    @Throws(DataFormatException::class)
    internal fun makeSymbolTable() {
        var bit_num = 1
        var pos = 0 // the current position in the decode table
        var table_mask = 1 shl bits
        var bit_mask = table_mask shr 1 // don't do 0 length codes
        var next_symbol = bit_mask // base of allocation for long codes

        // fill entries for codes short enough for a direct mapping
        while (bit_num <= bits) {
            for (symbol in 0 until max_symbol) {
                if (lens[symbol].toInt() == bit_num) {
                    var leaf = pos

                    if ((pos += bit_mask) > table_mask)
                    // ensure capacity
                        throw DataFormatException("symbol table overruns")
                    // fill all possible lookups of this symbol with the symbol itself
                    while (leaf < pos) symbols[leaf++] = symbol
                }
            }
            bit_mask = bit_mask shr 1
            bit_num++
        }

        // if there are any codes longer than table.bits
        if (pos != table_mask) {
            // clear the remainder of the table
            for (i in pos until table_mask) symbols[i] = 0

            // give ourselves room for codes to grow by up to 16 more bits
            pos = pos shl 16
            table_mask = table_mask shl 16
            bit_mask = 1 shl 15

            while (bit_num <= 16) {
                for (symbol in 0 until max_symbol) {
                    if (lens[symbol].toInt() == bit_num) {
                        var leaf = pos shr 16
                        for (fill in 0 until bit_num - bits) {
                            // if this path hasn't been taken yet, 'allocate' two entries
                            if (symbols[leaf] == 0) {
                                symbols[next_symbol shl 1] = 0
                                symbols[(next_symbol shl 1) + 1] = 0
                                symbols[leaf] = next_symbol++
                            }
                            // follow the path and select either left or right for next bit
                            leaf = symbols[leaf] shl 1
                            if (pos shr 15 - fill and 1 > 0)
                            // odd
                                leaf++
                        }
                        symbols[leaf] = symbol

                        if ((pos += bit_mask) > table_mask)
                            throw DataFormatException("symbol table overflow")
                    }
                }
                bit_mask = bit_mask shr 1
                bit_num++
            }
        }

        // full table?
        if (pos == table_mask)
            return

        // either erroneous table, or all elements are 0 - let's find out.
        for (sym in 0 until max_symbol)
            if (lens[sym].toInt() != 0)
                throw DataFormatException("erroneous symbol table")
    }

    /**
     * reads in code lengths for symbols
     * first to last in the given table. The code lengths are stored in their
     * own special LZX way.
     */
    @Throws(DataFormatException::class, IOException::class)
    internal fun readLengthTable(bin: BitsInputStream, first: Int, last: Int) {
        val preTree = LZXTree(6, LZX_PRETREE_NUM_ELEMENTS)
        for (i in 0 until preTree.max_symbol)
            preTree.lens[i] = bin.readLE(4).toByte()
        preTree.makeSymbolTable()

        var pos = first
        while (pos < last) {
            var symbol = preTree.readHuffmanSymbol(bin)
            if (symbol == 0x11) {
                val pos2 = pos + bin.readLE(4) + 4
                while (pos < pos2) lens[pos++] = 0.toByte()
            } else if (symbol == 0x12) {
                val pos2 = pos + bin.readLE(5) + 20
                while (pos < pos2) lens[pos++] = 0.toByte()
            } else if (symbol == 0x13) {
                val pos2 = pos + bin.readLE(1) + 4
                symbol = lens[pos] - preTree.readHuffmanSymbol(bin)
                if (symbol < 0)
                    symbol += 0x11
                while (pos < pos2) lens[pos++] = symbol.toByte()
            } else {
                symbol = lens[pos] - symbol
                if (symbol < 0)
                    symbol += 0x11
                lens[pos++] = symbol.toByte()
            }
        }
    }

    /**
     * decodes one huffman symbol from the bitstream using the
     * stated table and return it.
     * @throws IOException
     */
    @Throws(IOException::class)
    internal fun readHuffmanSymbol(bin: BitsInputStream): Int {
        val next = bin.peekUnder(16)

        /* TODO: it's very strange that bin.peek(bits) will raise EOFException,
		 * we have to use peekUnder(bits) here, but how should it happen like this?
		 */
        var symbol = symbols[bin.peekUnder(bits)]
        if (symbol >= max_symbol) {
            var j = 1 shl 16 - bits
            do {
                j = j shr 1
                symbol = symbol shl 1
                symbol = symbol or if (next and j > 0) 1 else 0
                symbol = symbols[symbol]
            } while (symbol >= max_symbol)
        }
        bin.readLE(lens[symbol].toInt())
        return symbol
    }

    fun clear() {
        for (i in lens.indices)
            lens[i] = 0
    }

    companion object {

        val LZX_LENTABLE_SAFETY = 64 // allow length table decoding overruns
        val LZX_PRETREE_NUM_ELEMENTS = 20
    }
}