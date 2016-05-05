package org.jg.geom.io;

import java.util.ArrayList;
import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonOutput;
import org.jayson.JaysonType;
import org.jg.geom.GeomFactory;
import org.jg.geom.LineSet;
import org.jg.geom.LineString;
import org.jg.util.VectList;

/**
 *
 * @author tofarrell
 */
public class LineSetHandler extends GeomHandler<LineSet> {

    public LineSetHandler() {
        super(LineSet.CODE, LineSet.class);
    }

    @Override
    public LineSet parseRemaining(GeomFactory factory, JaysonInput input) throws JaysonException {
        ArrayList<LineString> lineStrings = new ArrayList();
        while(true){
            JaysonType type = input.next();
            switch(type){
                case BEGIN_ARRAY:
                    VectList vects = new VectList();
                    while (input.next() != JaysonType.END_ARRAY) {
                        vects.add(input.num(), input.nextNum());
                    }
                    lineStrings.add(factory.lineString(vects));
                    break;
                case END_ARRAY:
                    LineSet ret = factory.lineSet(lineStrings);
                    return ret;
                default:
                    throw new JaysonException("Expected BEGIN_ARRAY|END_ARRAY, found "+type);
            }
        }
    }

    @Override
    public void renderRemaining(LineSet value, JaysonOutput out) throws JaysonException {
        for(int i = 0; i < value.numLineStrings(); i++){
            LineString lineString = value.getLineString(i);
            out.beginArray();
            for (int j = 0; j < lineString.numPoints(); j++) {
                out.num(lineString.getX(j)).num(lineString.getY(j));
            }
            out.endArray();
        }
        out.endArray();
    }
}
