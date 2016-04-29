package org.jg.gfx.fill;

import java.awt.Color;
import java.awt.Paint;
import java.beans.ConstructorProperties;

/**
 *
 * @author tofarrell
 */
public class ColorFill implements Fill {

    private final long color;
    private transient Color paint;

    @ConstructorProperties({"color"})
    public ColorFill(long color) {
        this.color = color;
    }

    public long getColor() {
        return color;
    }

    @Override
    public Color toPaint() {
        Color ret = paint;
        if(ret == null){
            ret = new Color((int)((color & 0xFF0000) >> 16),
                    (int)((color & 0xFF00) >> 8),
                    (int)((color & 0xFF)),
                    (int)((color & 0xFF000000L) >> 24));
            paint = ret;
        }
        return ret;
    }
}
