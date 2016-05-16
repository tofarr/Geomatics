package org.lcd.filter;

import org.lcd.Result;
import org.lcd.ResultIterator;

/**
 *
 * @author tofar
 */
public class AllFilter extends Filter {

    public static final AllFilter INSTANCE = new AllFilter();

    private AllFilter() {
    }

    @Override
    public boolean match(Result result) {
        return true;
    }

    @Override
    public boolean matchCurrent(ResultIterator iter) {
        return true;
    }

}
