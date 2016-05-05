package org.jayson.render;

import java.lang.reflect.Type;
import org.jayson.AbstractPrioritized;
import org.jayson.JsonException;

/**
 *
 * @author tofarrell
 */
public abstract class JsonRenderFactory extends AbstractPrioritized {

    public JsonRenderFactory(int priority) {
        super(priority);
    }

    public abstract JsonRender getRenderFor(Type type) throws JsonException;
}
