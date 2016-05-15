package org.lcd;

import java.beans.Transient;
import java.util.Objects;
import org.lcd.criteria.Criteria;
import org.jayson.parser.StaticFactory;

/**
 *
 * @author tofar
 */
public final class Attr<E> {

    private final String name;
    private final String title;
    private final String description;
    private final Criteria criteria;
    private final Class<E> type;
    private final E defaultValue;

    public Attr(String name, String title, String description, Criteria criteria, Class<E> type, E defaultValue) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name must not be null!");
        }
        if(type == null){
            throw new NullPointerException("Attr must have a type!");
        }
        if(title == null){
            title = name;
        }
        this.name = name;
        this.title = title;
        this.description = description;
        this.criteria = criteria;
        this.type = type;
        this.defaultValue = defaultValue;
    }
    
    @StaticFactory({"name", "title", "description", "criteria", "typeName", "defaultValue"})
    public static <E> Attr<E> valueOf(String name, String title, String description, Criteria criteria, String typeName, E defaultValue) throws NullPointerException, IllegalArgumentException {
        try {
            Class type = (Class) Class.forName(typeName);
            return new Attr(name, title, description, criteria, type, title);
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Invalid typeName : " + typeName, ex);
        }
    }


    public String getName() {
        return name;
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

    @Transient
    public Class<E> getType() {
        return type;
    }

    public String getTypeName() {
        return type.getName();
    }

    public E getDefaultValue() {
        return defaultValue;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.name);
        hash = 79 * hash + Objects.hashCode(this.title);
        hash = 79 * hash + Objects.hashCode(this.description);
        hash = 79 * hash + Objects.hashCode(this.criteria);
        hash = 79 * hash + Objects.hashCode(this.type);
        hash = 79 * hash + Objects.hashCode(this.defaultValue);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Attr)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        final Attr<?> other = (Attr<?>) obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.title, other.title)
                && Objects.equals(this.description, other.description)
                && Objects.equals(this.criteria, other.criteria)
                && Objects.equals(this.type, other.type)
                && Objects.equals(this.defaultValue, other.defaultValue);
    }
}
