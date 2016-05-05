package org.jayson.render;

import java.lang.reflect.Type;
import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonOutput;

/**
 *
 * @author tofarrell
 */
public final class BooleanRender implements JaysonRender<Boolean> {

    public static final BooleanRender INSTANCE = new BooleanRender();

    private BooleanRender() {
    }

    @Override
    public void render(Boolean value, Jayson coder, JaysonOutput out) throws JaysonException {
        out.bool(value);
    }

    public static class BooleanRenderFactory extends JaysonRenderFactory {

        public BooleanRenderFactory(int priority){
            super(priority);
        }

        @Override
        public JaysonRender getRenderFor(Type type) throws JaysonException {
            return (type == boolean.class || type == Boolean.class) ? INSTANCE : null;
        }

    }
}
