package org.roa.sorter;

import java.beans.ConstructorProperties;
import org.roa.element.Element;

/**
 *
 * @author tofarrell
 */
public class DescSorter implements Sorter {

    private final Sorter sorter;

    @ConstructorProperties({"sorter"})
    public DescSorter(Sorter sorter) throws NullPointerException {
        if(sorter == null){
            throw new NullPointerException("Sorter must not be null!");
        }
        this.sorter = sorter;
    }

    public Sorter getSorter() {
        return sorter;
    }

    @Override
    public int compare(Element o1, Element o2) {
        return sorter.compare(o2, o1);
    }

}
