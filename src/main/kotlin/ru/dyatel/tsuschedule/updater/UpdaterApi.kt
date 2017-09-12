package ru.dyatel.tsuschedule.updater

import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import ru.dyatel.tsuschedule.MIME_APK
import ru.dyatel.tsuschedule.ParsingException
import ru.dyatel.tsuschedule.handle
import ru.dyatel.tsuschedule.utilities.find
import ru.dyatel.tsuschedule.utilities.iterator
import java.net.HttpURLConnection

class UpdaterApi {

    private companion object {
        const val REPOSITORY = "dya-tel/TSU-Schedule"
        const val URL = "https://api.github.com/repos/$REPOSITORY/releases"
    }

    private val connection = Jsoup.connect(URL)
            .ignoreHttpErrors(true)
            .ignoreContentType(true)

    fun setTimeout(timeout: Int) {
        connection.timeout(timeout)
    }

    fun getLatestRelease(allowPrerelease: Boolean): Release? {
        val response = connection.method(Connection.Method.GET).execute()
        if (response.statusCode() == HttpURLConnection.HTTP_NOT_FOUND)
            return null

        val releases = try {
            JSONArray(response.body())
        } catch (e: Exception) {
            throw ParsingException(e)
        }

        for (releaseJson in releases) {
            val release = try {
                parseRelease(releaseJson as JSONObject)
            } catch (e: Exception) {
                (e as? ParsingException ?: ParsingException(e)).handle()
                continue
            }
            if (!release.isPrerelease || allowPrerelease) return release
        }

        return null
    }

    private fun parseRelease(json: JSONObject): Release {
        val links = json.find<JSONArray>("assets").iterator().asSequence()
                .map { it as JSONObject }
                .filter { it.find<String>("content_type") == MIME_APK }
                .map { it.find<String>("browser_download_url") }
                .toList()

        if (links.isEmpty()) throw ParsingException("No .apk files in assets")

        val url = links.singleOrNull() ?: throw ParsingException("Too many .apk files in assets")
        return Release(json.find("tag_name"), url)
    }

}
