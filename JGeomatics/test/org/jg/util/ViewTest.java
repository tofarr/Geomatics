package org.jg.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import org.jg.geom.GeomException;
import org.jg.geom.Rect;
import org.jg.geom.Vect;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar
 */
public class ViewTest {
    
    @Test
    public void testConstructorFailures() {
        Rect rect = Rect.valueOf(2000, 1000, 6000, 7000);
        Vect vect = Vect.valueOf(4500, 3500);
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
        View view = new View(Rect.valueOf(2000, 1000, 6000, 7000), 400, 300);

        assertEquals(Rect.valueOf(2000, 1000, 6000, 7000), view.getBounds());
        assertEquals(Vect.valueOf(4000, 4000), view.getCenter());
        assertEquals(400, view.getWidthPx(), 0.00001);
        assertEquals(300, view.getHeightPx(), 0.00001);
        assertEquals(10, view.getResolutionX(), 0.00001);
        assertEquals(20, view.getResolutionY(), 0.00001);

        Transform transform = view.getTransform();
        assertEquals(Vect.valueOf(0, 300), transform.transform(Vect.valueOf(2000, 1000)));
        assertEquals(Vect.valueOf(0, 0), transform.transform(Vect.valueOf(2000, 7000)));
        assertEquals(Vect.valueOf(400, 300), transform.transform(Vect.valueOf(6000, 1000)));
        assertEquals(Vect.valueOf(400, 0), transform.transform(Vect.valueOf(6000, 7000)));
        assertEquals(Vect.valueOf(200, 150), transform.transform(Vect.valueOf(4000, 4000)));
    }

    @Test
    public void test_B() {
        View view = new View(Vect.valueOf(4000, 2500), 5, 800, 600);

        assertEquals(Rect.valueOf(2000, 1000, 6000, 4000), view.getBounds());
        assertEquals(Vect.valueOf(4000, 2500), view.getCenter());
        assertEquals(800, view.getWidthPx(), 0.00001);
        assertEquals(600, view.getHeightPx(), 0.00001);
        assertEquals(5, view.getResolutionX(), 0.00001);
        assertEquals(5, view.getResolutionY(), 0.00001);

        Transform transform = view.getTransform();
        assertEquals(Vect.valueOf(0, 600), transform.transform(Vect.valueOf(2000, 1000)));
        assertEquals(Vect.valueOf(0, 0), transform.transform(Vect.valueOf(2000, 4000)));
        assertEquals(Vect.valueOf(800, 600), transform.transform(Vect.valueOf(6000, 1000)));
        assertEquals(Vect.valueOf(800, 0), transform.transform(Vect.valueOf(6000, 4000)));
        assertEquals(Vect.valueOf(400, 300), transform.transform(Vect.valueOf(4000, 2500)));
    }

    @Test
    public void test_C() {
        View view = new View(Vect.valueOf(4000, 2500), 10, 20, 400, 300);

        assertEquals(Rect.valueOf(2000, -500, 6000, 5500), view.getBounds());
        assertEquals(Vect.valueOf(4000, 2500), view.getCenter());
        assertEquals(400, view.getWidthPx(), 0.00001);
        assertEquals(300, view.getHeightPx(), 0.00001);
        assertEquals(10, view.getResolutionX(), 0.00001);
        assertEquals(20, view.getResolutionY(), 0.00001);

        Transform transform = view.getTransform();
        assertEquals(Vect.valueOf(0, 225), transform.transform(Vect.valueOf(2000, 1000)));
        assertEquals(Vect.valueOf(0, 75), transform.transform(Vect.valueOf(2000, 4000)));
        assertEquals(Vect.valueOf(400, 225), transform.transform(Vect.valueOf(6000, 1000)));
        assertEquals(Vect.valueOf(400, 75), transform.transform(Vect.valueOf(6000, 4000)));
        assertEquals(Vect.valueOf(200, 150), transform.transform(Vect.valueOf(4000, 2500)));
    }

    @Test
    public void testEquals() {
        View a = new View(Rect.valueOf(2000, 1000, 6000, 7000), 400, 300);
        View b = new View(Vect.valueOf(4000, 4000), 10, 20, 400, 300);
        assertEquals(a, b);
        assertFalse(a.equals(null));
        assertFalse(a.equals(new View(Rect.valueOf(2000, 1000, 6000, 7001), 400, 300)));
        assertFalse(a.equals(new View(Rect.valueOf(2000, 1000, 6000, 7000), 401, 300)));
        assertFalse(a.equals(new View(Rect.valueOf(2000, 1000, 6000, 7000), 400, 301)));
    }

    @Test
    public void testHashCode() {
        View a = new View(Rect.valueOf(2000, 1000, 6000, 7000), 400, 300);
        View b = new View(Vect.valueOf(4000, 4000), 10, 20, 400, 300);
        assertEquals(a.hashCode(), b.hashCode());
        assertFalse(a.hashCode() == new View(Rect.valueOf(2000, 1000, 6000, 7001), 400, 300).hashCode());
        assertFalse(a.hashCode() == new View(Rect.valueOf(2000, 1000, 6000, 7000), 401, 300).hashCode());
        assertFalse(a.hashCode() == new View(Rect.valueOf(2000, 1000, 6000, 7000), 400, 301).hashCode());
    }

    @Test
    public void testClone() {
        View a = new View(Rect.valueOf(2000, 1000, 6000, 7000), 400, 300);
        assertSame(a, a.clone());
    }

    @Test
    public void testToString() {
        View a = new View(Rect.valueOf(2000, 1000, 6000, 7000), 400, 300);
        assertEquals("{bounds:[2000,1000,6000,7000],widthPx:400,heightPx:300}", a.toString());
    }

    @Test
    public void testExternaliize() throws IOException, ClassNotFoundException {
        View a = new View(Rect.valueOf(2000, 1000, 6000, 7000), 400, 300);
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
            a.write(out);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = View.read(in);
        }
        assertEquals(a, b);
        
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
            View.read(new DataInputStream(new InputStream(){
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
    public void testZoom() {
        View a = new View(Rect.valueOf(2000, 1000, 6000, 7000), 400, 300);
        View b = a.zoom(0.5);
        assertEquals(a.getCenter(), b.getCenter());
        assertEquals(new View(Rect.valueOf(3000, 2500, 5000, 5500), 400, 300), b);
    }

    @Test
    public void testZoomTo() {
        View a = new View(Rect.valueOf(2000, 1000, 6000, 7000), 400, 300);
        View b = a.zoomTo(2);
        assertEquals(a.getCenter(), b.getCenter());
        assertEquals(new View(Rect.valueOf(3600, 3700, 4400, 4300), 400, 300), b);
    }

    @Test
    public void testPanTo() {
        View a = new View(Rect.valueOf(2000, 1000, 6000, 7000), 400, 300);
        View b = a.panTo(Vect.valueOf(10000, 15000));
        assertEquals(new View(Rect.valueOf(8000, 12000, 12000, 18000), 400, 300), b);
    }
    
    
    @Test
    public void testResizeTo() {
        View a = new View(Rect.valueOf(2000, 1000, 6000, 7000), 400, 300);
        View b = a.resizeTo(640, 480);
        assertEquals(new View(Rect.valueOf(800,-800,7200,8800), 640, 480), b);
    }
    
    @Test
    public void testExternalize() throws Exception {
        View a = new View(Rect.valueOf(2000, 1000, 6000, 7000), 400, 300);
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
            a.write(out);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = View.read(in);
        }
        assertEquals(a, b);
        
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
            Transform.read(new DataInputStream(new InputStream(){
                @Override
                public int read() throws IOException {
                    throw new IOException();
                }
            }));
            fail("Exception expected");
        }catch(GeomException ex){   
        }
    }

}