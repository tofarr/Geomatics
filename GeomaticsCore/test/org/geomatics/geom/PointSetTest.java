package org.geomatics.geom;

import org.geomatics.geom.Area;
import org.geomatics.geom.PathSegType;
import org.geomatics.geom.Relation;
import org.geomatics.geom.GeoShape;
import org.geomatics.geom.PathIter;
import org.geomatics.geom.VectBuilder;
import org.geomatics.geom.Linearizer;
import org.geomatics.geom.Network;
import org.geomatics.geom.PointSet;
import org.geomatics.geom.Rect;
import org.geomatics.geom.Vect;
import org.geomatics.geom.Ring;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.geomatics.util.Tolerance;
import org.geomatics.util.Transform;
import org.geomatics.util.TransformBuilder;
import org.geomatics.util.VectList;
import org.geomatics.util.VectSet;
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
        try {
            ps = PointSet.valueOf((VectSet) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
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
        Network network = new Network().addAllLinks(new VectList(0, 0, 100, 0, 0, 100));
        network.addVertex(50, 50); //network is not explicitized as part of this operation
        network.addVertex(100, 100);
        network.addVertex(1, 2);
        PointSet ps = PointSet.valueOf(network, Tolerance.DEFAULT);
        try {
            ps = PointSet.valueOf(null, Tolerance.DEFAULT);
            fail("Exception expected");
        } catch (NullPointerException ex) {
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
        assertEquals(Rect.valueOf(1, 2, 5, 6), ps.getBounds());
    }

    @Test
    public void testTransform() {
        PointSet ps = PointSet.valueOf(new VectSet().addAll(new VectList(1, 3, 7, 13)));
        PointSet result = ps.transform(new TransformBuilder().scale(2, 3).build());
        PointSet expected = PointSet.valueOf(new VectSet().addAll(new VectList(2, 9, 14, 39)));
        try {
            result.transform(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        assertEquals(expected, result);
        assertSame(ps, ps.transform(Transform.IDENTITY));
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
        PathIter iter = ps.iterator();

        assertFalse(iter.isDone());

        double[] coords = new double[6];
        assertEquals(PathSegType.MOVE, iter.currentSegment(coords));
        assertEquals(1, coords[0], 0.0001);
        assertEquals(3, coords[1], 0.0001);

        iter.next();
        assertFalse(iter.isDone());
        assertEquals(PathSegType.LINE, iter.currentSegment(coords));
        assertEquals(1, coords[0], 0.0001);
        assertEquals(3, coords[1], 0.0001);

        iter.next();
        assertFalse(iter.isDone());
        assertEquals(PathSegType.MOVE, iter.currentSegment(coords));
        assertEquals(7, coords[0], 0.0001);
        assertEquals(13, coords[1], 0.0001);

        iter.next();
        assertFalse(iter.isDone());
        assertEquals(PathSegType.LINE, iter.currentSegment(coords));
        assertEquals(7, coords[0], 0.0001);
        assertEquals(13, coords[1], 0.0001);

        iter.next();
        assertFalse(iter.isDone());
        assertEquals(PathSegType.MOVE, iter.currentSegment(coords));
        assertEquals(23, coords[0], 0.0001);
        assertEquals(29, coords[1], 0.0001);

        iter.next();
        assertFalse(iter.isDone());
        assertEquals(PathSegType.LINE, iter.currentSegment(coords));
        assertEquals(23, coords[0], 0.0001);
        assertEquals(29, coords[1], 0.0001);

        iter.next();
        assertTrue(iter.isDone());
        iter.next();
        assertTrue(iter.isDone());
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
        assertEquals("[\"PS\",1,3, 7,13, 23,29]", ps.toString());
    }

    @Test
    public void testToGeoShape() {
        PointSet ps = PointSet.valueOf(new VectSet().addAll(new VectList(1, 3, 7, 13, 23, 29)));
        GeoShape gs = ps.toGeoShape(Linearizer.DEFAULT, Tolerance.DEFAULT);
        assertEquals(new GeoShape(null, null, ps), gs);
    }

    @Test
    public void testAddTo() {
        VectList vects = new VectList(1, 3, 7, 13, 23, 29);
        PointSet ps = PointSet.valueOf(new VectSet().addAll(vects));
        Network network = new Network();
        network.addLink(1, 3, 2, 3);
        ps.addTo(network, Linearizer.DEFAULT, Tolerance.DEFAULT);
        assertEquals("GEOMETRYCOLLECTION(POINT(7 13),POINT(23 29),LINESTRING(1 3, 2 3))", network.toWkt());
    }

    @Test
    public void testBuffer() {
        VectList vects = new VectList(5, 5, 5, 7, 7, 5, 14, 5, 22, 5, 31, 5);
        PointSet ps = PointSet.valueOf(new VectSet().addAll(vects));
        assertNull(ps.buffer(-1, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertSame(ps, ps.buffer(0, Linearizer.DEFAULT, Tolerance.DEFAULT));
        Area buffered = (Area) ps.buffer(4, new Linearizer(4, Tolerance.DEFAULT), Tolerance.DEFAULT);
        assertEquals(Rect.valueOf(1,1,35,11), buffered.getBounds());
        assertEquals(3, buffered.numRings());
        assertEquals(222, buffered.getArea(), 1);
    }
    
    @Test
    public void testBuffer2(){
        VectList vects = new VectList(100, 100, 107.5, 100);
        PointSet ps = PointSet.valueOf(new VectSet().addAll(vects));
        Area buffered = (Area) ps.buffer(4, Linearizer.DEFAULT, Tolerance.DEFAULT);
        assertEquals(1, buffered.numRings());
        Rect bounds = buffered.getBounds();
        assertEquals(96, bounds.minX, 0.00001);
        assertEquals(96, bounds.minY, 0.00001);
        assertEquals(111.5, bounds.maxX, 0.00001);
        assertEquals(104, bounds.maxY, 0.00001);
        assertEquals(99, buffered.getArea(), 1);
    }

    @Test
    public void testRelate_Vect() {
        VectList vects = new VectList(5, 5, 5, 7, 7, 5, 14, 5, 22, 5, 31, 5);
        PointSet ps = PointSet.valueOf(new VectSet().addAll(vects));
        assertEquals(ps.relate(Vect.ZERO, Tolerance.DEFAULT), Relation.DISJOINT);
        assertEquals(ps.relate(Vect.valueOf(5, 5), Tolerance.DEFAULT), Relation.TOUCH | Relation.A_OUTSIDE_B);
        assertEquals(ps.relate(Vect.valueOf(14.1, 5.1), Tolerance.DEFAULT), Relation.DISJOINT);
        assertEquals(ps.relate(Vect.valueOf(14.1, 5.1), new Tolerance(0.2)), Relation.TOUCH | Relation.A_OUTSIDE_B);
        assertEquals(ps.relate(new VectBuilder(), Tolerance.DEFAULT), Relation.DISJOINT);
        assertEquals(ps.relate(new VectBuilder(5, 5), Tolerance.DEFAULT), Relation.TOUCH | Relation.A_OUTSIDE_B);
        assertEquals(ps.relate(new VectBuilder(14.1, 5.1), Tolerance.DEFAULT), Relation.DISJOINT);
        assertEquals(ps.relate(new VectBuilder(14.1, 5.1), new Tolerance(0.2)), Relation.TOUCH | Relation.A_OUTSIDE_B);
        try {
            ps.relate((Vect) null, Tolerance.DEFAULT);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            ps.relate(Vect.ZERO, null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            ps.relate((VectBuilder) null, Tolerance.DEFAULT);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            ps.relate(new VectBuilder(), null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testRelate_PointSet() {
        PointSet a = PointSet.valueOf(1,2, 3,4, 5,6, 7,8);
        PointSet b = PointSet.valueOf(5,6, 7,8, 9,10, 11,12);
        PointSet c = PointSet.valueOf(9,10, 11,12, 13,14, 15,16);
        PointSet d = PointSet.valueOf(13,14, 15,16, 17,18, 19,20);
        PointSet e = PointSet.valueOf(5.1,6, 7,8.2);
        
        assertEquals(Relation.TOUCH, a.relate(a, Tolerance.DEFAULT));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B | Relation.B_OUTSIDE_A, a.relate(b, Tolerance.DEFAULT));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B | Relation.B_OUTSIDE_A, b.relate(a, Tolerance.DEFAULT));
        assertEquals(Relation.DISJOINT, a.relate(c, Tolerance.DEFAULT));
        assertEquals(Relation.DISJOINT, a.relate(d, Tolerance.DEFAULT));
        assertEquals(Relation.DISJOINT, a.relate(e, Tolerance.DEFAULT));
        assertEquals(Relation.DISJOINT, e.relate(a, Tolerance.DEFAULT));
        
        assertEquals(Relation.A_OUTSIDE_B | Relation.TOUCH | Relation.B_OUTSIDE_A, a.relate(e, new Tolerance(0.11)));
        assertEquals(Relation.A_OUTSIDE_B | Relation.TOUCH | Relation.B_OUTSIDE_A, e.relate(a, new Tolerance(0.11)));
        
        assertEquals(Relation.A_OUTSIDE_B | Relation.TOUCH, a.relate(e, new Tolerance(0.22)));
        assertEquals(Relation.TOUCH | Relation.B_OUTSIDE_A, e.relate(a, new Tolerance(0.22)));
    }
    
    @Test
    public void testRelate_Geom() {
        PointSet a = PointSet.valueOf(1,2, 3,4, 5,6, 7,8);
        PointSet b = PointSet.valueOf(5,6, 7,8, 9,10, 11,12);
        Rect c = Rect.valueOf(9,10,11,12);
        Rect d = Rect.valueOf(1,2,7,8);
        Rect e = Rect.valueOf(0,1,8,9);
        Rect f = Rect.valueOf(3,4,5,6);
        
        assertEquals(Relation.TOUCH, a.relate(a, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B | Relation.B_OUTSIDE_A, a.relate(b, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(Relation.DISJOINT, a.relate(c, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(Relation.A_OUTSIDE_B | Relation.TOUCH | Relation.B_OUTSIDE_A, b.relate(c, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(Relation.A_INSIDE_B | Relation.TOUCH | Relation.B_OUTSIDE_A, a.relate(d, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(Relation.A_INSIDE_B | Relation.B_OUTSIDE_A, a.relate(e, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(Relation.A_OUTSIDE_B | Relation.TOUCH | Relation.B_OUTSIDE_A, a.relate(f, Linearizer.DEFAULT, Tolerance.DEFAULT));
        
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, a.relate(Vect.valueOf(3, 4), Linearizer.DEFAULT, Tolerance.DEFAULT));
        
        PointSet g = PointSet.valueOf(1,2, 3,4, 7,8, 9,10);
        assertEquals(Relation.A_OUTSIDE_B | Relation.TOUCH | Relation.B_OUTSIDE_A, a.relate(g, Linearizer.DEFAULT, Tolerance.DEFAULT));
    }

    @Test
    public void testUnion_Vect() {
        VectList vects = new VectList(5, 5, 5, 7, 7, 5, 14, 5, 22, 5, 31, 5);
        PointSet ps = PointSet.valueOf(new VectSet().addAll(vects));

        Vect touch = Vect.valueOf(14, 5);
        assertSame(ps, ps.union(touch, Linearizer.DEFAULT, Tolerance.DEFAULT));

        Vect outside = Vect.valueOf(14, 6);
        vects.add(14, 6);
        assertEquals(PointSet.valueOf(new VectSet().addAll(vects)), ps.union(outside, Linearizer.DEFAULT, Tolerance.DEFAULT));

        Vect outsideBounds = Vect.valueOf(14, 8);
        vects.set(vects.size() - 1, outsideBounds);
        assertEquals(PointSet.valueOf(new VectSet().addAll(vects)), ps.union(outsideBounds, Linearizer.DEFAULT, Tolerance.DEFAULT));
    }

    @Test
    public void testUnion_PointSet() {

        PointSet a = PointSet.valueOf(new VectSet().addAll(new VectList(2, 3, 5, 7, 11, 13)));
        PointSet b = PointSet.valueOf(new VectSet().addAll(new VectList(11, 13, 17, 19, 23, 29)));
        PointSet c = PointSet.valueOf(new VectSet().addAll(new VectList(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)));
        PointSet d = PointSet.valueOf(new VectSet().addAll(new VectList(17, 19, 23, 29)));

        assertSame(c, a.union(c, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertSame(c, c.union(a, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(c, a.union(b, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(c, a.union(d, Linearizer.DEFAULT, Tolerance.DEFAULT));
    }

    @Test
    public void testUnion() {

        PointSet a = PointSet.valueOf(new VectSet().addAll(new VectList(2, 3, 5, 7, 11, 13)));
        GeoShape b = Rect.valueOf(4, 7, 12, 14).toArea().toGeoShape();
        GeoShape c = new GeoShape(b.area, null, PointSet.valueOf(new VectSet().add(2, 3)));
        Ring d = Rect.valueOf(1, 2, 11, 14).toRing();

        assertEquals(c, a.union(b, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(c, a.union(c, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(d, a.union(d, Linearizer.DEFAULT, Tolerance.DEFAULT));

        try {
            a.union(null, Linearizer.DEFAULT, Tolerance.DEFAULT);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testIntersection() {

        PointSet a = PointSet.valueOf(new VectSet().addAll(new VectList(2, 3, 5, 7, 11, 13)));
        Rect b = Rect.valueOf(4, 7, 12, 14);
        PointSet c = PointSet.valueOf(new VectSet().add(5, 7).add(11, 13));
        Vect d = Vect.valueOf(2, 3);
        GeoShape e = new GeoShape(b.toArea(), null, d.toPointSet());
        Rect f = Rect.valueOf(13, 1, 14, 2);
        Vect g = Vect.valueOf(2, 4);
        
        assertEquals(a, a.intersection(a, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(c, a.intersection(b, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(c, a.intersection(c, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(d, a.intersection(d, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(a, a.intersection(e, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertNull(a.intersection(f, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertNull(a.intersection(g, Linearizer.DEFAULT, Tolerance.DEFAULT));
        
        try {
            a.intersection(null, Linearizer.DEFAULT, Tolerance.DEFAULT);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testLess() {

        PointSet a = PointSet.valueOf(new VectSet().addAll(new VectList(2, 3, 5, 7, 11, 13)));
        Rect b = Rect.valueOf(4, 7, 12, 14);
        PointSet c = PointSet.valueOf(new VectSet().add(5, 7).add(11, 13));
        Vect d = Vect.valueOf(2, 3);
        GeoShape e = new GeoShape(b.toArea(), null, d.toPointSet());
        Ring f = Rect.valueOf(13, 1, 14, 2).toRing();
        Vect g = Vect.valueOf(2, 4);
        
        
        assertNull(a.less(a, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(d, a.less(b, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(d, a.less(c, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(c, a.less(d, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertNull(a.less(e, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(a, a.less(f, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(a, a.less(g, Linearizer.DEFAULT, Tolerance.DEFAULT));
        
        try {
            a.less(null, Linearizer.DEFAULT, Tolerance.DEFAULT);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testXor() {

        PointSet a = PointSet.valueOf(new VectSet().addAll(new VectList(2, 3, 5, 7, 11, 13)));
        Rect b = Rect.valueOf(4, 7, 12, 14);
        PointSet c = PointSet.valueOf(new VectSet().add(5, 7).add(11, 13));
        Vect d = Vect.valueOf(2, 3);
        GeoShape e = new GeoShape(b.toArea(), null, d.toPointSet());
        Ring f = Rect.valueOf(13, 1, 14, 2).toRing();
        Vect g = Vect.valueOf(2, 4);
        
        assertNull(a.xor(a, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(e, a.xor(b, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(d, a.xor(c, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(d, c.xor(a, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(c, a.xor(d, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(b.toRing(), a.xor(e, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(new GeoShape(f.toArea(), null, a), a.xor(f, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(new PointSet(new VectList(2,3, 2,4, 5,7, 11,13)), a.xor(g, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertNull(d.toPointSet().xor(d, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertNull(d.toPointSet().xor(d.toGeoShape(Linearizer.DEFAULT, Tolerance.DEFAULT), Linearizer.DEFAULT, Tolerance.DEFAULT));
        
        try {
            a.xor(null, Linearizer.DEFAULT, Tolerance.DEFAULT);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testGetPoint_int() {
        PointSet ps = PointSet.valueOf(new VectSet().add(3, 4).add(1, 2).add(3, 3).add(3, 4));
        assertEquals(Vect.valueOf(1, 2), ps.getPoint(0));
        assertEquals(Vect.valueOf(3, 3), ps.getPoint(1));
        assertEquals(Vect.valueOf(3, 4), ps.getPoint(2));
        try {
            ps.getPoint(-1);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            ps.getPoint(3);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    @Test
    public void testGetPoint_int_VectBuilder() {
        PointSet ps = PointSet.valueOf(new VectSet().add(3, 4).add(1, 2).add(3, 3).add(3, 4));
        VectBuilder vect = new VectBuilder();
        assertSame(vect, ps.getPoint(0, vect));
        assertEquals(new VectBuilder(1, 2), vect);
        assertSame(vect, ps.getPoint(1, vect));
        assertEquals(new VectBuilder(3, 3), vect);
        assertSame(vect, ps.getPoint(2, vect));
        assertEquals(new VectBuilder(3, 4), vect);
        try {
            ps.getPoint(-1, vect);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            ps.getPoint(3, vect);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            ps.getPoint(0, null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testGetXY() {
        PointSet ps = PointSet.valueOf(new VectSet().add(3, 4).add(1, 2).add(3, 3).add(3, 4));
        assertEquals(1, ps.getX(0), 0.0001);
        assertEquals(2, ps.getY(0), 0.0001);
        assertEquals(3, ps.getX(1), 0.0001);
        assertEquals(3, ps.getY(1), 0.0001);
        assertEquals(3, ps.getX(2), 0.0001);
        assertEquals(4, ps.getY(2), 0.0001);
        
        try {
            ps.getX(-1);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            ps.getY(-1);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
    }
    
    @Test
    public void testEquals() {
        PointSet a = new PointSet(new VectList(0, 0, 100, 1, 100, 100));
        PointSet b = new PointSet(new VectList(0, 0, 100, 0, 100, 100));
        assertEquals(a, a);
        assertNotEquals(a, b);
        assertNotEquals(a, "");
    }

    @Test
    public void testHashCode() {
        Set<Integer> hashCodes = new HashSet<>();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                PointSet ps = new PointSet(new VectList(0, 0, x, y, 10, 10));
                hashCodes.add(ps.hashCode());
            }
        }
        assertEquals(100, hashCodes.size());
    }
      
    @Test
    public void testGetArea(){
        PointSet a = new PointSet(new VectList(0, 0, 100, 1, 100, 100));
        assertEquals(0, a.getArea(Linearizer.DEFAULT, Tolerance.DEFAULT), 0);
    }
}
