package org.geomatics.io.shapefile;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.geomatics.geom.Rect;

/**
 * Immutable shape file header - first 100 bytes of a .shp/shx file
 *
 * @author tofarrell
 */
public class ShpHeader {

    public static final long FILE_CODE = 0x270a;
    public static final long VERSION = 1000;
    public final long fileLengthInWords;
    public final long version;
    public final ShapeType shapeType;
    public final Rect bounds;
    public final Rect zmBounds;

    @ConstructorProperties({"fileLengthInWords", "version", "shapeType", "bounds", "zmBounds"})
    public ShpHeader(long fileLengthInWords, long version, ShapeType shapeType, Rect bounds, Rect zmBounds) throws IllegalArgumentException {
        if (shapeType == null) {
            throw new IllegalArgumentException("Invalid shapeType : null");
        }
        if (fileLengthInWords < 0) {
            throw new IllegalArgumentException("Invalid fileLengthInWords : " + fileLengthInWords);
        }
        if (bounds == null) {
            throw new IllegalArgumentException("Invalid bounds : null");
        }
        if (zmBounds == null) {
            throw new IllegalArgumentException("Invalid zmBounds : null");
        }
        this.fileLengthInWords = fileLengthInWords;
        this.version = version;
        this.shapeType = shapeType;
        this.bounds = bounds;
        this.zmBounds = zmBounds;
    }

    public ShpHeader(long fileLengthInWords, ShapeType shapeType, Rect bounds, Rect zmBounds) {
        this(fileLengthInWords, VERSION, shapeType, bounds, zmBounds);
    }

    public static ShpHeader read(InputStream in) throws IOException {
        if(IO.readUI32BE(in) != FILE_CODE){
            throw new IOException("File code did not match that of a shape file!");
        }
        in.skip(5 * 4); // Unused; five uint32
        long fileLengthInWords = IO.readUI32BE(in);
        long version = IO.readUI32LE(in);
        ShapeType shapeType = ShapeType.read(in);
        Rect bounds = Rect.valueOf(IO.readDoubleLE(in), IO.readDoubleLE(in), IO.readDoubleLE(in), IO.readDoubleLE(in));
        double minZ = IO.readDoubleLE(in);
        double maxZ = IO.readDoubleLE(in);
        double minM = IO.readDoubleLE(in);
        double maxM = IO.readDoubleLE(in);
        Rect zmBounds = Rect.valueOf(minZ, minM, maxZ, maxM);
        return new ShpHeader(fileLengthInWords, version, shapeType, bounds, zmBounds);
    }

    public void write(OutputStream out) throws IOException {
        IO.writeUI32BE(FILE_CODE, out);
        for (int i = 0; i < (5*4); i++) { // Unused; five uint32
            out.write(0);
        }
        IO.writeUI32BE(fileLengthInWords, out);
        IO.writeUI32LE(version, out);
        shapeType.write(out);
        IO.writeDoubleLE(bounds.minX, out);
        IO.writeDoubleLE(bounds.minY, out);
        IO.writeDoubleLE(bounds.maxX, out);
        IO.writeDoubleLE(bounds.maxY, out);
        IO.writeDoubleLE(zmBounds.minX, out);
        IO.writeDoubleLE(zmBounds.maxX, out);
        IO.writeDoubleLE(zmBounds.minY, out);
        IO.writeDoubleLE(zmBounds.maxY, out);
    }
}
