package org.jg;

import org.jg.util.View;
import org.jg.util.Transform;
import org.jg.Vect;
import org.jg.Rect;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar_000
 */
public class ViewTest {

    @Test
    public void testConstructorFailures() {
        Rect rect = new Rect(2000, 1000, 6000, 7000);
        Vect vect = new Vect(4500, 3500);
        try {
            new View(null, 400, 300);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            new View(rect, 0, 300);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            new View(rect, -1, 300);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            new View(rect, 400, 0);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            new View(rect, 400, -1);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            new View(null, 10, 10, 400, 300);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            //expected
        }
        try {
            new View(vect, 0, 10, 400, 300);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            new View(vect, Double.NaN, 10, 400, 300);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            new View(vect, 10, 0, 400, 300);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            new View(vect, 10, Double.POSITIVE_INFINITY, 400, 300);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            new View(vect, 10, 10, 0, 300);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            new View(vect, 10, 10, 400, 0);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            new View(vect, 10, 10, -1, 300);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
        try {
            new View(vect, 10, 10, 400, -1);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            //expected
        }
    }

    @Test
    public void test_A() {
        View view = new View(new Rect(2000, 1000, 6000, 7000), 400, 300);

        assertEquals(new Rect(2000, 1000, 6000, 7000), view.getBounds(new Rect()));
        assertEquals(new Vect(4000, 4000), view.getCenter(new Vect()));
        assertEquals(400, view.getWidthPx(), 0.00001);
        assertEquals(300, view.getHeightPx(), 0.00001);
        assertEquals(10, view.getResolutionX(), 0.00001);
        assertEquals(20, view.getResolutionY(), 0.00001);

        Transform transform = view.getTransform(new Transform());
        assertEquals(new Vect(0, 300), transform.transform(new Vect(2000, 1000), new Vect()));
        assertEquals(new Vect(0, 0), transform.transform(new Vect(2000, 7000), new Vect()));
        assertEquals(new Vect(400, 300), transform.transform(new Vect(6000, 1000), new Vect()));
        assertEquals(new Vect(400, 0), transform.transform(new Vect(6000, 7000), new Vect()));
        assertEquals(new Vect(200, 150), transform.transform(new Vect(4000, 4000), new Vect()));
    }

    @Test
    public void test_B() {
        View view = new View(new Vect(4000, 2500), 5, 800, 600);

        assertEquals(new Rect(2000, 1000, 6000, 4000), view.getBounds(new Rect()));
        assertEquals(new Vect(4000, 2500), view.getCenter(new Vect()));
        assertEquals(800, view.getWidthPx(), 0.00001);
        assertEquals(600, view.getHeightPx(), 0.00001);
        assertEquals(5, view.getResolutionX(), 0.00001);
        assertEquals(5, view.getResolutionY(), 0.00001);

        Transform transform = view.getTransform(new Transform());
        assertEquals(new Vect(0, 600), transform.transform(new Vect(2000, 1000), new Vect()));
        assertEquals(new Vect(0, 0), transform.transform(new Vect(2000, 4000), new Vect()));
        assertEquals(new Vect(800, 600), transform.transform(new Vect(6000, 1000), new Vect()));
        assertEquals(new Vect(800, 0), transform.transform(new Vect(6000, 4000), new Vect()));
        assertEquals(new Vect(400, 300), transform.transform(new Vect(4000, 2500), new Vect()));
    }

    @Test
    public void test_C() {
        View view = new View(new Vect(4000, 2500), 10, 20, 400, 300);

        assertEquals(new Rect(2000, -500, 6000, 5500), view.getBounds(new Rect()));
        assertEquals(new Vect(4000, 2500), view.getCenter(new Vect()));
        assertEquals(400, view.getWidthPx(), 0.00001);
        assertEquals(300, view.getHeightPx(), 0.00001);
        assertEquals(10, view.getResolutionX(), 0.00001);
        assertEquals(20, view.getResolutionY(), 0.00001);

        Transform transform = view.getTransform(new Transform());
        assertEquals(new Vect(0, 225), transform.transform(new Vect(2000, 1000), new Vect()));
        assertEquals(new Vect(0, 75), transform.transform(new Vect(2000, 4000), new Vect()));
        assertEquals(new Vect(400, 225), transform.transform(new Vect(6000, 1000), new Vect()));
        assertEquals(new Vect(400, 75), transform.transform(new Vect(6000, 4000), new Vect()));
        assertEquals(new Vect(200, 150), transform.transform(new Vect(4000, 2500), new Vect()));
    }

    @Test
    public void testEquals() {
        View a = new View(new Rect(2000, 1000, 6000, 7000), 400, 300);
        View b = new View(new Vect(4000, 4000), 10, 20, 400, 300);
        assertEquals(a, b);
        assertFalse(a.equals(null));
        assertFalse(a.equals(new View(new Rect(2000, 1000, 6000, 7001), 400, 300)));
        assertFalse(a.equals(new View(new Rect(2000, 1000, 6000, 7000), 401, 300)));
        assertFalse(a.equals(new View(new Rect(2000, 1000, 6000, 7000), 400, 301)));
    }

    @Test
    public void testHashCode() {
        View a = new View(new Rect(2000, 1000, 6000, 7000), 400, 300);
        View b = new View(new Vect(4000, 4000), 10, 20, 400, 300);
        assertEquals(a.hashCode(), b.hashCode());
        assertFalse(a.hashCode() == new View(new Rect(2000, 1000, 6000, 7001), 400, 300).hashCode());
        assertFalse(a.hashCode() == new View(new Rect(2000, 1000, 6000, 7000), 401, 300).hashCode());
        assertFalse(a.hashCode() == new View(new Rect(2000, 1000, 6000, 7000), 400, 301).hashCode());
    }

    @Test
    public void testClone() {
        View a = new View(new Rect(2000, 1000, 6000, 7000), 400, 300);
        assertSame(a, a.clone());
    }

    @Test
    public void testToString() {
        View a = new View(new Rect(2000, 1000, 6000, 7000), 400, 300);
        assertEquals("{bounds:[2000,1000,6000,7000],widthPx:400,heightPx:300}", a.toString());
    }

    @Test
    public void testExternaliize() throws IOException, ClassNotFoundException {
        View a = new View(new Rect(2000, 1000, 6000, 7000), 400, 300);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
            out.writeObject(a);
        }
        View b;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = (View) in.readObject();
        }
        assertEquals(a, b);
        bout = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
            a.writeData(out);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = View.read(in);
        }
        assertEquals(a, b);
    }

    @Test
    public void testZoom() {
        View a = new View(new Rect(2000, 1000, 6000, 7000), 400, 300);
        View b = a.zoom(0.5);
        assertEquals(a.getCenter(new Vect()), b.getCenter(new Vect()));
        assertEquals(new View(new Rect(3000, 2500, 5000, 5500), 400, 300), b);
    }

    @Test
    public void testZoomTo() {
        View a = new View(new Rect(2000, 1000, 6000, 7000), 400, 300);
        View b = a.zoomTo(2);
        assertEquals(a.getCenter(new Vect()), b.getCenter(new Vect()));
        assertEquals(new View(new Rect(3600, 3700, 4400, 4300), 400, 300), b);
    }

    @Test
    public void testPanTo() {
        View a = new View(new Rect(2000, 1000, 6000, 7000), 400, 300);
        View b = a.panTo(new Vect(10000, 15000));
        assertEquals(new View(new Rect(8000, 12000, 12000, 18000), 400, 300), b);
    }
    
    
    @Test
    public void testResizeTo() {
        View a = new View(new Rect(2000, 1000, 6000, 7000), 400, 300);
        View b = a.resizeTo(640, 480);
        assertEquals(new View(new Rect(800,-800,7200,8800), 640, 480), b);
    }
}
