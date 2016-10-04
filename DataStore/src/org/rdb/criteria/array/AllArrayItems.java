package org.rdb.criteria.array;

import java.beans.ConstructorProperties;
import org.jayson.element.ArrElement;
import org.jayson.element.Element;
import org.rdb.criteria.Criteria;

/**
 *
 * @author tofar
 */
public class AllArrayItems implements Criteria {

    private final Criteria criteria;

    @ConstructorProperties({"criteria"})
    public AllArrayItems(Criteria criteria) throws NullPointerException {
        if (criteria == null) {
            throw new NullPointerException("criteria must not be null");
        }
        this.criteria = criteria;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    @Override
    public boolean match(Element element) {
        if (element instanceof ArrElement) {
            ArrElement array = (ArrElement) element;
            for (Element e : array) {
                if (!criteria.match(e)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.criteria.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AllArrayItems) {
            return criteria.equals(((AllArrayItems) obj).criteria);
        }
        return false;
    }
}
