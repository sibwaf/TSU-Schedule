package ru.dyatel.tsuschedule.layout

import android.content.Context
import android.view.View
import org.jetbrains.anko.dip

private const val DP_ELEVATION = 5
val Context.DIM_ELEVATION
    get() = dip(DP_ELEVATION)
val Context.DIM_ELEVATION_F
    get() = DIM_ELEVATION.toFloat()
val View.DIM_ELEVATION
    get() = dip(DP_ELEVATION)
val View.DIM_ELEVATION_F
    get() = DIM_ELEVATION.toFloat()

private const val DP_CARD_RADIUS = 2
val View.DIM_CARD_RADIUS
    get() = dip(DP_CARD_RADIUS)
val View.DIM_CARD_RADIUS_F
    get() = DIM_CARD_RADIUS.toFloat()

private const val DP_SMALL = 2
val View.DIM_SMALL
    get() = dip(DP_SMALL)

private const val DP_MEDIUM = 4
val View.DIM_MEDIUM
    get() = dip(DP_MEDIUM)

private const val DP_LARGE = 8
val View.DIM_LARGE
    get() = dip(DP_LARGE)

private const val DP_ULTRA_LARGE = 16
val View.DIM_ULTRA_LARGE
    get() = dip(DP_ULTRA_LARGE)

val View.DIM_DIALOG_SIDE_PADDING
    get() = DIM_ULTRA_LARGE

val View.DIM_CARD_HORIZONTAL_MARGIN
    get() = DIM_LARGE

val View.DIM_CARD_VERTICAL_MARGIN
    get() = DIM_LARGE

val View.DIM_CARD_PADDING
    get() = DIM_LARGE

val View.DIM_TEXT_ITEM_HORIZONTAL_PADDING
    get() = DIM_ULTRA_LARGE

val View.DIM_TEXT_ITEM_VERTICAL_PADDING
    get() = DIM_LARGE

const val SP_TEXT_MEDIUM = 16f
