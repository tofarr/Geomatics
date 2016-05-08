package org.geomatics.gv.model;

/**
 * Non serializable object
 * @author tofar
 */
public class LayerViewModel {

    public final String path;
    public final LayerModel layer;

    public LayerViewModel(String path, LayerModel layer) {
        this.path = path;
        this.layer = layer;
    }

}
