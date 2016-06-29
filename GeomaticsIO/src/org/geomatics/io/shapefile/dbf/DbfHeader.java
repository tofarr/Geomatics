package org.geomatics.io.shapefile.dbf;

import java.beans.ConstructorProperties;
import java.beans.Transient;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.geomatics.io.shapefile.IO;

/**
 *
 * @author tofarrell
 */
public class DbfHeader {

    public DbfFileType type;
    private final DbfField[] fields;
    public final long lastModified;
    public final long numRecords;
    public final int firstRecordOffset;
    public final int numBytesPerRecord;
    public final byte tableFlags;
    public final byte codePageMark;

    @ConstructorProperties({"type", "fields", "lastModified", "numRecords", "firstRecordOffset", "numBytesPerRecord", "tableFlags", "codePageMark"})
    public DbfHeader(DbfFileType type, DbfField[] fields, long lastModified, long numRecords, int firstRecordOffset, int numBytesPerRecord, byte tableFlags, byte codePageMark) {
        if ((numRecords < 0) || (numRecords >= 0x100000000L)) {
            throw new IllegalArgumentException("invalid number of records : " + numRecords);
        }
        if (fields == null) {
            throw new IllegalArgumentException("Fields must be specified!");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(lastModified));
        int year = calendar.get(Calendar.YEAR);
        if ((year < 1900) || (year > 2155)) {
            throw new IllegalArgumentException("Invalid year : " + year);
        }
        if (firstRecordOffset <= 0) {
            throw new IllegalArgumentException("Invalid firstRecordOffset : " + firstRecordOffset + " (Value must be > 0)");
        }
        if (numBytesPerRecord < 0) {
            throw new IllegalArgumentException("Invalid numBytesPerRecord : " + numBytesPerRecord + " (Value must be >= 0)");
        }
        this.type = type;
        this.fields = fields.clone();
        this.lastModified = lastModified;
        this.numRecords = numRecords;
        this.firstRecordOffset = firstRecordOffset;
        this.numBytesPerRecord = numBytesPerRecord;
        this.tableFlags = tableFlags;
        this.codePageMark = codePageMark;
    }

    public DbfFileType getType() {
        return type;
    }

    public long getLastModified() {
        return lastModified;
    }

    public long getNumRecords() {
        return numRecords;
    }

    public int getFirstRecordOffset() {
        return firstRecordOffset;
    }

    public int getNumBytesPerRecord() {
        return numBytesPerRecord;
    }

    public byte getTableFlags() {
        return tableFlags;
    }

    public byte getCodePageMark() {
        return codePageMark;
    }

    public int numFields() {
        return fields.length;
    }

    public DbfField field(int index) throws IndexOutOfBoundsException {
        return fields[index];
    }

    public DbfField[] getFields() {
        return fields.clone();
    }

    public boolean hasCdx() {
        return (tableFlags & 0b1) != 0;
    }

    public boolean hasMemo() {
        return (tableFlags & 0b10) != 0;
    }

    @Transient
    public boolean isDatabase() {
        return (tableFlags & 0b100) != 0;
    }

    public static DbfHeader read(InputStream in) throws IOException {
        int len = in.available();
        DbfFileType type = DbfFileType.read(in);
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(in.read() + 1900, in.read() - 1, in.read());
        long lastModified = calendar.getTime().getTime();
        long numRecords = IO.readUI32LE(in);
        int firstRecordOffset = IO.readUI16LE(in);
        int numBytesPerRecord = IO.readUI16LE(in);
        in.skip(28 - 12); // reserved
        byte tableFlags = (byte) in.read();
        byte codePageMark = (byte) in.read();

        in.read(); // reserved - 2 zeros
        in.read();

        //Field descriptor here...
        List<DbfField> fieldList = new ArrayList<>();
        while (in.available() > (len + 1 - firstRecordOffset)) {
            DbfField field = DbfField.read(in);
            fieldList.add(field);
        }
        DbfField[] fields = fieldList.toArray(new DbfField[fieldList.size()]);

        if (in.read() != 0x0D) {
            throw new IllegalArgumentException("Did not find header record terminator where expected!");
        }

        return new DbfHeader(type, fields, lastModified, numRecords, firstRecordOffset, numBytesPerRecord, tableFlags, codePageMark);
    }

    public void write(OutputStream out) throws IOException {
        type.write(out);
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(new Date(lastModified));
        out.write(calendar.get(Calendar.YEAR) - 1900);
        out.write(calendar.get(Calendar.MONTH) + 1);
        out.write(calendar.get(Calendar.DATE));
        IO.writeUI32LE(numRecords, out);
        IO.writeUI16LE(firstRecordOffset, out);
        IO.writeUI16LE(numBytesPerRecord, out);
        for (int i = 12; i <= 27; i++) { // reserved
            out.write(0);
        }
        out.write(tableFlags);
        out.write(codePageMark);
        out.write(0);// reserved - 2 zeros
        out.write(0);

        //Field descriptor here...
        for (DbfField field : fields) {
            field.write(out);
        }
        out.write(0x0D); // Header record terminator
    }

    /**
     * Read a row - return true if the row was not deleted, otherwise skip and
     * return false
     * @param in
     * @param row
     * @return 
     * @throws java.io.IOException
     */
    public boolean readData(InputStream in, Object[] row) throws IOException {
        int flag = in.read();
        if (flag == 0x20) {
            for (int i = 0; i < fields.length; i++) {
                row[i] = fields[i].readData(in);
            }
            return true;
        } else {
            in.skip(numBytesPerRecord - 1);
            return false;
        }
    }

    public Object[] readData(InputStream in) throws IOException {
        Object[] row = new Object[fields.length];
        return readData(in, row) ? row : null;
    }

    /**
     * Write a row
     * @param row
     * @param out
     * @throws java.io.IOException
     */
    public void writeData(Object[] row, OutputStream out) throws IOException {
        out.write(0x20);
        for (int i = 0; i < fields.length; i++) {
            fields[i].writeData(row[i], out); // fields[i].readData(in);
        }
    }
}
