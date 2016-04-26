package org.jg.geom;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.TransformBuilder;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar_000
 */
public class RectTest {

    @Test
    public void testGetWidth() {
        Rect rect = Rect.valueOf(5, 5, 5, 5);
        assertEquals(0, rect.getWidth(), 0.00001);
        rect = Rect.valueOf(5, 5, 9, 5);
        assertEquals(4, rect.getWidth(), 0.00001);
        rect = Rect.valueOf(5, 5, 1, 5);
        assertEquals(4, rect.getWidth(), 0.00001);
    }

    @Test
    public void testGetHeight() {
        Rect rect = Rect.valueOf(5, 5, 5, 5);
        assertEquals(0, rect.getHeight(), 0.00001);
        rect = Rect.valueOf(5, 5, 5, 9);
        assertEquals(4, rect.getHeight(), 0.00001);
        rect = Rect.valueOf(5, 5, 5, 1);
        assertEquals(4, rect.getHeight(), 0.00001);
    }

    @Test
    public void testGetArea() {
        Rect rect = Rect.valueOf(1, 3, 7, 13);
        assertEquals(60, rect.getArea(), 0.00001);
        rect = Rect.valueOf(1, 13, 7, 3);
        assertEquals(60, rect.getArea(), 0.00001);
        rect = Rect.valueOf(7, 3, 1, 13);
        assertEquals(60, rect.getArea(), 0.00001);
        rect = Rect.valueOf(7, 13, 1, 3);
        assertEquals(60, rect.getArea(Linearizer.DEFAULT, Tolerance.DEFAULT), 0.00001);
    }

    @Test
    public void testGetCx() {
        Rect rect = Rect.valueOf(1, 3, 7, 13);
        assertEquals(4, rect.getCx(), 0.00001);
        rect = Rect.valueOf(1, 13, 7, 3);
        assertEquals(4, rect.getCx(), 0.00001);
        rect = Rect.valueOf(7, 3, 1, 13);
        assertEquals(4, rect.getCx(), 0.00001);
        rect = Rect.valueOf(7, 13, 1, 3);
        assertEquals(4, rect.getCx(), 0.00001);
    }

    @Test
    public void testGetCy() {
        Rect rect = Rect.valueOf(1, 3, 7, 13);
        assertEquals(8, rect.getCy(), 0.00001);
        rect = Rect.valueOf(1, 13, 7, 3);
        assertEquals(8, rect.getCy(), 0.00001);
        rect = Rect.valueOf(7, 3, 1, 13);
        assertEquals(8, rect.getCy(), 0.00001);
        rect = Rect.valueOf(7, 13, 1, 3);
        assertEquals(8, rect.getCy(), 0.00001);
    }

    @Test
    public void testGetCentroid() {
        Rect rect = Rect.valueOf(2, -17, -11, 3);
        assertEquals(-4.5, rect.getCx(), 0.00001);
        assertEquals(-7, rect.getCy(), 0.00001);
        assertEquals(Vect.valueOf(-4.5, -7), rect.getCentroid());
    }
    
    @Test
    public void testRelate_Vect() {
        Rect a = Rect.valueOf(10, 10, 20, 20);
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                Vect b = Vect.valueOf(x1, y1);
                int relate = a.relate(b, Tolerance.DEFAULT);
                if ((x1 > a.minX) && (y1 > a.minY) && (x1 < a.maxX) && (y1 < a.maxY)) {
                    assertEquals(Relation.B_INSIDE_A | Relation.A_OUTSIDE_B, relate);
                } else if ((((x1 == a.minX) || (x1 == a.maxX)) && (y1 >= a.minY) && (y1 <= a.maxY))
                        || (((y1 == a.minY) || (y1 == a.maxY)) && (x1 >= a.minX) && (x1 <= a.maxX))) {
                    assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, relate);
                } else {
                    assertEquals(Relation.DISJOINT, relate);
                }
            }
        }
        assertEquals(Relation.B_INSIDE_A | Relation.A_OUTSIDE_B, Rect.valueOf(20, 20, 10, 10).relate(new VectBuilder(15, 15), Tolerance.DEFAULT));
        try {
            a.relate((Vect) null, Tolerance.DEFAULT);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            a.relate((VectBuilder) null, Tolerance.DEFAULT);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            a.relate((Vect) null, Tolerance.DEFAULT);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    } 
    
    @Test
    public void testRelate_Geom() {
        Rect a = Rect.valueOf(10, 10, 20, 20);
        assertEquals(Relation.TOUCH | Relation.A_INSIDE_B | Relation.B_INSIDE_A, a.relate(a, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(Relation.TOUCH | Relation.A_INSIDE_B | Relation.B_INSIDE_A, a.relate(a.toRing(), Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, a.relate(new LineString(a.toRing().vects), Linearizer.DEFAULT, Tolerance.DEFAULT));
        
    }

    @Test
    public void testIntersection() {
        Rect a = Rect.valueOf(10, 10, 20, 20);
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                for (int x2 = x1; x2 < 30; x2 += 5) {
                    for (int y2 = y1; y2 < 30; y2 += 5) {
                        Rect b = Rect.valueOf(x1, y1, x2, y2);
                        boolean disjoint = (x1 > a.maxX) || (y1 > a.maxY) || (x2 < a.minX) || (y2 < a.minY);
                        if (disjoint) {
                            assertNull(a.intersection(b));
                        } else {
                            Rect c = Rect.valueOf(Math.max(x1, a.minX),
                                    Math.max(y1, a.minY),
                                    Math.min(x2, a.maxX),
                                    Math.min(y2, a.maxY));
                            assertEquals(c, a.intersection(b));
                        }
                    }
                }
            }
        }
        assertEquals(Rect.valueOf(10, 10, 20, 20), Rect.valueOf(20, 20, 10, 10).intersection(Rect.valueOf(10, 10, 20, 20)));
        try {
            a.intersection(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testAdd() {
        Rect a = Rect.valueOf(10, 10, 20, 20);
        for (int x1 = 0; x1 < 30; x1 += 5) {
            for (int y1 = 0; y1 < 30; y1 += 5) {
                for (int x2 = x1; x2 < 30; x2 += 5) {
                    for (int y2 = y1; y2 < 30; y2 += 5) {
                        Rect b = Rect.valueOf(x1, y1, x2, y2);
                        Rect c = a.add(b);
                        assertEquals(Math.min(x1, a.minX), c.getMinX(), 0.00001);
                        assertEquals(Math.min(y1, a.minY), c.getMinY(), 0.00001);
                        assertEquals(Math.max(x2, a.maxX), c.getMaxX(), 0.00001);
                        assertEquals(Math.max(y2, a.maxY), c.getMaxY(), 0.00001);
                    }
                }
            }
        }
        try {
            a.add(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
    }

    @Test
    public void testBuffer() {
        Rect rect = Rect.valueOf(11, 17, 29, 43);
        assertEquals(Rect.valueOf(10, 16, 30, 44), rect.buffer(1));
        assertEquals(Rect.valueOf(13, 19, 27, 41), rect.buffer(-2));
        assertEquals(Rect.valueOf(11, 17, 29, 43), rect.buffer(0));
        try {
            rect.buffer(Double.NaN);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //Expected
        }
        try {
            rect.buffer(Double.POSITIVE_INFINITY);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //Expected
        }
        assertNull(rect.buffer(-10));
        assertNull(Rect.valueOf(0, 0, 2, 10).buffer(-2));
        assertNull(Rect.valueOf(0, 0, 10, 2).buffer(-2));
    }

    @Test
    public void testBuffer_Tolerance() {
        Rect rect = Rect.valueOf(3, 7, 13, 23);
        assertSame(rect, rect.buffer(0, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(Rect.valueOf(4, 8, 12, 22), rect.buffer(-1, Linearizer.DEFAULT, Tolerance.DEFAULT));
        Area a = (Area) rect.buffer(2, new Linearizer(0.5, Tolerance.DEFAULT), Tolerance.DEFAULT);

        assertEquals(Rect.valueOf(1, 5, 15, 25), a.getBounds());
        assertEquals(264 + (Math.PI * 4), a.getArea(), 2);
        assertEquals(52 + (Math.PI * 4), a.shell.getLength(), 0.5);
    }

    @Test
    public void testHashCode() {
        assertEquals(Rect.valueOf(2, 3, 11, 17).hashCode(), Rect.valueOf(2, 3, 11, 17).hashCode()); // equal should have same hashcode
        Set<Integer> hashes = new HashSet<>();
        for (int i = 2; i < 52; i++) { // minor test - no collisions in 200 elements
            int a = Rect.valueOf(1, 1, 1, i).hashCode();
            int c = Rect.valueOf(1, -i, 1, 1).hashCode();
            int b = Rect.valueOf(1, 1, i, 1).hashCode();
            int d = Rect.valueOf(-i, 1, 1, 1).hashCode();
            assertFalse(hashes.contains(a));
            hashes.add(a);
            assertFalse(hashes.contains(b));
            hashes.add(b);
            assertFalse(hashes.contains(c));
            hashes.add(c);
            assertFalse(hashes.contains(d));
            hashes.add(d);
        }
    }

    @Test
    public void testEquals() {
        assertEquals(Rect.valueOf(1, 2, 3, 4), Rect.valueOf(1, 2, 3, 4));
        assertFalse(Rect.valueOf(1, 2, 3, 4).equals(Rect.valueOf(-1, 2, 3, 4)));
        assertFalse(Rect.valueOf(1, 2, 3, 4).equals(Rect.valueOf(1, -2, 3, 4)));
        assertFalse(Rect.valueOf(1, 2, 3, 4).equals(Rect.valueOf(1, 2, 5, 4)));
        assertFalse(Rect.valueOf(1, 2, 3, 4).equals(Rect.valueOf(1, 2, 3, 5)));
        assertFalse(Rect.valueOf(1, 2, 3, 4).equals(null));
        assertEquals(Rect.valueOf(1, 2, 3, 4), Rect.valueOf(1, 2, 3, 4));
        assertFalse(Rect.valueOf(1, 2, 3, 4).equals(Rect.valueOf(-1, 2, 3, 4)));
        assertFalse(Rect.valueOf(1, 2, 3, 4).equals(Rect.valueOf(1, -2, 3, 4)));
        assertFalse(Rect.valueOf(1, 2, 3, 4).equals(Rect.valueOf(1, 2, 5, 4)));
        assertFalse(Rect.valueOf(1, 2, 3, 4).equals(Rect.valueOf(1, 2, 3, 5)));
    }

    @Test
    public void testToString_0args() {
        assertEquals("[\"RE\",1,2,3,4]", Rect.valueOf(1, 2, 3, 4).toString());
        assertEquals("[\"RE\",1.5,2.5,3.5,4.5]", Rect.valueOf(1.5, 2.5, 3.5, 4.5).toString());
    }

    @Test
    public void testToString_Appendable() throws Exception {
        StringBuilder str = new StringBuilder();
        Rect.valueOf(1, 2, 3, 4).toString(str);
        assertEquals("[\"RE\",1,2,3,4]", str.toString());
        str.setLength(0);
        Rect.valueOf(1.5, 2.5, 3.5, 4.5).toString(str);
        assertEquals("[\"RE\",1.5,2.5,3.5,4.5]", str.toString());
        try {
            Rect.valueOf(1, 2, 3, 4).toString(new Appendable() {
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
            fail("Exception expected");
        } catch (GeomException ex) {

        }
        try {
            Rect.toString(Rect.valueOf(1, 2, 3, 4), new Appendable() {
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
            fail("Exception expected");
        } catch (GeomException ex) {

        }
    }

    @Test
    public void testClone() throws Exception {
        Rect a = Rect.valueOf(1, 2, 3, 4);
        Rect b = a.clone();
        assertSame(a, b);
    }

    @Test
    public void testExternalize() throws Exception {
        Rect a = Rect.valueOf(3, 7, 13, 29);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bout)) {
            a.write(out);
        }
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            Rect b = Rect.read(in);
            assertEquals(a, b);
        }
        
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
            Rect.read(new DataInputStream(new InputStream(){
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
    public void testValueOf() {
        Rect a = Rect.valueOf(3, 7, 13, 29);
        assertEquals(3, a.getMinX(), 0.00001);
        assertEquals(7, a.getMinY(), 0.00001);
        assertEquals(13, a.getMaxX(), 0.00001);
        assertEquals(29, a.getMaxY(), 0.00001);
        try {
            Rect.valueOf(Double.NaN, 7, 13, 29);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //Expected
        }
        try {
            Rect.valueOf(3, Double.POSITIVE_INFINITY, 13, 29);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //Expected
        }
        try {
            Rect.valueOf(3, 7, Double.NEGATIVE_INFINITY, 29);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //Expected
        }
        try {
            Rect.valueOf(3, 7, 13, Double.NaN);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //Expected
        }
    }

    @Test
    public void testGetBounds() {
        Rect a = Rect.valueOf(3, 7, 13, 29);
        Rect b = a.getBounds();
        assertSame(a, b);
    }

    @Test
    public void testTransform() {
        Transform transform = new TransformBuilder().scaleAround(2, 4, 9).build();
        Rect a = Rect.valueOf(3, 7, 13, 29);
        Rect b = a.transform(transform);
        assertEquals("[\"RE\",2,5,22,49]", b.toString());
    }

    @Test
    public void testPathIterator() {
        Rect a = Rect.valueOf(3, 7, 13, 29);
        Path2D.Double path = new Path2D.Double();
        path.append(a.pathIterator(), true);
        Rectangle2D bounds = path.getBounds2D();
        assertEquals(3, bounds.getMinX(), 0.0001);
        assertEquals(7, bounds.getMinY(), 0.0001);
        assertEquals(13, bounds.getMaxX(), 0.0001);
        assertEquals(29, bounds.getMaxY(), 0.0001);
    }

    @Test
    public void testAddTo() {
        Network network = new Network();
        Rect a = Rect.valueOf(3, 7, 13, 29);
        a.addTo(network, Linearizer.DEFAULT, Tolerance.DEFAULT);
        assertEquals("[[3,7, 13,7, 13,29, 3,29, 3,7]]", network.toString());
    }

    @Test
    public void testUnion() {
        Rect a = Rect.valueOf(10, 20, 30, 40);
        Rect b = Rect.valueOf(50, 60, 70, 80);
        Rect c = Rect.valueOf(11, 21, 29, 39);
        Rect d = Rect.valueOf(15, 25, 35, 45);
        Area e = Area.valueOf(Tolerance.DEFAULT, 15,25, 35,25, 35,45, 15,45, 15,25);
        assertSame(a, a.union(c, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertSame(a, c.union(a, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals("[\"AR\",[[10,20, 30,20, 30,40, 10,40, 10,20]],[[50,60, 70,60, 70,80, 50,80, 50,60]]]", a.union(b, Linearizer.DEFAULT, Tolerance.DEFAULT).toString());
        Ring expected = Ring.valueOf(Tolerance.DEFAULT, 10,20, 30,20, 30,25, 35,25, 35,45, 15,45, 15,40, 10,40, 10,20);
        assertEquals(expected, a.union(d, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(expected, a.union(e, Linearizer.DEFAULT, Tolerance.DEFAULT));
    }

    @Test
    public void testIntersection_B() {
        Rect a = Rect.valueOf(10, 20, 30, 40);
        Rect b = Rect.valueOf(50, 60, 70, 80);
        Rect c = Rect.valueOf(11, 21, 29, 39);
        Rect d = Rect.valueOf(15, 25, 35, 45);
        Area e = Area.valueOf(Tolerance.DEFAULT, 15,25, 35,25, 35,45, 15,45, 15,25);
        assertSame(c, a.intersection(c, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertSame(c, c.intersection(a, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertNull(a.intersection(b, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(Rect.valueOf(15,25,30,40), a.intersection(d, Linearizer.DEFAULT, Tolerance.DEFAULT));
        Ring result = Ring.valueOf(Tolerance.DEFAULT, 15,25, 30,25, 30,40, 15,40, 15,25);
        assertEquals(result, a.intersection(e, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(result, a.intersection(d.toGeoShape(Linearizer.DEFAULT, Tolerance.DEFAULT), Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertSame(result, a.intersection(result, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertNull(b.intersection(result, Linearizer.DEFAULT, Tolerance.DEFAULT));
    }

    @Test
    public void testLess() {
        Rect a = Rect.valueOf(10, 20, 30, 40);
        Rect b = Rect.valueOf(50, 60, 70, 80);
        Rect c = Rect.valueOf(11, 21, 29, 39);
        Rect d = Rect.valueOf(15, 25, 35, 45);
        Ring e = Ring.valueOf(Tolerance.DEFAULT, 10,20, 30,20, 30,40, 10,40, 10,20);
        Area f = Area.valueOf(Tolerance.DEFAULT, 11,21, 29,21, 29,39, 11,39, 11,21);
        Area g = new Area(e,new Area[]{f});
        assertSame(a, a.less(b, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertNull(c.less(a, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(g, a.less(c, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertEquals(g, a.less(f, Linearizer.DEFAULT, Tolerance.DEFAULT));
        Ring h = Ring.valueOf(Tolerance.DEFAULT, 15,25, 30,25, 30,40, 15,40, 15,25);
        Ring i = Ring.valueOf(Tolerance.DEFAULT, 10,20, 30,20, 30,25, 15,25, 15,40, 10,40, 10,20);
        assertEquals(i, a.less(h, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertNull(a.less(a.toArea(), Linearizer.DEFAULT, Tolerance.DEFAULT));
    }
}
