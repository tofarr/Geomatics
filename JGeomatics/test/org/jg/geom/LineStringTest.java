package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jg.util.SpatialNode;
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
    public void testValueOf(){
        assertEquals("[\"LS\", 0,0, 100,0, 100,100]", LineString.valueOf(Tolerance.DEFAULT, 0,0, 100,0, 100,100).toString());
        try{
            LineString.valueOf(Tolerance.DEFAULT, 1,2, 3);
            fail("Exception expected");
        }catch(IllegalArgumentException ex){
        }
        try{
            LineString.valueOf(Tolerance.DEFAULT, 1,Double.NEGATIVE_INFINITY, 3,4);
            fail("Exception expected");
        }catch(IllegalArgumentException ex){
        }
        try{
            LineString.valueOf(Tolerance.DEFAULT, 0,0);
            fail("Exception expected");
        }catch(IllegalArgumentException ex){
        }
        try{
            LineString.valueOf(Tolerance.DEFAULT, 40,0, 40,60, 0,20, 60,20);
            fail("Exception expected");
        }catch(IllegalArgumentException ex){
        }
        try{
            LineString.valueOf(Tolerance.DEFAULT);
            fail("Exception expected");
        }catch(IllegalArgumentException ex){
        }
        try{
            LineString.valueOf(null, 40,0, 40,60, 0,20, 60,20);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        try{
            LineString.valueOf(Tolerance.DEFAULT, (double[])null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        try{
            LineString.valueOf(Tolerance.DEFAULT, (VectList)null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
    }

    @Test
    public void testParseAll_ords() {
        assertEquals(0, LineString.parseAll(Tolerance.DEFAULT).length);
        assertEquals("[[\"LS\", 1,2, 3,5, 8,13]]", Arrays.toString(LineString.parseAll(Tolerance.DEFAULT, 1,2, 3,5, 8,13)));
        assertEquals("[[\"LS\", 0,20, 20,20], [\"LS\", 20,0, 20,20], [\"LS\", 20,20, 20,40, 40,40, 40,20, 20,20]]",
                Arrays.toString(LineString.parseAll(Tolerance.DEFAULT, 20,0, 20,40, 40,40, 40,20, 0,20)));
        assertEquals(0, LineString.parseAll(Tolerance.DEFAULT, 1,2, 1,2, 1,2).length);
        assertEquals("[[\"LS\", 1,2, 3,5, 8,13]]", Arrays.toString(LineString.parseAll(new Tolerance(0.5), 1,2, 1.1,2, 3,5, 8,13)));
    }
    
    @Test
    public void testParseAll_Network() {
        assertEquals(0, LineString.parseAll(Tolerance.DEFAULT, new Network()).length);
        assertEquals("[[\"LS\", 1,2, 3,5, 8,13]]",
                Arrays.toString(LineString.parseAll(Tolerance.DEFAULT, new Network().addAllLinks(new VectList(1,2, 3,5, 8,13)))));
        assertEquals("[[\"LS\", 0,20, 20,20], [\"LS\", 20,0, 20,20], [\"LS\", 20,20, 40,20, 40,40, 20,40, 20,20]]",
                Arrays.toString(LineString.parseAll(Tolerance.DEFAULT, new Network().addAllLinks(new VectList(20,0, 20,40, 40,40, 40,20, 0,20)))));
        Network network = new Network();
        network.addVertex(1, 2);
        assertEquals(0, LineString.parseAll(Tolerance.DEFAULT, network).length);
    }

    @Test
    public void testGetBounds() {
        LineString ls = LineString.valueOf(Tolerance.DEFAULT, 0, 0, 10, 0, 10, 10, 20, 10);
        assertEquals(Rect.valueOf(0, 0, 20, 10), ls.getBounds());
    }

    @Test
    public void testTransform() {
        Transform transform = new TransformBuilder().scale(2, 3).translate(-1, -2).build();
        LineString ls = LineString.valueOf(Tolerance.DEFAULT, 1, 3, 7, 13, 17, 29);
        assertSame(ls, ls.transform(Transform.IDENTITY));
        LineString transformed = ls.transform(transform);
        assertEquals(LineString.valueOf(Tolerance.DEFAULT, 1, 7, 13, 37, 33, 85), transformed);
        try {
            ls.transform(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        transform = new TransformBuilder().flipXAround(17).build();
        transformed = ls.transform(transform);
        assertEquals(LineString.valueOf(Tolerance.DEFAULT, 17, 29, 27, 13, 33, 3), transformed);
        ls = LineString.valueOf(Tolerance.DEFAULT, 10,10, 20,10, 20,20, 10,20, 10,10);
        transform = new TransformBuilder().flipY().build();
        transformed = ls.transform(transform);
        assertEquals(LineString.valueOf(Tolerance.DEFAULT, 10,-20, 20,-20, 20,-10, 10,-10, 10,-20), transformed);
        
        ls = LineString.valueOf(Tolerance.DEFAULT, 10,10, 10,20);
        transformed = ls.transform(transform);
        assertEquals(LineString.valueOf(Tolerance.DEFAULT, 10,-20, 10,-10), transformed);
    }
    
    @Test
    public void testSimplify(){
        LineString a = LineString.valueOf(Tolerance.DEFAULT, 1, 3, 7, 13, 17, 29);
        assertSame(a, a.simplify());
        LineString b = LineString.valueOf(Tolerance.DEFAULT, 1, 3, 7, 13);
        assertEquals(Line.valueOf(1, 3, 7, 13), b.simplify());
    }

    @Test
    public void testPathIterator() {
        LineString ls = LineString.valueOf(Tolerance.DEFAULT, 1, 3, 7, 13, 17, 29);
        PathIterator iter = ls.pathIterator();
        assertEquals(PathIterator.WIND_NON_ZERO, iter.getWindingRule());
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
        iter.next();
    }

    @Test
    public void testClone() {
        LineString ls = LineString.valueOf(Tolerance.DEFAULT, 1, 3, 7, 13, 17, 29);
        assertSame(ls, ls.clone());
    }

    @Test
    public void testToString_Appendable() {
        LineString ls = LineString.valueOf(Tolerance.DEFAULT, 1, 3, 7, 13, 17, 29);
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
        LineString ls = LineString.valueOf(Tolerance.DEFAULT, 20, 20, 20, 0, 0, 0, 0, 20, 20, 20);
        Network network = new Network();
        ls.addTo(network, Tolerance.FLATNESS, Tolerance.DEFAULT);
        assertEquals("[[0,0, 20,0, 20,20, 0,20, 0,0]]", network.toString());
    }
    
    @Test
    public void testToGeoShape(){
        LineString ls = LineString.valueOf(Tolerance.DEFAULT, 0,0, 10,0, 13,4, 0,4, 0,0);
        GeoShape gs = new GeoShape(null, new LineSet(ls), null);
        assertEquals(gs, ls.toGeoShape(Tolerance.FLATNESS, Tolerance.ZERO));
    }

    @Test
    public void testGetLength() {
        LineString ls = LineString.valueOf(Tolerance.DEFAULT, 0,0, 10,0, 13,4, 0,4, 0,0);
        assertEquals(32, ls.getLength(), 0.00001);
        assertEquals(0, LineString.getLength(new VectList()), 0.0001);
    }

    @Test
    public void testEquals() {
        LineString a = LineString.valueOf(Tolerance.DEFAULT, 0, 0, 100, 1, 100, 100);
        LineString b = LineString.valueOf(Tolerance.DEFAULT, 0, 0, 100, 0, 100, 100);
        assertEquals(a, a);
        assertNotEquals(a, b);
        assertNotEquals(a, "");
    }

    @Test
    public void testHashCode() {
        Set<Integer> hashCodes = new HashSet<>();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                LineString ls = LineString.valueOf(Tolerance.DEFAULT, 0, 0, x, y, 10, 10);
                hashCodes.add(ls.hashCode());
            }
        }
        assertEquals(100, hashCodes.size());
    }
    
    @Test
    public void testGetLineIndex() {
        //spiral
        final VectList vects = new VectList();
        final int diff = 5;
        final int max = 100;
        int j = 100;
        int i = 0;
        vects.add(i, i);
        while (i < j) {
            vects.add(i, j);
            vects.add(j, j);
            vects.add(j, i);
            vects.add(i + 5, i);
            i += diff;
            j -= diff;
        }
        vects.add(i, j);
        
        final Set<Line> lines = new HashSet<>();
        for(int n = vects.size()-1; n-- > 0;){
            Line line = vects.getLine(n);
            lines.add(line);
        }
        
        LineString a = LineString.valueOf(Tolerance.DEFAULT, vects);
        
        SpatialNode<Line> lineIndex = a.getLineIndex();
        assertEquals(Rect.valueOf(0, 0, max, max), lineIndex.getBounds());
        assertEquals(lines.size(), lineIndex.size());
        assertTrue(lineIndex.getDepth() <  Math.sqrt(max));
        lineIndex.forEach(new NodeProcessor<Line>(){
            @Override
            public boolean process(Rect bounds, Line value) {
                assertEquals(bounds, value.getBounds());
                assertTrue(lines.remove(value));
                return true;
            }
        
        });
        assertTrue(lines.isEmpty());
    }

    @Test
    public void testForInteractingLines() {
        LineString a = LineString.valueOf(Tolerance.DEFAULT, 0, 0, 0, 50, 50, 50, 50, 0, 10, 0, 10, 40, 40, 40, 40, 10, 20, 10, 20, 30, 30, 30, 30, 20);
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

        assertFalse(a.forInteractingLines(Rect.valueOf(0, 40, 10, 50), new NodeProcessor<Line>() {
            boolean done;
            @Override
            public boolean process(Rect bounds, Line value) {
                if(done){
                    fail("Already done");
                }
                return false;
            }

        }));

        assertTrue(map.isEmpty());
    }

    @Test
    public void testForOverlappingLines() {
        LineString a = LineString.valueOf(Tolerance.DEFAULT, 0, 0, 0, 50, 50, 50, 50, 0, 10, 0, 10, 40, 40, 40, 40, 10, 20, 10, 20, 30, 30, 30, 30, 20);
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
        LineString a = LineString.valueOf(Tolerance.DEFAULT, 0, 0, 0, 50, 50, 50, 50, 0, 10, 0, 10, 40, 40, 40, 40, 10, 20, 10, 20, 30, 30, 30, 30, 20);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
            out.writeObject(a);
        }
        LineString b;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = (LineString) in.readObject();
        }
        assertEquals(a, b);
    }

    @Test
    public void testBuffer_RightAngle() {
        Tolerance flatness = new Tolerance(0.5);
        LineString s = LineString.valueOf(Tolerance.DEFAULT, 20, 0, 20, 60, 60, 60);
        assertNull(s.buffer(-1, flatness, Tolerance.DEFAULT)); // buffer out of existance
        assertSame(s, s.buffer(0, flatness, Tolerance.DEFAULT)); // no op buffer

        Ring r = (Ring) s.buffer(5, flatness, Tolerance.DEFAULT);
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
        LineString s = LineString.valueOf(Tolerance.DEFAULT, 5,5, 25,5, 25,45, 5,45, 5,5);
        assertNull(s.buffer(-1, flatness, Tolerance.DEFAULT)); // buffer out of existance
        assertSame(s, s.buffer(0, flatness, Tolerance.DEFAULT)); // no op buffer

        Area r = (Area) s.buffer(5, flatness, Tolerance.DEFAULT);
        double area = (10 * 10 * 2)
                + (10 * 30 * 2)
                + (5 * 5 * 3 * 4)
                + (5 * 5 * Math.PI);
        assertEquals(area, r.getArea(), 1);
        assertEquals(Rect.valueOf(0,0,30,50), r.getBounds());
        assertNotNull(r.shell);
        assertEquals(2, r.numRings());
        assertEquals(2, r.getDepth());
    }

    @Test
    public void testBuffer_V() {
        Tolerance flatness = new Tolerance(0.5);
        LineString s = LineString.valueOf(Tolerance.DEFAULT, 20, 0, 20, 10, 19, 10.2, 20, 10.4, 20, 30);
        assertNull(s.buffer(-1, flatness, Tolerance.DEFAULT)); // buffer out of existance
        assertSame(s, s.buffer(0, flatness, Tolerance.DEFAULT)); // no op buffer

        Ring r = (Ring) s.buffer(5, flatness, Tolerance.DEFAULT);
        
        assertEquals(Rect.valueOf(14, -5, 25, 35), r.getBounds());
        assertEquals(382, r.getArea(), 1);
    }
    
    @Test
    public void testBuffer_Irregular() {
        Tolerance flatness = new Tolerance(0.5);
        LineString s = LineString.valueOf(Tolerance.DEFAULT, 10,5, 70,5, 70,23, 56,23, 56,13, 44,13, 44,9, 32,9);
        assertNull(s.buffer(-1, flatness, Tolerance.DEFAULT)); // buffer out of existance
        assertSame(s, s.buffer(0, flatness, Tolerance.DEFAULT)); // no op buffer

        Area r = (Area) s.buffer(4, flatness, Tolerance.DEFAULT);
        assertEquals(996, r.getArea(), 1);
        assertEquals(Rect.valueOf(6,1,74,27), r.getBounds());
        assertNotNull(r.shell);
        assertEquals(2, r.numRings());
        assertEquals(2, r.getDepth());
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

        result.clear();
        LineString.projectOutward(10,50, 10,10, 50,10, -5, flatness, Tolerance.DEFAULT, work, result);
        assertEquals(2, result.size());
        assertEquals(15, result.getX(0), 0.0001);
        assertEquals(10, result.getY(0), 0.0001);
        assertEquals(10, result.getX(1), 0.0001);
        assertEquals(15, result.getY(1), 0.0001);
        
        result.clear();
        LineString.projectOutward(50,10, 10,10, 10,50, -5, flatness, Tolerance.DEFAULT, work, result);
        assertTrue(2 < result.size());
        assertTrue(20 > result.size());
        assertEquals(Rect.valueOf(5, 5, 10, 10), result.getBounds());
    }

    @Test
    public void testGetPoint() {
        VectBuilder vect = new VectBuilder();
        LineString ls = new LineString(new VectList(1, 2, 3, 4, 5, 6));
        assertEquals(Vect.valueOf(1, 2), ls.getPoint(0));
        assertEquals(Vect.valueOf(3, 4), ls.getPoint(1));
        assertEquals(Vect.valueOf(5, 6), ls.getPoint(2));
        
        ls.getPoint(0, vect);
        assertEquals(new VectBuilder(1, 2), vect);
        ls.getPoint(1, vect);
        assertEquals(new VectBuilder(3, 4), vect);
        ls.getPoint(2, vect);
        assertEquals(new VectBuilder(5, 6), vect);
        
        try {
            ls.getPoint(-1);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            //expected
        }
        try {
            ls.getPoint(3);
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
    public void testNumPoints() {
        assertEquals(3, new LineString(new VectList(1, 2, 3, 4, 5, 6)).numPoints());
        assertEquals(1, new LineString(new VectList(1, 2)).numPoints());
    }

    @Test
    public void testNumLines() {
        assertEquals(2, new LineString(new VectList(1, 2, 3, 4, 5, 6)).numLines());
        assertEquals(0, new LineString(new VectList(1, 2)).numLines());
    }
    
    @Test
    public void testRelate(){
        LineString ls = new LineString(new VectList(0, 50, 50, 0, 100, 0, 100, 100));
        assertEquals(Relate.TOUCH, ls.relate(Vect.valueOf(0, 50), Tolerance.DEFAULT));
        assertEquals(Relate.TOUCH, ls.relate(new VectBuilder(25, 25), Tolerance.DEFAULT));
        assertEquals(Relate.TOUCH, ls.relate(Vect.valueOf(50, 0), Tolerance.DEFAULT));
        assertEquals(Relate.TOUCH, ls.relate(Vect.valueOf(75, 0), Tolerance.DEFAULT));
        assertEquals(Relate.TOUCH, ls.relate(Vect.valueOf(100, 0), Tolerance.DEFAULT));
        assertEquals(Relate.TOUCH, ls.relate(Vect.valueOf(100, 50), Tolerance.DEFAULT));
        assertEquals(Relate.TOUCH, ls.relate(Vect.valueOf(100, 100), Tolerance.DEFAULT));
        
        assertEquals(Relate.OUTSIDE, ls.relate(Vect.valueOf(1, 50), Tolerance.DEFAULT));
        assertEquals(Relate.OUTSIDE, ls.relate(new VectBuilder(24, 25), Tolerance.DEFAULT));
        assertEquals(Relate.OUTSIDE, ls.relate(Vect.valueOf(50, -1), Tolerance.DEFAULT));
        assertEquals(Relate.OUTSIDE, ls.relate(Vect.valueOf(75, 1), Tolerance.DEFAULT));
        assertEquals(Relate.OUTSIDE, ls.relate(Vect.valueOf(99, 1), Tolerance.DEFAULT));
        assertEquals(Relate.OUTSIDE, ls.relate(Vect.valueOf(99, 50), Tolerance.DEFAULT));
        assertEquals(Relate.OUTSIDE, ls.relate(Vect.valueOf(101, 100), Tolerance.DEFAULT));
        
        assertEquals(Relate.TOUCH, ls.relate(new VectBuilder(24, 25), new Tolerance(1)));
    }
    
    @Test
    public void testUnion() {
        LineString a = new LineString(new VectList(0, 90, 90,90, 90, 0)); //touch on point
        LineString b = new LineString(new VectList(0, 100, 100,100, 100, 0)); //touch on point
        LineString c = new LineString(new VectList(50,90, 140,0)); //touch on point
        GeoShape d = new GeoShape(Rect.valueOf(40, 80, 95, 95).toArea(), null, new PointSet(new VectList(10, 10, 30, 90, 30, 100, 100, 100)));
        
        assertEquals(a, a.union(a, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(new LineSet(a, b), a.union(b, Tolerance.FLATNESS, Tolerance.DEFAULT));
        
        LineSet e = new LineSet(
            new LineString(new VectList(0,90, 50,90)),
            new LineString(new VectList(50,90, 90,50)),
            new LineString(new VectList(50,90, 90,90, 90,50)),
            new LineString(new VectList(90,0, 90,50)),
            new LineString(new VectList(90,50, 140,0))
        );
        assertEquals(e, a.union(c, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(e, a.union(c.toLineSet(), Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(e, a.unionLineSet(c.toLineSet(), Tolerance.DEFAULT));
        
        LineSet f = new LineSet(
            new LineString(new VectList(0,100, 100,100, 100,40)),
            new LineString(new VectList(50,90, 100,40)),
            new LineString(new VectList(100,0, 100,40)),
            new LineString(new VectList(100,40, 140,0))
        );
        assertEquals(f, b.union(c, Tolerance.FLATNESS, Tolerance.DEFAULT));
        
        assertEquals("GEOMETRYCOLLECTION(POLYGON((40 80, 95 80, 95 95, 40 95, 40 80)),LINESTRING(0 90, 40 90),LINESTRING(90 0, 90 80),POINT(10 10),POINT(30 100),POINT(100 100))",
                a.union(d, Tolerance.FLATNESS, Tolerance.DEFAULT).toGeoShape(Tolerance.FLATNESS, Tolerance.ZERO).toWkt());
        assertEquals("GEOMETRYCOLLECTION(POLYGON((40 80, 95 80, 95 95, 40 95, 40 80)),LINESTRING(0 100, 100 100, 100 0),POINT(10 10),POINT(30 90))",
                b.union(d, Tolerance.FLATNESS, Tolerance.DEFAULT).toGeoShape(Tolerance.FLATNESS, Tolerance.ZERO).toWkt());
        assertEquals("GEOMETRYCOLLECTION(POLYGON((40 80, 95 80, 95 95, 40 95, 40 80)),LINESTRING(60 80, 140 0),POINT(10 10),POINT(30 90),POINT(30 100),POINT(100 100))",
                c.union(d, Tolerance.FLATNESS, Tolerance.DEFAULT).toGeoShape(Tolerance.FLATNESS, Tolerance.ZERO).toWkt());
        
        LineString g = new LineString(new VectList(0, 100, 100,100, 100, 200)); //touch on point
        assertEquals("MULTILINESTRING((0 90, 90 90, 90 0), (0 100, 100 100, 100 200))", a.union(g, Tolerance.FLATNESS, Tolerance.DEFAULT).toGeoShape(Tolerance.FLATNESS, Tolerance.ZERO).toWkt());
        assertEquals("MULTILINESTRING((0 90, 90 90, 90 0), (0 100, 100 100, 100 200))", a.unionLineSet(g.toLineSet(), Tolerance.DEFAULT).toGeoShape().toWkt());
    }

    @Test
    public void testIntersection() {
        LineString a = new LineString(new VectList(0, 90, 90,90, 90, 0)); //touch on point
        Rect b = Rect.valueOf(100,100,120,120); // disjoint
        LineString c = new LineString(new VectList(50,90, 50,50, 100,50)); //touch on point
        LineString d = new LineString(new VectList(50,90, 90,90, 90,50)); //touch on line
        Rect e = Rect.valueOf(80,80, 100,100);
        LineString f = new LineString(new VectList(10,10, 80,10, 80,80)); //disjoint but bounds not disjoint
        
        assertEquals(a, a.intersection(a, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertNull(a.intersection(b, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(new PointSet(new VectList(50,90, 90,50)), a.intersection(c, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(d, a.intersection(d, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(new LineString(new VectList(80,90, 90,90, 90,80)), a.intersection(e, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertNull(a.intersection(f, Tolerance.FLATNESS, Tolerance.ZERO));
    }

    @Test
    public void testLess() {
        LineString a = new LineString(new VectList(0, 90, 90,90, 90, 0)); //touch on point
        Rect b = Rect.valueOf(100,100,120,120); // disjoint
        LineString c = new LineString(new VectList(50,90, 50,50, 100,50)); //touch on point
        LineString d = new LineString(new VectList(50,90, 90,90, 90,50)); //touch on line
        Rect e = Rect.valueOf(80,80, 100,100);
        
        assertNull(a.less(a, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals(a, a.less(b, Tolerance.FLATNESS, Tolerance.DEFAULT));
        assertEquals("[\"LS\", 0,90, 50,90, 90,90, 90,50, 90,0]",
                a.less(c, Tolerance.FLATNESS, Tolerance.DEFAULT).toString());
        assertEquals("[\"LT\", [0,90, 50,90], [90,0, 90,50]]",
                a.less(d, Tolerance.FLATNESS, Tolerance.DEFAULT).toString());
        assertEquals("[\"LT\", [0,90, 80,90], [90,0, 90,80]]", a.less(e, Tolerance.FLATNESS, Tolerance.DEFAULT).toString());
    }
}
