package org.jg.geom;

import java.awt.geom.PathIterator;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.VectList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofarrell
 */
public class LineSetTest {

    static final Tolerance TOL = Tolerance.DEFAULT;

    @Test
    public void testValueOf_Tolerance_Network() {
        Network network = new Network();
        network.addAllLinks(new VectList(0, 0, 100, 0, 100, 100, 0, 100, 0, 0));
        network.addAllLinks(new VectList(10, 10, 90, 10, 90, 90));
        network.addVertex(10, 90);
        LineSet ls = LineSet.valueOf(TOL, network);
        assertEquals("[\"LT\", [0,0, 100,0, 100,100, 0,100, 0,0], [10,10, 90,10, 90,90]]", ls.toString());
        try{
            LineSet.valueOf(TOL, (Network)null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        try{
            LineSet.valueOf(null, network);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
    }

    @Test
    public void testValueOf_Tolerance_VectList() {
        assertNull(LineSet.valueOf(TOL, new VectList()));
        LineSet ls = LineSet.valueOf(TOL, new VectList(0,0, 100,100, 100,0, 0,100, 0,0));
        assertEquals("[\"LT\", [0,0, 50,50, 0,100, 0,0], [50,50, 100,0, 100,100, 50,50]]", ls.toString());
        try{
            LineSet.valueOf(TOL, (VectList)null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        try{
            LineSet.valueOf(null, new VectList(0,0, 100,100, 100,0, 0,100, 0,0));
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
    }

    @Test
    public void testValueOf_Tolerance_doubleArr() {
        assertNull(LineSet.valueOf(TOL));
        assertNull(LineSet.valueOf(TOL, 0,0));
        LineSet ls = LineSet.valueOf(TOL, 0,20, 80,20, 60,0, 40,20, 20,0, 0,20);
        assertEquals("[\"LT\", [0,20, 20,0, 40,20, 0,20], [40,20, 60,0, 80,20, 40,20]]", ls.toString());
        try{
            LineSet.valueOf(TOL, (double[])null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        try{
            LineSet.valueOf(null, 0,0, 100,100, 100,0, 0,100, 0,0);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        try{
            LineSet.valueOf(TOL, 0,0, 100,100, 100,0, 0,100, 0);
            fail("Exception expected");
        }catch(IllegalArgumentException ex){
        }
        try{
            LineSet.valueOf(TOL, Double.NaN,0);
            fail("Exception expected");
        }catch(IllegalArgumentException ex){
        }
    }

    /**
     * Test of numLineStrings method, of class LineSet.
     */
    @Test
    public void testNumLineStrings() {
        System.out.println("numLineStrings");
        LineSet instance = null;
        int expResult = 0;
        int result = instance.numLineStrings();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLineString method, of class LineSet.
     */
    @Test
    public void testGetLineString() {
        System.out.println("getLineString");
        int index = 0;
        LineSet instance = null;
        LineString expResult = null;
        LineString result = instance.getLineString(index);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBounds method, of class LineSet.
     */
    @Test
    public void testGetBounds() {
        System.out.println("getBounds");
        LineSet instance = null;
        Rect expResult = null;
        Rect result = instance.getBounds();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of transform method, of class LineSet.
     */
    @Test
    public void testTransform() {
        System.out.println("transform");
        Transform transform = null;
        LineSet instance = null;
        LineSet expResult = null;
        LineSet result = instance.transform(transform);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of simplify method, of class LineSet.
     */
    @Test
    public void testSimplify() {
        System.out.println("simplify");
        LineSet instance = null;
        Geom expResult = null;
        Geom result = instance.simplify();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pathIterator method, of class LineSet.
     */
    @Test
    public void testPathIterator() {
        System.out.println("pathIterator");
        LineSet instance = null;
        PathIterator expResult = null;
        PathIterator result = instance.pathIterator();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of clone method, of class LineSet.
     */
    @Test
    public void testClone() {
        System.out.println("clone");
        LineSet instance = null;
        LineSet expResult = null;
        LineSet result = instance.clone();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class LineSet.
     */
    @Test
    public void testToString_0args() {
        System.out.println("toString");
        LineSet instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class LineSet.
     */
    @Test
    public void testToString_Appendable() {
        System.out.println("toString");
        Appendable appendable = null;
        LineSet instance = null;
        instance.toString(appendable);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toGeoShape method, of class LineSet.
     */
    @Test
    public void testToGeoShape_Tolerance_Tolerance() {
        System.out.println("toGeoShape");
        Tolerance flatness = null;
        Tolerance accuracy = null;
        LineSet instance = null;
        GeoShape expResult = null;
        GeoShape result = instance.toGeoShape(flatness, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toGeoShape method, of class LineSet.
     */
    @Test
    public void testToGeoShape_0args() {
        System.out.println("toGeoShape");
        LineSet instance = null;
        GeoShape expResult = null;
        GeoShape result = instance.toGeoShape();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addTo method, of class LineSet.
     */
    @Test
    public void testAddTo_3args() {
        System.out.println("addTo");
        Network network = null;
        Tolerance flatness = null;
        Tolerance accuracy = null;
        LineSet instance = null;
        instance.addTo(network, flatness, accuracy);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addTo method, of class LineSet.
     */
    @Test
    public void testAddTo_Network() {
        System.out.println("addTo");
        Network network = null;
        LineSet instance = null;
        instance.addTo(network);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buffer method, of class LineSet.
     */
    @Test
    public void testBuffer() {
        System.out.println("buffer");
        double amt = 0.0;
        Tolerance flatness = null;
        Tolerance accuracy = null;
        LineSet instance = null;
        Geom expResult = null;
        Geom result = instance.buffer(amt, flatness, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of relate method, of class LineSet.
     */
    @Test
    public void testRelate_Vect_Tolerance() {
        System.out.println("relate");
        Vect vect = null;
        Tolerance accuracy = null;
        LineSet instance = null;
        Relate expResult = null;
        Relate result = instance.relate(vect, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of relate method, of class LineSet.
     */
    @Test
    public void testRelate_VectBuilder_Tolerance() {
        System.out.println("relate");
        VectBuilder vect = null;
        Tolerance accuracy = null;
        LineSet instance = null;
        Relate expResult = null;
        Relate result = instance.relate(vect, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of union method, of class LineSet.
     */
    @Test
    public void testUnion_3args() {
        System.out.println("union");
        Geom other = null;
        Tolerance flatness = null;
        Tolerance accuracy = null;
        LineSet instance = null;
        Geom expResult = null;
        Geom result = instance.union(other, flatness, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of union method, of class LineSet.
     */
    @Test
    public void testUnion_LineSet_Tolerance() {
        System.out.println("union");
        LineSet other = null;
        Tolerance accuracy = null;
        LineSet instance = null;
        LineSet expResult = null;
        LineSet result = instance.union(other, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of union method, of class LineSet.
     */
    @Test
    public void testUnion_GeoShape_Tolerance() {
        System.out.println("union");
        GeoShape other = null;
        Tolerance accuracy = null;
        LineSet instance = null;
        GeoShape expResult = null;
        GeoShape result = instance.union(other, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of intersection method, of class LineSet.
     */
    @Test
    public void testIntersection_3args() {
        System.out.println("intersection");
        Geom other = null;
        Tolerance flatness = null;
        Tolerance accuracy = null;
        LineSet instance = null;
        Geom expResult = null;
        Geom result = instance.intersection(other, flatness, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of intersection method, of class LineSet.
     */
    @Test
    public void testIntersection_GeoShape_Tolerance() {
        System.out.println("intersection");
        GeoShape other = null;
        Tolerance accuracy = null;
        LineSet instance = null;
        GeoShape expResult = null;
        GeoShape result = instance.intersection(other, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of less method, of class LineSet.
     */
    @Test
    public void testLess_3args() {
        System.out.println("less");
        Geom other = null;
        Tolerance flatness = null;
        Tolerance accuracy = null;
        LineSet instance = null;
        Geom expResult = null;
        Geom result = instance.less(other, flatness, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of less method, of class LineSet.
     */
    @Test
    public void testLess_GeoShape_Tolerance() {
        System.out.println("less");
        GeoShape other = null;
        Tolerance accuracy = null;
        LineSet instance = null;
        LineSet expResult = null;
        LineSet result = instance.less(other, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hashCode method, of class LineSet.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        LineSet instance = null;
        int expResult = 0;
        int result = instance.hashCode();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of equals method, of class LineSet.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object obj = null;
        LineSet instance = null;
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
