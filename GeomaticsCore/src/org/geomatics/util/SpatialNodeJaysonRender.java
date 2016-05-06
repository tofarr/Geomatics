package org.geomatics.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonOutput;
import org.jayson.render.JaysonRender;
import org.jayson.render.JaysonRenderFactory;

/**
 *
 * @author tofarrell
 */
public class SpatialNodeJaysonRender implements JaysonRender<SpatialNode> {

    public static final SpatialNodeJaysonRender INSTANCE = new SpatialNodeJaysonRender();

    public SpatialNodeJaysonRender() {
    }

    @Override
    public void render(SpatialNode node, Jayson coder, JaysonOutput out) throws JaysonException {
        out.beginObject();
        if (node.isBranch()) {
            out.name("a");
            render(node.getA(), coder, out);
            out.name("b");
            render(node.getB(), coder, out);
        } else {
            out.name("itemBounds").beginArray();
            for (int i = 0; i < node.size(); i++) {
                coder.render(node.getItemBounds(i), out);
            }
            out.endArray();
            out.name("itemValues").beginArray();
            for (int i = 0; i < node.size(); i++) {
                coder.render(node.getItemValue(i), out);
            }
            out.endArray();
        }
        out.endObject();
    }

    public static class SpatialNodeRenderFactory extends JaysonRenderFactory {

        public SpatialNodeRenderFactory(int priority) {
            super(priority);
        }

        @Override
        public JaysonRender getRenderFor(Type type) {
            if (type instanceof Class) {
                Class clazz = (Class) type;
                if (SpatialNode.class.isAssignableFrom(clazz)) {
                    return INSTANCE;
                }
            }
            return null;
        }
    }
}
