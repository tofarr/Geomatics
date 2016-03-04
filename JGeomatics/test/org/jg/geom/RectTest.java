package org.jg.geom;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.jg.util.Network;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.TransformBuilder;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar_000
 */
public class RectTest {

    @Test
    public void testGetWidth() {
        Rect rect = Rect.valueOf(5, 5, 5, 5);
        assertEquals(0, rect.getWidth(), 0.00001);
        rect = Rect.valueOf(5, 5, 9, 5);
        assertEquals(4, rect.getWidth(), 0.00001);
        rect = Rect.valueOf(5, 5, 1, 5);
        assertEquals(4, rect.getWidth(), 0.00001);
    }

    @Test
    public void testGetHeight() {
        Rect rect = Rect.valueOf(5, 5, 5, 5);
        assertEquals(0, rect.getHeight(), 0.00001);
        rect = Rect.valueOf(5, 5, 5, 9);
        assertEquals(4, rect.getHeight(), 0.00001);
        rect = Rect.valueOf(5, 5, 5, 1);
        assertEquals(4, rect.getHeight(), 0.00001);
    }

    @Test
    public void testGetArea() {
        Rect rect = Rect.valueOf(1, 3, 7, 13);
        assertEquals(60, rect.getArea(), 0.00001);
        rect = Rect.valueOf(1, 13, 7, 3);
        assertEquals(60, rect.getArea(), 0.00001);
        rect = Rect.valueOf(7, 3, 1, 13);
        assertEquals(60, rect.getArea(), 0.00001);
        rect = Rect.valueOf(7, 13, 1, 3);
        assertEquals(60, rect.getArea(), 0.00001);
    }

    @Test
    public void testGetCx() {
        Rect rect = Rect.valueOf(1, 3, 7, 13);
        assertEquals(4, rect.getCx(), 0.00001);
        rect = Rect.valueOf(1, 13, 7, 3);
        assertEquals(4, rect.getCx(), 0.00001);
        rect = Rect.valueOf(7, 3, 1, 13);
        assertEquals(4, rect.getCx(), 0.00001);
        rect = Rect.valueOf(7, 13, 1, 3);
        assertEquals(4, rect.getCx(), 0.00001);
    }

    @Test
    public void testGetCy() {
        Rect rect = Rect.valueOf(1, 3, 7, 13);
        assertEquals(8, rect.getCy(), 0.00001);
        rect = Rect.valueOf(1, 13, 7, 3);
        assertEquals(8, rect.getCy(), 0.00001);
        rect = Rect.valueOf(7, 3, 1, 13);
        assertEquals(8, rect.getCy(), 0.00001);
        rect = Rect.valueOf(7, 13, 1, 3);
        assertEquals(8, rect.getCy(), 0.00001);
    }

    @Test
    public void testGetCentroid() {
        Rect rect = Rect.valueOf(2, -17, -11, 3);
        assertEquals(-4.5, rect.getCx(), 0.00001);
        assertEquals(-7, rect.getCy(), 0.00001);
        assertEquals(Vect.valueOf(-4.5, -7), rect.getCentroid());
    }

    @Test
    public void testIsDisjoint() {
        Rect a = Rect.valueOf(10, 10, 20, 20);
        RectBuilder b = new RectBuilder();
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                for (int x2 = x1; x2 < 30; x2 += 5) {
                    for (int y2 = y1; y2 < 30; y2 += 5) {
                        b.set(x1, y1, x2, y2);
                        Rect c = Rect.valueOf(x1, y1, x2, y2);
                        boolean disjoint = (c.minX > a.maxX) || (c.minY > a.maxY) || (c.maxX < a.minX) || (c.maxY < a.minY);
                        assertEquals(disjoint, a.isDisjoint(b));
                        assertEquals(disjoint, a.isDisjoint(c));
                        assertEquals(disjoint, c.isDisjoint(a));
                    }
                }
            }
        }
        assertFalse(Rect.valueOf(1, 2, 3, 4).isDisjoint(b.set(1, 4, 3, 2)));
        assertFalse(Rect.valueOf(3, 2, 1, 4).isDisjoint(b.set(1, 2, 3, 4)));
        assertFalse(Rect.valueOf(1, 2, 3, 4).isDisjoint(b.set(1, 4, 3, 2).build()));
        assertFalse(Rect.valueOf(3, 2, 1, 4).isDisjoint(b.set(1, 2, 3, 4).build()));
        try {
            a.isDisjoint((Rect) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            a.isDisjoint((RectBuilder) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertFalse(Rect.valueOf(3, 2, 1, 4).isDisjoint(new RectBuilder()));
    }

    @Test
    public void testIsOverlapping() {
        Rect a = Rect.valueOf(10, 10, 20, 20);
        RectBuilder b = new RectBuilder();
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                for (int x2 = x1; x2 < 30; x2 += 5) {
                    for (int y2 = y1; y2 < 30; y2 += 5) {
                        boolean overlapping = (x1 < a.maxX) && (y1 < a.maxY) && (x2 > a.minX) && (y2 > a.minY);
                        b.set(x1, y1, x2, y2);
                        Rect c = Rect.valueOf(x1, y1, x2, y2);
                        assertEquals(overlapping, a.isOverlapping(b));
                        assertEquals(overlapping, a.isOverlapping(c));
                        assertEquals(overlapping, c.isOverlapping(a));
                    }
                }
            }
        }
        assertTrue(Rect.valueOf(1, 2, 3, 4).isOverlapping(b.set(1, 4, 3, 2)));
        assertTrue(Rect.valueOf(3, 2, 1, 4).isOverlapping(b.set(1, 2, 3, 4)));
        try {
            a.isOverlapping((Rect) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            a.isOverlapping((RectBuilder) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertFalse(Rect.valueOf(1, 2, 3, 4).isOverlapping(new RectBuilder()));
    }

    @Test
    public void testContains_Rect() {
        Rect a = Rect.valueOf(10, 10, 20, 20);
        RectBuilder b = new RectBuilder();
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                for (int x2 = x1; x2 < 30; x2 += 5) {
                    for (int y2 = y1; y2 < 30; y2 += 5) {
                        boolean contains = (x1 >= a.minX) && (y1 >= a.minY) && (x2 <= a.maxX) && (y2 <= a.maxY);
                        b.set(x1, y1, x2, y2);
                        Rect c = Rect.valueOf(x1, y1, x2, y2);
                        assertEquals(contains, a.contains(b));
                        assertEquals(contains, a.contains(c));
                    }
                }
            }
        }
        assertTrue(Rect.valueOf(1, 2, 3, 4).contains(b.set(1, 4, 3, 2)));
        assertTrue(Rect.valueOf(3, 2, 1, 4).contains(b.set(1, 2, 3, 4)));
        try {
            a.contains((Rect) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertFalse(Rect.valueOf(1, 2, 3, 4).contains(new RectBuilder()));
    }

    @Test
    public void testIsContainedBy() {
        Rect a = Rect.valueOf(10, 10, 20, 20);
        RectBuilder b = new RectBuilder();
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                for (int x2 = x1; x2 < 30; x2 += 5) {
                    for (int y2 = y1; y2 < 30; y2 += 5) {
                        boolean containedBy = (x1 <= a.minX) && (y1 <= a.minY) && (x2 >= a.maxX) && (y2 >= a.maxY);
                        b.set(x1, y1, x2, y2);
                        Rect c = Rect.valueOf(x1, y1, x2, y2);
                        assertEquals(containedBy, a.isContainedBy(b));
                        assertEquals(containedBy, a.isContainedBy(c));
                    }
                }
            }
        }
        assertTrue(Rect.valueOf(1, 2, 3, 4).isContainedBy(b.set(1, 4, 3, 2)));
        assertTrue(Rect.valueOf(3, 2, 1, 4).isContainedBy(b.set(1, 2, 3, 4)));
        assertFalse(Rect.valueOf(1, 2, 3, 4).isContainedBy(new RectBuilder()));
        try {
            a.isContainedBy((Rect) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            a.isContainedBy((RectBuilder) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testContains_Vect() {
        Rect a = Rect.valueOf(10, 10, 20, 20);
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                boolean contains = (x1 >= a.minX) && (y1 >= a.minY) && (x1 <= a.maxX) && (y1 <= a.maxY);
                Vect b = Vect.valueOf(x1, y1);
                assertEquals(contains, a.contains(b));
            }
        }
        assertTrue(Rect.valueOf(3, 4, 1, 2).contains(Vect.valueOf(1, 2)));
        try {
            a.contains((Vect) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testRelate() {
        Rect a = Rect.valueOf(10, 10, 20, 20);
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                Vect b = Vect.valueOf(x1, y1);
                Relate relate = a.relate(b);
                if ((x1 > a.minX) && (y1 > a.minY) && (x1 < a.maxX) && (y1 < a.maxY)) {
                    assertEquals(Relate.INSIDE, relate);
                } else if ((((x1 == a.minX) || (x1 == a.maxX)) && (y1 >= a.minY) && (y1 <= a.maxY))
                        || (((y1 == a.minY) || (y1 == a.maxY)) && (x1 >= a.minX) && (x1 <= a.maxX))) {
                    assertEquals(Relate.TOUCH, relate);
                } else {
                    assertEquals(Relate.OUTSIDE, relate);
                }
            }
        }
        assertEquals(Relate.INSIDE, Rect.valueOf(20, 20, 10, 10).relate(new VectBuilder(15, 15)));
        try {
            a.relate((Vect) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testIntersection() {
        Rect a = Rect.valueOf(10, 10, 20, 20);
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                for (int x2 = x1; x2 < 30; x2 += 5) {
                    for (int y2 = y1; y2 < 30; y2 += 5) {
                        Rect b = Rect.valueOf(x1, y1, x2, y2);
                        boolean disjoint = (x1 > a.maxX) || (y1 > a.maxY) || (x2 < a.minX) || (y2 < a.minY);
                        if (disjoint) {
                            assertNull(a.intersection(b));
                        } else {
                            Rect c = Rect.valueOf(Math.max(x1, a.minX),
                                    Math.max(y1, a.minY),
                                    Math.min(x2, a.maxX),
                                    Math.min(y2, a.maxY));
                            assertEquals(c, a.intersection(b));
                        }
                    }
                }
            }
        }
        assertEquals(Rect.valueOf(10, 10, 20, 20), Rect.valueOf(20, 20, 10, 10).intersection(Rect.valueOf(10, 10, 20, 20)));
        try {
            a.intersection(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testUnion_Rect_Rect() {
        Rect a = Rect.valueOf(10, 10, 20, 20);
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                for (int x2 = x1; x2 < 30; x2 += 5) {
                    for (int y2 = y1; y2 < 30; y2 += 5) {
                        Rect b = Rect.valueOf(x1, y1, x2, y2);
                        Rect c = a.union(b);
                        assertEquals(Math.min(x1, a.minX), c.getMinX(), 0.00001);
                        assertEquals(Math.min(y1, a.minY), c.getMinY(), 0.00001);
                        assertEquals(Math.max(x2, a.maxX), c.getMaxX(), 0.00001);
                        assertEquals(Math.max(y2, a.maxY), c.getMaxY(), 0.00001);
                    }
                }
            }
        }
        try {
            a.union(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testUnion_Rect() {
        Rect a = Rect.valueOf(10, 10, 20, 20);
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                for (int x2 = x1; x2 < 30; x2 += 5) {
                    for (int y2 = y1; y2 < 30; y2 += 5) {
                        Rect b = Rect.valueOf(x1, y1, x2, y2);
                        Rect c = a.union(b);
                        assertEquals(Math.min(x1, 10), c.getMinX(), 0.00001);
                        assertEquals(Math.min(y1, 10), c.getMinY(), 0.00001);
                        assertEquals(Math.max(x2, 20), c.getMaxX(), 0.00001);
                        assertEquals(Math.max(y2, 20), c.getMaxY(), 0.00001);
                    }
                }
            }
        }
        assertSame(a, a.union(a));
        try {
            a.union((Rect) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testBuffer() {
        Rect rect = Rect.valueOf(11, 17, 29, 43);
        assertEquals(Rect.valueOf(10, 16, 30, 44), rect.buffer(1));
        assertEquals(Rect.valueOf(13, 19, 27, 41), rect.buffer(-2));
        assertEquals(Rect.valueOf(11, 17, 29, 43), rect.buffer(0));
        try {
            rect.buffer(Double.NaN);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //Expected
        }
        try {
            rect.buffer(Double.POSITIVE_INFINITY);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //Expected
        }
        assertNull(rect.buffer(-10));
        assertNull(Rect.valueOf(0, 0, 2, 10).buffer(-2));
        assertNull(Rect.valueOf(0, 0, 10, 2).buffer(-2));
    }
    
    @Test
    public void testBuffer_Tolerance(){
        Rect rect = Rect.valueOf(3, 7, 13, 23);
        assertSame(rect, rect.buffer(0, Tolerance.DEFAULT));
        assertEquals(Rect.valueOf(4, 8, 12, 22), rect.buffer(-1, Tolerance.DEFAULT));
        RingSet ringSet = (RingSet)rect.buffer(2, new Tolerance(0.5));
        
        assertEquals(Rect.valueOf(1, 5, 15, 25), ringSet.getBounds());
        assertEquals(264 + (Math.PI * 4), ringSet.getArea(), 0.5);
        assertEquals(52 + (Math.PI * 4), ringSet.ring.getLength(), 0.5);
    }

    @Test
    public void testHashCode() {
        assertEquals(Rect.valueOf(2, 3, 11, 17).hashCode(), Rect.valueOf(2, 3, 11, 17).hashCode()); // equal should have same hashcode
        Set<Integer> hashes = new HashSet<>();
        for (int i = 2; i < 52; i++) { // minor test - no collisions in 200 elements
            int a = Rect.valueOf(1, 1, 1, i).hashCode();
            int c = Rect.valueOf(1, -i, 1, 1).hashCode();
            int b = Rect.valueOf(1, 1, i, 1).hashCode();
            int d = Rect.valueOf(-i, 1, 1, 1).hashCode();
            assertFalse(hashes.contains(a));
            hashes.add(a);
            assertFalse(hashes.contains(b));
            hashes.add(b);
            assertFalse(hashes.contains(c));
            hashes.add(c);
            assertFalse(hashes.contains(d));
            hashes.add(d);
        }
    }

    @Test
    public void testEquals() {
        assertEquals(Rect.valueOf(1, 2, 3, 4), Rect.valueOf(1, 2, 3, 4));
        assertFalse(Rect.valueOf(1, 2, 3, 4).equals(Rect.valueOf(-1, 2, 3, 4)));
        assertFalse(Rect.valueOf(1, 2, 3, 4).equals(Rect.valueOf(1, -2, 3, 4)));
        assertFalse(Rect.valueOf(1, 2, 3, 4).equals(Rect.valueOf(1, 2, 5, 4)));
        assertFalse(Rect.valueOf(1, 2, 3, 4).equals(Rect.valueOf(1, 2, 3, 5)));
        assertFalse(Rect.valueOf(1, 2, 3, 4).equals(null));
        assertEquals(Rect.valueOf(1, 2, 3, 4), Rect.valueOf(1, 2, 3, 4));
        assertFalse(Rect.valueOf(1, 2, 3, 4).equals(Rect.valueOf(-1, 2, 3, 4)));
        assertFalse(Rect.valueOf(1, 2, 3, 4).equals(Rect.valueOf(1, -2, 3, 4)));
        assertFalse(Rect.valueOf(1, 2, 3, 4).equals(Rect.valueOf(1, 2, 5, 4)));
        assertFalse(Rect.valueOf(1, 2, 3, 4).equals(Rect.valueOf(1, 2, 3, 5)));
    }

    @Test
    public void testToString_0args() {
        assertEquals("[1,2,3,4]", Rect.valueOf(1, 2, 3, 4).toString());
        assertEquals("[1.5,2.5,3.5,4.5]", Rect.valueOf(1.5, 2.5, 3.5, 4.5).toString());
    }

    @Test
    public void testToString_Appendable() throws Exception {
        StringBuilder str = new StringBuilder();
        Rect.valueOf(1, 2, 3, 4).toString(str);
        assertEquals("[1,2,3,4]", str.toString());
        str.setLength(0);
        Rect.valueOf(1.5, 2.5, 3.5, 4.5).toString(str);
        assertEquals("[1.5,2.5,3.5,4.5]", str.toString());
        try {
            Rect.valueOf(1, 2, 3, 4).toString(new Appendable() {
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
        } catch (GeomException ex) {

        }
    }

    @Test
    public void testClone() throws Exception {
        Rect a = Rect.valueOf(1, 2, 3, 4);
        Rect b = a.clone();
        assertSame(a, b);
    }

    @Test
    public void testExternalize() throws Exception {
        Rect a = Rect.valueOf(3, 7, 13, 29);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bout)) {
            a.write(out);
        }
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            Rect b = Rect.read(in);
            assertEquals(a, b);
        }
    }

    @Test
    public void testValueOf() {
        Rect a = Rect.valueOf(3, 7, 13, 29);
        assertEquals(3, a.getMinX(), 0.00001);
        assertEquals(7, a.getMinY(), 0.00001);
        assertEquals(13, a.getMaxX(), 0.00001);
        assertEquals(29, a.getMaxY(), 0.00001);
        try {
            Rect.valueOf(Double.NaN, 7, 13, 29);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //Expected
        }
        try {
            Rect.valueOf(3, Double.POSITIVE_INFINITY, 13, 29);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //Expected
        }
        try {
            Rect.valueOf(3, 7, Double.NEGATIVE_INFINITY, 29);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //Expected
        }
        try {
            Rect.valueOf(3, 7, 13, Double.NaN);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //Expected
        }
    }

    @Test
    public void testGetBounds() {
        Rect a = Rect.valueOf(3, 7, 13, 29);
        Rect b = a.getBounds();
        assertSame(a, b);
    }

    @Test
    public void testAddBoundsTo() {
        Rect a = Rect.valueOf(3, 7, 13, 29);
        RectBuilder b = new RectBuilder(4, 6, 12, 30);
        a.addBoundsTo(b);
        assertEquals(new RectBuilder(3, 6, 13, 30), b);
    }

    @Test
    public void testTransform() {
        Transform transform = new TransformBuilder().scaleAround(2, 4, 9).build();
        Rect a = Rect.valueOf(3, 7, 13, 29);
        Rect b = a.transform(transform);
        assertEquals("[2,5,22,49]", b.toString());
    }

    @Test
    public void testPathIterator() {
        Rect a = Rect.valueOf(3, 7, 13, 29);
        Path2D.Double path = new Path2D.Double();
        path.append(a.pathIterator(), true);
        Rectangle2D bounds = path.getBounds2D();
        assertEquals(3, bounds.getMinX(), 0.0001);
        assertEquals(7, bounds.getMinY(), 0.0001);
        assertEquals(13, bounds.getMaxX(), 0.0001);
        assertEquals(29, bounds.getMaxY(), 0.0001);
    }

    @Test
    public void testAddTo() {
        Network network = new Network();
        Rect a = Rect.valueOf(3, 7, 13, 29);
        a.addTo(network, Tolerance.DEFAULT);
        assertEquals("[[3,7, 13,7, 13,29, 3,29, 3,7]]", network.toString());
    }
    
    @Test
    public void testRelate_Tolerance(){
        Rect a = Rect.valueOf(10, 10, 20, 20);
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                Vect b = Vect.valueOf(x1, y1);
                Relate relate = a.relate(b, Tolerance.DEFAULT);
                if ((x1 > a.minX) && (y1 > a.minY) && (x1 < a.maxX) && (y1 < a.maxY)) {
                    assertEquals(Relate.INSIDE, relate);
                } else if ((((x1 == a.minX) || (x1 == a.maxX)) && (y1 >= a.minY) && (y1 <= a.maxY))
                        || (((y1 == a.minY) || (y1 == a.maxY)) && (x1 >= a.minX) && (x1 <= a.maxX))) {
                    assertEquals(Relate.TOUCH, relate);
                } else {
                    assertEquals(Relate.OUTSIDE, relate);
                }
            }
        }
        assertEquals(Relate.INSIDE, Rect.valueOf(20, 20, 10, 10).relate(new VectBuilder(15, 15), Tolerance.DEFAULT));
        try {
            a.relate((Vect) null, Tolerance.DEFAULT);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            a.relate((VectBuilder) null, Tolerance.DEFAULT);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            a.relate((Vect) null, Tolerance.DEFAULT);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }
}
