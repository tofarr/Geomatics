package org.jg.geom.io;

import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonOutput;
import org.jg.geom.GeomFactory;
import org.jg.geom.Path;
import org.jg.geom.PathIter;

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
        
    }

    @Override
    public void renderRemaining(Path path, JaysonOutput out) throws JaysonException {
        PathIter iter = path.iterator();
        double[] coords = new double[6];
        PathSegType prev = 
        while(!iter.isDone()){
            PathSegType type = iter.currentSegment(coords);
            switch(){                   
                case MOVE:
                    
                case LINE:
                    
                case CLOSE:
                    
                case CUBIC:
                    
                case QUAD:
                    
            }
            prev = type;
        }
        out.endArray();
    }

}
