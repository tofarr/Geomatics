package org.geomatics.gv.beans;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.geomatics.util.View;

/**
 *
 * @author tofarrell
 */
public final class GeomViewer {

    private final List<GeomViewerListener> listeners;
    private final List<GeomLayer> layers;
    private View view;

    @ConstructorProperties({"view", "factories"})
    public GeomViewer(View view, List<GeomLayer> layers) {
        if (view == null) {
            throw new NullPointerException("view must not be null!");
        }
        this.listeners = new ArrayList<>();
        this.layers = new ArrayList<>();
        for (GeomLayer layer : layers) {
            addLayer(layer);
        }
        this.view = view;
    }
    
    public GeomViewer(GeomViewer geomViewer){
        this.view = geomViewer.view;
        this.listeners = new ArrayList<>();
        this.layers = new ArrayList<>();
        for(GeomLayer layer : geomViewer.layers){
            this.layers.add(new GeomLayer(layer));
        }
    }

    public int numLayers() {
        return layers.size();
    }

    public GeomLayer getLayer(int index) throws IndexOutOfBoundsException {
        return layers.get(index);
    }

    public List<GeomLayer> getLayers() {
        return new ArrayList<>(layers);
    }

    public void addLayer(GeomLayer layer) {
        addLayer(layer, layers.size());
    }

    public void addLayer(GeomLayer layer, int index) throws NullPointerException {
        if (layers.contains(layer)) {
            return;
        }
        layers.add(index, layer);
        for (GeomViewerListener listener : listeners) {
            listener.layerAdded(layer, index);
        }
    }

    public void removeLayer(GeomLayer layer) throws NullPointerException {
        int index = this.layers.indexOf(layer);
        if (index >= 0) {
            layers.remove(index);
            for (GeomViewerListener listener : listeners) {
                listener.layerRemoved(layer, index);
            }
        }
    }

    public int indexOf(GeomLayer layer) {
        return layers.indexOf(layer);
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        if (view == null) {
            throw new NullPointerException("view must not be null!");
        }
        if (Objects.equals(this.view, view)) {
            return;
        }
        View old = this.view;
        this.view = view;
        for (GeomViewerListener listener : listeners) {
            listener.viewUpdated(old, view);
        }
    }

    public void addListener(GeomViewerListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GeomViewerListener listener) {
        listeners.remove(listener);
    }

    public interface GeomViewerListener {

        void viewUpdated(View old, View view);

        void layerAdded(GeomLayer layer, int index);

        void layerRemoved(GeomLayer layer, int index);
    }
}
