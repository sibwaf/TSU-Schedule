package ru.dyatel.tsuschedule.parsing.util;

/**
 * A Filter to use in the {@link ru.dyatel.tsuschedule.parsing.util.IterableFilter} instance.
 * <p>
 * Multiple filters can be applied.
 * </p>
 *
 * @see ru.dyatel.tsuschedule.parsing.util.IterableFilter#apply(ru.dyatel.tsuschedule.parsing.util.Filter)
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
