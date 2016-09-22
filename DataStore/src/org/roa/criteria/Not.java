package org.roa.criteria;

import org.ds.element.model.Element;
import org.jayson.parser.StaticFactory;

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
        } else if(criteria == All.INSTANCE){
            return None.INSTANCE;
        } else if (criteria == None.INSTANCE) {
            return All.INSTANCE;
        } else {
            return new Not(criteria);
        }
    }

    public Criteria getCriteria() {
        return criteria;
    }

    @Override
    public boolean match(Element element) {
        return !criteria.match(element);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + criteria.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Not){
            return criteria.equals(((Not)obj).criteria);
        }
        return false;
    }    
}
