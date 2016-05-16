
package org.lcd.criteria;

import java.beans.ConstructorProperties;

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
    protected boolean matchNull(boolean aNull, boolean bNull) {
        return !bNull;
    }

    @Override
    protected boolean matchStr(String a, String b) {
        return a.compareTo(b) <  0;
    }

    @Override
    protected boolean matchNum(double a, double b) {
        return a < b;
    }

    @Override
    protected boolean matchObj(C a, C b) {
        return a.compareTo(b) < 0;
    }
    
}
