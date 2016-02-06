package org.jg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar_000
 */
public class RectTest {

    @Test
    public void testReset() {
        Rect a = new Rect(2, 4, 8, 16);
        a.reset();
        assertEquals(Double.MAX_VALUE, a.getMinX(), 0.00001);
        assertEquals(Double.MAX_VALUE, a.getMinY(), 0.00001);
        assertEquals(-Double.MAX_VALUE, a.getMaxX(), 0.00001);
        assertEquals(-Double.MAX_VALUE, a.getMaxY(), 0.00001);
        a.union(2, 4);
        assertEquals(2, a.getMinX(), 0.00001);
        assertEquals(4, a.getMinY(), 0.00001);
        assertEquals(2, a.getMaxX(), 0.00001);
        assertEquals(4, a.getMaxY(), 0.00001);

    }

    @Test
    public void testSet_4args() {
        Rect a = new Rect().set(2, 4, 8, 16);
        assertTrue(a.isValid());
        assertEquals(2, a.getMinX(), 0.00001);
        assertEquals(4, a.getMinY(), 0.00001);
        assertEquals(8, a.getMaxX(), 0.00001);
        assertEquals(16, a.getMaxY(), 0.00001);
        a.set(3, 4, 1, 2);
        assertFalse(a.isValid());
        try {
            a.set(Double.NaN, 3, 2, 1);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            a.set(4, Double.POSITIVE_INFINITY, 2, 1);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            a.set(4, 3, Double.NEGATIVE_INFINITY, 1);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            a.set(4, 3, 2, Double.NaN);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        assertEquals(3, a.getMinX(), 0.00001);
        assertEquals(4, a.getMinY(), 0.00001);
        assertEquals(1, a.getMaxX(), 0.00001);
        assertEquals(2, a.getMaxY(), 0.00001);
    }

    @Test
    public void testSet_Rect() {
        Rect a = new Rect().set(new Rect(2, 4, 8, 16));
        assertTrue(a.isValid());
        assertEquals(2, a.getMinX(), 0.00001);
        assertEquals(4, a.getMinY(), 0.00001);
        assertEquals(8, a.getMaxX(), 0.00001);
        assertEquals(16, a.getMaxY(), 0.00001);
        a.set(new Rect(3, 4, 1, 2));
        assertTrue(!a.isValid());
        try {
            a.set(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals(3, a.getMinX(), 0.00001);
        assertEquals(4, a.getMinY(), 0.00001);
        assertEquals(1, a.getMaxX(), 0.00001);
        assertEquals(2, a.getMaxY(), 0.00001);
    }

    @Test
    public void testIsValid() {
        Rect rect = new Rect(5, 5, 5, 5);
        assertTrue(rect.isValid());
        rect.set(5, 5, 4, 5);
        assertFalse(rect.isValid());
        rect.set(5, 5, 5, 4);
        assertFalse(rect.isValid());
    }

    @Test
    public void testGetWidth() {
        Rect rect = new Rect(5, 5, 5, 5);
        assertEquals(0, rect.getWidth(), 0.00001);
        rect.set(5, 5, 9, 5);
        assertEquals(4, rect.getWidth(), 0.00001);
        rect.set(5, 5, 1, 5);
        assertEquals(-4, rect.getWidth(), 0.00001);
    }

    @Test
    public void testGetHeight() {
        Rect rect = new Rect(5, 5, 5, 5);
        assertEquals(0, rect.getHeight(), 0.00001);
        rect.set(5, 5, 5, 9);
        assertEquals(4, rect.getHeight(), 0.00001);
        rect.set(5, 5, 5, 1);
        assertEquals(-4, rect.getHeight(), 0.00001);
    }

    @Test
    public void testGetArea() {
        Rect rect = new Rect(1, 3, 7, 13);
        assertEquals(60, rect.getArea(), 0.00001);
        rect.set(1, 13, 7, 3);
        assertEquals(-60, rect.getArea(), 0.00001);
        rect.set(7, 3, 1, 13);
        assertEquals(-60, rect.getArea(), 0.00001);
        rect.set(7, 13, 1, 3);
        assertEquals(60, rect.getArea(), 0.00001);
    }

    @Test
    public void testGetCx() {
        Rect rect = new Rect(1, 3, 7, 13);
        assertEquals(4, rect.getCx(), 0.00001);
        rect.set(1, 13, 7, 3);
        assertEquals(4, rect.getCx(), 0.00001);
        rect.set(7, 3, 1, 13);
        assertEquals(4, rect.getCx(), 0.00001);
        rect.set(7, 13, 1, 3);
        assertEquals(4, rect.getCx(), 0.00001);
    }

    @Test
    public void testGetCy() {
        Rect rect = new Rect(1, 3, 7, 13);
        assertEquals(8, rect.getCy(), 0.00001);
        rect.set(1, 13, 7, 3);
        assertEquals(8, rect.getCy(), 0.00001);
        rect.set(7, 3, 1, 13);
        assertEquals(8, rect.getCy(), 0.00001);
        rect.set(7, 13, 1, 3);
        assertEquals(8, rect.getCy(), 0.00001);
    }

    @Test
    public void testGetCentroid() {
        Rect rect = new Rect(2, -17, -11, 3);
        assertEquals(-4.5, rect.getCx(), 0.00001);
        assertEquals(-7, rect.getCy(), 0.00001);
        assertEquals(new Vect(-4.5, -7), rect.getCentroid(new Vect()));
    }

    @Test
    public void testIsDisjoint() {
        Rect a = new Rect(10, 10, 20, 20);
        Rect b = new Rect();
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                for (int x2 = x1; x2 < 30; x2 += 5) {
                    for (int y2 = y1; y2 < 30; y2 += 5) {
                        boolean disjoint = (x1 > a.maxX) || (y1 > a.maxY) || (x2 < a.minX) || (y2 < a.minY);
                        b.set(x1, y1, x2, y2).normalize();
                        assertEquals(disjoint, a.isDisjoint(b));
                        assertEquals(disjoint, b.isDisjoint(a));
                    }
                }
            }
        }
        assertTrue(a.set(1, 2, 3, 4).isDisjoint(b.set(1, 4, 3, 2)));
        assertTrue(a.set(3, 2, 1, 4).isDisjoint(b.set(1, 2, 3, 4)));
        try {
            a.isDisjoint(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testIsOverlapping() {
        Rect a = new Rect(10, 10, 20, 20);
        Rect b = new Rect();
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                for (int x2 = x1; x2 < 30; x2 += 5) {
                    for (int y2 = y1; y2 < 30; y2 += 5) {
                        boolean overlapping = (x1 < a.maxX) && (y1 < a.maxY) && (x2 > a.minX) && (y2 > a.minY);
                        b.set(x1, y1, x2, y2).normalize();
                        assertEquals(overlapping, a.isOverlapping(b));
                        assertEquals(overlapping, b.isOverlapping(a));
                    }
                }
            }
        }
        assertFalse(a.set(1, 2, 3, 4).isOverlapping(b.set(1, 4, 3, 2)));
        assertFalse(a.set(3, 2, 1, 4).isOverlapping(b.set(1, 2, 3, 4)));
        try {
            a.isOverlapping(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testContains_Rect() {
        Rect a = new Rect(10, 10, 20, 20);
        Rect b = new Rect();
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                for (int x2 = x1; x2 < 30; x2 += 5) {
                    for (int y2 = y1; y2 < 30; y2 += 5) {
                        boolean contains = (x1 >= a.minX) && (y1 >= a.minY) && (x2 <= a.maxX) && (y2 <= a.maxY);
                        b.set(x1, y1, x2, y2).normalize();
                        assertEquals(contains, a.contains(b));
                    }
                }
            }
        }
        assertFalse(a.set(1, 2, 3, 4).contains(b.set(1, 4, 3, 2)));
        assertFalse(a.set(3, 2, 1, 4).contains(b.set(1, 2, 3, 4)));
        try {
            a.contains((Rect) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testContains_Vect() {
        Rect a = new Rect(10, 10, 20, 20);
        Vect b = new Vect();
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                boolean contains = (x1 >= a.minX) && (y1 >= a.minY) && (x1 <= a.maxX) && (y1 <= a.maxY);
                b.set(x1, y1);
                assertEquals(contains, a.contains(b));
            }
        }
        assertFalse(a.set(3, 4, 1, 2).contains(b.set(1, 2)));
        try {
            a.contains((Vect) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testRelate() {
        Rect a = new Rect(10, 10, 20, 20);
        Vect b = new Vect();
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                b.set(x1, y1);
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
        assertEquals(Relate.OUTSIDE, a.set(20, 20, 10, 10).relate(b.set(15, 15)));
        try {
            a.relate((Vect) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testIntersection() {
        Rect a = new Rect(10, 10, 20, 20);
        Rect b = new Rect();
        Rect c = new Rect();
        Rect d = new Rect();
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                for (int x2 = x1; x2 < 30; x2 += 5) {
                    for (int y2 = y1; y2 < 30; y2 += 5) {
                        b.set(x1, y1, x2, y2);
                        boolean disjoint = (x1 > a.maxX) || (y1 > a.maxY) || (x2 < a.minX) || (y2 < a.minY);
                        if (disjoint) {
                            assertFalse(a.intersection(b, c).isValid());
                        } else {
                            d.set(Math.max(x1, a.minX),
                                    Math.max(y1, a.minY),
                                    Math.min(x2, a.maxX),
                                    Math.min(y2, a.maxY));
                            assertEquals(d, a.intersection(b, c));
                        }
                    }
                }
            }
        }
        assertEquals(d.reset(), a.set(20, 20, 10, 10).intersection(b.set(10, 10, 20, 20), c));
        try {
            a.intersection(null, c);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            a.intersection(b, null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testUnion_Rect_Rect() {
        Rect a = new Rect(10, 10, 20, 20);
        Rect b = new Rect();
        Rect c = new Rect();
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                for (int x2 = x1; x2 < 30; x2 += 5) {
                    for (int y2 = y1; y2 < 30; y2 += 5) {
                        b.set(x1, y1, x2, y2);
                        a.union(b, c);
                        assertEquals(Math.min(x1, a.minX), c.getMinX(), 0.00001);
                        assertEquals(Math.min(y1, a.minY), c.getMinY(), 0.00001);
                        assertEquals(Math.max(x2, a.maxX), c.getMaxX(), 0.00001);
                        assertEquals(Math.max(y2, a.maxY), c.getMaxY(), 0.00001);
                    }
                }
            }
        }
        b.reset();
        a.union(b, c);
        assertEquals(a, c);
        assertFalse(b.isValid());
        b.union(a, c);
        assertEquals(a, c);
        assertFalse(b.isValid());
        a.reset();
        a.union(b, c);
        assertFalse(a.isValid());
        assertFalse(b.isValid());
        assertFalse(c.isValid());

        try {
            a.union(b, null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            a.union(null, c);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testUnion_Rect() {
        Rect a = new Rect(10, 10, 20, 20);
        Rect b = new Rect();
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                for (int x2 = x1; x2 < 30; x2 += 5) {
                    for (int y2 = y1; y2 < 30; y2 += 5) {
                        a.set(10, 10, 20, 20);
                        b.set(x1, y1, x2, y2);
                        assertSame(a, a.union(b));
                        assertEquals(Math.min(x1, 10), a.getMinX(), 0.00001);
                        assertEquals(Math.min(y1, 10), a.getMinY(), 0.00001);
                        assertEquals(Math.max(x2, 20), a.getMaxX(), 0.00001);
                        assertEquals(Math.max(y2, 20), a.getMaxY(), 0.00001);
                    }
                }
            }
        }
        b.reset();
        assertSame(a.set(10, 20, 30, 40), a.union(b));
        assertEquals(a, b.set(10, 20, 30, 40));
        try {
            a.union((Rect) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testUnion_Vect() {
        Rect a = new Rect(10, 10, 20, 20);
        Vect b = new Vect();
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                a.set(10, 10, 20, 20);
                b.set(x1, y1);
                assertSame(a, a.union(b));
                assertEquals(Math.min(x1, 10), a.getMinX(), 0.00001);
                assertEquals(Math.min(y1, 10), a.getMinY(), 0.00001);
                assertEquals(Math.max(x1, 20), a.getMaxX(), 0.00001);
                assertEquals(Math.max(y1, 20), a.getMaxY(), 0.00001);
            }
        }
        b.set(15, 15);
        assertSame(a.set(10, 20, 30, 40), a.union(b));
        try {
            a.union((Vect) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testUnion_double_double() {
        Rect a = new Rect(10, 10, 20, 20);
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                a.set(10, 10, 20, 20);
                assertSame(a, a.union(x1, y1));
                assertEquals(Math.min(x1, 10), a.getMinX(), 0.00001);
                assertEquals(Math.min(y1, 10), a.getMinY(), 0.00001);
                assertEquals(Math.max(x1, 20), a.getMaxX(), 0.00001);
                assertEquals(Math.max(y1, 20), a.getMaxY(), 0.00001);
            }
        }
        assertSame(a.set(10, 20, 30, 40), a.union(15, 25));
        try {
            a.union((Vect) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals(new Rect(14, 25, 15, 26), a.reset().union(15, 25).union(14, 26));
    }

    @Test
    public void testUnionAll() {
        Rect rect = new Rect(3, 11, 23, 37);
        double[] ords = new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 101, 102, 103};
        assertSame(rect, rect.unionAll(ords, 5, 1));
        try {
            rect.unionAll(null, 0, 13);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            rect.unionAll(ords, -1, 5);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            rect.unionAll(ords, 1, 14);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            rect.unionAll(ords, 1, 4);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        try {
            rect.unionAll(new double[]{Double.NaN, 0, 0, 0}, 0, 4);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        try {
            rect.unionAll(new double[]{Double.NEGATIVE_INFINITY, 0, 0, 0}, 0, 4);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        assertEquals(new Rect(1, 2, 23, 37), rect);
        rect = new Rect(23, 37, 1, 2);
        assertSame(rect, rect.unionAll(ords, 5, 1));
        assertEquals(new Rect(1, 2, 3, 4), rect);
    }

    @Test
    public void testBuffer() {
        Rect rect = new Rect(11, 17, 29, 43);
        assertEquals(new Rect(10, 16, 30, 44), rect.buffer(1));
        assertEquals(new Rect(12, 18, 28, 42), rect.buffer(-2));
        assertEquals(new Rect(12, 18, 28, 42), rect.buffer(0));
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
        rect.buffer(-10);
        assertFalse(rect.isValid());
        try {
            rect.buffer(1);
            fail("Exception expected");
        } catch (IllegalStateException ex) {
            //Expected
        }
        assertFalse(rect.set(0, 0, 2, 10).buffer(-2).isValid());
        assertFalse(rect.set(0, 0, 10, 2).buffer(-2).isValid());
    }

    @Test
    public void testHashCode() {
        assertEquals(new Rect(2, 3, 11, 17).hashCode(), new Rect(2, 3, 11, 17).hashCode()); // equal should have same hashcode
        Set<Integer> hashes = new HashSet<>();
        for (int i = 2; i < 52; i++) { // minor test - no collisions in 200 elements
            int a = new Rect(1, 1, 1, i).hashCode();
            int c = new Rect(1, -i, 1, 1).hashCode();
            int b = new Rect(1, 1, i, 1).hashCode();
            int d = new Rect(-i, 1, 1, 1).hashCode();
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
        assertEquals(new Rect(1, 2, 3, 4), new Rect(1, 2, 3, 4));
        assertFalse(new Rect(1, 2, 3, 4).equals(new Rect(-1, 2, 3, 4)));
        assertFalse(new Rect(1, 2, 3, 4).equals(new Rect(1, -2, 3, 4)));
        assertFalse(new Rect(1, 2, 3, 4).equals(new Rect(1, 2, 5, 4)));
        assertFalse(new Rect(1, 2, 3, 4).equals(new Rect(1, 2, 3, 5)));
        assertFalse(new Rect(1, 2, 3, 4).equals(null));
        assertEquals(new Rect(1, 2, 3, 4), new Rect(1, 2, 3, 4));
        assertFalse(new Rect(1, 2, 3, 4).equals(new Rect(-1, 2, 3, 4)));
        assertFalse(new Rect(1, 2, 3, 4).equals(new Rect(1, -2, 3, 4)));
        assertFalse(new Rect(1, 2, 3, 4).equals(new Rect(1, 2, 5, 4)));
        assertFalse(new Rect(1, 2, 3, 4).equals(new Rect(1, 2, 3, 5)));
    }

    @Test
    public void testToString_0args() {
        assertEquals("[1,2,3,4]", new Rect(1, 2, 3, 4).toString());
        assertEquals("[1.5,2.5,3.5,4.5]", new Rect(1.5, 2.5, 3.5, 4.5).toString());
    }

    @Test
    public void testToString_Appendable() throws Exception {
        StringBuilder str = new StringBuilder();
        new Rect(1, 2, 3, 4).toString(str);
        assertEquals("[1,2,3,4]", str.toString());
        str.setLength(0);
        new Rect(1.5, 2.5, 3.5, 4.5).toString(str);
        assertEquals("[1.5,2.5,3.5,4.5]", str.toString());
    }

    @Test
    public void testClone() throws Exception {
        Rect a = new Rect(1, 2, 3, 4);
        Rect b = a.clone();
        assertEquals(a, b);
        a.reset();
        assertFalse(a.equals(b));
    }

    @Test
    public void testExternalize() throws Exception {
        Rect a = new Rect(3, 7, 13, 23);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
            out.writeObject(a);
        }
        Rect b;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = (Rect) in.readObject();
        }
        assertEquals(a, b);
        bout = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
            a.writeData(out);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = Rect.read(in);
        }
        assertEquals(a, b);
    }

    @Test
    public void testNormalize(){
        Rect rect = new Rect(3, 7, 13, 23);
        assertSame(rect, rect.normalize());
        assertEquals(new Rect(3, 7, 13, 23), rect);
        
        assertSame(rect, rect.set(13, 7, 3, 23).normalize());
        assertEquals(new Rect(3, 7, 13, 23), rect);
        
        assertSame(rect, rect.set(3, 23, 13, 7).normalize());
        assertEquals(new Rect(3, 7, 13, 23), rect);
    }
    
    @Test
    public void testConstructor(){
        Rect a = new Rect(3, 7, 13, 23);
        Rect b = new Rect(a);
        assertEquals(a, b);
    }
}
