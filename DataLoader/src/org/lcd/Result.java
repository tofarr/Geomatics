package org.lcd;

import java.beans.ConstructorProperties;
import java.util.Arrays;
import java.util.Objects;
import org.lcd.criteria.Criteria;

/**
 *
 * @author tofar
 */
public class Result {

    private final AttrSet attrs;
    private final Object[] values;

    @ConstructorProperties({"attrs", "values"})
    public Result(AttrSet attrs, Object[] values) {
        this.attrs = attrs;
        this.values = new Object[values.length];
        for(int v = values.length; v-- > 0;){
            Object value = values[v];
            if(value != null){
                Attr attr = attrs.getAttr(v);
                Class<?> type = attr.getType();
                if(!type.isAssignableFrom(value.getClass())){
                   throw new ClassCastException(type+" cannot be assigned from "+value);
                }
                if(attr.getCriteria() != null){
                    if(!attr.getCriteria().match(value)){
                        throw new IllegalArgumentException("Value for "+attr.getName()+" does not match criteria!");
                    }
                }
                this.values[v] = value;
            }
        }
        for(Attr attr : attrs){
            Criteria criteria = attr.getCriteria();
            if((criteria != null) && !(criteria.match(this))){
                throw new IllegalArgumentException(attr.getName()+" criteria failed not match criteria!");
            }
        }
    }

    public Result(ResultIterator iter) {
        this.attrs = iter.getAttrs();
        this.values = new Object[attrs.numAttrs()];
        for (int i = 0; i < values.length; i++) {
            this.values[i] = iter.getByIndex(i);
        }
    }

    public AttrSet getAttrs() {
        return attrs;
    }

    public Object getByName(String name) throws IllegalArgumentException {
        int index = attrs.indexOf(name);
        if (index < 0) {
            throw new IllegalArgumentException("Unknown attribute : " + name);
        }
        return values[index];
    }

    public Object getByIndex(int index) throws IndexOutOfBoundsException {
        return values[index];
    }
    
    public Result filter(AttrSet attrs){
        if(attrs.equals(this.attrs)){
            return this;
        }
        Object[] values = new Object[attrs.numAttrs()];
        for(int i = attrs.numAttrs(); i-- > 0;){
            Attr newAttr = attrs.getAttr(i);
            values[i] = getByName(newAttr.getName());
        }
        return new Result(attrs, values);
    }
    
    public Result copyFrom(Result result){
        Object[] values = new Object[attrs.numAttrs()];
        for(int i = attrs.numAttrs(); i-- > 0;){
            Attr newAttr = attrs.getAttr(i);
            values[i] = result.getByName(newAttr.getName());
        }
        return new Result(attrs, values);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.attrs);
        hash = 59 * hash + Arrays.deepHashCode(this.values);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Result)) {
            return false;
        }
        final Result other = (Result) obj;
        return Objects.equals(this.attrs, other.attrs) && Arrays.deepEquals(this.values, other.values);
    }
    
    
}
