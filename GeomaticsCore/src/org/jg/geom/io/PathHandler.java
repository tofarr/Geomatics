package org.jg.geom.io;

import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonOutput;
import org.jayson.JaysonType;
import org.jg.geom.GeomFactory;
import org.jg.geom.Path;
import org.jg.geom.PathBuilder;
import org.jg.geom.PathIter;
import org.jg.geom.PathSegType;

/**
 *
 * @author tofarrell
 */
public class PathHandler extends GeomHandler<Path> {

    public PathHandler() {
        super(Path.CODE, Path.class);
    }

    @Override
    public Path parseRemaining(GeomFactory factory, JaysonInput input) throws JaysonException {
        PathBuilder builder = new PathBuilder();
        PathSegType seg = PathSegType.MOVE;
        while(true){
            JaysonType type = input.next();
            switch(type){
                case STRING:
                    seg = PathSegType.fromCode(input.str().charAt(0));
                    break;
                case NUMBER:
                    switch(seg){
                        case MOVE:
                            builder.moveToOrds(input.num(), input.nextNum());
                            break;
                        case LINE:
                            builder.lineToOrds(input.num(), input.nextNum());
                            break;
                        case QUAD:
                            builder.quadToOrds(input.num(), input.nextNum(), input.nextNum(), input.nextNum());
                            break;
                        case CUBIC:
                            builder.cubicToOrds(input.num(), input.nextNum(), input.nextNum(), input.nextNum(), input.nextNum(), input.nextNum());
                            break;
                        case CLOSE:
                            builder.close();
                            break;
                    }
                case END_ARRAY:
                    Path path = builder.build();
                    return path;
                default:
                    throw new JaysonException("Unexpected type : "+type);
            }
        }
    }

    @Override
    public void renderRemaining(Path path, JaysonOutput out) throws JaysonException {
        PathIter iter = path.iterator();
        double[] coords = new double[6];
        PathSegType prev = null;
        while(!iter.isDone()){
            PathSegType type = iter.currentSegment(coords);
            if(type != prev){
                out.name(Character.toString(type.code));
            }
            switch(type){                   
                case MOVE:
                case LINE:
                    out.num(coords[0]).num(coords[1]);
                    break;
                case CUBIC:
                    out.num(coords[0]).num(coords[1]);
                    out.num(coords[2]).num(coords[3]);
                    out.num(coords[4]).num(coords[5]);
                    break;
                case QUAD:
                    out.num(coords[0]).num(coords[1]);
                    out.num(coords[2]).num(coords[3]);
                    break;
            }
            prev = type;
        }
        out.endArray();
    }

}
