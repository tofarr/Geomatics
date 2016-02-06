package org.jg;

/**
 * Generic callback interface
 *
 * @author tofar_000
 * @param <E>
 */
public interface Processor<E> {

    /**
     * Process the value given
     * @param value
     * @return
     */
    boolean process(E value);
}
