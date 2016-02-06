
package org.jg;

import org.jg.VectMap.Iter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar_000
 */
public class VectMapTest {

    @Test
    public void testConstructor(){
        VectMap vects = new VectMap(5, 1);
        assertEquals(16, vects.ords.length);
        assertEquals(8, vects.values.length);
        assertEquals(0, vects.size());
        assertEquals(1, vects.maxJumps);
        vects = new VectMap(9, 3);
        assertEquals(32, vects.ords.length);
        assertEquals(16, vects.values.length);
        assertEquals(0, vects.size());
        assertEquals(3, vects.maxJumps);
        try{
            vects = new VectMap(0, 1);
            fail("Exception expected");
        }catch(IllegalArgumentException ex){
            //expection expected
        }
        try{
            vects = new VectMap(5, 0);
            fail("Exception expected");
        }catch(IllegalArgumentException ex){
            //expection expected
        }
    }
    
    @Test
    public void testSize() {
        VectMap<String> vects = new VectMap();
        assertEquals(0, vects.size());
        assertSame(vects, vects.put(new Vect(1, 2), "A"));
        assertEquals(1, vects.size());
        vects.put(new Vect(1, 2), "B");
        assertEquals(1, vects.size());
        vects.put(3, 4, "C"); //HASHES ALL SEEM SAME - NEED TO APPLY TO MAP TOO!
        assertEquals(2, vects.size());
        vects.put(3, 4, "D");
        assertEquals(2, vects.size());
        vects.putAll(new VectMap().put(new Vect(5, 6), "E").put(new Vect(7, 8), "F"));
        assertEquals(4, vects.size());
        HashMap<Vect, String> map = new HashMap<>();
        map.put(new Vect(5, 6), "G");
        map.put(new Vect(7, 8), "H");
        vects.putAll(map);
        assertEquals(4, vects.size());
        vects.putAll(new VectMap().put(9, 10, "I").put(11, 12, "J"));
        assertEquals(6, vects.size());
        vects.putAll(new VectMap().put(9, 10, "K").put(11, 12, "L"));
        assertEquals(6, vects.size());
        assertEquals("K", vects.remove(9, 10));
        assertEquals(5, vects.size());
        
    }

    @Test
    public void testIsEmpty() {
        VectMap vects = new VectMap();
        assertTrue(vects.isEmpty());
        vects.put(1, 2, "A");
        assertFalse(vects.isEmpty());
        vects.remove(1, 2);
        assertTrue(vects.isEmpty());
    }

    @Test
    public void testContainsKey() {
        VectMap<Integer> vects = new VectMap();
        for(int i = 1; i <= 32; i += 2){
            assertFalse(vects.containsKey(i+1, i+2));
            assertFalse(vects.containsKey(new Vect(i+1, i+2)));
            vects.put(i+1, i+2, i);
            assertTrue(vects.containsKey(i+1, i+2));
            assertTrue(vects.containsKey(new Vect(i+1, i+2)));
        }
        //vects = new VectMap(4, 4);
        vects = new VectMap(4, 5);
        vects.put(1, 1, -1);
        vects.put(1, 2, -2);
        vects.put(1, 3, -3);
        vects.put(1, 4, -4);
        assertFalse(vects.containsKey(1, 5));
        vects.put(1, 5, -5);
        try{
            vects.containsKey(1, Double.NaN);
            fail("Exception Expected");
        }catch(IllegalArgumentException ex){
            //expected
        }
        try{
            vects.containsKey(Double.NaN, 1);
            fail("Exception Expected");
        }catch(IllegalArgumentException ex){
            //expected
        }
        try{
            vects.containsKey(null);
            fail("Exception Expected");
        }catch(NullPointerException ex){
            //expected
        }
    }

    @Test
    public void testGet() {
        VectMap<Integer> vects = new VectMap();
        for(int i = 1; i <= 32; i += 2){
            assertNull(vects.get(i+1, i+2));
            assertNull(vects.get(new Vect(i+1, i+2)));
            vects.put(i+1, i+2, i);
            assertEquals(new Integer(i), vects.get(i+1, i+2));
            assertEquals(new Integer(i), vects.get(new Vect(i+1, i+2)));
        }
        //vects = new VectMap(4, 4);
        vects = new VectMap(4, 5);
        vects.put(1, 1, -1);
        vects.put(1, 2, -2);
        vects.put(1, 3, -3);
        vects.put(1, 4, -4);
        assertNull(vects.get(1, 5));
        vects.put(1, 5, -5);
        try{
            vects.get(1, Double.NaN);
            fail("Exception Expected");
        }catch(IllegalArgumentException ex){
            //expected
        }
        try{
            vects.get(Double.NaN, 1);
            fail("Exception Expected");
        }catch(IllegalArgumentException ex){
            //expected
        }
        try{
            vects.get(null);
            fail("Exception Expected");
        }catch(NullPointerException ex){
            //expected
        }
    }
    
    @Test
    public void testPut() {
        VectMap<String> vects = new VectMap<>();
        vects.put(1, 2, "A");
        vects.put(new Vect(3, 4), "B");
        vects.put(3, 4, "C");
        vects.put(new Vect(1, 2), "D");
        assertEquals(2, vects.size());
        assertTrue(vects.containsKey(1, 2));
        assertTrue(vects.containsKey(3, 4));
        
        vects = new VectMap(2, 1);
        vects.put(0, 1, "E"); // these were formulated to have the same hash
        vects.put(0, 2, "F");
        vects.put(0, 23, "G");
        assertEquals(3, vects.size());
        assertEquals("[0,1,E, 0,2,F, 0,23,G]", vects.toString());
    }
    
    @Test
    public void testPutAll_Map() {
        HashMap<Vect,String> map = new HashMap<>();
        map.put(new Vect(1, 2), "A");
        map.put(new Vect(3, 4), "B");
        map.put(new Vect(5, 6), "C");
        VectMap vects = new VectMap();
        assertSame(vects, vects.putAll(map));
        assertSame(vects, vects.putAll(new HashMap<>()));
        try{
            vects.putAll((Map)null);
            fail("Exception expected");
        }catch(NullPointerException ex){
            //expected
        }
        assertEquals(3, vects.size());
        assertTrue(vects.containsKey(1, 2));
        assertTrue(vects.containsKey(3, 4));
        assertTrue(vects.containsKey(5, 6));
        assertEquals("A", vects.get(1, 2));
        assertEquals("B", vects.get(3, 4));
        assertEquals("C", vects.get(5, 6));
    }

    @Test
    public void testAddAll_VectMap() {
        VectMap vects = new VectMap();
        assertSame(vects, vects.putAll(new VectMap().put(1, 2, "A").put(3, 4, "B").put(5, 6, "C")));
        assertSame(vects, vects.putAll(new VectMap()));
        try{
            vects.putAll((VectMap)null);
            fail("Exception expected");
        }catch(NullPointerException ex){
            //expected
        }
        assertEquals(3, vects.size());
        assertTrue(vects.containsKey(1, 2));
        assertTrue(vects.containsKey(3, 4));
        assertTrue(vects.containsKey(5, 6));
        assertEquals("A", vects.get(1, 2));
        assertEquals("B", vects.get(3, 4));
        assertEquals("C", vects.get(5, 6));
    }

    @Test
    public void testRemove() {
        VectMap vects = new VectMap().put(1, 2, "A").put(3, 4, "B").put(5, 6, "C");
        try{
            vects.remove(null);
            fail("Exception expected");
        }catch(NullPointerException ex){
            //expected
        }
        try{
            vects.remove(1, Double.NaN);
            fail("Exception expected");
        }catch(IllegalArgumentException ex){
            //expected
        }
        try{
            vects.remove(Double.NEGATIVE_INFINITY, 1);
            fail("Exception expected");
        }catch(IllegalArgumentException ex){
            //expected
        }
        assertNull(vects.remove(new Vect(7, 8)));
        assertNull(vects.remove(7, 8));
        assertEquals("C", vects.remove(new Vect(5, 6)));
        assertEquals(2, vects.size());
        assertFalse(vects.containsKey(5, 6));
        assertEquals("B", vects.remove(3, 4));
        assertFalse(vects.containsKey(3, 4));
        assertEquals(1, vects.size());
        vects = new VectMap(2, 1);
        vects.put(0, 1, "A"); // these were formulated to have the same hash
        vects.put(0, 2, "B");
        vects.remove(0, 3);
        assertEquals(2, vects.size());
        assertEquals("[0,1,A, 0,2,B]", vects.toString());
    }

    @Test
    public void testClear() {
        VectMap a = new VectMap().put(1, 2, "A").put(3, 4, "B").put(5, 6, "C");
        a.clear();
        assertFalse(a.containsKey(1, 2));
        assertFalse(a.containsKey(3, 4));
        assertFalse(a.containsKey(5, 6));
        assertNull(a.get(1, 2));
        assertNull(a.get(3, 4));
        assertNull(a.get(5, 6));
        assertTrue(a.isEmpty());
        assertEquals(0, a.size());
        a.put(1, 2, "A").put(3, 4, "B").put(5, 6, "C");
        VectMap b = a.clone();
        a.clear();
        a.clear();
        assertTrue(a.isEmpty());
        assertEquals(3, b.size());
        
    }

    @Test
    public void testIterator() {
        VectMap vects = new VectMap().put(1, 2, "A").put(3, 4, "B").put(5, 6, "C");
        Iter iter = vects.iterator();
        Map<Vect, String> expected = new HashMap<>();
        expected.put(new Vect(1, 2), "A");
        expected.put(new Vect(3, 4), "B");
        expected.put(new Vect(5, 6), "C");
        Vect vect = new Vect();
        assertTrue(iter.next());
        iter.getVect(vect);
        assertEquals(iter.getX(), vect.x, 0.0001);
        assertEquals(iter.getY(), vect.y, 0.0001);
        assertEquals(expected.get(vect), iter.getValue());
        assertTrue(expected.remove(vect) != null);
        assertTrue(iter.next());
        iter.getVect(vect);
        assertEquals(iter.getX(), vect.x, 0.0001);
        assertEquals(iter.getY(), vect.y, 0.0001);
        assertEquals(expected.get(vect), iter.getValue());
        assertTrue(expected.remove(vect) != null);
        assertTrue(iter.next());
        iter.getVect(vect);
        assertEquals(iter.getX(), vect.x, 0.0001);
        assertEquals(iter.getY(), vect.y, 0.0001);
        assertEquals(expected.get(vect), iter.getValue());
        assertTrue(expected.remove(vect) != null);
        assertFalse(iter.next());
        vects.put(7, 8, "D");
        try{
            iter.next();
            fail("Exception expected");
        }catch(ConcurrentModificationException ex){
            //expected
        }
    }

    @Test
    public void testEqualsHash() {
        VectMap a = new VectMap().put(1, 2, "A").put(3, 4, "B").put(5, 6, "C");
        VectMap b = new VectMap().put(1, 2, "A").put(3, 4, "B").put(5, 6, "C");
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a, b);
        b.put(7, 8, "D");
        assertFalse(a.hashCode() == b.hashCode());
        assertFalse(a.equals(b));
        b.remove(7, 8);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a, b);
        b.remove(3, 4);
        assertFalse(a.hashCode() == b.hashCode());
        assertFalse(a.equals(b));
        assertFalse(a.equals(null));
        b = a.clone();
        b.put(3, 4, "D");
        assertFalse(a.equals(b));
        b = new VectMap().put(1,2,"A").put(4,3,"B").put(5,6,"C");
        assertFalse(a.equals(b));
    }

    @Test
    public void testToString() {
        VectMap a = new VectMap();
        assertEquals("[]", a.toString());
        StringBuilder expectedB = new StringBuilder("[");
        for(int i = 1; i <= 102; i += 2){
            a.put(i, i+1, -i);
            expectedB.append(i).append(',').append(i+1).append(',').append(-i).append(", ");
        }
        expectedB.setLength(expectedB.length()-2);
        expectedB.append(']');
        String expected = expectedB.toString();
        assertEquals("{size:51}", a.toString());
        StringBuilder str = new StringBuilder();
        a.toString(str);
        assertEquals(expected, str.toString());
        try{
        a.toString(new Appendable(){

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
        }catch(IllegalStateException ex){
            
        }
        
    }

    @Test
    public void testClone() throws Exception {
        VectMap a = new VectMap().put(1, 2, "A").put(3, 4, "B").put(5, 6, "C");
        VectMap b = a.clone();
        assertEquals(a, b);
        b.put(7, 8, "D");
        assertFalse(a.equals(b));
        b = a.clone();
        assertEquals(a, b);
        b.put(5, 6, "C");
        assertTrue(a.equals(b));
        b = a.clone();
        assertEquals(a, b);
        a.put(7, 8, "D");
        assertFalse(a.equals(b));
        b = a.clone();
        assertEquals(a, b);
        a.put(5, 6, "C");
        assertTrue(a.equals(b));
    }

    @Test
    public void testExternalize() throws Exception {
        VectMap a = new VectMap().put(3, 7, "A").put(13, 23, "B");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
            out.writeObject(a);
        }
        VectMap b;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = (VectMap) in.readObject();
        }
        assertEquals(a, b);
    }
}
