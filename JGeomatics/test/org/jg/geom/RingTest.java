package org.jg.geom;

import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jg.util.SpatialNode;
import org.jg.util.SpatialNode.NodeProcessor;
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

    @Test
    public void testGetArea_0args() {
        Ring ring = Ring.valueOf(TOL, 0,0, 100,0, 120,100, 20,100, 0,0);
        assertEquals(10000, ring.getArea(), 0.0001);
        assertEquals(10000, ring.getArea(), 0.0001);
    }

    @Test
    public void testGetLength() {
        Ring ring = Ring.valueOf(TOL, 0,0, 3,4, 3,14, 0,10, 0,0);
        assertEquals(30, ring.getLength(), 0.0001);
        assertEquals(30, ring.getLength(), 0.0001);
    }

    @Test
    public void testNumVects() {
        Ring ring = Ring.valueOf(TOL, 0,0, 3,4, 3,14, 0,10, 0,0);
        assertEquals(5, ring.numVects());
    }

    @Test
    public void testNumLines() {
        Ring ring = Ring.valueOf(TOL, 0,0, 3,4, 3,14, 0,10, 0,0);
        assertEquals(4, ring.numVects());
    }

    @Test
    public void testGetBounds() {
        Ring ring = Ring.valueOf(TOL, 0,0, 3,4, 3,14, 0,10, 0,0);
        Rect bounds = ring.getBounds();
        assertEquals(Rect.valueOf(0,0,3,14), bounds);
        assertSame(bounds, ring.getBounds());
    }

    @Test
    public void testAddBoundsTo() {
        Ring ring = Ring.valueOf(TOL, 0,0, 3,4, 3,14, 0,10, 0,0);
        RectBuilder builder = new RectBuilder().add(-1, -2);
        ring.addBoundsTo(builder);
        assertEquals(Rect.valueOf(-1,-2,3,14), builder.build());
    }

    @Test
    public void testRelate() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        assertEquals(Relate.TOUCH, ring.relate(Vect.ZERO, TOL));
        assertEquals(Relate.TOUCH, ring.relate(new VectBuilder(), TOL));
        assertEquals(Relate.TOUCH, ring.relate(Vect.valueOf(0, 5), TOL));
        assertEquals(Relate.TOUCH, ring.relate(Vect.valueOf(0, 10), TOL));
        assertEquals(Relate.TOUCH, ring.relate(Vect.valueOf(3, 4), TOL));
        assertEquals(Relate.TOUCH, ring.relate(Vect.valueOf(6, 8), TOL));
        assertEquals(Relate.TOUCH, ring.relate(Vect.valueOf(6, 12), TOL));
        assertEquals(Relate.TOUCH, ring.relate(Vect.valueOf(6, 14), TOL));
        assertEquals(Relate.TOUCH, ring.relate(Vect.valueOf(3, 12), TOL));
        assertEquals(Relate.OUTSIDE, ring.relate(Vect.valueOf(3, 1), TOL));
        assertEquals(Relate.OUTSIDE, ring.relate(Vect.valueOf(7, 12), TOL));
        assertEquals(Relate.INSIDE, ring.relate(Vect.valueOf(2, 3), TOL));
    }

    @Test
    public void testGetLineIndex() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        final Set<Line> expected = new HashSet<>(Arrays.asList(
                new Line(0,0,6,8),
                new Line(0,10,0,0),
                new Line(6,8,6,14),
                new Line(6,14,0,10)));
        SpatialNode<Line> lineIndex = ring.getLineIndex();
        assertSame(lineIndex, ring.getLineIndex());
        lineIndex.forEach(new NodeProcessor<Line>() {
            @Override
            public boolean process(Rect bounds, Line value) {
                assertTrue(expected.remove(value));
                assertEquals(bounds, value.getBounds());
                return true;
            }
        });
        assertTrue(expected.isEmpty());
    }
    
    @Test
    public void testGetCentroid() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        Vect centroid = ring.getCentroid();
        assertEquals(Vect.valueOf(2.75,7.75), centroid);
        assertSame(centroid, ring.getCentroid());
    }

    @Test
    public void testIsConvex() {
        VectList vects = new VectList(0,0, 6,8, 6,14, 0,10, 0,0);
        Ring ring = Ring.valueOf(TOL, vects);
        assertTrue(ring.isConvex());
        assertTrue(ring.isConvex());
        
        VectBuilder target = new VectBuilder();
        for(int i = 1; i < vects.size(); i++){
            Line line = vects.getLine(i-1);
            line.projectOutward(0.5, -0.8, TOL, target);
            VectList newVects = vects.clone();
            newVects.insert(i, target.build());
            Ring newRing = Ring.valueOf(TOL, newVects);
            assertFalse(newRing.isConvex());
        }
        
        //Star test!
        Network network = new Network();
        network.addAllLinks(new VectList(0,0, 6,4, -1,4, 5,0,  2.5,7, 0,0));
        Ring[] rings = Ring.parseAll(TOL, network); 
        assertFalse(rings[0].isConvex());
        assertTrue(rings[1].isConvex());
    }
    
    @Test
    public void testGetVect_int() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        assertEquals(Vect.valueOf(6,8), ring.getVect(1));
        try {
            ring.getVect(-1);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            ring.getVect(5);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    @Test
    public void testGetVect_int_VectBuilder() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        VectBuilder target = new VectBuilder();
        assertSame(target, ring.getVect(1, target));
        assertEquals(new VectBuilder(6,8), target);
        try {
            ring.getVect(1, null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            ring.getVect(-1, target);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            ring.getVect(5, target);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    @Test
    public void testGetLine() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        assertEquals(Line.valueOf(6,14,0,10), ring.getLine(2));
        try {
            ring.getLine(-1);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            ring.getLine(4);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    @Test
    public void testGetX() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        assertEquals(6, ring.getX(1), 0.0001);
        try {
            ring.getX(-1);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            ring.getX(5);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    @Test
    public void testGetY() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        assertEquals(8, ring.getY(1), 0.0001);
        try {
            ring.getX(-1);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            ring.getX(5);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    @Test
    public void testGetVects() {
        VectList vects = new VectList(0,0, 6,8, 6,14, 0,10, 0,0);
        Ring ring = Ring.valueOf(TOL, vects);
        VectList target = new VectList();
        ring.getVects(target);
        try {
            ring.getVects(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        assertEquals(vects, target);
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
