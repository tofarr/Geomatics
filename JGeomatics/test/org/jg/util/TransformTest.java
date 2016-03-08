package org.jg.util;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import org.jg.geom.GeomException;
import org.jg.geom.Vect;
import org.jg.geom.VectBuilder;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofarrell
 */
public class TransformTest {

    @Test
    public void testValueOf() {
        AffineTransform at = new AffineTransform();
        at.scale(2, 3);
        at.translate(-1, -2);
        Transform transform = Transform.valueOf(at);
        VectBuilder src = new VectBuilder(11, 23);
        VectBuilder dst = new VectBuilder();
        transform.transform(src, dst);
        assertEquals("[11,23]", src.toString());
        assertEquals("[20,63]", dst.toString());
    }

    @Test
    public void testTransform() {
        VectBuilder src = new VectBuilder(7, 13);
        VectBuilder dst = new VectBuilder();
        Transform.IDENTITY.transform(src, dst);
        assertEquals("[7,13]", src.toString());
        assertEquals("[7,13]", dst.toString());
        new TransformBuilder().scale(3, 2).build().transform(src, dst);
        assertEquals("[7,13]", src.toString());
        assertEquals("[21,26]", dst.toString());
        new TransformBuilder().scale(3, 2).shear(-1, 3).build().transform(src, dst);
        assertEquals("[7,13]", src.toString());
        assertEquals("[-5,89]", dst.toString());
        new TransformBuilder().rotateDegreesAround(90, 11, 17).build().transform(src, dst);
        assertEquals("[7,13]", src.toString());
        assertEquals("[15,13]", dst.toString());
        new TransformBuilder().translate(3, 5).build().transform(src, dst);
        assertEquals("[7,13]", src.toString());
        assertEquals("[10,18]", dst.toString());
        new TransformBuilder().shear(2, 3).build().transform(src, dst);
        assertEquals("[7,13]", src.toString());
        assertEquals("[33,34]", dst.toString());
        new TransformBuilder().translate(3, 2).shear(-1, 3).build().transform(src, dst);
        assertEquals("[7,13]", src.toString());
        assertEquals("[-5,45]", dst.toString());
        new TransformBuilder().shear(0, 1).build().transform(src, dst);
        assertEquals("[7,13]", src.toString());
        assertEquals("[7,20]", dst.toString());
        new TransformBuilder().shear(1, 0).build().transform(src, dst);
        assertEquals("[7,13]", src.toString());
        assertEquals("[20,13]", dst.toString());
    }

    @Test
    public void testGetInverse() throws Exception {
        Transform a = new TransformBuilder().translate(2, 3).build();
        Transform b = a.getInverse();
        assertEquals(Vect.valueOf(5, 9), b.transform(a.transform(Vect.valueOf(5, 9))));
        
        a = new TransformBuilder().translate(2, 3).scale(3, 2).build();
        b = a.getInverse();
        assertEquals(Vect.valueOf(5, 9), b.transform(a.transform(Vect.valueOf(5, 9))));
        
        a = new TransformBuilder().rotateDegrees(90).build();
        b = a.getInverse();
        assertEquals(Vect.valueOf(5, 9), b.transform(a.transform(Vect.valueOf(5, 9))));
        
        a = new TransformBuilder().rotateDegreesAround(90, 3, 7).build();
        b = a.getInverse();
        assertEquals(Vect.valueOf(5, 9), b.transform(a.transform(Vect.valueOf(5, 9))));
        
        a = new TransformBuilder().rotateDegreesAround(40, 3, 7).build();
        b = a.getInverse();
        assertTrue(Vect.valueOf(5, 9).match(b.transform(a.transform(Vect.valueOf(5, 9))), Tolerance.DEFAULT));
    }

    @Test
    public void testToArray() {
        Transform transform = Transform.valueOf(1, 2, 3, 4, 5, 6);
        assertTrue(Arrays.equals(new double[]{1, 2, 3, 4, 5, 6}, transform.toArray()));
    }

    @Test
    public void testToAffineTransform() {
        Transform transform = Transform.valueOf(1, 2, 3, 4, 5, 6);
        AffineTransform at = transform.toAffineTransform();
        assertEquals(1, at.getScaleX(), 0.0001);
        assertEquals(4, at.getScaleY(), 0.0001);
        assertEquals(2, at.getShearX(), 0.0001);
        assertEquals(3, at.getShearY(), 0.0001);
        assertEquals(5, at.getTranslateX(), 0.0001);
        assertEquals(6, at.getTranslateY(), 0.0001);
    }

    @Test
    public void testToString() {
        Transform transform = Transform.valueOf(1, 2, 3, 4, 5, 6);
        assertEquals("[1,2,3,4,5,6]", transform.toString());
        transform = Transform.valueOf(1.5, 2.5, 3.5, 4.5, 5.5, 6.5);
        assertEquals("[1.5,2.5,3.5,4.5,5.5,6.5]", transform.toString());
        try {
            transform.toString(new Appendable() {
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
    public void testHashCode() {
        Transform a = Transform.valueOf(1, 2, 3, 4, 5, 6);
        Transform b = Transform.valueOf(1, 2, 3, 4, 5, 6);
        assertEquals(a.hashCode(), b.hashCode());
        b = Transform.valueOf(1, 2, 3, 4, 5, 7);
        assertFalse(a.hashCode() == b.hashCode());
        b = Transform.valueOf(1, 2, 3, 4, 7, 6);
        assertFalse(a.hashCode() == b.hashCode());
        b = Transform.valueOf(1, 2, 3, 7, 5, 6);
        assertFalse(a.hashCode() == b.hashCode());
        b = Transform.valueOf(1, 2, 7, 4, 5, 6);
        assertFalse(a.hashCode() == b.hashCode());
        b = Transform.valueOf(1, 7, 3, 4, 5, 6);
        assertFalse(a.hashCode() == b.hashCode());
        b = Transform.valueOf(7, 2, 3, 4, 5, 6);
        assertFalse(a.hashCode() == b.hashCode());
    }

    @Test
    public void testEquals() {
        Transform a = Transform.valueOf(1, 2, 3, 4, 5, 6);
        Transform b = Transform.valueOf(1, 2, 3, 4, 5, 6);
        assertEquals(a, b);
        b = Transform.valueOf(1, 2, 3, 4, 5, 7);
        assertFalse(a.equals(b));
        b = Transform.valueOf(1, 2, 3, 4, 7, 6);
        assertFalse(a.equals(b));
        b = Transform.valueOf(1, 2, 3, 7, 5, 6);
        assertFalse(a.equals(b));
        b = Transform.valueOf(1, 2, 7, 4, 5, 6);
        assertFalse(a.equals(b));
        b = Transform.valueOf(1, 7, 3, 4, 5, 6);
        assertFalse(a.equals(b));
        b = Transform.valueOf(7, 2, 3, 4, 5, 6);
        assertFalse(a.equals(b));
        assertFalse(a.equals(""));
    }

    @Test
    public void testClone() {
        Transform a = Transform.valueOf(1, 2, 3, 4, 5, 6);
        assertSame(a, a.clone());
    }

    @Test
    public void testExternalize() throws Exception {
        Transform a = Transform.valueOf(1, 2, 3, 4, 5, 6);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
            out.writeObject(a);
        }
        Transform b;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = (Transform) in.readObject();
        }
        assertEquals(a, b);
        bout = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
            a.write(out);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = Transform.read(in);
        }
        assertEquals(a, b);
    }

}
