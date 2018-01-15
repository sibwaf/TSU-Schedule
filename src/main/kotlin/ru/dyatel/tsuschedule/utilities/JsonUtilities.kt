package ru.dyatel.tsuschedule.utilities

import org.json.JSONArray
import org.json.JSONObject

inline fun <reified T> JSONObject.find(name: String): T =
        get(name) as T? ?: throw NoSuchElementException()

operator fun JSONArray.iterator() = object : Iterator<Any> {

    private var current = 0

    override fun hasNext() = current < this@iterator.length()

    override fun next() = this@iterator[current++]

}
