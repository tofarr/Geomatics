package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.IOException;
import java.util.Arrays;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.TransformBuilder;
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

    @Test
    public void testNumLineStrings() {
        LineSet ls = LineSet.valueOf(TOL, 0,20, 80,20, 60,0, 40,20, 20,0, 0,20);
        assertEquals(2, ls.numLineStrings());
    }

    @Test
    public void testGetLineString() {
        LineSet ls = LineSet.valueOf(TOL, 0,20, 80,20, 60,0, 40,20, 20,0, 0,20);
        assertEquals("[\"LS\", 0,20, 20,0, 40,20, 0,20]", ls.getLineString(0).toString());
        assertEquals("[\"LS\", 40,20, 60,0, 80,20, 40,20]", ls.getLineString(1).toString());
        try{
            ls.getLineString(-1);
            fail("Exception expected");
        }catch(IndexOutOfBoundsException ex){
        }
        try{
            ls.getLineString(2);
            fail("Exception expected");
        }catch(IndexOutOfBoundsException ex){
        }
    }

    @Test
    public void testGetBounds() {
        LineSet ls = LineSet.valueOf(TOL, 0,20, 80,20, 60,0, 20,40, 0,20);
        assertEquals(Rect.valueOf(0,0,80,40), ls.getBounds());
    }

    @Test
    public void testTransform() {
        LineSet a = LineSet.valueOf(TOL, 0,20, 80,20, 60,0, 20,40, 0,20);
        assertSame(a, a.transform(Transform.IDENTITY));
        LineSet b = a.transform(new TransformBuilder().translate(2, 3).build());
        LineSet c = LineSet.valueOf(TOL, 2,23, 82,23, 62,3, 22,43, 2,23);
        assertEquals(c, b);
    }

    @Test
    public void testSimplify() {
        LineSet a = LineSet.valueOf(TOL, 0,20, 80,20, 60,0, 20,40, 0,20);
        assertSame(a, a.simplify());
        LineSet b = LineSet.valueOf(TOL, 4,5, 2,3);
        assertEquals(Line.valueOf(2,3, 4,5), b.simplify());
    }

    @Test
    public void testPathIterator() {
        LineSet ls = LineSet.valueOf(TOL, new VectList(0,0, 100,100, 100,0, 0,100, 0,0));
        PathIterator iter = ls.pathIterator();
        assertPath(iter, PathIterator.SEG_MOVETO, 0, 0);
        iter.next();
        assertPath(iter, PathIterator.SEG_LINETO, 50, 50);
        iter.next();
        assertPath(iter, PathIterator.SEG_LINETO, 0, 100);
        iter.next();
        assertPath(iter, PathIterator.SEG_LINETO, 0, 0);
        
        iter.next();
        assertPath(iter, PathIterator.SEG_MOVETO, 50, 50);
        iter.next();
        assertPath(iter, PathIterator.SEG_LINETO, 100, 0);
        iter.next();
        assertPath(iter, PathIterator.SEG_LINETO, 100, 100);
        iter.next();
        assertPath(iter, PathIterator.SEG_LINETO, 50, 50);
        
        iter.next();
        assertTrue(iter.isDone());
    }
    
    void assertPath(PathIterator iter, int expectedResult, double... expectedCoords){
        double[] coords = new double[6];
        float[] fcoords = new float[6];
        expectedCoords = Arrays.copyOf(expectedCoords, 6);
        assertFalse(iter.isDone());
        assertEquals(expectedResult, iter.currentSegment(coords));
        assertEquals(expectedResult, iter.currentSegment(fcoords));
        for(int i = 6; i-- > 0;){
            assertEquals(expectedCoords[i], coords[i], 0.0001);
            assertEquals(expectedCoords[i], fcoords[i], 0.0001);
        }
    }
    
    @Test
    public void testClone() {
        LineSet ls = LineSet.valueOf(TOL, new VectList(0,0, 100,100, 100,0, 0,100, 0,0));
        assertSame(ls, ls.clone());
    }

    @Test
    public void testToString() {
        LineSet ls = LineSet.valueOf(TOL, new VectList(0,0, 100,100, 100,0, 0,100, 0,0));
        assertEquals("[\"LT\", [0,0, 50,50, 0,100, 0,0], [50,50, 100,0, 100,100, 50,50]]", ls.toString());
        try{
            ls.toString(new Appendable(){
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
        LineSet ls = LineSet.valueOf(TOL, new VectList(0,0, 100,100, 100,0, 0,100, 0,0));
        assertEquals(new GeoShape(null, ls, null), ls.toGeoShape(Tolerance.FLATNESS, TOL));
    }

    @Test
    public void testAddTo() {
        LineSet ls = LineSet.valueOf(TOL, new VectList(0,0, 100,100, 100,0, 0,100, 0,0));
        Network network = new Network();
        network.addLink(0, 100, 30, 80);
        ls.addTo(network, Tolerance.FLATNESS, TOL);
        assertEquals("[[0,100, 0,0, 50,50],[0,100, 30,80],[0,100, 50,50],[50,50, 100,0, 100,100, 50,50]]", network.toString());
    }

    @Test
    public void testBuffer() {
        LineSet ls = LineSet.valueOf(TOL, new VectList(0,0, 100,100, 100,0, 0,100, 0,0));
        assertNull(ls.buffer(-1, Tolerance.FLATNESS, TOL));
        assertSame(ls, ls.buffer(0, Tolerance.FLATNESS, TOL));
        Area area = (Area)ls.buffer(5, Tolerance.FLATNESS, TOL);
        assertEquals(3, area.numRings());
        assertNotNull(area.shell);
        assertEquals(2, area.numChildren());
        Rect bounds = area.getBounds();
        assertEquals(-5, bounds.minX, 0.1);
        assertEquals(-5, bounds.minY, 0.1);
        assertEquals(105, bounds.maxX, 0.1);
        assertEquals(105, bounds.maxY, 0.1);
        assertEquals(4604, area.getArea(), 1);

        assertEquals(Relate.OUTSIDE, area.relate(-10, 50, TOL));
        assertEquals(Relate.TOUCH, area.relate(Vect.valueOf(-5, 50), TOL));
        assertEquals(Relate.INSIDE, area.relate(new VectBuilder(0, 50), TOL));
        assertEquals(Relate.TOUCH, area.relate(5, 50, TOL));
        assertEquals(Relate.OUTSIDE, area.relate(25, 50, TOL));
        assertEquals(Relate.INSIDE, area.relate(50, 50, TOL));
        assertEquals(Relate.OUTSIDE, area.relate(75, 50, TOL));
        assertEquals(Relate.TOUCH, area.relate(95, 50, TOL));
        assertEquals(Relate.INSIDE, area.relate(100, 50, TOL));
        assertEquals(Relate.TOUCH, area.relate(105, 50, TOL));
        assertEquals(Relate.OUTSIDE, area.relate(110, 50, TOL));
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
