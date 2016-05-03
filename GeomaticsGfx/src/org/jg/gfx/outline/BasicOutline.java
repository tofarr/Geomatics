package org.jg.gfx.outline;

import java.awt.BasicStroke;
import java.beans.ConstructorProperties;
import java.beans.Transient;

/**
 *
 * @author tofarrell
 */
public class BasicOutline implements Outline {

    private final float width;
    private transient BasicStroke stroke;

    @ConstructorProperties({"width"})
    public BasicOutline(float width) {
        this.width = width;
    }

    public float getWidth() {
        return width;
    }

    @Override
    @Transient
    public double getPadding() {
        return width;
    }

    
    @Override
    public BasicStroke toStroke() {
        BasicStroke ret = stroke;
        if(ret == null){
            ret = new BasicStroke(width);
            stroke = ret;
        }
        return ret;
    }
}
