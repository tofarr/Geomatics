package org.om.criteria.array;

import java.beans.ConstructorProperties;
import org.om.criteria.Criteria;
import org.om.element.ArrElement;
import org.om.element.Element;
import org.om.element.NumElement;

/**
 *
 * @author tofar
 */
public class ArraySize implements Criteria {

    private final Criteria criteria;

    @ConstructorProperties({"criteria"})
    public ArraySize(Criteria criteria) throws NullPointerException {
        if(criteria == null){
            throw new NullPointerException("criteria must not be null");
        }
        this.criteria = criteria;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    @Override
    public boolean match(Element element) {
        if(element instanceof ArrElement){
            int size = ((ArrElement)element).size();
            NumElement num = NumElement.valueOf(size);
            return criteria.match(element);
        }
        return false;
    }
}
