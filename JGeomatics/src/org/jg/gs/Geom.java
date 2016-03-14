package org.jg.gs;

import java.util.List;
import org.jg.util.Tolerance;
import org.jg.util.VectList;

/**
 *
 * @author tofarrell
 */
public class Geom {

    /** Area for this geom (may be null) */
    private final Area area;
    /** Line strings which are not part of an area */
    private final List<LineString> hangLines;
    /** Points which are not part of a line */
    private final VectList points;
    /** Flag indicating whether or not this geom is normalized */
    private boolean normalized;

    private Geom(Area area, List<LineString> hangLines, VectList points, Boolean normalized) {
        this.area = area;
        this.hangLines = hangLines;
        this.points = points;
        this.normalized = normalized;
    }

    public Geom(Area area, List<LineString> hangLines, VectList points) {
        this.area = area;
        this.hangLines = hangLines;
        this.points = points;
    }
    
    public Geom normalize(){
        if(normalized){
            return this;
        }
    }
    
    public Geom buffer(Tolerance flatness, Tolerance accuracy){
        
    }
    
    public Geom union(Geom other, Tolerance accuracy){
        
    }

    
    public Geom intersection(Geom other, Tolerance accuracy){
        
    }

    public Geom less(Geom other, Tolerance accuracy){
        
    }
}
