package org.lcd.criteria;

import java.beans.ConstructorProperties;

/**
 *
 * @author tofar
 */
public class Contains implements Criteria {
    
    private final String value;

    @ConstructorProperties({"value"})
    public Contains(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean match(Object value) {
        return (value == null) ? false : value.toString().contains(this.value);
    }
}
