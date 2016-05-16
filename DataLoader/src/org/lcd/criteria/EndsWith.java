package org.lcd.criteria;

import java.beans.ConstructorProperties;

/**
 *
 * @author tofar
 */
public class EndsWith implements Criteria {
    
    private final String value;

    @ConstructorProperties({"value"})
    public EndsWith(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean match(Object value) {
        return (value == null) ? false : value.toString().endsWith(this.value);
    }
}