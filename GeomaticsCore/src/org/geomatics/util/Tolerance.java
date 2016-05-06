package org.geomatics.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import org.geomatics.geom.Vect;

/**
 * Immutable Tolerance for checking values are within a specified amount. Used for
 * combating the effects of rounding errors when dealing with floating point
 * numbers.
 *
 * @author tim.ofarrell
 */
public final class Tolerance implements Serializable, Cloneable {

    /**
     * The default tolerance - 0.00001
     */
    public static final Tolerance DEFAULT = new Tolerance(0.00001);
    /**
     * No tolerance
     */
    public static final Tolerance ZERO = new Tolerance(0);
    /**
     * Tolerance value
     */
    public final double tolerance;
    /**
     * Square of tolerance 
     */
    public final double toleranceSq;

    /**
     * Create a new instance of Tolerance
     *
     * @param tolerance
     * @throws IllegalArgumentException if tolerance was infinite or NaN
     */
    public Tolerance(double tolerance) throws IllegalArgumentException {
        Vect.check(tolerance, "Invalid tolerance: {0}");
        this.tolerance = Math.abs(tolerance);
        this.toleranceSq = tolerance * tolerance;
    }

    /**
     * Get tolerance value
     *
     * @return
     */
    public double getTolerance() {
        return tolerance;
    }

    /**
     * Get square of tolerance
     * 
     * @return
     */
    public double getToleranceSq() {
        return toleranceSq;
    }

    /**
     * Check the value given against 0
     *
     * @param value
     * @return 1 if the value was greater than tolerance, -1 if the value was
     * less than -tolerance, otherwise 0
     */
    public int check(double value) {
        Vect.check(value, "Invalid value {0}");
        if (value > tolerance) {
            return 1;
        } else if (value < -tolerance) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * Determine if the values given are the same within this tolerance
     *
     * @param a
     * @param b
     * @return true if same, false otherwise
     */
    public boolean match(double a, double b) {
        return check(a - b) == 0;
    }

    /**
     * Determine if the vectors given are the same within this tolerance
     *
     * @param ax
     * @param ay
     * @param bx
     * @param by
     * @return true if same, false otherwise
     */
    public boolean match(double ax, double ay, double bx, double by) {
        double distSq = Vect.distSq(ax, ay, bx, by);
        return distSq <= toleranceSq;
    }

    /**
     * Get the most precise tolerance (smallest) between this and that given
     *
     * @param other
     * @return
     */
    public Tolerance mostPrecise(Tolerance other) {
        return (tolerance <= other.tolerance) ? this : other;
    }

    /**
     * Get the least precise tolerance (largest) between this and that given
     *
     * @param other
     * @return
     */
    public Tolerance leastPrecise(Tolerance other) {
        return (tolerance >= other.tolerance) ? this : other;
    }
    
    /**
     * Snap a value to a fixed stops based on this tolerance, aligned at 0. e.g.: If the tolerance
     * was 10, 0 - 5 would get snapped to 0, 5-15 would get snapped to 10, 15-25 would get snapped to
     * 20, etc...
     * @param value
     * @return
     */
    public double snap(double value){
        return (tolerance == 0) ? value : Math.round(value / tolerance) * tolerance;
    }

    @Override
    public String toString() {
        return Vect.ordToStr(tolerance);
    }
    
    /**
     * Convert this tolerance to a string in the format tolerance and add it to
     * the appendable given
     *
     * @param appendable
     * @throws IOException if there was an output error
     * @throws NullPointerException if appendable was null
     */
    public void toString(Appendable appendable) throws IOException, NullPointerException {
        appendable.append(Vect.ordToStr(tolerance));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Vect.hash(tolerance);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tolerance) {
            Tolerance other = (Tolerance) obj;
            return this.tolerance == other.tolerance;
        }
        return false;
    }

    @Override
    public Tolerance clone() {
        return this;
    }

    /**
     * Write this tolerance to the output given
     *
     * @param out
     * @throws IOException if out was null
     * @throws NullPointerException if out was null
     */
    public void writeData(DataOutput out) throws IOException, NullPointerException {
        out.writeDouble(tolerance);
    }

    /**
     * Read a tolerance from the input given
     *
     * @param in
     * @return a line
     * @throws IOException if there was an error
     * @throws NullPointerException if in was null
     */
    public static Tolerance read(DataInput in) throws IOException, NullPointerException {
        return new Tolerance(in.readDouble());
    }

}
