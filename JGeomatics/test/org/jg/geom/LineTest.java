package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;
import org.jg.util.Network;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar
 */
public class LineTest {

    @Test
    public void testConstructor_4args() {
        Tolerance tolerance = new Tolerance(0.0001);
        Line line = new Line(2, 5, 11, 19);
        assertEquals(line.ax, 2, 0.00001);
        assertEquals(line.ay, 5, 0.00001);
        assertEquals(line.bx, 11, 0.00001);
        assertEquals(line.by, 19, 0.00001);
        assertTrue(line.isValid(tolerance));
        try {
            new Line(Double.NaN, 5, 11, 19);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            new Line(2, Double.POSITIVE_INFINITY, 11, 19);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            new Line(2, 5, Double.NEGATIVE_INFINITY, 19);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            new Line(2, 5, 11, Double.NaN);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
    }

    @Test
    public void testConsstructor_Vect_Vect() {
        Vect a = new Vect(2, 5);
        Vect b = new Vect(11, 19);
        Line line = new Line(a, b);
        assertEquals(line.ax, 2, 0.00001);
        assertEquals(line.ay, 5, 0.00001);
        assertEquals(line.bx, 11, 0.00001);
        assertEquals(line.by, 19, 0.00001);
        assertEquals(a, line.getA());
        assertEquals(b, line.getB());
        try {
            new Line(a, null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            new Line(null, b);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testGetters() {
        Line line = new Line(2, 5, 11, 19);
        assertEquals(line.getAx(), 2, 0.00001);
        assertEquals(line.getAy(), 5, 0.00001);
        assertEquals(line.getBx(), 11, 0.00001);
        assertEquals(line.getBy(), 19, 0.00001);
        assertEquals(new Vect(2, 5), line.getA());
        assertEquals(new Vect(11, 19), line.getB());
        assertEquals(new Vect(6.5, 12), line.getMid());
        assertEquals(14.0 / 9, line.getDydx(), 0.00001);
        assertEquals(277, line.getLengthSq(), 0.00001);
        assertEquals(Math.sqrt(277), line.getLength(), 0.00001);
        assertEquals(Math.atan2(14, 9), line.getDirectionInRadians(), 0.00001);
    }

    @Test
    public void testSign() {
        Line line = new Line(0, 0, 100, 100);
        assertEquals(1, line.sign(new Vect(0, 100), Tolerance.DEFAULT), 0.000001);
        assertEquals(-1, line.sign(new Vect(100, 0), Tolerance.DEFAULT), 0.000001);
        assertEquals(0, line.sign(new Vect(0, 0), Tolerance.DEFAULT), 0.000001);
        assertEquals(0, line.sign(new Vect(50, 50), Tolerance.DEFAULT), 0.000001);
        assertEquals(0, line.sign(new Vect(100, 100), Tolerance.DEFAULT), 0.000001);
        assertEquals(0, line.sign(new Vect(50.0000001, 50), Tolerance.DEFAULT), 0.000001);
        try {
            line.sign(null, Tolerance.DEFAULT);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            line.sign(new Vect(50, 50), null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testProject() {
        Line line = new Line(50, 0, 100, 50);
        VectBuilder vect = new VectBuilder();
        assertTrue(line.project(0.5, Tolerance.DEFAULT, vect));
        assertEquals(new Vect(75, 25), vect);

        assertTrue(line.project(0, Tolerance.DEFAULT, vect));
        assertEquals(line.getA(new VectBuilder()), vect);
        assertTrue(line.project(1, Tolerance.DEFAULT, vect));
        assertEquals(line.getB(new VectBuilder()), vect);
        assertTrue(line.project(-0.0000001, Tolerance.DEFAULT, vect));
        assertEquals(line.getA(new VectBuilder()), vect);
        assertTrue(line.project(0.0000001, Tolerance.DEFAULT, vect));
        assertEquals(line.getA(new VectBuilder()), vect);
        assertTrue(line.project(0.9999999, Tolerance.DEFAULT, vect));
        assertEquals(line.getB(new VectBuilder()), vect);
        assertTrue(line.project(1.0000001, Tolerance.DEFAULT, vect));
        assertEquals(line.getB(new VectBuilder()), vect);
        assertFalse(line.project(2, Tolerance.DEFAULT, vect));
        assertEquals(new Vect(150, 100), vect);
        assertFalse(line.project(-1, Tolerance.DEFAULT, vect));
        assertEquals(new Vect(0, -50), vect);

        try {
            line.project(Double.NaN, Tolerance.DEFAULT, new VectBuilder());
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            line.project(1, null, new VectBuilder());
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            line.project(1, Tolerance.DEFAULT, null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testProjectClosest() {
        Line line = new Line(50, 0, 100, 50);
        VectBuilder vect = new VectBuilder();
        line.projectClosest(0.5, Tolerance.DEFAULT, vect);
        assertEquals(new Vect(75, 25), vect);
        line.projectClosest(0, Tolerance.DEFAULT, vect);
        assertEquals(new Vect(50, 0), vect);
        line.projectClosest(1, Tolerance.DEFAULT, vect);
        assertEquals(new Vect(100, 50), vect);
        line.projectClosest(-0.0000001, Tolerance.DEFAULT, vect);
        assertEquals(new Vect(50, 0), vect);
        line.projectClosest(0.0000001, Tolerance.DEFAULT, vect);
        assertEquals(new Vect(50, 0), vect);
        line.projectClosest(0.9999999, Tolerance.DEFAULT, vect);
        assertEquals(new Vect(100, 50), vect);
        line.projectClosest(1.01, new Tolerance(0.01), vect);
        assertEquals(new Vect(100, 50), vect);
        line.projectClosest(2, Tolerance.DEFAULT, vect);
        assertEquals(new Vect(100, 50), vect);
        line.projectClosest(-1, Tolerance.DEFAULT, vect);
        assertEquals(new Vect(50, 0), vect);
        try {
            line.projectClosest(Double.NaN, Tolerance.DEFAULT, vect);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            line.projectClosest(0.5, null, vect);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            line.projectClosest(0.5, Tolerance.DEFAULT, null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testDistLineVect() {
        Line line = new Line(50, 0, 100, 50);
        assertEquals(Math.sqrt(1250), line.distLineVect(new Vect(50, 50)), 0.00001);
        assertEquals(1250, line.distLineVectSq(new Vect(50, 50)), 0.00001);
        line = new Line(2, 5, 6, 8);
        assertEquals(5, line.distLineVect(new Vect(9, 4)), 0.00001);
        assertEquals(25, line.distLineVectSq(new Vect(9, 4)), 0.00001);
        assertEquals(5, line.distLineVect(new Vect(13, 7)), 0.00001);
        assertEquals(25, line.distLineVectSq(new Vect(13, 7)), 0.00001);
    }

    @Test
    public void testDistSegVect() {
        Line line = new Line(50, 0, 100, 50);
        assertEquals(Math.sqrt(1250), line.distSegVect(new Vect(50, 50)), 0.00001);
        assertEquals(1250, line.distSegVectSq(new Vect(50, 50)), 0.00001);
        line = new Line(2, 5, 6, 8);
        assertEquals(5, line.distSegVect(new Vect(9, 4)), 0.00001);
        assertEquals(25, line.distSegVectSq(new Vect(9, 4)), 0.00001);
        assertEquals(5, line.distSegVect(new Vect(10, 11)), 0.00001);
        assertEquals(25, line.distSegVectSq(new Vect(10, 11)), 0.00001);
        assertEquals(5, line.distSegVect(new Vect(-2, 2)), 0.00001);
        assertEquals(25, line.distSegVectSq(new Vect(-2, 2)), 0.00001);
    }

    @Test
    public void testIsParallel() {
        Line line = new Line(2, 5, 6, 8);
        assertTrue(line.isParallel(line, Tolerance.DEFAULT));
        assertTrue(line.isParallel(new Line(6, 8, 2, 5), Tolerance.DEFAULT));
        assertTrue(line.isParallel(new Line(-2, 2, 10, 11), Tolerance.DEFAULT));
        assertFalse(line.isParallel(new Line(2, 8, 6, 5), Tolerance.DEFAULT));
        try {
            line.isParallel(null, Tolerance.DEFAULT);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            line.isParallel(new Line(2, 8, 6, 5), null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testIntersectsSeg() {
        Line line = new Line(2, 5, 6, 8);
        assertFalse(line.intersectsSeg(line, Tolerance.DEFAULT)); // parallel lines do not intersect
        assertTrue(line.intersectsSeg(new Line(2, 8, 6, 5), Tolerance.DEFAULT));
        assertTrue(line.intersectsSeg(new Line(2, 2, 2, 10), Tolerance.DEFAULT));
        assertTrue(line.intersectsSeg(new Line(2, 6, 8, 6), Tolerance.DEFAULT));
        assertTrue(line.intersectsSeg(new Line(1, 5, 4, 5), Tolerance.DEFAULT));
        assertFalse(line.intersectsSeg(new Line(1, 4.99999, 4, 4.99999), Tolerance.DEFAULT));
        assertTrue(line.intersectsSeg(new Line(1, 4.999999, 4, 4.999999), Tolerance.DEFAULT));
        assertTrue(line.intersectsSeg(new Line(1, 5.00001, 4, 5.00001), Tolerance.DEFAULT));
        assertTrue(line.intersectsSeg(new Line(2, 4, 2, 6), Tolerance.DEFAULT));
        assertTrue(line.intersectsSeg(new Line(6, 6, 6, 10), Tolerance.DEFAULT));
        assertTrue(line.intersectsSeg(new Line(6.0000001, 6, 6.0000001, 10), Tolerance.DEFAULT));
        line = new Line(2, 2, 10, 10);
        assertTrue(line.intersectsSeg(new Line(4, 0, 0, 4), Tolerance.DEFAULT));
        assertTrue(line.intersectsSeg(new Line(3.999999, 0, 0, 3.999999), Tolerance.DEFAULT));
        assertTrue(line.intersectsSeg(new Line(8, 12, 12, 8), Tolerance.DEFAULT));
        assertTrue(line.intersectsSeg(new Line(8.0000001, 12, 12, 8.0000001), Tolerance.DEFAULT));

        assertFalse(new Line(0, 0, 4, 4).intersectsSeg(new Line(0, 9, 9, 0), Tolerance.DEFAULT));
        assertFalse(new Line(8, 8, 12, 12).intersectsSeg(new Line(0, 9, 9, 0), Tolerance.DEFAULT));
        assertFalse(new Line(0, 9, 9, 0).intersectsSeg(new Line(0, 0, 4, 4), Tolerance.DEFAULT));
        assertFalse(new Line(0, 9, 9, 0).intersectsSeg(new Line(8, 8, 12, 12), Tolerance.DEFAULT));

        assertFalse(new Line(10, 5, 5, 5).intersectsSeg(new Line(0, 10, 0, 0), Tolerance.DEFAULT));
        assertFalse(new Line(10, 5, 5, 5).intersectsSeg(new Line(0, 4, 0, 0), Tolerance.DEFAULT));
        assertTrue(new Line(0, 0, 10, 0).intersectsSeg(new Line(0.0000001, -1, 0.0000001, 1), Tolerance.DEFAULT));
        assertTrue(new Line(0, 0, 10, 10).intersectsSeg(new Line(0, 2, 2, 2), Tolerance.DEFAULT));
        assertFalse(new Line(0, 0, 2, 2).intersectsSeg(new Line(0, 5, 2, 3), Tolerance.DEFAULT));
        assertFalse(new Line(0, 2, 2, 0).intersectsSeg(new Line(0, 3, 2, 5), Tolerance.DEFAULT));

        assertTrue(new Line(0, 0, 10, 0).intersectsSeg(new Line(1, 0.0000001, 1, 10), Tolerance.DEFAULT));
        assertTrue(new Line(0, 0, 10, 0).intersectsSeg(new Line(1, -10, 1, -0.0000001), Tolerance.DEFAULT));

        try {
            line.intersectsSeg(null, Tolerance.DEFAULT);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            line.intersectsSeg(new Line(28, 12, 32, 8), null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testIntersectionLine() {
        Line line = new Line(2, 5, 6, 8);
        VectBuilder vect = new VectBuilder();
        assertFalse(line.intersectionLine(line, Tolerance.DEFAULT, vect)); // parallel lines do not intersect

        assertTrue(line.intersectionLine(new Line(2, 8, 6, 5), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(4, 6.5), vect);

        assertTrue(line.intersectionLine(new Line(2, 2, 2, 10), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(2, 5), vect);

        assertTrue(line.intersectionLine(new Line(2, 8, 8, 8), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(6, 8), vect);

        assertTrue(line.intersectionLine(new Line(1, 5, 4, 5), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(2, 5), vect);

        assertTrue(line.intersectionLine(new Line(1, 4.999999, 4, 4.999999), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(2, 5), vect);

        assertTrue(line.intersectionLine(new Line(1, 5.000001, 4, 5.000001), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(2, 5), vect);

        assertTrue(line.intersectionLine(new Line(2, 4, 2, 6), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(2, 5), vect);

        assertTrue(line.intersectionLine(new Line(6, 6, 6, 10), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(6, 8), vect);

        assertTrue(line.intersectionLine(new Line(6.0000001, 6, 6.0000001, 10), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(6, 8), vect);

        line = Line.valueOf(2, 2, 10, 10);

        assertTrue(line.intersectionLine(new Line(4, 0, 0, 4), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(2, 2), vect);

        assertTrue(line.intersectionLine(new Line(3.999999, 0, 0, 3.999999), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(2, 2), vect);

        assertTrue(line.intersectionLine(new Line(8, 12, 12, 8), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(10, 10), vect);

        assertTrue(line.intersectionLine(new Line(8.0000001, 12, 12, 8.0000001), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(10, 10), vect);

        assertTrue(Line.valueOf(0, 0, 4, 4).intersectionLine(new Line(0, 9, 9, 0), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(4.5, 4.5), vect);

        assertTrue(Line.valueOf(8, 8, 12, 12).intersectionLine(new Line(0, 9, 9, 0), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(4.5, 4.5), vect);

        assertTrue(Line.valueOf(0, 9, 9, 0).intersectionLine(new Line(0, 0, 4, 4), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(4.5, 4.5), vect);

        assertTrue(Line.valueOf(0, 9, 9, 0).intersectionLine(new Line(8, 8, 12, 12), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(4.5, 4.5), vect);

        assertTrue(new Line(10, 0, 0, 0).intersectionLine(new Line(0.0000001, -1, 0.0000001, 1), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(0, 0), vect);
        assertTrue(new Line(0, 0, 10, 10).intersectionLine(new Line(0, 2, 2, 2), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(2, 2), vect);

        try {
            line.intersectionLine(null, Tolerance.DEFAULT, vect);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            line.intersectionLine(new Line(0, 0, 4, 4), null, vect);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            line.intersectionLine(new Line(0, 0, 4, 4), Tolerance.DEFAULT, null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testIntersectionSeg() {
        Line line = new Line(2, 5, 6, 8);
        VectBuilder vect = new VectBuilder();
        assertFalse(line.intersectionSeg(line, Tolerance.DEFAULT, vect)); // parallel lines do not intersect
        assertTrue(line.intersectionSeg(new Line(2, 8, 6, 5), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(4, 6.5), vect);
        assertTrue(line.intersectionSeg(new Line(2, 2, 2, 10), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(2, 5), vect);
        assertTrue(line.intersectionSeg(new Line(2, 8, 8, 8), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(6, 8), vect);
        assertTrue(line.intersectionSeg(new Line(1, 5, 4, 5), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(2, 5), vect);
        assertTrue(line.intersectionSeg(new Line(1, 4.999999, 4, 4.999999), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(2, 5), vect);
        assertTrue(line.intersectionSeg(new Line(1, 5.000001, 4, 5.000001), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(2, 5), vect);
        assertTrue(line.intersectionSeg(new Line(2, 4, 2, 6), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(2, 5), vect);
        assertTrue(line.intersectionSeg(new Line(6, 6, 6, 10), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(6, 8), vect);
        assertTrue(line.intersectionSeg(new Line(6.0000001, 6, 6.0000001, 10), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(6, 8), vect);
        line = new Line(2, 2, 10, 10);
        assertTrue(line.intersectionSeg(new Line(4, 0, 0, 4), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(2, 2), vect);
        assertTrue(line.intersectionSeg(new Line(3.999999, 0, 0, 3.999999), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(2, 2), vect);
        assertTrue(line.intersectionSeg(new Line(8, 12, 12, 8), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(10, 10), vect);
        assertTrue(line.intersectionSeg(new Line(8.0000001, 12, 12, 8.0000001), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(10, 10), vect);

        assertFalse(new Line(0, 0, 4, 4).intersectionSeg(new Line(0, 9, 9, 0), Tolerance.DEFAULT, vect));
        assertFalse(new Line(8, 8, 12, 12).intersectionSeg(new Line(0, 9, 9, 0), Tolerance.DEFAULT, vect));
        assertFalse(new Line(0, 9, 9, 0).intersectionSeg(new Line(0, 0, 4, 4), Tolerance.DEFAULT, vect));
        assertFalse(new Line(0, 9, 9, 0).intersectionSeg(new Line(8, 8, 12, 12), Tolerance.DEFAULT, vect));

        assertFalse(new Line(0, 0, 4, 4).intersectionSeg(new Line(0, 9, 9, 0), Tolerance.DEFAULT, vect));
        assertFalse(new Line(8, 8, 12, 12).intersectionSeg(new Line(0, 9, 9, 0), Tolerance.DEFAULT, vect));
        assertFalse(new Line(0, 9, 9, 0).intersectionSeg(new Line(0, 0, 4, 4), Tolerance.DEFAULT, vect));
        assertFalse(new Line(0, 9, 9, 0).intersectionSeg(new Line(8, 8, 12, 12), Tolerance.DEFAULT, vect));

        assertFalse(new Line(10, 5, 5, 5).intersectionSeg(new Line(0, 10, 0, 0), Tolerance.DEFAULT, vect));
        assertFalse(new Line(10, 5, 5, 5).intersectionSeg(new Line(0, 4, 0, 0), Tolerance.DEFAULT, vect));
        assertTrue(new Line(0, 0, 10, 0).intersectionSeg(new Line(0.0000001, -1, 0.0000001, 1), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(0, 0), vect);
        assertTrue(new Line(0, 0, 10, 10).intersectionSeg(new Line(0, 2, 2, 2), Tolerance.DEFAULT, vect));
        assertEquals(new Vect(2, 2), vect);
        assertFalse(new Line(0, 0, 2, 2).intersectionSeg(new Line(0, 5, 2, 3), Tolerance.DEFAULT, vect));
        assertFalse(new Line(0, 2, 2, 0).intersectionSeg(new Line(0, 3, 2, 5), Tolerance.DEFAULT, vect));

        try {
            line.intersectionSeg(null, Tolerance.DEFAULT, vect);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            line.intersectionSeg(new Line(8, 12, 12, 8), null, vect);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            line.intersectionSeg(new Line(8, 12, 12, 8), Tolerance.DEFAULT, null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testHashCode() {
        assertEquals(new Line(1, 2, 3, 4).hashCode(), new Line(1, 2, 3, 4).hashCode()); // equal should have same hashcode
        Set<Integer> hashes = new HashSet<>();
        for (int i = 1; i < 100; i++) { // minor test - no collisions in 100 elements
            int hash = new Line(0, 0, i, i).hashCode();
            assertFalse(hashes.contains(hash));
            hashes.add(hash);
        }
    }

    @Test
    public void testEquals() {
        Line line = new Line(1, 2, 3, 4);
        assertEquals(line, line);
        assertEquals(line, new Line(1, 2, 3, 4));
        assertFalse(line.equals(new Line(1, 3, 2, 4)));
        assertFalse(line.equals(new Line(1, 3, 2, 4)));
        assertFalse(line.equals(new Line(3, 4, 1, 2)));
        assertFalse(line.equals(new Line(3, 4, 1, 2)));
        assertFalse(line.equals(null)); // equals null should not throw an NPE
        assertFalse(line.equals(new Line(1, 2, 3, 5)));
        assertFalse(line.equals(new Line(1, 2, 5, 4)));
        assertFalse(line.equals(new Line(1, 5, 3, 4)));
        assertFalse(line.equals(new Line(5, 2, 3, 4)));
    }

    @Test
    public void testToString() throws IOException {
        assertEquals("[1,2,3,4]", new Line(1, 2, 3, 4).toString());
        assertEquals("[4,3,2,1]", new Line(4, 3, 2, 1).toString());
        assertEquals("[1.2,3.4,5.6,7.8]", new Line(1.2, 3.4, 5.6, 7.8).toString());
        StringBuilder str = new StringBuilder();
        new Line(1, 2, 3, 4).toString(str);
        assertEquals("[1,2,3,4]", str.toString());
        str.setLength(0);
        new Line(1.1, 2.2, 3.3, 4.4).toString(str);
        assertEquals("[1.1,2.2,3.3,4.4]", str.toString());
    }

    @Test
    public void testClone() {
        Line a = new Line(1, 2, 3, 4);
        assertNotSame(a, a.clone());
        assertEquals(a, a.clone());
    }

    @Test
    public void testNormalize() {
        Line a = new Line(1, 2, 3, 4);
        assertEquals(new Line(1, 2, 3, 4), a.normalize());
        assertSame(a, Line.valueOf(3, 2, 1, 1).normalize());
        assertEquals(new Line(1, 1, 3, 2), a);
    }

    @Test
    public void testIsValid() {
        Line a = new Line(1, 2, 3, 4);
        assertTrue(a.isValid(Tolerance.DEFAULT));
        assertTrue(Line.valueOf(1, 2, 1, 4).isValid(Tolerance.DEFAULT));
        assertTrue(Line.valueOf(1, 2, 3, 2).isValid(Tolerance.DEFAULT));
        assertFalse(Line.valueOf(1, 2, 1, 2).isValid(Tolerance.DEFAULT));
        assertFalse(Line.valueOf(1, 2, 1.01, 2).isValid(new Tolerance(0.1)));
        assertFalse(Line.valueOf(1, 2, 1, 2.01).isValid(new Tolerance(0.1)));
    }

    @Test
    public void testExternalize() throws Exception {
        Line a = new Line(3, 7, 13, 23);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
            out.writeObject(a);
        }
        Line b;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = (Line) in.readObject();
        }
        assertEquals(a, b);
        bout = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
            a.write(out);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = Line.read(in);
        }
        assertEquals(a, b);
    }

    @Test
    public void testGetBounds() {
        Line a = new Line(13, 23, 3, 7);
        assertEquals(new Rect(3, 7, 13, 23), a.getBounds());
    }

    @Test
    public void testCompareTo() {
        Line a = new Line(1, 2, 3, 4);
        Line b = new Line(1, 2, 3, 4);
        assertEquals(0, a.compareTo(b));
        assertEquals(1, a.compareTo(b = Line.valueOf(1, 2, 3, 0)));
        assertEquals(-1, b.compareTo(a));
        assertEquals(1, a.compareTo(b = Line.valueOf(1, 2, 0, 4)));
        assertEquals(-1, b.compareTo(a));
        assertEquals(1, a.compareTo(b = Line.valueOf(1, 0, 3, 4)));
        assertEquals(-1, b.compareTo(a));
        assertEquals(1, a.compareTo(b = Line.valueOf(0, 2, 3, 4)));
        assertEquals(-1, b.compareTo(a));
    }

    /**
     * Test of valueOf method, of class Line.
     */
    @Test
    public void testValueOf() {
        System.out.println("valueOf");
        double ax = 0.0;
        double ay = 0.0;
        double bx = 0.0;
        double by = 0.0;
        Line expResult = null;
        Line result = Line.valueOf(ax, ay, bx, by);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAx method, of class Line.
     */
    @Test
    public void testGetAx() {
        System.out.println("getAx");
        Line instance = null;
        double expResult = 0.0;
        double result = instance.getAx();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAy method, of class Line.
     */
    @Test
    public void testGetAy() {
        System.out.println("getAy");
        Line instance = null;
        double expResult = 0.0;
        double result = instance.getAy();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBx method, of class Line.
     */
    @Test
    public void testGetBx() {
        System.out.println("getBx");
        Line instance = null;
        double expResult = 0.0;
        double result = instance.getBx();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBy method, of class Line.
     */
    @Test
    public void testGetBy() {
        System.out.println("getBy");
        Line instance = null;
        double expResult = 0.0;
        double result = instance.getBy();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getA method, of class Line.
     */
    @Test
    public void testGetA_0args() {
        System.out.println("getA");
        Line instance = null;
        Vect expResult = null;
        Vect result = instance.getA();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getB method, of class Line.
     */
    @Test
    public void testGetB_0args() {
        System.out.println("getB");
        Line instance = null;
        Vect expResult = null;
        Vect result = instance.getB();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getA method, of class Line.
     */
    @Test
    public void testGetA_VectBuilder() {
        System.out.println("getA");
        VectBuilder target = null;
        Line instance = null;
        VectBuilder expResult = null;
        VectBuilder result = instance.getA(target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getB method, of class Line.
     */
    @Test
    public void testGetB_VectBuilder() {
        System.out.println("getB");
        VectBuilder target = null;
        Line instance = null;
        VectBuilder expResult = null;
        VectBuilder result = instance.getB(target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getMid method, of class Line.
     */
    @Test
    public void testGetMid() {
        System.out.println("getMid");
        Line instance = null;
        Vect expResult = null;
        Vect result = instance.getMid();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLength method, of class Line.
     */
    @Test
    public void testGetLength() {
        System.out.println("getLength");
        Line instance = null;
        double expResult = 0.0;
        double result = instance.getLength();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLengthSq method, of class Line.
     */
    @Test
    public void testGetLengthSq() {
        System.out.println("getLengthSq");
        Line instance = null;
        double expResult = 0.0;
        double result = instance.getLengthSq();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDydx method, of class Line.
     */
    @Test
    public void testGetDydx() {
        System.out.println("getDydx");
        Line instance = null;
        double expResult = 0.0;
        double result = instance.getDydx();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetDirectionInRadians() {
        assertEquals(0, Line.valueOf(0, 0, 10, 0).getDirectionInRadians(), 0.00001);
        assertEquals(Math.PI, Line.valueOf(0, 0, -10, 0).getDirectionInRadians(), 0.00001);
        assertEquals(Math.PI/2, Line.valueOf(0, 0, 0, 10).getDirectionInRadians(), 0.00001);
        assertEquals(Math.PI*3/2, Line.valueOf(0, 0, 0, -10).getDirectionInRadians(), 0.00001);
        assertEquals(Math.PI/4, Line.valueOf(0, 0, 10, 10).getDirectionInRadians(), 0.00001);
    }

    /**
     * Test of sign method, of class Line.
     */
    @Test
    public void testSign_Vect_Tolerance() {
        System.out.println("sign");
        Vect vect = null;
        Tolerance tolerance = null;
        Line instance = null;
        int expResult = 0;
        int result = instance.sign(vect, tolerance);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sign method, of class Line.
     */
    @Test
    public void testSign_Vect() {
        System.out.println("sign");
        Vect vect = null;
        Line instance = null;
        double expResult = 0.0;
        double result = instance.sign(vect);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of distLineVectSq method, of class Line.
     */
    @Test
    public void testDistLineVectSq() {
        System.out.println("distLineVectSq");
        Vect vect = null;
        Line instance = null;
        double expResult = 0.0;
        double result = instance.distLineVectSq(vect);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pntLineDistSq method, of class Line.
     */
    @Test
    public void testPntLineDistSq() {
        System.out.println("pntLineDistSq");
        double ax = 0.0;
        double ay = 0.0;
        double bx = 0.0;
        double by = 0.0;
        double x = 0.0;
        double y = 0.0;
        double expResult = 0.0;
        double result = Line.pntLineDistSq(ax, ay, bx, by, x, y);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of distSegVectSq method, of class Line.
     */
    @Test
    public void testDistSegVectSq() {
        System.out.println("distSegVectSq");
        Vect vect = null;
        Line instance = null;
        double expResult = 0.0;
        double result = instance.distSegVectSq(vect);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pntSegDistSq method, of class Line.
     */
    @Test
    public void testPntSegDistSq() {
        System.out.println("pntSegDistSq");
        double ax = 0.0;
        double ay = 0.0;
        double bx = 0.0;
        double by = 0.0;
        double x = 0.0;
        double y = 0.0;
        double expResult = 0.0;
        double result = Line.pntSegDistSq(ax, ay, bx, by, x, y);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDenom method, of class Line.
     */
    @Test
    public void testGetDenom() {
        System.out.println("getDenom");
        double iax = 0.0;
        double iay = 0.0;
        double ibx = 0.0;
        double iby = 0.0;
        double jax = 0.0;
        double jay = 0.0;
        double jbx = 0.0;
        double jby = 0.0;
        double expResult = 0.0;
        double result = Line.getDenom(iax, iay, ibx, iby, jax, jay, jbx, jby);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of intersectionLineInternal method, of class Line.
     */
    @Test
    public void testIntersectionLineInternal() {
        System.out.println("intersectionLineInternal");
        double ax = 0.0;
        double ay = 0.0;
        double bx = 0.0;
        double by = 0.0;
        double jax = 0.0;
        double jay = 0.0;
        double jbx = 0.0;
        double jby = 0.0;
        Tolerance tolerance = null;
        VectBuilder target = null;
        boolean expResult = false;
        boolean result = Line.intersectionLineInternal(ax, ay, bx, by, jax, jay, jbx, jby, tolerance, target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of intersectionSegInternal method, of class Line.
     */
    @Test
    public void testIntersectionSegInternal() {
        System.out.println("intersectionSegInternal");
        double ax = 0.0;
        double ay = 0.0;
        double bx = 0.0;
        double by = 0.0;
        double jax = 0.0;
        double jay = 0.0;
        double jbx = 0.0;
        double jby = 0.0;
        Tolerance tolerance = null;
        VectBuilder target = null;
        boolean expResult = false;
        boolean result = Line.intersectionSegInternal(ax, ay, bx, by, jax, jay, jbx, jby, tolerance, target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addBoundsTo method, of class Line.
     */
    @Test
    public void testAddBoundsTo() {
        System.out.println("addBoundsTo");
        RectBuilder target = null;
        Line instance = null;
        instance.addBoundsTo(target);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of transform method, of class Line.
     */
    @Test
    public void testTransform() {
        System.out.println("transform");
        Transform transform = null;
        Line instance = null;
        Line expResult = null;
        Line result = instance.transform(transform);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pathIterator method, of class Line.
     */
    @Test
    public void testPathIterator() {
        System.out.println("pathIterator");
        Line instance = null;
        PathIterator expResult = null;
        PathIterator result = instance.pathIterator();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addTo method, of class Line.
     */
    @Test
    public void testAddTo() {
        System.out.println("addTo");
        Network network = null;
        double flatness = 0.0;
        Line instance = null;
        instance.addTo(network, flatness);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of compare method, of class Line.
     */
    @Test
    public void testCompare() {
        System.out.println("compare");
        double iax = 0.0;
        double iay = 0.0;
        double ibx = 0.0;
        double iby = 0.0;
        double jax = 0.0;
        double jay = 0.0;
        double jbx = 0.0;
        double jby = 0.0;
        int expResult = 0;
        int result = Line.compare(iax, iay, ibx, iby, jax, jay, jbx, jby);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class Line.
     */
    @Test
    public void testToString_0args() {
        System.out.println("toString");
        Line instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class Line.
     */
    @Test
    public void testToString_Appendable() {
        System.out.println("toString");
        Appendable appendable = null;
        Line instance = null;
        instance.toString(appendable);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
