package org.rdb.criteria;

import java.beans.ConstructorProperties;
import org.jayson.element.Element;
import org.jayson.element.ElementType;

/**
 *
 * @author tofarrell
 */
public class TypeOf implements Criteria {

    private final ElementType type;

    @ConstructorProperties({"type"})
    public TypeOf(ElementType type) {
        this.type = type;
    }

    public ElementType getType() {
        return type;
    }

    @Override
    public boolean match(Element element) {
        ElementType t = (element == null) ? null : element.getType();
        return type == t;
    }

}
