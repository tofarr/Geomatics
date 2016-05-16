package org.lcd.criteria;

import java.beans.ConstructorProperties;
import org.lcd.sort.SimpleSortOrder;

/**
 *
 * @author tofar
 * @param <C>
 */
public class Less<C extends Comparable> extends Equal<C> {

    @ConstructorProperties({"value"})
    public Less(C value) {
        super(value);
    }  
    @Override
    public boolean match(C value) {
        return SimpleSortOrder.compareValues(this.getValue(), value) > 0;
    }
}
