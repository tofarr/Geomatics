package org.geomatics.util;

import java.util.Arrays;
import org.geomatics.geom.Vect;

/**
 *
 * @author tofarrell
 */
public class ViewPoint {

    public static final ViewPoint DEFAULT = new ViewPoint(Vect.ZERO, 1, -1, null);
    private final Vect center;
    private final double resolution;
    private final double[] levels;
    private final int index;

    private ViewPoint(Vect center, double resolution, int index, double... levels) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        this.center = center;
        this.resolution = resolution;
        this.levels = levels;
        this.index = index;
    }

    public static ViewPoint valueOf(Vect center, int index, double... levels) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        if (center == null) {
            throw new NullPointerException();
        }
        levels = levels.clone();
        double prev = -1;
        for (double resolution : levels) {
            if (Double.isInfinite(resolution) || Double.isNaN(resolution) || (resolution <= 0)) {
                throw new IllegalArgumentException("Invalid resolution : " + resolution);
            }
            if (resolution <= prev) {
                throw new IllegalArgumentException("Resolutions should be unique and in ascending order : " + resolution);
            }
            prev = resolution;
        }
        return new ViewPoint(center, levels[index], index, levels);
    }

    public static ViewPoint valueOf(Vect center, double resolution) throws NullPointerException, IllegalArgumentException {
        if (center == null) {
            throw new NullPointerException();
        }
        if (Double.isInfinite(resolution) || Double.isNaN(resolution) || (resolution <= 0)) {
            throw new IllegalArgumentException("Invalid resolution : " + resolution);
        }
        return new ViewPoint(center, resolution, -1, null);
    }

    public Vect getCenter() {
        return center;
    }

    public double getResolution() {
        return resolution;
    }

    public double[] getLevels() {
        return (levels == null) ? null : levels.clone();
    }

    public int getIndex() {
        return index;
    }

    public boolean hasLevels() {
        return levels != null;
    }

    public int numLevels() {
        return (levels == null) ? 0 : levels.length;
    }

    public double levelAt(int index) throws NullPointerException {
        return levels[index];
    }

    public ViewPoint moveTo(Vect center) {
        return new ViewPoint(center, resolution, index, levels);
    }

    public ViewPoint zoomTo(double resolution) {
        if (Double.isInfinite(resolution) || Double.isNaN(resolution) || (resolution <= 0)) {
            throw new IllegalArgumentException("Invalid resolution : " + resolution);
        }
        if (levels == null) {
            return new ViewPoint(center, resolution, index, levels);
        } else {
            int _index = Math.min(levels.length, Math.abs(Arrays.binarySearch(levels, resolution)));
            return (index == _index) ? this : new ViewPoint(center, levels[_index], _index, levels);
        }
    }

    public ViewPoint zoomIn() {
        if (levels == null) {
            return new ViewPoint(center, resolution / 2, -1, levels);
        } else {
            return (index == 0) ? this : new ViewPoint(center, levels[index - 1], index - 1, levels);
        }
    }

    public ViewPoint zoomOut() {
        if (levels == null) {
            return new ViewPoint(center, resolution * 2, -1, levels);
        } else {
            return (index == levels.length - 1) ? this : new ViewPoint(center, levels[index + 1], index + 1, levels);
        }
    }

    public ViewPoint zoom(Vect anchorPx, Vect dimensions, int delta) {
        int newIndex;
        double newResolution;
        if (levels != null) {
            newIndex = Math.min(Math.max(index - delta, 0), levels.length - 1);
            newResolution = levels[newIndex];
        } else {
            newIndex = -1;
            newResolution = resolution / Math.pow(2, -delta);
        }
        
        double dx = anchorPx.x - (dimensions.x / 2);
        double dy = (dimensions.y / 2) - anchorPx.y; // Y axis is inverted
        double x = center.x + (dx * resolution) - (dx * newResolution);
        double y = center.y + (dy * resolution) - (dy * newResolution);
        
        return new ViewPoint(Vect.valueOf(x, y), newResolution, newIndex, levels);
    }

    public ViewPoint zoomOut(Vect anchor) {
        if (levels == null) {
            return new ViewPoint(center, resolution * 2, -1, levels);
        } else {
            return (index == levels.length - 1) ? this : new ViewPoint(center, levels[index + 1], index + 1, levels);
        }
    }
    
    
    public ViewPoint panPx(int xPx, int yPx){
        Vect newCenter = Vect.valueOf(center.x + (xPx * resolution), center.y + (yPx * resolution));
        return new ViewPoint(newCenter, resolution, index, levels);
    }
    
    public View build(int width, int height) throws IllegalArgumentException {
        return new View(center, resolution, width, height);
    }
}
