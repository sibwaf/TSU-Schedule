package ru.dyatel.tsuschedule

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.File
import java.io.FileNotFoundException
import java.net.HttpURLConnection

private const val REPOSITORY = "dya-tel/TSU-Schedule"
private const val URL = "https://api.github.com/repos/$REPOSITORY/releases/latest"

private const val MIME_APK = "application/vnd.android.package-archive"

data class Release(val version: String, val url: String) {

    private companion object {
        val VERSION_PATTERN = Regex("^((?:\\d+)(?:\\.\\d+)*)(.*)$")
    }

    fun isNewerThanInstalled(): Boolean {
        val match = VERSION_PATTERN.matchEntire(BuildConfig.VERSION_NAME)
                ?: throw RuntimeException("Current version name is malformed")

        val number = match.groupValues[1]
        val isPrerelease = match.groupValues[2].isNotEmpty()

        val currentVersionComponents = number.split(".")
        val releaseVersionComponents = version.split(".")

        val componentCount = minOf(currentVersionComponents.size, releaseVersionComponents.size)

        for (i in 0 until componentCount) {
            if (releaseVersionComponents[i] > currentVersionComponents[i]) return true
            if (releaseVersionComponents[i] < currentVersionComponents[i]) return false
        }

        if (currentVersionComponents.size > componentCount) {
            val sum = currentVersionComponents
                    .filterIndexed { index, _ -> index >= componentCount }
                    .sumBy { it.toInt() }
            if (sum > 0) return false
        } else if (releaseVersionComponents.size > componentCount) {
            val sum = releaseVersionComponents
                    .filterIndexed { index, _ -> index >= componentCount }
                    .sumBy { it.toInt() }
            if (sum > 0) return true
        }

        return isPrerelease
    }
}

class Updater {

    private companion object {
        val VERSION_PATTERN = Regex("^v((?:\\d+)(?:\\.\\d+)*)$")
    }

    private val connection = Jsoup.connect(URL)
            .ignoreHttpErrors(true)
            .ignoreContentType(true)

    fun setTimeout(timeout: Int) {
        connection.timeout(timeout)
    }

    fun getLatestRelease(): Release? {
        val response = connection.method(Connection.Method.GET).execute()
        if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND)
            return null

        val obj = JSONObject(response.body())

        val rawVersion = obj["tag_name"] as? String
                ?: throw UpdateParsingException("Tag is not a string")
        val version = VERSION_PATTERN.matchEntire(rawVersion)?.groupValues?.get(1)
                ?: throw UpdateParsingException("Version tag is malformed: <$rawVersion>")

        val links = mutableListOf<String>()

        val assets = obj["assets"] as? JSONArray
                ?: throw UpdateParsingException("Asset list is not an array")
        for (i in 0 until assets.length()) {
            val asset = assets[i] as? JSONObject
                    ?: throw UpdateParsingException("Asset is not an object")

            val link = asset["browser_download_url"] as? String
                    ?: throw UpdateParsingException("Link is not a string")
            val mime = asset["content_type"] as? String
                    ?: throw UpdateParsingException("Content type is not a string")

            if (mime == MIME_APK) links += link
        }

        if (links.isEmpty()) throw UpdateParsingException("Didn't find an .apk in assets")
        val url = links.singleOrNull() ?: throw UpdateParsingException("Too many .apk files in assets")

        return Release(version, url)
    }

    fun installUpdate(file: File, context: Context) {
        val uri = UpdateFileProvider.getUriForFile(context, file)
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE, uri)
                .putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        context.startActivity(intent)
    }

}

class UpdateParsingException : RuntimeException {

    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)

}

private const val CONTENT_UPDATE = "update"

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
                    .appendPath(CONTENT_UPDATE)
                    .appendPath(relative.path)
                    .build()
        }

    }

    override fun onCreate() = true

    override fun getType(uri: Uri) = MIME_APK

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        if (mode != "r")
            throw FileNotFoundException("Can't open $uri in <$mode> mode: file is read-only")

        val path = uri.path

        val updatePath = path.removePrefix("/$CONTENT_UPDATE/")
        if (path != updatePath) {
            val file = getUpdateDirectory(context).resolve(updatePath)
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        }

        throw FileNotFoundException("Unknown path")
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
