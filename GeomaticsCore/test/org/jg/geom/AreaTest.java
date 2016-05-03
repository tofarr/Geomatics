package org.jg.geom;

import java.awt.geom.PathIterator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jg.geom.Network.LinkProcessor;
import org.jg.geom.Network.VertexProcessor;
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
    public void testValueOf_VectList_B() {
        VectList vects = new VectList();
        for(int i = 10; i < 60; i+=10){
            vects.add(i*2-10, i);
            vects.add(-i*2, i);
            vects.add(0, -i);
        }
        vects.add(10, -50);
        vects.add(10, 10);
        Area a = Area.valueOf(TOL, vects);
        
        //Technically, a solution with 3 rings and a depth of 1 would also be valid, but we test for 5.
        assertEquals(1, a.numChildren());
        assertEquals(5, a.numRings());
        assertEquals(24, a.numLines());
        assertEquals(29, a.numVects());
        assertEquals(5, a.getDepth());
        assertEquals(5600, a.getArea(), 0.001);
        assertEquals(Rect.valueOf(-100,-50,90,50), a.getBounds());
    }

    @Test
    public void testValueOf_Tolerance_Network() {
        Network network = new Network();
        network.addAllLinks(new VectList(0,10, 100,10, 100,40, 0,40, 0,10));
        network.addAllLinks(new VectList(0,60, 100,60, 100,90, 0,90, 0,60));
        network.addLink(0, 0, 100, 100);
        network.addLink(10, 50, 10, 90);
        
        Area expected = new Area(null,
                    new Area(new Ring(new VectList(0,10, 10,10, 40,40, 0,40, 0,10), null)),
                    new Area(new Ring(new VectList(0,60, 10,60, 10,90, 0,90, 0,60), null)),
                    new Area(new Ring(new VectList(10,10, 100,10, 100,40, 40,40, 10,10), null)),
                    new Area(new Ring(new VectList(10,60, 60,60, 90,90, 10,90, 10,60), null)),
                    new Area(new Ring(new VectList(60,60, 100,60, 100,90, 90,90, 60,60), null)));

        Area found = Area.valueOf(TOL, network);
        assertEquals(expected,found);
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
        assertEquals(3500, Area.valueOf(TOL, network).getArea(Linearizer.DEFAULT, TOL), 0.01);
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
        Area c = new Area(
                new Ring(new VectList(70,90, 120,90, 120,160, 70,160, 70,90), null),
                new Area(new Ring(new VectList(80,100, 110,100, 110,150, 80,150, 80,100), null))
        );
        assertEquals(c, b);
        
        network.addAllLinks(new VectList(60,0, 70,0, 60,10, 60,0));
        Area d = Area.valueOf(TOL, network);
        Area e = d.transform(new TransformBuilder().flipYAround(80).flipXAround(60).build());
        assertEquals(new Area(null, new Area(Ring.valueOf(TOL, 50,160, 60,150, 60,160, 50,160)), c), e);
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
        PathIter iter = area.iterator();
        for(int i = 90; i >= 0; i -= 10){
            int x = 40 + i;
            int y = 30 + i;
            assertFalse(iter.isDone());
            checkIter(iter, PathSegType.MOVE, -x, -y);
            checkIter(iter, PathSegType.LINE, x, -y);
            checkIter(iter, PathSegType.LINE, x, y);
            checkIter(iter, PathSegType.LINE, -x, y);
            checkIter(iter, PathSegType.CLOSE, -x, -y);
        }
        
        assertTrue(iter.isDone());
        iter.next();
        assertTrue(iter.isDone());
    }
    
    private void checkIter(PathIter iter, PathSegType result, double x, double y){
        double[] coords = new double[6];
        assertEquals(result, iter.currentSegment(coords));
        assertArrayEquals(new double[]{x,y,0,0,0,0}, coords, 0.001);
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
        assertEquals(5, area.getDepth());
        assertEquals(Ring.valueOf(TOL, -80,-70, 80,-70, 80,70, -80,70, -80,-70), area.getChild(0).shell);
        assertEquals(Ring.valueOf(TOL, 90,0, 100,0, 100,10, 90,0), area.getChild(1).shell);
        try{
            area.getChild(2);
            fail("Exception Expected");
        }catch(IndexOutOfBoundsException ex){  
        }
    }

    @Test
    public void testGetLineIndex() {
        Network network = new Network();
        network.addAllLinks(new VectList(0,0, 40,0, 40,50, 0,50, 0,0));
        network.addAllLinks(new VectList(10,10, 30,10, 30,40, 10,40, 10,10));
        network.addAllLinks(new VectList(100,100, 140,100, 140,150, 100,150, 100,100));
        Area area = Area.valueOf(TOL, network);
        SpatialNode<Line> lineIndex = area.getLineIndex();
        assertEquals(Rect.valueOf(0,0,140,150), lineIndex.getBounds());
        assertTrue(lineIndex.getDepth() < 3);

        final Set<Line> expected = new HashSet<>();
        expected.add(Line.valueOf(0,0, 40,0));
        expected.add(Line.valueOf(40,0, 40,50));
        expected.add(Line.valueOf(40,50, 0,50));
        expected.add(Line.valueOf(0,50, 0,0));
        
        expected.add(Line.valueOf(10,10, 30,10));
        expected.add(Line.valueOf(30,10, 30,40));
        expected.add(Line.valueOf(30,40, 10,40));
        expected.add(Line.valueOf(10,40, 10,10));
        
        expected.add(Line.valueOf(100,100, 140,100));
        expected.add(Line.valueOf(140,100, 140,150));
        expected.add(Line.valueOf(140,150, 100,150));
        expected.add(Line.valueOf(100,150, 100,100));
        
        lineIndex.forEach(new NodeProcessor<Line>() {
            @Override
            public boolean process(Rect bounds, Line value) {
                assertTrue(expected.remove(value));
                return true;
            }
        });
        assertTrue(expected.isEmpty());
        assertSame(lineIndex, area.getLineIndex());
    }

    @Test
    public void testGetRings() {
        Network network = new Network();
        Ring r1 = Ring.valueOf(TOL, 0,0, 40,0, 40,50, 0,50, 0,0);
        Ring r2 = Ring.valueOf(TOL, 10,10, 30,10, 30,40, 10,40, 10,10);
        Ring r3 = Ring.valueOf(TOL, 100,100, 140,100, 140,150, 100,150, 100,100);
        r1.addTo(network);
        r2.addTo(network);
        r3.addTo(network);
        Area area = Area.valueOf(TOL, network);
        List<Ring> a = area.getRings();
        try{
            area.getRings(null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        List<Ring> expected = Arrays.asList(r1, r2, r3);
        assertEquals(expected, a);
        List<Ring> b = new ArrayList<>();
        area.getRings(b);
        assertEquals(expected, b);
    }

    @Test
    public void testGetVects() {
        Network network = new Network();
        Ring r1 = Ring.valueOf(TOL, 0,0, 40,0, 40,50, 0,50, 0,0);
        Ring r2 = Ring.valueOf(TOL, 10,10, 30,10, 30,40, 10,40, 10,10);
        Ring r3 = Ring.valueOf(TOL, 100,100, 140,100, 140,150, 100,150, 100,100);
        r1.addTo(network);
        r2.addTo(network);
        r3.addTo(network);
        Area area = Area.valueOf(TOL, network);
        VectSet expected = new VectSet();
        expected.addAll(r1.vects);
        expected.addAll(r2.vects);
        expected.addAll(r3.vects);
        assertEquals(expected, area.getVects(new VectSet()));
    }

    @Test
    public void testToString() {
        Network network = new Network();
        network.addAllLinks(new VectList(0,0, 40,0, 40,50, 0,50, 0,0));
        network.addAllLinks(new VectList(10,10, 30,10, 30,40, 10,40, 10,10));
        network.addAllLinks(new VectList(100,100, 140,100, 140,150, 100,150, 100,100));
        Area area = Area.valueOf(TOL, network);
        assertEquals("[\"AR\",null,[[0,0, 40,0, 40,50, 0,50, 0,0],[[10,10, 30,10, 30,40, 10,40, 10,10]]],[[100,100, 140,100, 140,150, 100,150, 100,100]]]", area.toString());
        try {
            area.toString(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            area.toString(new Appendable() {
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
    public void testAddTo_Network() {
        Network a = new Network();
        a.addAllLinks(new VectList(0,0, 40,0, 40,50, 0,50, 0,0));
        a.addAllLinks(new VectList(10,10, 30,10, 30,40, 10,40, 10,10));
        a.addAllLinks(new VectList(100,100, 140,100, 140,150, 100,150, 100,100));
        Area area = Area.valueOf(TOL, a);
        Network b = new Network();
        area.addTo(b, Linearizer.DEFAULT, TOL);
        assertEquals(a, b);
    }

    @Test
    public void testToGeoShape() {
        Network a = new Network();
        a.addAllLinks(new VectList(0,0, 40,0, 40,50, 0,50, 0,0));
        a.addAllLinks(new VectList(10,10, 30,10, 30,40, 10,40, 10,10));
        a.addAllLinks(new VectList(100,100, 140,100, 140,150, 100,150, 100,100));
        Area area = Area.valueOf(TOL, a);
        GeoShape expected = new GeoShape(area, null, null);
        assertEquals(expected, area.toGeoShape(Linearizer.DEFAULT, TOL));
    }

    @Test
    public void testRelate_Vect() {
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
        final Area area = Area.valueOf(TOL, network);
        network.forEachVertex(new VertexProcessor(){
            @Override
            public boolean process(double x, double y, int numLinks) {
                assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, area.relate(x, y, TOL));
                return true;
            }
        });
        network.forEachLink(new LinkProcessor(){
            @Override
            public boolean process(double ax, double ay, double bx, double by) {
                VectBuilder mid = new VectBuilder(ax, ay).add(bx, by).div(2);
                assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, area.relate(mid, TOL));
                assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B, area.relate(mid.build(), TOL));
                return true;
            }
        });
        assertEquals(Relation.DISJOINT, area.relate(90, 20, TOL));
        assertEquals(Relation.DISJOINT, area.relate(120, 20, TOL));
        assertEquals(Relation.DISJOINT, area.relate(60, 90, TOL));
        
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, area.relate(-75, 10, TOL));
        assertEquals(Relation.DISJOINT, area.relate(-65, 10, TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, area.relate(-55, 10, TOL));
        assertEquals(Relation.DISJOINT, area.relate(-45, 10, TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, area.relate(0, 10, TOL));
        assertEquals(Relation.DISJOINT, area.relate(45, 10, TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, area.relate(55, 10, TOL));
        assertEquals(Relation.DISJOINT, area.relate(65, 10, TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, area.relate(75, 10, TOL));
        
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, area.relate(-75, 65, TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, area.relate(-65, 65, TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, area.relate(-55, 65, TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, area.relate(-45, 65, TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, area.relate(0, 65, TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, area.relate(45, 65, TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, area.relate(55, 65, TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, area.relate(65, 65, TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, area.relate(75, 65, TOL));
        
        assertEquals(Relation.DISJOINT, area.relate(-65, 55, TOL));
        assertEquals(Relation.DISJOINT, area.relate(-55, 55, TOL));
        assertEquals(Relation.DISJOINT, area.relate(-45, 55, TOL));
        assertEquals(Relation.DISJOINT, area.relate(0, 55, TOL));
        assertEquals(Relation.DISJOINT, area.relate(45, 55, TOL));
        assertEquals(Relation.DISJOINT, area.relate(55, 55, TOL));
        assertEquals(Relation.DISJOINT, area.relate(65, 55, TOL));
    }
    
    @Test
    public void testRelate_Area() {
        Area a = Area.valueOf(TOL, 0,0, 100,0, 100,100, 0,100, 0,0);
        Ring b = Ring.valueOf(TOL, 50,50, 150,50, 150,150, 50,150, 50,50);
        Area c = new Area(b);
        Area d = Area.valueOf(TOL, 150,50, 250,50, 250,150, 150,150, 150,50);
        Area e = new Area(Ring.valueOf(TOL, 40,40, 160,40, 160,160, 40,160, 40,40), c);
        Area f = new Area(null, c, d);
        
        assertEquals(Relation.TOUCH | Relation.A_INSIDE_B | Relation.B_INSIDE_A, a.relate(a, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.ALL, a.relate(b, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.ALL, a.relate(c, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.DISJOINT, a.relate(d, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.ALL, a.relate(e, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.ALL, a.relate(f, Linearizer.DEFAULT, TOL));
        
        assertEquals(Relation.ALL, c.relate(a, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.TOUCH | Relation.A_INSIDE_B | Relation.B_INSIDE_A, c.relate(b, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.TOUCH | Relation.A_INSIDE_B | Relation.B_INSIDE_A, c.relate(c, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_OUTSIDE_A | Relation.TOUCH, c.relate(d, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.TOUCH | Relation.A_OUTSIDE_B | Relation.B_OUTSIDE_A, c.relate(e, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.ALL ^ Relation.A_OUTSIDE_B, c.relate(f, Linearizer.DEFAULT, TOL));
        
        assertEquals(Relation.DISJOINT, d.relate(a, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_OUTSIDE_A | Relation.TOUCH, d.relate(b, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_OUTSIDE_A | Relation.TOUCH, d.relate(c, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.TOUCH | Relation.A_INSIDE_B | Relation.B_INSIDE_A, d.relate(d, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.ALL, d.relate(e, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.ALL ^ Relation.A_OUTSIDE_B, d.relate(f, Linearizer.DEFAULT, TOL));
        
        assertEquals(Relation.ALL, e.relate(a, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_OUTSIDE_A | Relation.TOUCH, e.relate(b, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_OUTSIDE_A | Relation.TOUCH, e.relate(c, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.ALL, e.relate(d, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.TOUCH | Relation.A_INSIDE_B | Relation.B_INSIDE_A, e.relate(e, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.ALL, e.relate(f, Linearizer.DEFAULT, TOL));
         
        assertEquals(Relation.ALL, f.relate(a, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.ALL ^ Relation.B_OUTSIDE_A, f.relate(b, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.ALL ^ Relation.B_OUTSIDE_A, f.relate(c, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.ALL ^ Relation.B_OUTSIDE_A, f.relate(d, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.ALL, f.relate(e, Linearizer.DEFAULT, TOL));
        assertEquals(Relation.TOUCH | Relation.A_INSIDE_B | Relation.B_INSIDE_A, f.relate(f, Linearizer.DEFAULT, TOL));
    } 
    
    @Test
    public void testRelate_Area_B() {
        Network network = new Network();
        network.addAllLinks(new VectList(0,0, 4,0, 0,4, 0,0));
        network.addAllLinks(new VectList(1,1, 2,1, 1,2, 1,1));
        Area a = Area.valueOf(TOL, network);
        // Testing with a large tolerance means the internal area may not be processed correctly 
        // - we may be missing the inside relation
        assertTrue(Relation.isTouch(a.relate(a, Linearizer.DEFAULT, new Tolerance(2))));
        
        network = new Network();
        network.addAllLinks(new VectList(1,1, 8,1, 1,8, 1,1));
        network.addAllLinks(new VectList(20,0, 30,0, 20,10, 20,0));
        network.addAllLinks(new VectList(21,1, 28,1, 21,8, 21,1));
        Area b = Area.valueOf(TOL, network);
        network.addAllLinks(new VectList(0,0, 10,0, 0,10, 0,0));        
        Area c = Area.valueOf(TOL, network);
        
        // Testing with a large tolerance means the internal area may not be processed correctly 
        // - we may be missing the inside relation
        assertEquals(Relation.TOUCH, b.relate(b, Linearizer.DEFAULT, new Tolerance(2)));
        int relate = b.relate(c, Linearizer.DEFAULT, new Tolerance(0.6));
        assertTrue(Relation.isTouch(relate));
        assertTrue(Relation.isAOutsideB(relate));
        assertTrue(Relation.isBOutsideA(relate));
        relate = c.relate(b, Linearizer.DEFAULT, new Tolerance(0.6));
        assertTrue(Relation.isTouch(relate));
        assertTrue(Relation.isAOutsideB(relate));
        assertTrue(Relation.isBOutsideA(relate));
    }
    
    @Test
    public void testBuffer() {
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
        final Area area = Area.valueOf(TOL, network);
        assertSame(area, area.buffer(0, Linearizer.DEFAULT, TOL));
        
        Area resultA = (Area)area.buffer(4, Linearizer.DEFAULT, TOL); // leave channels
        assertNull(resultA.shell);
        assertEquals(2, resultA.numChildren());
        assertEquals(6, resultA.numRings());
        assertEquals(5, resultA.getDepth());
        assertTrue(Rect.valueOf(-84, -74, 104, 74).match(resultA.getBounds(), TOL));
        assertEquals(23458, resultA.getArea(), 1);
        
        Area resultB = (Area)area.buffer(5, Linearizer.DEFAULT, TOL); // clear channels except at corners
        assertNull(resultB.shell);
        assertEquals(2, resultB.numChildren());
        assertEquals(10, resultB.numRings()); 
        assertEquals(2, resultB.getDepth());
        assertEquals(Rect.valueOf(-85, -75, 105, 75), resultB.getBounds());
        assertEquals(25733, resultB.getArea(), 1);
        
        Ring resultC = (Ring)area.buffer(8, Linearizer.DEFAULT, TOL);
        assertEquals(Rect.valueOf(-88, -78, 108, 78), resultC.getBounds());
        assertEquals(27852, resultC.getArea(), 1);
        
        Ring resultD = (Ring)area.buffer(-40, Linearizer.DEFAULT, TOL);
        assertNull(resultD);
        
    }
    
    @Test
    public void testHashCode() {
        Set<Integer> hashCodes = new HashSet<>();
        for (int x = 0; x <= 10; x++) {
            for (int y = 1; y < 10; y++) {
                Area area = Area.valueOf(TOL, 0,0, 50,50, 100,0, 150,50, 200,0, 250+x,50+y, 300,0, 0,0);
                hashCodes.add(area.hashCode());
            }
        }
        assertEquals(99, hashCodes.size());
    }

    @Test
    public void testEquals() {
        Area a = Area.valueOf(TOL, 0,0, 50,50, 100,0, 150,50, 200,0, 250,50, 300,0, 0,0);
        Area b = Area.valueOf(TOL, 0,0, 50,50, 100,0, 150,50, 200,0, 250,51, 300,0, 0,0);
        Area c = Area.valueOf(TOL, 0,0, 50,50, 100,0, 150,50, 200,0, 250,50, 300,0, 0,0);
        assertEquals(a, a);
        assertNotEquals(a, b);
        assertEquals(a, c);
        assertNotEquals(a, "");
        assertFalse(a.equals(null));
    }
    
    @Test
    public void testUnion(){
        Network n1 = new Network();
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                int x = 40 * i;
                int y = 30 * j;
                n1.addAllLinks(new VectList(x,y, x+30,y, x+30,y+20, x,y+20, x,y));
            }
        }
        Area a1 = Area.valueOf(TOL, n1);
        
        Network n2 = new Network();
        for(int i = 0; i < 50; i += 10){
            int x = 40 + i;
            int y = 30 + i;
            n2.addLink(-x, -y, x, -y);
            n2.addLink(x, -y, x, y);
            n2.addLink(x, y, -x, y);
            n2.addLink(-x, y, -x, -y);
        }
        Area a2 = Area.valueOf(TOL, n2);
        
        Area a3 = a1.union(a2, TOL);
        
        assertEquals(1, a3.numChildren());
        assertEquals(49, a3.numLines());
        assertEquals(3, a3.numRings());
        assertEquals(52, a3.numVects());
        assertEquals(Rect.valueOf(-80,-70,110,80), a3.getBounds());
        assertEquals(15400, a3.getArea(), 0.0001);
        assertEquals(3, a3.getDepth());
    }

    @Test
    public void testUnion_3args() {
        Network n1 = new Network();
        for(int i = 0; i < 3; i++){
            for(int j = 0; j < 3; j++){
                int x = 40 * i;
                int y = 30 * j;
                n1.addAllLinks(new VectList(x,y, x+30,y, x+30,y+20, x,y+20, x,y));
            }
        }
        Area a1 = Area.valueOf(TOL, n1);
        LineSet ls = LineSet.valueOf(TOL, -100,10, 15,10, 150,145);
        
        GeoShape geom = (GeoShape)a1.union(ls, Linearizer.DEFAULT, TOL);
        assertEquals(a1, geom.area);
        Network lines = new Network();
        lines.addLink(-100,10, 0,10);
        lines.addLink(25,20, 40,35);
        lines.addLink(55,50, 65,60);
        lines.addLink(70,65, 80,75);
        lines.addLink(85,80, 150,145);
        assertEquals(LineSet.valueOf(TOL, lines), geom.lines);
        assertNull(geom.points);
    }

    @Test
    public void testUnion_Ring_Tolerance() {
        Area a = Area.valueOf(TOL, 0,0, 100,0, 100,100, 0,100, 0,0);
        Ring r1 = Ring.valueOf(TOL, 50,50, 150,50, 150,150, 50,150, 50,50);
        Ring r2 = (Ring)a.union(r1, Linearizer.DEFAULT, TOL);
        Ring r3 = Ring.valueOf(TOL, 0,0, 100,0, 100,50, 150,50, 150,150, 50,150, 50,100, 0,100, 0,0);
        assertEquals(r3, r2);
    }

    @Test
    public void testIntersection_Area_Tolerance() {
        Area a = Area.valueOf(TOL, 0,0, 100,0, 100,100, 0,100, 0,0);
        Area b = Area.valueOf(TOL, 50,50, 150,50, 150,150, 50,150, 50,50);
        Area c = Area.valueOf(TOL, 50,50, 100,50, 100,100, 50,100, 50,50);
        Area d = Area.valueOf(TOL, 80,20, 140,20, 140,80, 80,80, 80,60, 120,60, 120,40, 80,40, 80,20);
        Area e = Area.valueOfInternal(Arrays.asList(
            Ring.valueOf(TOL, 80,20, 100,20, 100,40, 80,40, 80,20),
            Ring.valueOf(TOL, 80,60, 100,60, 100,80, 80,80, 80,60)));
        Area f = Area.valueOf(TOL, 100,0, 200,0, 200,100, 100,100, 100,0);
        Area g = Area.valueOf(TOL, 100,100, 200,100, 200,200, 100,200, 100,100);
        Ring h = Ring.valueOf(TOL, 300,0, 310,0, 310,10, 300,10, 300,0);
        
        assertEquals(c.toGeoShape(), a.intersection(b, TOL));
        assertEquals(c.toGeoShape(), b.intersection(a, TOL));
        assertEquals(c.toGeoShape(), a.intersection(c, TOL));
        assertEquals(c.toGeoShape(), b.intersection(c, TOL));

        assertEquals(c.toGeoShape(), a.intersection(b.shell, TOL));
        assertEquals(c.toGeoShape(), b.intersection(a.shell, TOL));
        assertEquals(c.toGeoShape(), a.intersection(c.shell, TOL));
        assertEquals(c.toGeoShape(), b.intersection(c.shell, TOL));

        assertEquals(e.toGeoShape(), a.intersection(d, TOL));
        assertEquals(e.toGeoShape(), d.intersection(a, TOL));
        
        assertEquals(Line.valueOf(100, 0, 100, 100), a.intersection(f, TOL).simplify());
        assertEquals(Vect.valueOf(100, 100), a.intersection(g, TOL).simplify());
        
        assertNull(a.intersection(h, TOL));
    }
    
    @Test
    public void testIntersection_3args() {
        Area a = Area.valueOf(TOL, 0,0, 100,0, 100,100, 0,100, 0,0);
        GeoShape b = new GeoShape(Area.valueOf(TOL, 0,0, 50,0, 0,50, 0,0),
                LineSet.valueOf(TOL, 0,120, 120,0),
                PointSet.valueOf(0,150, 75,75, 150,0));
        GeoShape c = new GeoShape(Area.valueOf(TOL, 0,0, 50,0, 0,50, 0,0),
                LineSet.valueOf(TOL, 20,100, 100,20),
                PointSet.valueOf(75,75));
        Ring d = Ring.valueOf(TOL, 200,0, 300,0, 200,100, 200,0);
        Area e = new Area(d);
        assertEquals(c, a.intersection(b, Linearizer.DEFAULT, TOL));
        assertNull(a.intersection(d, Linearizer.DEFAULT, TOL));
        assertNull(a.intersection(e, Linearizer.DEFAULT, TOL));
    }

    @Test
    public void testLess_Area_Tolerance() {
        Area a = Area.valueOf(TOL, 0,0, 100,0, 100,100, 0,100, 0,0);
        Area b = Area.valueOf(TOL, 50,50, 150,50, 150,150, 50,150, 50,50);
        Area c = Area.valueOf(TOL, 0,0, 100,0, 100,50, 50,50, 50,100, 0,100, 0,0);
        Area d = Area.valueOf(TOL, 50,50, 100,50, 100,100, 50,100, 50,50);
        Area e = Area.valueOf(TOL, 20,20, 80,20, 80,80, 20,80, 20,20);
        Area f = Area.valueOfInternal(Arrays.asList(Ring.valueOf(TOL, 0,0, 100,0, 100,100, 0,100, 0,0),
            Ring.valueOf(TOL, 20,20, 80,20, 80,80, 20,80, 20,20)));
        Ring g = Ring.valueOf(TOL, 100,0, 200,0, 200,100, 100,100, 100,0);
        
        assertEquals(c, a.less(b, TOL));
        assertEquals(c, a.less(d, TOL));
        assertEquals(f, a.less(e, TOL));
        assertEquals(a, a.less(g, TOL));
    }

    @Test
    public void testLess_3args() {
        Area a = Area.valueOf(TOL, 0,0, 100,0, 100,100, 0,100, 0,0);
        GeoShape b = new GeoShape(Area.valueOf(TOL, 0,0, 50,0, 0,50, 0,0),
                LineSet.valueOf(TOL, 0,120, 100,20, 100,40, 140,0),
                PointSet.valueOf(0,150, 100,100, 150,0));
        Area c = Area.valueOf(TOL, 0,50, 50,0, 100,0, 100,100, 0,100, 0,50);
        assertEquals(c, a.less(b, Linearizer.DEFAULT, TOL));
        Area d = Area.valueOf(TOL, 200,0, 300,0, 300,100, 200,0);
        assertSame(a, a.less(d, Linearizer.DEFAULT, TOL));
        assertSame(a, a.less(d, TOL));
        assertSame(a, a.less(d.getShell(), TOL));
    }
    
    @Test
    public void testToggleTo(){
        Network network = new Network();
        Area a = Area.valueOf(TOL, 0,0, 100,0, 100,100, 0,0);
        Area b = Area.valueOf(TOL, 0,0, 100,100, 0,100, 0,0);
        Area c = new Area(a.getBounds().buffer(10).toRing(), a, b);
        Area d = new Area(null, a, b);
        a.toggleTo(network);
        assertEquals("[[0,0, 100,0, 100,100, 0,0]]", network.toString());
        a.toggleTo(network);
        assertEquals("[[0,0],[100,0],[100,100]]", network.toString());
        a.toggleTo(network);
        b.toggleTo(network);
        assertEquals("[[0,0, 100,0, 100,100, 0,100, 0,0]]", network.toString());
        b.toggleTo(network);
        assertEquals("[[0,0, 100,0, 100,100, 0,0],[0,100]]", network.toString());
        c.toggleTo(network);
        assertEquals("[[-10,-10, 110,-10, 110,110, -10,110, -10,-10],[0,0, 100,100, 0,100, 0,0],[100,0]]", network.toString());
        d.toggleTo(network);
        assertEquals("[[-10,-10, 110,-10, 110,110, -10,110, -10,-10],[0,0, 100,0, 100,100, 0,0],[0,100]]", network.toString());
        try{
            a.toggleTo(null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
    }
    

    @Test
    public void testXor_3args() {
        Area a = Area.valueOf(TOL, 0,0, 100,0, 100,100, 0,100, 0,0);
        GeoShape b = new GeoShape(Area.valueOf(TOL, 0,0, 50,0, 0,50, 0,0),
                LineSet.valueOf(TOL, 0,120, 100,20, 100,40, 140,0),
                PointSet.valueOf(0,150, 100,100, 150,0));
        GeoShape c = new GeoShape(Area.valueOf(TOL, 0,50, 50,0, 100,0, 100,100, 0,100, 0,50),
                b.lines.less(a, Linearizer.DEFAULT, TOL),
                b.points.less(a, TOL));
        assertEquals(c, a.xor(b, Linearizer.DEFAULT, TOL));
        assertEquals(c, b.xor(a, Linearizer.DEFAULT, TOL));
        assertNull(a.xor(a, Linearizer.DEFAULT, TOL));
        assertEquals(a.union(b.area, TOL).less(a.intersection(b.area, TOL), Linearizer.DEFAULT, TOL).simplify(), a.xor(b.area, Linearizer.DEFAULT, TOL));
        assertEquals(a.union(c.area, TOL).less(a.intersection(c.area, TOL), Linearizer.DEFAULT, TOL).simplify(), a.xor(c.area, Linearizer.DEFAULT, TOL));
        Geom d = b.union(c, Linearizer.DEFAULT, TOL);
        Geom e = b.intersection(c, Linearizer.DEFAULT, TOL);
        Geom f = d.less(e, Linearizer.DEFAULT, TOL);
        assertEquals(f, b.xor(c, Linearizer.DEFAULT, TOL));
        assertNull(a.xor(a.toGeoShape(), Linearizer.DEFAULT, TOL));
    }
    
    @Test
    public void testGetInternalPoint(){
        Network network = new Network();
        for(int i = 0; i < 5; i++){
            Rect.valueOf(13-i,17-i,23+i,29+i).addTo(network, Linearizer.DEFAULT, TOL);
        }
        Area a = Area.valueOf(TOL, network);
        Vect internalPoint = a.getInternalPoint(new Tolerance(2));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, a.relate(internalPoint, TOL));
        
        Area b = Area.valueOf(TOL, 0,0, 101,0, 101,100, 1,0, 0,1, 0,0);
        internalPoint = b.getInternalPoint(new Tolerance(2));
        assertEquals(Relation.A_OUTSIDE_B | Relation.B_INSIDE_A, b.relate(internalPoint, TOL));
        
        Area c = Area.valueOf(TOL, 0,0, 2,0, 2,1, 1,0, 0,1, 0,0);
        internalPoint = c.getInternalPoint(new Tolerance(2));
        assertNull(internalPoint);
        
        network = new Network();
        for(int i = 0; i < 5; i++){
            Rect.valueOf(5-i,5-i,5+i,5+i).addTo(network, Linearizer.DEFAULT, TOL);
        }
        Area d = Area.valueOf(TOL, network);
        internalPoint = d.getInternalPoint(new Tolerance(2));
        assertNull(internalPoint);
    }
    
    @Test
    public void testGetConvexHull(){
        Network network = new Network();
        network.addAllLinks(new VectList(0,0, 100,0, 40,40, 0,100, 0,0));
        network.addAllLinks(new VectList(10,10, 80,10, 30,30, 10,80, 10,10));
        network.addAllLinks(new VectList(200,0, 300,0, 240,40, 200,100, 200,0));
        Area a = Area.valueOf(TOL, network);
        Ring b = Ring.valueOf(TOL, 0,0, 300,0, 200,100, 0,100, 0,0);
        Ring c = a.getConvexHull(TOL);
        assertEquals(b, c);
        assertSame(c, c.toArea().getConvexHull(TOL));
        assertEquals(Ring.valueOf(TOL, 0,0, 100,0, 0,100, 0,0), a.getChild(0).getConvexHull(TOL));
    }   
    
    @Test
    public void testIsConvexRing(){
        Network network = new Network();
        network.addAllLinks(new VectList(0,0, 100,0, 0,100, 0,0));
        network.addAllLinks(new VectList(10,10, 80,10, 30,30, 10,80, 10,10));
        network.addAllLinks(new VectList(200,0, 300,0, 240,40, 200,100, 200,0));
        network.addAllLinks(new VectList(400,0, 500,0, 400,100, 400,0));
        Area a = Area.valueOf(TOL, network);
        assertFalse(a.isConvexRing());
        assertFalse(a.getChild(0).isConvexRing());
        assertFalse(a.getChild(1).isConvexRing());
        assertTrue(a.getChild(2).isConvexRing());
    }

    @Test
    public void testBisect(){
        Network network = new Network();
        network.addAllLinks(new VectList(0,0, 120,0, 40,40, 0,120, 0,0));
        network.addAllLinks(new VectList(10,10, 80,10, 30,30, 10,80, 10,10));
        network.addAllLinks(new VectList(150,0, 270,0, 190,40, 150,120, 150,0));
        Area a = Area.valueOf(TOL, network);
        List<Area> parts = a.bisect(Line.valueOf(0,50, 10,50), TOL);
        assertEquals(2, parts.size());
        assertEquals(a.intersection(Rect.valueOf(0,0, 300,50).toRing(), TOL).simplify(), parts.get(0));
        assertEquals(a.intersection(Rect.valueOf(0,50, 300,150).toRing(), TOL).simplify(), parts.get(1));
        
        parts = a.bisect(Line.valueOf(0,0, 10,10), TOL);
        assertEquals(2, parts.size());        
        assertEquals(a.intersection(Ring.valueOf(TOL, 0,0, 300,0, 300,300, 0,0), TOL).simplify(), parts.get(0));
        assertEquals(a.intersection(Ring.valueOf(TOL, 0,0, 0,300, 300,300, 0,0), TOL).area, parts.get(1));
        
        parts = a.bisect(Line.valueOf(-10,0, -10,10), TOL);
        assertEquals(1, parts.size());
        assertEquals(a, parts.get(0));
        
        
        network = new Network();
        network.addAllLinks(new VectList(0,0, 100,0, 100,20, 0,20, 0,0));
        network.addAllLinks(new VectList(30,40, 70,40, 70,60, 30,60, 30,40));
        a = Area.valueOf(TOL, network);
        
        parts = a.bisect(Line.valueOf(0,30, 20,60), TOL);
        assertEquals(1, parts.size());
        assertEquals(a, parts.get(0));
        
        parts = a.bisect(Line.valueOf(80,60, 100,30), TOL);
        assertEquals(1, parts.size());
        assertEquals(a, parts.get(0));
        
    }
}
