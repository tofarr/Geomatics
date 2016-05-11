package org.om.criteria.value;

import java.beans.ConstructorProperties;
import org.om.criteria.Criteria;
import org.om.element.Element;
import org.om.element.ValueElement;

/**
 *
 * @author tofar
 */
public class Greater<C extends Comparable> implements Criteria {

    final C value;

    @ConstructorProperties({"value"})
    public Greater(C value) {
        this.value = value;
    }

    public C getValue() {
        return value;
    }

    @Override
    public boolean match(Element element) {
        if(element == null){
            return (value != null);
        }
        if (element instanceof ValueElement) {
            ValueElement<?> v = (ValueElement) element;
            if (value == null) {
                return false; // null is considered last
            }
            return value.compareTo(v.getValue()) > 0;
        }
        return false;
    }

}
