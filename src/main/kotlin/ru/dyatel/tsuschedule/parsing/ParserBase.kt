package ru.dyatel.tsuschedule.parsing

import ru.dyatel.tsuschedule.ParsingException

abstract class ParserBase {

    protected fun <T> Collection<T>.requireSingle() = singleOrNull() ?: throw ParsingException()

    protected fun <T> Collection<T>.requireSingleOrNull() = if (isEmpty()) null else requireSingle()

}
