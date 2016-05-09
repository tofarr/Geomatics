package org.geomatics.gv.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.geomatics.geom.Rect;
import org.geomatics.util.ViewPoint;
import org.jayson.parser.StaticFactory;

/**
 *
 * @author tofarrell
 */
public class GeomViewerModel {

    private static final Logger LOG = Logger.getLogger(GeomViewerModel.class.getName());

    private final Rect bounds;
    private final ViewPoint viewPoint;
    private final List<String> paths;
    private final String defaultPath;

    private GeomViewerModel(Rect bounds, ViewPoint viewPoint, List<String> paths, String defaultPath) {
        this.bounds = bounds;
        this.viewPoint = viewPoint;
        this.paths = paths;
        this.defaultPath = defaultPath;
    }

    @StaticFactory({"bounds", "viewPoint", "defaultPath", "paths"})
    public static GeomViewerModel valueOf(Rect bounds, ViewPoint viewPoint, String defaultPath, String... paths) throws NullPointerException {
        if (bounds == null) {
            throw new NullPointerException("bounds must not be null");
        }
        if (viewPoint == null) {
            throw new NullPointerException("viewPoint must not be null");
        }
        List<String> pathList = new ArrayList<>();
        for (String path : paths) {
            if (pathList.contains(path)) {
                LOG.info("Duplicate path in index : " + path);
            } else if(path != null) {
                File file = new File(path);
                if (file.exists() && file.canRead()) {
                    pathList.add(path);
                } else {
                    LOG.info("Unreadable path in index " + path);
                }
            }
        }
        return new GeomViewerModel(bounds, viewPoint, Collections.unmodifiableList(pathList), defaultPath);
    }

    public final int numPaths() {
        return paths.size();
    }

    public String getPath(int index) throws IndexOutOfBoundsException {
        return paths.get(index);
    }

    public Rect getBounds() {
        return bounds;
    }

    public ViewPoint getViewPoint() {
        return viewPoint;
    }

    public List<String> getPaths() {
        return paths;
    }

    public String getDefaultPath() {
        return defaultPath;
    }

    public GeomViewerModel withBounds(Rect bounds) {
        if (bounds == null) {
            throw new NullPointerException("bounds must not be null");
        }
        return new GeomViewerModel(bounds, viewPoint, paths, defaultPath);
    }

    public GeomViewerModel withViewPoint(ViewPoint viewPoint) {
        if (viewPoint == null) {
            throw new NullPointerException("viewPoint must not be null");
        }
        return new GeomViewerModel(bounds, viewPoint, paths, defaultPath);
    }

    public GeomViewerModel withExtraPath(String path) {
        int index = paths.indexOf(path);
        if (index >= 0) {
            return this;
        }
        ArrayList<String> newPaths = new ArrayList<>(paths);
        newPaths.add(path);
        return new GeomViewerModel(bounds, viewPoint, Collections.unmodifiableList(newPaths), defaultPath);
    }

    public GeomViewerModel withoutPath(String path) {
        int index = paths.indexOf(path);
        if (index < 0) {
            return this;
        }
        ArrayList<String> newPaths = new ArrayList<>(paths);
        newPaths.remove(index);
        return new GeomViewerModel(bounds, viewPoint, Collections.unmodifiableList(newPaths), defaultPath);
    }

    public int indexOf(String path) {
        return paths.indexOf(path);
    }

    public List<String> getPathsNotInOther(GeomViewerModel other) {
        ArrayList<String> newPaths = new ArrayList<>(paths);
        newPaths.removeAll(other.paths);
        return newPaths;
    }
}
