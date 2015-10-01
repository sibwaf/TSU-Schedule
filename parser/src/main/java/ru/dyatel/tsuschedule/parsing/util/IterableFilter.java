package ru.dyatel.tsuschedule.parsing.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IterableFilter<T> {

    private List<Filter<T>> filters = new ArrayList<Filter<T>>();

    public void apply(Filter<T> filter) {
        filters.add(filter);
    }

    public Set<T> filter(Set<T> collection) {
        Set<T> result = new HashSet<T>();
        filterCollection(collection, result);
        return result;
    }

    public List<T> filter(List<T> collection) {
        List<T> result = new ArrayList<T>();
        filterCollection(collection, result);
        return result;
    }

    private void filterCollection(Collection<T> source, Collection<T> result) {
        for (T e : source) {
            if (check(e)) result.add(e);
        }
    }

    private boolean check(T e) {
        for (Filter<T> filter : filters) {
            if (!filter.accept(e)) return false;
        }
        return true;
    }

}
