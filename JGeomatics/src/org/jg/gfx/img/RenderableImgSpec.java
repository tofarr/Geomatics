package org.jg.gfx.img;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.beans.ConstructorProperties;
import org.jg.geom.GeomException;
import org.jg.geom.Rect;
import org.jg.gfx.renderable.Renderable;
import org.jg.util.Transform;

/**
 *
 * @author tofarrell
 */
public class RenderableImgSpec implements ImgSpec {

    private final Renderable renderable;
    private final Rect bounds;
    private transient BufferedImage img;

    @ConstructorProperties({"renderable", "bounds"})
    public RenderableImgSpec(Renderable renderable, Rect bounds) {
        this.renderable = renderable;
        this.bounds = bounds;
    }

    public Renderable getRenderable() {
        return renderable;
    }

    public Rect getBounds() {
        return bounds;
    }

    @Override
    public int imgWidth() throws GeomException {
        return (int)Math.ceil(bounds.getWidth());
    }

    @Override
    public int imgHeight() throws GeomException {
        return (int)Math.ceil(bounds.getHeight());
    }

    @Override
    public BufferedImage toImg() throws GeomException {
        BufferedImage ret = img;
        if(ret == null){
            ret = new BufferedImage(imgWidth(), imgHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D)ret.createGraphics();
            try{
                renderable.render(g, Transform.IDENTITY);
            }finally{
                g.dispose();
            }
            img = ret;
        }
        return ret;
        
    }

}
