package org.jg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tim.ofarrell
 */
public class VectListTest {
    
    @Test
    public void testConstructor(){
        VectList vects = new VectList(new double[0]);
        vects.add(1, 2);
        assertEquals(new VectList(1, 2), vects);
        try {
            vects = new VectList(-1);
            fail("Exception Expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            vects = new VectList(1,2,3);
            fail("Exception Expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
    }

    @Test
    public void testSize() {
        VectList vects = new VectList();
        assertEquals(0, vects.size());
        vects.add(new Vect(0, 0));
        assertEquals(1, vects.size());
        vects = new VectList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);
        assertEquals(5, vects.size());
        vects.remove(0);
        assertEquals(4, vects.size());
    }

    @Test
    public void testIsEmpty() {
        VectList vects = new VectList();
        assertTrue(vects.isEmpty());
        vects.add(new Vect(0, 0));
        assertFalse(vects.isEmpty());
        vects.remove(0);
        assertTrue(vects.isEmpty());
        vects = new VectList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);
        assertFalse(vects.isEmpty());
        vects.remove(0);
        assertFalse(vects.isEmpty());
    }

    @Test
    public void testGet_int_Vect() {
        VectList vects = new VectList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);
        assertEquals(new Vect(1, 2), vects.get(0, new Vect()));
        assertEquals(new Vect(3, 4), vects.get(1, new Vect()));
        assertEquals(new Vect(5, 6), vects.get(2, new Vect()));
        assertEquals(new Vect(7, 8), vects.get(3, new Vect()));
        assertEquals(new Vect(9, 0), vects.get(4, new Vect()));
        Vect vect = new Vect();
        assertSame(vect, vects.get(4, vect));
        try {
            vects.get(-1, new Vect());
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.get(5, new Vect());
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        vects.remove(4);
        try {
            vects.get(4, new Vect());
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.get(3, (Vect) null);
            fail("Exception Expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testGet_int_Line() {
        VectList vects = new VectList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);
        assertEquals(new Line(1, 2, 3, 4), vects.get(0, new Line()));
        assertEquals(new Line(3, 4, 5, 6), vects.get(1, new Line()));
        assertEquals(new Line(5, 6, 7, 8), vects.get(2, new Line()));
        assertEquals(new Line(7, 8, 9, 0), vects.get(3, new Line()));
        Line line = new Line();
        assertSame(line, vects.get(3, line));
        try {
            vects.get(-1, new Line());
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.get(5, new Line());
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        vects.remove(4);
        try {
            vects.get(3, new Line());
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.get(2, (Line) null);
            fail("Exception Expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testGetX() {
        VectList vects = new VectList(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(1, vects.getX(0), 0.0001);
        assertEquals(3, vects.getX(1), 0.0001);
        assertEquals(5, vects.getX(2), 0.0001);
        assertEquals(7, vects.getX(3), 0.0001);
        try {
            vects.getX(-1);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.getX(4);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        vects.remove(3);
        try {
            vects.getX(3);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
    }

    @Test
    public void testGetY() {
        VectList vects = new VectList(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(2, vects.getY(0), 0.0001);
        assertEquals(4, vects.getY(1), 0.0001);
        assertEquals(6, vects.getY(2), 0.0001);
        assertEquals(8, vects.getY(3), 0.0001);
        try {
            vects.getY(-1);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.getY(4);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        vects.remove(3);
        try {
            vects.getY(3);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
    }

    @Test
    public void testGetBounds() {
        VectList vects = new VectList();
        Rect bounds = new Rect();
        assertFalse(vects.getBounds(bounds).isValid());
        vects.add(1, 2);
        vects.add(5, 4);
        vects.add(3, 6);
        assertEquals(new Rect(1, 2, 5, 6), vects.getBounds(bounds));
        vects.add(0, 1);
        assertEquals(new Rect(0, 1, 5, 6), vects.getBounds(bounds));
        vects.remove(0);
        assertEquals(new Rect(0, 1, 5, 6), vects.getBounds(bounds));
        vects.set(1, new Vect(7, 8));
        assertEquals(new Rect(0, 1, 7, 8), vects.getBounds(bounds));
        vects.insert(0, new Vect(-1, -2));
        assertEquals(new Rect(-1, -2, 7, 8), vects.getBounds(bounds));
        vects.clear();
        assertFalse(vects.getBounds(bounds).isValid());
        vects.addAll(new VectList(1, 2, 3, 4), 0, 2);
        assertEquals(new Rect(1, 2, 3, 4), vects.getBounds(bounds));
        vects.addAll(new VectList(1, 2, 5, 6), 1, 1);
        assertEquals(new Rect(1, 2, 5, 6), vects.getBounds(bounds));
        vects.transform(new Transform().scale(2));
        assertEquals(new Rect(2, 4, 10, 12), vects.getBounds(bounds));
    }

    @Test
    public void testIndexOf() {
        VectList vects = new VectList(1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 1, 2);
        assertEquals(0, vects.indexOf(new Vect(1, 2), 0));
        assertEquals(1, vects.indexOf(new Vect(3, 4), 0));
        assertEquals(4, vects.indexOf(new Vect(1, 2), 1));
        assertEquals(-1, vects.indexOf(new Vect(1, 3), 0));
        assertEquals(-1, vects.indexOf(new Vect(1, 2), 7));
        try {
            vects.indexOf(new Vect(1, 2), -1);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.indexOf(new Vect(1, 2), 8);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
    }

    @Test
    public void testLastIndexOf() {
        VectList vects = new VectList(1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 1, 2);
        assertEquals(6, vects.lastIndexOf(1, 2, 7));
        assertEquals(4, vects.lastIndexOf(1, 2, 6));
        assertEquals(5, vects.lastIndexOf(3, 4, 7));
        assertEquals(0, vects.lastIndexOf(1, 2, 1));
        assertEquals(-1, vects.lastIndexOf(1, 2, 0));
        assertEquals(-1, vects.lastIndexOf(1, 3, 7));
        try {
            vects.lastIndexOf(1, 2, -1);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.lastIndexOf(1, 2, 8);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
    }

    @Test
    public void testGetOrds() {
        VectList vects = new VectList(1, 2, 3, 4, 5, 6, 7, 8);
        double[] dst = new double[10];
        vects.getOrds(1, dst, 6, 2);

        try {
            vects.getOrds(1, null, 6, 2);
            fail("Exception Expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            vects.getOrds(-1, dst, 6, 2);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.getOrds(3, dst, 6, 2);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.getOrds(1, dst, -1, 2);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.getOrds(1, dst, 8, 2);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }

        double[] expected = new double[]{0, 0, 0, 0, 0, 0, 3, 4, 5, 6};
        assertTrue(Arrays.equals(expected, dst));
    }

    @Test
    public void testTransform() {
        Transform matrix = new Transform().scale(2).translate(3, 5);
        VectList vects = new VectList(1, 2, 3, 4);
        assertSame(vects, vects.transform(matrix));
        assertSame(vects, vects.transform(new Transform()));
        try {
            vects.transform(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals("[5,9, 9,13]", vects.toString());
        vects.clear();
        assertSame(vects, vects.transform(matrix));
        assertEquals("[]", vects.toString());
        vects.addAll(1, 2, 3, 4);
        assertSame(vects, vects.transform(matrix));
        assertEquals("[5,9, 9,13]", vects.toString());
    }

    @Test
    public void testIterator() {
        VectList.Iter iter = new VectList(1, 2, 3, 4, 5, 6).iterator();
        Vect vect = new Vect();
        assertEquals(0, iter.nextIndex());
        assertEquals(-1, iter.prevIndex());
        assertFalse(iter.prev(vect));
        assertEquals(0, iter.nextIndex());
        assertEquals(-1, iter.prevIndex());
        assertTrue(iter.next(vect));

        assertEquals(new Vect(1, 2), vect);
        assertEquals(1, iter.nextIndex());
        assertEquals(0, iter.prevIndex());
        assertTrue(iter.next(vect));

        assertEquals(new Vect(3, 4), vect);
        assertEquals(2, iter.nextIndex());
        assertEquals(1, iter.prevIndex());

        assertTrue(iter.next(vect));
        assertEquals(new Vect(5, 6), vect);
        assertEquals(3, iter.nextIndex());
        assertEquals(2, iter.prevIndex());

        assertFalse(iter.next(vect));

    }

    @Test
    public void testIterator_int() {
        VectList.Iter iter = new VectList(1, 2, 3, 4, 5, 6).iterator(2);
        Vect vect = new Vect();
        assertEquals(2, iter.nextIndex());
        assertEquals(1, iter.prevIndex());
        assertTrue(iter.next(vect));
        assertEquals(new Vect(5, 6), vect);
        assertEquals(3, iter.nextIndex());
        assertEquals(2, iter.prevIndex());

        assertFalse(iter.next(vect));

        assertTrue(iter.prev(vect));
        assertEquals(new Vect(5, 6), vect);
        assertEquals(2, iter.nextIndex());
        assertEquals(1, iter.prevIndex());

        assertTrue(iter.prev(vect));
        assertEquals(new Vect(3, 4), vect);
        assertEquals(1, iter.nextIndex());
        assertEquals(0, iter.prevIndex());
    }

    @Test
    public void testAdd_Vect() {
        VectList vects = new VectList();
        assertSame(vects, vects.add(new Vect(1, 2)));
        try {
            vects.add(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals("[1,2]", vects.toString());
        vects = new VectList(1, 2, 3, 4);
        vects.add(new Vect(5, 6));
        assertEquals("[1,2, 3,4, 5,6]", vects.toString());
    }

    @Test
    public void testAdd_double_double() {
        VectList vects = new VectList();
        assertSame(vects, vects.add(1, 2));
        try {
            vects.add(1, Double.NaN);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            vects.add(Double.POSITIVE_INFINITY, 1);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        assertEquals("[1,2]", vects.toString());
        vects = new VectList(1, 2, 3, 4);
        vects.add(5, 6);
        assertEquals("[1,2, 3,4, 5,6]", vects.toString());
    }

    @Test
    public void testAdd_VectList_int() {
        VectList vects = new VectList(1, 2);
        vects.add(new VectList(5, 6, 3, 4), 1);
        try {
            vects.add(null, 1);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            vects.add(new VectList(9, 10), -1);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.add(new VectList(9, 10), 5);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        assertEquals(new VectList(1, 2, 3, 4), vects);

    }

    @Test
    public void testRemove() {
        VectList vects = new VectList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);
        assertSame(vects, vects.remove(4));
        vects.remove(2);
        vects.remove(0);
        try {
            vects.remove(-1);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.remove(3);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        assertEquals("[3,4, 7,8]", vects.toString());
    }

    @Test
    public void testSet() {
        VectList vects = new VectList(1, 2, 3, 4, 5, 6, 7, 8);
        assertSame(vects, vects.set(1, new Vect(4, 3)));
        vects.set(0, new Vect(2, 1));
        vects.set(3, new Vect(8, 7));
        try {
            vects.set(-1, new Vect(2, 1));
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.set(4, new Vect(2, 1));
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.set(0, null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals("[2,1, 4,3, 5,6, 8,7]", vects.toString());
    }

    @Test
    public void testInsert() {
        VectList vects = new VectList(1, 2, 3, 4, 5, 6, 7, 8);
        assertSame(vects, vects.insert(1, new Vect(4, 3)));
        vects.insert(0, new Vect(2, 1));
        assertSame(vects, vects.insert(6, new Vect(8, 7)));
        try {
            vects.insert(-1, new Vect(2, 1));
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.insert(8, new Vect(2, 1));
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.insert(0, null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals("[2,1, 1,2, 4,3, 3,4, 5,6, 7,8, 8,7]", vects.toString());
        assertEquals("[2,1, 1,2, 4,3, 3,4, 5,6, 7,8, 8,7]", vects.toString());
        vects = new VectList(1, 2, 5, 6);
        vects.insert(1, new Vect(3, 4));
        assertEquals("[1,2, 3,4, 5,6]", vects.toString());
    }

    @Test
    public void testClear() {
        VectList vects = new VectList(1, 2, 3, 4, 5, 6, 7, 8);
        assertSame(vects, vects.clear());
        assertEquals(0, vects.size());
        assertEquals("[]", vects.toString());
        vects = new VectList(1, 2, 3, 4);
        assertSame(vects, vects.clear());
        assertEquals(0, vects.size());
        assertEquals("[]", vects.toString());
    }

    @Test
    public void testAddAll_VectList() {
        VectList vects = new VectList(1, 2, 3, 4);
        assertSame(vects, vects.addAll(vects));
        assertSame(vects, vects.addAll(Arrays.asList(new Vect(5, 6), new Vect(7, 8))));
        assertSame(vects, vects.addAll(vects));
        try {
            vects.addAll((VectList) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            vects.addAll(Arrays.asList(new Vect(3, 2), null));
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            vects.addAll(new Vect(3, 2), null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals(12, vects.size());
        assertEquals("[1,2, 3,4, 1,2, 3,4, 5,6, 7,8, 1,2, 3,4, 1,2, 3,4, 5,6, 7,8]", vects.toString());
    }
    
    
    @Test
    public void testAddAll_VectList2() {
        VectList a = new VectList();
        VectList b = new VectList(1, 2, 3, 4);
        a.addAll(b);
        assertEquals(a, b);
        a.add(5, 6);
        assertEquals("[1,2, 3,4, 5,6]", a.toString());
        assertEquals("[1,2, 3,4]", b.toString());
    }

    @Test
    public void testAddAll_VectArr() {
        VectList vects = new VectList(1, 2, 3, 4);
        assertSame(vects, vects.addAll(new Vect(5, 6), new Vect(7, 8)));
        try {
            vects.addAll(new Vect(3, 2), null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals(4, vects.size());
        assertEquals("[1,2, 3,4, 5,6, 7,8]", vects.toString());
    }

    @Test
    public void testAddAll_Iterable() {
        VectList vects = new VectList(1, 2, 3, 4);
        assertSame(vects, vects.addAll(Arrays.asList(new Vect(5, 6), new Vect(7, 8))));
        try {
            vects.addAll(Arrays.asList(new Vect(3, 2), null));
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            vects.addAll((Iterable<Vect>) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals(4, vects.size());
        assertEquals("[1,2, 3,4, 5,6, 7,8]", vects.toString());
    }

    @Test
    public void testAddAll_3args_1() {
        VectList vects = new VectList(1, 2);
        assertSame(vects, vects.addAll(new VectList(1, 2, 3, 4, 5, 6, 7, 8), 1, 2));
        try {
            vects.addAll(new VectList(1, 2, 3, 4, 5, 6, 7, 8), -1, 2);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.addAll(new VectList(1, 2, 3, 4, 5, 6, 7, 8), 1, 5);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.addAll((VectList) null, 0, 1);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals(3, vects.size());
        assertEquals("[1,2, 3,4, 5,6]", vects.toString());
    }

    @Test
    public void testAddAll_3args_2() {
        VectList vects = new VectList(1, 2);
        assertSame(vects, vects.addAll(new double[]{1, 2, 3, 4, 5, 6, 7, 8}, 1, 2));
        try {
            vects.addAll(new double[]{1, 2, 3, 4, 5, 6, 7, 8}, -1, 2);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.addAll(new double[]{1, 2, 3, 4, 5, 6, 7, 8}, 2, 5);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.addAll((double[]) null, 0, 1);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            vects.addAll(new double[]{1, 2, Double.NaN, 4, 5, 6, 7, 8}, 0, 2);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            vects.addAll(new double[]{1, 2, Double.NaN, 4, 5, 6, 7, 8}, 0, -1);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        assertEquals(3, vects.size());
        assertEquals("[1,2, 2,3, 4,5]", vects.toString());
    }

    @Test
    public void testAddAll_doubleArr() {
        VectList vects = new VectList(1, 2);
        assertSame(vects, vects.addAll(new double[]{3, 4, 5, 6}));
        try {
            vects.addAll(new double[]{1, 2, 3, Double.NaN, 5, 6, 7, 8});
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            vects.addAll((double[]) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            vects.addAll(new double[]{1, 2, 3});
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        assertEquals(3, vects.size());
        assertEquals("[1,2, 3,4, 5,6]", vects.toString());
    }

    @Test
    public void testSwap() {
        VectList vects = new VectList(16).addAll(1, 2, 3, 4, 5, 6);
        assertSame(vects, vects.swap(0, 2));
        assertSame(vects, vects.swap(2, 2));
        try {
            vects.swap(-1, 1);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.swap(1, 3);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        assertEquals("[5,6, 3,4, 1,2]", vects.toString());
    }

    @Test
    public void testReverse() {
        VectList vects = new VectList(16).addAll(1, 2, 3, 4, 5, 6);
        assertSame(vects, vects.reverse());
        assertEquals(new VectList(5, 6, 3, 4, 1, 2), vects);
        assertSame(vects, vects.clear().addAll(1, 2, 3, 4, 5, 6, 7, 8).reverse());
        assertEquals(new VectList(7, 8, 5, 6, 3, 4, 1, 2), vects);
        assertSame(vects, vects.clear().reverse());
        assertEquals(new VectList(), vects);
    }

    @Test
    public void testSort() {
        VectList vects = new VectList(9,0,7,8,5,6,3,4,1,2);
        assertEquals(vects, vects.sort());
        assertEquals(new VectList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0), vects);
        vects = new VectList(5, 6, 1, 2, 7, 8, 3, 4, 9, 0);
        assertEquals(vects, vects.sort());
        assertEquals(new VectList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0), vects);
        vects = new VectList(5, 6, 1, 2, 7, 8, 3, 4);
        assertEquals(vects, vects.sort());
        assertEquals(new VectList(1, 2, 3, 4, 5, 6, 7, 8), vects);
        vects.clear().sort();
        assertEquals(new VectList(), vects);
    }

    @Test
    public void testEquals() {
        VectList vects = new VectList(1, 2, 3, 4);
        assertTrue(vects.equals(vects));
        assertEquals(vects, new VectList(1, 2, 3, 4));
        assertEquals(vects, new VectList(1, 2, 3, 4));
        assertFalse(vects.equals(new VectList(1, 2, 4, 3)));
        assertFalse(vects.equals(new VectList(1, 2, 4, 3)));
        assertFalse(vects.equals(new VectList(1, 2, 3, 4, 5, 6)));
        assertFalse(vects.equals(new VectList(1, 2, 3, 4, 5, 6)));
        assertFalse(vects.equals(null));
    }

    @Test
    public void testHashCode() {
        VectList vects = new VectList(1, 2, 3, 4);
        int hash = vects.hashCode();
        assertEquals(hash, vects.hashCode());
        vects.add(5, 6);
        assertFalse(hash == vects.hashCode());
    }

    @Test
    public void testToString() {
        VectList vects = new VectList();
        for (int i = 0; i < 102;) {
            vects.add(i++, i++);
        }
        assertEquals("{size:51, bounds:[0,1,100,101]}", vects.toString());
        try {
            vects.toString(new Appendable() {

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
        } catch (IllegalStateException ex) {
            //expected
        }
    }

    @Test
    public void testClone() {
        VectList a = new VectList(1, 2, 3, 4);
        VectList b = a.clone();
        assertNotSame(a, b);
        assertEquals(a, b);
        b.set(0, new Vect(2, 1));
        assertEquals("[1,2, 3,4]", a.toString());
        assertEquals("[2,1, 3,4]", b.toString());
        b = a.clone();
        a.set(0, new Vect(2, 1));
        assertEquals("[2,1, 3,4]", a.toString());
        assertEquals("[1,2, 3,4]", b.toString());
    }

    @Test
    public void testExternalize() throws Exception {
        VectList a = new VectList(1,2,3,4);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try(ObjectOutputStream out = new ObjectOutputStream(bout)){
            out.writeObject(a);
        }
        VectList b;
        try(ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))){
            b = (VectList)in.readObject();
        }
        assertEquals(a, b);
        bout = new ByteArrayOutputStream();
        try(ObjectOutputStream out = new ObjectOutputStream(bout)){
            a.writeData(out);
        }
        try(ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))){
            b = VectList.read(in);
        }
        assertEquals(a, b);
    }
}
