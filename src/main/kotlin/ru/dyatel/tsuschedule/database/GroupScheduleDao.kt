package ru.dyatel.tsuschedule.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.FOREIGN_KEY
import org.jetbrains.anko.db.INTEGER
import org.jetbrains.anko.db.SqlType
import org.jetbrains.anko.db.TEXT
import org.jetbrains.anko.db.dropTable
import org.jetbrains.anko.db.update
import ru.dyatel.tsuschedule.events.Event
import ru.dyatel.tsuschedule.events.EventBus
import ru.dyatel.tsuschedule.events.EventListener
import ru.dyatel.tsuschedule.model.GroupLesson
import ru.dyatel.tsuschedule.model.Lesson
import ru.dyatel.tsuschedule.utilities.schedulePreferences

abstract class GroupScheduleDao(
        table: String,
        keyColumn: String,
        protected val context: Context,
        databaseManager: DatabaseManager
) : ScheduleDao<GroupLesson>(table, keyColumn, databaseManager) {

    protected object Columns {
        const val TEACHER = "teacher"
        const val SUBGROUP = "subgroup"
    }

    private companion object {
        val PARSER = object : LessonParser<GroupLesson>() {
            override fun decorate(columns: Map<String, Any?>, base: Lesson): GroupLesson {
                return with(base) {
                    val teacher = columns[Columns.TEACHER] as String?
                    val subgroup = (columns[Columns.SUBGROUP] as Long?)?.toInt()
                    GroupLesson(parity, weekday, time, discipline, auditory, teacher, type, subgroup)
                }
            }
        }

        val SERIALIZER = object : LessonSerializer<GroupLesson>() {
            override fun decorate(lesson: GroupLesson, result: ContentValues) {
                result.apply {
                    put(Columns.TEACHER, lesson.teacher)
                    put(Columns.SUBGROUP, lesson.subgroup)
                }
            }
        }
    }

    override val parser = PARSER
    override val serializer = SERIALIZER

    override fun decorateTable(columns: MutableMap<String, SqlType>) {
        columns.apply {
            put(Columns.TEACHER, TEXT)
            put(Columns.SUBGROUP, INTEGER)
        }
    }

    override fun upgradeTables(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            db.dropTable(table, true)
            createTables(db)
            return
        }

        val preferences = context.schedulePreferences

        if (oldVersion < 6) {
            val group = preferences.group
            if (group != null && group in preferences.groups) {
                db.execSQL("ALTER TABLE $table ADD COLUMN $keyColumn ${TEXT.render()}")
                db.update(table, keyColumn to group).exec()
            } else {
                db.dropTable(table, true)
                createTables(db)
                return
            }
        }
    }

}

@Deprecated("Use RawGroupScheduleDao instead")
class UnfilteredGroupScheduleDao(
        context: Context,
        databaseManager: DatabaseManager
) : GroupScheduleDao("lessons", "`group`", context, databaseManager) {

    override fun save(key: String, lessons: Collection<GroupLesson>) {
        executeTransaction {
            super.save(key, lessons)
            databaseManager.filteredGroupSchedule.save(key, lessons)
        }
    }

    override fun remove(key: String) {
        executeTransaction {
            super.remove(key)
            databaseManager.filteredGroupSchedule.remove(key)
        }
    }
}

class RawGroupScheduleDao(
        context: Context,
        databaseManager: DatabaseManager
) : GroupScheduleDao("lessons_raw", Columns.ID, context, databaseManager) {

    private object Columns {
        const val ID = "id"
    }

    override fun decorateTable(columns: MutableMap<String, SqlType>) {
        super.decorateTable(columns)

        val idColumn = FOREIGN_KEY(Columns.ID, ScheduleSnapshotDao.TABLE, ScheduleSnapshotDao.Columns.ID)
        columns[idColumn.first] = idColumn.second
    }

    override fun upgradeTables(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 7) {
            db.dropTable(table, true)
            createTables(db)
            return
        }
    }

    fun transferSnapshot(old: Long, new: Long) {
        execute {
            update(table, Columns.ID to new)
                    .whereSimple("${Columns.ID} = ?", old.toString())
                    .exec()
        }
    }

    fun requestCurrentSnapshot(group: String): List<GroupLesson> {
        return executeTransaction {
            val snapshot = databaseManager.snapshots.request(group).singleOrNull { it.selected }
                    ?: return@executeTransaction emptyList()

            request(snapshot.id.toString())
        }
    }
}

class FilteredGroupScheduleDao(
        context: Context,
        databaseManager: DatabaseManager
) : GroupScheduleDao("filtered", "`group`", context, databaseManager), EventListener {

    init {
        EventBus.subscribe(this, Event.DATA_MODIFIER_SET_CHANGED)
    }

    override fun save(key: String, lessons: Collection<GroupLesson>) {
        val filters = databaseManager.filters.request(key).filter { it.enabled }
        val filtered = lessons
                .mapNotNull {
                    var result: GroupLesson? = it
                    for (filter in filters) {
                        if (result == null)
                            break
                        result = filter.apply(result)
                    }
                    result
                }

        super.save(key, filtered)

        EventBus.broadcast(Event.DATA_UPDATED, key)
    }

    override fun handleEvent(type: Event, payload: Any?) {
        context.schedulePreferences.groups.forEach {
            val unfiltered = databaseManager.rawGroupSchedule.requestCurrentSnapshot(it)
            save(it, unfiltered)
        }
    }
}
