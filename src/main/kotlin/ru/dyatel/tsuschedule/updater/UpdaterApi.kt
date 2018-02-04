package ru.dyatel.tsuschedule.updater

import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import ru.dyatel.tsuschedule.GITHUB_REPOSITORY
import ru.dyatel.tsuschedule.MIME_APK
import ru.dyatel.tsuschedule.ParsingException
import ru.dyatel.tsuschedule.log
import ru.dyatel.tsuschedule.utilities.find
import ru.dyatel.tsuschedule.utilities.iterator
import java.net.HttpURLConnection

class UpdaterApi {

    private val connection = Jsoup.connect("https://api.github.com/repos/$GITHUB_REPOSITORY/releases")
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
            val token = try {
                parseRelease(releaseJson as JSONObject)
            } catch (e: Exception) {
                e.log()
                continue
            }
            if (!token.prerelease || allowPrerelease)
                return token.release
        }

        return null
    }

    private fun parseRelease(json: JSONObject): ReleaseToken {
        val links = json.find<JSONArray>("assets").iterator().asSequence()
                .map { it as JSONObject }
                .filter { it.find<String>("content_type") == MIME_APK }
                .map { it.find<String>("browser_download_url") }
                .toList()

        if (links.isEmpty())
            throw ParsingException("No .apk files in assets")

        val url = links.singleOrNull() ?: throw ParsingException("Too many .apk files in assets")
        val release = Release(json.find("tag_name"), url)

        return ReleaseToken(release, json.find("prerelease"))
    }

}

private class ReleaseToken(val release: Release, val prerelease: Boolean)
