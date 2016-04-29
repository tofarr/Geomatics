package org.jg.gfx.renderable;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.beans.ConstructorProperties;
import org.jg.geom.Rect;
import org.jg.geom.Vect;
import org.jg.gfx.img.ImgSpec;
import org.jg.util.Transform;

/**
 *
 * @author tofarrell
 */
public class FloatingImg implements Renderable {

    private final ImgSpec img;
    private final Vect location;
    private final Vect offsetPx;

    @ConstructorProperties({"img", "location", "offsetPx"})
    public FloatingImg(ImgSpec img, Vect location, Vect offsetPx) {
        this.img = img;
        this.location = location;
        this.offsetPx = offsetPx;
    }

    public ImgSpec getImg() {
        return img;
    }

    public Vect getLocation() {
        return location;
    }

    public Vect getOffsetPx() {
        return offsetPx;
    }

    @Override
    public boolean boundsVariable() {
        return true;
    }

    @Override
    public Rect toBounds(double resolution) {
        BufferedImage _img = img.toImg()
        return bounds; //How does this work? Width is floating!
    }

    @Override
    public void render(Graphics2D g, Transform transform) {
        BufferedImage _img = img.toImg();
        Rect _bounds = bounds.transform(transform);
        g.drawImage(_img, (int) Math.round(_bounds.minX), (int) Math.round(_bounds.minY), (int) Math.round(_bounds.maxX), (int) Math.round(_bounds.maxY),
                0, 0, _img.getWidth(), _img.getHeight(), null);
    }

}

