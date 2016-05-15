package org.lcd.criteria;

import org.jayson.parser.StaticFactory;
import org.lcd.Result;
import org.lcd.ResultIterator;

/**
 *
 * @author tofar
 */
public class Not implements Criteria {

    private final Criteria criteria;

    private Not(Criteria criteria) throws NullPointerException {
        this.criteria = criteria;
    }

    @StaticFactory({"criteria"})
    public static Criteria valueOf(Criteria criteria) {
        if (criteria == null) {
            throw new NullPointerException("criteria must not be null");
        } else if (criteria instanceof Not) {
            return ((Not) criteria).criteria;
        } else {
            return new Not(criteria);
        }
    }

    public Criteria getCriteria() {
        return criteria;
    }

    @Override
    public boolean match(Result result) {
        return !criteria.match(result);
    }

    @Override
    public boolean matchCurrent(ResultIterator iter) {
        return !criteria.matchCurrent(iter);
    }
}
