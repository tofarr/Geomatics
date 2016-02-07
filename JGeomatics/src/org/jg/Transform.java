package org.jg;

import java.beans.Transient;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

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
public final class Transform implements Cloneable, Externalizable {

    /**
     * Mode indicating identity transform
     */
    public static final int IDENTITY = 0;
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

    double m00;
    double m01;
    double m10;
    double m11;
    double m02;
    double m12;
    int mode;

    /**
     * Create a new identity matrix
     */
    public Transform() {
        reset();
    }

    /**
     * Create a new matrix with the ordinates given
     *
     * @param m00
     * @param m01
     * @param m10
     * @param m11
     * @param m02
     * @param m12
     */
    public Transform(double m00, double m01, double m10, double m11, double m02, double m12) throws IllegalArgumentException {
        set(m00, m01, m10, m11, m02, m12);
    }

    /**
     * Get ordinate
     *
     * @return
     */
    public double getM00() {
        return m00;
    }

    /**
     * Get ordinate
     *
     * @return
     */
    public double getM01() {
        return m01;
    }

    /**
     * Get ordinate
     *
     * @return
     */
    public double getM10() {
        return m10;
    }

    /**
     * Get ordinate
     *
     * @return
     */
    public double getM11() {
        return m11;
    }

    /**
     * Get ordinate
     *
     * @return
     */
    public double getM02() {
        return m02;
    }

    /**
     * Get ordinate
     *
     * @return
     */
    public double getM12() {
        return m12;
    }

    /**
     * Get the mode for this transform - Bitmask combination of SCALE |
     * TRANSLATE | SHEAR
     *
     * @return mode
     */
    @Transient
    public int getMode() {
        return mode;
    }

    /**
     * Reset to identity
     *
     * @return this
     */
    public Transform reset() {
        m00 = m11 = 1;
        m01 = m10 = m02 = m12 = 0;
        mode = IDENTITY;
        return this;
    }

    /**
     * Set the internal state of this transform to match that given
     *
     * @param transform
     * @return this
     * @throws NullPointerException if transform was null
     */
    public Transform set(Transform transform) throws NullPointerException {
        return setInternal(transform.m00, transform.m01, transform.m10, transform.m11, transform.m02, transform.m12);
    }

    /**
     * Set the ordinates of this to the ordinates given
     *
     * @param m00
     * @param m01
     * @param m10
     * @param m11
     * @param m02
     * @param m12
     * @return this
     */
    public Transform set(double m00, double m01, double m10, double m11, double m02, double m12) throws IllegalArgumentException {
        Util.check(m00, "Invalid m00 : {0}");
        Util.check(m01, "Invalid m01 : {0}");
        Util.check(m10, "Invalid m10 : {0}");
        Util.check(m11, "Invalid m11 : {0}");
        Util.check(m02, "Invalid m02 : {0}");
        Util.check(m12, "Invalid m12 : {0}");
        return setInternal(m00, m01, m10, m11, m02, m12);
    }

    Transform setInternal(double m00, double m01, double m10, double m11, double m02, double m12) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
        this.m02 = m02;
        this.m12 = m12;
        this.mode = calculateMode(m00, m01, m10, m11, m02, m12);
        return this;
    }

    static int calculateMode(double m00, double m01, double m10, double m11, double m02, double m12) {
        int ret = IDENTITY;
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
     * Translate by vector given. Effect of this operation is as if a matrix
     * [1,0,0,1,tx,ty] was created and then added to this matrix
     *
     * @param tx
     * @param ty
     * @return this
     * @throws IllegalArgumentException if tx or ty was infinite or NaN
     */
    public Transform translate(double tx, double ty) throws IllegalArgumentException {
        Util.check(tx, "Invalid tx {0}");
        Util.check(ty, "Invalid ty {0}");
        return addInternal(1, 0, 0, 1, tx, ty);
    }

    /**
     * Scale by value given. Effect of this operation is as if a matrix
     * [scale,0,0,scale,0,0] was created and then added to this matrix
     *
     * @param scale
     * @return this
     * @throws IllegalArgumentException if scale was infinite or NaN or 0
     */
    public Transform scale(double scale) throws IllegalArgumentException {
        return scale(scale, scale);
    }

    /**
     * Scale by values given. Effect of this operation is as if a matrix
     * [sx,0,0,sy,0,0] was created and then added to this matrix
     *
     * @param sx
     * @param sy
     * @return this
     * @throws IllegalArgumentException if sx, sy was infinite or NaN
     */
    public Transform scale(double sx, double sy) throws IllegalArgumentException {
        Util.check(sx, "Invalid sx {0}");
        Util.check(sy, "Invalid sy {0}");
        if (sx == 0) {
            throw new IllegalArgumentException("sx must not be 0!");
        } else if (sy == 0) {
            throw new IllegalArgumentException("sy must not be 0!");
        }
        return addInternal(sx, 0, 0, sy, 0, 0);
    }

    /**
     * Scale around the point given :
     * translate(-ox,-oy).scale(scale).translate(ox,oy)
     *
     * @param scale
     * @param ox
     * @param oy
     * @return this
     * @throws IllegalArgumentException if scale ox or oy was infinite or NaN
     */
    public Transform scaleAround(double scale, double ox, double oy) throws IllegalArgumentException {
        return scaleAround(scale, scale, ox, oy);
    }

    /**
     * Scale around the point given : same as
     * translate(-ox,-oy).scale(scale).translate(ox,oy)
     *
     * @param sx scale x
     * @param sy scale y
     * @param ox origin x
     * @param oy origin y
     * @return this
     * @throws IllegalArgumentException if sx,sy,ox,oy is NaN or infinite
     */
    public Transform scaleAround(double sx, double sy, double ox, double oy) throws IllegalArgumentException {
        Util.check(sx, "Invalid sx {0}");
        Util.check(sy, "Invalid sy {0}");
        if (sx == 0) {
            throw new IllegalArgumentException("sx must not be 0!");
        } else if (sy == 0) {
            throw new IllegalArgumentException("sy must not be 0!");
        }
        return translate(-ox, -oy).scale(sx, sy).translate(ox, oy);
    }

    /**
     * Rotate degrees around origin. Effect of this operation is as if a matrix
     * [cos,-sin,sin,cos,0,0] was created and then added to this matrix
     *
     * @param degrees
     * @return
     * @throws IllegalArgumentException if degrees was infinite or NaN
     */
    public Transform rotateDegrees(double degrees) throws IllegalArgumentException {
        return rotateRadians(Math.toRadians(degrees));
    }

    /**
     * Rotate degrees around origin. Effect of this operation is as if a matrix
     * [cos,-sin,sin,cos,0,0] was created and then added to this matrix
     *
     * @param radians
     * @return
     * @throws IllegalArgumentException if radians was infinite or NaN
     */
    public Transform rotateRadians(double radians) throws IllegalArgumentException {
        Util.check(radians, "Invalid rotation {0}");
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        return addInternal(cos, -sin, sin, cos, 0, 0);
    }

    /**
     * Rotate degrees around the point given:
     * translate(-ox,-oy).rotate(degrees).translate(ox,oy)
     *
     * @param degrees
     * @param ox
     * @param oy
     * @return
     * @throws IllegalArgumentException if degrees/ox/oy was NaN or infinite
     */
    public Transform rotateDegreesAround(double degrees, double ox, double oy) throws IllegalArgumentException {
        return rotateRadiansAround(Math.toRadians(degrees), ox, oy);
    }

    /**
     * Rotate degrees around the point given:
     * translate(-ox,-oy).rotate(radians).translate(ox,oy)
     *
     * @param radians
     * @param ox
     * @param oy
     * @return
     * @throws IllegalArgumentException if radians/ox/oy was NaN or infinite
     */
    public Transform rotateRadiansAround(double radians, double ox, double oy) throws IllegalArgumentException {
        Util.check(radians, "Invalid rotation {0}");
        return translate(-ox, -oy).rotateRadians(radians).translate(ox, oy);
    }

    /**
     * Flip x axis for any points transformed points
     *
     * @return this
     */
    public Transform flipX() {
        return scale(-1, 1);
    }

    /**
     * Flip x axis for any points transformed points around the x value given
     * translate(-ox,0).flipX().translate(ox,0)
     *
     * @param ox
     * @return this
     */
    public Transform flipXAround(double ox) throws IllegalArgumentException {
        return translate(-ox, 0).flipX().translate(ox, 0);
    }

    /**
     * Flip y axis for any points transformed points
     *
     * @return this
     */
    public Transform flipY() {
        return scale(1, -1);
    }

    /**
     * Flip x axis for any points transformed points around the x value given
     * translate(0,-oy).flipY().translate(0,oy)
     *
     * @param oy
     * @return this
     */
    public Transform flipYAround(double oy) throws IllegalArgumentException {
        return translate(0, -oy).flipY().translate(0, oy);
    }

    /**
     * Shear by the values given
     *
     * @param shx
     * @param shy
     * @return this
     * @throws IllegalArgumentException if shx or shy was Infinite or NaN
     */
    public Transform shear(double shx, double shy) throws IllegalArgumentException {
        Util.check(shx, "Invalid shx {0}");
        Util.check(shy, "Invalid shy {0}");
        return addInternal(1, shx, shy, 1, 0, 0);
    }

    /**
     * Add the transform given to this matrix. Order is this then the given
     * matrix. (Unlike affine transform where order is given then this.)
     *
     * @param matrix
     * @return this
     * @throws NullPointerException if matrix was null
     */
    public Transform add(Transform matrix) throws NullPointerException {
        return addInternal(matrix.m00, matrix.m01, matrix.m10, matrix.m11, matrix.m02, matrix.m12);
    }

    Transform addInternal(double n00, double n01, double n10, double n11, double n02, double n12) {
        double p00 = n00 * m00 + n01 * m10;
        double p01 = n00 * m01 + n01 * m11;
        double p10 = n10 * m00 + n11 * m10;
        double p11 = n10 * m01 + n11 * m11;
        double p02 = n00 * m02 + n01 * m12 + n02;
        double p12 = n10 * m02 + n11 * m12 + n12;
        m00 = p00;
        m01 = p01;
        m10 = p10;
        m11 = p11;
        m02 = p02;
        m12 = p12;
        mode = calculateMode(m00, m01, m10, m11, m02, m12);
        return this;
    }

    /**
     * Set this matrix to its inverse value
     *
     * @return this
     */
    public Transform invert() {
        double det = m00 * m11 - m01 * m10;
        return set(m11 / det, -m10 / det,
                -m01 / det, m00 / det,
                (m01 * m12 - m11 * m02) / det,
                (m10 * m02 - m00 * m12) / det);
    }

    /**
     * Place a transformed version of the source vector given in the destination
     * vector given
     *
     * @param src
     * @param target
     * @return target
     * @throws NullPointerException if src or dst was null
     */
    public Vect transform(Vect src, Vect target) throws NullPointerException {
        double x = src.getX();
        double y = src.getY();
        switch (mode) {
            case IDENTITY:
                return target.set(src);
            case (SCALE | SHEAR):
                return target.set((m00 * x) + (m01 * y),
                        (m10 * x) + (m11 * y));
            case (SCALE | TRANSLATE):
                return target.set((m00 * x) + m02,
                        (m11 * y) + m12);
            case SCALE:
                return target.set((m00 * x), (m11 * y));
            case (SHEAR | TRANSLATE):
                return target.set(x + (m01 * y) + m02,
                        y + (m10 * x) + m12);
            case SHEAR:
                return target.set(x + (m01 * y), y + (m10 * x));
            case TRANSLATE:
                return target.set(x + m02,
                        y + m12);
            default: // apply all
                return target.set((m00 * x) + (m01 * y) + m02,
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
            case IDENTITY:
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

    @Override
    public String toString() {
        return "[" + Util.ordToStr(m00) + ',' + Util.ordToStr(m01)
                + ',' + Util.ordToStr(m10) + ',' + Util.ordToStr(m11)
                + ',' + Util.ordToStr(m02) + ',' + Util.ordToStr(m12) + ']';
    }

    /**
     * Convert this transform to a string in the format [m00,m01,m10,m11,m02,m12] and add it to
     * the appendable given
     *
     * @param appendable
     * @throws IOException if there was an output error
     * @throws NullPointerException if appendable was null
     */
    public void toString(Appendable appendable) throws IOException, NullPointerException {
        appendable.append('[').append(Util.ordToStr(m00)).append(',')
                .append(Util.ordToStr(m01)).append(',')
                .append(Util.ordToStr(m10)).append(',')
                .append(Util.ordToStr(m11)).append(',')
                .append(Util.ordToStr(m02)).append(',')
                .append(Util.ordToStr(m12)).append(']');
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Util.hash(m00);
        hash = 89 * hash + Util.hash(m01);
        hash = 89 * hash + Util.hash(m10);
        hash = 89 * hash + Util.hash(m11);
        hash = 89 * hash + Util.hash(m02);
        hash = 89 * hash + Util.hash(m12);
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
        return new Transform(m00, m01, m10, m11, m02, m12);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        writeData(out);
    }

    /**
     * Write this transform to the output given
     *
     * @param out
     * @throws IOException if out was null
     * @throws NullPointerException if out was null
     */
    public void writeData(DataOutput out) throws IOException, NullPointerException {
        out.writeDouble(m00);
        out.writeDouble(m01);
        out.writeDouble(m10);
        out.writeDouble(m11);
        out.writeDouble(m02);
        out.writeDouble(m12);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        readData(in);
    }

    /**
     * Set the ordinates for this transform from the input given
     * @param in
     * @return this
     * @throws IOException if there was an error
     * @throws NullPointerException if in was null
     */
    public Transform readData(DataInput in) throws IOException, NullPointerException {
        return set(in.readDouble(), in.readDouble(), in.readDouble(),
                in.readDouble(), in.readDouble(), in.readDouble());
    }

    /**
     * Read a transform from the input given
     * @param in
     * @return a line
     * @throws IOException if there was an error
     * @throws NullPointerException if in was null
     */
    public static Transform read(DataInput in) throws IOException, NullPointerException {
        return new Transform().readData(in);
    }
}
