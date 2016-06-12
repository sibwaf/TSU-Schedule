package ru.dyatel.tsuschedule.data;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import org.jetbrains.annotations.NotNull;
import ru.dyatel.tsuschedule.ActivityUtilKt;
import ru.dyatel.tsuschedule.events.Event;
import ru.dyatel.tsuschedule.events.EventBus;
import ru.dyatel.tsuschedule.events.EventListener;
import ru.dyatel.tsuschedule.parsing.Lesson;
import ru.dyatel.tsuschedule.parsing.LessonUtilKt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class LessonDAO extends SQLiteOpenHelper implements EventListener {

    private static final int DB_VERSION = 2;
    private static final String DB_FILE = "data.db";

    private static final String TABLE_UNFILTERED = "lessons";
    private static final String TABLE_FILTERED = "filtered";

    private static final String QUERY_WHERE_SUBGROUP =
            LessonTable.SUBGROUP + "=0 OR " + LessonTable.SUBGROUP + "=?";

    private EventBus eventBus;

    public LessonDAO(Activity activity) {
        super(activity, DB_FILE, null, DB_VERSION);
        eventBus = ActivityUtilKt.getEventBus(activity);
        eventBus.subscribe(this, Event.DATA_MODIFIER_SET_CHANGED);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LessonTable.getCreateQuery(TABLE_UNFILTERED));
        db.execSQL(LessonTable.getCreateQuery(TABLE_FILTERED));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Recreate tables
        db.execSQL(LessonTable.getDropQuery(TABLE_UNFILTERED));
        db.execSQL(LessonTable.getDropQuery(TABLE_FILTERED));
        onCreate(db);
    }

    public void update(Collection<Lesson> lessons) {
        SQLiteDatabase db = getWritableDatabase();

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
        SQLiteDatabase db = getWritableDatabase();

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
    }

    public List<Lesson> request(int subgroup) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_FILTERED,
                null,
                QUERY_WHERE_SUBGROUP, new String[]{String.valueOf(subgroup)},
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
    public void handleEvent(@NotNull Event type) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                applyModifiers();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                eventBus.broadcast(Event.DATA_UPDATED);
            }
        }.execute();
    }

}
