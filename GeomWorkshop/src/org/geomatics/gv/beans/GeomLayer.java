package org.geomatics.gv.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.geomatics.geom.Geom;
import org.geomatics.gfx.fill.Fill;
import org.geomatics.gfx.outline.Outline;
import org.geomatics.gfx.renderable.Renderable;

/**
 *
 * @author tofarrell
 */
public class GeomLayer {

    private final List<GeomLayerListener> listeners;
    private String title;
    private Geom geom;
    private Fill fill;
    private Fill outlineFill;
    private Outline outline;
    private Renderable symbol;

    public GeomLayer() {
        listeners = new ArrayList<>();
    }
    
    public GeomLayer(GeomLayer layer){
        this();
        synchronized(layer){
            this.title = layer.title;
            this.geom = layer.geom;
            this.fill = layer.fill;
            this.outlineFill = layer.outlineFill;
            this.outline = layer.outline;
            this.symbol = layer.symbol;
        }
    }
    
    public synchronized String getTitle() {
        return title;
    }

    public synchronized void setTitle(String title) {
        if (!Objects.equals(this.title, title)) {
            String old = this.title;
            this.title = title;
            for (GeomLayerListener listener : listeners) {
                listener.titleChanged(this, old, title);
            }
        }
    }

    public synchronized Geom getGeom() {
        return geom;
    }

    public synchronized void setGeom(Geom geom) {
        if (!Objects.equals(this.geom, geom)) {
            Geom old = this.geom;
            this.geom = geom;
            for (GeomLayerListener listener : listeners) {
                listener.geomChanged(this, old, geom);
            }
        }
    }

    public synchronized Fill getFill() {
        return fill;
    }

    public synchronized void setFill(Fill fill) {
        if (!Objects.equals(this.fill, fill)) {
            this.fill = fill;
            for (GeomLayerListener listener : listeners) {
                listener.styleChanged(this);
            }
        }
    }

    public synchronized Fill getOutlineFill() {
        return outlineFill;
    }

    public synchronized void setOutlineFill(Fill outlineFill) {
        if (!Objects.equals(this.outlineFill, outlineFill)) {
            this.outlineFill = outlineFill;
            for (GeomLayerListener listener : listeners) {
                listener.styleChanged(this);
            }
        }
    }

    public synchronized Outline getOutline() {
        return outline;
    }

    public synchronized void setOutline(Outline outline) {
        if (!Objects.equals(this.outline, outline)) {
            this.outline = outline;
            for (GeomLayerListener listener : listeners) {
                listener.styleChanged(this);
            }
        }
    }

    public synchronized Renderable getSymbol() {
        return symbol;
    }

    public synchronized void setSymbol(Renderable symbol) {
        if (!Objects.equals(this.symbol, symbol)) {
            this.symbol = symbol;
            for (GeomLayerListener listener : listeners) {
                listener.styleChanged(this);
            }
        }
    }

    public synchronized void addListener(GeomLayerListener listener){
        listeners.add(listener);
    }
    
    public synchronized void removeListener(GeomLayerListener listener){
        listeners.remove(listener);
    }
    
    public interface GeomLayerListener {

        void titleChanged(GeomLayer layer, String oldTitle, String newTitle);

        void geomChanged(GeomLayer layer, Geom oldGeom, Geom newGeom);

        void pathChanged(GeomLayer layer, String oldPath, String newPath);

        void styleChanged(GeomLayer layer);

    }

}
