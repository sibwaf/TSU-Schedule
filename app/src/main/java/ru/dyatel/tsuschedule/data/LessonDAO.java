package ru.dyatel.tsuschedule.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import ru.dyatel.tsuschedule.events.Event;
import ru.dyatel.tsuschedule.events.EventBus;
import ru.dyatel.tsuschedule.events.EventListener;
import ru.dyatel.tsuschedule.parsing.Lesson;
import ru.dyatel.tsuschedule.parsing.LessonUtilKt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class LessonDAO implements DatabasePart, EventListener {

	private static final String TABLE_UNFILTERED = "lessons";
	private static final String TABLE_FILTERED = "filtered";

	private static final String QUERY_WHERE_SUBGROUP =
			LessonTable.SUBGROUP + "=0 OR " + LessonTable.SUBGROUP + "=?";

	private EventBus eventBus;
	private DatabaseManager manager;

	public LessonDAO(DatabaseManager manager, EventBus eventBus) {
		this.manager = manager;
		this.eventBus = eventBus;

		eventBus.subscribe(this, Event.DATA_MODIFIER_SET_CHANGED);
	}

	@Override
	public void createTables(SQLiteDatabase db) {
		db.execSQL(LessonTable.INSTANCE.getCreateQuery(TABLE_UNFILTERED));
		db.execSQL(LessonTable.INSTANCE.getCreateQuery(TABLE_FILTERED));
	}

	@Override
	public void upgradeTables(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Recreate tables
		db.execSQL(DatabaseManagerKt.getDropTableQuery(TABLE_UNFILTERED));
		db.execSQL(DatabaseManagerKt.getDropTableQuery(TABLE_FILTERED));
		createTables(db);
	}

	public void update(Collection<Lesson> lessons) {
		SQLiteDatabase db = manager.getWritableDatabase();

		db.beginTransaction();
		try {
			db.delete(TABLE_UNFILTERED, null, null); // Clear table from previous data
			for (Lesson l : lessons) {
				db.insert(TABLE_UNFILTERED, null, LessonUtilKt.toContentValues(l));
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		applyModifiers();
	}

	private void applyModifiers() {
		SQLiteDatabase db = manager.getWritableDatabase();

		Cursor cursor = db.query(TABLE_UNFILTERED, null, null, null, null, null, null);
		Map<String, Integer> indices = LessonUtilKt.getLessonColumnIndices(cursor);

		db.beginTransaction();
		try {
			db.delete(TABLE_FILTERED, null, null);
			while (cursor.moveToNext()) {
				Lesson lesson = LessonUtilKt.constructLessonFromCursor(cursor, indices);
				// TODO: apply modifiers to the lesson
				db.insert(TABLE_FILTERED, null, LessonUtilKt.toContentValues(lesson));
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		cursor.close();

		eventBus.broadcast(Event.DATA_UPDATED);
	}

	public List<Lesson> request(int subgroup) {
		SQLiteDatabase db = manager.getReadableDatabase();

		Cursor cursor = db.query(
				TABLE_FILTERED,
				null,
				subgroup == 0 ? null : QUERY_WHERE_SUBGROUP, subgroup == 0 ? null : new String[]{String.valueOf(subgroup)},
				null, null,
				LessonTable.TIME
		);
		Map<String, Integer> indices = LessonUtilKt.getLessonColumnIndices(cursor);

		List<Lesson> lessons = new ArrayList<>();
		while (cursor.moveToNext()) {
			lessons.add(LessonUtilKt.constructLessonFromCursor(cursor, indices));
		}

		cursor.close();

		return lessons;
	}

	@Override
	public void handleEvent(Event type) {
		applyModifiers();
	}

}
