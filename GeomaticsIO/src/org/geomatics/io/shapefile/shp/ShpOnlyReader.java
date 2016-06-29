package org.geomatics.io.shapefile.shp;

import org.geomatics.io.shapefile.IO;
import java.io.IOException;
import java.io.InputStream;
import org.geomatics.geom.Rect;

/**
 *
 * @author tofarrell
 */
public class ShpOnlyReader implements ShpReader {

    private final InputStream in;
    private final ShpHeader header;
    private long recordNumber;
    private long recordLength;
    private ShapeType shapeType;
    private int[] segments;
    private double[] ords;
    private Rect bounds;

    public ShpOnlyReader(InputStream in) throws IOException {
        this.in = in;
        header = ShpHeader.read(in);
    }

    public ShpOnlyReader(InputStream in, ShpHeader header) {
        if (in == null) {
            throw new NullPointerException("In must not be null");
        }
        this.in = in;
        this.header = header;
    }

    public ShpHeader getHeader() {
        return header;
    }

    public long getRecordNumber() {
        return recordNumber;
    }

    public long getRecordLength() {
        return recordLength;
    }

    public ShapeType getShapeType() {
        return shapeType;
    }

    public int[] getSegments() {
        return segments;
    }

    public double[] getOrds() {
        return ords;
    }

    public Rect getBounds() {
        return bounds;
    }

    public boolean next() throws IOException {
        long startOffset = in.available();
        if(in.available() <= 0){
            recordNumber = recordLength = 0;
            shapeType = null;
            segments = null;
            ords = null;
            bounds = null;
            return false;
        }
        recordNumber = IO.readUI32BE(in);
        recordLength = IO.readUI32BE(in); // length in 16 bit words
        shapeType = ShapeType.read(in);
        switch (shapeType) {
            case POINT:
                segments = null;
                bounds = null;
                ords = new double[]{IO.readDoubleLE(in), IO.readDoubleLE(in)};
                break;
            case POLYLINE:
            case POLYGON:
                parsePoly();
                break;
            case MULTIPOINT:
                parseMultiPoint();
                break;
            case NULL:
                segments = null;
                bounds = null;
                ords = null;
                break;
            default:
                throw new UnsupportedOperationException("Unsupported shapeType: " + shapeType);
        }
        long target = startOffset - (8 + (recordLength * 2));
        if(in.available() != target){
            throw new IllegalStateException("Corrupt stream");
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    void parseMultiPoint() throws IOException {
        parseBounds();
        long numPoints = IO.readUI32LE(in);
        if ((numPoints * 16) + 28 != recordLength) {
            throw new IllegalStateException("Length for record " + recordNumber + " did not match number of points (" + numPoints + ")!");
        }
        parseOrds(numPoints);
    }

    void parsePoly() throws IOException {
        parseBounds();
        int numParts = (int) IO.readUI32LE(in);
        int numPoints = (int) IO.readUI32LE(in);
        if (((numParts * 4) + (numPoints * 8 * 2) + 44) != (recordLength * 2)) {
            throw new IllegalArgumentException("Length for record " + recordNumber + " did not match number of points (" + numPoints + ")!");
        }
        segments = new int[numParts];
        for (int s = 0; s < segments.length; s++) {
            int segment = (int) IO.readUI32LE(in);
            if (segment > numPoints) {
                throw new IllegalArgumentException("Part specified with index beyond file end for record " + recordNumber);
            }
            segments[s] = segment;

        }
        parseOrds(numPoints);
    }

    void parseBounds() throws IOException {
        bounds = Rect.valueOf(IO.readDoubleLE(in), IO.readDoubleLE(in), IO.readDoubleLE(in), IO.readDoubleLE(in));
    }

    void parseOrds(long numPoints) throws IOException {
        ords = new double[(int) numPoints * 2];
        for (int i = 0; i < ords.length; i++) {
            ords[i] = IO.readDoubleLE(in);
        }
    }
}
