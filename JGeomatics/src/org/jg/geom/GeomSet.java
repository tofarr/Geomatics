package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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

    GeomSet(Geom[] geoms) {
        this.geoms = geoms;
    }
    
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
                geom.addBoundsTo(builder);
            }
            ret = builder.build();
            bounds = ret;
        }
        return ret;
    }

    @Override
    public void addBoundsTo(RectBuilder target) throws NullPointerException {
        target.add(getBounds());
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
        Network network = new Network();
        addTo(network, Tolerance.DEFAULT);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        return RingSet.valueOf(network);
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
}
