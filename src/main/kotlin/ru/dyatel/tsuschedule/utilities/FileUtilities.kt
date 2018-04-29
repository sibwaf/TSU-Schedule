package ru.dyatel.tsuschedule.utilities

import android.content.Context
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import ru.dyatel.tsuschedule.BuildConfig
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

fun File.getContentUri(context: Context): Uri {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        return Uri.fromFile(this)
    }

    return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, this)
}
