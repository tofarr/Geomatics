package org.jayson.render;

import java.lang.reflect.Type;
import org.jayson.AbstractPrioritized;
import org.jayson.JaysonException;

/**
 *
 * @author tofarrell
 */
public abstract class JaysonRenderFactory extends AbstractPrioritized {

    public JaysonRenderFactory(int priority) {
        super(priority);
    }

    public abstract JaysonRender getRenderFor(Type type) throws JaysonException;
}
