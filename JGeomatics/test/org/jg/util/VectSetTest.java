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
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.jg.geom.GeomException;
import org.jg.geom.Vect;
import org.jg.geom.VectBuilder;
import org.jg.util.VectSet.VectSetProcessor;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar
 */
public class VectSetTest {

    @Test
    public void testConstructor() {
        VectSet vects = new VectSet(5, 1);
        assertEquals(16, vects.ords.length);
        assertEquals(0, vects.size());
        assertEquals(1, vects.maxJumps);
        vects = new VectSet(9, 3);
        assertEquals(32, vects.ords.length);
        assertEquals(0, vects.size());
        assertEquals(3, vects.maxJumps);
        try {
            vects = new VectSet(0, 1);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expection expected
        }
        try {
            vects = new VectSet(5, 0);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expection expected
        }
    }

    @Test
    public void testSize() {
        VectSet vects = new VectSet();
        assertEquals(0, vects.size());
        vects.add(Vect.valueOf(1, 2));
        assertEquals(1, vects.size());
        vects.add(Vect.valueOf(1, 2));
        assertEquals(1, vects.size());
        vects.add(3, 4); //HASHES ALL SEEM SAME - NEED TO APPLY TO MAP TOO!
        assertEquals(2, vects.size());
        vects.add(3, 4);
        assertEquals(2, vects.size());
        vects.addAll(Arrays.asList(Vect.valueOf(5, 6), Vect.valueOf(7, 8)));
        assertEquals(4, vects.size());
        vects.addAll(Arrays.asList(Vect.valueOf(5, 6), Vect.valueOf(7, 8)));
        assertEquals(4, vects.size());
        vects.addAll(new VectSet().add(9, 10).add(11, 12));
        assertEquals(6, vects.size());
        vects.addAll(new VectSet().add(9, 10).add(11, 12));
        assertEquals(6, vects.size());
        vects.addAll(new VectList(13, 14, 15, 16));
        assertEquals(8, vects.size());
        vects.addAll(new VectList(13, 14, 15, 16));
        assertEquals(8, vects.size());
        vects.remove(13, 14);
        assertEquals(7, vects.size());

    }

    @Test
    public void testIsEmpty() {
        VectSet vects = new VectSet();
        assertTrue(vects.isEmpty());
        vects.add(1, 2);
        assertFalse(vects.isEmpty());
        vects.remove(1, 2);
        assertTrue(vects.isEmpty());
    }

    @Test
    public void testContains() {
        VectSet vects = new VectSet();
        for (int i = 1; i <= 32; i += 2) {
            assertFalse(vects.contains(i + 1, i + 2));
            assertFalse(vects.contains(Vect.valueOf(i + 1, i + 2)));
            assertFalse(vects.contains(new VectBuilder(i + 1, i + 2)));
            vects.add(i + 1, i + 2);
            assertTrue(vects.contains(i + 1, i + 2));
            assertTrue(vects.contains(Vect.valueOf(i + 1, i + 2)));
            assertTrue(vects.contains(new VectBuilder(i + 1, i + 2)));
        }
        //vects = new VectSet(4, 4);
        vects = new VectSet(4, 5);
        vects.add(1, 1);
        vects.add(1, 2);
        vects.add(1, 3);
        vects.add(1, 4);
        assertFalse(vects.contains(1, 5));
        vects.add(1, 5);
        try {
            vects.contains(1, Double.NaN);
            fail("Exception Expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            vects.contains(Double.NaN, 1);
            fail("Exception Expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            vects.contains((Vect)null);
            fail("Exception Expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            vects.contains((VectBuilder)null);
            fail("Exception Expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testAdd() {
        VectSet vects = new VectSet();
        vects.add(1, 2);
        vects.add(Vect.valueOf(3, 4));
        vects.add(3, 4);
        vects.add(Vect.valueOf(1, 2));
        assertEquals(2, vects.size());
        assertTrue(vects.contains(1, 2));
        assertTrue(vects.contains(3, 4));

        vects = new VectSet(2, 1);
        vects.add(0, 1); // these were formulated to have the same hash
        vects.add(0, 2);
        vects.add(0, 23);
        assertEquals(3, vects.size());
        assertEquals("[0,1, 0,2, 0,23]", vects.toString());
    }

    /*
    @Test
    public void testSameHash(){
        Vect a = Vect.valueOf(0, 1);
        int hashCode = a.hashCode();
        int index4 = hashCode & 3;
        int index8 = hashCode & 7;
        int index16 = hashCode & 15;
        for(int x = 0; x < 0x100; x++){
            for(int y = 0; y < 0x1000; y++){
                a.set(x, y);
                int h = a.hashCode();
                int i4 = h & 3;
                int i8 = h & 7;
                int i16 = h & 15;
                if((i4 == index4) && (i8== index8) && (i16 != index16)){
                    System.out.println(x+" "+y);
                }
            }
        }
        fail("Not found");
    }
     */
    @Test
    public void testAddAll_VectList() {
        VectSet vects = new VectSet();
        assertSame(vects, vects.addAll(new VectList(1, 2, 3, 4, 5, 6)));
        try {
            vects.addAll((VectList) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals(3, vects.size());
        assertTrue(vects.contains(1, 2));
        assertTrue(vects.contains(3, 4));
        assertTrue(vects.contains(5, 6));
    }

    @Test
    public void testAddAll_VectSet() {
        VectSet vects = new VectSet();
        assertSame(vects, vects.addAll(new VectSet().add(1, 2).add(3, 4).add(5, 6)));
        assertSame(vects, vects.addAll(new VectSet()));
        assertSame(vects, vects.addAll(vects));
        try {
            vects.addAll((VectList) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals(3, vects.size());
        assertTrue(vects.contains(1, 2));
        assertTrue(vects.contains(3, 4));
        assertTrue(vects.contains(5, 6));
    }

    @Test
    public void testAddAll_Iterable() {
        VectSet vects = new VectSet();
        assertSame(vects, vects.addAll(Arrays.asList(Vect.valueOf(1, 2), Vect.valueOf(3, 4), Vect.valueOf(5, 6))));
        try {
            vects.addAll((Iterable<Vect>) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        assertEquals(3, vects.size());
        assertTrue(vects.contains(1, 2));
        assertTrue(vects.contains(3, 4));
        assertTrue(vects.contains(5, 6));
    }

    @Test
    public void testRemove() {
        VectSet vects = new VectSet().add(1, 2).add(3, 4).add(5, 6);
        try {
            vects.remove(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            vects.remove(1, Double.NaN);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            vects.remove(Double.NEGATIVE_INFINITY, 1);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        assertFalse(vects.remove(Vect.valueOf(7, 8)));
        assertFalse(vects.remove(7, 8));
        assertTrue(vects.remove(Vect.valueOf(5, 6)));
        assertEquals(2, vects.size());
        assertFalse(vects.contains(5, 6));
        assertTrue(vects.remove(3, 4));
        assertFalse(vects.contains(3, 4));
        assertEquals(1, vects.size());
        vects = new VectSet(2, 1);
        vects.add(0, 1); // these were formulated to have the same hash
        vects.add(0, 2);
        vects.remove(0, 3);
        assertEquals(2, vects.size());
        assertEquals("[0,1, 0,2]", vects.toString());
    }

    @Test
    public void testClear() {
        VectSet a = new VectSet().add(1, 2).add(3, 4).add(5, 6);
        a.clear();
        assertFalse(a.contains(1, 2));
        assertFalse(a.contains(3, 4));
        assertFalse(a.contains(5, 6));
        assertTrue(a.isEmpty());
        assertEquals(0, a.size());
        a.add(1, 2).add(3, 4).add(5, 6);
        VectSet b = a.clone();
        a.clear();
        assertTrue(a.isEmpty());
        assertEquals(3, b.size());
        a.clear();
        assertTrue(a.isEmpty());
    }

    @Test
    public void testIterator() {
        VectSet vects = new VectSet().add(1, 2).add(3, 4).add(5, 6);
        Set<Vect> expected = new HashSet<>(Arrays.asList(Vect.valueOf(1, 2), Vect.valueOf(3, 4), Vect.valueOf(5, 6)));
        for (Vect vect : vects) {
            assertTrue(expected.remove(vect));
        }
        assertTrue(expected.isEmpty());
        Iterator<Vect> iter = vects.iterator();
        Vect vect = iter.next();
        iter.remove();
        assertTrue(iter.hasNext());
        try {
            iter.remove();
            fail("Exception expected");
        } catch (IllegalStateException ex) {
        }
        assertEquals(2, vects.size());
        assertEquals("[3,4, 5,6]", vects.toString());
        iter.next();
        vects.add(7, 8);
        try {
            iter.next();
            fail("Exception expected");
        } catch (ConcurrentModificationException ex) {
        }
    }

    @Test
    public void testToList() {
        VectSet vects = new VectSet().add(1, 2).add(3, 4).add(5, 6);
        VectList list = new VectList();
        assertSame(list, vects.toList(list));
        list.sort();
        assertEquals("[1,2, 3,4, 5,6]", list.toString());
    }

    @Test
    public void testEqualsHash() {
        VectSet a = new VectSet().add(1, 2).add(3, 4).add(5, 6);
        VectSet b = new VectSet().add(1, 2).add(5, 6).add(3, 4);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a, b);
        b.add(7, 8);
        assertFalse(a.hashCode() == b.hashCode());
        assertFalse(a.equals(b));
        b.remove(7, 8);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a, b);
        b.remove(3, 4);
        assertFalse(a.hashCode() == b.hashCode());
        assertFalse(a.equals(b));
        assertFalse(a.equals(null));
    }

    @Test
    public void testToString() {
        VectSet a = new VectSet();
        StringBuilder expectedB = new StringBuilder("[");
        for (int i = 1; i <= 102; i += 2) {
            a.add(i, i + 1);
            expectedB.append(i).append(',').append(i + 1).append(", ");
        }
        expectedB.setLength(expectedB.length() - 2);
        expectedB.append(']');
        String expected = expectedB.toString();
        assertEquals("{size:51, bounds:[1,2,101,102]}", a.toString());
        StringBuilder str = new StringBuilder();
        a.toString(str);
        assertEquals(expected, str.toString());
    }

    @Test
    public void testClone() throws Exception {
        VectSet a = new VectSet().add(1, 2).add(3, 4).add(5, 6);
        VectSet b = a.clone();
        assertEquals(a, b);
        b.add(7, 8);
        assertFalse(a.equals(b));
        b = a.clone();
        assertEquals(a, b);
        b.add(5, 6);
        assertTrue(a.equals(b));
        b = a.clone();
        assertEquals(a, b);
        a.add(7, 8);
        assertFalse(a.equals(b));
        b = a.clone();
        assertEquals(a, b);
        a.add(5, 6);
        assertTrue(a.equals(b));
    }

    @Test
    public void testExternalize() throws Exception {
        VectSet a = new VectSet().add(3, 7).add(13, 23);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
            out.writeObject(a);
        }
        VectSet b;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = (VectSet) in.readObject();
        }
        assertEquals(a, b);
        bout = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
            a.write(out);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = VectSet.read(in);
        }
        assertEquals(a, b);
        try {
            a.write(new DataOutputStream(new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                    throw new IOException();
                }

            }));
            fail("Exception expected");
        } catch (GeomException ex) {
        }
        try {
            VectSet.read(new DataInputStream(new InputStream() {
                @Override
                public int read() throws IOException {
                    throw new IOException();
                }

            }));
            fail("Exception expected");
        } catch (GeomException ex) {
        }
    }

    @Test
    public void testForEach() {
        final VectSet vects = new VectSet();
        final Set<Vect> expected = new HashSet<>();
        for(int i = 1; i < 8;){
            Vect vect = Vect.valueOf(i++, i++);
            vects.add(vect);
            expected.add(vect);
        }
        assertTrue(vects.forEach(new VectSetProcessor(){
            @Override
            public boolean process(double x, double y) {
                assertTrue(expected.remove(Vect.valueOf(x, y)));
                return true;
            }        
        }));
        assertTrue(expected.isEmpty());
        assertFalse(vects.forEach(new VectSetProcessor(){
            boolean done;
            @Override
            public boolean process(double x, double y) {
                assertFalse(done);
                done = true;
                return false;
            }        
        }));
        try{
            vects.forEach(new VectSetProcessor(){
               @Override
               public boolean process(double x, double y) {
                   vects.add(9, 10);
                   return true;
               }        
            });
            fail("Exception expected");
        }catch(ConcurrentModificationException ex){
        }
    }
}
