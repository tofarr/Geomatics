package org.geomatics.geom;

import org.geomatics.geom.LineString;
import org.geomatics.geom.Geom;
import org.geomatics.geom.GeoShape;
import org.geomatics.geom.RectBuilder;
import org.geomatics.geom.PathIter;
import org.geomatics.geom.Line;
import org.geomatics.geom.VectBuilder;
import org.geomatics.geom.Linearizer;
import org.geomatics.geom.PointSet;
import org.geomatics.geom.Network;
import org.geomatics.geom.Area;
import org.geomatics.geom.PathSegType;
import org.geomatics.geom.Relation;
import org.geomatics.geom.Vect;
import org.geomatics.geom.Rect;
import org.geomatics.geom.Ring;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.geomatics.util.SpatialNode;
import org.geomatics.util.SpatialNode.NodeProcessor;
import org.geomatics.util.Tolerance;
import org.geomatics.util.Transform;
import org.geomatics.util.TransformBuilder;
import org.geomatics.util.VectList;
import org.geomatics.util.VectSet;
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
        assertEquals("[\"RG\",0,0, 100,0, 50,100, 0,0]", ring.toString());
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
            Ring.valueOf(TOL, (double[]) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            Ring.valueOf(TOL, (VectList) null);
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
    public void testParseAll_B() {
        Network network = new Network();
        assertEquals(0, Ring.parseAll(TOL, network).length);
        
        network.addAllLinks(new VectList(0,0, 100,0, 100,100, 60,100, 20,60, 20,100, 50,70, 0,20, 0,0));
        Ring[] rings = Ring.parseAll(TOL, network);
        
        List<Ring> expected = new ArrayList<>();
        expected.add(Ring.valueOf(TOL, 20,60, 40,80, 20,100, 20,60));
        expected.add(Ring.valueOf(TOL, 0,0, 100,0, 100,100, 60,100, 40,80, 50,70, 0,20, 0,0));
        
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
        assertEquals(5, ring.numPoints());
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
    public void testRelate_Vect() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ring.relate(Vect.ZERO, TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ring.relate(new VectBuilder(), TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ring.relate(Vect.valueOf(0, 5), TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ring.relate(Vect.valueOf(0, 10), TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ring.relate(Vect.valueOf(3, 4), TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ring.relate(Vect.valueOf(6, 8), TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ring.relate(Vect.valueOf(6, 12), TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ring.relate(Vect.valueOf(6, 14), TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, ring.relate(Vect.valueOf(3, 12), TOL));
        assertEquals(Relation.DISJOINT, ring.relate(Vect.valueOf(3, 1), TOL));
        assertEquals(Relation.DISJOINT, ring.relate(Vect.valueOf(7, 12), TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, ring.relate(Vect.valueOf(2, 3), TOL));
    }

    @Test
    public void testRelate() {
        Ring a = Ring.valueOf(TOL, 0,0, 100,0, 0,100, 0,0);
        Ring b = Ring.valueOf(TOL, 0,100, 100,0, 100,100, 0,100);
        Ring c = Ring.valueOf(TOL, 100,0, 200,0, 100,100, 100,0);
        Ring d = Ring.valueOf(TOL, 5,5, 10,5, 5,10, 5,5);
        Ring e = Ring.valueOf(TOL, 0,0, 100,0, 100,100, 0,0);

        assertEquals(Relation.TOUCH | Relation.A_INSIDE_B | Relation.B_INSIDE_A, a.relate(a, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B | Relation.B_OUTSIDE_A, a.relate(b, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B | Relation.B_OUTSIDE_A, a.relate(c, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.A_INSIDE_B | Relation.B_INSIDE_A, a.relate(d, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.ALL, a.relate(e, Linearizer.DEFAULT, TOL));
        
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
        assertEquals(5, rings.length);
        for(Ring r : rings){
            assertTrue(r.isConvex());
        }
        
        assertFalse(Ring.valueOf(TOL, 0,0, 100,0, 10,10, 0,100, 0,0).isConvex());
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
        assertSame(ring, ring.buffer(0, Linearizer.DEFAULT, TOL));
        Ring a = (Ring)ring.buffer(-1, Linearizer.DEFAULT, TOL);
        assertEquals(5, a.numPoints());
        Rect bounds = a.getBounds();
        assertEquals(bounds.minX, 1, 0.01);
        assertEquals(bounds.minY, 3, 0.01);
        assertEquals(bounds.maxX, 5, 0.01);
        assertEquals(bounds.maxY, 12.125, 0.01);
        assertEquals(20.53, a.getArea(), 0.01);
        Ring b = (Ring)ring.buffer(10, Linearizer.DEFAULT, TOL);
        bounds = b.getBounds();
        assertEquals(bounds.minX, -10, 0.01);
        assertEquals(bounds.minY, -10, 0.01);
        assertEquals(bounds.maxX, 16, 0.01);
        assertEquals(bounds.maxY, 24, 0.01);
        assertEquals(694, b.getArea(), 10);
        assertNull(ring.buffer(-15, Linearizer.DEFAULT, TOL));
    } 
    
    @Test
    public void testBuffer_L(){
        Ring a = Ring.valueOf(TOL, 0,0, 50,0, 50,10, 10,10, 10,50, 0,50, 0,0);
        
        GeoShape result = (GeoShape)a.buffer(-5, Linearizer.DEFAULT, TOL);
        assertEquals(5.49, result.area.getArea(), 0.01);
        assertEquals(result.area.numRings(), 1);
        Rect bounds = result.area.getBounds();
        assertEquals(bounds.minX, 5, 0.01);
        assertEquals(bounds.minY, 5, 0.01);
        assertEquals(bounds.maxX, 10, 0.01);
        assertEquals(bounds.maxY, 10, 0.01);
        assertEquals(2, result.lines.numLineStrings());
        assertNull(result.points);

        assertNull(a.buffer(-8, Linearizer.DEFAULT, TOL)); // leaves nothing
        assertNull(a.buffer(-20, Linearizer.DEFAULT, TOL)); // leaves nothing
        
        Ring ring = (Ring) a.buffer(10, Linearizer.DEFAULT, TOL);
        assertEquals(3190, ring.getArea(), 1);
        assertEquals(Rect.valueOf(-10, -10, 60, 60), ring.getBounds());
    }

    
    @Test
    public void testBuffer_toLine(){
        Ring a = Ring.valueOf(TOL, 30,40, 40,40, 40,70, 30,70, 30,40);
        assertNull(a.buffer(-6, Linearizer.DEFAULT, TOL));
        Geom b = a.buffer(-5, Linearizer.DEFAULT, TOL);
        assertEquals(Line.valueOf(35,45, 35,65), b);
    }
    
    @Test
    public void testBuffer_connectedBlobs(){
        Ring ring = Ring.valueOf(TOL, 0,0, 50,0, 50,25, 30,25, 30,10, 10,10, 10,30, 25,30, 25,50, 0,50, 0,0);
        GeoShape result = (GeoShape)ring.buffer(-5, Linearizer.DEFAULT, TOL);
        assertTrue(Rect.valueOf(5,5,45,45).match(result.getBounds(), TOL));
        assertEquals(2, result.lines.numLineStrings());
        assertNull(result.area.shell);
        assertEquals(3, result.area.numChildren());
        assertEquals(3, result.area.numRings());
        assertEquals(316.5, result.area.getArea(), 1);
        
    }
    
    @Test
    public void testBuffer_toDeath(){
        Ring ring = Ring.valueOf(TOL, -40,-30, 40,-30, 40,30, -40,30, -40,-30);
        assertNull(ring.buffer(-40, Linearizer.DEFAULT, TOL));
        ring = Ring.valueOf(TOL, 90,0, 100,0, 100,10, 90,0);
        assertNull(ring.buffer(-40, Linearizer.DEFAULT, TOL));
        ring = Ring.valueOf(TOL, 0,0, 200,0, 200,10, 191,1, 9,1, 0,10, 0,0);
        assertNull(ring.buffer(-40, Linearizer.DEFAULT, TOL));
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
        
        transform = new TransformBuilder().translate(100, 200).build();
        transformed = ring.transform(transform);
        expected = Ring.valueOf(TOL, 100,200, 106,208, 106,214, 100,210, 100,200);
        assertEquals(expected, transformed);
    }

    @Test
    public void testPathIterator() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        PathIter iter = ring.iterator();
        assertFalse(iter.isDone());
        double[] coords = new double[6];
        LineSetTest.assertPath(iter, PathSegType.MOVE, 0, 0);
        iter.next();
        LineSetTest.assertPath(iter, PathSegType.LINE, 6, 8);
        iter.next();
        LineSetTest.assertPath(iter, PathSegType.LINE, 6, 14);
        iter.next();
        LineSetTest.assertPath(iter, PathSegType.LINE, 0, 10);
        iter.next();
        LineSetTest.assertPath(iter, PathSegType.CLOSE, 0, 0);
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
        assertEquals(new GeoShape(new Area(ring), null, null), ring.toGeoShape(Linearizer.DEFAULT, TOL));
    }

    @Test
    public void testAddTo_3args() {
        Network network = new Network();
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        ring.addTo(network, Linearizer.DEFAULT, TOL);
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
        
        assertEquals(a, a.union(a, Linearizer.DEFAULT, TOL));
        assertEquals(e, a.union(b, Linearizer.DEFAULT, TOL));
        assertEquals(g, a.union(c, Linearizer.DEFAULT, TOL));
        assertEquals(g, c.union(a, Linearizer.DEFAULT, TOL));
        assertEquals(h, a.union(d, Linearizer.DEFAULT, TOL));
        
        assertEquals(j, a.union(f, Linearizer.DEFAULT, TOL));
        assertEquals(k, a.union(i, Linearizer.DEFAULT, TOL));
        assertEquals(m, a.union(l, Linearizer.DEFAULT, TOL));
        
        assertEquals(e, e.union(a, Linearizer.DEFAULT, TOL));
        assertEquals(e, e.union(f, Linearizer.DEFAULT, TOL));
        
        assertEquals(o, a.union(n, Linearizer.DEFAULT, TOL));
        assertEquals(o, n.union(a, Linearizer.DEFAULT, TOL));
        
        assertEquals(q, a.union(p, Linearizer.DEFAULT, TOL));
        assertEquals(q, p.union(a, Linearizer.DEFAULT, TOL));
        
    }

    @Test
    public void testIntersection() {
        Ring a = new Ring(new VectList(0,0, 10,0, 10,10, 0,10, 0,0), null);
        Ring b = new Ring(new VectList(10,0, 20,0, 20,10, 10,10, 10,0), null);
        Ring c = new Ring(new VectList(10,10, 20,10, 20,20, 10,20, 10,10), null);
        Ring d = new Ring(new VectList(20,0, 30,0, 30,10, 20,10, 20,0), null);
        Ring e = new Ring(new VectList(5,5, 15,5, 15,15, 5,15, 5,5), null);
        
        assertEquals(a, a.intersection(a, Linearizer.DEFAULT, TOL));
        assertEquals(Line.valueOf(10, 0, 10, 10), a.intersection(b, Linearizer.DEFAULT, TOL));
        assertEquals(Vect.valueOf(10, 10), a.intersection(c, Linearizer.DEFAULT, TOL));
        assertNull(a.intersection(d, Linearizer.DEFAULT, TOL));
        assertEquals(new Ring(new VectList(5,5, 10,5, 10,10, 5,10, 5,5), null), a.intersection(e, Linearizer.DEFAULT, TOL));
        
        LineString f = LineString.valueOf(TOL, 10,5, 25,5, 25,0, 40,0);
        LineString g = LineString.valueOf(TOL, 20,5, 25,5, 25,0, 30,0);
        assertEquals(g, d.intersection(f, Linearizer.DEFAULT, TOL));
        
        PointSet h = PointSet.valueOf(new VectSet().add(25, 6).add(30, 6).add(30, 10).add(35, 5));
        PointSet i = PointSet.valueOf(new VectSet().add(25, 6).add(30, 6).add(30, 10));
        assertEquals(i, d.intersection(h, Linearizer.DEFAULT, TOL));
        
        GeoShape j = new GeoShape(null, f.toLineSet(), h);
        GeoShape k = new GeoShape(null, g.toLineSet(), i);
        assertEquals(k, d.intersection(j, Linearizer.DEFAULT, TOL));
        
        Ring l = new Ring(new VectList(0,0, 30,0, 30,10, 10,10, 10,20, 30,20, 30,30, 0,30, 0,0), null);
        Ring m = new Ring(new VectList(0,0, 10,0, 10,20, 20,20, 20,0, 30,0, 30,30, 0,30, 0,0), null);
        Area n = new Area(null, new Ring(new VectList(0,0, 10,0, 10,10, 10,20, 20,20, 30,20, 30,30, 0,30, 0,0), null).toArea(),
                new Ring(new VectList(20,0, 30,0, 30,10, 20,10, 20,0), null).toArea());
        assertEquals(n, l.intersection(m, Linearizer.DEFAULT, TOL));
    }

    @Test
    public void testLess() {
        Ring a = new Ring(new VectList(0,0, 10,0, 10,10, 0,10, 0,0), null);
        Ring b = new Ring(new VectList(10,0, 20,0, 20,10, 10,10, 10,0), null);
        Ring c = new Ring(new VectList(10,10, 20,10, 20,20, 10,20, 10,10), null);
        Ring d = new Ring(new VectList(20,0, 30,0, 30,10, 20,10, 20,0), null);
        Ring e = new Ring(new VectList(5,5, 15,5, 15,15, 5,15, 5,5), null);
        
        
        assertNull(a.less(a, Linearizer.DEFAULT, TOL));
        assertEquals(a, a.less(b, Linearizer.DEFAULT, TOL));
        assertEquals(a, a.less(c, Linearizer.DEFAULT, TOL));
        assertEquals(new Ring(new VectList(0,0, 10,0, 10,5, 5,5, 5,10, 0,10, 0,0), null), a.less(e, Linearizer.DEFAULT, TOL));
        
        assertEquals(a, a.less(d, Linearizer.DEFAULT, TOL));
        
        LineString f = LineString.valueOf(TOL, 10,5, 25,5, 25,0, 40,0);
        assertEquals(d, d.less(f, Linearizer.DEFAULT, TOL));
        
        PointSet h = PointSet.valueOf(new VectSet().add(25, 6).add(30, 6).add(30, 10).add(35, 5));
        assertEquals(d, d.less(h, Linearizer.DEFAULT, TOL));
        
        GeoShape j = new GeoShape(null, f.toLineSet(), h);
        assertEquals(d, d.less(j, Linearizer.DEFAULT, TOL));
        
        Ring l = new Ring(new VectList(0,0, 30,0, 30,10, 10,10, 10,20, 30,20, 30,30, 0,30, 0,0), null);
        Ring m = new Ring(new VectList(0,0, 10,0, 10,20, 20,20, 20,0, 30,0, 30,30, 0,30, 0,0), null);
        Ring n = new Ring(new VectList(10,0, 20,0, 20,10, 10,10, 10,0), null);
        assertEquals(n, l.less(m, Linearizer.DEFAULT, TOL));
    }

    @Test
    public void testXor() {
        Ring a = new Ring(new VectList(0,0, 10,0, 10,10, 0,10, 0,0), null);
        Ring b = new Ring(new VectList(10,0, 20,0, 20,10, 10,10, 10,0), null);
        Ring c = new Ring(new VectList(10,10, 20,10, 20,20, 10,20, 10,10), null);
        Ring d = new Ring(new VectList(20,0, 30,0, 30,10, 20,10, 20,0), null);
        Ring e = new Ring(new VectList(5,5, 15,5, 15,15, 5,15, 5,5), null);
        
        
        assertNull(a.xor(a, Linearizer.DEFAULT, TOL));
        assertEquals(a.union(b, Linearizer.DEFAULT, TOL).less(a.intersection(b, Linearizer.DEFAULT, TOL), Linearizer.DEFAULT, TOL), a.xor(b, Linearizer.DEFAULT, TOL));
        assertEquals(a.union(c, Linearizer.DEFAULT, TOL).less(a.intersection(c, Linearizer.DEFAULT, TOL), Linearizer.DEFAULT, TOL), a.xor(c, Linearizer.DEFAULT, TOL));
        assertEquals(a.union(d, Linearizer.DEFAULT, TOL), a.xor(d, Linearizer.DEFAULT, TOL));
        assertEquals(a.union(e, Linearizer.DEFAULT, TOL).less(a.intersection(e, Linearizer.DEFAULT, TOL), Linearizer.DEFAULT, TOL), a.xor(e, Linearizer.DEFAULT, TOL));
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
    public void testToString() {
        Ring ring = Ring.valueOf(TOL, 0,0, 6,8, 6,14, 0,10, 0,0);
        assertEquals("[\"RG\",0,0, 6,8, 6,14, 0,10, 0,0]", ring.toString());
    }
    
    @Test
    public void testBuildGeomFromRing(){
        assertEquals(Ring.valueOf(TOL, 0,0, 10,0, 0,10, 0,0), Ring.buildGeomFromRing(new VectList(0,0, 10,0, 0,10, 0,0), TOL));
        assertNull(Ring.buildGeomFromRing(new VectList(0,0, 0,10, 10,0 ,0,0), TOL));
    }
    
    @Test
    public void testGetEdgeBuffer(){
        Ring ring = Ring.valueOf(TOL, 0,0, 10,0, 0,10, 0,0);
        assertEquals(new VectList(0,0, 10,0, 0,10, 0,0), ring.getEdgeBuffer(0, Linearizer.DEFAULT, TOL));
    }
    
    @Test
    public void testParsePathFromNetwork(){
        Network network = new Network().addAllLinks(new VectList(0,0, 10,0, 0,10, 0,0));
        VectList template = new VectList(0.01,0.01, 10.01,0.01, 0.01,10.01, 0.01,0.01);
        VectList result = Ring.parsePathFromNetwork(network, template, new Tolerance(0.05));
        assertEquals(new VectList(0,0, 10,0, 0,10, 0,0), result);
    }
    
    @Test
    public void testConvexHull(){
        Ring a = Ring.valueOf(TOL, 20,0, 100,0, 100,90, 20,90, 20,20, 80,20, 80,70, 40,70, 40,40, 60,40, 50,60, 70,60, 70,30, 30,30, 30,80, 90,80, 90,10, 20,10, 20,0);
        Ring b = Ring.valueOf(TOL, 20,0, 100,0, 100,90, 20,90, 20,0);
        assertEquals(b, a.convexHull(TOL));
        assertSame(b, b.convexHull(TOL));
    }
    
    @Test
    public void testRelate_E(){
        //This strange case was identified as failing
        Vect vect = Vect.valueOf(1.0658141036401503E-14, -34.0);
        Ring ring = Ring.valueOf(TOL, -40,-30, 40,-30, 40,30, -40,30, -40,-30);
        ring = (Ring)ring.buffer(4, Linearizer.DEFAULT, TOL);
        int relation = ring.relate(vect, TOL);
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, relation);
    }
    
    @Test
    public void testGetInternalPoint_A(){
        Ring ring = Ring.valueOf(TOL, 20,30, 50,30, 50,70, 20,70, 20,30);
        Vect internal = ring.getInternalPoint(TOL);
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, ring.relate(internal, TOL));
    }
    
    @Test
    public void testGetInternalPoint_B(){
        Ring ring = Ring.valueOf(TOL, 0,0, 10,0, 10,50, 50,50, 50,10, 40,10, 40,40, 20,40, 20,0, 60,0, 60,60, 0,60, 0,0);
        Vect internal = ring.getInternalPoint(TOL);
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, ring.relate(internal, TOL));
        Tolerance tol = new Tolerance(6);
        internal = ring.getInternalPoint(tol);
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, ring.relate(internal, tol));
        tol = new Tolerance(10);
        internal = ring.getInternalPoint(tol);
        assertNull(internal);
    }
}
