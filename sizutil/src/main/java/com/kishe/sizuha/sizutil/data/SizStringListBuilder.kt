package com.kishe.sizuha.sizutil.data

import java.util.*

class StringListBuilder {

    var items: MutableList<String>

    constructor() {
        items = LinkedList()
    }

    constructor(capacity: Int) {
        items = ArrayList(capacity)
    }

    fun append(v: String): StringListBuilder {
        items.add(v)
        return this
    }

    fun append(v: Int): StringListBuilder {
        items.add(v.toString())
        return this
    }

    fun append(v: Long): StringListBuilder {
        items.add(v.toString())
        return this
    }

    fun append(v: Float): StringListBuilder {
        items.add(v.toString())
        return this
    }

    fun append(v: Double): StringListBuilder {
        items.add(v.toString())
        return this
    }

    fun append(v: Boolean): StringListBuilder {
        items.add(if (v) "1" else "0")
        return this
    }

    fun toArray(): Array<String> {
        return items.toTypedArray()
    }

    override fun toString(): String {
        return  stringListToString(items)
    }

}