package org.lcd.criteria;

import org.lcd.Result;
import org.lcd.ResultIterator;

/**
 *
 * @author tofar
 */
public interface Criteria {

    public boolean match(Result result);

    public boolean matchCurrent(ResultIterator iter);
}
