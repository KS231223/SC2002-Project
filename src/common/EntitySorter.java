package common;

import java.util.Collections;
import java.util.List;
import java.util.Comparator;

public class  EntitySorter {

    public static void sortByFirstValue(List<Entity> list) {
        Collections.sort(list, (e1, e2) -> e1.values[0].compareTo(e2.values[0]));
    }

    public static void sortByFirstValueDescending(List<Entity> list) {
        Collections.sort(list, (e1, e2) -> e2.values[0].compareTo(e1.values[0]));
    }
}
