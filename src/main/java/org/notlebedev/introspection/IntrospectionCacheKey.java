package org.notlebedev.introspection;

import java.util.Objects;
import java.util.Set;

class IntrospectionCacheKey<C> {
    private final C key;
    private final Set<Class<?>> omitClasses;
    private final boolean isSample;

    public IntrospectionCacheKey(C key, Set<Class<?>> omitClasses, boolean isSample) {
        this.key = key;
        this.omitClasses = omitClasses;
        this.isSample = isSample;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntrospectionCacheKey<?> that = (IntrospectionCacheKey<?>) o;
        if(isSample)
            return key.equals(that.key) &&
                    omitClasses.containsAll(that.omitClasses);
        else
            return key.equals(that.key) &&
                    that.omitClasses.containsAll(omitClasses);
        //Classes loaded can be used if classes omitted are subset of classes omitted
        //in request, hovewer, than excessive classes must be removed from result
        //The mechanism with isSample field is used to determine direction of
        //inclusion
    }

    @Override
    public int hashCode() {
        return Objects.hash(key); //omitClasses field is not considered
    }
}
