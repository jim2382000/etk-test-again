package net.entellitrak.aea.gl.api.java.comparator;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * Comparator for comparing lists.
 * Compares elements pairwise in the list (starting from the front) until one is smaller than the other. Once it finds
 * one, that list is considered smaller.
 * If each pairwise element in the list is the same, the lists are considered equal.
 * If one list is a prefix of the other list, the shorter list is considered less than the longer list.
 *
 * @author Zachary.Miller
 *
 * @param <T> the type of the item in the lists
 */
public class ListComparator<T extends Comparable<T>> implements Comparator<List<T>>, Serializable {

    /**
     * Used by {@link Serializable} interface.
     */
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(final List<T> list1, final List<T> list2) {
        List<T> remainingList1 = list1;
        List<T> remainingList2 = list2;

        Integer result = null;

        while(result == null) {
            if(remainingList1.isEmpty() && remainingList2.isEmpty()) {
                result = 0;
            } else if(remainingList1.isEmpty()) {
                result = -1;
            } else if(remainingList2.isEmpty()) {
                result = 1;
            } else {
                final int elementComparison = remainingList1.get(0).compareTo(remainingList2.get(0));
                if(elementComparison != 0) {
                    result = elementComparison;
                } else {
                    remainingList1 = remainingList1.subList(1, remainingList1.size());
                    remainingList2 = remainingList2.subList(1, remainingList2.size());
                }
            }
        }

        return result;
    }
}
