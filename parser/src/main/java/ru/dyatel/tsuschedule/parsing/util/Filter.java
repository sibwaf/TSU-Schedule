package ru.dyatel.tsuschedule.parsing.util;

public interface Filter<T> {

    boolean accept(T obj);

}
