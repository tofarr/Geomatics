package org.jg.util;

import java.awt.geom.AffineTransform;
import org.jg.geom.Vect;

/**
 *
 * @author tofar_000
 */
public final class TransformBuilder {

    double m00;
    double m01;
    double m10;
    double m11;
    double m02;
    double m12;

    /**
     * Create a new identity matrix
     */
    public TransformBuilder() {
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
     * @throws IllegalArgumentException if an ordinate was infinite or NaN
     */
    public TransformBuilder(double m00, double m01, double m10, double m11, double m02, double m12) throws IllegalArgumentException {
        set(m00, m01, m10, m11, m02, m12);
    }

    /**
     * Create a new transform based on that given
     *
     * @param transform
     * @throws IllegalArgumentException if an ordinate was infinite or NaN
     * @throws NullPointerException if transform was null
     */
    public TransformBuilder(AffineTransform transform) throws IllegalArgumentException, NullPointerException {
        set(transform.getScaleX(), transform.getShearX(), transform.getShearY(), transform.getScaleY(), transform.getTranslateX(), transform.getTranslateY());
    }

    public Transform build() {
        return Transform.valueOf(m00, m01, m10, m11, m02, m12);
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
     * Reset to identity
     *
     * @return this
     */
    public TransformBuilder reset() {
        m00 = m11 = 1;
        m01 = m10 = m02 = m12 = 0;
        return this;
    }

    /**
     * Set the internal state of this transform to match that given
     *
     * @param transform
     * @return this
     * @throws NullPointerException if transform was null
     */
    public TransformBuilder set(Transform transform) throws NullPointerException {
        return setInternal(transform.m00, transform.m01, transform.m10, transform.m11, transform.m02, transform.m12);
    }

    /**
     * Set the internal state of this transform to match that given
     *
     * @param transform
     * @return this
     * @throws NullPointerException if transform was null
     */
    public TransformBuilder set(TransformBuilder transform) throws NullPointerException {
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
    public TransformBuilder set(double m00, double m01, double m10, double m11, double m02, double m12) throws IllegalArgumentException {
        Vect.check(m00, "Invalid m00 : {0}");
        Vect.check(m01, "Invalid m01 : {0}");
        Vect.check(m10, "Invalid m10 : {0}");
        Vect.check(m11, "Invalid m11 : {0}");
        Vect.check(m02, "Invalid m02 : {0}");
        Vect.check(m12, "Invalid m12 : {0}");
        return setInternal(m00, m01, m10, m11, m02, m12);
    }

    TransformBuilder setInternal(double m00, double m01, double m10, double m11, double m02, double m12) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
        this.m02 = m02;
        this.m12 = m12;
        return this;
    }

    /**
     * Translate by vector given. Effect of this operation is as if a matrix [1,0,0,1,tx,ty] was created and then added to this matrix
     *
     * @param tx
     * @param ty
     * @return this
     * @throws IllegalArgumentException if tx or ty was infinite or NaN
     */
    public TransformBuilder translate(double tx, double ty) throws IllegalArgumentException {
        Vect.check(tx, "Invalid tx {0}");
        Vect.check(ty, "Invalid ty {0}");
        return addInternal(1, 0, 0, 1, tx, ty);
    }

    /**
     * Scale by value given. Effect of this operation is as if a matrix [scale,0,0,scale,0,0] was created and then added to this matrix
     *
     * @param scale
     * @return this
     * @throws IllegalArgumentException if scale was infinite or NaN or 0
     */
    public TransformBuilder scale(double scale) throws IllegalArgumentException {
        return scale(scale, scale);
    }

    /**
     * Scale by values given. Effect of this operation is as if a matrix [sx,0,0,sy,0,0] was created and then added to this matrix
     *
     * @param sx
     * @param sy
     * @return this
     * @throws IllegalArgumentException if sx, sy was infinite or NaN
     */
    public TransformBuilder scale(double sx, double sy) throws IllegalArgumentException {
        Vect.check(sx, "Invalid sx {0}");
        Vect.check(sy, "Invalid sy {0}");
        if (sx == 0) {
            throw new IllegalArgumentException("sx must not be 0!");
        } else if (sy == 0) {
            throw new IllegalArgumentException("sy must not be 0!");
        }
        return addInternal(sx, 0, 0, sy, 0, 0);
    }

    /**
     * Scale around the point given : translate(-ox,-oy).scale(scale).translate(ox,oy)
     *
     * @param scale
     * @param ox
     * @param oy
     * @return this
     * @throws IllegalArgumentException if scale ox or oy was infinite or NaN
     */
    public TransformBuilder scaleAround(double scale, double ox, double oy) throws IllegalArgumentException {
        return scaleAround(scale, scale, ox, oy);
    }

    /**
     * Scale around the point given : same as translate(-ox,-oy).scale(scale).translate(ox,oy)
     *
     * @param sx scale x
     * @param sy scale y
     * @param ox origin x
     * @param oy origin y
     * @return this
     * @throws IllegalArgumentException if sx,sy,ox,oy is NaN or infinite
     */
    public TransformBuilder scaleAround(double sx, double sy, double ox, double oy) throws IllegalArgumentException {
        Vect.check(sx, "Invalid sx {0}");
        Vect.check(sy, "Invalid sy {0}");
        if (sx == 0) {
            throw new IllegalArgumentException("sx must not be 0!");
        } else if (sy == 0) {
            throw new IllegalArgumentException("sy must not be 0!");
        }
        return translate(-ox, -oy).scale(sx, sy).translate(ox, oy);
    }

    /**
     * Rotate degrees around origin. Effect of this operation is as if a matrix [cos,-sin,sin,cos,0,0] was created and then added to this
     * matrix
     *
     * @param degrees
     * @return
     * @throws IllegalArgumentException if degrees was infinite or NaN
     */
    public TransformBuilder rotateDegrees(double degrees) throws IllegalArgumentException {
        return rotateRadians(Math.toRadians(degrees));
    }

    /**
     * Rotate degrees around origin. Effect of this operation is as if a matrix [cos,-sin,sin,cos,0,0] was created and then added to this
     * matrix
     *
     * @param radians
     * @return
     * @throws IllegalArgumentException if radians was infinite or NaN
     */
    public TransformBuilder rotateRadians(double radians) throws IllegalArgumentException {
        Vect.check(radians, "Invalid rotation {0}");
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        return addInternal(cos, -sin, sin, cos, 0, 0);
    }

    /**
     * Rotate degrees around the point given: translate(-ox,-oy).rotate(degrees).translate(ox,oy)
     *
     * @param degrees
     * @param ox
     * @param oy
     * @return
     * @throws IllegalArgumentException if degrees/ox/oy was NaN or infinite
     */
    public TransformBuilder rotateDegreesAround(double degrees, double ox, double oy) throws IllegalArgumentException {
        return rotateRadiansAround(Math.toRadians(degrees), ox, oy);
    }

    /**
     * Rotate degrees around the point given: translate(-ox,-oy).rotate(radians).translate(ox,oy)
     *
     * @param radians
     * @param ox
     * @param oy
     * @return
     * @throws IllegalArgumentException if radians/ox/oy was NaN or infinite
     */
    public TransformBuilder rotateRadiansAround(double radians, double ox, double oy) throws IllegalArgumentException {
        Vect.check(radians, "Invalid rotation {0}");
        return translate(-ox, -oy).rotateRadians(radians).translate(ox, oy);
    }

    /**
     * Flip x axis for any points transformed points
     *
     * @return this
     */
    public TransformBuilder flipX() {
        return scale(-1, 1);
    }

    /**
     * Flip x axis for any points transformed points around the x value given translate(-ox,0).flipX().translate(ox,0)
     *
     * @param ox
     * @return this
     */
    public TransformBuilder flipXAround(double ox) throws IllegalArgumentException {
        return translate(-ox, 0).flipX().translate(ox, 0);
    }

    /**
     * Flip y axis for any points transformed points
     *
     * @return this
     */
    public TransformBuilder flipY() {
        return scale(1, -1);
    }

    /**
     * Flip x axis for any points transformed points around the x value given translate(0,-oy).flipY().translate(0,oy)
     *
     * @param oy
     * @return this
     */
    public TransformBuilder flipYAround(double oy) throws IllegalArgumentException {
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
    public TransformBuilder shear(double shx, double shy) throws IllegalArgumentException {
        Vect.check(shx, "Invalid shx {0}");
        Vect.check(shy, "Invalid shy {0}");
        return addInternal(1, shx, shy, 1, 0, 0);
    }

    /**
     * Add the transform given to this matrix. Order is this then the given matrix. (Unlike affine transform where order is given then
     * this.)
     *
     * @param matrix
     * @return this
     * @throws NullPointerException if matrix was null
     */
    public TransformBuilder add(TransformBuilder matrix) throws NullPointerException {
        return addInternal(matrix.m00, matrix.m01, matrix.m10, matrix.m11, matrix.m02, matrix.m12);
    }

    TransformBuilder addInternal(double n00, double n01, double n10, double n11, double n02, double n12) {
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
        return this;
    }

    /**
     * Set this matrix to its inverse value
     *
     * @return this
     */
    public TransformBuilder invert() {
        double det = m00 * m11 - m01 * m10;
        return set(m11 / det, -m10 / det,
                -m01 / det, m00 / det,
                (m01 * m12 - m11 * m02) / det,
                (m10 * m02 - m00 * m12) / det);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        Transform.toString(m00, m01, m10, m11, m02, m12, str);
        return str.toString();
    }

    @Override
    public TransformBuilder clone() {
        return new TransformBuilder(m00, m01, m10, m11, m02, m12);
    }
}
