package ru.dyatel.tsuschedule.updater

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import ru.dyatel.tsuschedule.BuildConfig
import ru.dyatel.tsuschedule.MIME_APK
import java.io.File
import java.io.FileNotFoundException

class UpdateFileProvider : ContentProvider() {

    companion object {

        fun getUpdateDirectory(context: Context) = context.cacheDir!!

        fun getUriForFile(context: Context, file: File): Uri {
            val relative = file.relativeToOrNull(getUpdateDirectory(context))
                    ?: throw IllegalArgumentException("File is not contained in update directory")
            if (relative.extension != "apk") throw IllegalArgumentException("File is not an .apk")

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return Uri.fromFile(file)

            return Uri.Builder()
                    .scheme("content")
                    .authority(BuildConfig.APPLICATION_ID)
                    .appendPath(relative.path)
                    .build()
        }

    }

    override fun onCreate() = true

    override fun getType(uri: Uri) = MIME_APK

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != "r")
            throw FileNotFoundException("Can't open $uri in <$mode> mode: file is read-only")

        val path = uri.path.removePrefix("/")
        val file = getUpdateDirectory(context).resolve(path)
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun insert(uri: Uri?, values: ContentValues?): Uri {
        throw UnsupportedOperationException()
    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        throw UnsupportedOperationException()
    }

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException()
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException()
    }

}
