package ru.dyatel.tsuschedule.utilities

import android.app.AlertDialog

fun AlertDialog.setMessage(id: Int) = setMessage(context.getString(id))
