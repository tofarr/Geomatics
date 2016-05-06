package org.geomatics.gv.modelold;

import java.beans.PropertyChangeSupport;
import org.geomatics.geom.Geom;
import org.geomatics.gfx.fill.Fill;
import org.geomatics.gfx.outline.Outline;
import org.geomatics.gfx.renderable.Renderable;

/**
 *
 * @author tofarrell
 */
public class GeomLayerFactory {

    public static final String TITLE = "title";
    public static final String FILE = "file";
    public static final String GEOM = "geom";
    public static final String RENDER_GEOM = "renderGeom";
    public static final String FILL = "fill";
    public static final String OUTLINE_FILL = "outlineFill";
    public static final String OUTLINE = "outline";
    public static final String SYMBOL = "symbol";
    private final PropertyChangeSupport change;
    private String title;
    private String file;
    private Geom geom;
    private Fill fill;
    private Fill outlineFill;
    private Outline outline;
    private Renderable symbol;

    public GeomLayerFactory() {
        change = new PropertyChangeSupport(this);
    }
    
    public GeomLayerz build(){
        
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        change.firePropertyChange(TITLE, this.title, this.title = title);
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        change.firePropertyChange(FILE, this.file, this.file = file);
    }

    public Geom getGeom() {
        return geom;
    }

    public void setGeom(Geom geom) {
        change.firePropertyChange(GEOM, this.geom, this.geom = geom);
    }

    public Fill getFill() {
        return fill;
    }

    public void setFill(Fill fill) {
        change.firePropertyChange(FILL, this.fill, this.fill = fill);
    }

    public Fill getOutlineFill() {
        return outlineFill;
    }

    public void setOutlineFill(Fill outlineFill) {
        change.firePropertyChange(OUTLINE_FILL, this.outlineFill, this.outlineFill = outlineFill);
    }

    public Outline getOutline() {
        return outline;
    }

    public void setOutline(Outline outline) {
        change.firePropertyChange(OUTLINE, this.outline, this.outline = outline);
    }

    public Renderable getSymbol() {
        return symbol;
    }

    public void setSymbol(Renderable symbol) {
        change.firePropertyChange(SYMBOL, this.symbol, this.symbol = symbol);
    }

}
