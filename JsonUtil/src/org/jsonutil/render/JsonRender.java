package org.jsonutil.render;

import org.jsonutil.JsonCoder;
import org.jsonutil.JsonException;
import org.jsonutil.JsonOutput;

/**
 *
 * @author tofarrell
 */
public interface JsonRender<E> {

    void render(E value, JsonCoder coder, JsonOutput out) throws JsonException;
}
