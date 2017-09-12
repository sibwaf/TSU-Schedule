package ru.dyatel.tsuschedule.updater

import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import ru.dyatel.tsuschedule.MIME_APK
import ru.dyatel.tsuschedule.ParsingException
import ru.dyatel.tsuschedule.VERSION_PATTERN
import ru.dyatel.tsuschedule.utilities.find
import java.net.HttpURLConnection

class UpdaterApi {

    private companion object {
        const val REPOSITORY = "dya-tel/TSU-Schedule"
        const val URL = "https://api.github.com/repos/$REPOSITORY/releases/latest"
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

        try {
            val obj = JSONObject(response.body())

            val rawVersion = obj.find<String>("tag_name")
            val version = VERSION_PATTERN.matchEntire(rawVersion)?.groupValues?.get(1)
                    ?: throw ParsingException("Version tag is malformed: <$rawVersion>")

            val links = mutableListOf<String>()

            val assets = obj.find<JSONArray>("assets")
            for (i in 0 until assets.length()) {
                val asset = assets[i] as JSONObject

                val link = asset.find<String>("browser_download_url")
                val mime = asset.find<String>("content_type")

                if (mime == MIME_APK) links += link
            }

            if (links.isEmpty()) throw ParsingException("Didn't find an .apk in assets")
            val url = links.singleOrNull() ?: throw ParsingException("Too many .apk files in assets")

            return Release(version, url)
        } catch (e: Exception) {
            if (e is ParsingException) throw e
            else throw ParsingException(e)
        }
    }

}
