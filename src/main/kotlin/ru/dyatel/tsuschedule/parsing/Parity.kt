package ru.dyatel.tsuschedule.parsing

import android.content.Context
import ru.dyatel.tsuschedule.R

enum class Parity(i: Int, private val textResource: Int) {

    ODD(0, R.string.odd_week), EVEN(1, R.string.even_week), BOTH(-1, 0);

    val index: Int = i
        get() {
            if (this == BOTH)
                throw IllegalArgumentException("Can't get index for Parity.BOTH!")
            return field
        }

    fun toText(context: Context): String {
        if (this == BOTH)
            throw IllegalArgumentException("Can't get text resource for Parity.BOTH!")
        return context.getString(textResource)
    }

}

fun indexToParity(index: Int) = when(index) {
    0 -> Parity.ODD
    1 -> Parity.EVEN
    else -> throw IllegalArgumentException("Bad index $index")
}
