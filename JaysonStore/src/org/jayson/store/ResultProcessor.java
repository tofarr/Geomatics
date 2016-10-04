
package org.jayson.store;

/**
 *
 * @author tofarr
 */
public interface ResultProcessor<E> {
    
    boolean process(E result);
}
