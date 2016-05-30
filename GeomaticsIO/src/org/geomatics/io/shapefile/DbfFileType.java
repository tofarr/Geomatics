/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geomatics.io.shapefile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author tofarrell
 */
public enum DbfFileType {

    FOXBASE(0x02),
    FOXBASE_PLUS(0x03), //FoxBASE+/Dbase III plus, no memo
    VISUAL_FOX_PRO(0x30),
    VISUAL_FOX_PRO_AUTO_INCREMENT(0x31), // Visual FoxPro, autoincrement enabled
    VISUAL_FOX_PRO_VAR(0x32), // Visual FoxPro with field type Varchar or Varbinary
    DBASE_4_SQL_TABLE(0x43), // dBASE IV SQL table files, no memo
    DBASE4_SQL_SYSTEM(0x63), // dBASE IV SQL system files, no memo
    DBASE4(0x83), // FoxBASE+/dBASE III PLUS, with memo
    DBASE4_WITH_MEMO(0x8B), // dBASE IV with memo
    DBASE4_SQL_TABLE_WITH_MEMO(0xCB), // dBASE IV SQL table files, with memo
    FOX_PRO_2(0xF5), // FoxPro 2.x (or earlier) with memo
    HI_PER_SIX(0xE5), // HiPer-Six format with SMT memo file
    FOX_BASE(0xFB) // FoxBASE;
    ;

    public final int code;

    DbfFileType(int code) {
        this.code = code;
    }

    public static DbfFileType fromCode(int code) throws IllegalArgumentException {
        for (DbfFileType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code : " + code);
    }

    public static DbfFileType read(InputStream in) throws IOException {
        return fromCode(in.read());
    }

    public void write(OutputStream out) throws IOException {
        out.write(code);
    }
}
