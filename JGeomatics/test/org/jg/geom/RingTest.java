package org.jg.geom;

import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jg.util.SpatialNode;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofarrell
 */
public class RingTest {

    static final Tolerance TOL = Tolerance.DEFAULT;

    @Test
    public void testValueOf() {
        Ring ring = Ring.valueOf(TOL, 0, 0, 50, 100, 100, 0, 0, 0);
        assertEquals("[\"RG\", 0,0, 100,0, 50,100, 0,0]", ring.toString());
        assertEquals(5000, ring.getArea(), 0.0001);
        assertEquals(Rect.valueOf(0, 0, 100, 100), ring.getBounds());
        assertEquals(Vect.valueOf(50, 100.0 / 3), ring.getCentroid());
        assertEquals((Math.sqrt(12500) * 2) + 100, ring.getLength(), 0.0001);
        try {
            Ring.valueOf(TOL, 1, 2, 3);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        try {
            Ring.valueOf(TOL, 1, Double.NEGATIVE_INFINITY, 3, 4);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        try {
            Ring.valueOf(TOL, 0, 0);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        try {
            Ring.valueOf(TOL, 0, 0, 100, 100, 100, 0, 0, 100, 0, 0);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        try {
            Ring.valueOf(TOL);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        try {
            Ring.valueOf(null, 0, 0, 50, 100, 100, 0, 0, 0);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            LineString.valueOf(TOL, (double[]) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            LineString.valueOf(TOL, (VectList) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testParseAll() {
        Network network = new Network();
        assertEquals(0, Ring.parseAll(TOL, network).length);
        
        network.addAllLinks(new VectList(0, 0, 100, 100, 100, 0, 0, 100, 0, 0));
        Ring[] rings = Ring.parseAll(TOL, network);
        
        List<Ring> expected = new ArrayList<>();
        expected.add(Ring.valueOf(TOL, 0,0, 50,50, 0,100, 0,0));
        expected.add(Ring.valueOf(TOL, 100,0, 100,100, 50,50, 100,0));
        
        for(Ring ring : rings){
            assertTrue(expected.remove(ring));
        }
        assertTrue(expected.isEmpty());
    }

    /**
     * Test of getArea method, of class Ring.
     */
    @Test
    public void testGetArea_0args() {
        System.out.println("getArea");
        Ring instance = null;
        double expResult = 0.0;
        double result = instance.getArea();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getArea method, of class Ring.
     */
    @Test
    public void testGetArea_VectList() {
        System.out.println("getArea");
        VectList vects = null;
        double expResult = 0.0;
        double result = Ring.getArea(vects);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLength method, of class Ring.
     */
    @Test
    public void testGetLength() {
        System.out.println("getLength");
        Ring instance = null;
        double expResult = 0.0;
        double result = instance.getLength();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of numVects method, of class Ring.
     */
    @Test
    public void testNumVects() {
        System.out.println("numVects");
        Ring instance = null;
        int expResult = 0;
        int result = instance.numVects();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of numLines method, of class Ring.
     */
    @Test
    public void testNumLines() {
        System.out.println("numLines");
        Ring instance = null;
        int expResult = 0;
        int result = instance.numLines();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBounds method, of class Ring.
     */
    @Test
    public void testGetBounds() {
        System.out.println("getBounds");
        Ring instance = null;
        Rect expResult = null;
        Rect result = instance.getBounds();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addBoundsTo method, of class Ring.
     */
    @Test
    public void testAddBoundsTo() {
        System.out.println("addBoundsTo");
        RectBuilder bounds = null;
        Ring instance = null;
        instance.addBoundsTo(bounds);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of relate method, of class Ring.
     */
    @Test
    public void testRelate_Vect_Tolerance() {
        System.out.println("relate");
        Vect vect = null;
        Tolerance accuracy = null;
        Ring instance = null;
        Relate expResult = null;
        Relate result = instance.relate(vect, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of relate method, of class Ring.
     */
    @Test
    public void testRelate_VectBuilder_Tolerance() {
        System.out.println("relate");
        VectBuilder vect = null;
        Tolerance accuracy = null;
        Ring instance = null;
        Relate expResult = null;
        Relate result = instance.relate(vect, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of relate method, of class Ring.
     */
    @Test
    public void testRelate_3args() {
        System.out.println("relate");
        double x = 0.0;
        double y = 0.0;
        Tolerance tolerance = null;
        Ring instance = null;
        Relate expResult = null;
        Relate result = instance.relate(x, y, tolerance);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLineIndex method, of class Ring.
     */
    @Test
    public void testGetLineIndex() {
        System.out.println("getLineIndex");
        Ring instance = null;
        SpatialNode<Line> expResult = null;
        SpatialNode<Line> result = instance.getLineIndex();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCentroid method, of class Ring.
     */
    @Test
    public void testGetCentroid_0args() {
        System.out.println("getCentroid");
        Ring instance = null;
        Vect expResult = null;
        Vect result = instance.getCentroid();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCentroid method, of class Ring.
     */
    @Test
    public void testGetCentroid_VectList() {
        System.out.println("getCentroid");
        VectList vects = null;
        Vect expResult = null;
        Vect result = Ring.getCentroid(vects);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isConvex method, of class Ring.
     */
    @Test
    public void testIsConvex_0args() {
        System.out.println("isConvex");
        Ring instance = null;
        boolean expResult = false;
        boolean result = instance.isConvex();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isConvex method, of class Ring.
     */
    @Test
    public void testIsConvex_VectList() {
        System.out.println("isConvex");
        VectList vects = null;
        boolean expResult = false;
        boolean result = Ring.isConvex(vects);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVect method, of class Ring.
     */
    @Test
    public void testGetVect_int() {
        System.out.println("getVect");
        int index = 0;
        Ring instance = null;
        Vect expResult = null;
        Vect result = instance.getVect(index);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVect method, of class Ring.
     */
    @Test
    public void testGetVect_int_VectBuilder() {
        System.out.println("getVect");
        int index = 0;
        VectBuilder target = null;
        Ring instance = null;
        VectBuilder expResult = null;
        VectBuilder result = instance.getVect(index, target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLine method, of class Ring.
     */
    @Test
    public void testGetLine() {
        System.out.println("getLine");
        int index = 0;
        Ring instance = null;
        Line expResult = null;
        Line result = instance.getLine(index);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getX method, of class Ring.
     */
    @Test
    public void testGetX() {
        System.out.println("getX");
        int index = 0;
        Ring instance = null;
        double expResult = 0.0;
        double result = instance.getX(index);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getY method, of class Ring.
     */
    @Test
    public void testGetY() {
        System.out.println("getY");
        int index = 0;
        Ring instance = null;
        double expResult = 0.0;
        double result = instance.getY(index);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVects method, of class Ring.
     */
    @Test
    public void testGetVects() {
        System.out.println("getVects");
        VectList target = null;
        Ring instance = null;
        VectList expResult = null;
        VectList result = instance.getVects(target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addTo method, of class Ring.
     */
    @Test
    public void testAddTo_Network() {
        System.out.println("addTo");
        Network network = null;
        Ring instance = null;
        instance.addTo(network);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buffer method, of class Ring.
     */
    @Test
    public void testBuffer() {
        System.out.println("buffer");
        double amt = 0.0;
        Tolerance flatness = null;
        Tolerance accuracy = null;
        Ring instance = null;
        Area expResult = null;
        Area result = instance.buffer(amt, flatness, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEdgeBuffer method, of class Ring.
     */
    @Test
    public void testGetEdgeBuffer() {
        System.out.println("getEdgeBuffer");
        double amt = 0.0;
        Tolerance flatness = null;
        Tolerance tolerance = null;
        Ring instance = null;
        VectList expResult = null;
        VectList result = instance.getEdgeBuffer(amt, flatness, tolerance);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of transform method, of class Ring.
     */
    @Test
    public void testTransform() {
        System.out.println("transform");
        Transform transform = null;
        Ring instance = null;
        Ring expResult = null;
        Ring result = instance.transform(transform);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pathIterator method, of class Ring.
     */
    @Test
    public void testPathIterator() {
        System.out.println("pathIterator");
        Ring instance = null;
        PathIterator expResult = null;
        PathIterator result = instance.pathIterator();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of clone method, of class Ring.
     */
    @Test
    public void testClone() {
        System.out.println("clone");
        Ring instance = null;
        Ring expResult = null;
        Ring result = instance.clone();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toGeoShape method, of class Ring.
     */
    @Test
    public void testToGeoShape_Tolerance_Tolerance() {
        System.out.println("toGeoShape");
        Tolerance flatness = null;
        Tolerance accuracy = null;
        Ring instance = null;
        GeoShape expResult = null;
        GeoShape result = instance.toGeoShape(flatness, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toGeoShape method, of class Ring.
     */
    @Test
    public void testToGeoShape_0args() {
        System.out.println("toGeoShape");
        Ring instance = null;
        GeoShape expResult = null;
        GeoShape result = instance.toGeoShape();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addTo method, of class Ring.
     */
    @Test
    public void testAddTo_3args() {
        System.out.println("addTo");
        Network network = null;
        Tolerance flatness = null;
        Tolerance accuracy = null;
        Ring instance = null;
        instance.addTo(network, flatness, accuracy);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toArea method, of class Ring.
     */
    @Test
    public void testToArea() {
        System.out.println("toArea");
        Ring instance = null;
        Area expResult = null;
        Area result = instance.toArea();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of union method, of class Ring.
     */
    @Test
    public void testUnion() {
        System.out.println("union");
        Geom other = null;
        Tolerance flatness = null;
        Tolerance accuracy = null;
        Ring instance = null;
        Geom expResult = null;
        Geom result = instance.union(other, flatness, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of intersection method, of class Ring.
     */
    @Test
    public void testIntersection() {
        System.out.println("intersection");
        Geom other = null;
        Tolerance flatness = null;
        Tolerance accuracy = null;
        Ring instance = null;
        Geom expResult = null;
        Geom result = instance.intersection(other, flatness, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of less method, of class Ring.
     */
    @Test
    public void testLess() {
        System.out.println("less");
        Geom other = null;
        Tolerance flatness = null;
        Tolerance accuracy = null;
        Ring instance = null;
        Geom expResult = null;
        Geom result = instance.less(other, flatness, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hashCode method, of class Ring.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        Ring instance = null;
        int expResult = 0;
        int result = instance.hashCode();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of equals method, of class Ring.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object obj = null;
        Ring instance = null;
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class Ring.
     */
    @Test
    public void testToString_0args() {
        System.out.println("toString");
        Ring instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class Ring.
     */
    @Test
    public void testToString_Appendable() {
        System.out.println("toString");
        Appendable appendable = null;
        Ring instance = null;
        instance.toString(appendable);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class Ring.
     */
    @Test
    public void testToString_boolean() {
        System.out.println("toString");
        boolean summarize = false;
        Ring instance = null;
        String expResult = "";
        String result = instance.toString(summarize);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
