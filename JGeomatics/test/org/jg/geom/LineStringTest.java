package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.jg.util.Network;
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
    public void testValueOf() {
        assertNull(LineString.valueOf(new VectList()));
        assertEquals("[3,7]", LineString.valueOf(new VectList().add(3, 7)).toString());
        try {
            LineString.valueOf((VectList)null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        assertEquals("[0,0, 10,0, 10,10, 20,10]", LineString.valueOf(new VectList().addAll(0, 0, 10, 0, 10, 10, 20, 10)).toString());
        assertEquals("[0,0, 10,0, 10,10, 20,10]", LineString.valueOf(0, 0, 10, 0, 10, 0, 10, 10, 20, 10).toString());
    }

    @Test
    public void testGetBounds() {
        LineString ls = LineString.valueOf(new VectList().addAll(0, 0, 10, 0, 10, 10, 20, 10));
        assertEquals(Rect.valueOf(0, 0, 20, 10), ls.getBounds());
    }

    @Test
    public void testAddBoundsTo() {
        RectBuilder bounds = new RectBuilder();
        LineString.valueOf(new VectList().addAll(1, 3, 7, 13)).addBoundsTo(bounds);
        LineString.valueOf(new VectList().addAll(5, 7, 11, 17)).addBoundsTo(bounds);
        assertEquals(new RectBuilder(1, 3, 11, 17), bounds);
        try {
            LineString.valueOf(new VectList().addAll(5, 7, 11, 17)).addBoundsTo(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testTransform() {
        Transform transform = new TransformBuilder().scale(2, 3).translate(-1, -2).build();
        LineString ls = LineString.valueOf(new VectList().addAll(1, 3, 7, 13, 17, 29));
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
        assertEquals("[1,3, 7,13, 17,29]", ls.toString());
        try{
            ls.toString(null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        try{
            ls.toString(new Appendable(){
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
        }catch(GeomException ex){
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
        LineString ls = LineString.valueOf(new VectList().addAll(0,0, 1,0, 1,10));
        assertTrue(ls.isValid(Tolerance.DEFAULT));
        assertFalse(ls.isValid(new Tolerance(1.1)));
        assertFalse(LineString.valueOf(1,3).isValid(Tolerance.DEFAULT));
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
        for(int x = 0; x < 10; x++){
            for(int y = 0; y < 10; y++){
                LineString ls = LineString.valueOf(0, 0, x, y, 10, 10);
                hashCodes.add(ls.hashCode());
            }
        }
        assertEquals(100, hashCodes.size());
    }

    @Test
    public void testGetInteractingLines() {
        LineString a = LineString.valueOf(0, 0, 0, 50, 50, 50, 50, 0, 10, 0, 10, 40, 40, 40, 40, 10, 20, 10, 20, 30, 30, 30, 30, 20);
        final Map<Rect, Line> map = new HashMap<>();
        map.put(Rect.valueOf(0, 0, 0, 50), Line.valueOf(0, 0, 0, 50));
        map.put(Rect.valueOf(0, 50, 50, 50), Line.valueOf(0, 50, 50, 50));
        
        map.put(Rect.valueOf(10, 0, 10, 40), Line.valueOf(10, 0, 10, 40));
        map.put(Rect.valueOf(10, 40, 40, 40), Line.valueOf(10, 40, 40, 40));
        
        assertTrue(a.getInteractingLines(Rect.valueOf(0, 40, 10, 50), new NodeProcessor<Line>(){
            @Override
            public boolean process(Rect bounds, Line value) {
                assertEquals(map.remove(bounds), value);
                return true;
            }
        
        }));
        assertTrue(map.isEmpty());
    }

    @Test
    public void testGetOverlappingLines() {
        LineString a = LineString.valueOf(0, 0, 0, 50, 50, 50, 50, 0, 10, 0, 10, 40, 40, 40, 40, 10, 20, 10, 20, 30, 30, 30, 30, 20);
        final Map<Rect, Line> map = new HashMap<>();
        map.put(Rect.valueOf(0, 50, 50, 50), Line.valueOf(0, 50, 50, 50));
        
        assertTrue(a.getOverlappingLines(Rect.valueOf(0, 40, 10, 51), new NodeProcessor<Line>(){
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
    public void testBuffer() {
        Tolerance tolerance = new Tolerance(0.5);
        LineString s = LineString.valueOf(37, 61);
        assertNull(s.buffer(-1, tolerance)); // buffer out of existance
        assertSame(s, s.buffer(0, tolerance)); // no op buffer
        
        // buffer a point
        Geom g = s.buffer(5, tolerance); 
        RingSet ringSet = (RingSet)g;
        Rect bounds = ringSet.getBounds();
        assertEquals(32, bounds.minX, 0.5);
        assertEquals(56, bounds.minY, 0.5);
        assertEquals(42, bounds.maxX, 0.5);
        assertEquals(66, bounds.maxY, 0.5);
        assertEquals(Math.PI * 25, ringSet.getArea(), 0.6);
        assertEquals(Math.PI * 10, ringSet.ring.getLength(), 0.5);
        
        //buffer self crossing string
        s = LineString.valueOf(20, 0, 20, 60, 60, 60, 60, 20, 0, 20);
        Geom c = s.buffer(5, tolerance);
        System.out.println(c);
        
    }

    /**
     * Test of projectOutward method, of class LineString.
     */
    @Test
    public void testProjectOutward() {
//        System.out.println("projectOutward");
//        double ax = 0.0;
//        double ay = 0.0;
//        double bx = 0.0;
//        double by = 0.0;
//        double cx = 0.0;
//        double cy = 0.0;
//        double amt = 0.0;
//        Tolerance tolerance = null;
//        VectBuilder work = null;
//        VectList result_2 = null;
//        LineString.projectOutward(ax, ay, bx, by, cx, cy, amt, tolerance, work, result_2);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVect method, of class LineString.
     */
    @Test
    public void testGetVect_int() {
        System.out.println("getVect");
        int index = 0;
        LineString instance = null;
        Vect expResult = null;
        Vect result = instance.getVect(index);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getVect method, of class LineString.
     */
    @Test
    public void testGetVect_int_VectBuilder() {
        System.out.println("getVect");
        int index = 0;
        VectBuilder target = null;
        LineString instance = null;
        VectBuilder expResult = null;
        VectBuilder result = instance.getVect(index, target);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLine method, of class LineString.
     */
    @Test
    public void testGetLine() {
        System.out.println("getLine");
        int index = 0;
        LineString instance = null;
        Line expResult = null;
        Line result = instance.getLine(index);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getX method, of class LineString.
     */
    @Test
    public void testGetX() {
        System.out.println("getX");
        int index = 0;
        LineString instance = null;
        double expResult = 0.0;
        double result = instance.getX(index);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getY method, of class LineString.
     */
    @Test
    public void testGetY() {
        System.out.println("getY");
        int index = 0;
        LineString instance = null;
        double expResult = 0.0;
        double result = instance.getY(index);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of numVects method, of class LineString.
     */
    @Test
    public void testNumVects() {
        System.out.println("numVects");
        LineString instance = null;
        int expResult = 0;
        int result = instance.numVects();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of numLines method, of class LineString.
     */
    @Test
    public void testNumLines() {
        System.out.println("numLines");
        LineString instance = null;
        int expResult = 0;
        int result = instance.numLines();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isEmpty method, of class LineString.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");
        LineString instance = null;
        boolean expResult = false;
        boolean result = instance.isEmpty();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
