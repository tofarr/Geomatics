package org.jg;

import org.jg.VectSet.Iter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tim.ofarrell
 */
public class VectSetTest {

    @Test
    public void testConstructor(){
        VectSet vects = new VectSet(5, 1);
        assertEquals(16, vects.ords.length);
        assertEquals(0, vects.size());
        assertEquals(1, vects.maxJumps);
        vects = new VectSet(9, 3);
        assertEquals(32, vects.ords.length);
        assertEquals(0, vects.size());
        assertEquals(3, vects.maxJumps);
        try{
            vects = new VectSet(0, 1);
            fail("Exception expected");
        }catch(IllegalArgumentException ex){
            //expection expected
        }
        try{
            vects = new VectSet(5, 0);
            fail("Exception expected");
        }catch(IllegalArgumentException ex){
            //expection expected
        }
    }
    
    @Test
    public void testSize() {
        VectSet vects = new VectSet();
        assertEquals(0, vects.size());
        vects.add(new Vect(1, 2));
        assertEquals(1, vects.size());
        vects.add(new Vect(1, 2));
        assertEquals(1, vects.size());
        vects.add(3, 4); //HASHES ALL SEEM SAME - NEED TO APPLY TO MAP TOO!
        assertEquals(2, vects.size());
        vects.add(3, 4);
        assertEquals(2, vects.size());
        vects.addAll(Arrays.asList(new Vect(5, 6), new Vect(7, 8)));
        assertEquals(4, vects.size());
        vects.addAll(Arrays.asList(new Vect(5, 6), new Vect(7, 8)));
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
        for(int i = 1; i <= 32; i += 2){
            assertFalse(vects.contains(i+1, i+2));
            assertFalse(vects.contains(new Vect(i+1, i+2)));
            vects.add(i+1, i+2);
            assertTrue(vects.contains(i+1, i+2));
            assertTrue(vects.contains(new Vect(i+1, i+2)));
        }
        //vects = new VectSet(4, 4);
        vects = new VectSet(4, 5);
        vects.add(1, 1);
        vects.add(1, 2);
        vects.add(1, 3);
        vects.add(1, 4);
        assertFalse(vects.contains(1, 5));
        vects.add(1, 5);
        try{
            vects.contains(1, Double.NaN);
            fail("Exception Expected");
        }catch(IllegalArgumentException ex){
            //expected
        }
        try{
            vects.contains(Double.NaN, 1);
            fail("Exception Expected");
        }catch(IllegalArgumentException ex){
            //expected
        }
        try{
            vects.contains(null);
            fail("Exception Expected");
        }catch(NullPointerException ex){
            //expected
        }
    }

    @Test
    public void testAdd() {
        VectSet vects = new VectSet();
        vects.add(1, 2);
        vects.add(new Vect(3, 4));
        vects.add(3, 4);
        vects.add(new Vect(1, 2));
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
        Vect a = new Vect(0, 1);
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
        try{
            vects.addAll((VectList)null);
            fail("Exception expected");
        }catch(NullPointerException ex){
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
        try{
            vects.addAll((VectList)null);
            fail("Exception expected");
        }catch(NullPointerException ex){
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
        assertSame(vects, vects.addAll(Arrays.asList(new Vect(1, 2), new Vect(3, 4), new Vect(5, 6))));
        try{
            vects.addAll((Iterable<Vect>)null);
            fail("Exception expected");
        }catch(NullPointerException ex){
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
        assertFalse(vects.remove(new Vect(7, 8)));
        assertFalse(vects.remove(7, 8));
        assertTrue(vects.remove(new Vect(5, 6)));
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
        
    }

    @Test
    public void testIterator() {
        VectSet vects = new VectSet().add(1, 2).add(3, 4).add(5, 6);
        Iter iter = vects.iterator();
        Set<Vect> expected = new HashSet<>(Arrays.asList(new Vect(1, 2), new Vect(3, 4), new Vect(5, 6)));
        Vect vect = new Vect();
        assertTrue(iter.next(vect));
        assertTrue(expected.remove(vect));
        assertTrue(iter.next(vect));
        assertTrue(expected.remove(vect));
        try{
            iter.next(null);
            fail("Exception expected");
        }catch(NullPointerException ex){
            //expected
        }
        assertTrue(iter.next(vect));
        assertTrue(expected.remove(vect));
        assertFalse(iter.next(vect));
        vects.add(7, 8);
        try{
            iter.next(vect);
            fail("Exception expected");
        }catch(ConcurrentModificationException ex){
            //expected
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
        for(int i = 1; i <= 102; i += 2){
            a.add(i, i+1);
            expectedB.append(i).append(',').append(i+1).append(", ");
        }
        expectedB.setLength(expectedB.length()-2);
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
            a.writeData(out);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = VectSet.read(in);
        }
        assertEquals(a, b);
    }
}
