package ru.dyatel.tsuschedule

import android.app.AlertDialog
import java.io.File
import java.net.URL

fun URL.download(destination: File, timeout: Int, onProgressUpdate: (Int) -> Unit) {
    val connection = openConnection().apply {
        connectTimeout = timeout
        readTimeout = timeout
    }

    val length = connection.contentLength
    var downloaded = 0
    connection.getInputStream().buffered().use { input ->
        destination.outputStream().buffered().use { output ->
            val buffer = ByteArray(8192)
            var count: Int
            do {
                count = input.read(buffer)
                if (count == -1) break

                output.write(buffer, 0, count)

                downloaded += count
                onProgressUpdate(downloaded * 100 / length)
            } while (true)
        }
    }
}

fun AlertDialog.setMessage(id: Int) = setMessage(context.getString(id))
