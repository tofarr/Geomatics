package org.rdb.criteria.value;

import java.beans.ConstructorProperties;
import java.util.Objects;
import org.jayson.element.Element;
import org.rdb.criteria.Criteria;
import org.rdb.sorter.ValueSorter;

/**
 *
 * @author tofar
 */
public class Greater implements Criteria {

    final Element element;

    @ConstructorProperties({"value"})
    public Greater(Element element) {
        this.element = element;
    }

    public Element getValue() {
        return element;
    }

    @Override
    public boolean match(Element element) {
        return ValueSorter.INSTANCE.compare(this.element, element) > 0;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Greater) && Objects.equals(element, ((Greater) obj).element);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.element);
        return hash;
    }
}
