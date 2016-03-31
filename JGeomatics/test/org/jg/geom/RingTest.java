package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jg.util.SpatialNode;
import org.jg.util.SpatialNode.NodeProcessor;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.TransformBuilder;
import org.jg.util.VectList;
import org.jg.util.VectSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofarrell
 */
public class RingTest {

    static final Tolerance TOL = Tolerance.DEFAULT;

    @Test
    public void testValueOf() {
        Ring ring = Ring.valueOf(TOL, 0, 0, 50, 100, 100, 0, 0, 0);
        assertEquals("[\"RG\", 0,0, 100,0, 50,100, 0,0]", ring.toString());
        assertEquals(5000, ring.getArea(), 0.0001);
        assertEquals(Rect.valueOf(0, 0, 100, 100), ring.getBounds());
        assertEquals(Vect.valueOf(50, 100.0 / 3), ring.getCentroid());
        assertEquals((Math.sqrt(12500) * 2) + 100, ring.getLength(), 0.0001);
        try {
            Ring.valueOf(TOL, 1, 2, 3);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        try {
            Ring.valueOf(TOL, 1, Double.NEGATIVE_INFINITY, 3, 4);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        try {
            Ring.valueOf(TOL, 0, 0);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        try {
            Ring.valueOf(TOL, 0, 0, 100, 100, 100, 0, 0, 100, 0, 0);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        try {
            Ring.valueOf(TOL);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        try {
            Ring.valueOf(null, 0, 0, 50, 100, 100, 0, 0, 0);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            LineString.valueOf(TOL, (double[]) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            LineString.valueOf(TOL, (VectList) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testParseAll() {
        Network network = new Network();
        assertEquals(0, Ring.parseAll(TOL, network).length);
        
        network.addAllLinks(new VectList(0, 0, 100, 100, 100, 0, 0, 100, 0, 0));
        Ring[] rings = Ring.parseAll(TOL, network);
        
        List<Ring> expected = new ArrayList<>();
        expected.add(Ring.valueOf(TOL, 0,0, 50,50, 0,100, 0,0));
        expected.add(Ring.valueOf(TOL, 100,0, 100,100, 50,50, 100,0));
        
        for(Ring ring : rings){
            assertTrue(expected.remove(ring));
        }
        assertTrue(expected.isEmpty());
    }

    @Test
    public void testGetArea_0args() {
        Ring ring = Ring.valueOf(TOL, 0,0, 100,0, 120,100, 20,100, 0,0);
        assertEquals(10000, ring.getArea(), 0.0001);
        assertEquals(10000, ring.getArea(), 0.0001);
    }

    @Test
    public void testGetLength() {
        Ring ring = Ring.valueOf(TOL, 0,0, 3,4, 3,14, 0,10, 0,0);
        assertEquals(30, ring.getLength(), 0.0001);
        assertEquals(30, ring.getLength(), 0.0001);
    }

    @Test
    public void testNumVects() {
        Ring ring = Ring.valueOf(TOL, 0,0, 3,4, 3,14, 0,10, 0,0);
        assertEquals(5, ring.numVects());
    }

    @Test
    public void testNumLines() {
        Ring ring = Ring.valueOf(TOL, 0,0, 3,4, 3,14, 0,10, 0,0);
        assertEquals(4, ring.numLines());
    }

    @Test
    public void testGetBounds() {
        Ring ring = Ring.valueOf(TOL, 0,0, 3,4, 3,14, 0,10, 0,0);
        Rect bounds = ring.getBounds();
        assertEquals(Rect.valueOf(0,0,3,14), bounds);
        assertSame(bounds, ring.getBounds());
    }

    @Test
    public void testAddBoundsTo() {
        Ring ring = Ring.valueOf(TOL, 0,0, 3,4, 3,14, 0,10, 0,0);
        RectBuilder builder = new RectBuilder().add(-1, -2);
        ring.addBoundsTo(builder);
        assertEquals(Rect.valueOf(-1,-2,3,14), builder.build());
    }

    @Test
    public void testRelate() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        assertEquals(Relate.TOUCH, ring.relate(Vect.ZERO, TOL));
        assertEquals(Relate.TOUCH, ring.relate(new VectBuilder(), TOL));
        assertEquals(Relate.TOUCH, ring.relate(Vect.valueOf(0, 5), TOL));
        assertEquals(Relate.TOUCH, ring.relate(Vect.valueOf(0, 10), TOL));
        assertEquals(Relate.TOUCH, ring.relate(Vect.valueOf(3, 4), TOL));
        assertEquals(Relate.TOUCH, ring.relate(Vect.valueOf(6, 8), TOL));
        assertEquals(Relate.TOUCH, ring.relate(Vect.valueOf(6, 12), TOL));
        assertEquals(Relate.TOUCH, ring.relate(Vect.valueOf(6, 14), TOL));
        assertEquals(Relate.TOUCH, ring.relate(Vect.valueOf(3, 12), TOL));
        assertEquals(Relate.OUTSIDE, ring.relate(Vect.valueOf(3, 1), TOL));
        assertEquals(Relate.OUTSIDE, ring.relate(Vect.valueOf(7, 12), TOL));
        assertEquals(Relate.INSIDE, ring.relate(Vect.valueOf(2, 3), TOL));
    }

    @Test
    public void testGetLineIndex() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        final Set<Line> expected = new HashSet<>(Arrays.asList(
                new Line(0,0,6,8),
                new Line(0,10,0,0),
                new Line(6,8,6,14),
                new Line(6,14,0,10)));
        SpatialNode<Line> lineIndex = ring.getLineIndex();
        assertSame(lineIndex, ring.getLineIndex());
        lineIndex.forEach(new NodeProcessor<Line>() {
            @Override
            public boolean process(Rect bounds, Line value) {
                assertTrue(expected.remove(value));
                assertEquals(bounds, value.getBounds());
                return true;
            }
        });
        assertTrue(expected.isEmpty());
    }
    
    @Test
    public void testGetCentroid() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        Vect centroid = ring.getCentroid();
        assertEquals(Vect.valueOf(2.75,7.75), centroid);
        assertSame(centroid, ring.getCentroid());
    }

    @Test
    public void testIsConvex() {
        VectList vects = new VectList(0,0, 6,8, 6,14, 0,10, 0,0);
        Ring ring = Ring.valueOf(TOL, vects);
        assertTrue(ring.isConvex());
        assertTrue(ring.isConvex());
        
        VectBuilder target = new VectBuilder();
        for(int i = 1; i < vects.size(); i++){
            Line line = vects.getLine(i-1);
            line.projectOutward(0.5, -0.8, TOL, target);
            VectList newVects = vects.clone();
            newVects.insert(i, target.build());
            Ring newRing = Ring.valueOf(TOL, newVects);
            assertFalse(newRing.isConvex());
        }
        
        //Star test!
        Network network = new Network();
        network.addAllLinks(new VectList(0,0, 6,4, -1,4, 5,0,  2.5,7, 0,0));
        Ring[] rings = Ring.parseAll(TOL, network); 
        assertFalse(rings[0].isConvex());
        assertTrue(rings[1].isConvex());
    }
    
    @Test
    public void testGetVect_int() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        assertEquals(Vect.valueOf(6,8), ring.getVect(1));
        try {
            ring.getVect(-1);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            ring.getVect(5);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    @Test
    public void testGetVect_int_VectBuilder() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        VectBuilder target = new VectBuilder();
        assertSame(target, ring.getVect(1, target));
        assertEquals(new VectBuilder(6,8), target);
        try {
            ring.getVect(1, null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            ring.getVect(-1, target);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            ring.getVect(5, target);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    @Test
    public void testGetLine() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        assertEquals(Line.valueOf(6,14,0,10), ring.getLine(2));
        try {
            ring.getLine(-1);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            ring.getLine(4);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    @Test
    public void testGetX() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        assertEquals(6, ring.getX(1), 0.0001);
        try {
            ring.getX(-1);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            ring.getX(5);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    @Test
    public void testGetY() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        assertEquals(8, ring.getY(1), 0.0001);
        try {
            ring.getX(-1);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            ring.getX(5);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    @Test
    public void testGetVects() {
        VectList vects = new VectList(0,0, 6,8, 6,14, 0,10, 0,0);
        Ring ring = Ring.valueOf(TOL, vects);
        VectList target = new VectList();
        ring.getVects(target);
        try {
            ring.getVects(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        assertEquals(vects, target);
    }

    @Test
    public void testAddTo_Network() {
        VectList vects = new VectList(0,0, 6,8, 6,14, 0,10, 0,0);
        Ring ring = Ring.valueOf(TOL, vects);
        Network network = new Network();
        network.addLink(0,0,1,1);
        ring.addTo(network);
        assertEquals("[[0,0, 0,10, 6,14, 6,8, 0,0],[0,0, 1,1]]", network.toString());
    }

    @Test
    public void testBuffer() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        assertSame(ring, ring.buffer(0, Tolerance.FLATNESS, TOL).shell);
        assertEquals(new Area(ring), ring.buffer(0, Tolerance.FLATNESS, TOL));
        Area a = ring.buffer(-1, Tolerance.FLATNESS, TOL);
        assertEquals(1, a.numRings());
        assertEquals(5, a.numVects());
        Rect bounds = a.getBounds();
        assertEquals(bounds.minX, 1, 0.01);
        assertEquals(bounds.minY, 3, 0.01);
        assertEquals(bounds.maxX, 5, 0.01);
        assertEquals(bounds.maxY, 12.125, 0.01);
        assertEquals(20.53, a.getArea(), 0.01);
        Area b = ring.buffer(10, Tolerance.FLATNESS, TOL);
        assertEquals(1, b.numRings());
        bounds = b.getBounds();
        assertEquals(bounds.minX, -10, 0.01);
        assertEquals(bounds.minY, -10, 0.01);
        assertEquals(bounds.maxX, 16, 0.01);
        assertEquals(bounds.maxY, 24, 0.01);
        assertEquals(694, b.getArea(), 1);
    }

    @Test
    public void testTransform() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        assertSame(ring, ring.transform(Transform.IDENTITY));
        try{
            ring.transform(null);
            fail("Exception Expected");
        }catch(NullPointerException ex){   
        }
        Transform transform = new TransformBuilder().flipXAround(7).build();
        Ring transformed = ring.transform(transform);
        Ring expected = Ring.valueOf(TOL, 8,8, 14,0, 14,10, 8,14, 8,8);
        assertEquals(expected, transformed);
    }

    @Test
    public void testPathIterator() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        PathIterator iter = ring.pathIterator();
        assertEquals(PathIterator.WIND_NON_ZERO, iter.getWindingRule());
        assertFalse(iter.isDone());
        double[] coords = new double[6];
        float[] fcoords = new float[6];
        assertEquals(PathIterator.SEG_MOVETO, iter.currentSegment(coords));
        assertEquals(PathIterator.SEG_MOVETO, iter.currentSegment(fcoords));
        assertEquals(0, coords[0], 0.00001);
        assertEquals(0, coords[1], 0.00001);
        assertEquals(0, fcoords[0], 0.00001);
        assertEquals(0, fcoords[1], 0.00001);
        iter.next();
        assertFalse(iter.isDone());
        assertEquals(PathIterator.SEG_LINETO, iter.currentSegment(coords));
        assertEquals(PathIterator.SEG_LINETO, iter.currentSegment(fcoords));
        assertEquals(6, coords[0], 0.00001);
        assertEquals(8, coords[1], 0.00001);
        assertEquals(6, fcoords[0], 0.00001);
        assertEquals(8, fcoords[1], 0.00001);
        iter.next();
        assertFalse(iter.isDone());
        assertEquals(PathIterator.SEG_LINETO, iter.currentSegment(coords));
        assertEquals(PathIterator.SEG_LINETO, iter.currentSegment(fcoords));
        assertEquals(6, coords[0], 0.00001);
        assertEquals(14, coords[1], 0.00001);
        assertEquals(6, fcoords[0], 0.00001);
        assertEquals(14, fcoords[1], 0.00001);
        iter.next();
        assertFalse(iter.isDone());
        assertEquals(PathIterator.SEG_LINETO, iter.currentSegment(coords));
        assertEquals(PathIterator.SEG_LINETO, iter.currentSegment(fcoords));
        assertEquals(0, coords[0], 0.00001);
        assertEquals(10, coords[1], 0.00001);
        assertEquals(0, fcoords[0], 0.00001);
        assertEquals(10, fcoords[1], 0.00001);
        iter.next();
        assertFalse(iter.isDone());
        assertEquals(PathIterator.SEG_CLOSE, iter.currentSegment(coords));
        assertEquals(PathIterator.SEG_CLOSE, iter.currentSegment(fcoords));
        assertEquals(0, coords[0], 0.00001);
        assertEquals(0, coords[1], 0.00001);
        assertEquals(0, fcoords[0], 0.00001);
        assertEquals(0, fcoords[1], 0.00001);
        iter.next();
        assertTrue(iter.isDone());
        iter.next();
    }

    @Test
    public void testClone() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        assertSame(ring, ring.clone());
    }

    @Test
    public void testToGeoShape() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        assertEquals(new GeoShape(new Area(ring), null, null), ring.toGeoShape(Tolerance.FLATNESS, TOL));
    }

    @Test
    public void testAddTo_3args() {
        Network network = new Network();
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        ring.addTo(network, Tolerance.FLATNESS, TOL);
        assertEquals("[[0,0, 6,8, 6,14, 0,10, 0,0]]", network.toString());
    }

    @Test
    public void testToArea() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        assertEquals(new Area(ring), ring.toArea());
    }

    @Test
    public void testUnion() {
        Ring a = new Ring(new VectList(0,0, 10,0, 10,10, 0,10, 0,0), null);
        Ring b = new Ring(new VectList(10,0, 20,0, 20,10, 10,10, 10,0), null);
        Ring c = new Ring(new VectList(20,0, 30,0, 30,10, 20,10, 20,0), null);
        Ring d = new Ring(new VectList(5,5, 15,5, 15,15, 5,15, 5,5), null);
        Ring e = new Ring(new VectList(0,0, 10,0, 20,0, 20,10, 10,10, 0,10, 0,0), null);
        Line f = Line.valueOf(5,5, 15,5);
        Area g = new Area(null, a.toArea(), c.toArea());
        Ring h = new Ring(new VectList(0,0, 10,0, 10,5, 15,5, 15,15, 5,15, 5,10, 0,10, 0,0), null);
        PointSet i = new PointSet(new VectList(5,0, 5,5, 10,0, 10,5, 12,5, 15,5, 20,5));
        GeoShape j = new GeoShape(a.toArea(), Line.valueOf(10,5, 15,5).toLineString().toLineSet(), null);
        GeoShape k = new GeoShape(a.toArea(), null, new PointSet(new VectList(12, 5, 15,5, 20,5)));
        GeoShape l = new GeoShape(null, Line.valueOf(10,5, 15,5).toLineString().toLineSet(), new PointSet(new VectList(5,0, 5,5, 10,0, 20,5)));
        GeoShape m = new GeoShape(a.toArea(), Line.valueOf(10,5, 15,5).toLineString().toLineSet(), new PointSet(new VectList(20,5)));
        Ring n = new Ring(new VectList(10,10, 20,10, 20,20, 10,20, 10,10), null);
        Area o = new Area(null, a.toArea(), n.toArea());
        Ring p = new Ring(new VectList(10,5, 20,5, 20,20, 10,20, 10,5), null);
        Ring q = new Ring(new VectList(0,0, 10,0, 10,5, 20,5, 20,20, 10,20, 10,10, 0,10, 0,0), null);
        
        assertEquals(a, a.union(a, Tolerance.FLATNESS, TOL));
        assertEquals(e, a.union(b, Tolerance.FLATNESS, TOL));
        assertEquals(g, a.union(c, Tolerance.FLATNESS, TOL));
        assertEquals(g, c.union(a, Tolerance.FLATNESS, TOL));
        assertEquals(h, a.union(d, Tolerance.FLATNESS, TOL));
        
        assertEquals(j, a.union(f, Tolerance.FLATNESS, TOL));
        assertEquals(k, a.union(i, Tolerance.FLATNESS, TOL));
        assertEquals(m, a.union(l, Tolerance.FLATNESS, TOL));
        
        assertEquals(e, e.union(a, Tolerance.FLATNESS, TOL));
        assertEquals(e, e.union(f, Tolerance.FLATNESS, TOL));
        
        assertEquals(o, a.union(n, Tolerance.FLATNESS, TOL));
        assertEquals(o, n.union(a, Tolerance.FLATNESS, TOL));
        
        assertEquals(q, a.union(p, Tolerance.FLATNESS, TOL));
        assertEquals(q, p.union(a, Tolerance.FLATNESS, TOL));
        
    }

    @Test
    public void testIntersection() {
        Ring a = new Ring(new VectList(0,0, 10,0, 10,10, 0,10, 0,0), null);
        Ring b = new Ring(new VectList(10,0, 20,0, 20,10, 10,10, 10,0), null);
        Ring c = new Ring(new VectList(10,10, 20,10, 20,20, 10,20, 10,10), null);
        Ring d = new Ring(new VectList(20,0, 30,0, 30,10, 20,10, 20,0), null);
        Ring e = new Ring(new VectList(5,5, 15,5, 15,15, 5,15, 5,5), null);
        
        assertEquals(a, a.intersection(a, Tolerance.FLATNESS, TOL));
        assertEquals(Line.valueOf(10, 0, 10, 10), a.intersection(b, Tolerance.FLATNESS, TOL));
        assertEquals(Vect.valueOf(10, 10), a.intersection(c, Tolerance.FLATNESS, TOL));
        assertNull(a.intersection(d, Tolerance.FLATNESS, TOL));
        assertEquals(new Ring(new VectList(5,5, 10,5, 10,10, 5,10, 5,5), null), a.intersection(e, Tolerance.FLATNESS, TOL));
        
        LineString f = LineString.valueOf(TOL, 10,5, 25,5, 25,0, 40,0);
        LineString g = LineString.valueOf(TOL, 20,5, 25,5, 25,0, 30,0);
        assertEquals(g, d.intersection(f, Tolerance.FLATNESS, TOL));
        
        PointSet h = PointSet.valueOf(new VectSet().add(25, 6).add(30, 6).add(30, 10).add(35, 5));
        PointSet i = PointSet.valueOf(new VectSet().add(25, 6).add(30, 6).add(30, 10));
        assertEquals(i, d.intersection(h, Tolerance.FLATNESS, TOL));
        
        GeoShape j = new GeoShape(null, f.toLineSet(), h);
        GeoShape k = new GeoShape(null, g.toLineSet(), i);
        assertEquals(k, d.intersection(j, Tolerance.FLATNESS, TOL));
        
        Ring l = new Ring(new VectList(0,0, 30,0, 30,10, 10,10, 10,20, 30,20, 30,30, 0,30, 0,0), null);
        Ring m = new Ring(new VectList(0,0, 10,0, 10,20, 20,20, 20,0, 30,0, 30,30, 0,30, 0,0), null);
        Area n = new Area(null, new Ring(new VectList(0,0, 10,0, 10,10, 10,20, 20,20, 30,20, 30,30, 0,30, 0,0), null).toArea(),
                new Ring(new VectList(20,0, 30,0, 30,10, 20,10, 20,0), null).toArea());
        assertEquals(n, l.intersection(m, Tolerance.FLATNESS, TOL));
    }

    @Test
    public void testLess() {
        Ring a = new Ring(new VectList(0,0, 10,0, 10,10, 0,10, 0,0), null);
        Ring b = new Ring(new VectList(10,0, 20,0, 20,10, 10,10, 10,0), null);
        Ring c = new Ring(new VectList(10,10, 20,10, 20,20, 10,20, 10,10), null);
        Ring d = new Ring(new VectList(20,0, 30,0, 30,10, 20,10, 20,0), null);
        Ring e = new Ring(new VectList(5,5, 15,5, 15,15, 5,15, 5,5), null);
        
        assertNull(a.less(a, Tolerance.FLATNESS, TOL));
        assertEquals(a, a.less(b, Tolerance.FLATNESS, TOL));
        assertEquals(a, a.less(c, Tolerance.FLATNESS, TOL));
        assertEquals(a, a.less(d, Tolerance.FLATNESS, TOL));
        assertEquals(new Ring(new VectList(0,0, 10,0, 10,5, 5,5, 5,10, 0,10, 0,0), null), a.less(e, Tolerance.FLATNESS, TOL));
        
        LineString f = LineString.valueOf(TOL, 10,5, 25,5, 25,0, 40,0);
        LineString g = LineString.valueOf(TOL, 20,5, 25,5, 25,0, 30,0);
        assertEquals(g, d.less(f, Tolerance.FLATNESS, TOL));
        
        PointSet h = PointSet.valueOf(new VectSet().add(25, 6).add(30, 6).add(30, 10).add(35, 5));
        PointSet i = PointSet.valueOf(new VectSet().add(25, 6).add(30, 6).add(30, 10));
        assertEquals(i, d.less(h, Tolerance.FLATNESS, TOL));
        
        GeoShape j = new GeoShape(null, f.toLineSet(), h);
        GeoShape k = new GeoShape(null, g.toLineSet(), i);
        assertEquals(k, d.less(j, Tolerance.FLATNESS, TOL));
        
        Ring l = new Ring(new VectList(0,0, 30,0, 30,10, 10,10, 10,20, 30,20, 30,30, 0,30, 0,0), null);
        Ring m = new Ring(new VectList(0,0, 10,0, 10,20, 20,20, 20,0, 30,0, 30,30, 0,30, 0,0), null);
        Area n = new Area(null, new Ring(new VectList(0,0, 10,0, 10,10, 10,20, 20,20, 30,20, 30,30, 0,30, 0,0), null).toArea(),
                new Ring(new VectList(20,0, 30,0, 30,10, 20,10, 20,0), null).toArea());
        assertEquals(n, l.less(m, Tolerance.FLATNESS, TOL));
    }

    @Test
    public void testHashCode() {
        Set<Integer> hashCodes = new HashSet<>();
        for (int x = 0; x <= 10; x++) {
            for (int y = 1; y < 10; y++) {
                Ring ring = new Ring(new VectList(0, 0, 10, 0, x, y, 0, 0), null);
                hashCodes.add(ring.hashCode());
            }
        }
        assertEquals(99, hashCodes.size());
    }

    @Test
    public void testEquals() {
        Ring a = new Ring(new VectList(0, 0, 100, 1, 100, 100, 0, 0), null);
        Ring b = new Ring(new VectList(0, 0, 100, 0, 100, 100, 0, 0), null);
        assertEquals(a, a);
        assertNotEquals(a, b);
        assertNotEquals(a, "");
    }

    @Test
    public void testToString_0args() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        assertEquals("[\"RG\", 0,0, 6,8, 6,14, 0,10, 0,0]", ring.toString());
        try {
            ring.toString(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            ring.toString(new Appendable() {
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
}
