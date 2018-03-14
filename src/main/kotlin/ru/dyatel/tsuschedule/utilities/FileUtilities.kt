package ru.dyatel.tsuschedule.utilities

import java.io.File
import java.net.URL

private const val BUFFER_SIZE = 8192

fun URL.download(destination: File, timeout: Int, onProgressUpdate: (Int) -> Unit) {
    val connection = openConnection().apply {
        connectTimeout = timeout
        readTimeout = timeout
    }

    connection.getInputStream().buffered(BUFFER_SIZE).use { input ->
        destination.outputStream().buffered(BUFFER_SIZE).use { output ->
            val buffer = ByteArray(BUFFER_SIZE)

            val length = connection.contentLength
            var downloaded = 0

            do {
                val count = input.read(buffer).takeUnless { it == -1 } ?: break
                output.write(buffer, 0, count)

                if (length > 0) {
                    downloaded += count
                    onProgressUpdate(downloaded * 100 / length)
                }
            } while (true)
        }
    }
}
