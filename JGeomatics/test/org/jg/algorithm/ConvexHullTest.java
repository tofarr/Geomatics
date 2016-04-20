package org.jg.algorithm;

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
        VectList a = new VectList(0, 0, 0, 100, 20, 100, 30, 20, 100, 20, 100, 0, 0, 0);
        VectList b = new VectList(0, 0, 100, 0, 100, 20, 20, 100, 0, 100, 0, 0);
        assertEquals(b, ConvexHull.getConvexHull(a));
    }

    @Test
    public void testGetConvexHull_B() {
        VectList a = new VectList(30, 20, 100, 20, 100, 0, 0, 0, 0, 100, 20, 100, 30, 20);
        VectList b = new VectList(0, 0, 100, 0, 100, 20, 20, 100, 0, 100, 0, 0);
        assertEquals(b, ConvexHull.getConvexHull(a));
    }

    @Test
    public void testGetConvexHull_C() {
        VectList a = new VectList(0, 0);
        assertEquals(a, ConvexHull.getConvexHull(a));
        VectList b = new VectList();
        assertEquals(b, ConvexHull.getConvexHull(b));
    }

    @Test
    public void testGetConvexHull_D() {
        VectList a = new VectList(0,20, 0, 0, 0, 100, 0, 80, 0, 60, 0, 40);
        VectList b = new VectList(0, 0, 0, 100, 0, 0);
        assertEquals(b, ConvexHull.getConvexHull(a));
    }

    @Test
    public void testGetConvexHull_E() {
        VectList a = new VectList(0, 0, 100, 0, 80, 0, 60, 0, 40, 0);
        VectList b = new VectList(0, 0, 100, 0, 0, 0);
        assertEquals(b, ConvexHull.getConvexHull(a));
    }

    @Test
    public void testGetConvexHull_F() {
        VectList a = new VectList(0, 0, 100, 100, 80, 80, 60, 60, 40, 40);
        VectList b = new VectList(0, 0, 100, 100, 0, 0);
        assertEquals(b, ConvexHull.getConvexHull(a));
    }

    @Test
    public void testGetConvexHull_G() {
        try {
            ConvexHull.getConvexHull(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
    }
}
