@file:Suppress("unused")

package ru.dyatel.tsuschedule

import android.util.Log
import com.crashlytics.android.Crashlytics
import java.io.IOException
import java.lang.IllegalArgumentException
import java.net.SocketTimeoutException

open class ParsingException : RuntimeException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}

class EmptyResultException : ParsingException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}

open class BadGroupException : IllegalArgumentException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}

class BlankGroupIndexException : BadGroupException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}

class ShortGroupIndexException : BadGroupException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}

fun Exception.log() {
    if (BuildConfig.DEBUG)
        Log.e("ExceptionHandler", "Caught an exception:", this)
    else
        Crashlytics.logException(this)
}

fun Exception.handle(showMessage: (Int) -> Unit = {}) {
    val message = when (this) {
        is EmptyResultException -> R.string.failure_empty_result
        is ParsingException -> {
            log()
            R.string.failure_parsing_failed
        }
        is SocketTimeoutException -> R.string.failure_connection_timeout
        is IOException -> R.string.failure_unsuccessful_request
        else -> throw this
    }
    showMessage(message)
}
