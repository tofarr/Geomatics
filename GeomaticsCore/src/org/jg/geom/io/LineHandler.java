package org.jg.geom.io;

import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonOutput;
import org.jayson.JaysonType;
import org.jg.geom.GeomFactory;
import org.jg.geom.Line;

/**
 *
 * @author tofarrell
 */
public class LineHandler extends GeomHandler<Line> {

    public LineHandler() {
        super(Line.CODE, Line.class);
    }

    @Override
    public Line parseRemaining(GeomFactory factory, JaysonInput input) throws JaysonException {
        Line ret = factory.line(input.nextNum(), input.nextNum(), input.nextNum(), input.nextNum());
        input.next(JaysonType.END_ARRAY);
        return ret;
    }

    @Override
    public void renderRemaining(Line value, JaysonOutput out) throws JaysonException {
        out.num(value.ax).num(value.ay).num(value.bx).num(value.by).endArray();
    }

}
