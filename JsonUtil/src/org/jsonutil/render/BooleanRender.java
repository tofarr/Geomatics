package org.jsonutil.render;

import java.lang.reflect.Type;
import org.jsonutil.JsonCoder;
import org.jsonutil.JsonException;
import org.jsonutil.JsonOutput;

/**
 *
 * @author tofarrell
 */
public final class BooleanRender implements JsonRender<Boolean> {

    public static final BooleanRender INSTANCE = new BooleanRender();

    private BooleanRender() {
    }

    @Override
    public void render(Boolean value, JsonCoder coder, JsonOutput out) throws JsonException {
        out.bool(value);
    }

    public static class BooleanRenderFactory extends JsonRenderFactory {

        public BooleanRenderFactory() {
            super(LATE);
        }

        @Override
        public JsonRender getRenderFor(Type type) throws JsonException {
            return (type == boolean.class || type == Boolean.class) ? INSTANCE : null;
        }

    }
}
