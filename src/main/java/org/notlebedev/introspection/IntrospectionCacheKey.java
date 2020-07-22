package org.notlebedev.introspection;

import java.util.Objects;
import java.util.Set;

class IntrospectionCacheKey<C> {
    C key;
    Set<Class<?>> omitClasses;

    public IntrospectionCacheKey(C key, Set<Class<?>> omitClasses) {
        this.key = key;
        this.omitClasses = omitClasses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntrospectionCacheKey<?> that = (IntrospectionCacheKey<?>) o;
        return key.equals(that.key) &&
                that.omitClasses.containsAll(omitClasses);
        //Classes loaded can be used if classes omitted are subset of classes omitted
        //in request, hovewer, than excessive classes must be removed from result
    }

    @Override
    public int hashCode() {
        return Objects.hash(key); //omitClasses field is not considered
    }
}
