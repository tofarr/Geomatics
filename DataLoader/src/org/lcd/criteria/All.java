package org.lcd.criteria;

import org.jayson.parser.StaticFactory;

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
    public boolean match(Object result) {
        return true;
    }
}
