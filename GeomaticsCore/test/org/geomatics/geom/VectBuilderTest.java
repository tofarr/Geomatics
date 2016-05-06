package org.geomatics.geom;

import org.geomatics.geom.VectBuilder;
import org.geomatics.geom.Vect;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar_000
 */
public class VectBuilderTest {

    @Test
    public void testConstructor() {
        VectBuilder a = new VectBuilder();
        assertEquals(0, a.getX(), 0.00001);
        assertEquals(0, a.getY(), 0.00001);
    }
    
    @Test
    public void testConstructor_Vect() {
        VectBuilder a = new VectBuilder(Vect.valueOf(1, 3));
        try {
            a = new VectBuilder((Vect)null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        assertEquals(1, a.getX(), 0.00001);
        assertEquals(3, a.getY(), 0.00001);
    }
    
    @Test
    public void testConstructor_VectBuilder() {
        VectBuilder a = new VectBuilder(new VectBuilder(1, 3));
        try {
            a = new VectBuilder((VectBuilder)null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        assertEquals(1, a.getX(), 0.00001);
        assertEquals(3, a.getY(), 0.00001);
    }

    @Test
    public void testConstructor_double_double() {
        VectBuilder a = new VectBuilder(1, 3);
        try {
            a = new VectBuilder(Double.NaN, 4);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        try {
            a = new VectBuilder(3, Double.POSITIVE_INFINITY);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        assertEquals(1, a.getX(), 0.00001);
        assertEquals(3, a.getY(), 0.00001);
    }

    @Test
    public void testSet_double_double() {
        VectBuilder a = new VectBuilder();
        assertSame(a, a.set(1, 2));
        try {
            a.set(Double.NaN, 4);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        try {
            a.set(3, Double.POSITIVE_INFINITY);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        assertEquals(1, a.getX(), 0.00001);
        assertEquals(2, a.getY(), 0.00001);
    }

    @Test
    public void testSet_Vect() {
        VectBuilder a = new VectBuilder();
        assertSame(a, a.set(new Vect(1, 2)));
        try {
            a.set((Vect)null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        assertEquals(1, a.getX(), 0.00001);
        assertEquals(2, a.getY(), 0.00001);
    }

    @Test
    public void testSet_VectBuilder() {
        VectBuilder a = new VectBuilder();
        assertSame(a, a.set(new VectBuilder(1, 2)));
        try {
            a.set((VectBuilder)null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        assertEquals(1, a.getX(), 0.00001);
        assertEquals(2, a.getY(), 0.00001);
    }

    @Test
    public void testSetX() {
        VectBuilder a = new VectBuilder(1, 2);
        a.setX(3);
        try {
            a.setX(Double.NaN);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        assertEquals(3, a.getX(), 0.00001);
    }

    @Test
    public void testSetY() {
        VectBuilder a = new VectBuilder(1, 2);
        a.setY(4);
        try {
            a.setY(Double.POSITIVE_INFINITY);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        assertEquals(4, a.getY(), 0.00001);
    }
    
    @Test
    public void testAdd_Vect() {
        VectBuilder a = new VectBuilder(1, 2);
        assertSame(a, a.add(new Vect(3, 5)));
        try{
            a.add((Vect)null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        assertEquals(4, a.getX(), 0.00001);
        assertEquals(7, a.getY(), 0.00001);
    }

    @Test
    public void testAdd_VectBuilder() {
        VectBuilder a = new VectBuilder(1, 2);
        assertSame(a, a.add(new VectBuilder(3, 5)));
        try{
            a.add((VectBuilder)null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        assertEquals(4, a.getX(), 0.00001);
        assertEquals(7, a.getY(), 0.00001);
    }

    @Test
    public void testAdd_double_double() {
        VectBuilder a = new VectBuilder(1, 2);
        assertSame(a, a.add(3, 5));
        try {
            a.set(Double.NaN, 11);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        try {
            a.set(13, Double.POSITIVE_INFINITY);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        assertEquals(4, a.getX(), 0.00001);
        assertEquals(7, a.getY(), 0.00001);
    }

    @Test
    public void testSub_Vect() {
        VectBuilder a = new VectBuilder(3, 5);
        assertSame(a, a.sub(new Vect(1, 2)));
        try{
            a.sub((Vect)null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        assertEquals(2, a.getX(), 0.00001);
        assertEquals(3, a.getY(), 0.00001);
    }


    @Test
    public void testSub_VectBuilder() {
        VectBuilder a = new VectBuilder(3, 5);
        assertSame(a, a.sub(new VectBuilder(1, 2)));
        try{
            a.sub((VectBuilder)null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        assertEquals(2, a.getX(), 0.00001);
        assertEquals(3, a.getY(), 0.00001);
    }

    @Test
    public void testSub_double_double() {
        VectBuilder a = new VectBuilder(3, 5);
        assertSame(a, a.sub(1, 2));
        try {
            a.set(Double.NaN, 11);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        try {
            a.set(13, Double.POSITIVE_INFINITY);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        assertEquals(2, a.getX(), 0.00001);
        assertEquals(3, a.getY(), 0.00001);
    }

    @Test
    public void testMul() {
        VectBuilder a = new VectBuilder(3, 5);
        assertSame(a, a.mul(2));
        try{
            a.mul(Double.NaN);
            fail("Exception expected");
        }catch(IllegalArgumentException ex){
        }
        assertEquals(6, a.getX(), 0.00001);
        assertEquals(10, a.getY(), 0.00001);
    }

    @Test
    public void testDiv() {
        VectBuilder a = new VectBuilder(6, 10);
        assertSame(a, a.div(2));
        try{
            a.div(Double.POSITIVE_INFINITY);
            fail("Exception expected");
        }catch(IllegalArgumentException ex){
        }
        try{
            a.div(0);
            fail("Exception expected");
        }catch(IllegalArgumentException ex){
        }
        assertEquals(3, a.getX(), 0.00001);
        assertEquals(5, a.getY(), 0.00001);
    }

    @Test
    public void testToString() {
        assertEquals("[\"PT\",0,0]", new VectBuilder().toString());
        assertEquals("[\"PT\",3.4,5.6]", new VectBuilder(3.4, 5.6).toString());
    }

    @Test
    public void testClone() {
        VectBuilder a = new VectBuilder(5, 7);
        assertNotSame(a, a.clone());
        assertEquals(a, a.clone());
    }
 
    @Test
    public void testCompareTo() {
        assertEquals(-1, new VectBuilder(1, 2).compareTo(new VectBuilder(3, 4)));
        try {
            new VectBuilder(1, 2).compareTo(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testHashCode() {
        assertEquals(new VectBuilder(1, 2).hashCode(), new VectBuilder(1, 2).hashCode()); // equal should have same hashcode
        Set<Integer> hashes = new HashSet<>();
        for (int i = 1; i < 100; i++) { // minor test - no collisions in 200 elements
            int a = new VectBuilder(0, i).hashCode();
            int b = new VectBuilder(i, 0).hashCode();
            assertFalse(hashes.contains(a));
            hashes.add(a);
            assertFalse(hashes.contains(b));
            hashes.add(b);
        }
    }

    @Test
    public void testEquals() {
        assertEquals(new VectBuilder(1, 2), new VectBuilder(1, 2));
        assertFalse(new VectBuilder(1, 2).equals(new VectBuilder(1, 3)));
        assertFalse(new VectBuilder(1, 2).equals(new VectBuilder(-1, 2)));
        assertFalse(new VectBuilder(1, 2).equals((Object) null)); // equals null should not throw an NPE
    }
    
    @Test
    public void testBuild(){
        VectBuilder vect = new VectBuilder();
        assertSame(Vect.ZERO, vect.build());
        vect.set(1, 0);
        assertEquals(Vect.valueOf(1, 0), vect.build());
        vect.set(0, 2);
        assertEquals(Vect.valueOf(0, 2), vect.build());
        vect.set(2, 3);
        assertEquals(Vect.valueOf(2, 3), vect.build());
    }
}
