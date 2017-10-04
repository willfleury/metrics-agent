package com.fleury.metrics.agent.transformer.util;

import java.util.Collection;

public class CollectionUtil {

    public static <T> boolean isEmpty(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return true;
        }

        return false;
    }

    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }
}
