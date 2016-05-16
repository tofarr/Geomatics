package org.lcd.filter;

import org.lcd.Result;
import org.lcd.ResultIterator;

/**
 *
 * @author tofar
 */
public class NoneFilter extends Filter {

    public static final NoneFilter INSTANCE = new NoneFilter();

    private NoneFilter() {
    }

    @Override
    public boolean match(Result result) {
        return false;
    }

    @Override
    public boolean matchCurrent(ResultIterator iter) {
        return false;
    }

}
