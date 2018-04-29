@file:Suppress("unused")

package ru.dyatel.tsuschedule

import android.util.Log
import com.crashlytics.android.Crashlytics
import org.jsoup.UncheckedIOException
import java.io.IOException
import java.lang.IllegalArgumentException
import java.net.SocketTimeoutException
import java.util.concurrent.CancellationException

open class ParsingException : RuntimeException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}

class EmptyResultException : RuntimeException {
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
    if (BuildConfig.ENABLE_CRASHLYTICS) {
        Crashlytics.logException(this)
    } else {
        Log.e("ExceptionHandler", "Caught an exception:", this)
    }
}

fun Exception.handle(showMessage: (Int) -> Unit = {}) {
    val exception = if (this is UncheckedIOException) ioException() else this

    val message = when (exception) {
        is EmptyResultException -> R.string.failure_empty_result
        is ParsingException -> {
            log()
            R.string.failure_parsing_failed
        }
        is SocketTimeoutException -> R.string.failure_connection_timeout
        is IOException -> R.string.failure_unsuccessful_request
        is CancellationException -> return
        else -> throw exception
    }
    showMessage(message)
}
