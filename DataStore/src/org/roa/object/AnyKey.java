package org.roa.object;

import java.beans.ConstructorProperties;
import org.roa.criteria.Criteria;
import org.roa.element.Element;
import org.roa.element.ObjElement;

/**
 *
 * @author tofar
 */
public class AnyKey implements Criteria {

    private final Criteria criteria;

    @ConstructorProperties({"criteria"})
    public AnyKey(Criteria criteria) throws NullPointerException {
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
        if (element instanceof ObjElement) {
            ObjElement obj = (ObjElement) element;
            for (String key : obj) {
                Element e = obj.getElement(key);
                if (criteria.match(e)) {
                    return true;
                }
            }
        }
        return false;
    }
}
