package ru.dyatel.tsuschedule.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * An utility to extract all unique values in a {@link java.util.Collection}
 * received from {@link #getKey(Object)} method.
 * <p>
 * <code>IT</code> is the type of elements in the {@link java.util.Collection} and
 * <code>KT</code> is the type of keys that will be extracted
 * </p>
 *
 * @see #getKey(Object)
 */
public abstract class KeyExtractor<IT, KT> {

    /**
     * Method to get every unique value in an element from the given {@link java.util.Collection}.
     *
     * @param collection a collection to extract keys from
     * @return a {@link java.util.HashSet} of all unique values returned by {@link #getKey(Object)}
     * @see #getKey(Object)
     */
    public Set<KT> extract(Collection<IT> collection) {
        Set<KT> result = new HashSet<>();
        for (IT item : collection) {
            result.add(getKey(item));
        }
        return result;
    }

    /**
     * A method to extract a key from an element from the collection.
     * <p>
     * <em>Make sure that keys have a correct {@link Object#hashCode()} method!</em>
     * </p>
     *
     * @param element an element containing a key value
     * @return a key value
     */
    protected abstract KT getKey(IT element);

}
