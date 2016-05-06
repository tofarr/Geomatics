package org.geomatics.gv.modelold;

import java.beans.PropertyChangeSupport;
import java.util.Objects;
import org.geomatics.geom.Geom;
import org.geomatics.gfx.fill.Fill;
import org.geomatics.gfx.outline.Outline;
import org.geomatics.gfx.renderable.Renderable;

/**
 *
 * @author tofarrell
 */
public class GeomLayerModel {

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
    private boolean synchronize;
    private Fill fill;
    private Fill outlineFill;
    private Outline outline;
    private Renderable symbol;

    @Constructor
    public GeomLayerModel(){
        super();
    }
    
    public GeomLayerModel() {
        change = new PropertyChangeSupport(this);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        change.firePropertyChange(TITLE, title, this.title = title);
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        if(Objects.equals(this.file, file)){
            return; // no change
        }
        if(file == null){ // removing file
            
        }
        if(geom == null){ // loading geom
            
        }else{ // storing geom
            
        }
        
//        if(this.file == null){ // Did not have a file and now we do, so 
//            setGeom(null);
//        }
//            setRenderGeom(null);
//        }else if(file != null){
//            setGeom(null);            
//        }
        change.firePropertyChange(FILE, file, this.file = file);
    }

    public Geom getGeom() {
        return geom;
    }

    public void setGeom(Geom geom) {
        change.firePropertyChange(GEOM, geom, this.geom = geom);
    }

    public boolean isSynchronize() {
        return synchronize;
    }

    public void setSynchronize(boolean synchronize) {
        this.synchronize = synchronize;
    }

    public Fill getFill() {
        return fill;
    }

    public void setFill(Fill fill) {
        change.firePropertyChange(FILL, fill, this.fill = fill);
    }

    public Fill getOutlineFill() {
        return outlineFill;
    }

    public void setOutlineFill(Fill outlineFill) {
        change.firePropertyChange(OUTLINE_FILL, outlineFill, this.outlineFill = outlineFill);
    }

    public Outline getOutline() {
        return outline;
    }

    public void setOutline(Outline outline) {
        change.firePropertyChange(OUTLINE, outline, this.outline = outline);
    }

    public Renderable getSymbol() {
        return symbol;
    }

    public void setSymbol(Renderable symbol) {
        change.firePropertyChange(SYMBOL, symbol, this.symbol = symbol);
    }
}
