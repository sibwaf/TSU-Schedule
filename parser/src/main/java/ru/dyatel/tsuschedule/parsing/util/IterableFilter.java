package ru.dyatel.tsuschedule.parsing.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An utility to filter {@link java.util.Set} and {@link java.util.List} collections.
 * <p>
 * Filtered collection will have all elements which were accepted by every applied filter.
 * </p>
 *
 * @see ru.dyatel.tsuschedule.parsing.util.Filter
 */
public class IterableFilter<T> {

    private List<Filter<T>> filters = new ArrayList<Filter<T>>();

    /**
     * Add a {@link ru.dyatel.tsuschedule.parsing.util.Filter} which will be used
     * to determine if an object should pass to the resulting collection.
     * <p>
     * Filters are applied in the order they were added.
     * If filter doesn't accept an element, next filters won't receive it.
     * </p>
     * <p>
     * For example, if all applied filters assume that there is no <code>null</code> elements
     * in the collection, you should apply filter to check it before all other ones.
     * </p>
     *
     * @param filter filter to be added to the filter list
     * @see #filterCollection(Collection, Collection)
     */
    public void apply(Filter<T> filter) {
        filters.add(filter);
    }

    /**
     * Method to filter any {@link java.util.Set}.
     * <p>
     * When using this method,
     * <em>make sure elements being filtered have a correct {@link Object#hashCode()} methods!</em>
     * </p>
     *
     * @param collection {@link java.util.Set} to be filtered
     * @return <em>new</em> instance of {@link java.util.HashSet} which contains all accepted elements
     * @see #filterCollection(Collection, Collection)
     */
    public Set<T> filter(Set<T> collection) {
        Set<T> result = new HashSet<T>();
        filterCollection(collection, result);
        return result;
    }

    /**
     * Method to filter any {@link java.util.List}.
     *
     * @param collection {@link java.util.List} to be filtered
     * @return <em>new</em> instance of {@link java.util.ArrayList} which contains all accepted elements
     * @see #filterCollection(Collection, Collection)
     */
    public List<T> filter(List<T> collection) {
        List<T> result = new ArrayList<T>();
        filterCollection(collection, result);
        return result;
    }

    /**
     * A private method to filter a {@link java.util.Collection} regardless of it's implementation.
     *
     * @param source a collection that needs to be filtered
     * @param result a collection which will have all filtered elements
     * @see #check(T)
     */
    private void filterCollection(Collection<T> source, Collection<T> result) {
        for (T e : source) {
            if (check(e)) result.add(e);
        }
    }

    /**
     * A private method to check if an element passes all applied filters.
     * <p>
     * Filters are applied in the order they were added.
     * </p>
     *
     * @param e element to filter
     * @return true if an element should remain, false otherwise
     */
    private boolean check(T e) {
        for (Filter<T> filter : filters) {
            if (!filter.accept(e)) return false;
        }
        return true;
    }

}
