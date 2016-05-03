package org.jg.gfx.renderable;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.beans.ConstructorProperties;
import org.jg.geom.Rect;
import org.jg.gfx.img.ImgSpec;
import org.jg.util.Transform;

/**
 *
 * @author tofarrell
 */
public class FixedImg implements Renderable {

    private final long id;
    private final ImgSpec img;
    private final Rect bounds;

    @ConstructorProperties({"id", "img", "bounds"})
    public FixedImg(long id, ImgSpec img, Rect bounds) {
        this.id = id;
        this.img = img;
        this.bounds = bounds;
    }

    @Override
    public long getId() {
        return id;
    }
    
    public ImgSpec getImg() {
        return img;
    }

    public Rect getBounds() {
        return bounds;
    }

    @Override
    public boolean boundsVariable() {
        return false;
    }
    

    @Override
    public Rect toBounds(double resolution) {
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
