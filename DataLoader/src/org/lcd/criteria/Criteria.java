package org.lcd.criteria;

/**
 *
 * @author tofar
 */
public interface Criteria<E> {

    public boolean match(E value);
}
