package org.jg.algorithm;

import org.jg.geom.Network;
import org.jg.util.Tolerance;
import org.jg.util.VectList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofarrell
 */
public class ConvexHullTest {

    @Test
    public void testGetConvexHull_A() {
        ConvexHull hull = new ConvexHull(Tolerance.DEFAULT);
        VectList a = new VectList(0, 0, 0, 100, 20, 100, 30, 20, 100, 20, 100, 0, 0, 0);
        VectList b = new VectList(0, 0, 100, 0, 100, 20, 20, 100, 0, 100, 0, 0);
        assertEquals(b, hull.getConvexHull(a));
    }

    @Test
    public void testGetConvexHull_B() {
        ConvexHull hull = new ConvexHull(Tolerance.DEFAULT);
        VectList a = new VectList(30, 20, 100, 20, 100, 0, 0, 0, 0, 100, 20, 100, 30, 20);
        VectList b = new VectList(0, 0, 100, 0, 100, 20, 20, 100, 0, 100, 0, 0);
        assertEquals(b, hull.getConvexHull(a));
    }

    @Test
    public void testGetConvexHull_C() {
        ConvexHull hull = new ConvexHull(Tolerance.DEFAULT);
        VectList a = new VectList(0, 0);
        assertEquals(a, hull.getConvexHull(a));
        VectList b = new VectList();
        assertEquals(b, hull.getConvexHull(b));
    }

    @Test
    public void testGetConvexHull_D() {
        VectList a = new VectList(0, 20, 0, 0, 0, 100, 0, 80, 0, 60, 0, 40);
        VectList b = new VectList(0, 0, 0, 100, 0, 0);
        assertEquals(b, new ConvexHull(Tolerance.DEFAULT).getConvexHull(a));
    }

    @Test
    public void testGetConvexHull_E() {
        VectList a = new VectList(0, 0, 100, 0, 80, 0, 60, 0, 40, 0);
        VectList b = new VectList(0, 0, 100, 0, 0, 0);
        assertEquals(b, new ConvexHull(Tolerance.DEFAULT).getConvexHull(a));
    }

    @Test
    public void testGetConvexHull_F() {
        VectList a = new VectList(0, 0, 100, 100, 80, 80, 60, 60, 40, 40);
        VectList b = new VectList(0, 0, 100, 100, 0, 0);
        assertEquals(b, new ConvexHull(Tolerance.DEFAULT).getConvexHull(a));
    }

    @Test
    public void testGetConvexHull_G() {
        ConvexHull hull = new ConvexHull(Tolerance.DEFAULT);
        try {
            hull = new ConvexHull(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            hull.getConvexHull((VectList) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            hull.getConvexHull((Network) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
    }
}
