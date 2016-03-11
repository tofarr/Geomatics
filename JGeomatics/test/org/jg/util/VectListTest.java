package org.jg.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import org.jg.geom.GeomException;
import org.jg.geom.Line;
import org.jg.geom.Rect;
import org.jg.geom.Vect;
import org.jg.geom.VectBuilder;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofarrell
 */
public class VectListTest {

    @Test
    public void testConstructor() {
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
            vects = new VectList(1, 2, 3);
            fail("Exception Expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
    }

    @Test
    public void testSize() {
        VectList vects = new VectList();
        assertEquals(0, vects.size());
        vects.add(Vect.ZERO);
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
        vects.add(Vect.ZERO);
        assertFalse(vects.isEmpty());
        vects.remove(0);
        assertTrue(vects.isEmpty());
        vects = new VectList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);
        assertFalse(vects.isEmpty());
        vects.remove(0);
        assertFalse(vects.isEmpty());
    }

    @Test
    public void testGetVect() {
        VectList vects = new VectList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);
        assertEquals(new VectBuilder(1, 2), vects.getVect(0, new VectBuilder()));
        assertEquals(new VectBuilder(3, 4), vects.getVect(1, new VectBuilder()));
        assertEquals(Vect.valueOf(5, 6), vects.getVect(2));
        assertEquals(Vect.valueOf(7, 8), vects.getVect(3));
        assertEquals(Vect.valueOf(9, 0), vects.getVect(4));
        VectBuilder vect = new VectBuilder();
        assertSame(vect, vects.getVect(4, vect));
        try {
            vects.getVect(-1, new VectBuilder());
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.getVect(5, new VectBuilder());
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.getVect(-1);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.getVect(5);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        vects.remove(4);
        try {
            vects.getVect(3, (VectBuilder) null);
            fail("Exception Expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testGetLine() {
        VectList vects = new VectList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);
        assertEquals(Line.valueOf(1, 2, 3, 4), vects.getLine(0));
        assertEquals(Line.valueOf(3, 4, 5, 6), vects.getLine(1));
        assertEquals(Line.valueOf(5, 6, 7, 8), vects.getLine(2));
        assertEquals(Line.valueOf(7, 8, 9, 0), vects.getLine(3));
        try {
            vects.getLine(-1);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.getLine(5);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        vects.remove(4);
        try {
            vects.getLine(3);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
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
        assertNull(vects.getBounds());
        vects.add(1, 2);
        vects.add(5, 4);
        vects.add(3, 6);
        assertEquals(Rect.valueOf(1, 2, 5, 6), vects.getBounds());
        vects.add(0, 1);
        assertEquals(Rect.valueOf(0, 1, 5, 6), vects.getBounds());
        vects.remove(0);
        assertEquals(Rect.valueOf(0, 1, 5, 6), vects.getBounds());
        vects.set(1, Vect.valueOf(7, 8));
        assertEquals(Rect.valueOf(0, 1, 7, 8), vects.getBounds());
        vects.insert(0, Vect.valueOf(-1, -2));
        assertEquals(Rect.valueOf(-1, -2, 7, 8), vects.getBounds());
        vects.clear();
        assertNull(vects.getBounds());
        vects.addAll(new VectList(1, 2, 3, 4), 0, 2);
        assertEquals(Rect.valueOf(1, 2, 3, 4), vects.getBounds());
        assertEquals(Rect.valueOf(1, 2, 3, 4), vects.getBounds());
        vects.addAll(new VectList(1, 2, 5, 6), 1, 1);
        assertEquals(Rect.valueOf(1, 2, 5, 6), vects.getBounds());
        vects.transform(new TransformBuilder().scale(2).build());
        assertEquals(Rect.valueOf(2, 4, 10, 12), vects.getBounds());
    }

    @Test
    public void testIndexOf() {
        VectList vects = new VectList(1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 1, 2);
        assertEquals(0, vects.indexOf(Vect.valueOf(1, 2), 0));
        assertEquals(1, vects.indexOf(Vect.valueOf(3, 4), 0));
        assertEquals(4, vects.indexOf(Vect.valueOf(1, 2), 1));
        assertEquals(-1, vects.indexOf(Vect.valueOf(1, 3), 0));
        assertEquals(-1, vects.indexOf(Vect.valueOf(1, 2), 7));
        try {
            vects.indexOf(Vect.valueOf(1, 2), -1);
            fail("Exception Expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.indexOf(Vect.valueOf(1, 2), 8);
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
        assertEquals(0, new VectList(1, 2, 3, 2, 1, 4).lastIndexOf(Vect.valueOf(1, 2), 3));
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
        try {
            vects.lastIndexOf(null, 0);
            fail("Exception Expected");
        } catch (NullPointerException ex) {
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
        Transform matrix = new TransformBuilder().scale(2).translate(3, 5).build();
        VectList vects = new VectList(1, 2, 3, 4);
        vects.transform(matrix);
        vects.transform(Transform.IDENTITY);
        try {
            vects.transform(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals("[5,9, 9,13]", vects.toString());
        vects.clear();
        vects.transform(matrix);
        assertEquals("[]", vects.toString());
        vects.addAll(1, 2, 3, 4);
        vects.transform(matrix);
        assertEquals("[5,9, 9,13]", vects.toString());
    }

    @Test
    public void testIterator() {
        VectList vects = new VectList(1, 2, 3, 4, 5, 6);
        Iterator<Vect> iter = vects.iterator();
        assertTrue(iter.hasNext());
        assertEquals(Vect.valueOf(1, 2), iter.next());
        assertTrue(iter.hasNext());
        assertEquals(Vect.valueOf(3, 4), iter.next());
        iter.remove();
        assertTrue(iter.hasNext());
        assertEquals(Vect.valueOf(5, 6), iter.next());
        assertFalse(iter.hasNext());
        assertEquals("[1,2, 5,6]", vects.toString());
        vects.addAll(7, 8, 9, 10);
        iter = vects.iterator(1);
        assertTrue(iter.hasNext());
        assertEquals(Vect.valueOf(5, 6), iter.next());
        assertTrue(iter.hasNext());
        assertEquals(Vect.valueOf(7, 8), iter.next());
        iter.remove();
        assertTrue(iter.hasNext());
        assertEquals(Vect.valueOf(9, 10), iter.next());
        assertFalse(iter.hasNext());
        assertEquals("[1,2, 5,6, 9,10]", vects.toString());
    }

    @Test
    public void testAdd_Vect() {
        VectList vects = new VectList();
        assertSame(vects, vects.add(Vect.valueOf(1, 2)));
        try {
            vects.add((Vect)null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals("[1,2]", vects.toString());
        vects = new VectList(1, 2, 3, 4);
        vects.add(Vect.valueOf(5, 6));
        assertEquals("[1,2, 3,4, 5,6]", vects.toString());
    }
    
    @Test
    public void testAdd_VectBuilder() {
        VectList vects = new VectList();
        assertSame(vects, vects.add(new VectBuilder(1, 2)));
        try {
            vects.add((VectBuilder)null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals("[1,2]", vects.toString());
        vects = new VectList(1, 2, 3, 4);
        vects.add(new VectBuilder(5, 6));
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
        assertSame(vects, vects.set(1, Vect.valueOf(4, 3)));
        vects.set(0, Vect.valueOf(2, 1));
        vects.set(3, Vect.valueOf(8, 7));
        try {
            vects.set(-1, Vect.valueOf(2, 1));
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.set(4, Vect.valueOf(2, 1));
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
        assertSame(vects, vects.insert(1, Vect.valueOf(4, 3)));
        vects.insert(0, Vect.valueOf(2, 1));
        assertSame(vects, vects.insert(6, Vect.valueOf(8, 7)));
        try {
            vects.insert(-1, Vect.valueOf(2, 1));
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            vects.insert(8, Vect.valueOf(2, 1));
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
        vects.insert(1, Vect.valueOf(3, 4));
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
        assertSame(vects, vects.addAll(Arrays.asList(Vect.valueOf(5, 6), Vect.valueOf(7, 8))));
        assertSame(vects, vects.addAll(vects));
        try {
            vects.addAll((VectList) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            vects.addAll(Arrays.asList(Vect.valueOf(3, 2), null));
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            vects.addAll(Vect.valueOf(3, 2), null);
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
        assertSame(vects, vects.addAll(Vect.valueOf(5, 6), Vect.valueOf(7, 8)));
        try {
            vects.addAll(Vect.valueOf(3, 2), null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals(4, vects.size());
        assertEquals("[1,2, 3,4, 5,6, 7,8]", vects.toString());
    }

    @Test
    public void testAddAll_Iterable() {
        VectList a = new VectList(1, 2, 3, 4);
        assertSame(a, a.addAll(Arrays.asList(Vect.valueOf(5, 6), Vect.valueOf(7, 8))));
        try {
            a.addAll(Arrays.asList(Vect.valueOf(3, 2), null));
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            a.addAll((Iterable<Vect>) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals(4, a.size());
        assertEquals("[1,2, 3,4, 5,6, 7,8]", a.toString());
        
        VectList b = new VectList();
        b.addAll((Iterable<Vect>)a);
        assertEquals("[1,2, 3,4, 5,6, 7,8]", b.toString());
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
        vects.reverse();
        assertEquals(new VectList(5, 6, 3, 4, 1, 2), vects);
        vects.clear().addAll(1, 2, 3, 4, 5, 6, 7, 8).reverse();
        assertEquals(new VectList(7, 8, 5, 6, 3, 4, 1, 2), vects);
        vects.clear().reverse();
        assertEquals(new VectList(), vects);
    }

    @Test
    public void testSort() {
        VectList vects = new VectList(9, 0, 7, 8, 5, 6, 3, 4, 1, 2);
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
        for (int i = 0; i < 10;) {
            vects.add(i++, i++);
        }
        StringBuilder str = new StringBuilder("[0,1, 2,3, 4,5, 6,7, 8,9]");
        assertEquals(str.toString(), vects.toString(false));
        assertEquals(str.toString(), vects.toString(true));
        str.setLength(str.length()-1);
        for (int i = 10; i < 102;) {
            double x = i++;
            double y = i++;
            vects.add(x, y);
            str.append(", ").append(Vect.ordToStr(x)).append(',').append(Vect.ordToStr(y));
            
        }
        str.append(']');
        assertEquals("{size:51, bounds:[0,1,100,101]}", vects.toString());
        assertEquals("{size:51, bounds:[0,1,100,101]}", vects.toString(true));
        assertEquals(str.toString(), vects.toString(false));
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
        } catch (GeomException ex) {
            //expected
        }
    }

    @Test
    public void testClone() {
        VectList a = new VectList(1, 2, 3, 4);
        VectList b = a.clone();
        assertNotSame(a, b);
        assertEquals(a, b);
        b.set(0, Vect.valueOf(2, 1));
        assertEquals("[1,2, 3,4]", a.toString());
        assertEquals("[2,1, 3,4]", b.toString());
        b = a.clone();
        a.set(0, Vect.valueOf(2, 1));
        assertEquals("[2,1, 3,4]", a.toString());
        assertEquals("[1,2, 3,4]", b.toString());
    }

    @Test
    public void testExternalize() throws Exception {
        VectList a = new VectList(1, 2, 3, 4);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
            out.writeObject(a);
        }
        VectList b;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = (VectList) in.readObject();
        }
        assertEquals(a, b);
        bout = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
            a.write(out);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = VectList.read(in);
        }
        assertEquals(a, b);
        try{
            VectList.read(new DataInputStream(new InputStream(){
                @Override
                public int read() throws IOException {
                    throw new IOException();
                }
            
            }));
            fail("Exception expected");
        }catch(GeomException ex){
        }
        try{
            a.write(new DataOutputStream(new OutputStream(){
                
                @Override
                public void write(int b) throws IOException {
                    throw new IOException();
                }
            
            }));
            fail("Exception expected");
        }catch(GeomException ex){
        }
        try{
            VectList.read(new DataInputStream(new InputStream(){
                @Override
                public int read() throws IOException {
                    throw new IOException();
                }
            
            }));
            fail("Exception expected");
        }catch(GeomException ex){
        }
    }
    
    @Test
    public void testIsOrdered(){
        assertTrue(new VectList(1,2, 3,4, 5,6).isOrdered());
        assertTrue(new VectList(1,2, 3,4, 5,6, 1,2).isOrdered());
        assertFalse(new VectList(5,6, 3,4, 1,2).isOrdered());
        assertFalse(new VectList(1,2, 5,6, 3,4, 1,2).isOrdered());
        assertTrue(new VectList(1,2, 3,4, 1,2).isOrdered());
    }
    
    @Test
    public void testCompareTo(){
        VectList a = new VectList(1,2, 3,4, 5,6);
        VectList b = new VectList(1,2, 3,4, 5,6, 5,6);
        VectList c = a.clone();
        c.reverse();
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(1, b.compareTo(a));
        assertEquals(-1, a.compareTo(c));
        assertEquals(1, c.compareTo(a));
        try{
            a.compareTo(null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        
    }
}
