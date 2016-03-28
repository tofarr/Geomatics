package org.jg.geom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jg.util.SpatialNode;
import org.jg.util.SpatialNode.NodeProcessor;
import org.jg.util.Tolerance;
import org.jg.util.TransformBuilder;
import org.jg.util.VectList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar
 */
public class RingTest {

//    @Test
//    public void testConstructor() {
//        Ring ring = new Ring(new VectList(0, 5, 20, 5, 20, 10, 0, 10, 0, 5));
//        assertEquals(100, ring.getArea(), 0.001);
//        assertEquals(Rect.valueOf(0, 5, 20, 10), ring.getBounds());
//        assertEquals(Vect.valueOf(10, 7.5), ring.getCentroid());
//        assertEquals(50, ring.getLength(), 0.00001);
//        assertEquals(50, ring.getLength(), 0.00001);
//        assertEquals("[0,5, 20,5, 20,10, 0,10, 0,5]", ring.toString());
//        try {
//            ring = new Ring(null);
//            fail("Exception expected");
//        } catch (NullPointerException ex) {
//        }
//        try {
//            ring = new Ring(new VectList(0, 5, 20, 10, 0, 5));
//            fail("Exception expected");
//        } catch (IllegalArgumentException ex) {
//        }
//        try {
//            ring = new Ring(new VectList(0, 5, 20, 10, 0, 10, 0, 6));
//            fail("Exception expected");
//        } catch (IllegalArgumentException ex) {
//        }
//        try {
//            ring = new Ring(new VectList(0, 5, 20, 10, 0, 10, 1, 5));
//            fail("Exception expected");
//        } catch (IllegalArgumentException ex) {
//        }
//        assertTrue(ring.isConvex());
//        assertTrue(ring.isValid());
//
//        ring = new Ring(new VectList(0, 5, 0, 10, 20, 10, 20, 5, 0, 5));
//        //assertEquals(100, ring.getArea(), 0.001);
//        assertEquals(Rect.valueOf(0, 5, 20, 10), ring.getBounds());
//        //assertEquals(Vect.valueOf(10,7.5), ring.getCentroid());
//        assertEquals(50, ring.getLength(), 0.00001);
//        assertEquals("[0,5, 0,10, 20,10, 20,5, 0,5]", ring.toString());
//        //assertTrue(ring.isConvex());
//        assertFalse(ring.isValid());
//    }
//
//    @Test
//    public void testValueOf() {
//        Network network = new Network();
//        assertTrue(Ring.valueOf(network).isEmpty());
//        network.addAllLinks(new VectList(0, 0, 10, 0, 10, 30, 0, 30, 0, 20, 20, 20, 20, 10, 0, 10, 0, 0));
//        List<Ring> rings = Ring.valueOf(network);
//        assertEquals(1, rings.size());
//        Ring ring = rings.get(0);
//        try {
//            ring = new Ring(null);
//            fail("Exception expected");
//        } catch (NullPointerException ex) {
//        }
//        //assertEquals(100, ring.getArea(), 0.001); // invalid so do not test
//        assertEquals(Rect.valueOf(0, 0, 20, 30), ring.getBounds());
//        //assertEquals(Vect.valueOf(10,15), ring.getCentroid()); // invalid so do not test
//        assertEquals(120, ring.getLength(), 0.00001);
//        assertEquals("[0,0, 10,0, 10,30, 0,30, 0,20, 20,20, 20,10, 0,10, 0,0]", ring.toString());
//        //assertTrue(ring.isConvex()); // invalid so do not test
//        assertFalse(ring.isValid());
//
//        network.explicitIntersections(Tolerance.DEFAULT);
//        rings = Ring.valueOf(network);
//        assertEquals(3, rings.size());
//        assertEquals("[[0,0, 10,0, 10,10, 0,10, 0,0], [0,20, 10,20, 10,30, 0,30, 0,20], [10,10, 20,10, 20,20, 10,20, 10,10]]", rings.toString());
//    }
//
//    @Test
//    public void testValueOf_B() {
//        Network network = new Network();
//        network.addAllLinks(new VectList(50, 100, 0, 50, 50, 0, 100, 50, 50, 100));
//        network.addLink(100, 50, 150, 50);
//        network.addLink(50, 100, 50, 150);
//        network.addLink(50, 150, 100, 150);
//        network.addLink(350, 50, 400, 150);
//        List<Ring> rings = Ring.valueOf(network);
//        assertEquals(1, rings.size());
//        Ring ring = rings.get(0);
//        assertEquals(5000, ring.getArea(), 0.001);
//        assertEquals(Rect.valueOf(0, 0, 100, 100), ring.getBounds());
//        assertEquals(Vect.valueOf(50, 50), ring.getCentroid());
//        assertEquals(Math.sqrt(5000) * 4, ring.getLength(), 0.00001);
//        assertEquals("[0,50, 50,0, 100,50, 50,100, 0,50]", ring.toString());
//        assertTrue(ring.isConvex());
//        assertTrue(ring.isValid());
//    }
//    
//    @Test
//    public void testValueOf_C() {
//        Network network = new Network();
//        network.addAllLinks(new VectList(0,50, 50,0, 100,50, 0,50, 50,100, 100,50));
//        List<Ring> rings = Ring.valueOf(network);
//        ArrayList<Ring> expected = new ArrayList<>();
//        expected.add(new Ring(new VectList(0,50, 50,0, 100,50, 0,50)));
//        expected.add(new Ring(new VectList(0,50, 100,50, 50,100, 0,50)));
//        assertEquals(expected, rings);
//    }  
//
//    @Test
//    public void testValueOf_D() {
//        Network network = new Network();
//        network.addAllLinks(new VectList(10,0, 10,10, 0,10, 0,0, 30,0, 30,10, 20,10, 20,0));
//        network.explicitIntersections(Tolerance.DEFAULT);
//        List<Ring> rings = Ring.valueOf(network);
//        ArrayList<Ring> expected = new ArrayList<>();
//        expected.add(new Ring(new VectList(0,0, 10,0, 10,10, 0,10, 0,0)));
//        expected.add(new Ring(new VectList(20,0, 30,0, 30,10, 20,10, 20,0)));
//        assertEquals(expected, rings);
//    }
//
//    @Test
//    public void testValueOf_E() {
//        Network network = new Network();
//        //Network has some explicit intersections but not others - it is completely rubbish input!
//        network.addAllLinks(new VectList(0,10, 0,20, 20,0, 30,10, 40,20, 40,10, 30,10, 0,10));
//        List<Ring> rings = Ring.valueOf(network);
//        ArrayList<Ring> expected = new ArrayList<>();
//        expected.add(new Ring(new VectList(0,10, 0,20, 20,0, 30,10, 0,10)));
//        expected.add(new Ring(new VectList(30,10, 40,10, 40,20, 30,10)));
//        assertEquals(expected, rings);
//        assertFalse(rings.get(0).isValid());
//        assertTrue(rings.get(1).isValid());
//    }
//
//    @Test
//    public void testTriangle() {
//        Ring ring = new Ring(new VectList(0, 0, 10, 0, 10, 10, 0, 0));
//        assertEquals(50, ring.getArea(), 0.0001);
//        assertEquals(Math.sqrt(200) + 20, ring.getLength(), 0.0001);
//        assertEquals(Rect.valueOf(0, 0, 10, 10), ring.getBounds());
//    }
//
//    @Test
//    public void testAddBoundsTo() {
//        Ring ring = new Ring(new VectList(3, 5, 13, 17, 7, 11, 3, 5));
//        RectBuilder bounds = new RectBuilder();
//        ring.addBoundsTo(bounds);
//        assertEquals(new RectBuilder(3, 5, 13, 17), bounds);
//    }
//
//    @Test
//    public void testRelate() {
//        Ring ring = new Ring(new VectList(0, 0, 5, 30, 10, 30, 10, 10, 20, 10, 20, 30, 25, 30, 30, 0, 0, 0)); //should work regardless of validity
//        assertEquals(Relate.INSIDE, ring.relate(Vect.valueOf(15, 5), Tolerance.DEFAULT));
//        assertEquals(Relate.OUTSIDE, ring.relate(15, 15, Tolerance.DEFAULT));
//        assertEquals(Relate.TOUCH, ring.relate(Vect.valueOf(15, 10), Tolerance.DEFAULT));
//        assertEquals(Relate.OUTSIDE, ring.relate(30, 30, Tolerance.DEFAULT));
//        assertEquals(Relate.OUTSIDE, ring.relate(35, 30, Tolerance.DEFAULT));
//        assertEquals(Relate.INSIDE, ring.relate(5, 10, Tolerance.DEFAULT));
//        assertEquals(Relate.INSIDE, ring.relate(8, 12, Tolerance.DEFAULT));
//        assertEquals(Relate.INSIDE, ring.relate(25, 10, Tolerance.DEFAULT));
//        assertEquals(Relate.INSIDE, ring.relate(22, 12, Tolerance.DEFAULT));
//        assertEquals(Relate.TOUCH, ring.relate(2.5, 15, Tolerance.DEFAULT));
//        assertEquals(Relate.TOUCH, ring.relate(27.5, 15, Tolerance.DEFAULT));
//    }
//
//    @Test
//    public void testGetLineIndex() {
//        VectList vects = new VectList(0, 0, 5, 30, 10, 30, 10, 10, 20, 10, 20, 30, 25, 30, 30, 0, 0, 0);
//        Ring ring = new Ring(vects); //should work regardless of validity
//        assertFalse(ring.isValid());
//        final Map<Line, Rect> lines = new HashMap<>();
//        for (int i = 0; i < vects.size() - 1; i++) {
//            Line line = vects.getLine(i);
//            lines.put(line, line.getBounds());
//        }
//        SpatialNode<Line> lineIndex = ring.getLineIndex();
//        lineIndex.forEach(new NodeProcessor<Line>() {
//            @Override
//            public boolean process(Rect bounds, Line value) {
//                assertEquals(lines.remove(value), bounds);
//                return true;
//            }
//        });
//        assertTrue(lines.isEmpty());
//    }
//
//    @Test
//    public void testIsValid() {
//        Ring a = new Ring(new VectList(0, 0, 10, 0, 10, 10, 0, 10, 0, 0));
//        assertTrue(a.isValid());
//        assertTrue(a.isValid());
//        assertFalse(new Ring(new VectList(0, 0, 0, 10, 10, 10, 10, 0, 0, 0)).isValid());
//        assertFalse(new Ring(new VectList(0, 0, 10, 10, 10, 0, 0, 10, 0, 0)).isValid());
//    }
//
//    @Test
//    public void testNormalize() {
//        Ring a = new Ring(new VectList(10, 0, 10, 10, 0, 10, 0, 0, 10, 0));
//        Ring b = a.normalize();
//        Ring c = b.normalize();
//        assertEquals("[10,0, 10,10, 0,10, 0,0, 10,0]", a.toString());
//        assertEquals("[0,0, 10,0, 10,10, 0,10, 0,0]", b.toString());
//        assertSame(b, c);
//    }
//
//    @Test
//    public void testGetCentroid_0args() {
//        Ring a = new Ring(new VectList(60, 50, 90, 50, 90, 70, 60, 70, 60, 50));
//        assertEquals(Vect.valueOf(75, 60), a.getCentroid());
//        assertSame(a.getCentroid(), a.getCentroid());
//        a = new Ring(new VectList(60, 50, 60, 70, 90, 70, 90, 50, 60, 50));
//        assertEquals(Vect.valueOf(75, 60), a.getCentroid());
//        assertSame(a.getCentroid(), a.getCentroid());
//    }
//
//    @Test
//    public void testIsConvex() {
//        Ring a = new Ring(new VectList(60, 50, 90, 50, 90, 70, 60, 70, 60, 50));
//        assertTrue(a.isConvex());
//        assertTrue(a.isConvex());
//        assertFalse(new Ring(new VectList(0, 0, 10, 0, 9, 5, 10, 10, 0, 10, 0, 0)).isConvex());
//        assertFalse(new Ring(new VectList(0, 0, 10, 0, 9, 5, 10, 10, 0, 10, 1, 5, 0, 0)).isConvex());
//        assertFalse(new Ring(new VectList(9, 5, 10, 10, 0, 10, 0, 0, 10, 0, 9, 5)).isConvex());
//    }
//
//    @Test
//    public void testGetVects() {
//        VectList a = new VectList(60, 50, 90, 50, 90, 70, 60, 70, 60, 50);
//        VectList b = new VectList();
//        Ring ring = new Ring(a);
//        ring.getVects(b);
//        assertEquals(a, b);
//        try {
//            ring.getVects(null);
//            fail("Exception expected!");
//        } catch (NullPointerException ex) {
//        }
//    }
//
//    @Test
//    public void testHashCode() {
//        Ring a = new Ring(new VectList(60, 50, 90, 50, 90, 70, 60, 70, 60, 50));
//        Ring b = new Ring(new VectList(60, 50, 90, 50, 90, 70, 60, 70, 60, 50));
//        Ring c = new Ring(new VectList(90, 50, 90, 70, 60, 70, 60, 50, 90, 50));
//        assertEquals(a.hashCode(), b.hashCode());
//        assertNotEquals(a.hashCode(), c.hashCode());
//    }
//
//    @Test
//    public void testEquals() {
//        Ring a = new Ring(new VectList(60, 50, 90, 50, 90, 70, 60, 70, 60, 50));
//        Ring b = new Ring(new VectList(60, 50, 90, 50, 90, 70, 60, 70, 60, 50));
//        Ring c = new Ring(new VectList(90, 50, 90, 70, 60, 70, 60, 50, 90, 50));
//        assertEquals(a, b);
//        assertNotEquals(a, c);
//        assertNotEquals(a, "");
//    }
//
//    @Test
//    public void testToString() {
//        StringBuilder str = new StringBuilder("[");
//        VectList vects = new VectList();
//        for (int i = 0; i <= 30; i++) {
//            vects.add(i, 0);
//            str.append(i).append(",0, ");
//        }
//        str.setLength(str.length() - 2);
//        for (int j = 31; j-- > 0;) {
//            vects.add(0, j);
//            str.append(", 0,").append(j);
//        }
//        str.append(']');
//        Ring a = new Ring(vects);
//        assertEquals("{size:62, bounds:[0,0,30,30]}", a.toString());
//        assertEquals(str.toString(), a.toString(false));
//        StringBuilder str2 = new StringBuilder();
//        a.toString(str2);
//        assertEquals(str.toString(), str2.toString());
//        try {
//            a.toString(new Appendable() {
//                @Override
//                public Appendable append(CharSequence csq) throws IOException {
//                    throw new IOException();
//                }
//
//                @Override
//                public Appendable append(CharSequence csq, int start, int end) throws IOException {
//                    throw new IOException();
//                }
//
//                @Override
//                public Appendable append(char c) throws IOException {
//                    throw new IOException();
//                }
//
//            });
//            fail("Exception expected");
//        } catch (GeomException ex) {
//        }
//    }
//
//    @Test
//    public void testExternalize() throws Exception {
//        Ring a = new Ring(new VectList(100, 0, 200, 0, 200, 100, 100, 0));
//        ByteArrayOutputStream bout = new ByteArrayOutputStream();
//        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
//            out.writeObject(a);
//        }
//        Ring b;
//        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
//            b = (Ring) in.readObject();
//        }
//        assertEquals(a, b);
//        bout = new ByteArrayOutputStream();
//        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
//            a.write(out);
//        }
//        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
//            b = Ring.read(in);
//        }
//        assertEquals(a, b);
//    }
//    
//    @Test
//    public void testTransform(){
//        Ring ring = new Ring(new VectList(0,0, 10,0, 10,10, 0,0));
//        Ring transformed = ring.transform(new TransformBuilder().flipXAround(20).build());
//        assertEquals("[40,0, 30,0, 30,10, 40,0]", transformed.toString());
//        assertTrue(ring.isValid());
//        assertFalse(transformed.isValid());
//        assertEquals(50, ring.getArea(), 0.00001);
//        assertTrue(ring.isConvex());
//        transformed = ring.transform(new TransformBuilder().flipXAround(20).build());
//        transformed = transformed.normalize();
//        assertEquals("[30,0, 40,0, 30,10, 30,0]", transformed.toString());
//        assertTrue(ring.isValid());
//        assertTrue(transformed.isValid());
//        assertEquals(50, ring.getArea(), 0.00001);
//        assertTrue(ring.isConvex());
//        assertEquals(50, transformed.getArea(), 0.00001);
//        assertTrue(transformed.isConvex());
//    }
//    
//    @Test
//    public void testBuffer(){
//        Ring ring = new Ring(new VectList(0,0, 10,0, 10,10, 0,0));
//        Area buffered = ring.buffer(5, new Tolerance(0.5), Tolerance.DEFAULT);
//        Rect bounds = buffered.getBounds();
//        //assertEquals(-5, bounds.minX, 0.1)
//               
//        assertEquals(-5, bounds.minX, 0.1);
//        assertEquals(-5, bounds.minY, 0.1);
//        assertEquals(15, bounds.maxX, 0.1);
//        assertEquals(15, bounds.maxY, 0.1);
//        
//        assertEquals(50, ring.getArea(), 0.00001);
//    }
}