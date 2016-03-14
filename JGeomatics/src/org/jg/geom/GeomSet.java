package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jg.util.SpatialNode.NodeProcessor;
import org.jg.util.Tolerance;
import org.jg.util.Transform;

/**
 *
 * @author tofar_000
 */
public class GeomSet implements Geom, Serializable {

    final Geom[] geoms;
    Rect bounds;

    GeomSet(Geom... geoms) {
        this.geoms = geoms;
    }
    
    /*
    public static Geom valueOf(Geom... geoms) throws NullPointerException{
        switch(geoms.length){
            case 0:
                return null;
            case 1:
                if(geoms[0] != null){
                    return geoms[0];
                }
            default:
                ArrayList<Geom> result = new ArrayList<>();
                flatten(geoms, result);
                switch(result.size()){
                    case 0:
                        return null;
                    case 1:
                        return result.get(0);
                    default:
                        return new GeomSet(result.toArray(new Geom[result.size()]));
                }
        }
    }*/
    
    
    public static Geom normalizedValueOf(Geom... geoms) throws NullPointerException{
        switch(geoms.length){
            case 0:
                return null;
            case 1:
                if(geoms[0] != null){
                    return geoms[0];
                }
            default:
                ArrayList<Geom> result = new ArrayList<>();
                flatten(geoms, result);
                switch(result.size()){
                    case 0:
                        return null;
                    case 1:
                        return result.get(0);
                    default:
                        Geom[] ret = result.toArray(new Geom[result.size()]);
                        Arrays.sort(ret, COMPARATOR);
                        return new GeomSet(ret);
                }
        }
    }
    
    static void flatten(Geom[] geoms, List<Geom> result){
        for(Geom geom : geoms){
            if(geom instanceof GeomSet){
                flatten(((GeomSet)geom).geoms, result);
            }else if(geom != null){
                result.add(geom);
            }
        }
    }

    @Override
    public Rect getBounds() {
        Rect ret = bounds;
        if(bounds == null){
            RectBuilder builder = new RectBuilder();
            for(Geom geom : geoms){
                if(geom instanceof Vect){
                    builder.add((Vect)geom);
                }else{
                    builder.add(geom.getBounds());
                }
            }
            ret = builder.build();
            bounds = ret;
        }
        return ret;
    }

    @Override
    public GeomSet transform(Transform transform) throws NullPointerException {
        Geom[] transformed = new Geom[geoms.length];
        for(int g = geoms.length; g-- > 0;){
            transformed[g] = geoms[g].transform(transform);
        }
        return new GeomSet(transformed);
    }

    @Override
    public PathIterator pathIterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GeomSet clone() {
        return this;
    }

    @Override
    public void toString(Appendable appendable) throws NullPointerException, GeomException {
        try {
            appendable.append("[\"GS\"");
            for(Geom geom : geoms){
                appendable.append(',');
                geom.toString(appendable);
            }
            appendable.append(']');
        } catch (IOException ex) {
            throw new GeomException("Error writing GeomSet", ex);
        }
    }

    @Override
    public void addTo(Network network, Tolerance tolerance) throws NullPointerException, IllegalArgumentException {
        for(Geom geom : geoms){
            geom.addTo(network, tolerance);
        }
    }

    @Override
    public Geom buffer(double amt, Tolerance flatness, final Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        final Network network = new Network();
        final List<Geom> buffered = new ArrayList<>(geoms.length);
        for(int g = geoms.length; g-- > 0;){
            Geom geom = geoms[g];
            Geom bufferedGeom = geom.buffer(amt, flatness, tolerance);
            if(bufferedGeom != null){
                buffered.add(bufferedGeom);
                bufferedGeom.addTo(network, tolerance);
            }
        }
        network.explicitIntersections(tolerance);
        network.forEachLink(new NodeProcessor<Line>(){
            VectBuilder vect = new VectBuilder();
            
            @Override
            public boolean process(Rect bounds, Line line) {
                line.getMid(vect);
                for(Geom bufferedGeom : buffered){
                    if(bufferedGeom.relate(vect, tolerance) == Relate.INSIDE){
                        network.removeLink(line);
                        break;
                    }
                }
                return true;
            }
        
        });
        return Area.valueOf(network);
    }

    @Override
    public Relate relate(Vect vect, Tolerance tolerance) throws NullPointerException {
        Relate ret = Relate.OUTSIDE;
        if(getBounds().relate(vect) == Relate.OUTSIDE){
            return ret;
        }
        for(Geom geom : geoms){
            switch(geom.relate(vect, tolerance)){
                case INSIDE:
                    ret = (ret == Relate.OUTSIDE) ? Relate.INSIDE : Relate.OUTSIDE;
                    break;
                case TOUCH:
                    return Relate.TOUCH;
            }
        }
        return ret;
    }

    @Override
    public Relate relate(VectBuilder vect, Tolerance tolerance) throws NullPointerException {
        Relate ret = Relate.OUTSIDE;
        if(getBounds().relate(vect) == Relate.OUTSIDE){
            return ret;
        }
        for(Geom geom : geoms){
            switch(geom.relate(vect, tolerance)){
                case INSIDE:
                    ret = (ret == Relate.OUTSIDE) ? Relate.INSIDE : Relate.OUTSIDE;
                    break;
                case TOUCH:
                    return Relate.TOUCH;
            }
        }
        return ret;
    }

    @Override
    public Geom union(Geom other, Tolerance flatness, Tolerance tolerance) throws NullPointerException {
        if(getBounds().buffer(tolerance.tolerance).isDisjoint(other.getBounds())){
            return normalizedValueOf(this, other);
        }else{
            return Network.union(flatness, tolerance, this, other);
        }
    }

    @Override
    public Geom intersection(Geom other, Tolerance flatness, Tolerance tolerance) throws NullPointerException {
        if(getBounds().buffer(tolerance.tolerance).isDisjoint(other.getBounds())){
            return null;
        }else{
            return Network.intersection(flatness, tolerance, this, other);
        }
    }

    @Override
    public Geom less(Geom other, Tolerance flatness, Tolerance tolerance) throws NullPointerException {
        if(getBounds().buffer(tolerance.tolerance).isDisjoint(other.getBounds())){
            return this;
        }else{
            return Network.less(flatness, tolerance, this, other);
        }
    }

    public Geom union(Tolerance flatness, Tolerance tolerance) throws NullPointerException {
        return Network.union(flatness, tolerance, geoms);
    }    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Arrays.deepHashCode(this.geoms);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof GeomSet){
            GeomSet geomSet = (GeomSet)obj;
            return Arrays.equals(geoms, geomSet.geoms);
        }else{
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        toString(str);
        return str.toString();
    }
}
