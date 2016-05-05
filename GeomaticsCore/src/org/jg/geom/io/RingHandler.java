package org.jg.geom.io;

import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonOutput;
import org.jayson.JaysonType;
import org.jg.geom.GeomFactory;
import org.jg.geom.Ring;
import org.jg.util.VectList;

/**
 *
 * @author tofarrell
 */
public class RingHandler extends GeomHandler<Ring> {

    public RingHandler() {
        super(Ring.CODE, Ring.class);
    }

    @Override
    public Ring parseRemaining(GeomFactory factory, JaysonInput input) throws JaysonException {
        VectList vects = new VectList();
        while (input.next() != JaysonType.END_ARRAY) {
            vects.add(input.num(), input.nextNum());
        }
        return factory.ring(vects);

    }

    @Override
    public void renderRemaining(Ring value, JaysonOutput out) throws JaysonException {
        for (int i = 0; i < value.numPoints(); i++) {
            out.num(value.getX(i)).num(value.getY(i));
        }
        out.endArray();
    }

}
