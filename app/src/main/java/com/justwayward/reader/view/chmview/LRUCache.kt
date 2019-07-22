package com.justwayward.reader.view.chmview

import java.util.TreeMap

class LRUCache<K : Comparable<K>, V>(private val capacity: Int) {

    internal var cacheMap: MutableMap<K, Item> = TreeMap()

    init {
        if (capacity < 1)
            throw IllegalArgumentException("capacity must be positive integer")
    }

    @Synchronized
    operator fun get(key: K): V? {
        val item = cacheMap[key] ?: return null

        for (i in cacheMap.values) {
            i.hits--
        }

        item.hits += 2
        return item.value
    }

    @Synchronized
    fun prune(): V? {
        if (cacheMap.size >= capacity) {
            var kick: Item? = null
            for (item in cacheMap.values) {
                if (kick == null || kick.hits > item.hits) {
                    kick = item
                }
            }
            cacheMap.remove(kick!!.key)
            return kick.value
        }
        return null
    }

    @Synchronized
    fun put(key: K, `val`: V) {
        if (cacheMap.containsKey(key)) { // just refresh the value
            cacheMap[key] = Item(key, `val`)
            return
        }
        prune()
        cacheMap[key] = Item(key, `val`)
    }

    @Synchronized
    fun clear() {
        cacheMap.clear()
    }

    fun size(): Int {
        return cacheMap.size
    }

    override fun toString(): String {
        return "LRUCache " + size() + "/" + capacity + ": " + cacheMap.toString()
    }

    internal inner class Item(var key: K, var value: V) {
        var hits: Int = 0

        init {
            this.hits = 1
        }

        override fun toString(): String {
            return "($hits)"
        }
    }
}
