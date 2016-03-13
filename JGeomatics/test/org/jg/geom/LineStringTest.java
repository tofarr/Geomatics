package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jg.util.SpatialNode.NodeProcessor;
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
public class LineStringTest {

    @Test
    public void testValueOf() {
        assertNull(LineString.valueOf(new VectList()));
        assertEquals("[\"LS\", 3,7]", LineString.valueOf(new VectList().add(3, 7)).toString());
        try {
            LineString.valueOf((VectList) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        assertEquals("[\"LS\", 0,0, 10,0, 10,10, 20,10]", LineString.valueOf(new VectList().addAll(0, 0, 10, 0, 10, 10, 20, 10)).toString());
        assertEquals("[\"LS\", 0,0, 10,0, 10,10, 20,10]", LineString.valueOf(0, 0, 10, 0, 10, 0, 10, 10, 20, 10).toString());
    }

    @Test
    public void testGetBounds() {
        LineString ls = LineString.valueOf(new VectList().addAll(0, 0, 10, 0, 10, 10, 20, 10));
        assertEquals(Rect.valueOf(0, 0, 20, 10), ls.getBounds());
    }

    @Test
    public void testTransform() {
        Transform transform = new TransformBuilder().scale(2, 3).translate(-1, -2).build();
        LineString ls = LineString.valueOf(new VectList().addAll(1, 3, 7, 13, 17, 29));
        assertSame(ls, ls.transform(Transform.IDENTITY));
        LineString transformed = ls.transform(transform);
        assertEquals(LineString.valueOf(new VectList().addAll(1, 7, 13, 37, 33, 85)), transformed);
        try {
            ls.transform(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testPathIterator() {
        LineString ls = LineString.valueOf(new VectList().addAll(1, 3, 7, 13, 17, 29));
        PathIterator iter = ls.pathIterator();
        assertFalse(iter.isDone());
        double[] coords = new double[6];
        float[] fcoords = new float[6];
        assertEquals(PathIterator.SEG_MOVETO, iter.currentSegment(coords));
        assertEquals(PathIterator.SEG_MOVETO, iter.currentSegment(fcoords));
        assertEquals(1, coords[0], 0.00001);
        assertEquals(3, coords[1], 0.00001);
        assertEquals(1, fcoords[0], 0.00001);
        assertEquals(3, fcoords[1], 0.00001);
        iter.next();
        assertFalse(iter.isDone());
        assertEquals(PathIterator.SEG_LINETO, iter.currentSegment(coords));
        assertEquals(PathIterator.SEG_LINETO, iter.currentSegment(fcoords));
        assertEquals(7, coords[0], 0.00001);
        assertEquals(13, coords[1], 0.00001);
        assertEquals(7, fcoords[0], 0.00001);
        assertEquals(13, fcoords[1], 0.00001);
        iter.next();
        assertFalse(iter.isDone());
        assertEquals(PathIterator.SEG_LINETO, iter.currentSegment(coords));
        assertEquals(PathIterator.SEG_LINETO, iter.currentSegment(fcoords));
        assertEquals(17, coords[0], 0.00001);
        assertEquals(29, coords[1], 0.00001);
        assertEquals(17, fcoords[0], 0.00001);
        assertEquals(29, fcoords[1], 0.00001);
        iter.next();
        assertTrue(iter.isDone());

        ls = LineString.valueOf(new VectList().addAll(1, 3));
        iter = ls.pathIterator();
        assertFalse(iter.isDone());
        assertEquals(PathIterator.SEG_MOVETO, iter.currentSegment(coords));
        assertEquals(PathIterator.SEG_MOVETO, iter.currentSegment(fcoords));
        assertEquals(1, coords[0], 0.00001);
        assertEquals(3, coords[1], 0.00001);
        assertEquals(1, fcoords[0], 0.00001);
        assertEquals(3, fcoords[1], 0.00001);
        iter.next();
        assertFalse(iter.isDone());
        assertEquals(PathIterator.SEG_LINETO, iter.currentSegment(coords));
        assertEquals(PathIterator.SEG_LINETO, iter.currentSegment(fcoords));
        assertEquals(1, coords[0], 0.00001);
        assertEquals(3, coords[1], 0.00001);
        assertEquals(1, fcoords[0], 0.00001);
        assertEquals(3, fcoords[1], 0.00001);
        iter.next();
        assertTrue(iter.isDone());
    }

    @Test
    public void testClone() {
        LineString ls = LineString.valueOf(new VectList().addAll(1, 3, 7, 13, 17, 29));
        assertSame(ls, ls.clone());
    }

    @Test
    public void testToString_Appendable() {
        LineString ls = LineString.valueOf(new VectList().addAll(1, 3, 7, 13, 17, 29));
        assertEquals("[\"LS\", 1,3, 7,13, 17,29]", ls.toString());
        try {
            ls.toString(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            ls.toString(new Appendable() {
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
    public void testAddTo() {
        LineString ls = LineString.valueOf(new VectList().addAll(20, 0, 20, 20, 30, 20, 30, 10, 0, 10));
        Network network = new Network();
        ls.addTo(network, Tolerance.DEFAULT);
        assertEquals("[[0,10, 30,10, 30,20, 20,20, 20,0]]", network.toString());
    }

    @Test
    public void testGetLength_0args() {
        LineString ls = LineString.valueOf(new VectList().addAll(20, 0, 20, 20, 30, 20, 30, 10, 0, 10));
        assertEquals(70, ls.getLength(), 0.00001);
    }

    @Test
    public void testIsValid() {
        LineString ls = LineString.valueOf(new VectList().addAll(0, 0, 1, 0, 1, 10));
        assertTrue(ls.isValid(Tolerance.DEFAULT));
        assertFalse(ls.isValid(new Tolerance(1.1)));
        assertFalse(LineString.valueOf(1, 3).isValid(Tolerance.DEFAULT));
    }

    @Test
    public void testNormalize() {
        LineString a = LineString.valueOf(100, 100, 100, 50, 100, 0, 50, 0, 0, 0);
        LineString b = LineString.valueOf(0, 0, 100, 0, 100, 100);
        assertEquals(b, a.normalize(Tolerance.DEFAULT));
        assertSame(b, b.normalize(Tolerance.DEFAULT));
    }

    @Test
    public void testEquals() {
        LineString a = LineString.valueOf(0, 0, 100, 1, 100, 100);
        LineString b = LineString.valueOf(0, 0, 100, 0, 100, 100);
        assertEquals(a, a);
        assertNotEquals(a, b);
    }

    @Test
    public void testHashCode() {
        Set<Integer> hashCodes = new HashSet<>();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                LineString ls = LineString.valueOf(0, 0, x, y, 10, 10);
                hashCodes.add(ls.hashCode());
            }
        }
        assertEquals(100, hashCodes.size());
    }

    @Test
    public void testForInteractingLines() {
        LineString a = LineString.valueOf(0, 0, 0, 50, 50, 50, 50, 0, 10, 0, 10, 40, 40, 40, 40, 10, 20, 10, 20, 30, 30, 30, 30, 20);
        final Map<Rect, Line> map = new HashMap<>();
        map.put(Rect.valueOf(0, 0, 0, 50), Line.valueOf(0, 0, 0, 50));
        map.put(Rect.valueOf(0, 50, 50, 50), Line.valueOf(0, 50, 50, 50));

        map.put(Rect.valueOf(10, 0, 10, 40), Line.valueOf(10, 0, 10, 40));
        map.put(Rect.valueOf(10, 40, 40, 40), Line.valueOf(10, 40, 40, 40));

        assertTrue(a.forInteractingLines(Rect.valueOf(0, 40, 10, 50), new NodeProcessor<Line>() {
            @Override
            public boolean process(Rect bounds, Line value) {
                assertEquals(map.remove(bounds), value);
                return true;
            }

        }));
        assertTrue(map.isEmpty());
    }

    @Test
    public void testForOverlappingLines() {
        LineString a = LineString.valueOf(0, 0, 0, 50, 50, 50, 50, 0, 10, 0, 10, 40, 40, 40, 40, 10, 20, 10, 20, 30, 30, 30, 30, 20);
        final Map<Rect, Line> map = new HashMap<>();
        map.put(Rect.valueOf(0, 50, 50, 50), Line.valueOf(0, 50, 50, 50));

        assertTrue(a.forOverlappingLines(Rect.valueOf(0, 40, 10, 51), new NodeProcessor<Line>() {
            @Override
            public boolean process(Rect bounds, Line value) {
                assertEquals(map.remove(bounds), value);
                return true;
            }

        }));
        assertTrue(map.isEmpty());
    }

    @Test
    public void testExternalize() throws Exception {
        LineString a = LineString.valueOf(0, 0, 0, 50, 50, 50, 50, 0, 10, 0, 10, 40, 40, 40, 40, 10, 20, 10, 20, 30, 30, 30, 30, 20);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
            out.writeObject(a);
        }
        LineString b;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = (LineString) in.readObject();
        }
        assertEquals(a, b);
        bout = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
            a.write(out);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = LineString.read(in);
        }
        assertEquals(a, b);
    }

    @Test
    public void testBuffer_Point() {
        Tolerance flatness = new Tolerance(0.5);
        LineString s = LineString.valueOf(37, 61);
        assertNull(s.buffer(-1, flatness, Tolerance.DEFAULT)); // buffer out of existance
        assertSame(s, s.buffer(0, flatness, Tolerance.DEFAULT)); // no op buffer

        // buffer a point
        Geom g = s.buffer(5, flatness, Tolerance.DEFAULT);
        RingSet ringSet = (RingSet) g;
        Rect bounds = ringSet.getBounds();
        assertEquals(32, bounds.minX, 0.5);
        assertEquals(56, bounds.minY, 0.5);
        assertEquals(42, bounds.maxX, 0.5);
        assertEquals(66, bounds.maxY, 0.5);
        assertEquals(Math.PI * 25, ringSet.getArea(), 0.6);
        assertEquals(Math.PI * 10, ringSet.shell.getLength(), 0.5);

    }

    @Test
    public void testBuffer_RightAngle() {
        Tolerance flatness = new Tolerance(0.5);
        LineString s = LineString.valueOf(20, 0, 20, 60, 60, 60);
        assertNull(s.buffer(-1, flatness, Tolerance.DEFAULT)); // buffer out of existance
        assertSame(s, s.buffer(0, flatness, Tolerance.DEFAULT)); // no op buffer

        RingSet r = (RingSet) s.buffer(5, flatness, Tolerance.DEFAULT);
        double area = (35 * 10)
                + (55 * 10)
                + (25 * Math.PI)
                + (5 * 5 * 3)
                + (25 * Math.PI * 0.25);
        assertEquals(area, r.getArea(), 1);
        assertEquals(Rect.valueOf(15, -5, 65, 65), r.getBounds());

    }

    @Test
    public void testBuffer_Loop() {
        Tolerance flatness = new Tolerance(0.5);
        LineString s = LineString.valueOf(20, 0, 20, 60, 60, 60, 60, 20, 0, 20);
        assertNull(s.buffer(-1, flatness, Tolerance.DEFAULT)); // buffer out of existance
        assertSame(s, s.buffer(0, flatness, Tolerance.DEFAULT)); // no op buffer

        RingSet r = (RingSet) s.buffer(5, flatness, Tolerance.DEFAULT);
        double area = (30 * 10 * 4)
                + (5 * 5 * 3 * 3)
                + (5 * 5 * Math.PI * 0.25 * 3)
                + (10 * 10)
                + (5 * 5 * Math.PI)
                + (15 * 10 * 2);
        assertEquals(area, r.getArea(), 1);
    }

    @Test
    public void testBuffer_V() {
        Tolerance flatness = new Tolerance(0.5);
        LineString s = LineString.valueOf(20, 0, 20, 10, 19, 10.2, 20, 10.4, 20, 30);
        assertNull(s.buffer(-1, flatness, Tolerance.DEFAULT)); // buffer out of existance
        assertSame(s, s.buffer(0, flatness, Tolerance.DEFAULT)); // no op buffer

        RingSet r = (RingSet) s.buffer(5, flatness, Tolerance.DEFAULT);

        assertEquals(1, r.numRings());
        assertEquals(Rect.valueOf(14, -5, 25, 35), r.getBounds());
        assertEquals(382, r.getArea(), 1);
    }

    @Test
    public void testProjectOutward() {
        Tolerance flatness = new Tolerance(0.5);
        VectBuilder work = new VectBuilder();
        VectList result = new VectList();

        LineString.projectOutward(0, 0, 0, 10, 10, 10, 5, flatness, Tolerance.DEFAULT, work, result);
        assertEquals(2, result.size());
        assertEquals(5, result.getX(0), 0.0001);
        assertEquals(10, result.getY(0), 0.0001);
        assertEquals(0, result.getX(1), 0.0001);
        assertEquals(5, result.getY(1), 0.0001);

        result.clear();
        LineString.projectOutward(19, 10.2, 20, 10, 20, 0, 5, flatness, Tolerance.DEFAULT, work, result);
        assertEquals(2, result.size());
        assertEquals(19.019419324309084, result.getX(0), 0.0001);
        assertEquals(5.097096621545399, result.getY(0), 0.0001);
        assertEquals(15, result.getX(1), 0.0001);
        assertEquals(10, result.getY(1), 0.0001);
    }

    @Test
    public void testGetVect() {
        VectBuilder vect = new VectBuilder();
        LineString ls = new LineString(new VectList(1, 2, 3, 4, 5, 6));
        assertEquals(Vect.valueOf(1, 2), ls.getVect(0));
        assertEquals(Vect.valueOf(3, 4), ls.getVect(1));
        assertEquals(Vect.valueOf(5, 6), ls.getVect(2));
        
        ls.getVect(0, vect);
        assertEquals(new VectBuilder(1, 2), vect);
        ls.getVect(1, vect);
        assertEquals(new VectBuilder(3, 4), vect);
        ls.getVect(2, vect);
        assertEquals(new VectBuilder(5, 6), vect);
        
        try {
            ls.getVect(-1);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            ls.getVect(3);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
    }
    @Test
    public void testGetLine() {
        LineString ls = new LineString(new VectList(1, 2, 3, 4, 5, 6));
        assertEquals(Line.valueOf(1,2,3,4), ls.getLine(0));
        assertEquals(Line.valueOf(3,4,5,6), ls.getLine(1));
        try {
            ls.getLine(-1);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            ls.getLine(2);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        
    }

    @Test
    public void testGetXY() {
        LineString ls = new LineString(new VectList(1, 2, 3, 4, 5, 6));
        assertEquals(1, ls.getX(0), 0.0001);
        assertEquals(3, ls.getX(1), 0.0001);
        assertEquals(5, ls.getX(2), 0.0001);
        assertEquals(2, ls.getY(0), 0.0001);
        assertEquals(4, ls.getY(1), 0.0001);
        assertEquals(6, ls.getY(2), 0.0001);
        try {
            ls.getX(-1);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            ls.getX(3);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            ls.getY(-1);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            ls.getY(3);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
    }

    @Test
    public void testNumVects() {
        assertEquals(3, new LineString(new VectList(1, 2, 3, 4, 5, 6)).numVects());
        assertEquals(1, new LineString(new VectList(1, 2)).numVects());
    }

    @Test
    public void testNumLines() {
        assertEquals(2, new LineString(new VectList(1, 2, 3, 4, 5, 6)).numLines());
        assertEquals(0, new LineString(new VectList(1, 2)).numLines());
    }
}
