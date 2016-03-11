package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.jg.util.Network;
import org.jg.util.Tolerance;
import org.jg.util.Transform;

/**
 *
 * @author tofar_000
 */
public class RingSet implements Geom {

    public static final RingSet[] EMPTY = new RingSet[0];

    final Ring shell;
    final RingSet[] children;
    
    RingSet(Ring shell, RingSet[] children) {
        this.shell = shell;
        this.children = children;
    }
    
    public RingSet(Ring shell, Collection<RingSet> children) {
        this.shell = shell;
        this.children = ((children == null) || children.isEmpty()) ? EMPTY : children.toArray(new RingSet[children.size()]);
        if((shell == null) && children.isEmpty()){
            throw new IllegalArgumentException("Must define either an outer shell or children");
        }
    }

    public RingSet(Ring shell) {
        this(shell, EMPTY);
    }

    public static RingSet valueOf(Network network){
        List<Ring> rings = Ring.valueOf(network);
        switch(rings.size()){
            case 0:
                return null;
            case 1:
                return new RingSet(rings.get(0), EMPTY);
            default:
                RingSetBuilder builder = new RingSetBuilder(null);
                for(Ring ring : rings){
                    builder.add(ring);
                }
                return builder.build();
        }
    }
    
    public double getArea(){
        if(shell == null){
            double ret = 0;
            for(RingSet ringSet : children){
                ret += ringSet.getArea();
            }
            return ret;
        }else{
            double ret = shell.getArea();
            for(RingSet ringSet : children){
                ret -= ringSet.getArea();
            }
            return ret;
        }
    }

    @Override
    public Rect getBounds() {
        if(children.length == 0){
            return shell.getBounds();
        }else{
            RectBuilder bounds = new RectBuilder();
            if(shell != null){
                shell.addBoundsTo(bounds);
            }
            for(RingSet ringSet : children){
                ringSet.addBoundsTo(bounds);
            }
            return bounds.build();
        }
    }

    @Override
    public void addBoundsTo(RectBuilder target) throws NullPointerException {
        target.add(getBounds());
    }

    @Override
    public Geom transform(Transform transform) throws NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PathIterator pathIterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RingSet clone() {
        return this;
    }
    
    public int numRings(){
        int ret = (shell == null) ? 0 : 1;
        for(RingSet child : children){
            ret += child.numRings();
        }
        return ret;
    }
    
    @Override
    public String toString() {
        return "{shell:" + shell + ", holes:" + Arrays.toString(children) + '}';
    }
    
    @Override
    public void toString(Appendable appendable) throws NullPointerException, GeomException {
        try{
            appendable.append('{');
            if(shell != null){
                appendable.append("shell:");
                shell.toString(appendable);
            }
            if(children.length > 0){
                if(shell != null){
                    appendable.append(',');
                }
                appendable.append("children:[");
                for(int c = 0; c < children.length; c++){
                    if(c != 0){
                        appendable.append(',');
                    }
                    children[c].toString(appendable);
                }
                appendable.append("]}");
                
            }
        }catch(IOException ex){
            throw new GeomException("Error writing", ex);
        }
    }

    @Override
    public void addTo(Network network, Tolerance tolerance) throws NullPointerException, IllegalArgumentException {
        if(shell != null){
            shell.addTo(network, tolerance);
        }
        for(int c = 0; c < children.length; c++){
            children[c].addTo(network, tolerance);
        }
    }

    @Override
    public Geom buffer(double amt, Tolerance flatness, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Relate relate(Vect vect, Tolerance tolerance) throws NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Relate relate(VectBuilder vect, Tolerance tolerance) throws NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    static class RingSetBuilder{
        final Ring shell;
        final ArrayList<RingSetBuilder> children;

        RingSetBuilder(Ring shell) {
            this.shell = shell;
            children = new ArrayList<>();
        }
        
        RingSet build(){
            if(shell == null){
                if(children.size() == 1){
                    return children.get(0).build();
                }
            }
            RingSet[] _children = new RingSet[children.size()];
            for(int c = 0; c < _children.length; c++){
                _children[c] = children.get(c).build();
            }
            return new RingSet(shell, _children);
        }
        
        boolean add(Ring ring){
            if(!canAdd(ring)){
                return false;
            }
            for(RingSetBuilder child : children){
                if(child.add(ring)){
                    return true;
                }
            }
            children.add(new RingSetBuilder(ring));
            return true;
        }
        
        boolean canAdd(Ring ring){
            if(shell == null){
                return true;
            }
            int i = 0;
            while(true){
                Relate relate = shell.relate(ring.vects.getX(i), ring.vects.getY(i), Tolerance.ZERO);
                switch(relate){
                    case INSIDE:
                        return true;
                    case OUTSIDE:
                        return false;
                }
                i++;
            }
        }
    }
}
