package org.rdb.criteria.array;

import java.beans.ConstructorProperties;
import java.util.Objects;
import org.jayson.element.ArrElement;
import org.jayson.element.Element;
import org.rdb.criteria.Criteria;

/**
 *
 * @author tofar
 */
public class ArrayItem implements Criteria {

    private final int index;
    private final Criteria criteria;

    @ConstructorProperties({"index", "criteria"})
    public ArrayItem(int index, Criteria criteria) throws NullPointerException, IllegalArgumentException {
        if (index < 0) {
            throw new IllegalArgumentException("Invalid index : " + index);
        }
        if (criteria == null) {
            throw new NullPointerException("criteria must not be null");
        }
        this.index = index;
        this.criteria = criteria;
    }

    public int getIndex() {
        return index;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    @Override
    public boolean match(Element element) {
        if (element instanceof ArrElement) {
            ArrElement array = (ArrElement) element;
            if(array.size() > index){
                element = array.valueAt(index);
                return criteria.match(element);
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArrayItem) {
            final ArrayItem a = (ArrayItem)obj;
            return (index == a.index) && criteria.equals(a.criteria);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + this.index;
        hash = 67 * hash + Objects.hashCode(this.criteria);
        return hash;
    }
}
