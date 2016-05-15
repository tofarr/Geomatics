package org.lcd;

import java.beans.ConstructorProperties;

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
        this.values = values.clone();
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
}
