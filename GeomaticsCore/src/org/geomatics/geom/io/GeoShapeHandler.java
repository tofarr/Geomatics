package org.geomatics.geom.io;

import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonOutput;
import org.jayson.JaysonType;
import org.geomatics.geom.Area;
import org.geomatics.geom.GeoShape;
import org.geomatics.geom.GeomFactory;
import org.geomatics.geom.LineSet;
import org.geomatics.geom.PointSet;

/**
 *
 * @author tofarrell
 */
public class GeoShapeHandler extends GeomHandler<GeoShape> {

    private final AreaHandler areaHandler;
    private final LineSetHandler lineSetHandler;
    private final PointSetHandler pointSetHandler;
    
    public GeoShapeHandler() {
        super(GeoShape.CODE, GeoShape.class);
        areaHandler = new AreaHandler();
        lineSetHandler = new LineSetHandler();
        pointSetHandler = new PointSetHandler();
    }

    @Override
    public GeoShape parseRemaining(GeomFactory factory, JaysonInput input) throws JaysonException {
        JaysonType type = input.next();
        final Area area;
        switch(type){
            case NULL:
                area = null;
                break;
            case BEGIN_ARRAY:
                area = areaHandler.parseRemaining(factory, input);
                break;
            default:
                throw new JaysonException("Unexpected type : "+type);
        }
        final LineSet lineSet;
        switch(type){
            case NULL:
                lineSet = null;
                break;
            case BEGIN_ARRAY:
                lineSet = lineSetHandler.parseRemaining(factory, input);
                break;
            default:
                throw new JaysonException("Unexpected type : "+type);
        }
        final PointSet pointSet;
        switch(type){
            case NULL:
                pointSet = null;
                break;
            case BEGIN_ARRAY:
                pointSet = pointSetHandler.parseRemaining(factory, input);
                break;
            default:
                throw new JaysonException("Unexpected type : "+type);
        }
        return factory.geoShape(area, lineSet, pointSet);
    }

    @Override
    public void renderRemaining(GeoShape value, JaysonOutput out) throws JaysonException {
        if(value.area == null){
            out.nul();
        }else{
            out.beginArray();
            areaHandler.renderRemaining(value.area, out);
        }
        if(value.lines == null){
            out.nul();
        }else{
            out.beginArray();
            lineSetHandler.renderRemaining(value.lines, out);
        }
        if(value.points == null){
            out.nul();
        }else{
            out.beginArray();
            pointSetHandler.renderRemaining(value.points, out);
        }
        out.endArray();
    }
}
