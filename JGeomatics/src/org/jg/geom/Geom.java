package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.Serializable;
import org.jg.util.Network;
import org.jg.util.Tolerance;
import org.jg.util.Transform;

/**
 *
 * @author tofar_000
 */
public interface Geom extends Cloneable, Serializable {

    Rect getBounds();

    void addBoundsTo(RectBuilder target) throws NullPointerException;

    Geom transform(Transform transform) throws NullPointerException;

    PathIterator pathIterator();

    Geom clone();

    void toString(Appendable appendable) throws NullPointerException, GeomException;

    void addTo(Network network, Tolerance tolerance) throws NullPointerException, IllegalArgumentException;

    Geom buffer(double amt, Tolerance tolerance) throws IllegalArgumentException, NullPointerException;
    
    Relate relate(Vect vect, Tolerance tolerance) throws NullPointerException;
    
    Relate relate(VectBuilder vect, Tolerance tolerance) throws NullPointerException;

}
