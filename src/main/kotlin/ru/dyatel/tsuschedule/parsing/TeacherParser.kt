package ru.dyatel.tsuschedule.parsing

import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.Jsoup
import ru.dyatel.tsuschedule.data.Teacher
import ru.dyatel.tsuschedule.utilities.find
import ru.dyatel.tsuschedule.utilities.iterator

class TeacherParser {

    private val connection = Jsoup.connect("http://schedule.tsu.tula.ru/autocomplete/teacher_search.php")
            .ignoreContentType(true)
            .header("X-Requested-With", "XMLHttpRequest")
            .method(Connection.Method.GET)

    fun setTimeout(timeout: Int) {
        connection.timeout(timeout)
    }

    fun getTeachers(name: String): Set<Teacher> {
        val response = connection.data("term", name).execute()

        return JSONArray(response.body()).iterator().asSequence()
                .map { it as JSONObject }
                .map { Teacher(it.find("id"), it.find("value")) }
                .toSet()
    }

}
