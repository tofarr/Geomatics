package org.geomatics.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonOutput;
import org.jayson.JaysonType;
import org.jayson.parser.JaysonParser;
import org.jayson.parser.JaysonParserFactory;
import org.jayson.render.JaysonRender;
import org.jayson.render.JaysonRenderFactory;
import org.geomatics.geom.Rect;

/**
 *
 * @author tofar
 */
public class SpatialNodeJaysonParser extends JaysonParser<SpatialNode> {

    private Type contentType;

    public SpatialNodeJaysonParser(Type contentType) {
        if (contentType == null) {
            throw new NullPointerException();
        }
        this.contentType = contentType;
    }

    @Override
    public SpatialNode parse(JaysonType type, Jayson coder, JaysonInput input) throws JaysonException {
        if (type != JaysonType.BEGIN_OBJECT) {
            throw new JaysonException("Expected BEGIN_OBJECT found " + type);
        }
        SpatialNode a = null;
        SpatialNode b = null;
        Rect[] itemBounds = null;
        Object[] itemValues = null;
        while (true) {
            type = input.next();
            switch (type) {
                case NAME:
                    String name = input.str();
                    switch (name) {
                        case "a":
                            a = parse(coder, input);
                            break;
                        case "b":
                            b = parse(coder, input);
                            break;
                        case "itemBounds":
                            itemBounds = coder.parse(Rect[].class, input);
                            break;
                        case "values":
                            List itemValueList = new ArrayList();
                            while (true) {
                                type = input.next();
                                if (type == JaysonType.END_ARRAY) {
                                    itemValues = itemValueList.toArray();
                                    break;
                                } else {
                                    itemValueList.add(coder.parse(contentType, type, input));
                                }
                            }
                            break;
                        default:
                            input.skip();

                    }
                case END_OBJECT:
                    if (a != null) {
                        if (b == null) {
                            throw new JaysonException("Defined a without b!");
                        }
                        if (itemBounds != null) {
                            throw new JaysonException("Defined itemBounds on branch!");
                        }
                        if (itemValues != null) {
                            throw new JaysonException("Defined itemValues on branch!");
                        }
                        return new SpatialNode(a, b);
                    } else if (b != null) {
                        throw new JaysonException("Defined b without a!");
                    } else {
                        return new SpatialNode(itemBounds, itemValues);
                    }
            }
        }
    }

    public static class SpatialNodeParserFactory extends JaysonParserFactory {

        public SpatialNodeParserFactory(int priority) {
            super(priority);
        }

        @Override
        public JaysonParser getParserFor(Type type) {
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                Type rawType = pt.getRawType();
                if (rawType instanceof Class) {
                    Class collectionType = (Class) rawType;
                    if (SpatialNode.class.isAssignableFrom(collectionType)) {
                        return new SpatialNodeJaysonParser(pt.getActualTypeArguments()[0]);
                    }
                }
            }
            return null;
        }
    }

}
