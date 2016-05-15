package org.lcd.criteria;

import org.jayson.parser.StaticFactory;
import org.lcd.Result;
import org.lcd.ResultIterator;

/**
 *
 * @author tofar
 */
public class All implements Criteria{

    public static final All INSTANCE = new All();
    
    private All() {
    }

    @StaticFactory({})
    public static All getInstance(){
        return INSTANCE;
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
