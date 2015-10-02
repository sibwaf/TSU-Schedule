package ru.dyatel.tsuschedule.parsing.util;

import java.util.HashSet;
import java.util.Set;

public abstract class KeyExtractor<IT, KT> {

    public Set<KT> extract(Set<IT> set) {
        Set<KT> result = new HashSet<KT>();
        for (IT item : set) {
            result.add(getKey(item));
        }
        return result;
    }

    protected abstract KT getKey(IT element);

}
