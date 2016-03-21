package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.IOException;
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
        try {
            ps = PointSet.valueOf(network, null);
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
        PathIterator iter = ps.pathIterator();
        assertEquals(PathIterator.WIND_NON_ZERO, iter.getWindingRule());

        assertFalse(iter.isDone());

        double[] coords = new double[6];
        assertEquals(PathIterator.SEG_MOVETO, iter.currentSegment(coords));
        assertEquals(1, coords[0], 0.0001);
        assertEquals(3, coords[1], 0.0001);

        float[] fcoords = new float[6];
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
        assertEquals("[\"PS\", 1,3, 7,13, 23,29]", ps.toString());
        try {
            ps.toString(new Appendable() {
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
        } catch (GeomException ex) {
        }
    }

    @Test
    public void testToGeoShape() {
        PointSet ps = PointSet.valueOf(new VectSet().addAll(new VectList(1, 3, 7, 13, 23, 29)));
        GeoShape gs = ps.toGeoShape(Tolerance.FLATNESS, Tolerance.DEFAULT);
        assertEquals(new GeoShape(null, null, ps), gs);
    }

    @Test
    public void testAddTo() {
        VectList vects = new VectList(1, 3, 7, 13, 23, 29);
        PointSet ps = PointSet.valueOf(new VectSet().addAll(vects));
        Network network = new Network();
        network.addLink(1, 3, 2, 3);
        ps.addTo(network, Tolerance.FLATNESS, Tolerance.DEFAULT);
        assertEquals("GEOMETRYCOLLECTION(POINT(7 13),POINT(23 29),LINESTRING(1 3, 2 3))", network.toWkt());
    }

    @Test
    public void testBuffer() {
        VectList vects = new VectList(5, 5, 5, 7, 7, 5, 14, 5, 22, 5, 31, 5);
        PointSet ps = PointSet.valueOf(new VectSet().addAll(vects));
        assertNull(ps.buffer(-1, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertSame(ps, ps.buffer(0, Tolerance.FLATNESS, Tolerance.DEFAULT));
        Area buffered = (Area) ps.buffer(4, Tolerance.FLATNESS, Tolerance.DEFAULT);
        Network network = new Network();
        buffered.addTo(network, Tolerance.FLATNESS, Tolerance.DEFAULT);
        Rect bounds = buffered.getBounds();
        assertEquals(1, bounds.minX, 0.00001);
        assertEquals(1, bounds.minY, 0.00001);
        assertEquals(35, bounds.maxX, 0.00001);
        assertEquals(11, bounds.maxY, 0.00001);
        assertEquals(3, buffered.numRings());
        assertEquals(276, buffered.getArea(), 1);
    }

    @Test
    public void testRelate() {
        VectList vects = new VectList(5, 5, 5, 7, 7, 5, 14, 5, 22, 5, 31, 5);
        PointSet ps = PointSet.valueOf(new VectSet().addAll(vects));
        assertEquals(ps.relate(Vect.ZERO, Tolerance.DEFAULT), Relate.OUTSIDE);
        assertEquals(ps.relate(Vect.valueOf(5, 5), Tolerance.DEFAULT), Relate.TOUCH);
        assertEquals(ps.relate(Vect.valueOf(14.1, 5.1), Tolerance.DEFAULT), Relate.OUTSIDE);
        assertEquals(ps.relate(Vect.valueOf(14.1, 5.1), new Tolerance(0.2)), Relate.TOUCH);
        assertEquals(ps.relate(new VectBuilder(), Tolerance.DEFAULT), Relate.OUTSIDE);
        assertEquals(ps.relate(new VectBuilder(5, 5), Tolerance.DEFAULT), Relate.TOUCH);
        assertEquals(ps.relate(new VectBuilder(14.1, 5.1), Tolerance.DEFAULT), Relate.OUTSIDE);
        assertEquals(ps.relate(new VectBuilder(14.1, 5.1), new Tolerance(0.2)), Relate.TOUCH);
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
    public void testUnion_Vect() {
        VectList vects = new VectList(5, 5, 5, 7, 7, 5, 14, 5, 22, 5, 31, 5);
        PointSet ps = PointSet.valueOf(new VectSet().addAll(vects));

        Vect touch = Vect.valueOf(14, 5);
        assertSame(ps, ps.union(touch, Tolerance.FLATNESS, Tolerance.DEFAULT));

        Vect outside = Vect.valueOf(14, 6);
        vects.add(14, 6);
        assertEquals(PointSet.valueOf(new VectSet().addAll(vects)), ps.union(outside, Tolerance.FLATNESS, Tolerance.DEFAULT));

        Vect outsideBounds = Vect.valueOf(14, 8);
        vects.set(vects.size() - 1, outsideBounds);
        assertEquals(PointSet.valueOf(new VectSet().addAll(vects)), ps.union(outsideBounds, Tolerance.FLATNESS, Tolerance.DEFAULT));
    }

    @Test
    public void testUnion_PointSet() {

        PointSet a = PointSet.valueOf(new VectSet().addAll(new VectList(2, 3, 5, 7, 11, 13)));
        PointSet b = PointSet.valueOf(new VectSet().addAll(new VectList(11, 13, 17, 19, 23, 29)));
        PointSet c = PointSet.valueOf(new VectSet().addAll(new VectList(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)));
        PointSet d = PointSet.valueOf(new VectSet().addAll(new VectList(17, 19, 23, 29)));

        assertSame(c, a.union(c, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertSame(c, c.union(a, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(c, a.union(b, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(c, a.union(d, Tolerance.FLATNESS, Tolerance.DEFAULT));
    }

    @Test
    public void testUnion() {

        PointSet a = PointSet.valueOf(new VectSet().addAll(new VectList(2, 3, 5, 7, 11, 13)));
        GeoShape b = Rect.valueOf(4, 7, 12, 14).toArea().toGeoShape();
        GeoShape c = new GeoShape(b.area, null, PointSet.valueOf(new VectSet().add(2, 3)));
        Ring d = Rect.valueOf(1, 2, 11, 14).toRing();

        assertEquals(c, a.union(b, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(c, a.union(c, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(d, a.union(d, Tolerance.FLATNESS, Tolerance.DEFAULT));

        try {
            a.union(null, Tolerance.FLATNESS, Tolerance.DEFAULT);
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
        
        assertEquals(a, a.intersection(a, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(c, a.intersection(b, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(c, a.intersection(c, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(d, a.intersection(d, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(a, a.intersection(e, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertNull(a.intersection(f, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertNull(a.intersection(g, Tolerance.FLATNESS, Tolerance.DEFAULT));
        
        try {
            a.intersection(null, Tolerance.FLATNESS, Tolerance.DEFAULT);
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
        
        
        assertNull(a.less(a, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(d, a.less(b, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(d, a.less(c, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(c, a.less(d, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertNull(a.less(e, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(a, a.less(f, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(a, a.less(g, Tolerance.FLATNESS, Tolerance.DEFAULT));
        
        try {
            a.less(null, Tolerance.FLATNESS, Tolerance.DEFAULT);
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
}
