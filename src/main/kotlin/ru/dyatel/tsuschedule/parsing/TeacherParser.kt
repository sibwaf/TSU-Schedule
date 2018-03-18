package ru.dyatel.tsuschedule.parsing

import org.json.JSONArray
import org.json.JSONObject
import ru.dyatel.tsuschedule.model.Teacher
import ru.dyatel.tsuschedule.utilities.find
import ru.dyatel.tsuschedule.utilities.iterator

object TeacherParser {

    fun parse(data: String): Set<Teacher> {
        return JSONArray(data).iterator().asSequence()
                .map { it as JSONObject }
                .map { Teacher(it.find("id"), it.find("value")) }
                .toSet()
    }

}
