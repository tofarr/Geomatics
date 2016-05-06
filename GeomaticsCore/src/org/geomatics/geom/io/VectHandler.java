package org.geomatics.geom.io;

import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonOutput;
import org.jayson.JaysonType;
import org.geomatics.geom.GeomFactory;
import org.geomatics.geom.Vect;

/**
 *
 * @author tofarrell
 */
public class VectHandler extends GeomHandler<Vect> {

    public VectHandler() {
        super(Vect.CODE, Vect.class);
    }

    @Override
    public Vect parseRemaining(GeomFactory factory, JaysonInput input) throws JaysonException {
        Vect ret = factory.vect(input.nextNum(), input.nextNum());
        input.next(JaysonType.END_ARRAY);
        return ret;
    }

    @Override
    public void renderRemaining(Vect value, JaysonOutput out) throws JaysonException {
        out.num(value.x).num(value.y).endArray();
    }

}
