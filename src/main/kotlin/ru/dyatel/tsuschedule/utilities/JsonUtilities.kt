package ru.dyatel.tsuschedule.utilities

import org.json.JSONObject

inline fun <reified T> JSONObject.find(name: String): T {
    val result = get(name) ?: throw NoSuchElementException()
    return result as T
}
