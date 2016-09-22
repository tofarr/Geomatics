package org.roa;

/**
 *
 * @author tofarr
 * @param <T>
 */
public interface Processor<T> {

    public boolean process(T value);
}
