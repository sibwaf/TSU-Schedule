package ru.dyatel.tsuschedule.util;

/**
 * A Filter to use in the {@link IterableFilter} instance.
 * <p>
 * Multiple filters can be applied.
 * </p>
 *
 * @see IterableFilter#apply(Filter)
 */
public interface Filter<T> {

    /**
     * Checks if given object should be added to the filtered collection.
     *
     * @param obj object to check
     * @return true if object should remain, false otherwise
     */
    boolean accept(T obj);

}
