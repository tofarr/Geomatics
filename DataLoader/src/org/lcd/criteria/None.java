
package org.lcd.criteria;

import org.jayson.parser.StaticFactory;
import org.lcd.Result;
import org.lcd.ResultIterator;

/**
 *
 * @author tofar
 */
public class None implements Criteria{

    public static final None INSTANCE = new None();
    
    private None() {
    }

    @StaticFactory({})
    public static None getInstance(){
        return INSTANCE;
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
