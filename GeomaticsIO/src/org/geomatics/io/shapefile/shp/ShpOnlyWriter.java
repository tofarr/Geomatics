package org.geomatics.io.shapefile.shp;

import org.geomatics.io.shapefile.IO;
import java.io.IOException;
import java.io.OutputStream;
import org.geomatics.geom.Rect;

/**
 *
 * @author tofarrell
 */
public class ShpOnlyWriter implements ShpWriter {

    private final OutputStream out;

    public ShpOnlyWriter(OutputStream out, ShpHeader header) throws IOException {
        this(out);
        header.write(out);
    }

    public ShpOnlyWriter(OutputStream out) {
        if (out == null) {
            throw new NullPointerException("Out must not be null");
        }
        this.out = out;

    }

    @Override
    public long write(long recordNumber, ShapeType shapeType, int[] segments, double[] ords, Rect bounds) throws IOException {
        long recordLength;
        IO.writeUI32BE(recordNumber, out);
        switch (shapeType) {
            case POINT:
                recordLength = 12;
                IO.writeUI32BE(recordLength, out); // length is 12 words
                IO.writeDoubleLE(ords[0], out);
                IO.writeDoubleLE(ords[1], out);
                break;
            case POLYLINE:
            case POLYGON:
                recordLength = ((segments.length * 4) + (ords.length * 8) + 44) / 2;
                IO.writeUI32BE(recordLength, out);
                writeBounds(bounds);
                IO.writeUI32LE(segments.length, out);
                IO.writeUI32LE(ords.length / 2, out);
                for (int i = 0; i < segments.length; i++) {
                    IO.writeUI32LE(segments[i], out);
                }
                writeOrds(ords);
                break;
            case MULTIPOINT:
                recordLength = (ords.length * 8) + 28;
                IO.writeUI32BE(recordLength, out);
                writeBounds(bounds);
                IO.writeUI32LE(ords.length / 2, out);
                writeOrds(ords);
                break;
            case NULL:
                recordLength = 4;
                IO.writeUI32BE(recordLength, out); // length is 4 words
                break;
            default:
                throw new UnsupportedOperationException("Unsupported shapeType: " + shapeType);
        }
        return recordLength;
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    private void writeBounds(Rect bounds) throws IOException {
        IO.writeDoubleLE(bounds.minX, out);
        IO.writeDoubleLE(bounds.minY, out);
        IO.writeDoubleLE(bounds.maxX, out);
        IO.writeDoubleLE(bounds.maxY, out);
    }

    private void writeOrds(double[] ords) throws IOException {
        for (int i = 0; i < ords.length; i++) {
            IO.writeDoubleLE(ords[i], out);
        }
    }
}
