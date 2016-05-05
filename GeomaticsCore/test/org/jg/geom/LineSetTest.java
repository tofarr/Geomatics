package org.jg.geom;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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
public class LineSetTest {

    static final Tolerance TOL = Tolerance.DEFAULT;

    @Test
    public void testValueOf_Tolerance_Network() {
        Network network = new Network();
        network.addAllLinks(new VectList(0, 0, 100, 0, 100, 100, 0, 100, 0, 0));
        network.addAllLinks(new VectList(10, 10, 90, 10, 90, 90));
        network.addVertex(10, 90);
        LineSet ls = LineSet.valueOf(TOL, network);
        assertEquals("[\"LT\",[0,0, 100,0, 100,100, 0,100, 0,0],[10,10, 90,10, 90,90]]", ls.toString());
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
        assertEquals("[\"LT\",[0,0, 50,50, 0,100, 0,0],[50,50, 100,0, 100,100, 50,50]]", ls.toString());
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
        assertEquals("[\"LT\",[0,20, 20,0, 40,20, 0,20],[40,20, 60,0, 80,20, 40,20]]", ls.toString());
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
        assertEquals("[\"LS\",0,20, 20,0, 40,20, 0,20]", ls.getLineString(0).toString());
        assertEquals("[\"LS\",40,20, 60,0, 80,20, 40,20]", ls.getLineString(1).toString());
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
        PathIter iter = ls.iterator();
        assertPath(iter, PathSegType.MOVE, 0, 0);
        iter.next();
        assertPath(iter, PathSegType.LINE, 50, 50);
        iter.next();
        assertPath(iter, PathSegType.LINE, 0, 100);
        iter.next();
        assertPath(iter, PathSegType.LINE, 0, 0);
        
        iter.next();
        assertPath(iter, PathSegType.MOVE, 50, 50);
        iter.next();
        assertPath(iter, PathSegType.LINE, 100, 0);
        iter.next();
        assertPath(iter, PathSegType.LINE, 100, 100);
        iter.next();
        assertPath(iter, PathSegType.LINE, 50, 50);
        
        iter.next();
        assertTrue(iter.isDone());
        iter.next();
        assertTrue(iter.isDone());
    }
    
    static void assertPath(PathIter iter, PathSegType expectedResult, double... expectedCoords){
        double[] coords = new double[6];
        expectedCoords = Arrays.copyOf(expectedCoords, 6);
        assertFalse(iter.isDone());
        assertEquals(expectedResult, iter.currentSegment(coords));
        for(int i = 6; i-- > 0;){
            assertEquals(expectedCoords[i], coords[i], 0.0001);
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
        assertEquals("[\"LT\",[0,0, 50,50, 0,100, 0,0],[50,50, 100,0, 100,100, 50,50]]", ls.toString());
    }

    @Test
    public void testToGeoShape() {
        LineSet ls = LineSet.valueOf(TOL, new VectList(0,0, 100,100, 100,0, 0,100, 0,0));
        assertEquals(new GeoShape(null, ls, null), ls.toGeoShape(Linearizer.DEFAULT, TOL));
    }

    @Test
    public void testAddTo() {
        LineSet ls = LineSet.valueOf(TOL, new VectList(0,0, 100,100, 100,0, 0,100, 0,0));
        Network network = new Network();
        network.addLink(0, 100, 30, 80);
        ls.addTo(network, Linearizer.DEFAULT, TOL);
        assertEquals("[[0,100, 0,0, 50,50],[0,100, 30,80],[0,100, 50,50],[50,50, 100,0, 100,100, 50,50]]", network.toString());
    }

    @Test
    public void testBuffer() {
        LineSet ls = LineSet.valueOf(TOL, new VectList(0,0, 100,100, 100,0, 0,100, 0,0));
        assertNull(ls.buffer(-1, Linearizer.DEFAULT, TOL));
        assertSame(ls, ls.buffer(0, Linearizer.DEFAULT, TOL));
        Area area = (Area)ls.buffer(5, Linearizer.DEFAULT, TOL);
        assertEquals(3, area.numRings());
        assertNotNull(area.shell);
        assertEquals(2, area.numChildren());
        Rect bounds = area.getBounds();
        assertEquals(-5, bounds.minX, 0.1);
        assertEquals(-5, bounds.minY, 0.1);
        assertEquals(105, bounds.maxX, 0.1);
        assertEquals(105, bounds.maxY, 0.1);
        assertEquals(4604, area.getArea(), 1);

        assertEquals(Relation.DISJOINT, area.relate(-10, 50, TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, area.relate(Vect.valueOf(-5, 50), TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, area.relate(new VectBuilder(0, 50), TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, area.relate(5, 50, TOL));
        assertEquals(Relation.DISJOINT, area.relate(25, 50, TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, area.relate(50, 50, TOL));
        assertEquals(Relation.DISJOINT, area.relate(75, 50, TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, area.relate(95, 50, TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, area.relate(100, 50, TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, area.relate(105, 50, TOL));
        assertEquals(Relation.DISJOINT, area.relate(110, 50, TOL));
    }

    @Test
    public void testRelate_Vect_Tolerance() {
        LineSet ls = LineSet.valueOf(TOL, 0,0, 100,100, 200,0, 200,100, 100,0, 0,100);
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ls.relate(Vect.valueOf(0, 0), TOL));
        assertEquals(Relation.DISJOINT, ls.relate(Vect.valueOf(-10, 0), TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ls.relate(Vect.valueOf(50, 50), TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ls.relate(Vect.valueOf(100, 100), TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ls.relate(Vect.valueOf(150, 50), TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ls.relate(Vect.valueOf(200, 0), TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ls.relate(Vect.valueOf(200, 50), TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ls.relate(Vect.valueOf(200, 100), TOL));
        assertEquals(Relation.DISJOINT, ls.relate(Vect.valueOf(210, 50), TOL));
        assertEquals(Relation.DISJOINT, ls.relate(Vect.valueOf(200, 110), TOL));
        assertEquals(Relation.DISJOINT, ls.relate(Vect.valueOf(175, 50), TOL));
        assertEquals(Relation.DISJOINT, ls.relate(Vect.valueOf(25, 50), TOL));
        try{
            ls.relate((Vect)null, TOL);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        try{
            ls.relate(Vect.valueOf(0, 0), null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
    }

    @Test
    public void testRelate_VectBuilder_Tolerance() {
        LineSet ls = LineSet.valueOf(TOL, 0,0, 100,100, 200,0, 200,100, 100,0, 0,100);
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ls.relate(new VectBuilder(0, 0), TOL));
        assertEquals(Relation.DISJOINT, ls.relate(new VectBuilder(-10, 0), TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ls.relate(new VectBuilder(50, 50), TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ls.relate(new VectBuilder(100, 100), TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ls.relate(new VectBuilder(150, 50), TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ls.relate(new VectBuilder(200, 0), TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ls.relate(new VectBuilder(200, 50), TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ls.relate(new VectBuilder(200, 100), TOL));
        assertEquals(Relation.DISJOINT, ls.relate(new VectBuilder(210, 50), TOL));
        assertEquals(Relation.DISJOINT, ls.relate(new VectBuilder(200, 110), TOL));
        assertEquals(Relation.DISJOINT, ls.relate(new VectBuilder(175, 50), TOL));
        assertEquals(Relation.DISJOINT, ls.relate(new VectBuilder(25, 50), TOL));
        try{
            ls.relate((VectBuilder)null, TOL);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        try{
            ls.relate(new VectBuilder(0, 0), null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
    }

    @Test
    public void testRelate_Geom() {
        LineSet ls = LineSet.valueOf(TOL, 0,0, 100,100, 200,0, 200,100, 100,0, 0,100);
        assertEquals(Relation.TOUCH, ls.relate(ls, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.TOUCH | Relation.A_INSIDE_B | Relation.B_OUTSIDE_A, ls.relate(ls.getBounds(), Linearizer.DEFAULT, TOL));
        assertEquals(Relation.TOUCH | Relation.A_INSIDE_B | Relation.A_OUTSIDE_B | Relation.B_OUTSIDE_A, ls.relate(Rect.valueOf(0,0,100,200), Linearizer.DEFAULT, TOL));
        try{
            ls.relate(null, Linearizer.DEFAULT, TOL);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
    }
    
    @Test
    public void testRelate_Geom_B() {
        LineSet a = LineSet.valueOf(TOL, 0,0, 100,0, 100,100, 0,100, 0,0);
        Ring b = Ring.valueOf(Tolerance.DEFAULT, 0,0, 100,0, 100,100, 0,100, 0,0);
        assertEquals(Relation.TOUCH | Relation.B_OUTSIDE_A, a.relate(b, Linearizer.DEFAULT, Tolerance.DEFAULT));
    }

    @Test
    public void testUnion() {
        LineSet a = LineSet.valueOf(TOL, 0,40, 40,40, 40,0);
        LineString b = LineString.valueOf(TOL, 0,60, 40,60, 40,100);
        LineSet c = LineSet.valueOf(TOL, 60,20, 20,20, 20,80, 60,80);
        Rect d = Rect.valueOf(0,60, 40,100);
        assertEquals(new LineSet(a.getLineString(0), b), a.union(b, Linearizer.DEFAULT, TOL));
        assertEquals(new LineSet(a.getLineString(0), b), a.union(b.toLineSet(), TOL));
        assertEquals("[\"LT\",[0,40, 20,40],[20,40, 20,20, 40,20],[20,40, 40,40, 40,20],[20,40, 20,80, 60,80],[40,0, 40,20],[40,20, 60,20]]",
                a.union(c, Linearizer.DEFAULT, TOL).toString());
        assertEquals(new GeoShape(d.toArea(), a, null), a.union(d, Linearizer.DEFAULT, TOL));
        assertEquals(Ring.valueOf(TOL, 0,60, 40,60, 40,100, 0,100, 0,60), b.toLineSet().union(d, Linearizer.DEFAULT, TOL));
        Geom e = c.union(d, Linearizer.DEFAULT, TOL);
        assertEquals("[\"GS\",[[0,60, 40,60, 40,100, 0,100, 0,60]],[[20,60, 20,20, 60,20],[40,80, 60,80]],null]",
                e.toString());
        LineSet f = LineSet.valueOf(TOL, 60,140, 80,140, 80,120);
        assertEquals("[\"GS\",[[0,60, 40,60, 40,100, 0,100, 0,60]],[[20,60, 20,20, 60,20],[40,80, 60,80],[60,140, 80,140, 80,120]],null]",
                f.union(e, Linearizer.DEFAULT, TOL).toString());
    }

    @Test
    public void testIntersection() {
        LineSet a = LineSet.valueOf(TOL, 0,40, 40,40, 40,0);
        LineString b = LineString.valueOf(TOL, 0,60, 40,60, 40,100);
        LineSet c = LineSet.valueOf(TOL, 60,20, 20,20, 20,80, 60,80);
        Rect d = Rect.valueOf(0,60, 40,100);
        assertNull(a.intersection(b, Linearizer.DEFAULT, TOL));
        assertNull(a.intersection(b.toGeoShape(Linearizer.DEFAULT, TOL), Linearizer.DEFAULT, TOL));
        assertEquals(PointSet.valueOf(new VectSet().add(20,40).add(40,20)),
                a.intersection(c, Linearizer.DEFAULT, TOL));
        assertNull(a.intersection(d, Linearizer.DEFAULT, TOL));
        assertEquals(LineString.valueOf(TOL, 0,60, 40,60, 40,100), b.toLineSet().intersection(d, Linearizer.DEFAULT, TOL));
        assertEquals(LineString.valueOf(TOL, 20,60, 20,80, 40,80),
                c.intersection(d, Linearizer.DEFAULT, TOL));
    }

    @Test
    public void testLess() {
        LineSet a = LineSet.valueOf(TOL, 0,40, 40,40, 40,0);
        LineString b = LineString.valueOf(TOL, 0,60, 40,60, 40,100);
        LineSet c = LineSet.valueOf(TOL, 60,20, 20,20, 20,80, 60,80);
        Rect d = Rect.valueOf(0,60, 40,100);
        assertSame(a, a.less(b, Linearizer.DEFAULT, TOL));
        assertSame(a, a.less(b.toGeoShape(Linearizer.DEFAULT, TOL), Linearizer.DEFAULT, TOL));
        assertEquals(LineSet.valueOf(TOL, 0,40, 20,40, 40,40, 40,20, 40,0),
                a.less(c, Linearizer.DEFAULT, TOL));
        assertSame(a, a.less(d, Linearizer.DEFAULT, TOL));
        assertNull(b.toLineSet().less(d, Linearizer.DEFAULT, TOL));
        assertEquals("[\"LT\",[20,60, 20,20, 60,20],[40,80, 60,80]]",
                c.less(d, Linearizer.DEFAULT, TOL).toString());
    }


    @Test
    public void testXor() {
        LineSet a = LineSet.valueOf(TOL, 0,40, 40,40, 40,0);
        LineString b = LineString.valueOf(TOL, 0,60, 40,60, 40,100);
        LineSet c = LineSet.valueOf(TOL, 60,20, 20,20, 20,80, 60,80);
        Rect d = Rect.valueOf(0,60, 40,100);
        assertEquals(a.union(b, TOL), a.xor(b, Linearizer.DEFAULT, TOL));
        assertEquals(a.union(b, TOL), a.xor(b.toGeoShape(Linearizer.DEFAULT, TOL), Linearizer.DEFAULT, TOL));
        assertEquals(a.union(c, TOL), a.xor(c, Linearizer.DEFAULT, TOL));
        assertEquals(new GeoShape(d.toArea(), a.less(d, Linearizer.DEFAULT, TOL), null), a.xor(d, Linearizer.DEFAULT, TOL));
        assertEquals(d.toRing(), b.toLineSet().xor(d, Linearizer.DEFAULT, TOL));
        assertEquals("[\"GS\",[[0,60, 40,60, 40,100, 0,100, 0,60]],[[20,60, 20,20, 60,20],[40,80, 60,80]],null]",
                c.xor(d, Linearizer.DEFAULT, TOL).toString());
        assertNull(a.xor(a, Linearizer.DEFAULT, TOL));
        assertNull(a.xor(a.toGeoShape(), Linearizer.DEFAULT, TOL));
    }

    @Test
    public void testEquals() {
        LineSet a = LineSet.valueOf(TOL, 0,0, 100,100, 100,0, 0,100, 0,0);
        LineSet b = LineSet.valueOf(TOL, 100,100, 0,0, 0,100, 100,0, 100,100);
        LineSet c = LineSet.valueOf(TOL, 0,0, 50,50, 0,100, 0,0);
        LineSet d = LineSet.valueOf(TOL, 0,0, 100,100, 90,10, 0,100, 0,0);
        assertEquals(a, a);
        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, d);
        assertNotEquals(a, "");
    }

    @Test
    public void testHashCode() {
        Set<Integer> hashCodes = new HashSet<>();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                LineSet ls = LineSet.valueOf(TOL, 0, 0, x, y, 10, 10);
                hashCodes.add(ls.hashCode());
            }
        }
        assertEquals(100, hashCodes.size());
    }
          
    @Test
    public void testGetArea(){
        LineSet a = LineSet.valueOf(TOL, 0,0, 100,100, 100,0, 0,100, 0,0);
        assertEquals(0, a.getArea(Linearizer.DEFAULT, Tolerance.DEFAULT), 0);
    }
}
