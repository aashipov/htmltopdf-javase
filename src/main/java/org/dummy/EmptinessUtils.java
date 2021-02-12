package org.dummy;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Map;

/**
 * If object is null or empty.
 */
public final class EmptinessUtils {

    private EmptinessUtils() {
        //Utility
    }

    /**
     * If object is null or empty?.
     * @param value some value
     * @return empty or null?
     */
    public static boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        } else if (value instanceof String) {
            return ((String) value).isEmpty();
        } else if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        } else if (value instanceof Map) {
            return ((Map<?,?>) value).isEmpty();
        } else if (value instanceof Dictionary) {
            return ((Dictionary<?,?>) value).isEmpty();
        } else {
            return false;
        }
    }

    /**
     * If object is NOT null or empty?.
     * @param value some value
     * @return NOT empty or null?
     */
    public static boolean isNotEmpty(Object value) {
        return !isEmpty(value);
    }
}
