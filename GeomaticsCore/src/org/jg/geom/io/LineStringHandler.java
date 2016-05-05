package org.jg.geom.io;

import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonOutput;
import org.jayson.JaysonType;
import org.jg.geom.GeomFactory;
import org.jg.geom.LineString;
import org.jg.util.VectList;

/**
 *
 * @author tofarrell
 */
public class LineStringHandler extends GeomHandler<LineString> {

    public LineStringHandler() {
        super(LineString.CODE, LineString.class);
    }

    @Override
    public LineString parseRemaining(GeomFactory factory, JaysonInput input) throws JaysonException {
        VectList vects = new VectList();
        while (input.next() != JaysonType.END_ARRAY) {
            vects.add(input.num(), input.nextNum());
        }
        return factory.lineString(vects);
        
    }

    @Override
    public void renderRemaining(LineString value, JaysonOutput out) throws JaysonException {
        for (int i = 0; i < value.numPoints(); i++) {
            out.num(value.getX(i)).num(value.getY(i));
        }
        out.endArray();
    }


}
