package org.geomatics.gv.model;

import org.geomatics.gfx.source.RenderableObjectSource;
import org.geomatics.util.View;
import org.geomatics.util.ViewPoint;

/**
 *
 * @author tofar
 */
public class GeomViewerGeomSource implements RenderableObjectSource {

    private final LayerModel[] layers;

    public GeomViewerGeomSource(LayerModel[] layers) throws NullPointerException {
        if (layers == null) {
            throw new NullPointerException();
        }
        this.layers = layers;
    }

    @Override
    public boolean load(View view, RenderableObjectProcessor processor) {
        for (LayerModel layer : layers) {
            if ((layer != null) && !layer.load(view, processor)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean load(ViewPoint viewPoint, RenderableObjectProcessor processor) {
        for (LayerModel layer : layers) {
            if ((layer != null) && !layer.load(viewPoint, processor)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object getAttributes(long renderableId) {
        int index = (int) renderableId;
        if ((index < 0) || (index > layers.length)) {
            return null;
        }
        return layers[index].getAttributes(0);
    }

}
