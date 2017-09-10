package ru.dyatel.tsuschedule.updater

import android.content.Context
import android.content.Intent
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import ru.dyatel.tsuschedule.MIME_APK
import ru.dyatel.tsuschedule.ParsingException
import java.io.File
import java.net.HttpURLConnection

private const val REPOSITORY = "dya-tel/TSU-Schedule"
private const val URL = "https://api.github.com/repos/$REPOSITORY/releases/latest"

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
                ?: throw ParsingException("Tag is not a string")
        val version = VERSION_PATTERN.matchEntire(rawVersion)?.groupValues?.get(1)
                ?: throw ParsingException("Version tag is malformed: <$rawVersion>")

        val links = mutableListOf<String>()

        val assets = obj["assets"] as? JSONArray
                ?: throw ParsingException("Asset list is not an array")
        for (i in 0 until assets.length()) {
            val asset = assets[i] as? JSONObject
                    ?: throw ParsingException("Asset is not an object")

            val link = asset["browser_download_url"] as? String
                    ?: throw ParsingException("Link is not a string")
            val mime = asset["content_type"] as? String
                    ?: throw ParsingException("Content type is not a string")

            if (mime == MIME_APK) links += link
        }

        if (links.isEmpty()) throw ParsingException("Didn't find an .apk in assets")
        val url = links.singleOrNull() ?: throw ParsingException("Too many .apk files in assets")

        return Release(version, url)
    }

    fun installUpdate(file: File, context: Context) {
        val uri = UpdateFileProvider.getUriForFile(context, file)
        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE, uri)
                .putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
        context.startActivity(intent)
    }

}
