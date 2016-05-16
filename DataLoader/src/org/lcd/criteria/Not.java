package org.lcd.criteria;

import org.jayson.parser.StaticFactory;
import org.lcd.Result;
import org.lcd.ResultIterator;

/**
 *
 * @author tofar
 */
public class Not<E> implements Criteria<E> {

    private final Criteria<E> criteria;

    private Not(Criteria<E> criteria) throws NullPointerException {
        this.criteria = criteria;
    }

    @StaticFactory({"criteria"})
    public static <E> Criteria<E> valueOf(Criteria<E> criteria) {
        if (criteria == null) {
            throw new NullPointerException("criteria must not be null");
        } else if (criteria instanceof Not) {
            return ((Not<E>) criteria).criteria;
        } else if(criteria == All.INSTANCE){
            return None.INSTANCE;
        } else if (criteria == None.INSTANCE) {
            return All.INSTANCE;
        } else {
            return new Not<>(criteria);
        }
    }
    
    public Criteria getCriteria() {
        return criteria;
    }

    @Override
    public boolean match(E value) {
        return !criteria.match(value);
    }
}
