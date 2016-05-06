package org.geomatics.gfx.font;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.beans.ConstructorProperties;
import java.util.HashMap;

/**
 *
 * @author tofarrell
 */
public class BasicFontSpec implements FontSpec {

    private final String family;
    private final float size;
    private transient Font font;

    @ConstructorProperties({"family", "size"})
    public BasicFontSpec(String family, float size) {
        this.family = family;
        this.size = size;
    }

    public String getFamily() {
        return family;
    }

    public float getSize() {
        return size;
    }

    @Override
    public Font toFont() {
        Font ret = font;
        if (ret == null) {
            HashMap<TextAttribute, Object> attribs = new HashMap<>();
            String familyName = family;
            if (familyName != null) {
                if (familyName.contains("'")) {
                    familyName = familyName.split("'")[1];
                }
            }
            attribs.put(TextAttribute.FAMILY, familyName);
            attribs.put(TextAttribute.SIZE, size);
            ret = Font.getFont(attribs);
            font = ret;
        }
        return ret;
    }

}
