/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geomatics.io.shapefile.dbf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author tofarrell
 */
public enum DbfFieldType {

    CHARACTER('C'),
    CURRENCY('Y'),
    NUMERIC('N'),
    FLOAT('F'),
    DATE('D'),
    DATETIME('T'),
    DOUBLE('B'),
    INTEGER('I'),
    LOGICAL('L'),
    MEMO('M'),
    GENERAL('G'),
    PICTURE('P'),
    AUTOINCREMENT_DB7('+'),
    DOUBLE_DB7('O'),
    TIMESTAMP_DB7('@');

    public final char code;

    DbfFieldType(char code) {
        this.code = code;
    }

    public static DbfFieldType fromCode(char code) throws IllegalArgumentException {
        for (DbfFieldType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code : "+code);
    }

    public static DbfFieldType read(InputStream in) throws IOException {
        return fromCode((char) in.read());
    }

    public void write(OutputStream out) throws IOException {
        out.write(code);
    }
}
