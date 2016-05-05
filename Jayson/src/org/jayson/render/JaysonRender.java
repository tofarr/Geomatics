package org.jayson.render;

import org.jayson.Jayson;
import org.jayson.JsonException;
import org.jayson.JsonOutput;

/**
 *
 * @author tofarrell
 */
public interface JsonRender<E> {

    void render(E value, Jayson coder, JsonOutput out) throws JsonException;
}
