package org.roa.criteria;

import java.beans.ConstructorProperties;
import java.util.Objects;
import org.ds.element.model.Element;

/**
 *
 * @author tofar
 */
public class Equal implements Criteria {

    private final Element value;

    @ConstructorProperties({"value"})
    public Equal(Element value) {
        this.value = value;
    }

    public Element getValue() {
        return value;
    }

    @Override
    public boolean match(Element element) {
        return Objects.equals(this.value, value);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Equal) && Objects.equals(value, ((Equal) obj).value);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.value);
        return hash;
    }
}
