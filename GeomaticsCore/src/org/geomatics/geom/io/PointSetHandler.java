package org.geomatics.geom.io;

import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonOutput;
import org.jayson.JaysonType;
import org.geomatics.geom.GeomFactory;
import org.geomatics.geom.PointSet;
import org.geomatics.util.VectSet;

/**
 *
 * @author tofarrell
 */
public class PointSetHandler extends GeomHandler<PointSet> {

    public PointSetHandler() {
        super(PointSet.CODE, PointSet.class);
    }

    @Override
    public PointSet parseRemaining(GeomFactory factory, JaysonInput input) throws JaysonException {
        VectSet vects = new VectSet();
        while (input.next() != JaysonType.END_ARRAY) {
            vects.add(input.num(), input.nextNum());
        }
        return factory.pointSet(vects);
    }

    @Override
    public void renderRemaining(PointSet value, JaysonOutput out) throws JaysonException {
        for (int i = 0; i < value.numPoints(); i++) {
            out.num(value.getX(i)).num(value.getY(i));
        }
        out.endArray();
    }

}
