package org.jsonutil.render;

import java.lang.reflect.Type;
import org.jsonutil.AbstractPrioritized;
import org.jsonutil.JsonException;

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
