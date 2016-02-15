
package org.jg.geom;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar_000
 */
public class RectBuilderTest {
    
    @Test
    public void testReset() {
        RectBuilder a = new RectBuilder(2, 4, 8, 16);
        a.reset();
        assertEquals(Double.NaN, a.getMinX(), 0.00001);
        assertEquals(Double.NaN, a.getMinY(), 0.00001);
        assertEquals(Double.NaN, a.getMaxX(), 0.00001);
        assertEquals(Double.NaN, a.getMaxY(), 0.00001);
        assertFalse(a.isValid());

    }

    @Test
    public void testSet_4args() {
        RectBuilder a = new RectBuilder().set(2, 4, 8, 16);
        assertTrue(a.isValid());
        assertEquals(2, a.getMinX(), 0.00001);
        assertEquals(4, a.getMinY(), 0.00001);
        assertEquals(8, a.getMaxX(), 0.00001);
        assertEquals(16, a.getMaxY(), 0.00001);
        a.set(3, 4, 1, 2);
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
        assertEquals(1, a.getMinX(), 0.00001);
        assertEquals(2, a.getMinY(), 0.00001);
        assertEquals(3, a.getMaxX(), 0.00001);
        assertEquals(4, a.getMaxY(), 0.00001);
    }

    @Test
    public void testSet_RectBuilder() {
        RectBuilder a = new RectBuilder().set(new RectBuilder(2, 4, 8, 16));
        assertTrue(a.isValid());
        assertEquals(2, a.getMinX(), 0.00001);
        assertEquals(4, a.getMinY(), 0.00001);
        assertEquals(8, a.getMaxX(), 0.00001);
        assertEquals(16, a.getMaxY(), 0.00001);
        a.set(new RectBuilder(3, 4, 1, 2));
        assertTrue(a.isValid());
        try {
            a.set((RectBuilder)null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals(1, a.getMinX(), 0.00001);
        assertEquals(2, a.getMinY(), 0.00001);
        assertEquals(3, a.getMaxX(), 0.00001);
        assertEquals(4, a.getMaxY(), 0.00001);
    }

    @Test
    public void testSet_Rect() {
        RectBuilder a = new RectBuilder().set(Rect.valueOf(2, 4, 8, 16));
        assertTrue(a.isValid());
        assertEquals(2, a.getMinX(), 0.00001);
        assertEquals(4, a.getMinY(), 0.00001);
        assertEquals(8, a.getMaxX(), 0.00001);
        assertEquals(16, a.getMaxY(), 0.00001);
        a.set(Rect.valueOf(3, 4, 1, 2));
        assertTrue(a.isValid());
        try {
            a.set((Rect)null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals(1, a.getMinX(), 0.00001);
        assertEquals(2, a.getMinY(), 0.00001);
        assertEquals(3, a.getMaxX(), 0.00001);
        assertEquals(4, a.getMaxY(), 0.00001);
    }

    @Test
    public void testIsValid() {
        RectBuilder rect = new RectBuilder(5, 5, 5, 5);
        assertTrue(rect.isValid());
        rect.set(5, 5, 4, 5);
        assertTrue(rect.isValid());
        rect.reset();
        assertFalse(rect.isValid());
    }

    @Test
    public void testGetWidth() {
        RectBuilder rect = new RectBuilder(5, 5, 5, 5);
        assertEquals(0, rect.getWidth(), 0.00001);
        rect.set(5, 5, 9, 5);
        assertEquals(4, rect.getWidth(), 0.00001);
        rect.set(5, 5, 1, 5);
        assertEquals(4, rect.getWidth(), 0.00001);
        rect.reset();
        assertEquals(Double.NaN, rect.getWidth(), 0.00001);
    }

    @Test
    public void testGetHeight() {
        RectBuilder rect = new RectBuilder(5, 5, 5, 5);
        assertEquals(0, rect.getHeight(), 0.00001);
        rect.set(5, 5, 5, 9);
        assertEquals(4, rect.getHeight(), 0.00001);
        rect.set(5, 5, 5, 1);
        assertEquals(4, rect.getHeight(), 0.00001);
        rect.reset();
        assertEquals(Double.NaN, rect.getHeight(), 0.00001);
    }

    @Test
    public void testGetArea() {
        RectBuilder rect = new RectBuilder(1, 3, 7, 13);
        assertEquals(60, rect.getArea(), 0.00001);
        rect.set(1, 13, 7, 3);
        assertEquals(60, rect.getArea(), 0.00001);
        rect.set(7, 3, 1, 13);
        assertEquals(60, rect.getArea(), 0.00001);
        rect.set(7, 13, 1, 3);
        assertEquals(60, rect.getArea(), 0.00001);
        rect.reset();
        assertEquals(Double.NaN, rect.getArea(), 0.00001);
    }
    
    @Test
    public void testAdd_double_double(){
        RectBuilder rect = new RectBuilder();
        assertSame(rect, rect.add(1, 4));
        assertEquals("[1,4,1,4]", rect.toString());
        assertSame(rect, rect.add(2, 3));
        try {
            rect.add(8,Double.POSITIVE_INFINITY);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            rect.add(Double.NaN,9);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        assertEquals("[1,3,2,4]", rect.toString());
    }
    
    @Test
    public void testAdd_Vect(){
        RectBuilder rect = new RectBuilder();
        assertSame(rect, rect.add(Vect.valueOf(1, 4)));
        assertEquals("[1,4,1,4]", rect.toString());
        assertSame(rect, rect.add(Vect.valueOf(2, 3)));
        try {
            rect.add((Vect)null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals("[1,3,2,4]", rect.toString());
    }
    
    @Test
    public void testAdd_VectBuilder(){
        RectBuilder rect = new RectBuilder();
        assertSame(rect, rect.add(new VectBuilder(1, 4)));
        assertEquals("[1,4,1,4]", rect.toString());
        assertSame(rect, rect.add(new VectBuilder(2, 3)));
        try {
            rect.add((VectBuilder)null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals("[1,3,2,4]", rect.toString());
    }
    
    @Test
    public void testAdd_Rect(){
        RectBuilder rect = new RectBuilder();
        assertSame(rect, rect.add(new RectBuilder(4,5,6,7)));
        assertEquals("[4,5,6,7]", rect.toString());
        assertSame(rect, rect.add(new RectBuilder(2, 3, 8, 9)));
        try {
            rect.add((RectBuilder)null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals("[2,3,8,9]", rect.toString());
    }
    
    @Test
    public void testAdd_RectBuilder(){
        RectBuilder rect = new RectBuilder();
        assertSame(rect, rect.add(Rect.valueOf(4,5,6,7)));
        assertEquals("[4,5,6,7]", rect.toString());
        assertSame(rect, rect.add(Rect.valueOf(2, 3, 8, 9)));
        try {
            rect.add((Rect)null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals("[2,3,8,9]", rect.toString());
    }
    
    @Test
    public void testAddAll_double() {
        RectBuilder rect = new RectBuilder(3, 11, 23, 37);
        double[] ords = new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 101, 102, 103};
        assertSame(rect, rect.addAll(ords, 5, 1));
        try {
            rect.addAll((double[])null, 0, 13);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            rect.addAll(ords, -1, 5);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            rect.addAll(ords, 1, 14);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            rect.addAll(new double[]{Double.NaN, 0, 0, 0}, 0, 4);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        try {
            rect.addAll(new double[]{0, 0, 0, Double.NEGATIVE_INFINITY}, 0, 4);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        assertEquals(new RectBuilder(3, 6, 23, 37), rect);
        rect = new RectBuilder(23, 37, 1, 2);
        assertSame(rect, rect.addAll(ords, 5, 1));
        assertEquals(new RectBuilder(1, 2, 23, 37), rect);
    }
    
////    @Test
////    public void testGetCx() {
////        RectBuilder rect = new RectBuilder(1, 3, 7, 13);
////        assertEquals(4, rect.getCx(), 0.00001);
////        rect.set(1, 13, 7, 3);
////        assertEquals(4, rect.getCx(), 0.00001);
////        rect.set(7, 3, 1, 13);
////        assertEquals(4, rect.getCx(), 0.00001);
////        rect.set(7, 13, 1, 3);
////        assertEquals(4, rect.getCx(), 0.00001);
////    }
////
////    @Test
////    public void testGetCy() {
////        RectBuilder rect = new RectBuilder(1, 3, 7, 13);
////        assertEquals(8, rect.getCy(), 0.00001);
////        rect.set(1, 13, 7, 3);
////        assertEquals(8, rect.getCy(), 0.00001);
////        rect.set(7, 3, 1, 13);
////        assertEquals(8, rect.getCy(), 0.00001);
////        rect.set(7, 13, 1, 3);
////        assertEquals(8, rect.getCy(), 0.00001);
////    }
////
////    @Test
////    public void testGetCentroid() {
////        RectBuilder rect = new RectBuilder(2, -17, -11, 3);
////        assertEquals(-4.5, rect.getCx(), 0.00001);
////        assertEquals(-7, rect.getCy(), 0.00001);
////        assertEquals(new Vect(-4.5, -7), rect.getCentroid(new Vect()));
////    }
////
////    @Test
////    public void testIsDisjoint() {
////        RectBuilder a = new RectBuilder(10, 10, 20, 20);
////        RectBuilder b = new RectBuilder();
////        for (int x1 = 0; x1 < 30; x1 += 5) {
////            for (int y1 = 0; y1 < 30; y1 += 5) {
////                for (int x2 = x1; x2 < 30; x2 += 5) {
////                    for (int y2 = y1; y2 < 30; y2 += 5) {
////                        boolean disjoint = (x1 > a.maxX) || (y1 > a.maxY) || (x2 < a.minX) || (y2 < a.minY);
////                        b.set(x1, y1, x2, y2).normalize();
////                        assertEquals(disjoint, a.isDisjoint(b));
////                        assertEquals(disjoint, b.isDisjoint(a));
////                    }
////                }
////            }
////        }
////        assertTrue(a.set(1, 2, 3, 4).isDisjoint(b.set(1, 4, 3, 2)));
////        assertTrue(a.set(3, 2, 1, 4).isDisjoint(b.set(1, 2, 3, 4)));
////        try {
////            a.isDisjoint(null);
////            fail("Exception expected");
////        } catch (NullPointerException ex) {
////            //expected
////        }
////    }
////
////    @Test
////    public void testIsOverlapping() {
////        RectBuilder a = new RectBuilder(10, 10, 20, 20);
////        RectBuilder b = new RectBuilder();
////        for (int x1 = 0; x1 < 30; x1 += 5) {
////            for (int y1 = 0; y1 < 30; y1 += 5) {
////                for (int x2 = x1; x2 < 30; x2 += 5) {
////                    for (int y2 = y1; y2 < 30; y2 += 5) {
////                        boolean overlapping = (x1 < a.maxX) && (y1 < a.maxY) && (x2 > a.minX) && (y2 > a.minY);
////                        b.set(x1, y1, x2, y2).normalize();
////                        assertEquals(overlapping, a.isOverlapping(b));
////                        assertEquals(overlapping, b.isOverlapping(a));
////                    }
////                }
////            }
////        }
////        assertFalse(a.set(1, 2, 3, 4).isOverlapping(b.set(1, 4, 3, 2)));
////        assertFalse(a.set(3, 2, 1, 4).isOverlapping(b.set(1, 2, 3, 4)));
////        try {
////            a.isOverlapping(null);
////            fail("Exception expected");
////        } catch (NullPointerException ex) {
////            //expected
////        }
////    }
////
////    @Test
////    public void testContains_RectBuilder() {
////        RectBuilder a = new RectBuilder(10, 10, 20, 20);
////        RectBuilder b = new RectBuilder();
////        for (int x1 = 0; x1 < 30; x1 += 5) {
////            for (int y1 = 0; y1 < 30; y1 += 5) {
////                for (int x2 = x1; x2 < 30; x2 += 5) {
////                    for (int y2 = y1; y2 < 30; y2 += 5) {
////                        boolean contains = (x1 >= a.minX) && (y1 >= a.minY) && (x2 <= a.maxX) && (y2 <= a.maxY);
////                        b.set(x1, y1, x2, y2).normalize();
////                        assertEquals(contains, a.contains(b));
////                    }
////                }
////            }
////        }
////        assertFalse(a.set(1, 2, 3, 4).contains(b.set(1, 4, 3, 2)));
////        assertFalse(a.set(3, 2, 1, 4).contains(b.set(1, 2, 3, 4)));
////        try {
////            a.contains((RectBuilder) null);
////            fail("Exception expected");
////        } catch (NullPointerException ex) {
////            //expected
////        }
////    }
////
////    @Test
////    public void testContains_Vect() {
////        RectBuilder a = new RectBuilder(10, 10, 20, 20);
////        Vect b = new Vect();
////        for (int x1 = 0; x1 < 30; x1 += 5) {
////            for (int y1 = 0; y1 < 30; y1 += 5) {
////                boolean contains = (x1 >= a.minX) && (y1 >= a.minY) && (x1 <= a.maxX) && (y1 <= a.maxY);
////                b.set(x1, y1);
////                assertEquals(contains, a.contains(b));
////            }
////        }
////        assertFalse(a.set(3, 4, 1, 2).contains(b.set(1, 2)));
////        try {
////            a.contains((Vect) null);
////            fail("Exception expected");
////        } catch (NullPointerException ex) {
////            //expected
////        }
////    }
////
////    @Test
////    public void testRelate() {
////        RectBuilder a = new RectBuilder(10, 10, 20, 20);
////        Vect b = new Vect();
////        for (int x1 = 0; x1 < 30; x1 += 5) {
////            for (int y1 = 0; y1 < 30; y1 += 5) {
////                b.set(x1, y1);
////                Relate relate = a.relate(b);
////                if ((x1 > a.minX) && (y1 > a.minY) && (x1 < a.maxX) && (y1 < a.maxY)) {
////                    assertEquals(Relate.INSIDE, relate);
////                } else if ((((x1 == a.minX) || (x1 == a.maxX)) && (y1 >= a.minY) && (y1 <= a.maxY))
////                        || (((y1 == a.minY) || (y1 == a.maxY)) && (x1 >= a.minX) && (x1 <= a.maxX))) {
////                    assertEquals(Relate.TOUCH, relate);
////                } else {
////                    assertEquals(Relate.OUTSIDE, relate);
////                }
////            }
////        }
////        assertEquals(Relate.OUTSIDE, a.set(20, 20, 10, 10).relate(b.set(15, 15)));
////        try {
////            a.relate((Vect) null);
////            fail("Exception expected");
////        } catch (NullPointerException ex) {
////            //expected
////        }
////    }
////
////    @Test
////    public void testIntersection() {
////        RectBuilder a = new RectBuilder(10, 10, 20, 20);
////        RectBuilder b = new RectBuilder();
////        RectBuilder c = new RectBuilder();
////        RectBuilder d = new RectBuilder();
////        for (int x1 = 0; x1 < 30; x1 += 5) {
////            for (int y1 = 0; y1 < 30; y1 += 5) {
////                for (int x2 = x1; x2 < 30; x2 += 5) {
////                    for (int y2 = y1; y2 < 30; y2 += 5) {
////                        b.set(x1, y1, x2, y2);
////                        boolean disjoint = (x1 > a.maxX) || (y1 > a.maxY) || (x2 < a.minX) || (y2 < a.minY);
////                        if (disjoint) {
////                            assertFalse(a.intersection(b, c).isValid());
////                        } else {
////                            d.set(Math.max(x1, a.minX),
////                                    Math.max(y1, a.minY),
////                                    Math.min(x2, a.maxX),
////                                    Math.min(y2, a.maxY));
////                            assertEquals(d, a.intersection(b, c));
////                        }
////                    }
////                }
////            }
////        }
////        assertEquals(d.reset(), a.set(20, 20, 10, 10).intersection(b.set(10, 10, 20, 20), c));
////        try {
////            a.intersection(null, c);
////            fail("Exception expected");
////        } catch (NullPointerException ex) {
////            //expected
////        }
////        try {
////            a.intersection(b, null);
////            fail("Exception expected");
////        } catch (NullPointerException ex) {
////            //expected
////        }
////    }
////
////    @Test
////    public void testUnion_RectBuilder_RectBuilder() {
////        RectBuilder a = new RectBuilder(10, 10, 20, 20);
////        RectBuilder b = new RectBuilder();
////        RectBuilder c = new RectBuilder();
////        for (int x1 = 0; x1 < 30; x1 += 5) {
////            for (int y1 = 0; y1 < 30; y1 += 5) {
////                for (int x2 = x1; x2 < 30; x2 += 5) {
////                    for (int y2 = y1; y2 < 30; y2 += 5) {
////                        b.set(x1, y1, x2, y2);
////                        a.union(b, c);
////                        assertEquals(Math.min(x1, a.minX), c.getMinX(), 0.00001);
////                        assertEquals(Math.min(y1, a.minY), c.getMinY(), 0.00001);
////                        assertEquals(Math.max(x2, a.maxX), c.getMaxX(), 0.00001);
////                        assertEquals(Math.max(y2, a.maxY), c.getMaxY(), 0.00001);
////                    }
////                }
////            }
////        }
////        b.reset();
////        a.union(b, c);
////        assertEquals(a, c);
////        assertFalse(b.isValid());
////        b.union(a, c);
////        assertEquals(a, c);
////        assertFalse(b.isValid());
////        a.reset();
////        a.union(b, c);
////        assertFalse(a.isValid());
////        assertFalse(b.isValid());
////        assertFalse(c.isValid());
////
////        try {
////            a.union(b, null);
////            fail("Exception expected");
////        } catch (NullPointerException ex) {
////            //expected
////        }
////        try {
////            a.union(null, c);
////            fail("Exception expected");
////        } catch (NullPointerException ex) {
////            //expected
////        }
////    }
////
////    @Test
////    public void testUnion_RectBuilder() {
////        RectBuilder a = new RectBuilder(10, 10, 20, 20);
////        RectBuilder b = new RectBuilder();
////        for (int x1 = 0; x1 < 30; x1 += 5) {
////            for (int y1 = 0; y1 < 30; y1 += 5) {
////                for (int x2 = x1; x2 < 30; x2 += 5) {
////                    for (int y2 = y1; y2 < 30; y2 += 5) {
////                        a.set(10, 10, 20, 20);
////                        b.set(x1, y1, x2, y2);
////                        assertSame(a, a.union(b));
////                        assertEquals(Math.min(x1, 10), a.getMinX(), 0.00001);
////                        assertEquals(Math.min(y1, 10), a.getMinY(), 0.00001);
////                        assertEquals(Math.max(x2, 20), a.getMaxX(), 0.00001);
////                        assertEquals(Math.max(y2, 20), a.getMaxY(), 0.00001);
////                    }
////                }
////            }
////        }
////        b.reset();
////        assertSame(a.set(10, 20, 30, 40), a.union(b));
////        assertEquals(a, b.set(10, 20, 30, 40));
////        try {
////            a.union((RectBuilder) null);
////            fail("Exception expected");
////        } catch (NullPointerException ex) {
////            //expected
////        }
////    }
////
////    @Test
////    public void testUnion_Vect() {
////        RectBuilder a = new RectBuilder(10, 10, 20, 20);
////        Vect b = new Vect();
////        for (int x1 = 0; x1 < 30; x1 += 5) {
////            for (int y1 = 0; y1 < 30; y1 += 5) {
////                a.set(10, 10, 20, 20);
////                b.set(x1, y1);
////                assertSame(a, a.union(b));
////                assertEquals(Math.min(x1, 10), a.getMinX(), 0.00001);
////                assertEquals(Math.min(y1, 10), a.getMinY(), 0.00001);
////                assertEquals(Math.max(x1, 20), a.getMaxX(), 0.00001);
////                assertEquals(Math.max(y1, 20), a.getMaxY(), 0.00001);
////            }
////        }
////        b.set(15, 15);
////        assertSame(a.set(10, 20, 30, 40), a.union(b));
////        try {
////            a.union((Vect) null);
////            fail("Exception expected");
////        } catch (NullPointerException ex) {
////            //expected
////        }
////    }
////
////    @Test
////    public void testUnion_double_double() {
////        RectBuilder a = new RectBuilder(10, 10, 20, 20);
////        for (int x1 = 0; x1 < 30; x1 += 5) {
////            for (int y1 = 0; y1 < 30; y1 += 5) {
////                a.set(10, 10, 20, 20);
////                assertSame(a, a.union(x1, y1));
////                assertEquals(Math.min(x1, 10), a.getMinX(), 0.00001);
////                assertEquals(Math.min(y1, 10), a.getMinY(), 0.00001);
////                assertEquals(Math.max(x1, 20), a.getMaxX(), 0.00001);
////                assertEquals(Math.max(y1, 20), a.getMaxY(), 0.00001);
////            }
////        }
////        assertSame(a.set(10, 20, 30, 40), a.union(15, 25));
////        try {
////            a.union((Vect) null);
////            fail("Exception expected");
////        } catch (NullPointerException ex) {
////            //expected
////        }
////        assertEquals(new RectBuilder(14, 25, 15, 26), a.reset().union(15, 25).union(14, 26));
////    }
////
////
////    @Test
////    public void testBuffer() {
////        RectBuilder rect = new RectBuilder(11, 17, 29, 43);
////        assertEquals(new RectBuilder(10, 16, 30, 44), rect.buffer(1));
////        assertEquals(new RectBuilder(12, 18, 28, 42), rect.buffer(-2));
////        assertEquals(new RectBuilder(12, 18, 28, 42), rect.buffer(0));
////        try {
////            rect.buffer(Double.NaN);
////            fail("Exception expected");
////        } catch (IllegalArgumentException ex) {
////            //Expected
////        }
////        try {
////            rect.buffer(Double.POSITIVE_INFINITY);
////            fail("Exception expected");
////        } catch (IllegalArgumentException ex) {
////            //Expected
////        }
////        rect.buffer(-10);
////        assertFalse(rect.isValid());
////        try {
////            rect.buffer(1);
////            fail("Exception expected");
////        } catch (IllegalStateException ex) {
////            //Expected
////        }
////        assertFalse(rect.set(0, 0, 2, 10).buffer(-2).isValid());
////        assertFalse(rect.set(0, 0, 10, 2).buffer(-2).isValid());
////    }
////
////    @Test
////    public void testHashCode() {
////        assertEquals(new RectBuilder(2, 3, 11, 17).hashCode(), new RectBuilder(2, 3, 11, 17).hashCode()); // equal should have same hashcode
////        Set<Integer> hashes = new HashSet<>();
////        for (int i = 2; i < 52; i++) { // minor test - no collisions in 200 elements
////            int a = new RectBuilder(1, 1, 1, i).hashCode();
////            int c = new RectBuilder(1, -i, 1, 1).hashCode();
////            int b = new RectBuilder(1, 1, i, 1).hashCode();
////            int d = new RectBuilder(-i, 1, 1, 1).hashCode();
////            assertFalse(hashes.contains(a));
////            hashes.add(a);
////            assertFalse(hashes.contains(b));
////            hashes.add(b);
////            assertFalse(hashes.contains(c));
////            hashes.add(c);
////            assertFalse(hashes.contains(d));
////            hashes.add(d);
////        }
////    }
////
////    @Test
////    public void testEquals() {
////        assertEquals(new RectBuilder(1, 2, 3, 4), new RectBuilder(1, 2, 3, 4));
////        assertFalse(new RectBuilder(1, 2, 3, 4).equals(new RectBuilder(-1, 2, 3, 4)));
////        assertFalse(new RectBuilder(1, 2, 3, 4).equals(new RectBuilder(1, -2, 3, 4)));
////        assertFalse(new RectBuilder(1, 2, 3, 4).equals(new RectBuilder(1, 2, 5, 4)));
////        assertFalse(new RectBuilder(1, 2, 3, 4).equals(new RectBuilder(1, 2, 3, 5)));
////        assertFalse(new RectBuilder(1, 2, 3, 4).equals(null));
////        assertEquals(new RectBuilder(1, 2, 3, 4), new RectBuilder(1, 2, 3, 4));
////        assertFalse(new RectBuilder(1, 2, 3, 4).equals(new RectBuilder(-1, 2, 3, 4)));
////        assertFalse(new RectBuilder(1, 2, 3, 4).equals(new RectBuilder(1, -2, 3, 4)));
////        assertFalse(new RectBuilder(1, 2, 3, 4).equals(new RectBuilder(1, 2, 5, 4)));
////        assertFalse(new RectBuilder(1, 2, 3, 4).equals(new RectBuilder(1, 2, 3, 5)));
////    }
////
////    @Test
////    public void testToString_0args() {
////        assertEquals("[1,2,3,4]", new RectBuilder(1, 2, 3, 4).toString());
////        assertEquals("[1.5,2.5,3.5,4.5]", new RectBuilder(1.5, 2.5, 3.5, 4.5).toString());
////    }
////
////    @Test
////    public void testToString_Appendable() throws Exception {
////        StringBuilder str = new StringBuilder();
////        new RectBuilder(1, 2, 3, 4).toString(str);
////        assertEquals("[1,2,3,4]", str.toString());
////        str.setLength(0);
////        new RectBuilder(1.5, 2.5, 3.5, 4.5).toString(str);
////        assertEquals("[1.5,2.5,3.5,4.5]", str.toString());
////    }
////
////    @Test
////    public void testClone() throws Exception {
////        RectBuilder a = new RectBuilder(1, 2, 3, 4);
////        RectBuilder b = a.clone();
////        assertEquals(a, b);
////        a.reset();
////        assertFalse(a.equals(b));
////    }
////
////    @Test
////    public void testExternalize() throws Exception {
////        RectBuilder a = new RectBuilder(3, 7, 13, 23);
////        ByteArrayOutputStream bout = new ByteArrayOutputStream();
////        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
////            out.writeObject(a);
////        }
////        RectBuilder b;
////        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
////            b = (RectBuilder) in.readObject();
////        }
////        assertEquals(a, b);
////        bout = new ByteArrayOutputStream();
////        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
////            a.writeData(out);
////        }
////        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
////            b = RectBuilder.read(in);
////        }
////        assertEquals(a, b);
////    }
////
////    @Test
////    public void testNormalize(){
////        RectBuilder rect = new RectBuilder(3, 7, 13, 23);
////        assertSame(rect, rect.normalize());
////        assertEquals(new RectBuilder(3, 7, 13, 23), rect);
////        
////        assertSame(rect, rect.set(13, 7, 3, 23).normalize());
////        assertEquals(new RectBuilder(3, 7, 13, 23), rect);
////        
////        assertSame(rect, rect.set(3, 23, 13, 7).normalize());
////        assertEquals(new RectBuilder(3, 7, 13, 23), rect);
////    }
////    
////    @Test
////    public void testConstructor(){
////        RectBuilder a = new RectBuilder(3, 7, 13, 23);
////        RectBuilder b = new RectBuilder(a);
////        assertEquals(a, b);
////    }    
}