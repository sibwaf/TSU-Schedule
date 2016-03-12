package ru.dyatel.tsuschedule.data;

import android.provider.BaseColumns;

public class LessonTable implements BaseColumns {

    private LessonTable() {
    }

    public static final String PARITY = "parity";
    public static final String WEEKDAY = "weekday";
    public static final String TIME = "time";

    public static final String DISCIPLINE = "discipline";
    public static final String AUDITORY = "auditory";
    public static final String TEACHER = "teacher";

    public static final String TYPE = "type";
    public static final String SUBGROUP = "subgroup";

}
