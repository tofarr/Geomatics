package org.geomatics.util;

import java.awt.geom.AffineTransform;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import org.geomatics.geom.GeomException;
import org.geomatics.geom.Vect;
import org.geomatics.geom.VectBuilder;

/**
 * Class representing an affine transfomrm. Different to the java affine
 * transform in that transforms are always applied in forward order, and
 * provides a fluent interface. (e.g.:
 * <pre>
 *  Transform t = new Transform().translate(1,2).scale(2);
 * //Same as
 * AffineTransform at = new AffineTransform();
 * at.scale(2);
 * at.translate(1,2);
 * </pre>
 *
 * @author tofar_000
 */
public final class Transform implements Cloneable, Serializable {

    /**
     * Mode indicating identity transform
     */
    public static final int NO_OP = 0;
    /**
     * Mode indicating translation
     */
    public static final int TRANSLATE = 1;
    /**
     * Mode indicating scaling
     */
    public static final int SCALE = 2;
    /**
     * Mode indicating shearing
     */
    public static final int SHEAR = 4;

    public static final Transform IDENTITY = new Transform(1, 0, 0, 1, 0, 0);

    public final double m00;
    public final double m01;
    public final double m10;
    public final double m11;
    public final double m02;
    public final double m12;
    public final int mode;

    private Transform(double m00, double m01, double m10, double m11, double m02, double m12, int mode) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
        this.m02 = m02;
        this.m12 = m12;
        this.mode = mode;
    }

    Transform(double m00, double m01, double m10, double m11, double m02, double m12) {
        this(m00, m01, m10, m11, m02, m12, calculateMode(m00, m01, m10, m11, m02, m12));
    }

    public static Transform valueOf(double m00, double m01, double m10, double m11, double m02, double m12) {
        Vect.check(m00, "Invalid m00 : {0}");
        Vect.check(m01, "Invalid m01 : {0}");
        Vect.check(m10, "Invalid m10 : {0}");
        Vect.check(m11, "Invalid m11 : {0}");
        Vect.check(m02, "Invalid m02 : {0}");
        Vect.check(m12, "Invalid m12 : {0}");
        int mode = calculateMode(m00, m01, m10, m11, m02, m12);
        return (mode == NO_OP) ? IDENTITY : new Transform(m00, m01, m10, m11, m02, m12, mode);
    }

    public static Transform valueOf(AffineTransform transform) {
        return valueOf(transform.getScaleX(), transform.getShearX(), transform.getShearY(), transform.getScaleY(), transform.getTranslateX(), transform.getTranslateY());
    }

    static int calculateMode(double m00, double m01, double m10, double m11, double m02, double m12) {
        int ret = NO_OP;
        if ((m02 != 0) || (m12 != 0)) {
            ret |= TRANSLATE;
        }
        if (!((m00 == 1) && (m11 == 1))) {
            ret |= SCALE;
        }
        if ((m01 != 0) || (m10 != 0)) {
            ret |= SHEAR;
        }
        return ret;
    }

    /**
     * Get the inverse of this transform
     *
     * @return this
     */
    public Transform getInverse() {
        double det = m00 * m11 - m01 * m10;
        
        //double m00, double m01, double m10, double m11, double m02, double m12
        return new Transform(
                m11 / det,
                -m01 / det,
                -m10 / det,
                m00 / det,
                (m01 * m12 - m11 * m02) / det,
                (m10 * m02 - m00 * m12) / det);
    }

    /**
     * Place a transformed version of the source vector given in the destination
     * vector given
     *
     * @param src
     * @param dst
     * @throws NullPointerException if src or dst was null
     */
    public void transform(VectBuilder src, VectBuilder dst) throws NullPointerException {
        double x = src.getX();
        double y = src.getY();
        switch (mode) {
            case NO_OP:
                dst.set(src);
                break;
            case (SCALE | SHEAR):
                dst.set((m00 * x) + (m01 * y),
                        (m10 * x) + (m11 * y));
                break;
            case (SCALE | TRANSLATE):
                dst.set((m00 * x) + m02,
                        (m11 * y) + m12);
                return;
            case SCALE:
                dst.set((m00 * x), (m11 * y));
                return;
            case (SHEAR | TRANSLATE):
                dst.set(x + (m01 * y) + m02,
                        y + (m10 * x) + m12);
                return;
            case SHEAR:
                dst.set(x + (m01 * y), y + (m10 * x));
                return;
            case TRANSLATE:
                dst.set(x + m02,
                        y + m12);
                return;
            default: // apply all
                dst.set((m00 * x) + (m01 * y) + m02,
                        (m10 * x) + (m11 * y) + m12);
        }
    }
    
    /**
     * Place a transformed version of the source vector given in the destination
     * vector given
     *
     * @param src
     * @throws NullPointerException if src was null
     */
    public Vect transform(Vect src) throws NullPointerException {
        double x = src.getX();
        double y = src.getY();
        switch (mode) {
            case NO_OP:
                return src;
            case (SCALE | SHEAR):
                return Vect.valueOf((m00 * x) + (m01 * y),
                        (m10 * x) + (m11 * y));
            case (SCALE | TRANSLATE):
                return Vect.valueOf((m00 * x) + m02,
                        (m11 * y) + m12);
            case SCALE:
                return Vect.valueOf((m00 * x), (m11 * y));
            case (SHEAR | TRANSLATE):
                return Vect.valueOf(x + (m01 * y) + m02,
                        y + (m10 * x) + m12);
            case SHEAR:
                return Vect.valueOf(x + (m01 * y), y + (m10 * x));
            case TRANSLATE:
                return Vect.valueOf(x + m02,
                        y + m12);
            default: // apply all
                return Vect.valueOf((m00 * x) + (m01 * y) + m02,
                        (m10 * x) + (m11 * y) + m12);
        }
    }

    /**
     * Transform vectors from the src array given and place them in the dst
     * array given
     *
     * @param src src array
     * @param srcIndex index within src array
     * @param target dst array
     * @param targetIndex index within dst array
     * @param numVects number of vectors to transform
     * @throws NullPointerException if src or dst was null
     * @throws IndexOutOfBoundsException if an index was out of bounds
     * @throws IllegalArgumentException if numVects < 0
     */
    public void transformOrds(double[] src, int srcIndex, double[] target, int targetIndex, int numVects) throws NullPointerException, IndexOutOfBoundsException, IllegalArgumentException {
        if (numVects < 0) {
            throw new IllegalArgumentException("NumVects must be >= 0");
        }
        int numOrds = numVects << 1;
        int srcEnd = srcIndex + numOrds;
        if ((srcIndex < 0) || (srcEnd > src.length)) {
            throw new IndexOutOfBoundsException("Invalid range [" + srcIndex + "," + srcEnd + "] in array of length " + src.length);
        }
        if ((targetIndex < 0) || ((targetIndex + numOrds) > target.length)) {
            throw new IndexOutOfBoundsException("Invalid range [" + targetIndex + "," + (targetIndex + numOrds) + "] in array of length " + target.length);
        }
        if ((src == target) && (srcIndex != targetIndex)) {
            src = src.clone();
        }
        switch (mode) {
            case NO_OP:
                System.arraycopy(src, srcIndex, target, targetIndex, numOrds);
                return;
            case (SCALE | SHEAR):
                while (srcIndex < srcEnd) {
                    double x = src[srcIndex++];
                    double y = src[srcIndex++];
                    target[targetIndex++] = (m00 * x) + (m01 * y);
                    target[targetIndex++] = (m10 * x) + (m11 * y);
                }
                break;
            case (SCALE | TRANSLATE):
                while (srcIndex < srcEnd) {
                    double x = src[srcIndex++];
                    double y = src[srcIndex++];
                    target[targetIndex++] = (m00 * x) + m02;
                    target[targetIndex++] = (m11 * y) + m12;
                }
                break;
            case SCALE:
                while (srcIndex < srcEnd) {
                    double x = src[srcIndex++];
                    double y = src[srcIndex++];
                    target[targetIndex++] = (m00 * x);
                    target[targetIndex++] = (m11 * y);
                }
                break;
            case (SHEAR | TRANSLATE):
                while (srcIndex < srcEnd) {
                    double x = src[srcIndex++];
                    double y = src[srcIndex++];
                    target[targetIndex++] = x + (m01 * y) + m02;
                    target[targetIndex++] = y + (m10 * x) + m12;
                }
                break;
            case SHEAR:
                while (srcIndex < srcEnd) {
                    double x = src[srcIndex++];
                    double y = src[srcIndex++];
                    target[targetIndex++] = x + (m01 * y);
                    target[targetIndex++] = y + (m10 * x);
                }
                break;
            case TRANSLATE:
                while (srcIndex < srcEnd) {
                    double x = src[srcIndex++];
                    double y = src[srcIndex++];
                    target[targetIndex++] = x + m02;
                    target[targetIndex++] = y + m12;
                }
                break;
            default: // apply all
                while (srcIndex < srcEnd) {
                    double x = src[srcIndex++];
                    double y = src[srcIndex++];
                    target[targetIndex++] = (m00 * x) + (m01 * y) + m02;
                    target[targetIndex++] = (m10 * x) + (m11 * y) + m12;
                }
        }
    }

    /**
     * Get an array representing this matrix [m00, m01, m10, m11, m02, m12]
     *
     * @return
     */
    public double[] toArray() {
        return new double[]{m00, m01, m10, m11, m02, m12};
    }

    /**
     * Convert this instance to an AffineTransform
     *
     * @return
     */
    public AffineTransform toAffineTransform() {
        return new AffineTransform(m00, m10, m01, m11, m02, m12);
    }
    
    /**
     * Convert this instance to a TransformBuilder
     * 
     * @return
     */
    public TransformBuilder toBuilder(){
        return new TransformBuilder(m00, m01, m10, m11, m02, m12);
    }
    
    /**
     * Read a Transform from to the DataInput given
     *
     * @param in
     * @return a Transform
     * @throws NullPointerException if in was null
     * @throws IllegalArgumentException if the stream contained infinite or NaN ordinates
     * @throws GeomException if there was an IO error
     */
    public static Transform read(DataInput in) throws NullPointerException, IllegalArgumentException, GeomException{
        try {
            double m00 = in.readDouble();
            double m01 = in.readDouble();
            double m10 = in.readDouble();
            double m11 = in.readDouble();
            double m02 = in.readDouble();
            double m12 = in.readDouble();
            return valueOf(m00, m01, m10, m11, m02, m12);
        } catch (IOException ex) {
            throw new GeomException("Error reading VectList", ex);
        }
    }
    
    /**
     * Write this Transform to the DataOutput given
     *
     * @param out
     * @throws NullPointerException if out was null
     * @throws GeomException if there was an IO error
     */
    public void write(DataOutput out) throws NullPointerException, GeomException{
        try {
            out.writeDouble(m00);
            out.writeDouble(m01);
            out.writeDouble(m10);
            out.writeDouble(m11);
            out.writeDouble(m02);
            out.writeDouble(m12);
        } catch (IOException ex) {
            throw new GeomException("Error writing VectList", ex);
        }
    }
    

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        toString(str);
        return str.toString();
    }

    /**
     * Convert this transform to a string in the format
     * [m00,m01,m10,m11,m02,m12] and add it to the appendable given
     *
     * @param appendable
     * @throws GeomException if there was an output error
     * @throws NullPointerException if appendable was null
     */
    public void toString(Appendable appendable) throws GeomException, NullPointerException {
        toString(m00, m01, m10, m11, m02, m12, appendable);
    }
    
     static void toString(double m00, double m01, double m10, double m11, double m02, double m12, Appendable appendable) throws GeomException, NullPointerException {   
        try {
            appendable.append('[').append(Vect.ordToStr(m00)).append(',')
                    .append(Vect.ordToStr(m01)).append(',')
                    .append(Vect.ordToStr(m10)).append(',')
                    .append(Vect.ordToStr(m11)).append(',')
                    .append(Vect.ordToStr(m02)).append(',')
                    .append(Vect.ordToStr(m12)).append(']');
        } catch (IOException ex) {
            throw new GeomException("Error writing transform", ex);
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Vect.hash(m00);
        hash = 89 * hash + Vect.hash(m01);
        hash = 89 * hash + Vect.hash(m10);
        hash = 89 * hash + Vect.hash(m11);
        hash = 89 * hash + Vect.hash(m02);
        hash = 89 * hash + Vect.hash(m12);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Transform) {
            Transform matrix = (Transform) obj;
            return (m00 == matrix.m00)
                    && (m01 == matrix.m01)
                    && (m10 == matrix.m10)
                    && (m11 == matrix.m11)
                    && (m02 == matrix.m02)
                    && (m12 == matrix.m12);
        } else {
            return false;
        }
    }

    @Override
    public Transform clone() {
        return this;
    }
}
