package org.geomatics.io.shapefile.dbf;

import java.beans.ConstructorProperties;
import java.beans.Transient;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.geomatics.io.shapefile.IO;


/**
 *
 * @author tofarrell
 */
public class DbfField {

    public final String name;
    public final DbfFieldType type;
    public final long displacement;
    public final byte lengthInBytes;
    public final byte numDecimals;
    public final byte flags;
    public final long autoIncrementNext;
    public final byte autoIncrementStep;

    @ConstructorProperties({"name", "type", "displacement", "lengthInBytes", "numDecimals", "flags", "autoIncrementNext", "autoIncrementStep"})
    public DbfField(String name, DbfFieldType type, long displacement, byte lengthInBytes, byte numDecimals, byte flags, long autoIncrementNext, byte autoIncrementStep) {
        if (name.length() > 11) {
            throw new IllegalArgumentException("Name must be less than 11 characters in length");
        }
        if (displacement < 0) {
            throw new IllegalArgumentException("displacement (" + displacement + ") in bytes must be greater than or equal to 0");
        }
        if (lengthInBytes <= 0) {
            throw new IllegalArgumentException("lengthInBytes (" + lengthInBytes + ") in bytes must be greater than 0");
        }
        if (type == null) {
            throw new IllegalArgumentException("Missing type");
        }
        this.name = name;
        this.type = type;
        this.displacement = displacement;
        this.lengthInBytes = lengthInBytes;
        this.numDecimals = numDecimals;
        this.flags = flags;
        this.autoIncrementNext = autoIncrementNext;
        this.autoIncrementStep = autoIncrementStep;
    }

    public static DbfField read(InputStream in) throws IOException {
        byte[] nameBytes = new byte[11];
        int read = in.read(nameBytes);
        int end = nameBytes.length;
        for (int i = 0; i < nameBytes.length; i++) {
            if (nameBytes[i] == 0) {
                end = i;
                break;
            }
        }
        String name = new String(nameBytes, 0, end);
        DbfFieldType type = DbfFieldType.read(in);
        long displacement = IO.readUI32LE(in);
        byte lengthInBytes = (byte) in.read();
        byte numDecimals = (byte) in.read();
        byte flags = (byte) in.read();
        long autoIncrementNext = IO.readUI32LE(in);
        byte autoIncrementStep = (byte) in.read();
        in.skip(32 - 24);
        return new DbfField(name, type, displacement, lengthInBytes, numDecimals, flags, autoIncrementNext, autoIncrementStep);
    }

    public void write(OutputStream out) throws IOException {
        byte[] nameBytes = name.getBytes();
        for (int i = 0; i < 11; i++) {
            out.write((nameBytes.length <= i) ? 0 : nameBytes[i]);
        }
        type.write(out);
        IO.writeUI32LE(displacement, out);
        out.write(lengthInBytes);
        out.write(numDecimals);
        out.write(flags);
        IO.writeUI32LE(autoIncrementNext, out);
        out.write(autoIncrementStep);
        for (int i = 24; i <= 32; i++) {
            out.write(0);
        }
    }

    public Object readData(InputStream in) throws IOException {
        char[] buf = new char[lengthInBytes];
        int min = -1;
        int max = -1;
        for (int i = 0; i < lengthInBytes; i++) {
            char c = (char) in.read();
            buf[i] = c;
            if (c != 32) {
                if (min == -1) {
                    min = i;
                }
                max = i;
            }
        }
        min++;
        max++;
        String str = new String(buf, min, max - min);
        switch (type) {
            case CHARACTER: {
                return str;
            }
            case FLOAT: {
                return new Double(str);
            }
            default:
                throw new UnsupportedOperationException("Unsupported type : " + type);
        }
    }

    public void writeData(Object value, OutputStream out) throws IOException {
        switch (type) {
            case CHARACTER:
            case FLOAT: {
                String str = (value == null) ? "" : value.toString();
                for (int i = Math.min(lengthInBytes, str.length()); i-- > 0;) {
                    out.write(str.charAt(i));
                }
                for (int i = str.length(); i < lengthInBytes; i++) { // pad with spaces
                    out.write(32);
                }
            }
            default:
                throw new UnsupportedOperationException("Unsupported type : " + type);
        }
    }

    public String getName() {
        return name;
    }

    public DbfFieldType getType() {
        return type;
    }

    public long getDisplacement() {
        return displacement;
    }

    public byte getLengthInBytes() {
        return lengthInBytes;
    }

    public byte getNumDecimals() {
        return numDecimals;
    }

    public byte getFlags() {
        return flags;
    }

    public long getAutoIncrementNext() {
        return autoIncrementNext;
    }

    public byte getAutoIncrementStep() {
        return autoIncrementStep;
    }

    @Transient
    public boolean isSystemCol() {
        return (flags & 0x1) != 0;
    }

    @Transient
    public boolean isNullable() {
        return (flags & 0x2) != 0;
    }

    @Transient
    public boolean isBinary() {
        return (flags & 0x4) != 0;
    }

    @Transient
    public boolean isAutoIncrementing() {
        return (flags & 0xC) != 0;
    }

}
