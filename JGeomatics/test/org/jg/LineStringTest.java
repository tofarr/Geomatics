package org.jg;

import org.jg.util.Tolerance;
import org.jg.VectList;
import org.jg.Vect;
import org.jg.Processor;
import org.jg.Rect;
import org.jg.LineString;
import org.jg.Line;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.jg.Line;
import org.jg.Processor;
import org.jg.Rect;
import org.jg.util.Tolerance;
import org.jg.Vect;
import org.jg.VectList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar_000
 */
public class LineStringTest {

    @Test
    public void testConstructor() {
        LineString ls = new LineString(new VectList(1,2,5,11));
        assertEquals("[1,2, 5,11]", ls.toString());
        ls = new LineString(new VectList());
        try{
            ls = new LineString(null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        assertFalse(ls.isValid(Tolerance.DEFAULT));
        assertEquals("[]", ls.toString());
        ls.getInteractingLines(new Rect(0, 0, 100, 100), new Processor<Line>(){
            @Override
            public boolean process(Line value) {
                throw new IllegalStateException("Impossible!");
            }
        
        }, new Line());
        
    }

    @Test
    public void testIsValid() {
        assertTrue(new LineString(new VectList(0, 0, 10, 0, 10, 10, 0, 10)).isValid(Tolerance.DEFAULT));
        assertFalse(new LineString(new VectList(0, 0, 0, 0, 10, 0, 10, 10, 0, 10)).isValid(Tolerance.DEFAULT));
        assertFalse(new LineString(new VectList(0, 0, 5, 0, 10, 0, 10, 10, 0, 10)).isValid(Tolerance.DEFAULT));
        assertFalse(new LineString(new VectList(0, 0, 5, 0, 10, 0, 10, 10, 0, 10, 0, 10)).isValid(Tolerance.DEFAULT));
        assertFalse(new LineString(new VectList(0, 0, 5, 0, 10, 0, 10, 10, 5, 10, 0, 10)).isValid(Tolerance.DEFAULT));
        assertFalse(new LineString(new VectList(0, 0, 5, 0, 10, 0, 10, 10, 5, 10.09, 0, 10)).isValid(new Tolerance(0.1)));
    }

    @Test
    public void testGetVects() {
        LineString ls = new LineString(new VectList(0, 0, 10, 0, 10, 10, 0, 10));
        VectList a = ls.getVects(new VectList());
        assertEquals("[0,0, 10,0, 10,10, 0,10]", a.toString());
        a.set(0, new Vect(1, 0));
        try {
            ls.getVects(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        VectList b = ls.getVects(new VectList());
        assertEquals("[0,0, 10,0, 10,10, 0,10]", b.toString());
    }

    @Test
    public void testGet_int_Vect() {
        LineString ls = new LineString(new VectList(1, 2, 3, 4, 5, 6, 7, 8));
        Vect vect = ls.get(1, new Vect());
        assertEquals(new Vect(3, 4), vect);
        vect.set(4, 3);
        assertSame(vect, ls.get(1, vect));
        try {
            ls.get(0, (Vect) null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            ls.get(-1, vect);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            ls.get(4, vect);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        assertEquals(new Vect(3, 4), vect);
    }

    @Test
    public void testGet_int_Line() {
        LineString ls = new LineString(new VectList(7, 8, 6, 5, 3, 4, 2, 1));
        Line line = ls.get(1, new Line());
        assertEquals(new Line(6, 5, 3, 4), line);
        line.set(3, 4, 5, 6);
        assertSame(line, ls.get(1, line));
        try {
            ls.getVects(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            ls.get(-1, line);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            ls.get(3, line);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        assertEquals(new Line(6, 5, 3, 4), line);
    }

    @Test
    public void testGetInteractingLines() {
        LineString ls = new LineString(new VectList(60, 210, 80, 200, 100, 200, 120, 220, 120, 240, 100, 260, 100, 280, 120, 280));
        final Set<Line> lines = new HashSet<>();
        lines.add(new Line(80, 200, 100, 200));
        lines.add(new Line(100, 200, 120, 220));
        lines.add(new Line(120, 220, 120, 240));
        lines.add(new Line(120, 240, 100, 260));
        lines.add(new Line(100, 260, 100, 280));
        final Line target = new Line();
        assertTrue(ls.getInteractingLines(new Rect(100, 200, 120, 260), new Processor<Line>() {

            @Override
            public boolean process(Line value) {
                assertSame(target, value);
                assertTrue(lines.remove(value));
                return true;
            }

        }, target));
        assertTrue(lines.isEmpty());

        Processor processor = new Processor<Line>() {
            boolean called;

            @Override
            public boolean process(Line value) {
                if (called) {
                    fail("Already called");
                }
                called = true;
                return false;
            }
        };

        assertFalse(ls.getInteractingLines(new Rect(100, 200, 120, 260), processor, target));

        try {
            ls.getInteractingLines(new Rect(100, 200, 120, 260), processor, null);
            fail("Exception Expected");
        } catch (NullPointerException ex) {
        }
        try {
            ls.getInteractingLines(new Rect(100, 200, 120, 260), null, target);
            fail("Exception Expected");
        } catch (NullPointerException ex) {
        }
        try {
            ls.getInteractingLines(null, processor, target);
            fail("Exception Expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testGetOverlappingLines() {
        LineString ls = new LineString(new VectList(60, 210, 80, 200, 100, 200, 120, 220, 120, 240, 100, 260, 100, 280, 120, 280));
        final Set<Line> lines = new HashSet<>();
        lines.add(new Line(100, 200, 120, 220));
        lines.add(new Line(120, 240, 100, 260));
        final Line target = new Line();
        assertTrue(ls.getOverlappingLines(new Rect(100, 200, 120, 260), new Processor<Line>() {

            @Override
            public boolean process(Line value) {
                assertSame(target, value);
                assertTrue(lines.remove(value));
                return true;
            }

        }, target));
        assertTrue(lines.isEmpty());

        Processor processor = new Processor<Line>() {
            boolean called;

            @Override
            public boolean process(Line value) {
                if (called) {
                    fail("Already called");
                }
                called = true;
                return false;
            }
        };

        assertFalse(ls.getOverlappingLines(new Rect(100, 200, 120, 260), processor, target));

        try {
            ls.getOverlappingLines(new Rect(100, 200, 120, 260), processor, null);
            fail("Exception Expected");
        } catch (NullPointerException ex) {
        }
        try {
            ls.getOverlappingLines(new Rect(100, 200, 120, 260), null, target);
            fail("Exception Expected");
        } catch (NullPointerException ex) {
        }
        try {
            ls.getOverlappingLines(null, processor, target);
            fail("Exception Expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testSize() {
        LineString ls = new LineString(new VectList(60, 210, 80, 200, 100, 200, 120, 220, 120, 240, 100, 260, 100, 280, 120, 280));
        assertEquals(7, ls.size());
        ls = new LineString(new VectList());
        assertEquals(0, ls.size());
    }

    @Test
    public void testNormalize() {
        LineString a = new LineString(new VectList(0, 10, 5, 10.09, 10, 10, 10, 0, 5, 0, 0, 0, 0, 10));
        LineString b = a.normalize(new Tolerance(0.1));
        try {
            a.normalize(null);
            fail("Exception Expected");
        } catch (NullPointerException ex) {
        }
        assertEquals("[0,10, 5,10.09, 10,10, 10,0, 5,0, 0,0, 0,10]", a.toString());
        assertEquals("[0,10, 0,0, 10,0, 10,10, 0,10]", b.toString());
        assertSame(b, b.normalize(Tolerance.DEFAULT));
        assertEquals("[0,10, 0,0, 10,0, 10,10, 0,10]", b.toString());
        assertEquals("[]", new LineString(new VectList()).normalize(Tolerance.DEFAULT).toString());
        assertEquals("[0,10, 0,0, 10,0, 10,10, 5,10.09, 0,10]", new LineString(new VectList(0, 10, 0, 0, 5, 0, 10, 0, 10, 10, 5, 10.09, 0, 10)).normalize(Tolerance.DEFAULT).toString());
        assertEquals("[0,10, 0,20]", new LineString(new VectList(0, 10, 0, 10, 0, 10, 0, 20)).normalize(Tolerance.DEFAULT).toString());
    }

    @Test
    public void testSplitOnSelfIntersect_A() {
        LineString a = new LineString(new VectList(0,0, 100,100, 100,0, 0,100));
        Set<LineString> expected = new HashSet<>();
        expected.add(new LineString(new VectList(0,0, 50,50)));
        expected.add(new LineString(new VectList(0,100, 50,50)));
        expected.add(new LineString(new VectList(50,50, 100,0, 100,100, 50,50)));
        Set<LineString> results = new HashSet<>();
        a.splitOnSelfIntersect(Tolerance.DEFAULT, results);
        assertEquals(expected, results);
    }

    @Test
    public void testSplitOnSelfIntersect_B() {
        LineString a = new LineString(new VectList(0,50, 100,50, 80,30, 40,70, 0,30, 0,50));
        Set<LineString> expected = new HashSet<>();
        expected.add(new LineString(new VectList(20,50, 60,50)));
        expected.add(new LineString(new VectList(0,100, 50,50)));
        expected.add(new LineString(new VectList(50,50, 100,0, 100,100, 50,50)));
        Set<LineString> results = new HashSet<>();
        a.splitOnSelfIntersect(Tolerance.DEFAULT, results);
        assertEquals(expected, results);
        fail("Try a shark tooth pattern also");
        
        fail("Try repeated intersections of same spot");
        
        fail("Try pentagram");
        
        fail("Try no intersection");
    }

//    /**
//     * Test of splitAgainst method, of class LineString.
//     */
//    @Test
//    public void testSplitAgainst() {
//        System.out.println("splitAgainst");
//        LineString other = null;
//        Tolerance tolerance = null;
//        Collection<LineString> results = null;
//        LineString instance = null;
//        instance.splitAgainst(other, tolerance, results);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getLength method, of class LineString.
//     */
//    @Test
//    public void testGetLength() {
//        System.out.println("getLength");
//        LineString instance = null;
//        double expResult = 0.0;
//        double result = instance.getLength();
//        assertEquals(expResult, result, 0.0);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of toString method, of class LineString.
//     */
//    @Test
//    public void testToString_0args() {
//        System.out.println("toString");
//        LineString instance = null;
//        String expResult = "";
//        String result = instance.toString();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of toString method, of class LineString.
//     */
//    @Test
//    public void testToString_Appendable() {
//        System.out.println("toString");
//        Appendable appendable = null;
//        LineString instance = null;
//        instance.toString(appendable);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of equals method, of class LineString.
//     */
//    @Test
//    public void testEquals() {
//        System.out.println("equals");
//        Object obj = null;
//        LineString instance = null;
//        boolean expResult = false;
//        boolean result = instance.equals(obj);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of hashCode method, of class LineString.
//     */
//    @Test
//    public void testHashCode() {
//        System.out.println("hashCode");
//        LineString instance = null;
//        int expResult = 0;
//        int result = instance.hashCode();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of writeExternal method, of class LineString.
//     */
//    @Test
//    public void testWriteExternal() throws Exception {
//        System.out.println("writeExternal");
//        ObjectOutput out = null;
//        LineString instance = null;
//        instance.writeExternal(out);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of readExternal method, of class LineString.
//     */
//    @Test
//    public void testReadExternal() throws Exception {
//        System.out.println("readExternal");
//        ObjectInput in = null;
//        LineString instance = null;
//        instance.readExternal(in);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
