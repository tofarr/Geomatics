package org.jg.geom.io;

import java.util.ArrayList;
import java.util.List;
import org.jg.geom.Vect;
import org.jg.util.Network;
import org.jg.util.VectList;

/**
 *
 * @author tofar
 */
public class WKT {
    //String wkt = "MULTILINESTRING"+network.toString().replace(",", " ").replace("  ", ", ").replace("[", "(").replace("]",")").replace(") (", "),(");
    //System.out.println(wkt);

    public static String toMultiLineString(Network network) {
        StringBuilder str = new StringBuilder("MULTILINESTRING(");
        List<VectList> lineStrings = new ArrayList<>();
        network.extractLines(lineStrings, false);
        for (int i = 0; i < lineStrings.size(); i++) {
            VectList lineString = lineStrings.get(i);
            if (i != 0) {
                str.append(',');
            }
            str.append('(');
            for (int j = 0; j < lineString.size(); j++) {
                if (j != 0) {
                    str.append(", ");
                }
                str.append(Vect.ordToStr(lineString.getX(j))).append(' ').append(Vect.ordToStr(lineString.getY(j)));
            }
            str.append(')');
        }
        str.append(')');
        return str.toString();
    }
}
