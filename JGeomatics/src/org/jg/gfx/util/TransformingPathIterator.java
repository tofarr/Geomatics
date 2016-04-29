package org.jg.gfx.util;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

/**
 *
 * @author tofarrell
 */
public class TransformingPathIterator implements PathIterator {

    final PathIterator iter;
    final AffineTransform transform;

    private TransformingPathIterator(PathIterator iter, AffineTransform transform) {
        this.iter = iter;
        this.transform = transform;
    }
    
    public static PathIterator valueOf(PathIterator iter, AffineTransform transform){
        if((transform == null) || transform.isIdentity()){
            return iter;
        }
        return new TransformingPathIterator(iter, transform);
    }

    @Override
    public int getWindingRule() {
        return iter.getWindingRule();
    }

    @Override
    public boolean isDone() {
        return iter.isDone();
    }

    @Override
    public void next() {
        iter.next();
    }

    @Override
    public int currentSegment(float[] coords) {
        int ret = iter.currentSegment(coords);
        switch (ret) {
            case SEG_CLOSE:
            case SEG_LINETO:
            case SEG_MOVETO:
                transform.transform(coords, 0, coords, 0, 1);
                break;
            case SEG_QUADTO:
                transform.transform(coords, 0, coords, 0, 2);
                break;
            case SEG_CUBICTO:
                transform.transform(coords, 0, coords, 0, 3);
                break;
        }
        return ret;
    }

    @Override
    public int currentSegment(double[] coords) {
        int ret = iter.currentSegment(coords);
        switch (ret) {
            case SEG_CLOSE:
            case SEG_LINETO:
            case SEG_MOVETO:
                transform.transform(coords, 0, coords, 0, 1);
                break;
            case SEG_QUADTO:
                transform.transform(coords, 0, coords, 0, 2);
                break;
            case SEG_CUBICTO:
                transform.transform(coords, 0, coords, 0, 3);
                break;
        }
        return ret;
    }

}
