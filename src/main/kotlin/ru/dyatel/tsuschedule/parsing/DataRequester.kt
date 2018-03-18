package ru.dyatel.tsuschedule.parsing

import hirondelle.date4j.DateTime
import org.jsoup.Connection
import org.jsoup.Jsoup
import ru.dyatel.tsuschedule.data.RawSchedule
import java.util.TimeZone

class DataRequester {

    var timeout = 30000

    private val timestamp: Long
        get() {
            val timezone = TimeZone.getDefault()
            return DateTime.now(timezone).getMilliseconds(timezone)
        }

    fun groupSchedule(group: String): RawSchedule {
        val data = Jsoup.connect("http://schedule.tsu.tula.ru/")
                .timeout(timeout)
                .data("group", group)
                .get()
                .getElementById("results")
                .outerHtml()
        return RawSchedule(timestamp, data)
    }

    fun teacherSchedule(teacher: String): RawSchedule {
        val data =  Jsoup.connect("http://schedule.tsu.tula.ru/")
                .timeout(timeout)
                .data("teacher", teacher)
                .get()
                .getElementById("results")
                .outerHtml()
        return RawSchedule(timestamp, data)
    }

    fun teacherSearch(query: String): String {
        return Jsoup.connect("http://schedule.tsu.tula.ru/autocomplete/teacher_search.php")
                .timeout(timeout)
                .ignoreContentType(true)
                .header("X-Requested-With", "XMLHttpRequest")
                .data("term", query)
                .method(Connection.Method.GET)
                .execute()
                .body()
    }

}
