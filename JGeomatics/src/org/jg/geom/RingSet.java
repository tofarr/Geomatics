package org.jg.geom;

import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import org.jg.util.Network;
import org.jg.util.Tolerance;
import org.jg.util.Transform;

/**
 *
 * @author tofar_000
 */
public class RingSet implements Geom {

    final Ring ring;
    final List<RingSet> children;

    public RingSet(Ring ring, List<RingSet> children) {
        this.ring = ring;
        this.children = children;
    }

    public RingSet(Ring ring) {
        this(ring, new ArrayList<RingSet>());
    }

    public void addInternal(RingSet child) {

    }

    @Override
    public Rect getBounds() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addBoundsTo(RectBuilder target) throws NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    public Geom clone() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void toString(Appendable appendable) throws NullPointerException, GeomException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addTo(Network network, double flatness) throws NullPointerException, IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Geom buffer(double amt, double flatness, Tolerance tolerance) throws IllegalArgumentException, NullPointerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
