package org.jg.geom;

import java.awt.geom.PathIterator;
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
import org.jg.util.VectList;
import org.jg.util.VectSet;
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
    public void testRelate_Vect() {
        Tolerance tolerance = new Tolerance(0.15);
        assertEquals(Relation.TOUCH, Vect.valueOf(2, 3).relate(Vect.valueOf(2, 3), tolerance));
        assertEquals(Relation.TOUCH, Vect.valueOf(2, 3).relate(Vect.valueOf(2.1, 3), tolerance));
        assertEquals(Relation.TOUCH, Vect.valueOf(2, 3).relate(new VectBuilder(2, 3.1), tolerance));
        assertEquals(Relation.TOUCH, Vect.valueOf(2, 3).relate(new VectBuilder(2.1, 3.1), tolerance));
        assertEquals(Relation.DISJOINT, Vect.valueOf(2, 3).relate(Vect.valueOf(2, 3.2), tolerance));
        assertEquals(Relation.DISJOINT, Vect.valueOf(2, 3).relate(Vect.valueOf(2.2, 3), tolerance));
        assertEquals(Relation.DISJOINT, Vect.valueOf(2, 3).relate(new VectBuilder(2.2, 3.2), tolerance));
        assertEquals(Relation.DISJOINT, Vect.valueOf(2, 3).relate(new VectBuilder(-2, 3), tolerance));
        assertEquals(Relation.DISJOINT, Vect.valueOf(2, 3).relate(Vect.valueOf(2, -3), tolerance));
    }

    @Test
    public void testRelate_Geom() {
        Tolerance tolerance = new Tolerance(0.15);
        assertEquals(Relation.TOUCH, Vect.valueOf(10, 20).relate(Vect.valueOf(10, 20), Linearizer.DEFAULT, tolerance));
        assertEquals(Relation.DISJOINT, Vect.valueOf(10, 20).relate(Vect.valueOf(10, 21), Linearizer.DEFAULT, tolerance));
        assertEquals(Relation.TOUCH, Vect.valueOf(10, 20).relate(Vect.valueOf(10.5, 20.5), Linearizer.DEFAULT, new Tolerance(1)));
        assertEquals(Relation.DISJOINT, Vect.valueOf(10, 20).relate(Rect.valueOf(11, 20, 15, 25), Linearizer.DEFAULT, tolerance));
        assertEquals(Relation.TOUCH | Relation.B_OUTSIDE_A, Vect.valueOf(10, 20).relate(Rect.valueOf(11, 20, 15, 25), Linearizer.DEFAULT, new Tolerance(1)));
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
        assertEquals("[\"PT\",0,0]", Vect.ZERO.toString());
        assertEquals("[\"PT\",3.4,5.6]", Vect.valueOf(3.4, 5.6).toString());
    }

    @Test
    public void testToString_double_double() {
        StringBuilder str = new StringBuilder();
        Vect.valueOf(3.4, 5.6).toString(str);
        assertEquals("[\"PT\",3.4,5.6]", str.toString());
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
        assertEquals("[\"PT\",1,2.3]", str.toString());
        try{
            v.toString(new Appendable(){
                @Override
                public Appendable append(CharSequence csq) throws IOException {
                    throw new IOException("SomeMsg");
                }

                @Override
                public Appendable append(CharSequence csq, int start, int end) throws IOException {
                    throw new IOException("SomeMsg");
                }

                @Override
                public Appendable append(char c) throws IOException {
                    throw new IOException("SomeMsg");
                }
            });
            fail("Exception expected");
        }catch(GeomException ex){
        }
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
            Vect.read(new DataInputStream(new InputStream(){
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
        Vect vect = Vect.valueOf(1, 2);
        Rect bounds = vect.getBounds();
        assertEquals(Rect.valueOf(1,2,1,2), bounds);
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
        vect.addTo(network, Linearizer.DEFAULT, Tolerance.DEFAULT);
        assertEquals("[[3,7]]", network.toString());
        vect.addTo(network, Linearizer.DEFAULT, Tolerance.DEFAULT);
        assertEquals("[[3,7]]", network.toString());
    }
    
    @Test
    public void testBuffer(){
        Vect vect = Vect.valueOf(3, 7);
        Ring buffered = (Ring)vect.buffer(2, new Linearizer(0.5, Tolerance.DEFAULT), Tolerance.DEFAULT);
        assertEquals(Rect.valueOf(1, 5, 5, 9), buffered.getBounds());
        assertEquals(Math.PI * 4, buffered.getArea(), 1.5);
        assertEquals(Math.PI * 4, buffered.getLength(), 0.5);
        
        buffered = (Ring)vect.buffer(2, Linearizer.DEFAULT, Tolerance.DEFAULT);
        Rect bounds = buffered.getBounds();
        assertEquals(1, bounds.minX, 0.0001);
        assertEquals(5, bounds.minY, 0.0001);
        assertEquals(5, bounds.maxX, 0.0001);
        assertEquals(9, bounds.maxY, 0.0001);
        assertEquals(Math.PI * 4, buffered.getArea(), 0.3);
        assertEquals(Math.PI * 4, buffered.getLength(), 0.1);
        
        assertNull(vect.buffer(-1, new Linearizer(0.1, Tolerance.DEFAULT), Tolerance.DEFAULT));
        assertSame(vect, vect.buffer(0, new Linearizer(0.1, Tolerance.DEFAULT), Tolerance.DEFAULT));
        
        buffered = (Ring)vect.buffer(2, new Linearizer(1.0, Tolerance.DEFAULT), Tolerance.DEFAULT);
        bounds = buffered.getBounds();
        assertEquals(1, bounds.minX, 0.0001);
        assertEquals(5, bounds.minY, 0.0001);
        assertEquals(5, bounds.maxX, 0.0001);
        assertEquals(9, bounds.maxY, 0.0001);
        assertEquals(4, buffered.numLines());
  
        Linearizer linearizer = new Linearizer(20.0, Tolerance.DEFAULT);
        assertSame(vect, vect.buffer(2, linearizer, Tolerance.DEFAULT));
    }
    
    @Test
    public void testUnion(){
        Rect rect = Rect.valueOf(10, 20, 30, 40);
        assertSame(rect, Vect.valueOf(20, 30).union(rect, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertSame(rect, Vect.valueOf(10, 30).union(rect, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertSame(rect, Vect.valueOf(15, 20).union(rect, Linearizer.DEFAULT, Tolerance.DEFAULT));
        
        Vect a = Vect.valueOf(5, 30);
        assertEquals("[\"GS\",[\"AR\"[[10,20, 30,20, 30,40, 10,40, 10,20]]],[\"PS\", 5,30]]", a.union(rect, Linearizer.DEFAULT, Tolerance.DEFAULT).toString());
        assertEquals(a, a.union(a, Linearizer.DEFAULT, Tolerance.DEFAULT));
        
        Vect b = Vect.valueOf(10, 25);
        PointSet c = new PointSet(new VectList(5, 30, 10,25));
        assertEquals(c, a.union(b, Linearizer.DEFAULT, Tolerance.DEFAULT));
        
        assertEquals(new PointSet(new VectList(5, 30, 10, 25, 15, 35)), Vect.valueOf(15, 35).union(c, Linearizer.DEFAULT, Tolerance.DEFAULT));
        
        GeoShape gs = b.toGeoShape(Linearizer.DEFAULT, Tolerance.DEFAULT);
        assertSame(gs, b.union(gs, Linearizer.DEFAULT, Tolerance.DEFAULT));
        
        gs = PointSet.valueOf(new VectSet().add(10, 20).add(10, 30)).toGeoShape(Linearizer.DEFAULT, Tolerance.DEFAULT);
        assertEquals("[\"PS\", 10,20, 10,25, 10,30]", b.union(gs, Linearizer.DEFAULT, Tolerance.DEFAULT).toString());
        
        gs = PointSet.valueOf(new VectSet().add(10, 20).add(10, 25)).toGeoShape(Linearizer.DEFAULT, Tolerance.DEFAULT);
        assertSame(gs, b.union(gs, Linearizer.DEFAULT, Tolerance.DEFAULT));
        
        PointSet mp = PointSet.valueOf(new VectSet().add(10, 20).add(10, 25));
        assertSame(mp, b.union(mp, Linearizer.DEFAULT, Tolerance.DEFAULT));
    }
    
    @Test
    public void testIntersection(){
        Rect rect = Rect.valueOf(10, 20, 30, 40);
        Vect a = Vect.valueOf(20, 30);
        assertSame(a, a.intersection(rect, Linearizer.DEFAULT, Tolerance.DEFAULT));
        a = Vect.valueOf(10, 30);
        assertSame(a, a.intersection(rect, Linearizer.DEFAULT, Tolerance.DEFAULT));
        a = Vect.valueOf(15, 20);
        assertSame(a, a.intersection(rect, Linearizer.DEFAULT, Tolerance.DEFAULT));
        
        a = Vect.valueOf(5, 30);
        assertNull(a.intersection(rect, Linearizer.DEFAULT, Tolerance.DEFAULT));
    }
    
    @Test
    public void testLess(){
        Rect rect = Rect.valueOf(10, 20, 30, 40);
        Vect a = Vect.valueOf(20, 20);
        assertNull(a.less(rect, Linearizer.DEFAULT, Tolerance.DEFAULT));
        a = Vect.valueOf(10, 30);
        assertNull( a.less(rect, Linearizer.DEFAULT, Tolerance.DEFAULT));
        a = Vect.valueOf(20, 30);
        assertNull(a.less(rect, Linearizer.DEFAULT, Tolerance.DEFAULT));
        
        a = Vect.valueOf(5, 30);
        assertSame(a, a.less(rect, Linearizer.DEFAULT, Tolerance.DEFAULT));
    }
    
    @Test
    public void testXor(){
        Rect rect = Rect.valueOf(10, 20, 30, 40);
        Vect a = Vect.valueOf(20, 20);
        assertNull(a.xor(a, Linearizer.DEFAULT, Tolerance.DEFAULT));
        assertSame(rect, a.xor(rect, Linearizer.DEFAULT, Tolerance.DEFAULT));
        a = Vect.valueOf(10, 30);
        assertSame(rect, a.xor(rect, Linearizer.DEFAULT, Tolerance.DEFAULT));
        a = Vect.valueOf(20, 30);
        assertSame(rect, a.xor(rect, Linearizer.DEFAULT, Tolerance.DEFAULT));
        Vect b = Vect.valueOf(5, 30);
        assertEquals(new GeoShape(rect.toArea(), null, new PointSet(new VectList(5,30))), b.xor(rect, Linearizer.DEFAULT, Tolerance.DEFAULT)); // shoul
        assertEquals(new PointSet(new VectList(5,30,20,20)), a.xor(b, Linearizer.DEFAULT, Tolerance.DEFAULT));
    }
    
    @Test
    public void testGetArea(){
        assertEquals(0, Vect.valueOf(20, 30).getArea(Linearizer.DEFAULT, Tolerance.DEFAULT), 0);
    }
}
