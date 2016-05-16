
package org.lcd.criteria;

import java.beans.ConstructorProperties;
import org.lcd.sort.SimpleSortOrder;

/**
 *
 * @author tofar
 * @param <C>
 */
public class Greater<C extends Comparable> extends Equal<C>{

    @ConstructorProperties({"value"})
    public Greater(C value) {
        super(value);
    }    

    @Override
    public boolean match(C value) {
        return SimpleSortOrder.compareValues(this.getValue(), value) < 0;
    }
    
}
