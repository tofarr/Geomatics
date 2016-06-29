package org.geomatics.io.shapefile.shp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.geomatics.io.shapefile.IO;

/**
 *
 * @author tofarrell
 */
public enum ShapeType {

    NULL(0),
    POINT(1),
    POLYLINE(3),
    POLYGON(5),
    MULTIPOINT(8),
    POINT_Z(11),
    POLYLINE_Z(13),
    POLYGON_Z(15),
    MULTIPOINT_Z(18),
    POINT_M(21),
    POLYLINE_M(23),
    POLYGON_M(25),
    MULTIPOINT_M(28),
    MULTIPATCH(31);

    public final long code;

    ShapeType(long code) {
        this.code = code;
    }

    public static ShapeType fromCode(long code) throws IllegalArgumentException {
        for (ShapeType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code : " + code);
    }

    public static ShapeType read(InputStream in) throws IOException {
        return fromCode(IO.readUI32LE(in));
    }

    public void write(OutputStream out) throws IOException {
        IO.writeUI32LE(code, out);
    }
}
