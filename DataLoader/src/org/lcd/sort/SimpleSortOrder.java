package org.lcd.sort;

import java.beans.ConstructorProperties;

/**
 *
 * @author tofar
 */
public class SimpleSortOrder<C extends Comparable> implements SortOrder<C> {

    private final String attr;

    @ConstructorProperties({"attr"})
    public SimpleSortOrder(String attr) {
        this.attr = attr;
    }

    public String getAttr() {
        return attr;
    }

    @Override
    public int compare(C o1, C o2) {
        return o1.compareTo(o2);
    }
}
