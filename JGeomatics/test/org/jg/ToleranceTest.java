package org.jg;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar_000
 */
public class ToleranceTest {

    @Test
    public void testGetTolerance() {
        Tolerance tolerance = new Tolerance(0.06);
        assertEquals(0.06, tolerance.getTolerance(), 0.00001);
        new Util(); // just for coverage
    }

    @Test
    public void testCheck() {
        Tolerance tolerance = new Tolerance(0.06);
        assertEquals(0, tolerance.check(0.05));
        assertEquals(0, tolerance.check(0.06));
        assertEquals(1, tolerance.check(0.07));
        assertEquals(0, tolerance.check(-0.05));
        assertEquals(0, tolerance.check(-0.06));
        assertEquals(-1, tolerance.check(-0.07));
    }

    @Test
    public void testMatch_double_double() {
        Tolerance tolerance = new Tolerance(0.06);
        assertTrue(tolerance.match(27, 27.05));
        assertTrue(tolerance.match(27, 27.06));
        assertFalse(tolerance.match(27, 27.07));
        assertFalse(tolerance.match(27, 28));
        assertTrue(tolerance.match(27, 26.95));
        assertTrue(tolerance.match(27, 26.94));
        assertFalse(tolerance.match(27, 26.93));
        assertFalse(tolerance.match(27, 26));
    }

    @Test
    public void testMatch_4args() {
        Tolerance tolerance = new Tolerance(0.06);
        assertTrue(tolerance.match(2, 5, 2, 5));
        assertTrue(tolerance.match(2, 5, 2.05, 4.95));
        assertFalse(tolerance.match(2, 5, 2.07, 4.95));
        assertFalse(tolerance.match(2, 5, 2.05, 4.93));
    }

    @Test
    public void testMostPrecise() {
        Tolerance a = new Tolerance(0.01);
        Tolerance b = new Tolerance(0.02);
        assertSame(a, a.mostPrecise(b));
        assertSame(a, b.mostPrecise(a));
    }

    @Test
    public void testLeastPrecise() {
        Tolerance a = new Tolerance(0.01);
        Tolerance b = new Tolerance(0.02);
        assertSame(b, a.leastPrecise(b));
        assertSame(b, b.leastPrecise(a));
    }

    @Test
    public void testHashCode() {
        Tolerance a = new Tolerance(0.01);
        Tolerance b = new Tolerance(0.02);
        Tolerance c = new Tolerance(0.01);
        assertEquals(a.hashCode(), c.hashCode());
        assertFalse(a.hashCode() == b.hashCode());
    }

    @Test
    public void testEquals() {
        Tolerance a = new Tolerance(0.01);
        Tolerance b = new Tolerance(0.02);
        Tolerance c = new Tolerance(0.01);
        assertEquals(a, c);
        assertFalse(a.equals(b));
        assertFalse(a.equals(""));
    }

    @Test
    public void testExternalize() throws Exception {
        Tolerance a = new Tolerance(0.5);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
            out.writeObject(a);
        }
        Tolerance b;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = (Tolerance) in.readObject();
        }
        assertEquals(a, b);
        bout = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
            a.writeData(out);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = Tolerance.read(in);
        }
        assertEquals(a, b);
    }
    
    @Test
    public void testClone(){
        Tolerance a = new Tolerance(0.001);
        Tolerance b = a.clone();
        assertSame(a, b);
    }

}
