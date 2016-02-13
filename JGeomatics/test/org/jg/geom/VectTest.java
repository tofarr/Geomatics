package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.jg.util.Network;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.TransformBuilder;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar_000
 */
public class VectTest {

    @Test
    public void testValueOf() {
        Vect a = Vect.valueOf(1, 2);
        try {
            a = Vect.valueOf(Double.NaN, 4);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        try {
            a = Vect.valueOf(3, Double.POSITIVE_INFINITY);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        assertEquals(1, a.x, 0.00001);
        assertEquals(2, a.y, 0.00001);
        assertEquals(1, a.getX(), 0.00001);
        assertEquals(2, a.getY(), 0.00001);
    }

    @Test
    public void testLength() {
        Vect a = Vect.valueOf(3, 4);
        assertEquals(5, a.length(), 0.00001);
        assertEquals(0, Vect.ZERO.length(), 0.00001);
    }

    @Test
    public void testLengthSq() {
        Vect a = Vect.valueOf(3, 4);
        assertEquals(25, a.lengthSq(), 0.00001);
    }

    @Test
    public void testDist() {
        Vect a = Vect.valueOf(3, 4);
        Vect b = Vect.valueOf(6, 8);
        assertEquals(5, a.dist(b), 0.00001);
        try {
            a.dist(null);
            fail("Exception Expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testDistSq_Vect() {
        Vect a = Vect.valueOf(3, 4);
        Vect b = Vect.valueOf(6, 8);
        assertEquals(25, a.distSq(b), 0.00001);
        try {
            a.distSq(null);
            fail("Exception Expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testDydx() {
        Vect a = Vect.valueOf(4, 3);
        assertEquals(0.75, a.dydx(), 0.00001);
    }

    @Test
    public void testDydxTo_Vect() {
        Vect a = Vect.valueOf(10, 20);
        Vect b = Vect.valueOf(14, 23);
        assertEquals(0.75, a.dydxTo(b), 0.00001);
        try {
            a.dydxTo(null);
            fail("Exception Expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testDirectionInRadians_0args() {
        assertEquals(0, Vect.valueOf(1, 0).directionInRadians(), 0.00001);
        assertEquals(Math.PI / 4, Vect.valueOf(1, 1).directionInRadians(), 0.00001);
        assertEquals(Math.PI / 2, Vect.valueOf(0, 1).directionInRadians(), 0.00001);
        assertEquals(Math.PI * 3 / 4, Vect.valueOf(-1, 1).directionInRadians(), 0.00001);
        assertEquals(Math.PI, Vect.valueOf(-1, 0).directionInRadians(), 0.00001);
        assertEquals(Math.PI * 5 / 4, Vect.valueOf(-1, -1).directionInRadians(), 0.00001);
        assertEquals(Math.PI * 3 / 2, Vect.valueOf(0, -1).directionInRadians(), 0.00001);
        assertEquals(Math.PI * 7 / 4, Vect.valueOf(1, -1).directionInRadians(), 0.00001);

        assertEquals(0, Vect.valueOf(1, 0).directionInRadians(), 0.00001);
        assertEquals(Math.PI / 2, Vect.valueOf(0, 2).directionInRadians(), 0.00001);
        assertEquals(Math.PI * 3 / 2, Vect.valueOf(0, -3).directionInRadians(), 0.00001);
        assertEquals(Math.PI / 4, Vect.valueOf(1, 1).directionInRadians(), 0.00001);
        assertEquals(Math.PI / 6, Vect.valueOf(10 * Math.cos(Math.PI / 6), 10 * Math.sin(Math.PI / 6)).directionInRadians(), 0.00001);
        try {
            Vect.ZERO.directionInRadians();
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
    }

    @Test
    public void testDirectionInRadiansTo_Vect() {
        assertEquals(0, Vect.valueOf(10, 10).directionInRadiansTo(Vect.valueOf(11, 10)), 0.00001);
        assertEquals(Math.PI / 4, Vect.valueOf(10, 10).directionInRadiansTo(Vect.valueOf(11, 11)), 0.00001);
        assertEquals(Math.PI / 2, Vect.valueOf(10, 10).directionInRadiansTo(Vect.valueOf(10, 11)), 0.00001);
        assertEquals(Math.PI * 3 / 4, Vect.valueOf(10, 10).directionInRadiansTo(Vect.valueOf(9, 11)), 0.00001);
        assertEquals(Math.PI, Vect.valueOf(10, 10).directionInRadiansTo(Vect.valueOf(9, 10)), 0.00001);
        assertEquals(Math.PI * 5 / 4, Vect.valueOf(10, 10).directionInRadiansTo(Vect.valueOf(9, 9)), 0.00001);
        assertEquals(Math.PI * 3 / 2, Vect.valueOf(10, 10).directionInRadiansTo(Vect.valueOf(10, 9)), 0.00001);
        assertEquals(Math.PI * 7 / 4, Vect.valueOf(10, 10).directionInRadiansTo(Vect.valueOf(11, 9)), 0.00001);

        assertEquals(0, Vect.valueOf(10, 10).directionInRadiansTo(Vect.valueOf(11, 10)), 0.00001);
        assertEquals(Math.PI / 6, Vect.valueOf(10, 20).directionInRadiansTo(Vect.valueOf(10 + 10 * Math.cos(Math.PI / 6), 20 + 10 * Math.sin(Math.PI / 6))), 0.00001);
        try {
            Vect.valueOf(3, 4).directionInRadiansTo(Vect.valueOf(3, 4));
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //Expected
        }
        try {
            Vect.valueOf(3, 4).directionInRadiansTo(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //Expected
        }
    }

    @Test
    public void testDot_Vect() {
        assertEquals(31, Vect.valueOf(2, 3).dot(Vect.valueOf(5, 7)), 0.00001);
        try {
            Vect.valueOf(3, 4).dot(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //Expected
        }
    }

    @Test
    public void testMatch() {
        Tolerance tolerance = new Tolerance(0.15);
        assertTrue(Vect.valueOf(2, 3).match(Vect.valueOf(2, 3), tolerance));
        assertTrue(Vect.valueOf(2, 3).match(Vect.valueOf(2.1, 3), tolerance));
        assertTrue(Vect.valueOf(2, 3).match(Vect.valueOf(2, 3.1), tolerance));
        assertTrue(Vect.valueOf(2, 3).match(Vect.valueOf(2.1, 3.1), tolerance));
        assertFalse(Vect.valueOf(2, 3).match(Vect.valueOf(2, 3.2), tolerance));
        assertFalse(Vect.valueOf(2, 3).match(Vect.valueOf(2.2, 3), tolerance));
        assertFalse(Vect.valueOf(2, 3).match(Vect.valueOf(2.2, 3.2), tolerance));
        assertFalse(Vect.valueOf(2, 3).match(Vect.valueOf(-2, 3), tolerance));
        assertFalse(Vect.valueOf(2, 3).match(Vect.valueOf(2, -3), tolerance));
    }

    @Test
    public void testCompare() {
        assertEquals(0, Vect.compare(1, 1, 1, 1));
        assertEquals(-1, Vect.compare(1, 1, 1, 2));
        assertEquals(-1, Vect.compare(1, 1, 2, 1));
        assertEquals(-1, Vect.compare(1, 1, 2, 2));
        assertEquals(1, Vect.compare(1, 2, 1, 1));
        assertEquals(0, Vect.compare(1, 2, 1, 2));
        assertEquals(-1, Vect.compare(1, 2, 2, 1));
        assertEquals(-1, Vect.compare(1, 2, 2, 2));
        assertEquals(1, Vect.compare(2, 1, 1, 1));
        assertEquals(1, Vect.compare(2, 1, 1, 2));
        assertEquals(0, Vect.compare(2, 1, 2, 1));
        assertEquals(-1, Vect.compare(2, 1, 2, 2));
        assertEquals(1, Vect.compare(2, 2, 1, 1));
        assertEquals(1, Vect.compare(2, 2, 1, 2));
        assertEquals(1, Vect.compare(2, 2, 2, 1));
        assertEquals(0, Vect.compare(2, 2, 2, 2));
    }

    @Test
    public void testCompareTo() {
        assertEquals(-1, Vect.valueOf(1, 2).compareTo(Vect.valueOf(3, 4)));
        try {
            Vect.valueOf(1, 2).compareTo(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testToString_0args() {
        assertEquals("[0,0]", Vect.ZERO.toString());
        assertEquals("[3.4,5.6]", Vect.valueOf(3.4, 5.6).toString());
    }

    @Test
    public void testToString_double_double() {
        StringBuilder str = new StringBuilder();
        Vect.valueOf(3.4, 5.6).toString(str);
        assertEquals("[3.4,5.6]", str.toString());
        try {
            Vect.ZERO.toString(null);
        } catch (NullPointerException ex) {
            //Expected
        }
    }

    @Test
    public void testToString_Appendable() {
        Vect v = new Vect(1, 2.3);
        StringBuilder str = new StringBuilder();
        v.toString(str);
        assertEquals("[1,2.3]", str.toString());
    }

    @Test
    public void testHashCode() {
        assertEquals(Vect.valueOf(1, 2).hashCode(), Vect.valueOf(1, 2).hashCode()); // equal should have same hashcode
        Set<Integer> hashes = new HashSet<>();
        for (int i = 1; i < 100; i++) { // minor test - no collisions in 200 elements
            int a = Vect.valueOf(0, i).hashCode();
            int b = Vect.valueOf(i, 0).hashCode();
            assertFalse(hashes.contains(a));
            hashes.add(a);
            assertFalse(hashes.contains(b));
            hashes.add(b);
        }
    }

    @Test
    public void testEquals() {
        assertEquals(Vect.valueOf(1, 2), Vect.valueOf(1, 2));
        assertFalse(Vect.valueOf(1, 2).equals(Vect.valueOf(1, 3)));
        assertFalse(Vect.valueOf(1, 2).equals(Vect.valueOf(-1, 2)));
        assertFalse(Vect.valueOf(1, 2).equals((Object) null)); // equals null should not throw an NPE

    }

    @Test
    public void testClone() {
        Vect a = Vect.valueOf(5, 7);
        assertSame(a, a.clone());
    }

    @Test
    public void testExternalize() throws IOException {
        Vect a = Vect.valueOf(7, 11);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bout)) {
            a.write(out);
        }
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            Vect b = Vect.read(in);
            assertEquals(a, b);
        }
    }

    @Test
    public void testGetBounds() {
        Vect vect = Vect.valueOf(1, 2);
        Rect bounds = vect.getBounds();
        assertEquals("[1,2,1,2]", bounds.toString());
    }

    @Test
    public void testPathIterator() {
        double[] coords = new double[6];
        float[] fcoords = new float[6];
        Vect vect = Vect.valueOf(1, 2);
        PathIterator iter = vect.pathIterator();
        assertEquals(PathIterator.WIND_NON_ZERO, iter.getWindingRule());

        assertFalse(iter.isDone());
        assertEquals(PathIterator.SEG_MOVETO, iter.currentSegment(coords));
        assertEquals(1, coords[0], 0.00001);
        assertEquals(2, coords[1], 0.00001);
        assertEquals(PathIterator.SEG_MOVETO, iter.currentSegment(fcoords));
        assertEquals(1, fcoords[0], 0.00001);
        assertEquals(2, fcoords[1], 0.00001);
        iter.next();

        assertFalse(iter.isDone());
        assertEquals(PathIterator.SEG_LINETO, iter.currentSegment(coords));
        assertEquals(1, coords[0], 0.00001);
        assertEquals(2, coords[1], 0.00001);
        assertEquals(PathIterator.SEG_LINETO, iter.currentSegment(fcoords));
        assertEquals(1, fcoords[0], 0.00001);
        assertEquals(2, fcoords[1], 0.00001);
        iter.next();

        assertTrue(iter.isDone());
    }

    @Test
    public void testToBuilder() {
        Vect vect = new Vect(3, 7);
        VectBuilder builder = vect.toBuilder();
        assertEquals(builder, new VectBuilder(3, 7));
    }

    @Test
    public void testAddBoundsTo() {
        Vect vect = Vect.valueOf(3, 7);
        RectBuilder builder = new RectBuilder();
        vect.addBoundsTo(builder);
        assertEquals(new RectBuilder(3, 7, 3, 7), builder);
        try {
            vect.addBoundsTo(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        Vect.valueOf(1, 11).addBoundsTo(builder);
        assertEquals(new RectBuilder(1, 7, 3, 11), builder);
    }

    @Test
    public void testTransform() {
        Transform transform = new TransformBuilder().translate(3, 7).build();
        assertEquals(Vect.valueOf(16, 36), Vect.valueOf(13, 29).transform(transform));
        try {
            Vect.valueOf(13, 29).transform(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testAddTo() {
        Vect vect = Vect.valueOf(3, 7);
        Network network = new Network();
        vect.addTo(network, 0);
        assertEquals("[[3,7]]", network.toString());
        vect.addTo(network, 0);
        assertEquals("[[3,7]]", network.toString());
    }
}
