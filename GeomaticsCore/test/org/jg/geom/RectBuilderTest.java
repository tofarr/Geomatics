package org.jg.geom;

import java.util.HashSet;
import java.util.Set;
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
            a.set((RectBuilder) null);
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
            a.set((Rect) null);
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
    public void testAdd_double_double() {
        RectBuilder rect = new RectBuilder();
        assertSame(rect, rect.add(1, 4));
        assertEquals("[\"RE\",1,4,1,4]", rect.toString());
        assertSame(rect, rect.add(2, 3));
        try {
            rect.add(8, Double.POSITIVE_INFINITY);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            rect.add(Double.NaN, 9);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        assertEquals("[\"RE\",1,3,2,4]", rect.toString());
    }

    @Test
    public void testAdd_Vect() {
        RectBuilder rect = new RectBuilder();
        assertSame(rect, rect.add(Vect.valueOf(1, 4)));
        assertEquals("[\"RE\",1,4,1,4]", rect.toString());
        assertSame(rect, rect.add(Vect.valueOf(2, 3)));
        try {
            rect.add((Vect) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals("[\"RE\",1,3,2,4]", rect.toString());
    }

    @Test
    public void testAdd_VectBuilder() {
        RectBuilder rect = new RectBuilder();
        assertSame(rect, rect.add(new VectBuilder(1, 4)));
        assertEquals("[\"RE\",1,4,1,4]", rect.toString());
        assertSame(rect, rect.add(new VectBuilder(2, 3)));
        try {
            rect.add((VectBuilder) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals("[\"RE\",1,3,2,4]", rect.toString());
    }

    @Test
    public void testAdd_Rect() {
        RectBuilder rect = new RectBuilder();
        assertSame(rect, rect.add(new RectBuilder(4, 5, 6, 7)));
        assertEquals("[\"RE\",4,5,6,7]", rect.toString());
        assertSame(rect, rect.add(new RectBuilder(2, 3, 8, 9)));
        try {
            rect.add((RectBuilder) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals("[\"RE\",2,3,8,9]", rect.toString());
    }

    @Test
    public void testAdd_RectBuilder() {
        RectBuilder rect = new RectBuilder();
        assertSame(rect, rect.add(new RectBuilder(4, 5, 6, 7)));
        assertEquals("[\"RE\",4,5,6,7]", rect.toString());
        assertSame(rect, rect.add(new RectBuilder(2, 3, 8, 9)));
        assertSame(rect, rect.add(new RectBuilder()));
        assertEquals("[\"RE\",2,3,8,9]", rect.toString());
        try {
            rect.add((Rect) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals("[\"RE\",2,3,8,9]", rect.toString());
    }

    @Test
    public void testAdd_Geom() {
        RectBuilder rect = new RectBuilder();
        assertSame(rect, rect.add((Geom)Rect.valueOf(4, 5, 6, 7)));
        assertEquals("[\"RE\",4,5,6,7]", rect.toString());
        assertSame(rect, rect.add((Geom)Vect.valueOf(2, 9)));
        try {
            rect.add((Geom) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals("[\"RE\",2,5,6,9]", rect.toString());
    }

    @Test
    public void testAddAll_double() {
        RectBuilder rect = new RectBuilder(3, 11, 23, 37);
        double[] ords = new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 101, 102, 103};
        assertSame(rect, rect.addAll(ords, 5, 1));
        try {
            rect.addAll((double[]) null, 0, 13);
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

    @Test
    public void testAddRects() {
        Rect[] rects = new Rect[]{Rect.valueOf(0, 0, 0, 0), Rect.valueOf(2, 4, 6, 13), Rect.valueOf(2, 4, 7, 12), Rect.valueOf(2, 3, 6, 12), Rect.valueOf(1, 4, 6, 12), Rect.valueOf(14, 14, 14, 14)};
        RectBuilder a = new RectBuilder();
        assertSame(a, a.addRects(rects));
        assertEquals("[\"RE\",0,0,14,14]", a.toString());
        a.reset();
        a.addRects(rects, 1, 3);
        assertEquals("[\"RE\",2,3,7,13]", a.toString());
        try {
            a.addRects(null);
            fail("Exception Expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            a.addRects(null, 0, 1);
            fail("Exception Expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            a.addRects(rects, -1, 1);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            a.addRects(rects, 0, 7);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            a.addRects(rects, 1, 6);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        rects[2] = null;

        try {
            a.addRects(rects, 0, 6);
            fail("Exception Expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals("[\"RE\",2,3,7,13]", a.toString());
    }

    @Test
    public void testGetCx() {
        RectBuilder rect = new RectBuilder(1, 3, 7, 13);
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
        RectBuilder rect = new RectBuilder(1, 3, 7, 13);
        assertEquals(8, rect.getCy(), 0.00001);
        rect.set(1, 13, 7, 3);
        assertEquals(8, rect.getCy(), 0.00001);
        rect.set(7, 3, 1, 13);
        assertEquals(8, rect.getCy(), 0.00001);
        rect.set(7, 13, 1, 3);
        assertEquals(8, rect.getCy(), 0.00001);
    }

    @Test
    public void testBuffer() {
        RectBuilder rect = new RectBuilder(11, 17, 29, 43);
        assertEquals(new RectBuilder(10, 16, 30, 44), rect.buffer(1));
        assertEquals(new RectBuilder(12, 18, 28, 42), rect.buffer(-2));
        assertEquals(new RectBuilder(12, 18, 28, 42), rect.buffer(0));
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
    public void testBuild() {
        RectBuilder rect = new RectBuilder();
        assertNull(rect.build());
        rect.set(1, 3, 7, 13);
        assertEquals(Rect.valueOf(1, 3, 7, 13), rect.build());
    }
    
    
    @Test
    public void testHashCode() {
        assertEquals(new RectBuilder(2, 3, 11, 17).hashCode(), new RectBuilder(2, 3, 11, 17).hashCode()); // equal should have same hashcode
        Set<Integer> hashes = new HashSet<>();
        for (int i = 2; i < 52; i++) { // minor test - no collisions in 200 elements
            int a = new RectBuilder(1, 1, 1, i).hashCode();
            int c = new RectBuilder(1, -i, 1, 1).hashCode();
            int b = new RectBuilder(1, 1, i, 1).hashCode();
            int d = new RectBuilder(-i, 1, 1, 1).hashCode();
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
        assertEquals(new RectBuilder(1, 2, 3, 4), new RectBuilder(1, 2, 3, 4));
        assertFalse(new RectBuilder(1, 2, 3, 4).equals(new RectBuilder(-1, 2, 3, 4)));
        assertFalse(new RectBuilder(1, 2, 3, 4).equals(new RectBuilder(1, -2, 3, 4)));
        assertFalse(new RectBuilder(1, 2, 3, 4).equals(new RectBuilder(1, 2, 5, 4)));
        assertFalse(new RectBuilder(1, 2, 3, 4).equals(new RectBuilder(1, 2, 3, 5)));
        assertFalse(new RectBuilder(1, 2, 3, 4).equals(null));
        assertEquals(new RectBuilder(1, 2, 3, 4), new RectBuilder(1, 2, 3, 4));
        assertFalse(new RectBuilder(1, 2, 3, 4).equals(new RectBuilder(-1, 2, 3, 4)));
        assertFalse(new RectBuilder(1, 2, 3, 4).equals(new RectBuilder(1, -2, 3, 4)));
        assertFalse(new RectBuilder(1, 2, 3, 4).equals(new RectBuilder(1, 2, 5, 4)));
        assertFalse(new RectBuilder(1, 2, 3, 4).equals(new RectBuilder(1, 2, 3, 5)));
        assertFalse(new RectBuilder().equals(new RectBuilder(1, 2, 3, 5)));
        assertTrue(new RectBuilder().equals(new RectBuilder()));
        
    }
    
    @Test
    public void testClone(){
        RectBuilder a = new RectBuilder(1,2,3,4);
        RectBuilder b = a.clone();
        assertEquals(a, b);
        assertNotSame(a, b);
        a.reset();
        b = a.clone();
        assertEquals(a, b);
        assertNotSame(a, b);        
    }
}
