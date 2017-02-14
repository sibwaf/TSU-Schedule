package ru.dyatel.tsuschedule

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

fun SQLiteDatabase.queryDV(table: String, columns: Array<String>? = null,
                           where: String? = null, whereArgs: Array<String>? = null,
                           groupBy: String? = null, having: String? = null,
                           orderBy: String? = null): Cursor =
        query(table, columns, where, whereArgs, groupBy, having, orderBy)
