package org.jg.gfx.util;

import java.awt.geom.PathIterator;
import org.jg.geom.PathIter;
import org.jg.geom.PathSegType;

/**
 *
 * @author tofar
 */
public class IterPathIterator implements PathIterator {

    private final int windingRule;
    private final PathIter iter;
    private final double[] dcoords;

    public IterPathIterator(int windingRule, PathIter iter) {
        if (iter == null) {
            throw new NullPointerException();
        }
        if (windingRule != WIND_EVEN_ODD && windingRule != WIND_NON_ZERO) {
            throw new IllegalArgumentException("Invalid winding rule : " + windingRule);
        }
        this.windingRule = windingRule;
        this.iter = iter;
        this.dcoords = new double[6];
    }

    public IterPathIterator(PathIter iter) {
        this(WIND_EVEN_ODD, iter);
    }

    @Override
    public int getWindingRule() {
        return windingRule;
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
        PathSegType ret = iter.currentSegment(dcoords);
        for (int i = 0; i < Math.min(coords.length, dcoords.length); i++) {
            coords[i] = (float) dcoords[i];
        }
        return ret.javaType;
    }

    @Override
    public int currentSegment(double[] coords) {
        return iter.currentSegment(coords).javaType;
    }

}
