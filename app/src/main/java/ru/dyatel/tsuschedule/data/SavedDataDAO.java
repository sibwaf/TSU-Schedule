package ru.dyatel.tsuschedule.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import ru.dyatel.tsuschedule.parsing.Lesson;
import ru.dyatel.tsuschedule.parsing.Parity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SavedDataDAO extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_FILE = "lessons.db";

    private class Table implements BaseColumns {

        public static final String TABLE_NAME = "lessons";

        public static final String COLUMN_PARITY = "parity";
        public static final String COLUMN_WEEKDAY = "weekday";
        public static final String COLUMN_TIME = "time";

        public static final String COLUMN_DISCIPLINE = "discipline";
        public static final String COLUMN_AUDITORY = "auditory";
        public static final String COLUMN_TEACHER = "teacher";

        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_SUBGROUP = "subgroup";

    }

    private static final String QUERY_CREATE_TABLE =
            "CREATE TABLE " + Table.TABLE_NAME + " (" +
                    Table._ID + " INTEGER PRIMARY KEY," +
                    Table.COLUMN_PARITY + " TEXT," +
                    Table.COLUMN_WEEKDAY + " TEXT," +
                    Table.COLUMN_TIME + " TEXT," +
                    Table.COLUMN_DISCIPLINE + " TEXT," +
                    Table.COLUMN_AUDITORY + " TEXT," +
                    Table.COLUMN_TEACHER + " TEXT," +
                    Table.COLUMN_TYPE + " TEXT," +
                    Table.COLUMN_SUBGROUP + " CHAR(1)" +
                    " )";

    private static final String QUERY_DROP_TABLE =
            "DROP TABLE IF EXISTS " + Table.TABLE_NAME;

    public SavedDataDAO(Context context) {
        super(context, DB_FILE, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(QUERY_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Recreate table
        db.execSQL(QUERY_DROP_TABLE);
        onCreate(db);
    }

    public void save(final Set<Lesson> lessons) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                SQLiteDatabase db = getWritableDatabase();

                db.beginTransaction();
                try {
                    db.delete(Table.TABLE_NAME, null, null); // Clear table from previous data

                    for (Lesson l : lessons) {
                        ContentValues values = new ContentValues();
                        values.put(Table.COLUMN_PARITY, l.getParity().toString());
                        values.put(Table.COLUMN_WEEKDAY, l.getWeekday());
                        values.put(Table.COLUMN_TIME, l.getTime());
                        values.put(Table.COLUMN_DISCIPLINE, l.getDiscipline());
                        values.put(Table.COLUMN_AUDITORY, l.getAuditory());
                        values.put(Table.COLUMN_TEACHER, l.getTeacher());
                        values.put(Table.COLUMN_TYPE, l.getType().toString());
                        values.put(Table.COLUMN_SUBGROUP, l.getSubgroup());
                        db.insert(Table.TABLE_NAME, null, values);
                    }

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                db.close();
                return null;
            }
        }.execute();
    }

    public void load(final DataListener listener) {
        new AsyncTask<Void, Void, Set<Lesson>>() {
            @Override
            protected void onPreExecute() {
                listener.beforeDataUpdate();
            }

            @Override
            protected Set<Lesson> doInBackground(Void... params) {
                Set<Lesson> result = Collections.emptySet();

                SQLiteDatabase db = getReadableDatabase();

                Cursor c = db.query(Table.TABLE_NAME, null, null, null, null, null, null);
                if (c.moveToFirst()) {
                    result = new HashSet<Lesson>();
                    int indexParity = c.getColumnIndexOrThrow(Table.COLUMN_PARITY);
                    int indexWeekday = c.getColumnIndexOrThrow(Table.COLUMN_WEEKDAY);
                    int indexTime = c.getColumnIndexOrThrow(Table.COLUMN_TIME);
                    int indexDiscipline = c.getColumnIndexOrThrow(Table.COLUMN_DISCIPLINE);
                    int indexAuditory = c.getColumnIndexOrThrow(Table.COLUMN_AUDITORY);
                    int indexTeacher = c.getColumnIndexOrThrow(Table.COLUMN_TEACHER);
                    int indexType = c.getColumnIndexOrThrow(Table.COLUMN_TYPE);
                    int indexSubgroup = c.getColumnIndexOrThrow(Table.COLUMN_SUBGROUP);
                    do {
                        result.add(new Lesson(
                                Enum.valueOf(Parity.class, c.getString(indexParity)),
                                c.getString(indexWeekday),
                                c.getString(indexTime),
                                c.getString(indexDiscipline),
                                c.getString(indexAuditory),
                                c.getString(indexTeacher),
                                Enum.valueOf(Lesson.Type.class, c.getString(indexType)),
                                c.getInt(indexSubgroup)
                        ));
                    } while (c.moveToNext());
                }
                c.close();

                db.close();
                return result;
            }

            @Override
            protected void onPostExecute(Set<Lesson> lessons) {
                listener.onDataUpdate(lessons);
                listener.afterDataUpdate();
            }

        }.execute();
    }

}
