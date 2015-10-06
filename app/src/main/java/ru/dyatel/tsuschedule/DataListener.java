package ru.dyatel.tsuschedule;

import ru.dyatel.tsuschedule.parsing.Lesson;

import java.util.Set;

public interface DataListener {

    void onLoad(Set<Lesson> lessons);

}
