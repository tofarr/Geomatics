package org.geomatics.geom.io;

import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonOutput;
import org.jayson.JaysonType;
import org.geomatics.geom.GeomFactory;
import org.geomatics.geom.Rect;

/**
 *
 * @author tofarrell
 */
public class RectHandler extends GeomHandler<Rect> {

    public RectHandler() {
        super(Rect.CODE, Rect.class);
    }

    @Override
    public Rect parseRemaining(GeomFactory factory, JaysonInput input) throws JaysonException {
        Rect ret = factory.rect(input.nextNum(), input.nextNum(), input.nextNum(), input.nextNum());
        input.next(JaysonType.END_ARRAY);
        return ret;
    }

    @Override
    public void renderRemaining(Rect value, JaysonOutput out) throws JaysonException {
        out.num(value.minX).num(value.minY).num(value.maxX).num(value.maxY).endArray();
    }

}
