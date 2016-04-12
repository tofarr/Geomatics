package org.jg.geom;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.TransformBuilder;
import org.jg.util.VectList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar
 */
public class LineTest {

    @Test
    public void testValueOf() {
        Tolerance tolerance = new Tolerance(0.0001);
        Line line = Line.valueOf(2, 5, 11, 19);
        assertEquals(line.ax, 2, 0.00001);
        assertEquals(line.ay, 5, 0.00001);
        assertEquals(line.bx, 11, 0.00001);
        assertEquals(line.by, 19, 0.00001);
        assertTrue(line.isValid(tolerance));
        try {
            Line.valueOf(Double.NaN, 5, 11, 19);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            Line.valueOf(2, Double.POSITIVE_INFINITY, 11, 19);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            Line.valueOf(2, 5, Double.NEGATIVE_INFINITY, 19);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            Line.valueOf(2, 5, 11, Double.NaN);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            Line.valueOf(2, 5, 2, 5);
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
        assertEquals(new VectBuilder(6.5, 12), line.getMid(new VectBuilder()));
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
        assertEquals(new VectBuilder(75, 25), vect);

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
        assertEquals(new VectBuilder(150, 100), vect);
        assertFalse(line.project(-1, Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(0, -50), vect);

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
    public void testProjectOutward() {
        Line line = new Line(50, 0, 100, 50);
        VectBuilder vect = new VectBuilder();
        assertTrue(line.projectOutward(0.5, Math.sqrt(18), Tolerance.DEFAULT, vect));
        assertTrue(vect.build().match(Vect.valueOf(78, 22), Tolerance.DEFAULT));
        assertTrue(line.projectOutward(0, Math.sqrt(18), Tolerance.DEFAULT, vect));
        assertTrue(vect.build().match(Vect.valueOf(53, -3), Tolerance.DEFAULT));
        assertTrue(line.projectOutward(1, Math.sqrt(18), Tolerance.DEFAULT, vect));
        assertTrue(vect.build().match(Vect.valueOf(103, 47), Tolerance.DEFAULT));
        assertTrue(line.projectOutward(-0.0000001, Math.sqrt(18), Tolerance.DEFAULT, vect));
        assertTrue(vect.build().match(Vect.valueOf(53, -3), Tolerance.DEFAULT));
        
        assertTrue(line.projectOutward(0.5, -Math.sqrt(18), Tolerance.DEFAULT, vect));
        assertTrue(vect.build().match(Vect.valueOf(72, 28), Tolerance.DEFAULT));
        assertTrue(line.projectOutward(0, -Math.sqrt(18), Tolerance.DEFAULT, vect));
        assertTrue(vect.build().match(Vect.valueOf(47, 3), Tolerance.DEFAULT));
        assertTrue(line.projectOutward(1, -Math.sqrt(18), Tolerance.DEFAULT, vect));
        assertTrue(vect.build().match(Vect.valueOf(97, 53), Tolerance.DEFAULT));
        assertTrue(line.projectOutward(-0.0000001, -Math.sqrt(18), Tolerance.DEFAULT, vect));
        assertTrue(vect.build().match(Vect.valueOf(47, 3), Tolerance.DEFAULT));
        
        
        try {
            line.projectOutward(Double.NaN, Math.sqrt(18), Tolerance.DEFAULT, new VectBuilder());
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            line.projectOutward(1, Double.POSITIVE_INFINITY, Tolerance.DEFAULT, new VectBuilder());
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            line.projectOutward(1, Math.sqrt(18), null, new VectBuilder());
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            line.projectOutward(1, Math.sqrt(18), Tolerance.DEFAULT, null);
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
        assertEquals(new VectBuilder(75, 25), vect);
        line.projectClosest(0, Tolerance.DEFAULT, vect);
        assertEquals(new VectBuilder(50, 0), vect);
        line.projectClosest(1, Tolerance.DEFAULT, vect);
        assertEquals(new VectBuilder(100, 50), vect);
        line.projectClosest(-0.0000001, Tolerance.DEFAULT, vect);
        assertEquals(new VectBuilder(50, 0), vect);
        line.projectClosest(0.0000001, Tolerance.DEFAULT, vect);
        assertEquals(new VectBuilder(50, 0), vect);
        line.projectClosest(0.9999999, Tolerance.DEFAULT, vect);
        assertEquals(new VectBuilder(100, 50), vect);
        line.projectClosest(1.01, new Tolerance(0.01), vect);
        assertEquals(new VectBuilder(100, 50), vect);
        line.projectClosest(2, Tolerance.DEFAULT, vect);
        assertEquals(new VectBuilder(100, 50), vect);
        line.projectClosest(-1, Tolerance.DEFAULT, vect);
        assertEquals(new VectBuilder(50, 0), vect);
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
        assertEquals(new VectBuilder(4, 6.5), vect);

        assertTrue(line.intersectionLine(new Line(2, 2, 2, 10), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(2, 5), vect);

        assertTrue(line.intersectionLine(new Line(2, 8, 8, 8), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(6, 8), vect);

        assertTrue(line.intersectionLine(new Line(1, 5, 4, 5), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(2, 5), vect);

        assertTrue(line.intersectionLine(new Line(1, 4.999999, 4, 4.999999), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(2, 5), vect);

        assertTrue(line.intersectionLine(new Line(1, 5.000001, 4, 5.000001), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(2, 5), vect);

        assertTrue(line.intersectionLine(new Line(2, 4, 2, 6), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(2, 5), vect);

        assertTrue(line.intersectionLine(new Line(6, 6, 6, 10), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(6, 8), vect);

        assertTrue(line.intersectionLine(new Line(6.0000001, 6, 6.0000001, 10), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(6, 8), vect);

        line = Line.valueOf(2, 2, 10, 10);

        assertTrue(line.intersectionLine(new Line(4, 0, 0, 4), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(2, 2), vect);

        assertTrue(line.intersectionLine(new Line(3.999999, 0, 0, 3.999999), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(2, 2), vect);

        assertTrue(line.intersectionLine(new Line(8, 12, 12, 8), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(10, 10), vect);

        assertTrue(line.intersectionLine(new Line(8.0000001, 12, 12, 8.0000001), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(10, 10), vect);

        assertTrue(Line.valueOf(0, 0, 4, 4).intersectionLine(new Line(0, 9, 9, 0), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(4.5, 4.5), vect);

        assertTrue(Line.valueOf(8, 8, 12, 12).intersectionLine(new Line(0, 9, 9, 0), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(4.5, 4.5), vect);

        assertTrue(Line.valueOf(0, 9, 9, 0).intersectionLine(new Line(0, 0, 4, 4), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(4.5, 4.5), vect);

        assertTrue(Line.valueOf(0, 9, 9, 0).intersectionLine(new Line(8, 8, 12, 12), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(4.5, 4.5), vect);

        assertTrue(new Line(10, 0, 0, 0).intersectionLine(new Line(0.0000001, -1, 0.0000001, 1), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(0, 0), vect);
        assertTrue(new Line(0, 0, 10, 10).intersectionLine(new Line(0, 2, 2, 2), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(2, 2), vect);

        try {
            line.intersectionLine(null, Tolerance.DEFAULT, vect);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            line.intersectionLine(new Line(0, 4, 4, 0), null, vect);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            line.intersectionLine(new Line(0, 4, 4, 0), Tolerance.DEFAULT, null);
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
        assertEquals(new VectBuilder(4, 6.5), vect);
        assertTrue(line.intersectionSeg(new Line(2, 2, 2, 10), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(2, 5), vect);
        assertTrue(line.intersectionSeg(new Line(2, 8, 8, 8), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(6, 8), vect);
        assertTrue(line.intersectionSeg(new Line(1, 5, 4, 5), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(2, 5), vect);
        assertTrue(line.intersectionSeg(new Line(1, 4.999999, 4, 4.999999), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(2, 5), vect);
        assertTrue(line.intersectionSeg(new Line(1, 5.000001, 4, 5.000001), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(2, 5), vect);
        assertTrue(line.intersectionSeg(new Line(2, 4, 2, 6), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(2, 5), vect);
        assertTrue(line.intersectionSeg(new Line(6, 6, 6, 10), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(6, 8), vect);
        assertTrue(line.intersectionSeg(new Line(6.0000001, 6, 6.0000001, 10), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(6, 8), vect);
        line = new Line(2, 2, 10, 10);
        assertTrue(line.intersectionSeg(new Line(4, 0, 0, 4), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(2, 2), vect);
        assertTrue(line.intersectionSeg(new Line(3.999999, 0, 0, 3.999999), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(2, 2), vect);
        assertTrue(line.intersectionSeg(new Line(8, 12, 12, 8), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(10, 10), vect);
        assertTrue(line.intersectionSeg(new Line(8.0000001, 12, 12, 8.0000001), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(10, 10), vect);

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
        assertEquals(new VectBuilder(0, 0), vect);
        assertTrue(new Line(0, 0, 10, 10).intersectionSeg(new Line(0, 2, 2, 2), Tolerance.DEFAULT, vect));
        assertEquals(new VectBuilder(2, 2), vect);
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
        assertEquals("[\"LN\",1,2,3,4]", new Line(1, 2, 3, 4).toString());
        assertEquals("[\"LN\",4,3,2,1]", new Line(4, 3, 2, 1).toString());
        assertEquals("[\"LN\",1.2,3.4,5.6,7.8]", new Line(1.2, 3.4, 5.6, 7.8).toString());
        StringBuilder str = new StringBuilder();
        new Line(1, 2, 3, 4).toString(str);
        assertEquals("[\"LN\",1,2,3,4]", str.toString());
        str.setLength(0);
        new Line(1.1, 2.2, 3.3, 4.4).toString(str);
        assertEquals("[\"LN\",1.1,2.2,3.3,4.4]", str.toString());
        try{
            new Line(1, 2, 3, 4).toString(new Appendable(){
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
            fail("Exception Expected");
        }catch(GeomException ex){
        }
        
    }

    @Test
    public void testClone() {
        Line a = new Line(1, 2, 3, 4);
        assertSame(a, a.clone());
    }

    @Test
    public void testNormalize() {
        Line a = new Line(1, 2, 3, 4);
        assertEquals(new Line(1, 2, 3, 4), a.normalize());
        assertEquals(a, Line.valueOf(3, 4, 1, 2).normalize());
    }

    @Test
    public void testIsValid() {
        Line a = new Line(1, 2, 3, 4);
        assertTrue(a.isValid(Tolerance.DEFAULT));
        assertTrue(Line.valueOf(1, 2, 1, 4).isValid(Tolerance.DEFAULT));
        assertTrue(Line.valueOf(1, 2, 3, 2).isValid(Tolerance.DEFAULT));
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
            Line.read(new DataInputStream(new InputStream(){
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

    @Test
    public void testGetDirectionInRadians() {
        assertEquals(0, Line.valueOf(0, 0, 10, 0).getDirectionInRadians(), 0.00001);
        assertEquals(Math.PI, Line.valueOf(0, 0, -10, 0).getDirectionInRadians(), 0.00001);
        assertEquals(Math.PI/2, Line.valueOf(0, 0, 0, 10).getDirectionInRadians(), 0.00001);
        assertEquals(Math.PI*3/2, Line.valueOf(0, 0, 0, -10).getDirectionInRadians(), 0.00001);
        assertEquals(Math.PI/4, Line.valueOf(0, 0, 10, 10).getDirectionInRadians(), 0.00001);
    }
    
    @Test
    public void testDistSegVectSq() {
        Line a = Line.valueOf(5, 7, 11, 15);
        assertEquals(0, a.distSegVectSq(Vect.valueOf(5, 7)), 0.00001);
        assertEquals(25, a.distSegVectSq(Vect.valueOf(1, 4)), 0.00001);
        assertEquals(25, a.distSegVectSq(Vect.valueOf(9, 4)), 0.00001);
        assertEquals(25, a.distSegVectSq(Vect.valueOf(4, 14)), 0.00001);
        assertEquals(25, a.distSegVectSq(Vect.valueOf(12, 8)), 0.00001);
        assertEquals(25, a.distSegVectSq(Vect.valueOf(14, 19)), 0.00001);
    }

    @Test
    public void testTransform() {
        Transform transform = new TransformBuilder().flipYAround(5).translate(2, 0).build();
        Line a = Line.valueOf(2,3,7,11);
        Line b = a.transform(transform);
        assertEquals("[\"LN\",4,7,9,-1]", b.toString());
    }

    @Test
    public void testPathIterator() {
        Line a = Line.valueOf(2, 3, 7, 13);
        Path2D.Double path = new Path2D.Double();
        path.append(a.pathIterator(), true);
        Rectangle2D bounds = path.getBounds2D();
        assertEquals(2, bounds.getMinX(), 0.0001);
        assertEquals(3, bounds.getMinY(), 0.0001);
        assertEquals(7, bounds.getMaxX(), 0.0001);
        assertEquals(13, bounds.getMaxY(), 0.0001);
    }

    @Test
    public void testAddTo() {
        Line a = Line.valueOf(2, 3, 7, 13);
        Network network = new Network();
        a.addTo(network, Tolerance.FLATNESS, Tolerance.DEFAULT);
        assertEquals("[[2,3, 7,13]]", network.toString());
    }
    
    @Test
    public void testBuffer(){
        Line a = Line.valueOf(2, 3, 7, 13);
        assertNull(a.buffer(-1, Tolerance.DEFAULT, Tolerance.DEFAULT));
        assertSame(a, a.buffer(0, Tolerance.DEFAULT, Tolerance.DEFAULT));
        Area b = (Area)a.buffer(3, new Tolerance(0.1), Tolerance.DEFAULT);
        
        Rect bounds = b.getBounds();
        assertEquals(-1, bounds.minX, 0.1);
        assertEquals(0, bounds.minY, 0.1);
        assertEquals(10, bounds.maxX, 0.1);
        assertEquals(16, bounds.maxY, 0.1);
        assertEquals((Math.sqrt(125) * 6) + (Math.PI * 9), b.getArea(), 0.2);
        assertEquals((Math.sqrt(125) * 2) + (Math.PI * 6), b.shell.getLength(), 0.1);
    }
    
    @Test
    public void testCounterClockwise(){
        Line a = Line.valueOf(0, 0, 100, 100);
        assertEquals(1, a.counterClockwise(new Vect(100, 0)));
        assertEquals(-1, a.counterClockwise(new VectBuilder(0, 100)));
        assertEquals(0, a.counterClockwise(Vect.valueOf(50, 50)));
        assertEquals(1, a.counterClockwise(new VectBuilder(150, 150)));
    }
    
    @Test
    public void testRelate_Vect(){
        Line a = Line.valueOf(0, 0, 100, 100);
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, a.relate(Vect.valueOf(50,50), Tolerance.DEFAULT));
        assertEquals(Relation.DISJOINT, a.relate(Vect.valueOf(51,50), Tolerance.DEFAULT));
        assertEquals(Relation.DISJOINT, a.relate(Vect.valueOf(50,49), Tolerance.DEFAULT));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, a.relate(Vect.valueOf(51,50), new Tolerance(1)));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, a.relate(new VectBuilder(50,49), new Tolerance(1)));
        assertEquals(Relation.DISJOINT, a.relate(new VectBuilder(101,101), Tolerance.DEFAULT));
    }
    
    @Test
    public void testRelate_Geom(){
        Line a = Line.valueOf(0, 0, 100, 100);
        Line b = Line.valueOf(0, 100, 100, 0);
        Rect c = Rect.valueOf(0, 0, 100, 100);
        Rect d = Rect.valueOf(200, 0, 300, 100);
        Rect e = Rect.valueOf(40, 40, 60, 60);
        Rect f = Rect.valueOf(-10, -10, 110, 110);
        
        assertEquals(Relation.TOUCH, a.relate(a, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B | Relation.B_OUTSIDE_A, a.relate(b, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(Relation.TOUCH | Relation.A_INSIDE_B | Relation.B_OUTSIDE_A, a.relate(c, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(Relation.DISJOINT, a.relate(d, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(Relation.TOUCH | Relation.A_INSIDE_B | Relation.A_OUTSIDE_B | Relation.B_OUTSIDE_A, a.relate(e, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(Relation.A_INSIDE_B | Relation.B_OUTSIDE_A, a.relate(f, Tolerance.FLATNESS, Tolerance.DEFAULT));
        
    }
    
    @Test
    public void testProjectOnToLine(){
        Line line = Line.valueOf(3, 7, 14, 18);
        VectBuilder target = new VectBuilder();
        line.projectOnToLine(Vect.valueOf(14, 7), Tolerance.DEFAULT, target);
        assertEquals(8.5, target.getX(), 0.00001);
        assertEquals(12.5, target.getY(), 0.00001);
    }
    
    @Test
    public void testIntersectionInternal(){
        Line i = Line.valueOf(0, 25, 55, 25);
        Line j = Line.valueOf(15, 0, 15, 65);
        VectBuilder target = new VectBuilder();
        assertTrue(i.intersectionSeg(j, Tolerance.DEFAULT, target));
        assertTrue(15 == target.getX());
        assertTrue(25 == target.getY());
        assertTrue(i.intersectionLine(j, Tolerance.DEFAULT, target));
        assertTrue(15 == target.getX());
        assertTrue(25 == target.getY());
        
        assertTrue(Line.intersectionLineInternal(0, 0, 0, 10, 0, 5, 10, 5, Tolerance.DEFAULT, target));
        assertEquals(new VectBuilder(0, 5), target);
        assertTrue(Line.intersectionLineInternal(10, 0, 10, 10, 0, 5, 10, 5, Tolerance.DEFAULT, target));
        assertEquals(new VectBuilder(10,5), target);
        
    }
    
    @Test
    public void testIntersectionCircleInternal(){      
                
        checkIntersection(13, 0, 15, 2); // Outside
        checkIntersection(7, 0, 7, 2); // Outside vertical
        checkIntersection(0, 23, 23, 23); // Outside horizontal

        checkIntersection(5.071067811865475244008443621049, 2, 25.071067811865475244008443621049, 22, 16.535533905932738, 13.464466094067262); // Touch
        checkIntersection(18, 0, 18, 2, 18, 17); // Touch vertical
        checkIntersection(10, 12, 16, 12, 13, 12); // Touch horizontal

        checkIntersection(8, 7, 23, 22, 13, 12, 18, 17); // Intersection
        checkIntersection(15, 7, 15, 22, 15, 12.417424305044161, 15, 21.58257569495584); // Intersection vertical
        checkIntersection(10, 15, 15, 15, 8.417424305044161, 15, 17.58257569495584, 15); // Intersection horizontal
        
        checkIntersection(8, 12, 23, 27, 9.464466094067262, 13.464466094067262, 16.535533905932738, 20.535533905932738); // Center
        checkIntersection(13, 0, 13, 1, 13, 12, 13, 22); // Center vertical
        checkIntersection(0, 17, 20, 17, 8, 17, 18, 17); // Center horizontal
        
    }
    
    private void checkIntersection(double ax, double ay, double bx, double by, double...intersections){
        final double cx = 13;
        final double cy = 17;
        final double r = 5;
        
        VectList expected = new VectList(intersections);
        VectList found = new VectList();
        VectBuilder work = new VectBuilder();
        Line.intersectionLineCircleInternal(ax, ay, bx, by, cx, cy, r, Tolerance.DEFAULT, work, found);
        assertEquals(expected, found);
    }

    @Test
    public void testUnion() {
        Line a = new Line(0,0, 100,100);
        Line b = new Line(0,100, 100,0);
        Rect c = new Rect(150,0,200,50);
        assertEquals(a, a.union(a, Tolerance.FLATNESS, Tolerance.DEFAULT));
        LineSet expected = new LineSet(
            new LineString(new VectList(0,0,50,50)),
            new LineString(new VectList(0,100,50,50)),
            new LineString(new VectList(50,50,100,0)),
            new LineString(new VectList(50,50,100,100))
        );
        Geom found = a.union(b, Tolerance.FLATNESS, Tolerance.DEFAULT);
        assertEquals(expected, found);
        LineSet d = new LineSet(
            new LineString(new VectList(0,0,100,100)),
            new LineString(new VectList(150,0,200,50))
        );
        assertEquals(d, a.union(d, Tolerance.FLATNESS, Tolerance.DEFAULT));
    }

    @Test
    public void testIntersection_B() {
        Line a = new Line(0,0, 100,100);
        Line b = new Line(0,100, 100,0);
        Rect c = new Rect(150,0,200,50);
        assertEquals(a, a.intersection(a, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertNull(a.intersection(c, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(Vect.valueOf(50,50), a.intersection(b, Tolerance.FLATNESS, Tolerance.DEFAULT));
    }

    @Test
    public void testLess() {
        Line a = new Line(0,0, 100,100);
        Line b = new Line(0,100, 100,0);
        Rect c = new Rect(150,0,200,50);
        Rect d = new Rect(-1,-1,101,101);
        assertEquals(a, a.less(c, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertNull(a.less(d, Tolerance.FLATNESS, Tolerance.DEFAULT));
        
        Geom geom = a.less(b, Tolerance.FLATNESS, Tolerance.DEFAULT);
        Geom expected = LineSet.valueOf(Tolerance.DEFAULT, 0,0, 50,50, 100,100).simplify();
        assertEquals(expected, a.less(b, Tolerance.FLATNESS, Tolerance.DEFAULT));
    }    
}
