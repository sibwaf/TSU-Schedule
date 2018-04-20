package ru.dyatel.tsuschedule.layout

import android.content.Context
import android.view.View
import org.jetbrains.anko.dip

private const val DIP_ELEVATION_CONST = 5
val Context.DIP_ELEVATION
    get() = dip(DIP_ELEVATION_CONST)
val Context.DIP_ELEVATION_F
    get() = DIP_ELEVATION.toFloat()
val View.DIP_ELEVATION
    get() = dip(DIP_ELEVATION_CONST)
val View.DIP_ELEVATION_F
    get() = DIP_ELEVATION.toFloat()

private const val DIP_CARD_RADIUS_CONST = 2
val Context.DIP_CARD_RADIUS
    get() = dip(DIP_CARD_RADIUS_CONST)
val Context.DIP_CARD_RADIUS_F
    get() = DIP_CARD_RADIUS.toFloat()
val View.DIP_CARD_RADIUS
    get() = dip(DIP_CARD_RADIUS_CONST)
val View.DIP_CARD_RADIUS_F
    get() = DIP_CARD_RADIUS.toFloat()