package org.jg;

import org.jg.util.Tolerance;
import org.jg.util.Transform;
import org.jg.Vect;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar_000
 */
public class TransformTest {

    @Test
    public void testConstructor() {
        Transform transform = new Transform();
        assertEquals(1, transform.getM00(), 0.0001);
        assertEquals(0, transform.getM01(), 0.0001);
        assertEquals(0, transform.getM02(), 0.0001);
        assertEquals(0, transform.getM10(), 0.0001);
        assertEquals(1, transform.getM11(), 0.0001);
        assertEquals(0, transform.getM12(), 0.0001);
        assertEquals(Transform.IDENTITY, transform.getMode());
    }

    @Test
    public void testGetters() {
        Transform transform = new Transform(1, 2, 3, 4, 5, 6);
        assertEquals(1, transform.getM00(), 0.0001);
        assertEquals(2, transform.getM01(), 0.0001);
        assertEquals(3, transform.getM10(), 0.0001);
        assertEquals(4, transform.getM11(), 0.0001);
        assertEquals(5, transform.getM02(), 0.0001);
        assertEquals(6, transform.getM12(), 0.0001);
        assertEquals(Transform.SCALE | Transform.SHEAR | Transform.TRANSLATE, transform.getMode());
    }

    @Test
    public void testGetMode() {
        Transform transform = new Transform();
        assertEquals(Transform.IDENTITY, transform.getMode());
        assertEquals(Transform.SCALE, transform.reset().scale(1, 2).getMode());
        assertEquals(Transform.SCALE, transform.reset().scale(2, 1).getMode());
        assertEquals(Transform.SCALE, transform.reset().scale(2).getMode());
        assertEquals(Transform.SHEAR, transform.reset().shear(1, 2).getMode());
        assertEquals(Transform.SHEAR, transform.reset().shear(2, 1).getMode());
        assertEquals(Transform.TRANSLATE, transform.reset().translate(0, 1).getMode());
        assertEquals(Transform.TRANSLATE, transform.reset().translate(1, 0).getMode());
        assertEquals(Transform.SCALE | Transform.SHEAR, transform.reset().scale(2).shear(2, 1).getMode());
        assertEquals(Transform.SCALE | Transform.TRANSLATE, transform.reset().scale(2).translate(1, 1).getMode());
        assertEquals(Transform.SHEAR | Transform.TRANSLATE, transform.reset().shear(2, 1).translate(1, 1).getMode());
        assertEquals(Transform.SCALE | Transform.SHEAR | Transform.TRANSLATE, transform.reset().scale(2).shear(2, 1).translate(1, 1).getMode());

    }

    @Test
    public void testReset() {
        Transform transform = new Transform(1, 2, 3, 4, 5, 6);
        assertSame(transform, transform.reset());
        assertEquals(Transform.IDENTITY, transform.getMode());
    }

    @Test
    public void testSet() {
        Transform transform = new Transform(1, 2, 3, 4, 5, 6);
        assertSame(transform, transform.set(7, 8, 9, 10, 11, 12));
        try {
            transform.set(Double.NaN, 0, 0, 1, 0, 0);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) { // expected
        }
        try {
            transform.set(1, Double.POSITIVE_INFINITY, 0, 1, 0, 0);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) { // expected
        }
        try {
            transform.set(1, 0, Double.NEGATIVE_INFINITY, 1, 0, 0);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) { // expected
        }
        try {
            transform.set(1, 0, 0, Double.NaN, 0, 0);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) { // expected
        }
        try {
            transform.set(1, 0, 0, 1, Double.POSITIVE_INFINITY, 0);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) { // expected
        }
        try {
            transform.set(1, 0, 0, 1, 0, Double.NEGATIVE_INFINITY);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) { // expected
        }
        assertEquals("[7,8,9,10,11,12]", transform.toString());
        Transform b = new Transform().set(transform);
        assertEquals(transform, b);
    }

    @Test
    public void testTranslate() {
        Transform transform = new Transform().translate(3, 7);
        assertEquals("[16,24]", transform.transform(new Vect(13, 17), new Vect()).toString());
        assertSame(transform, transform.translate(-4, -3));
        try {
            transform.translate(Double.NEGATIVE_INFINITY, 0);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        try {
            transform.translate(0, Double.NaN);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        assertEquals("[12,21]", transform.transform(new Vect(13, 17), new Vect()).toString());
    }

    @Test
    public void testScale() {
        Transform transform = new Transform().scale(2);
        assertEquals("[10,14]", transform.transform(new Vect(5, 7), new Vect()).toString());
        assertSame(transform, transform.scale(2));
        try {
            transform.scale(Double.POSITIVE_INFINITY);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        try {
            transform.scale(0);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        try {
            transform.scale(1, 0);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        assertEquals("[20,28]", transform.transform(new Vect(5, 7), new Vect()).toString());
    }

    @Test
    public void testScaleAround() {
        Transform transform = new Transform().scaleAround(2, 7, 11);
        assertEquals("[7,11]", transform.transform(new Vect(7, 11), new Vect()).toString());
        assertEquals("[9,13]", transform.transform(new Vect(8, 12), new Vect()).toString());
        assertEquals("[11,15]", transform.transform(new Vect(9, 13), new Vect()).toString());
        assertEquals("[5,9]", transform.transform(new Vect(6, 10), new Vect()).toString());
        try {
            transform.scaleAround(2, 7, Double.NaN);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        try {
            transform.scaleAround(2, Double.POSITIVE_INFINITY, 11);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        try {
            transform.scaleAround(Double.NEGATIVE_INFINITY, 7, 11);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        try {
            transform.scaleAround(0, 7, 11);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        try {
            transform.scaleAround(1, 0, 7, 11);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        assertEquals("[3,7]", transform.transform(new Vect(5, 9), new Vect()).toString());
    }

    @Test
    public void testRotateDegrees() {
        Transform transform = new Transform().rotateDegrees(45);
        Vect vect = transform.transform(new Vect(4, 4), new Vect());
        double y = Math.sqrt(32);
        assertEquals(0, vect.x, 0.0001);
        assertEquals(y, vect.y, 0.0001);
        try {
            transform.rotateDegrees(Double.NaN);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        vect = transform.rotateDegrees(45).transform(new Vect(10, 0), new Vect());
        assertEquals(0, vect.x, 0.0001);
        assertEquals(10, vect.y, 0.0001);
    }

    @Test
    public void testRotateRadians() {
        Transform transform = new Transform().rotateRadians(Math.PI / 4);
        Vect vect = transform.transform(new Vect(4, 4), new Vect());
        double y = Math.sqrt(32);
        assertEquals(0, vect.x, 0.0001);
        assertEquals(y, vect.y, 0.0001);
        try {
            transform.rotateRadians(Double.POSITIVE_INFINITY);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        vect = transform.rotateRadians(Math.PI / 4).transform(new Vect(10, 0), new Vect());
        assertEquals(0, vect.x, 0.0001);
        assertEquals(10, vect.y, 0.0001);
    }

    @Test
    public void testRotateDegreesAround() {
        Transform transform = new Transform().rotateDegreesAround(90, 3, 7);
        try {
            transform.rotateDegreesAround(Double.NaN, 3, 7);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        try {
            transform.rotateDegreesAround(90, Double.POSITIVE_INFINITY, 7);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        try {
            transform.rotateDegreesAround(90, 3, Double.NEGATIVE_INFINITY);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        assertEquals("[1,9]", transform.transform(new Vect(5, 9), new Vect()).toString());
        assertEquals("[3,9]", transform.transform(new Vect(5, 7), new Vect()).toString());
        assertEquals("[1,7]", transform.transform(new Vect(3, 9), new Vect()).toString());
        assertEquals("[3,7]", transform.transform(new Vect(3, 7), new Vect()).toString());
        assertEquals("[1,5]", transform.transform(new Vect(1, 9), new Vect()).toString());
        assertEquals("[3,5]", transform.transform(new Vect(1, 7), new Vect()).toString());
        assertEquals("[5,5]", transform.transform(new Vect(1, 5), new Vect()).toString());
        assertEquals("[5,7]", transform.transform(new Vect(3, 5), new Vect()).toString());
        assertEquals("[5,9]", transform.transform(new Vect(5, 5), new Vect()).toString());
    }

    /**
     * Test of rotateRadiansAround method, of class Transform.
     */
    @Test
    public void testRotateRadiansAround() {
        Transform transform = new Transform().translate(1, 0).rotateRadiansAround(Math.PI / 2, 3, 7);
        try {
            transform.rotateRadiansAround(Double.NaN, 3, 7);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        try {
            transform.rotateRadiansAround(90, Double.POSITIVE_INFINITY, 7);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        try {
            transform.rotateRadiansAround(90, 3, Double.NEGATIVE_INFINITY);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        assertEquals("[1,10]", transform.transform(new Vect(5, 9), new Vect()).toString());
        assertEquals("[3,10]", transform.transform(new Vect(5, 7), new Vect()).toString());
        assertEquals("[1,8]", transform.transform(new Vect(3, 9), new Vect()).toString());
        assertEquals("[3,8]", transform.transform(new Vect(3, 7), new Vect()).toString());
        assertEquals("[1,6]", transform.transform(new Vect(1, 9), new Vect()).toString());
        assertEquals("[3,6]", transform.transform(new Vect(1, 7), new Vect()).toString());
        assertEquals("[5,6]", transform.transform(new Vect(1, 5), new Vect()).toString());
        assertEquals("[5,8]", transform.transform(new Vect(3, 5), new Vect()).toString());
        assertEquals("[5,10]", transform.transform(new Vect(5, 5), new Vect()).toString());
    }

    @Test
    public void testFlipX() {
        Transform transform = new Transform().translate(2, 3).flipX();
        assertEquals("[-9,16]", transform.transform(new Vect(7, 13), new Vect()).toString());
    }

    @Test
    public void testFlipXAround() {
        Transform transform = new Transform().translate(2, 3).flipXAround(4);
        try {
            transform.flipXAround(Double.NaN);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        assertEquals("[-1,16]", transform.transform(new Vect(7, 13), new Vect()).toString());
    }

    @Test
    public void testFlipY() {
        Transform transform = new Transform().translate(2, 3).flipY();
        assertEquals("[9,-16]", transform.transform(new Vect(7, 13), new Vect()).toString());
    }

    @Test
    public void testFlipYAround() {
        Transform transform = new Transform().translate(2, 3).flipYAround(4);
        try {
            transform.flipYAround(Double.NaN);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        assertEquals("[9,-8]", transform.transform(new Vect(7, 13), new Vect()).toString());
    }

    @Test
    public void testShear() {
        Transform transform = new Transform().shear(2, 0);
        try {
            transform.shear(Double.NaN, 2);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        try {
            transform.shear(2, Double.POSITIVE_INFINITY);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        assertTrue(new Vect(0, 0).match(transform.transform(new Vect(0, 0), new Vect()), Tolerance.DEFAULT));
        assertTrue(new Vect(2, 1).match(transform.transform(new Vect(0, 1), new Vect()), Tolerance.DEFAULT));
        assertTrue(new Vect(1, 0).match(transform.transform(new Vect(1, 0), new Vect()), Tolerance.DEFAULT));
        assertTrue(new Vect(5, 2).match(transform.transform(new Vect(1, 2), new Vect()), Tolerance.DEFAULT));
        transform.reset().shear(0, 1); // full coverage
    }

    @Test
    public void testAdd() {
        Transform a = new Transform().translate(2, 3);
        Transform b = new Transform().scale(2);
        assertSame(a, a.add(b));
        try {
            a.add(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            ///expected
        }
        assertEquals(new Transform().scale(2), b);
        assertEquals("[18,32]", a.transform(new Vect(7, 13), new Vect()).toString());
    }

    @Test
    public void testInvert() {
        Transform a = new Transform().translate(2, 3).scale(2).invert();
        assertEquals("[4,9]", a.transform(new Vect(12, 24), new Vect()).toString());
    }

    @Test
    public void testTransform() {
        Transform transform = new Transform().scale(2).translate(3, 7);
        Vect a = new Vect(13, 17);
        Vect b = new Vect();
        transform.transform(a, b);
        assertEquals("[13,17]", a.toString());
        assertEquals("[29,41]", b.toString());
        transform.transform(a, a);
        assertEquals("[29,41]", a.toString());
        a.set(13, 17);
        try {
            transform.transform(a, null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            ///expected
        }
        try {
            transform.transform(null, b);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            ///expected
        }
        assertEquals("[13,17]", a.toString());
        assertEquals("[29,41]", b.toString());
        assertEquals("[13,17]", transform.reset().transform(a, b).toString());
        assertEquals("[31,43]", transform.shear(1, 2).translate(1, 0).transform(a, b).toString());
    }

    @Test
    public void testTransformOrds() {
        Transform transform = new Transform().scale(2).translate(3, 7);
        double[] src = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        double[] dst = new double[10];
        transform.transformOrds(src, 1, dst, 2, 3);
        try {
            transform.transformOrds(src, 1, dst, 2, -1);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        try {
            transform.transformOrds(src, -1, dst, 2, 3);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            ///expected
        }
        try {
            transform.transformOrds(src, 1, dst, -1, 3);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            ///expected
        }
        try {
            transform.transformOrds(src, 1, dst, 6, 3);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            ///expected
        }
        try {
            transform.transformOrds(src, 6, dst, 1, 3);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            ///expected
        }
        assertArrayEquals(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0}, src, 0.000001);
        assertArrayEquals(new double[]{0, 0, 7, 13, 11, 17, 15, 21, 0, 0}, dst, 0.000001);
        transform.transformOrds(src, 1, src, 2, 3);
        assertArrayEquals(new double[]{1, 2, 7, 13, 11, 17, 15, 21, 9, 0}, src, 0.000001);
        transform.transformOrds(src, 1, src, 1, 3);
        assertArrayEquals(new double[]{1, 7, 21, 29, 29, 37, 37, 21, 9, 0}, src, 0.000001);
        src = new double[]{1, 2};
        dst = new double[]{1, 2};
        transform.reset().transformOrds(src, 0, dst, 0, 1);
        assertArrayEquals(new double[]{1, 2}, dst, 0.000001);
        transform.scale(2).transformOrds(src, 0, dst, 0, 1);
        assertArrayEquals(new double[]{2, 4}, dst, 0.000001);
        transform.reset().scale(2).shear(1, 2).transformOrds(src, 0, dst, 0, 1);
        assertArrayEquals(new double[]{6, 8}, dst, 0.000001);
        transform.reset().shear(2, 1).translate(1, 2).transformOrds(src, 0, dst, 0, 1);
        assertArrayEquals(new double[]{6, 5}, dst, 0.000001);
        transform.reset().shear(2, 1).transformOrds(src, 0, dst, 0, 1);
        assertArrayEquals(new double[]{5, 3}, dst, 0.000001);
        transform.reset().translate(1, 2).transformOrds(src, 0, dst, 0, 1);
        assertArrayEquals(new double[]{2, 4}, dst, 0.000001);
        transform.reset().scale(2).shear(2, 1).translate(1, 0).transformOrds(src, 0, dst, 0, 1);
        assertArrayEquals(new double[]{11, 6}, dst, 0.000001);
    }

    @Test
    public void testToArray() {
        Transform transform = new Transform(1, 2, 3, 4, 5, 6);
        assertTrue(Arrays.equals(new double[]{1, 2, 3, 4, 5, 6}, transform.toArray()));
    }

    @Test
    public void testToString() {
        Transform transform = new Transform(1, 2, 3, 4, 5, 6);
        assertEquals("[1,2,3,4,5,6]", transform.toString());
        transform.set(1.5, 2.5, 3.5, 4.5, 5.5, 6.5);
        assertEquals("[1.5,2.5,3.5,4.5,5.5,6.5]", transform.toString());
    }

    @Test
    public void testHashCode() {
        Transform a = new Transform(1, 2, 3, 4, 5, 6);
        Transform b = new Transform(1, 2, 3, 4, 5, 6);
        assertEquals(a.hashCode(), b.hashCode());
        b.set(1, 2, 3, 4, 5, 7);
        assertFalse(a.hashCode() == b.hashCode());
        b.set(1, 2, 3, 4, 7, 6);
        assertFalse(a.hashCode() == b.hashCode());
        b.set(1, 2, 3, 7, 5, 6);
        assertFalse(a.hashCode() == b.hashCode());
        b.set(1, 2, 7, 4, 5, 6);
        assertFalse(a.hashCode() == b.hashCode());
        b.set(1, 7, 3, 4, 5, 6);
        assertFalse(a.hashCode() == b.hashCode());
        b.set(7, 2, 3, 4, 5, 6);
        assertFalse(a.hashCode() == b.hashCode());
    }

    @Test
    public void testEquals() {
        Transform a = new Transform(1, 2, 3, 4, 5, 6);
        Transform b = new Transform(1, 2, 3, 4, 5, 6);
        assertEquals(a, b);
        b.set(1, 2, 3, 4, 5, 7);
        assertFalse(a.equals(b));
        b.set(1, 2, 3, 4, 7, 6);
        assertFalse(a.equals(b));
        b.set(1, 2, 3, 7, 5, 6);
        assertFalse(a.equals(b));
        b.set(1, 2, 7, 4, 5, 6);
        assertFalse(a.equals(b));
        b.set(1, 7, 3, 4, 5, 6);
        assertFalse(a.equals(b));
        b.set(7, 2, 3, 4, 5, 6);
        assertFalse(a.equals(b));
        assertFalse(a.equals(""));
    }

    @Test
    public void testClone() {
        Transform a = new Transform(1, 2, 3, 4, 5, 6);
        Transform b = a.clone();
        assertEquals(a, b);
        assertNotSame(a, b);
    }

    @Test
    public void testToString_Appendable() throws Exception {
        Transform transform = new Transform(1, 2, 3, 4, 5, 6);
        StringBuilder str = new StringBuilder();
        transform.toString(str);
        assertEquals("[1,2,3,4,5,6]", str.toString());
        transform.set(1.1, 2.2, 3.3, 4.4, 5.5, 6.6);
        str.setLength(0);
        transform.toString(str);
        assertEquals("[1.1,2.2,3.3,4.4,5.5,6.6]", str.toString());
    }

    @Test
    public void testExternalize() throws Exception {
        Transform a = new Transform(1, 2, 3, 4, 5, 6);
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
            a.writeData(out);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = Transform.read(in);
        }
        assertEquals(a, b);
    }

    @Test
    public void testToTransform(){
        AffineTransform a = new AffineTransform();
        a.translate(1, 2);
        Transform t = new Transform(a);
        assertEquals(new Vect(11, 22), t.transform(new Vect(10, 20), new Vect()));
        AffineTransform b = t.toAffineTransform();
        assertEquals(a, b);
        t = new Transform(1, 2, 3, 4, 5, 6);
        b = t.toAffineTransform();
        t = new Transform(b);
        assertEquals("[1,2,3,4,5,6]", t.toString());
        
    }
}
