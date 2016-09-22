package org.ds.element.criteria.value;

import java.beans.ConstructorProperties;
import java.util.Objects;
import org.roa.criteria.Criteria;
import org.ds.element.model.Element;
import org.ds.element.sorter.ValueSorter;

/**
 *
 * @author tofar
 */
public class LessEq implements Criteria {

    final Element element;

    @ConstructorProperties({"value"})
    public LessEq(Element element) {
        this.element = element;
    }

    public Element getValue() {
        return element;
    }

    @Override
    public boolean match(Element element) {
        return ValueSorter.INSTANCE.compare(this.element, element) <= 0;
    }
    
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof LessEq) && Objects.equals(element, ((LessEq) obj).element);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.element);
        return hash;
    }
}
