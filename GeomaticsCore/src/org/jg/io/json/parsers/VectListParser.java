/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jg.io.json.parsers;

import org.jg.geom.GeomIOException;
import org.jayson.JsonReader;
import org.jayson.JsonType;
import org.jg.util.VectList;

/**
 *
 * @author tofarrell
 */
public final class VectListParser {

    public static final VectListParser INSTANCE = new VectListParser();

    public VectList parse(JsonReader reader) throws GeomIOException {
        VectList ret = new VectList();
        while (reader.next() != JsonType.END_ARRAY) {
            double x = reader.num();
            double y = reader.nextNum();
            ret.add(x, y);
        }
        return ret;
    }
}
