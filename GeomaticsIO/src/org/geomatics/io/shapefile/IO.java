package org.geomatics.io.shapefile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * IO Utilities
 *
 * @author tofarrell
 */
public class IO {

    /**
     * Read integer from stream given in Little Endian format
     */
    public static long readUI32LE(InputStream in) throws IOException {
        return (in.read() & 0xFF)
                | ((in.read() & 0xFF) << 8)
                | ((in.read() & 0xFF) << 16)
                | ((long) (in.read() & 0xFF) << 24);
    }

    /**
     * Write integer from stream given in Little Endian format
     */
    public static void writeUI32LE(long value, OutputStream out) throws IOException {
        out.write((int) (value & 0xFF));
        out.write((int) ((value >> 8) & 0xFF));
        out.write((int) ((value >> 16) & 0xFF));
        out.write((int) ((value >> 24) & 0xFF));
    }

    /**
     * Read integer from stream given in Little Endian format
     */
    public static long readUI32BE(InputStream in) throws IOException {
        return ((long) (in.read() & 0xFF) << 24)
                | ((in.read() & 0xFF) << 16)
                | ((in.read() & 0xFF) << 8)
                | (in.read() & 0xFF);
    }

    /**
     * Write integer from stream given in Little Endian format
     */
    public static void writeUI32BE(long value, OutputStream out) throws IOException {
        out.write((int) ((value >> 24) & 0xFF));
        out.write((int) ((value >> 16) & 0xFF));
        out.write((int) ((value >> 8) & 0xFF));
        out.write((int) (value & 0xFF));

    }

    /**
     * Read integer from stream given in Big Endian format
     */
    public static long readLongBE(InputStream in) throws IOException {
        return (((long) (in.read() & 0xff) << 56)
                | ((long) (in.read() & 0xff) << 48)
                | ((long) (in.read() & 0xff) << 40)
                | ((long) (in.read() & 0xff) << 32)
                | ((long) (in.read() & 0xff) << 24)
                | ((long) (in.read() & 0xff) << 16)
                | ((long) (in.read() & 0xff) << 8)
                | ((long) (in.read() & 0xff)));
    }

    /**
     * Read integer from stream given in Little Endian format
     */
    public static long readLongLE(InputStream in) throws IOException {
        return (((long) (in.read() & 0xff))
                | ((long) (in.read() & 0xff) << 8)
                | ((long) (in.read() & 0xff) << 16)
                | ((long) (in.read() & 0xff) << 24)
                | ((long) (in.read() & 0xff) << 32)
                | ((long) (in.read() & 0xff) << 40)
                | ((long) (in.read() & 0xff) << 48)
                | ((long) (in.read() & 0xff) << 56));
    }

    public static double readDoubleBE(InputStream in) throws IOException {
        return Double.longBitsToDouble(readLongBE(in));
    }

    public static double readDoubleLE(InputStream in) throws IOException {
        return Double.longBitsToDouble(readLongLE(in));
    }

    /**
     * Write integer from stream given in Big Endian format
     */
    public static void writeLongBE(long value, OutputStream out) throws IOException {
        out.write((int) (value >> 56) & 0xFF);
        out.write((int) (value >> 48) & 0xFF);
        out.write((int) (value >> 40) & 0xFF);
        out.write((int) (value >> 32) & 0xFF);
        out.write((int) (value >> 24) & 0xFF);
        out.write((int) (value >> 16) & 0xFF);
        out.write((int) (value >> 8) & 0xFF);
        out.write((int) value & 0xFF);
    }

    /**
     * Write integer from stream given in Little Endian format
     */
    public static void writeLongLE(long value, OutputStream out) throws IOException {
        out.write((int) value & 0xFF);
        out.write((int) (value >> 8) & 0xFF);
        out.write((int) (value >> 16) & 0xFF);
        out.write((int) (value >> 24) & 0xFF);
        out.write((int) (value >> 32) & 0xFF);
        out.write((int) (value >> 40) & 0xFF);
        out.write((int) (value >> 48) & 0xFF);
        out.write((int) (value >> 56) & 0xFF);
    }

    public static void writeDoubleBE(double value, OutputStream out) throws IOException {
        writeLongBE(Double.doubleToLongBits(value), out);
    }

    public static void writeDoubleLE(double value, OutputStream out) throws IOException {
        writeLongLE(Double.doubleToLongBits(value), out);
    }

    /**
     * Read integer from stream given in Little Endian format
     */
    public static int readUI16LE(InputStream in) throws IOException {
        return ((in.read() & 0xFF)
                | ((in.read() & 0xFF) << 8));
    }

    /**
     * Write integer from stream given in Little Endian format
     */
    public static void writeUI16LE(int value, OutputStream out) throws IOException {
        out.write(value & 0xFF);
        out.write((value >> 8) & 0xFF);
    }

}
