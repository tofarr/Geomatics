package org.jg;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar_000
 */
public class VectTest {

    @Test
    public void testConstructor() {
        Vect a = new Vect();
        assertEquals(0, a.x, 0.00001);
        assertEquals(0, a.y, 0.00001);
    }

    @Test
    public void testConstructor_double_double() {
        Vect a = new Vect(1, 2);
        try {
            a = new Vect(Double.NaN, 4);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        try {
            a = new Vect(3, Double.POSITIVE_INFINITY);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        assertEquals(1, a.x, 0.00001);
        assertEquals(2, a.y, 0.00001);
    }

    @Test
    public void testConstructor_Vect() {
        Vect a = new Vect(1, 2);
        Vect b = new Vect(a);
        try {
            b = new Vect(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        assertEquals(1, a.x, 0.00001);
        assertEquals(2, a.y, 0.00001);
        assertEquals(1, b.x, 0.00001);
        assertEquals(2, b.y, 0.00001);
        a.set(3, 4);
        assertEquals(3, a.x, 0.00001);
        assertEquals(4, a.y, 0.00001);
        assertEquals(1, b.x, 0.00001);
        assertEquals(2, b.y, 0.00001);
    }

    @Test
    public void testSet_double_double() {
        Vect a = new Vect();
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
        assertEquals(1, a.x, 0.00001);
        assertEquals(2, a.y, 0.00001);
    }

    @Test
    public void testSet_Vect() {
        Vect a = new Vect();
        assertSame(a, a.set(new Vect(1, 2)));
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
    public void testSetX() {
        Vect a = new Vect(1, 2);
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
        Vect a = new Vect(1, 2);
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
        Vect a = new Vect(1, 2);
        assertSame(a, a.add(new Vect(3, 5)));
        try{
            a.add(null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        assertEquals(4, a.getX(), 0.00001);
        assertEquals(7, a.getY(), 0.00001);
    }

    @Test
    public void testAdd_double_double() {
        Vect a = new Vect(1, 2);
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
        Vect a = new Vect(3, 5);
        assertSame(a, a.sub(new Vect(1, 2)));
        try{
            a.sub(null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        assertEquals(2, a.getX(), 0.00001);
        assertEquals(3, a.getY(), 0.00001);
    }

    @Test
    public void testSub_double_double() {
        Vect a = new Vect(3, 5);
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
        Vect a = new Vect(3, 5);
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
        Vect a = new Vect(6, 10);
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
    public void testLength() {
        Vect a = new Vect(3, 4);
        assertEquals(5, a.length(), 0.00001);
        a.set(0, 0);
        assertEquals(0, a.length(), 0.00001);
    }

    @Test
    public void testLengthSq() {
        Vect a = new Vect(3, 4);
        assertEquals(25, a.lengthSq(), 0.00001);
    }
    @Test
    public void testDist() {
        Vect a = new Vect(3, 4);
        Vect b = new Vect(6, 8);
        assertEquals(5, a.dist(b), 0.00001);
        try{
            a.dist(null);
            fail("Exception Expected");
        }catch(NullPointerException ex){
        }
    }

    @Test
    public void testDistSq_Vect() {
        Vect a = new Vect(3, 4);
        Vect b = new Vect(6, 8);
        assertEquals(25, a.distSq(b), 0.00001);
        try{
            a.distSq(null);
            fail("Exception Expected");
        }catch(NullPointerException ex){
        }
    }

    @Test
    public void testDydx() {
        Vect a = new Vect(4, 3);
        assertEquals(0.75, a.dydx(), 0.00001);
    }

    @Test
    public void testDydxTo_Vect() {
        Vect a = new Vect(10, 20);
        Vect b = new Vect(14, 23);
        assertEquals(0.75, a.dydxTo(b), 0.00001);
        try{
            a.dydxTo(null);
            fail("Exception Expected");
        }catch(NullPointerException ex){
        }
    }

    @Test
    public void testDirectionInRadians_0args() {
        assertEquals(0, new Vect(1, 0).directionInRadians(), 0.00001);
        assertEquals(Math.PI / 4, new Vect(1, 1).directionInRadians(), 0.00001);
        assertEquals(Math.PI / 2, new Vect(0, 1).directionInRadians(), 0.00001);
        assertEquals(Math.PI * 3 / 4, new Vect(-1, 1).directionInRadians(), 0.00001);
        assertEquals(Math.PI, new Vect(-1, 0).directionInRadians(), 0.00001);
        assertEquals(Math.PI * 5 / 4, new Vect(-1, -1).directionInRadians(), 0.00001);
        assertEquals(Math.PI * 3 / 2, new Vect(0, -1).directionInRadians(), 0.00001);
        assertEquals(Math.PI * 7 / 4, new Vect(1, -1).directionInRadians(), 0.00001);

        assertEquals(0, new Vect(1, 0).directionInRadians(), 0.00001);
        assertEquals(Math.PI / 2, new Vect(0, 2).directionInRadians(), 0.00001);
        assertEquals(Math.PI * 3 / 2, new Vect(0, -3).directionInRadians(), 0.00001);
        assertEquals(Math.PI / 4, new Vect(1, 1).directionInRadians(), 0.00001);
        assertEquals(Math.PI / 6, new Vect(10 * Math.cos(Math.PI / 6), 10 * Math.sin(Math.PI / 6)).directionInRadians(), 0.00001);
        try{
            new Vect().directionInRadians();
            fail("Exception expected");
        }catch(IllegalArgumentException ex){
        }
    }

    @Test
    public void testDirectionInRadiansTo_Vect() {
        assertEquals(0, new Vect(10, 10).directionInRadiansTo(new Vect(11, 10)), 0.00001);
        assertEquals(Math.PI / 4, new Vect(10, 10).directionInRadiansTo(new Vect(11, 11)), 0.00001);
        assertEquals(Math.PI / 2, new Vect(10, 10).directionInRadiansTo(new Vect(10, 11)), 0.00001);
        assertEquals(Math.PI * 3 / 4, new Vect(10, 10).directionInRadiansTo(new Vect(9, 11)), 0.00001);
        assertEquals(Math.PI, new Vect(10, 10).directionInRadiansTo(new Vect(9, 10)), 0.00001);
        assertEquals(Math.PI * 5 / 4, new Vect(10, 10).directionInRadiansTo(new Vect(9, 9)), 0.00001);
        assertEquals(Math.PI * 3 / 2, new Vect(10, 10).directionInRadiansTo(new Vect(10, 9)), 0.00001);
        assertEquals(Math.PI * 7 / 4, new Vect(10, 10).directionInRadiansTo(new Vect(11, 9)), 0.00001);

        assertEquals(0, new Vect(10, 10).directionInRadiansTo(new Vect(11, 10)), 0.00001);
        assertEquals(Math.PI / 6, new Vect(10, 20).directionInRadiansTo(new Vect(10 + 10 * Math.cos(Math.PI / 6), 20 + 10 * Math.sin(Math.PI / 6))), 0.00001);
        try {
            new Vect(3, 4).directionInRadiansTo(new Vect(3, 4));
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //Expected
        }
        try {
            new Vect(3, 4).directionInRadiansTo(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //Expected
        }
    }

    @Test
    public void testDot_Vect() {
         assertEquals(31, new Vect(2, 3).dot(new Vect(5, 7)), 0.00001);
        try {
            new Vect(3, 4).dot(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //Expected
        }
    }

    @Test
    public void testMatch() {
        Tolerance tolerance = new Tolerance(0.15);
        assertTrue(new Vect(2, 3).match(new Vect(2, 3), tolerance));
        assertTrue(new Vect(2, 3).match(new Vect(2.1, 3), tolerance));
        assertTrue(new Vect(2, 3).match(new Vect(2, 3.1), tolerance));
        assertTrue(new Vect(2, 3).match(new Vect(2.1, 3.1), tolerance));
        assertFalse(new Vect(2, 3).match(new Vect(2, 3.2), tolerance));
        assertFalse(new Vect(2, 3).match(new Vect(2.2, 3), tolerance));
        assertFalse(new Vect(2, 3).match(new Vect(2.2, 3.2), tolerance));
        assertFalse(new Vect(2, 3).match(new Vect(-2, 3), tolerance));
        assertFalse(new Vect(2, 3).match(new Vect(2, -3), tolerance));
    }

    @Test
    public void testCompare() {
        assertEquals(0, Vect.compare(1,1,1,1));
        assertEquals(-1, Vect.compare(1,1,1,2));
        assertEquals(-1, Vect.compare(1,1,2,1));
        assertEquals(-1, Vect.compare(1,1,2,2));
        assertEquals(1, Vect.compare(1,2,1,1));
        assertEquals(0, Vect.compare(1,2,1,2));
        assertEquals(-1, Vect.compare(1,2,2,1));
        assertEquals(-1, Vect.compare(1,2,2,2));
        assertEquals(1, Vect.compare(2,1,1,1));
        assertEquals(1, Vect.compare(2,1,1,2));
        assertEquals(0, Vect.compare(2,1,2,1));
        assertEquals(-1, Vect.compare(2,1,2,2));
        assertEquals(1, Vect.compare(2,2,1,1));
        assertEquals(1, Vect.compare(2,2,1,2));
        assertEquals(1, Vect.compare(2,2,2,1));
        assertEquals(0, Vect.compare(2,2,2,2));
    }

    @Test
    public void testCompareTo() {
        assertEquals(-1, new Vect(1,2).compareTo(new Vect(3,4)));
        try{
            new Vect(1,2).compareTo(null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
    }

    @Test
    public void testToString_0args() {
        assertEquals("[0,0]", new Vect().toString());
        assertEquals("[3.4,5.6]", new Vect(3.4, 5.6).toString());
    }

    @Test
    public void testToString_double_double() {
        StringBuilder str = new StringBuilder();
        new Vect(3.4,5.6).toString(str);
        assertEquals("[3.4,5.6]",str.toString());
        try{
            new Vect().toString(null);
        } catch (IllegalStateException ex) {
            //Expected
        }
    }

    @Test
    public void testHashCode() {
        assertEquals(new Vect(1, 2).hashCode(), new Vect(1, 2).hashCode()); // equal should have same hashcode
        Set<Integer> hashes = new HashSet<>();
        for (int i = 1; i < 100; i++) { // minor test - no collisions in 200 elements
            int a = new Vect(0, i).hashCode();
            int b = new Vect(i, 0).hashCode();
            assertFalse(hashes.contains(a));
            hashes.add(a);
            assertFalse(hashes.contains(b));
            hashes.add(b);
        }
    }

    @Test
    public void testEquals() {
        assertEquals(new Vect(1, 2), new Vect(1, 2));
        assertFalse(new Vect(1, 2).equals(new Vect(1, 3)));
        assertFalse(new Vect(1, 2).equals(new Vect(-1, 2)));
        assertFalse(new Vect(1, 2).equals((Object)null)); // equals null should not throw an NPE

    }

    @Test
    public void testClone() {
        Vect a = new Vect(5, 7);
        assertNotSame(a, a.clone());
        assertEquals(a, a.clone());
    }

    @Test
    public void testExternalize() throws Exception {
        Vect a = new Vect(7,11);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try(ObjectOutputStream out = new ObjectOutputStream(bout)){
            out.writeObject(a);
        }
        Vect b;
        try(ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))){
            b = (Vect)in.readObject();
        }
        assertEquals(a, b);
        bout = new ByteArrayOutputStream();
        try(ObjectOutputStream out = new ObjectOutputStream(bout)){
            a.writeData(out);
        }
        try(ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))){
            b = Vect.read(in);
        }
        assertEquals(a, b);
    }
    
    @Test
    public void testGetBounds(){
        Vect vect = new Vect(1, 2);
        Rect bounds = new Rect();
        vect.getBounds(bounds);
        assertEquals("[1,2,1,2]", bounds.toString());
    }
    
    @Test
    public void testGetPathIterator(){
        double[] coords = new double[6];
        AffineTransform at = new AffineTransform();
        at.translate(10, 20);
        Vect vect = new Vect(1, 2);
        PathIterator iter = vect.getPathIterator(at, 0);
        assertEquals(PathIterator.WIND_EVEN_ODD, iter.getWindingRule());
        
        assertFalse(iter.isDone());
        assertEquals(PathIterator.SEG_MOVETO, iter.currentSegment(coords));
        assertEquals(11, coords[0], 0.00001);
        assertEquals(22, coords[1], 0.00001);
        iter.next();
        
        assertFalse(iter.isDone());
        assertEquals(PathIterator.SEG_LINETO, iter.currentSegment(coords));
        assertEquals(11, coords[0], 0.00001);
        assertEquals(22, coords[1], 0.00001);
        iter.next();
        
        assertTrue(iter.isDone());
    }
}
