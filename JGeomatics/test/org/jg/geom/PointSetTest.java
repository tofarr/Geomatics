package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.IOException;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.TransformBuilder;
import org.jg.util.VectList;
import org.jg.util.VectSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofarrell
 */
public class PointSetTest {

    @Test
    public void testValueOf_VectSet() {
        PointSet ps = PointSet.valueOf(new VectSet().add(3, 4).add(1, 2).add(3, 3).add(3, 4));
        try{
            ps = PointSet.valueOf((VectSet)null);
            fail("Exception expected");
        }catch(NullPointerException ex){  
        }
        assertEquals(3, ps.numPoints());
        assertEquals(Vect.valueOf(1, 2), ps.getPoint(0));
        assertEquals(new VectBuilder(3, 3), ps.getPoint(1, new VectBuilder()));
        assertEquals(Vect.valueOf(3, 4), ps.getPoint(2));
        ps = PointSet.valueOf(new VectSet());
        assertNull(ps);
    }

    @Test
    public void testValueOf_Network() {
        Network network = new Network().addAllLinks(new VectList(0,0, 100,0, 0,100));
        network.addVertex(50, 50); //network is not explicitized as part of this operation
        network.addVertex(100, 100);
        network.addVertex(1, 2);
        PointSet ps = PointSet.valueOf(network, Tolerance.DEFAULT);
        try{
            ps = PointSet.valueOf(null, Tolerance.DEFAULT);
            fail("Exception expected");
        }catch(NullPointerException ex){  
        }
        try{
            ps = PointSet.valueOf(network, null);
            fail("Exception expected");
        }catch(NullPointerException ex){  
        }
        assertEquals(2, ps.numPoints());
        assertEquals(Vect.valueOf(1, 2), ps.getPoint(0));
        assertEquals(new VectBuilder(100, 100), ps.getPoint(1, new VectBuilder()));
        network.clear();
        ps = PointSet.valueOf(network, Tolerance.DEFAULT);
        assertNull(ps);
    }

    @Test
    public void testGetBounds() {
        PointSet ps = PointSet.valueOf(new VectSet().add(3, 4).add(1, 2).add(3, 3).add(5, 6).add(3, 4));
        assertEquals(Rect.valueOf(1,2,5, 6), ps.getBounds());
    }

    @Test
    public void testTransform() {
        PointSet ps = PointSet.valueOf(new VectSet().addAll(new VectList(1, 3, 7, 13)));
        PointSet result = ps.transform(new TransformBuilder().scale(2, 3).build());
        PointSet expected = PointSet.valueOf(new VectSet().addAll(new VectList(2, 9, 14, 39)));
        try{
            result.transform(null);
            fail("Exception expected");
        }catch(NullPointerException ex){  
        }
        assertEquals(expected, result);
    }

    @Test
    public void testSimplify() {
        PointSet ps = PointSet.valueOf(new VectSet().addAll(new VectList(1, 3, 7, 13)));
        assertSame(ps, ps.simplify());
        ps = PointSet.valueOf(new VectSet().add(1, 3));
        assertEquals(Vect.valueOf(1, 3), ps.simplify());
    }

    @Test
    public void testPathIterator() {
        PointSet ps = PointSet.valueOf(new VectSet().addAll(new VectList(1, 3, 7, 13, 23, 29)));
        PathIterator iter = ps.pathIterator();
        assertEquals(PathIterator.WIND_NON_ZERO, iter.getWindingRule());

        assertFalse(iter.isDone());
        
        double[] coords = new double[6];
        assertEquals(PathIterator.SEG_MOVETO, iter.currentSegment(coords));
        assertEquals(1, coords[0], 0.0001);
        assertEquals(3, coords[1], 0.0001);
        
        double[] fcoords = new double[6];
        assertEquals(PathIterator.SEG_MOVETO, iter.currentSegment(fcoords));
        assertEquals(1, fcoords[0], 0.0001);
        assertEquals(3, fcoords[1], 0.0001);
        
        iter.next();
        assertFalse(iter.isDone());
        assertEquals(PathIterator.SEG_LINETO, iter.currentSegment(coords));
        assertEquals(1, coords[0], 0.0001);
        assertEquals(3, coords[1], 0.0001);
                
        iter.next();
        assertFalse(iter.isDone());
        assertEquals(PathIterator.SEG_MOVETO, iter.currentSegment(coords));
        assertEquals(7, coords[0], 0.0001);
        assertEquals(13, coords[1], 0.0001);
                
        iter.next();
        assertFalse(iter.isDone());
        assertEquals(PathIterator.SEG_LINETO, iter.currentSegment(coords));
        assertEquals(7, coords[0], 0.0001);
        assertEquals(13, coords[1], 0.0001);
                
        iter.next();
        assertFalse(iter.isDone());
        assertEquals(PathIterator.SEG_MOVETO, iter.currentSegment(coords));
        assertEquals(23, coords[0], 0.0001);
        assertEquals(29, coords[1], 0.0001);
                
        iter.next();
        assertFalse(iter.isDone());
        assertEquals(PathIterator.SEG_LINETO, iter.currentSegment(coords));
        assertEquals(23, coords[0], 0.0001);
        assertEquals(29, coords[1], 0.0001);
                
        iter.next();
        assertTrue(iter.isDone());        
    }

    @Test
    public void testClone() {
        PointSet ps = PointSet.valueOf(new VectSet().addAll(new VectList(1, 3, 7, 13, 23, 29)));
        assertSame(ps, ps.clone());
    }

    @Test
    public void testToString() {
        PointSet ps = PointSet.valueOf(new VectSet().addAll(new VectList(1, 3, 7, 13, 23, 29)));
        assertEquals("[\"PS\", 1,3, 7,13, 23,29]", ps.toString());
        try{
            ps.toString(new Appendable(){
                @Override
                public Appendable append(CharSequence csq) throws IOException {
                    throw new IOException();
                }

                @Override
                public Appendable append(CharSequence csq, int start, int end) throws IOException {
                    throw new IOException();
                }

                @Override
                public Appendable append(char c) throws IOException {
                    throw new IOException();
                }
            
            });
            fail("Exception expected");
        }catch(GeomException ex){
        }
    }
    @Test
    public void testToGeoShape() {
        PointSet ps = PointSet.valueOf(new VectSet().addAll(new VectList(1, 3, 7, 13, 23, 29)));
        GeoShape gs = ps.toGeoShape(Tolerance.FLATNESS, Tolerance.DEFAULT);
        assertEquals(new GeoShape(null, null, ps), ps);
    }


    @Test
    public void testAddTo() {
        VectList vects = new VectList(1, 3, 7, 13, 23, 29);
        PointSet ps = PointSet.valueOf(new VectSet().addAll(vects));
        Network network = new Network();
        network.addLink(1, 3, 2, 3);
        ps.addTo(network);
        assertEquals("GEOMETRYCOLLECTION(POINT(7 13),POINT(23 29),LINESTRING(1 3, 2 3))", network.toWkt());
    }

    @Test
    public void testBuffer() {
        VectList vects = new VectList(5,5, 5,7, 7,5, 14,5, 22,5, 31,5);
        PointSet ps = PointSet.valueOf(new VectSet().addAll(vects));
        assertNull(ps.buffer(-1, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertSame(ps, ps.buffer(0, Tolerance.FLATNESS, Tolerance.DEFAULT));
        Area buffered = (Area)ps.buffer(4, Tolerance.FLATNESS, Tolerance.DEFAULT);
        Network network = new Network();
        buffered.addTo(network, Tolerance.FLATNESS, Tolerance.DEFAULT);
        Rect bounds = buffered.getBounds();
        assertEquals(1, bounds.minX, 0.00001);
        assertEquals(1, bounds.minY, 0.00001);
        assertEquals(35, bounds.maxX, 0.00001);
        assertEquals(11, bounds.maxY, 0.00001);
        fail("We also need to test area and number of rings to insure overlap was correct");
    }

    /**
     * Test of removeWithinBuffer method, of class PointSet.
     */
    @Test
    public void testRemoveWithinBuffer() {
        System.out.println("removeWithinBuffer");
        VectList vects = null;
        Network network = null;
        double amt = 0.0;
        Tolerance flatness = null;
        Tolerance tolerance = null;
        PointSet.removeWithinBuffer(vects, network, amt, flatness, tolerance);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of relate method, of class PointSet.
     */
    @Test
    public void testRelate_Vect_Tolerance() {
        System.out.println("relate");
        Vect vect = null;
        Tolerance tolerance = null;
        PointSet instance = null;
        Relate expResult = null;
        Relate result = instance.relate(vect, tolerance);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of relate method, of class PointSet.
     */
    @Test
    public void testRelate_VectBuilder_Tolerance() {
        System.out.println("relate");
        VectBuilder vect = null;
        Tolerance tolerance = null;
        PointSet instance = null;
        Relate expResult = null;
        Relate result = instance.relate(vect, tolerance);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of relateInternal method, of class PointSet.
     */
    @Test
    public void testRelateInternal() {
        System.out.println("relateInternal");
        double x = 0.0;
        double y = 0.0;
        Tolerance tolerance = null;
        PointSet instance = null;
        Relate expResult = null;
        Relate result = instance.relateInternal(x, y, tolerance);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of union method, of class PointSet.
     */
    @Test
    public void testUnion_3args() {
        System.out.println("union");
        Geom other = null;
        Tolerance flatness = null;
        Tolerance accuracy = null;
        PointSet instance = null;
        Geom expResult = null;
        Geom result = instance.union(other, flatness, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of union method, of class PointSet.
     */
    @Test
    public void testUnion_PointSet_Tolerance() {
        System.out.println("union");
        PointSet other = null;
        Tolerance accuracy = null;
        PointSet instance = null;
        PointSet expResult = null;
        PointSet result = instance.union(other, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of union method, of class PointSet.
     */
    @Test
    public void testUnion_GeoShape_Tolerance() {
        System.out.println("union");
        GeoShape other = null;
        Tolerance accuracy = null;
        PointSet instance = null;
        GeoShape expResult = null;
        GeoShape result = instance.union(other, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of intersection method, of class PointSet.
     */
    @Test
    public void testIntersection_3args() {
        System.out.println("intersection");
        Geom other = null;
        Tolerance flatness = null;
        Tolerance accuracy = null;
        PointSet instance = null;
        Geom expResult = null;
        Geom result = instance.intersection(other, flatness, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of intersection method, of class PointSet.
     */
    @Test
    public void testIntersection_Geom_Tolerance() {
        System.out.println("intersection");
        Geom other = null;
        Tolerance accuracy = null;
        PointSet instance = null;
        PointSet expResult = null;
        PointSet result = instance.intersection(other, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of less method, of class PointSet.
     */
    @Test
    public void testLess_3args() {
        System.out.println("less");
        Geom other = null;
        Tolerance flatness = null;
        Tolerance accuracy = null;
        PointSet instance = null;
        Geom expResult = null;
        Geom result = instance.less(other, flatness, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of less method, of class PointSet.
     */
    @Test
    public void testLess_Geom_Tolerance() {
        System.out.println("less");
        Geom other = null;
        Tolerance accuracy = null;
        PointSet instance = null;
        PointSet expResult = null;
        PointSet result = instance.less(other, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of numPoints method, of class PointSet.
     */
    @Test
    public void testNumPoints() {
        System.out.println("numPoints");
        PointSet instance = null;
        int expResult = 0;
        int result = instance.numPoints();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPoint method, of class PointSet.
     */
    @Test
    public void testGetPoint_int() {
        System.out.println("getPoint");
        int index = 0;
        PointSet instance = null;
        Vect expResult = null;
        Vect result = instance.getPoint(index);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPoint method, of class PointSet.
     */
    @Test
    public void testGetPoint_int_VectBuilder() {
        System.out.println("getPoint");
        int index = 0;
        VectBuilder target = null;
        PointSet instance = null;
        VectBuilder expResult = null;
        VectBuilder result = instance.getPoint(index, target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getX method, of class PointSet.
     */
    @Test
    public void testGetX() {
        System.out.println("getX");
        int index = 0;
        PointSet instance = null;
        double expResult = 0.0;
        double result = instance.getX(index);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getY method, of class PointSet.
     */
    @Test
    public void testGetY() {
        System.out.println("getY");
        int index = 0;
        PointSet instance = null;
        double expResult = 0.0;
        double result = instance.getY(index);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hashCode method, of class PointSet.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        PointSet instance = null;
        int expResult = 0;
        int result = instance.hashCode();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of equals method, of class PointSet.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object obj = null;
        PointSet instance = null;
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
