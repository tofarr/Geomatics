package org.rdb.criteria.object;

import java.beans.ConstructorProperties;
import org.jayson.element.Element;
import org.jayson.element.ObjElement;
import org.rdb.criteria.Criteria;

/**
 *
 * @author tofar
 */
public class Key implements Criteria {

    private String key;
    private Criteria criteria;

    @ConstructorProperties({"key", "criteria"})
    public Key(String key, Criteria criteria) {
        if (key == null) {
            throw new NullPointerException("key must not be null!");
        }
        if (criteria == null) {
            throw new NullPointerException("criteria must not be null!");
        }
        this.key = key;
        this.criteria = criteria;
    }

    public String getKey() {
        return key;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    @Override
    public boolean match(Element element) {
        if (element instanceof ObjElement) {
            ObjElement obj = (ObjElement) element;
            element = obj.getElement(key);
            return criteria.match(element);
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Key) {
            final Key a = (Key)obj;
            return key.equals(a.key) && criteria.equals(a.criteria);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + key.hashCode();
        hash = 67 * hash + criteria.hashCode();
        return hash;
    }
}
