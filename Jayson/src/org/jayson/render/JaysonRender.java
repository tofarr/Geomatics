package org.jayson.render;

import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonOutput;

/**
 *
 * @author tofarrell
 */
public interface JaysonRender<E> {

    void render(E value, Jayson coder, JaysonOutput out) throws JaysonException;
}
