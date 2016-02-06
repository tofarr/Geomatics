package org.jg;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/**
 * Tolerance for checking valies
 *
 * @author tim.ofarrell
 */
public final class Tolerance implements Serializable, Cloneable {

    public static final Tolerance DEFAULT = new Tolerance(0.00001);

    public final double tolerance;

    public Tolerance(double tolerance) throws IllegalArgumentException {
        Util.check(tolerance, "Invalid tolerance: {0}");
        this.tolerance = Math.abs(tolerance);
    }

    public double getTolerance() {
        return tolerance;
    }

    public int check(double value) {
        Util.check(value, "Invalid value {0}");
        if (value > tolerance) {
            return 1;
        } else if (value < -tolerance) {
            return -1;
        } else {
            return 0;
        }
    }

    public boolean match(double a, double b) {
        return check(a - b) == 0;
    }

    public boolean match(double ax, double ay, double bx, double by) {
        return match(ax, bx) && match(ay, by);
    }

    public Tolerance mostPrecise(Tolerance other) {
        return (tolerance <= other.tolerance) ? this : other;
    }

    public Tolerance leastPrecise(Tolerance other) {
        return (tolerance >= other.tolerance) ? this : other;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Util.hash(tolerance);
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

    public void writeData(DataOutput out) throws IOException {
        out.writeDouble(tolerance);
    }

    public static Tolerance read(DataInput in) throws IOException {
        return new Tolerance(in.readDouble());
    }

}
