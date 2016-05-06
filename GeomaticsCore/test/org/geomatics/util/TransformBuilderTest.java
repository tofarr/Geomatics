
package org.geomatics.util;

import org.geomatics.util.Tolerance;
import org.geomatics.util.TransformBuilder;
import org.geomatics.util.Transform;
import java.awt.geom.AffineTransform;
import java.util.Arrays;
import org.geomatics.geom.Vect;
import org.geomatics.geom.VectBuilder;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofarrell
 */
public class TransformBuilderTest {
    
    @Test
    public void testConstructor_AffineTransform(){
        AffineTransform at = new AffineTransform();
        at.scale(2, 3);
        at.translate(-1, -2);
        Transform transform = new TransformBuilder(at).build();
        VectBuilder src = new VectBuilder(11, 23);
        VectBuilder dst = new VectBuilder();
        transform.transform(src, dst);
        assertEquals(new VectBuilder(11,23), src);
        assertEquals(new VectBuilder(20,63), dst);
    }
    
    @Test
    public void testConstructor() {
        TransformBuilder transform = new TransformBuilder();
        assertEquals(1, transform.getM00(), 0.0001);
        assertEquals(0, transform.getM01(), 0.0001);
        assertEquals(0, transform.getM02(), 0.0001);
        assertEquals(0, transform.getM10(), 0.0001);
        assertEquals(1, transform.getM11(), 0.0001);
        assertEquals(0, transform.getM12(), 0.0001);
        assertEquals(Transform.NO_OP, transform.build().mode);
        assertEquals(Transform.IDENTITY, transform.build());
    }

    @Test
    public void testGetters() {
        TransformBuilder transform = new TransformBuilder(1, 2, 3, 4, 5, 6);
        assertEquals(1, transform.getM00(), 0.0001);
        assertEquals(2, transform.getM01(), 0.0001);
        assertEquals(3, transform.getM10(), 0.0001);
        assertEquals(4, transform.getM11(), 0.0001);
        assertEquals(5, transform.getM02(), 0.0001);
        assertEquals(6, transform.getM12(), 0.0001);
        assertEquals(Transform.SCALE | Transform.SHEAR | Transform.TRANSLATE, transform.build().mode);
    }
    
    @Test
    public void testGetMode() {
        TransformBuilder transform = new TransformBuilder();
        assertEquals(Transform.NO_OP, transform.build().mode);
        assertEquals(Transform.SCALE, transform.reset().scale(1, 2).build().mode);
        assertEquals(Transform.SCALE, transform.reset().scale(2, 1).build().mode);
        assertEquals(Transform.SCALE, transform.reset().scale(2).build().mode);
        assertEquals(Transform.SHEAR, transform.reset().shear(1, 2).build().mode);
        assertEquals(Transform.SHEAR, transform.reset().shear(2, 1).build().mode);
        assertEquals(Transform.TRANSLATE, transform.reset().translate(0, 1).build().mode);
        assertEquals(Transform.TRANSLATE, transform.reset().translate(1, 0).build().mode);
        assertEquals(Transform.SCALE | Transform.SHEAR, transform.reset().scale(2).shear(2, 1).build().mode);
        assertEquals(Transform.SCALE | Transform.TRANSLATE, transform.reset().scale(2).translate(1, 1).build().mode);
        assertEquals(Transform.SHEAR | Transform.TRANSLATE, transform.reset().shear(2, 1).translate(1, 1).build().mode);
        assertEquals(Transform.SCALE | Transform.SHEAR | Transform.TRANSLATE, transform.reset().scale(2).shear(2, 1).translate(1, 1).build().mode);
    }

    @Test
    public void testReset() {
        TransformBuilder transform = new TransformBuilder(1, 2, 3, 4, 5, 6);
        assertSame(transform, transform.reset());
        assertEquals(Transform.NO_OP, transform.build().mode);
        assertEquals(Transform.IDENTITY, transform.build());
    }

    @Test
    public void testSet() {
        TransformBuilder transform = new TransformBuilder(1, 2, 3, 4, 5, 6);
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
    }
    
    @Test
    public void testSet_Transform() {
        TransformBuilder transform = new TransformBuilder(1, 2, 3, 4, 5, 6);
        assertSame(transform, transform.set(Transform.valueOf(7, 8, 9, 10, 11, 12)));
        try {
            transform.set((Transform)null);
            fail("Exception expected");
        } catch (NullPointerException ex) { // expected
        }
        try {
            transform.set((TransformBuilder)null);
            fail("Exception expected");
        } catch (NullPointerException ex) { // expected
        }
        assertEquals("[7,8,9,10,11,12]", transform.toString());
        TransformBuilder b = new TransformBuilder().set(transform);
        assertEquals(transform, b);
    }

    @Test
    public void testTranslate() {
        TransformBuilder transform = new TransformBuilder().translate(3, 7);
        assertEquals(Vect.valueOf(16,24), transform.build().transform(Vect.valueOf(13, 17)));
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
        assertEquals(Vect.valueOf(12,21), transform.build().transform(Vect.valueOf(13, 17)));
    }

    @Test
    public void testScale() {
        TransformBuilder transform = new TransformBuilder().scale(2);
        assertEquals(Vect.valueOf(10,14), transform.build().transform(Vect.valueOf(5, 7)));
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
        assertEquals(Vect.valueOf(20,28), transform.build().transform(Vect.valueOf(5, 7)));
    }

    @Test
    public void testScaleAround() {
        TransformBuilder transform = new TransformBuilder().scaleAround(2, 7, 11);
        assertEquals(Vect.valueOf(7,11), transform.build().transform(Vect.valueOf(7, 11)));
        assertEquals(Vect.valueOf(9,13), transform.build().transform(Vect.valueOf(8, 12)));
        assertEquals(Vect.valueOf(11,15), transform.build().transform(Vect.valueOf(9, 13)));
        assertEquals(Vect.valueOf(5,9), transform.build().transform(Vect.valueOf(6, 10)));
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
        assertEquals(Vect.valueOf(3,7), transform.build().transform(Vect.valueOf(5, 9)));
    }

    @Test
    public void testRotateDegrees() {
        TransformBuilder transform = new TransformBuilder().rotateDegrees(45);
        Vect vect = transform.build().transform(Vect.valueOf(4, 4));
        double y = Math.sqrt(32);
        assertEquals(0, vect.x, 0.0001);
        assertEquals(y, vect.y, 0.0001);
        try {
            transform.rotateDegrees(Double.NaN);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        vect = transform.rotateDegrees(45).build().transform(Vect.valueOf(10, 0));
        assertEquals(0, vect.x, 0.0001);
        assertEquals(10, vect.y, 0.0001);
    }

    @Test
    public void testRotateRadians() {
        TransformBuilder transform = new TransformBuilder().rotateRadians(Math.PI / 4);
        Vect vect = transform.build().transform(Vect.valueOf(4, 4));
        double y = Math.sqrt(32);
        assertEquals(0, vect.x, 0.0001);
        assertEquals(y, vect.y, 0.0001);
        try {
            transform.rotateRadians(Double.POSITIVE_INFINITY);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        vect = transform.rotateRadians(Math.PI / 4).build().transform(Vect.valueOf(10, 0));
        assertEquals(0, vect.x, 0.0001);
        assertEquals(10, vect.y, 0.0001);
    }

    @Test
    public void testRotateDegreesAround() {
        TransformBuilder transform = new TransformBuilder().rotateDegreesAround(90, 3, 7);
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
        assertEquals(Vect.valueOf(1,9), transform.build().transform(Vect.valueOf(5, 9)));
        assertEquals(Vect.valueOf(3,9), transform.build().transform(Vect.valueOf(5, 7)));
        assertEquals(Vect.valueOf(1,7), transform.build().transform(Vect.valueOf(3, 9)));
        assertEquals(Vect.valueOf(3,7), transform.build().transform(Vect.valueOf(3, 7)));
        assertEquals(Vect.valueOf(1,5), transform.build().transform(Vect.valueOf(1, 9)));
        assertEquals(Vect.valueOf(3,5), transform.build().transform(Vect.valueOf(1, 7)));
        assertEquals(Vect.valueOf(5,5), transform.build().transform(Vect.valueOf(1, 5)));
        assertEquals(Vect.valueOf(5,7), transform.build().transform(Vect.valueOf(3, 5)));
        assertEquals(Vect.valueOf(5,9), transform.build().transform(Vect.valueOf(5, 5)));
    }

    /**
     * Test of rotateRadiansAround method, of class TransformBuilder.
     */
    @Test
    public void testRotateRadiansAround() {
        TransformBuilder transform = new TransformBuilder().translate(1, 0).rotateRadiansAround(Math.PI / 2, 3, 7);
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
        assertEquals(Vect.valueOf(1,10), transform.build().transform(Vect.valueOf(5, 9)));
        assertEquals(Vect.valueOf(3,10), transform.build().transform(Vect.valueOf(5, 7)));
        assertEquals(Vect.valueOf(1,8), transform.build().transform(Vect.valueOf(3, 9)));
        assertEquals(Vect.valueOf(3,8), transform.build().transform(Vect.valueOf(3, 7)));
        assertEquals(Vect.valueOf(1,6), transform.build().transform(Vect.valueOf(1, 9)));
        assertEquals(Vect.valueOf(3,6), transform.build().transform(Vect.valueOf(1, 7)));
        assertEquals(Vect.valueOf(5,6), transform.build().transform(Vect.valueOf(1, 5)));
        assertEquals(Vect.valueOf(5,8), transform.build().transform(Vect.valueOf(3, 5)));
        assertEquals(Vect.valueOf(5,10), transform.build().transform(Vect.valueOf(5, 5)));
    }

    @Test
    public void testFlipX() {
        TransformBuilder transform = new TransformBuilder().translate(2, 3).flipX();
        assertEquals(Vect.valueOf(-9,16), transform.build().transform(Vect.valueOf(7, 13)));
    }

    @Test
    public void testFlipXAround() {
        TransformBuilder transform = new TransformBuilder().translate(2, 3).flipXAround(4);
        try {
            transform.flipXAround(Double.NaN);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        assertEquals(Vect.valueOf(-1,16), transform.build().transform(Vect.valueOf(7, 13)));
    }

    @Test
    public void testFlipY() {
        TransformBuilder transform = new TransformBuilder().translate(2, 3).flipY();
        assertEquals(Vect.valueOf(9,-16), transform.build().transform(Vect.valueOf(7, 13)));
    }

    @Test
    public void testFlipYAround() {
        TransformBuilder transform = new TransformBuilder().translate(2, 3).flipYAround(4);
        try {
            transform.flipYAround(Double.NaN);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        assertEquals(Vect.valueOf(9,-8), transform.build().transform(Vect.valueOf(7, 13)));
    }

    @Test
    public void testShear() {
        TransformBuilder transform = new TransformBuilder().shear(2, 0);
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
        assertTrue(Vect.valueOf(0, 0).match(transform.build().transform(Vect.valueOf(0, 0)), Tolerance.DEFAULT));
        assertTrue(Vect.valueOf(2, 1).match(transform.build().transform(Vect.valueOf(0, 1)), Tolerance.DEFAULT));
        assertTrue(Vect.valueOf(1, 0).match(transform.build().transform(Vect.valueOf(1, 0)), Tolerance.DEFAULT));
        assertTrue(Vect.valueOf(5, 2).match(transform.build().transform(Vect.valueOf(1, 2)), Tolerance.DEFAULT));
        transform.reset().shear(0, 1); // full coverage
    }

    @Test
    public void testAdd() {
        TransformBuilder a = new TransformBuilder().translate(2, 3);
        TransformBuilder b = new TransformBuilder().scale(2);
        assertSame(a, a.add(b));
        try {
            a.add(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            ///expected
        }
        assertEquals(new TransformBuilder().scale(2), b);
        assertEquals(Vect.valueOf(18,32), a.build().transform(Vect.valueOf(7, 13)));
    }

    @Test
    public void testInvert() {
        TransformBuilder a = new TransformBuilder().translate(2, 3).scale(2).invert();
        assertEquals(Vect.valueOf(4,9), a.build().transform(Vect.valueOf(12, 24)));
    }

    @Test
    public void testTransform() {
        TransformBuilder transform = new TransformBuilder().scale(2).translate(3, 7);
        Vect a = Vect.valueOf(13, 17);
        Vect b = transform.build().transform(a);
        assertEquals(Vect.valueOf(13,17), a);
        assertEquals(Vect.valueOf(29,41), b);
        transform.build().transform(a);
        assertEquals(Vect.valueOf(13,17), a);
        assertEquals(Vect.valueOf(29,41), b);
        try {
            transform.build().transform(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
            ///expected
        }
        assertEquals(Vect.valueOf(13,17), transform.reset().build().transform(a));
        assertEquals(Vect.valueOf(31,43), transform.shear(1, 2).translate(1, 0).build().transform(a));
    }

    @Test
    public void testTransformBuilderOrds() {
        TransformBuilder transform = new TransformBuilder().scale(2).translate(3, 7);
        double[] src = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        double[] dst = new double[10];
        transform.build().transformOrds(src, 1, dst, 2, 3);
        try {
            transform.build().transformOrds(src, 1, dst, 2, -1);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            ///expected
        }
        try {
            transform.build().transformOrds(src, -1, dst, 2, 3);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            ///expected
        }
        try {
            transform.build().transformOrds(src, 1, dst, -1, 3);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            ///expected
        }
        try {
            transform.build().transformOrds(src, 1, dst, 6, 3);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            ///expected
        }
        try {
            transform.build().transformOrds(src, 6, dst, 1, 3);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
            ///expected
        }
        assertArrayEquals(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0}, src, 0.000001);
        assertArrayEquals(new double[]{0, 0, 7, 13, 11, 17, 15, 21, 0, 0}, dst, 0.000001);
        transform.build().transformOrds(src, 1, src, 2, 3);
        assertArrayEquals(new double[]{1, 2, 7, 13, 11, 17, 15, 21, 9, 0}, src, 0.000001);
        transform.build().transformOrds(src, 1, src, 1, 3);
        assertArrayEquals(new double[]{1, 7, 21, 29, 29, 37, 37, 21, 9, 0}, src, 0.000001);
        src = new double[]{1, 2};
        dst = new double[]{1, 2};
        transform.reset().build().transformOrds(src, 0, dst, 0, 1);
        assertArrayEquals(new double[]{1, 2}, dst, 0.000001);
        transform.scale(2).build().transformOrds(src, 0, dst, 0, 1);
        assertArrayEquals(new double[]{2, 4}, dst, 0.000001);
        transform.reset().scale(2).shear(1, 2).build().transformOrds(src, 0, dst, 0, 1);
        assertArrayEquals(new double[]{6, 8}, dst, 0.000001);
        transform.reset().shear(2, 1).translate(1, 2).build().transformOrds(src, 0, dst, 0, 1);
        assertArrayEquals(new double[]{6, 5}, dst, 0.000001);
        transform.reset().shear(2, 1).build().transformOrds(src, 0, dst, 0, 1);
        assertArrayEquals(new double[]{5, 3}, dst, 0.000001);
        transform.reset().translate(1, 2).build().transformOrds(src, 0, dst, 0, 1);
        assertArrayEquals(new double[]{2, 4}, dst, 0.000001);
        transform.reset().scale(2).shear(2, 1).translate(1, 0).build().transformOrds(src, 0, dst, 0, 1);
        assertArrayEquals(new double[]{11, 6}, dst, 0.000001);
    }

    @Test
    public void testToArray() {
        TransformBuilder transform = new TransformBuilder(1, 2, 3, 4, 5, 6);
        assertTrue(Arrays.equals(new double[]{1, 2, 3, 4, 5, 6}, transform.build().toArray()));
    }

    @Test
    public void testToString() {
        TransformBuilder transform = new TransformBuilder(1, 2, 3, 4, 5, 6);
        assertEquals("[1,2,3,4,5,6]", transform.toString());
        transform.set(1.5, 2.5, 3.5, 4.5, 5.5, 6.5);
        assertEquals("[1.5,2.5,3.5,4.5,5.5,6.5]", transform.toString());
    }

    @Test
    public void testHashCode() {
        TransformBuilder a = new TransformBuilder(1, 2, 3, 4, 5, 6);
        TransformBuilder b = new TransformBuilder(1, 2, 3, 4, 5, 6);
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
        TransformBuilder a = new TransformBuilder(1, 2, 3, 4, 5, 6);
        TransformBuilder b = new TransformBuilder(1, 2, 3, 4, 5, 6);
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
        TransformBuilder a = new TransformBuilder(1, 2, 3, 4, 5, 6);
        TransformBuilder b = a.clone();
        assertEquals(a, b);
        assertNotSame(a, b);
    }

    @Test
    public void testToString_Appendable() throws Exception {
        TransformBuilder transform = new TransformBuilder(1, 2, 3, 4, 5, 6);
        StringBuilder str = new StringBuilder();
        transform.build().toString(str);
        assertEquals("[1,2,3,4,5,6]", str.toString());
        transform.set(1.1, 2.2, 3.3, 4.4, 5.5, 6.6);
        str.setLength(0);
        transform.build().toString(str);
        assertEquals("[1.1,2.2,3.3,4.4,5.5,6.6]", str.toString());
    }  
    
    @Test
    public void testRotate(){
        assertEquals(Vect.valueOf(5, 5), new TransformBuilder().translate(1, 2).rotateRadians(0).build().transform(Vect.valueOf(4, 3)));
        assertEquals(Vect.valueOf(-5, 5), new TransformBuilder().translate(1, 2).rotateDegrees(90).build().transform(Vect.valueOf(4, 3)));
        assertEquals(Vect.valueOf(-5, -5), new TransformBuilder().translate(1, 2).rotateDegrees(180).build().transform(Vect.valueOf(4, 3)));
        assertEquals(Vect.valueOf(5, -5), new TransformBuilder().translate(1, 2).rotateDegrees(270).build().transform(Vect.valueOf(4, 3)));
        assertTrue(Vect.valueOf(0, 7.071).match(new TransformBuilder().translate(1, 2).rotateDegrees(45).build().transform(Vect.valueOf(4, 3)), new Tolerance(0.01)));
        
    }
}
