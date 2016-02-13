package org.jg.geom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
        assertEquals("[0,0]", new VectBuilder().toString());
        assertEquals("[3.4,5.6]", new VectBuilder(3.4, 5.6).toString());
    }

    @Test
    public void testClone() {
        VectBuilder a = new VectBuilder(5, 7);
        assertNotSame(a, a.clone());
        assertEquals(a, a.clone());
    }
    
    @Test
    public void testExternalize() throws IOException {
        VectBuilder a = new VectBuilder(7,11);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try(DataOutputStream out = new DataOutputStream(bout)){
            a.write(out);
        }
        try(DataInputStream in = new DataInputStream(new ByteArrayInputStream(bout.toByteArray()))){
            VectBuilder b = VectBuilder.read(in);
            assertEquals(a, b);
        }
    }
}
