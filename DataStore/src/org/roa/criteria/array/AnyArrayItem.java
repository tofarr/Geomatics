package org.roa.criteria.array;

import java.beans.ConstructorProperties;
import org.roa.criteria.Criteria;
import org.roa.element.ArrElement;
import org.roa.element.Element;

/**
 *
 * @author tofar
 */
public class AnyArrayItem implements Criteria {

    private final Criteria criteria;

    @ConstructorProperties({"criteria"})
    public AnyArrayItem(Criteria criteria) throws NullPointerException {
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
                if (criteria.match(e)) {
                    return true;
                }
            }
            return false;
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
        if (obj instanceof AnyArrayItem) {
            return criteria.equals(((AnyArrayItem) obj).criteria);
        }
        return false;
    }
}
