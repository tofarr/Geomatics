package org.geomatics.gv;

import java.beans.ConstructorProperties;
import org.geomatics.geom.Rect;
import org.geomatics.util.ViewPoint;

/**
 *
 * @author tofarrell
 */
public class GeomViewerModel {

    private final Rect screenBounds;
    private final GeomLayer[] layers;
    private final ViewPoint viewPoint;

    @ConstructorProperties({"screenBounds", "layers", "viewPoint"})
    public GeomViewerModel(Rect screenBounds, GeomLayer[] layers, ViewPoint viewPoint) {
        this.screenBounds = screenBounds;
        this.layers = (layers == null) ? null : layers.clone();
        this.viewPoint = viewPoint;
    }

    public GeomLayer[] getLayers() {
        return (layers == null) ? null : layers.clone();
    }

    public ViewPoint getViewPoint() {
        return viewPoint;
    }

}
