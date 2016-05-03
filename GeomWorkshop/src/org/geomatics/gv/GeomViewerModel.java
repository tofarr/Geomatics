package org.geomatics.gv;

import java.beans.ConstructorProperties;
import org.jg.geom.Rect;
import org.jg.util.ViewPoint;

/**
 *
 * @author tofarrell
 */
public class GeomViewerModel {

    private final Rect screenBounds;
    private final GeomLayer[] layers;
    private final ViewPoint viewPoint;

    @ConstructorProperties({"screenBounds","layers","viewPoint"})
    public GeomViewerModel(Rect screenBounds, GeomLayer[] layers, ViewPoint viewPoint) {
        this.screenBounds = screenBounds;
        this.layers = layers;
        this.viewPoint = viewPoint;
    }

    public Rect getScreenBounds() {
        return screenBounds;
    }

    public GeomLayer[] getLayers() {
        return layers;
    }

    public ViewPoint getViewPoint() {
        return viewPoint;
    }

}
