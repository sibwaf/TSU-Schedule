package ru.dyatel.tsuschedule.data;

import ru.dyatel.tsuschedule.parsing.Lesson;

import java.util.Set;

public interface DataListener {

    void onDataUpdate(Set<Lesson> lessons);

}
