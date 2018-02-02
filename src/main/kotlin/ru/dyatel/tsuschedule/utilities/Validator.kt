package ru.dyatel.tsuschedule.utilities

import ru.dyatel.tsuschedule.BlankGroupIndexException
import ru.dyatel.tsuschedule.ShortGroupIndexException

object Validator {

    fun validateGroup(group: String): String {
        val trimmed = group.trim()

        if (trimmed.isEmpty())
            throw BlankGroupIndexException()
        if (trimmed.length < 4)
            throw ShortGroupIndexException()

        return trimmed
    }

}