package org.ds;

/**
 *
 * @author tofarr
 */
public interface Processor<T> {
    
    public boolean process(T value);
}
