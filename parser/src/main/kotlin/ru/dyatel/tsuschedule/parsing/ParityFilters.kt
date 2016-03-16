package ru.dyatel.tsuschedule.parsing

import ru.dyatel.tsuschedule.util.Filter

class ParityFilter(private val parity: Parity) : Filter<Lesson> {

    override fun accept(obj: Lesson): Boolean =
            obj.parity == Parity.BOTH || obj.parity == parity

}

val evenParityFilter = ParityFilter(Parity.EVEN)
val oddParityFilter = ParityFilter(Parity.ODD)
