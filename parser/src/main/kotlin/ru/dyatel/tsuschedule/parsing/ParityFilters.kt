package ru.dyatel.tsuschedule.parsing

import ru.dyatel.tsuschedule.util.Filter

class ParityFilter(parity: Parity) : Filter<Lesson> {

    private val parity: Parity

    init {
        this.parity = parity
    }

    override fun accept(obj: Lesson?): Boolean =
            obj?.parity == Parity.BOTH || obj?.parity == parity

}

val evenParityFilter = ParityFilter(Parity.EVEN)
val oddParityFilter = ParityFilter(Parity.ODD)
