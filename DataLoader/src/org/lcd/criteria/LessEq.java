package org.lcd.criteria;

import java.beans.ConstructorProperties;

/**
 *
 * @author tofar
 */
public class LessEq<C extends Comparable> extends Equal<C> {

    @ConstructorProperties({"value"})
    public LessEq(C value) {
        super(value);
    }  

    @Override
    protected boolean matchNull(boolean aNull, boolean bNull) {
        return (!aNull) || (aNull == bNull);
    }

    @Override
    protected boolean matchStr(String a, String b) {
        return a.compareTo(b) >= 0;
    }

    @Override
    protected boolean matchNum(double a, double b) {
        return a >= b;
    }

    @Override
    protected boolean matchObj(C a, C b) {
        return a.compareTo(b) >= 0;
    }

}
