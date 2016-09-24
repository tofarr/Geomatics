package org.roa.attr;

import java.beans.ConstructorProperties;
import org.roa.criteria.Criteria;
import org.roa.element.Element;
import org.roa.element.ElementType;

/**
 * Attribute
 *
 * @author tofarrell
 */
public class Attr {

    private final String name;
    private final ElementType type;
    private final String title;
    private final String description;
    private final Criteria criteria;
    private final Element defaultValue;
    private final boolean generated;

    @ConstructorProperties({"name", "type", "title", "description", "criteria", "defaultValue", "generated"})
    public Attr(String name, ElementType type, String title, String description, Criteria criteria,
            Element defaultValue, boolean generated) {
        this.name = name;
        this.type = type;
        this.title = title;
        this.description = description;
        this.criteria = criteria;
        this.defaultValue = defaultValue;
        this.generated = generated;
    }

    public String getName() {
        return name;
    }

    public ElementType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    public Element getDefaultValue() {
        return defaultValue;
    }

    public boolean isGenerated() {
        return generated;
    }
}
