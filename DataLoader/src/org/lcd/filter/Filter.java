package org.lcd.filter;

import org.lcd.Result;
import org.lcd.ResultIterator;

/**
 *
 * @author tofar
 */
public abstract class Filter {

    Filter(){   
    }
    
    public abstract boolean match(Result result);

    public abstract boolean matchCurrent(ResultIterator iter);

}
