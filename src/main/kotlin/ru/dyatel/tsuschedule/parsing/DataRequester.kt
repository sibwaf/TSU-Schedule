package ru.dyatel.tsuschedule.parsing

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class DataRequester {

    var timeout = 30000

    fun groupSchedule(group: String): Element {
        return Jsoup.connect("http://schedule.tsu.tula.ru/")
                .timeout(timeout)
                .data("group", group)
                .get()
                .getElementById("results")
    }

    fun teacherSchedule(teacher: String): Element {
        return Jsoup.connect("http://schedule.tsu.tula.ru/")
                .timeout(timeout)
                .data("teacher", teacher)
                .get()
                .getElementById("results")
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

    fun examSchedule(group: String) : Element {
        return Jsoup.connect("http://schedule.tsu.tula.ru/exam/")
                .timeout(timeout)
                .data("group", group)
                .get()
                .getElementById("results")
    }

}
