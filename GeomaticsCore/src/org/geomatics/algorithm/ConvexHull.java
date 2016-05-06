package org.geomatics.algorithm;

import org.geomatics.geom.Line;
import org.geomatics.geom.Network;
import org.geomatics.util.Tolerance;
import org.geomatics.util.VectList;

/**
 * Implementation of the quickhull algorithm
 *
 * @author tofarrell
 */
public class ConvexHull {

    private final Tolerance accuracy;

    public ConvexHull(Tolerance accuracy) throws NullPointerException {
        if (accuracy == null) {
            throw new NullPointerException("Accuracy must not be null!");
        }
        this.accuracy = accuracy;
    }

    /**
     * Get the convex hull of the vects given
     *
     * @param vects
     * @throws NullPointerException if vects was null
     * @return convex hull
     */
    public VectList getConvexHull(VectList vects) throws NullPointerException {
        if (vects.size() < 2) {
            return vects.clone();
        }

        //Get points with min and max X value - these must be part of the convex hull
        int minIndex = 0;
        int maxIndex = 0;
        double minX = vects.getX(0);
        double maxX = minX;
        for (int i = vects.size(); i-- > 1;) {
            double x = vects.getX(i);
            if ((x < minX) || ((x == minX) && (vects.getY(i) < vects.getY(minIndex)))) {
                minX = x;
                minIndex = i;
            }
            if (x > maxX) {
                maxX = x;
                maxIndex = i;
            }
        }

        double minY = vects.getY(minIndex);
        double maxY = vects.getY(maxIndex);

        VectList results = new VectList();

        if (minX == maxX) { // we have a point or a vertical line
            for (int i = vects.size(); i-- > 0;) {
                double y = vects.getY(i);
                if (y > maxY) {
                    maxY = y;
                }
            }
            results.add(minX, minY);
            results.add(maxX, maxY);
            results.add(minX, minY);
            return results;
        } else {
            results.add(minX, minY);
            divide(minX, minY, maxX, maxY, vects, accuracy, results);
            divide(maxX, maxY, minX, minY, vects, accuracy, results);
            return results;
        }
    }

    public VectList getConvexHull(Network network) {
        final VectList vects = new VectList(network.numVects());
        network.forEachVertex(new Network.VertexProcessor() {
            @Override
            public boolean process(double x, double y, int numLinks) {
                vects.add(x, y);
                return true;
            }
        });
        return getConvexHull(vects);
    }

    public Network getConvexHullNetwork(Network network) {
        Network ret = new Network();
        VectList ring = getConvexHull(network);
        ret.addAllLinks(ring);
        return ret;
    }

    static void divide(double ax, double ay, double bx, double by, VectList input, Tolerance accuracy, VectList output) {

        //Get only points to the right of the line...
        VectList right = new VectList();
        filterRight(ax, ay, bx, by, input, accuracy, right);

        int index = indexOfFurthestPointFromLine(ax, ay, bx, by, right);
        if (index >= 0) {
            double x = right.getX(index);
            double y = right.getY(index);
            divide(ax, ay, x, y, right, accuracy, output);
            divide(x, y, bx, by, right, accuracy, output);
        } else {
            output.add(bx, by);
        }
    }

    /**
     * Get the point from the list given furthest from the line given.
     */
    static int indexOfFurthestPointFromLine(double ax, double ay, double bx, double by, VectList input) {
        double maxValue = 0;
        int maxIndex = -1;
        for (int i = 0; i < input.size(); i++) {
            double x = input.getX(i);
            double y = input.getY(i);
            double d = Line.distLineVectSq(ax, ay, bx, by, x, y);
            if (d > maxValue) {
                maxValue = d;
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    /**
     * Identify points from input which are to the left of the line given, and place them in output
     * given
     */
    static void filterRight(double ax, double ay, double bx, double by, VectList input, Tolerance accuracy, VectList output) {
        for (int i = 0; i < input.size(); i++) {
            double x = input.getX(i);
            double y = input.getY(i);
            if (Line.counterClockwise(ax, ay, bx, by, x, y, accuracy) > 0) {
                output.add(x, y);
            }
        }
    }

}
