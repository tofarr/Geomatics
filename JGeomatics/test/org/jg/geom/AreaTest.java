package org.jg.geom;

import java.awt.geom.PathIterator;
import java.util.Collection;
import java.util.List;
import org.jg.util.SpatialNode;
import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.util.TransformBuilder;
import org.jg.util.VectList;
import org.jg.util.VectSet;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar
 */
public class AreaTest {

    final Tolerance TOL = Tolerance.DEFAULT;
    
    @Test
    public void testConstructor(){
        Ring ring = Ring.valueOf(TOL, 0,0, 100,0, 0,100, 0,0);
        Area area = new Area(ring);
        try{
            area = new Area(null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        assertSame(ring, area.shell);
        assertEquals(0, area.children.length);
    }
    
    @Test
    public void testValueOf_VectList() {
        assertNull(Area.valueOf(TOL, 0,0, 100,0, 100,100, 0,100));
        assertEquals(new Area(new Ring(new VectList(0,0, 100,0, 100,100, 0,100, 0,0), null)),
                Area.valueOf(TOL, 0,0, 0,100, 100,100, 100,0, 0,0));
        
        assertEquals(new Area(null,
                    new Area(new Ring(new VectList(0,0, 100,0, 50,50, 0,0), null)),
                    new Area(new Ring(new VectList(0,100, 50,50, 100,100, 0,100), null))
                ),
                Area.valueOf(TOL, 0,0, 100,100, 0,100, 100,0, 0,0));
        
        assertEquals(new Area(new Ring(new VectList(0,0, 50,50, 100,0, 100,100, 0,100, 0,0), null),
                    new Area(new Ring(new VectList(10,90, 50,50, 90,90, 10,90), null))),
                Area.valueOf(TOL, 100,100, 100,0, 10,90, 90,90, 0,0, 0,100, 100,100));
        
        try{
            Area.valueOf(TOL, (VectList)null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
    }

    @Test
    public void testValueOf_Tolerance_Network() {
        Network network = new Network();
        network.addAllLinks(new VectList(0,10, 100,10, 100,40, 0,40, 0,10));
        network.addAllLinks(new VectList(0,60, 100,60, 100,90, 0,90, 0,60));
        network.addLink(0, 0, 100, 100);
        network.addLink(10, 50, 10, 90);
        
        Area expected = new Area(null,
                    new Area(new Ring(new VectList(0,10, 10,10, 100,10, 100,40, 40,40, 0,40, 0,10), null)),
                    new Area(new Ring(new VectList(0,60, 10,60, 60,60, 100,60, 100,90, 90,90, 10,90, 0,90, 0,60), null)));
        Area found = Area.valueOf(TOL, network);
        String wkt = found.toGeoShape().toWkt();
        System.out.println(wkt);
        assertEquals(expected,
                found);
        try{
            Area.valueOf(TOL, (Network)null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
    }

    @Test
    public void testGetArea() {
        Network network = new Network();
        network.addAllLinks(new VectList(0,0, 50,0, 50,70, 0,70, 0,0));
        assertEquals(3500, Area.valueOf(TOL, network).getArea(), 0.01);
        network.addAllLinks(new VectList(10,10, 40,10, 40,60, 10,60, 10,10));
        assertEquals(2000, Area.valueOf(TOL, network).getArea(), 0.01);
        network.addAllLinks(new VectList(80,0, 90,0, 90,10, 80,10, 80,0));
        assertEquals(2100, Area.valueOf(TOL, network).getArea(), 0.01);
    }

    @Test
    public void testGetBounds() {
        Network network = new Network();
        network.addAllLinks(new VectList(0,0, 50,0, 50,70, 0,70, 0,0));
        assertEquals(Rect.valueOf(0,0,50,70), Area.valueOf(TOL, network).getBounds());
        network.addAllLinks(new VectList(10,10, 40,10, 40,60, 10,60, 10,10));
        assertEquals(Rect.valueOf(0,0,50,70), Area.valueOf(TOL, network).getBounds());
        network.addAllLinks(new VectList(80,0, 90,0, 90,10, 80,10, 80,0));
        assertEquals(Rect.valueOf(0,0,90,70), Area.valueOf(TOL, network).getBounds());
    }

    @Test
    public void testTransform() {
        Network network = new Network();
        network.addAllLinks(new VectList(0,0, 50,0, 50,70, 0,70, 0,0));
        network.addAllLinks(new VectList(10,10, 40,10, 40,60, 10,60, 10,10));
        Area a = Area.valueOf(TOL, network);
        assertSame(a, a.transform(Transform.IDENTITY));
        try{
            a.transform(null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        Area b = a.transform(new TransformBuilder().flipYAround(80).flipXAround(60).build());
        assertEquals(new Area(
                new Ring(new VectList(70,90, 120,90, 120,160, 70,160, 70,90), null),
                new Area(new Ring(new VectList(80,100, 110,100, 110,150, 80,150, 80,100), null))
        ), b);
    }

    @Test
    public void testSimplify() {
        Network network = new Network();
        network.addAllLinks(new VectList(0,0, 50,0, 50,70, 0,70, 0,0));
        Area a = Area.valueOf(TOL, network);
        assertSame(a.shell, a.simplify());
        network.addAllLinks(new VectList(10,10, 40,10, 40,60, 10,60, 10,10));
        Area b = Area.valueOf(TOL, network);
        assertSame(b, b.simplify());
    }

    @Test
    public void testPathIterator() {
        Network network = new Network();
        for(int i = 0; i < 100; i += 10){
            int x = 40 + i;
            int y = 30 + i;
            network.addLink(-x, -y, x, -y);
            network.addLink(x, -y, x, y);
            network.addLink(x, y, -x, y);
            network.addLink(-x, y, -x, -y);
        }
        Area area = Area.valueOf(TOL, network);
        PathIterator iter = area.pathIterator();
        
        for(int i = 90; i >= 0; i -= 10){
            int x = 40 + i;
            int y = 30 + i;
            assertFalse(iter.isDone());
            checkIter(iter, PathIterator.SEG_MOVETO, -x, -y);
            checkIter(iter, PathIterator.SEG_LINETO, x, -y);
            checkIter(iter, PathIterator.SEG_LINETO, x, y);
            checkIter(iter, PathIterator.SEG_LINETO, -x, y);
            checkIter(iter, PathIterator.SEG_CLOSE, -x, -y);
        }
        
        assertTrue(iter.isDone());
        iter.next();
        assertTrue(iter.isDone());
    }
    
    private void checkIter(PathIterator iter, int result, double x, double y){
        double[] coords = new double[6];
        float[] fcoords = new float[6];
        assertEquals(result, iter.currentSegment(coords));
        assertEquals(result, iter.currentSegment(fcoords));
        assertArrayEquals(new double[]{x,y,0,0,0,0}, coords, 0.001);
        assertArrayEquals(new float[]{(float)x,(float)y,0,0,0,0}, fcoords, 0.001f);
        iter.next();
    }

    @Test
    public void testClone() {
        Area area = Area.valueOf(TOL, 0,0, 50,0, 50,70, 0,70, 0,0);
        assertSame(area, area.clone());
    }

    @Test
    public void testNums() {
        Network network = new Network();
        for(int i = 0; i < 50; i += 10){
            int x = 40 + i;
            int y = 30 + i;
            network.addLink(-x, -y, x, -y);
            network.addLink(x, -y, x, y);
            network.addLink(x, y, -x, y);
            network.addLink(-x, y, -x, -y);
        }
        network.addAllLinks(new VectList(90,0, 100,0, 100,10, 90,0));
        Area area = Area.valueOf(TOL, network);
        assertEquals(6, area.numRings());
        assertEquals(23, area.numLines());
        assertEquals(2, area.numChildren());
        assertEquals(29, area.numVects());
    }

    @Test
    public void testGetLineIndex() {
        Network network = new Network();
        network.addAllLinks(new VectList(0,0, 40,0, 40,50, 0,50, 0,0));
        network.addAllLinks(new VectList(10,10, 30,10, 30,40, 10,40, 10,10));
        network.addAllLinks(new VectList(100,100, 140,100, 140,150, 100,150, 100,100));
        Area area = Area.valueOf(TOL, network);
        SpatialNode<Line> lineIndex = area.getLineIndex();
        TEST WITH DOUBLE DONUT, GET TWICE
        System.out.println("getLineIndex");
        Area instance = null;
        SpatialNode<Line> expResult = null;
        SpatialNode<Line> result = instance.getLineIndex();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetRings_0args() {
        System.out.println("getRings");
        Area instance = null;
        List<Ring> expResult = null;
        List<Ring> result = instance.getRings();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetRings_Collection() {
        System.out.println("getRings");
        Collection<Ring> result_2 = null;
        Area instance = null;
        instance.getRings(result_2);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetVects() {
        System.out.println("getVects");
        VectSet result_2 = null;
        Area instance = null;
        VectSet expResult = null;
        VectSet result = instance.getVects(result_2);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testToString_0args() {
        System.out.println("toString");
        Area instance = null;
        String expResult = "";
        String result = instance.toString();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testToString_Appendable() {
        System.out.println("toString");
        Appendable appendable = null;
        Area instance = null;
        instance.toString(appendable);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testAddTo_Network() {
        System.out.println("addTo");
        Network network = null;
        Area instance = null;
        instance.addTo(network);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testAddTo_3args() {
        System.out.println("addTo");
        Network network = null;
        Tolerance flatness = null;
        Tolerance accuracy = null;
        Area instance = null;
        instance.addTo(network, flatness, accuracy);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testToGeoShape_Tolerance_Tolerance() {
        System.out.println("toGeoShape");
        Tolerance flatness = null;
        Tolerance accuracy = null;
        Area instance = null;
        GeoShape expResult = null;
        GeoShape result = instance.toGeoShape(flatness, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testToGeoShape_0args() {
        System.out.println("toGeoShape");
        Area instance = null;
        GeoShape expResult = null;
        GeoShape result = instance.toGeoShape();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testBuffer() {
        System.out.println("buffer");
        double amt = 0.0;
        Tolerance flatness = null;
        Tolerance accuracy = null;
        Area instance = null;
        Geom expResult = null;
        Geom result = instance.buffer(amt, flatness, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testRelate_Vect_Tolerance() {
        System.out.println("relate");
        Vect vect = null;
        Tolerance tolerance = null;
        Area instance = null;
        Relate expResult = null;
        Relate result = instance.relate(vect, tolerance);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testRelate_VectBuilder_Tolerance() {
        System.out.println("relate");
        VectBuilder vect = null;
        Tolerance tolerance = null;
        Area instance = null;
        Relate expResult = null;
        Relate result = instance.relate(vect, tolerance);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testRelate_3args() {
        System.out.println("relate");
        double x = 0.0;
        double y = 0.0;
        Tolerance tolerance = null;
        Area instance = null;
        Relate expResult = null;
        Relate result = instance.relate(x, y, tolerance);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testUnion_3args() {
        System.out.println("union");
        Geom other = null;
        Tolerance flatness = null;
        Tolerance accuracy = null;
        Area instance = null;
        Geom expResult = null;
        Geom result = instance.union(other, flatness, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testUnion_Area_Tolerance() {
        System.out.println("union");
        Area other = null;
        Tolerance accuracy = null;
        Area instance = null;
        Area expResult = null;
        Area result = instance.union(other, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testUnion_Ring_Tolerance() {
        System.out.println("union");
        Ring other = null;
        Tolerance accuracy = null;
        Area instance = null;
        Area expResult = null;
        Area result = instance.union(other, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testUnion_GeoShape_Tolerance() {
        System.out.println("union");
        GeoShape other = null;
        Tolerance accuracy = null;
        Area instance = null;
        GeoShape expResult = null;
        GeoShape result = instance.union(other, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testIntersection_3args() {
        System.out.println("intersection");
        Geom other = null;
        Tolerance flatness = null;
        Tolerance accuracy = null;
        Area instance = null;
        Geom expResult = null;
        Geom result = instance.intersection(other, flatness, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testIntersection_Area_Tolerance() {
        System.out.println("intersection");
        Area other = null;
        Tolerance accuracy = null;
        Area instance = null;
        GeoShape expResult = null;
        GeoShape result = instance.intersection(other, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testLess_Area_Tolerance() {
        System.out.println("less");
        Area other = null;
        Tolerance accuracy = null;
        Area instance = null;
        Area expResult = null;
        Area result = instance.less(other, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testLess_3args() {
        System.out.println("less");
        Geom other = null;
        Tolerance flatness = null;
        Tolerance accuracy = null;
        Area instance = null;
        Area expResult = null;
        Area result = instance.less(other, flatness, accuracy);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        Area instance = null;
        int expResult = 0;
        int result = instance.hashCode();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testEquals() {
        System.out.println("equals");
        Object obj = null;
        Area instance = null;
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetDepth() {
        System.out.println("getDepth");
        Area instance = null;
        int expResult = 0;
        int result = instance.getDepth();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testNumChildren() {
        System.out.println("numChildren");
        Area instance = null;
        int expResult = 0;
        int result = instance.numChildren();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testGetChild() {
        System.out.println("getChild");
        int index = 0;
        Area instance = null;
        Area expResult = null;
        Area result = instance.getChild(index);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
