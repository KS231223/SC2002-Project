package common;

import java.util.Collections;
import java.util.List;

/**
 * Utility methods for ordering lists of {@link Entity} instances by identifier.
 */
public class EntitySorter {

    /**
     * Sorts entities ascending using the first column value.
     */
    public static void sortByFirstValue(List<Entity> list) {
        Collections.sort(list, (e1, e2) -> e1.values[0].compareTo(e2.values[0]));
    }

    /**
     * Sorts entities descending using the first column value.
     */
    public static void sortByFirstValueDescending(List<Entity> list) {
        Collections.sort(list, (e1, e2) -> e2.values[0].compareTo(e1.values[0]));
    }
}
