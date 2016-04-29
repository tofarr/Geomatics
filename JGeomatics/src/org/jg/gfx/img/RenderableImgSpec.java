package org.jg.gfx.img;

import java.awt.image.BufferedImage;
import java.beans.ConstructorProperties;
import org.jg.geom.GeomException;
import org.jg.gfx.renderable.Renderable;

/**
 *
 * @author tofarrell
 */
public class RenderableImgSpec implements ImgSpec {

    private final Renderable renderable;
    private final double padding;

    @ConstructorProperties({"renderable"})
    public RenderableImgSpec(Renderable renderable) {
        this.renderable = renderable;
    }

    public Renderable getRenderable() {
        return renderable;
    }

    @Override
    public BufferedImage toImg() throws GeomException {
        renderable.toBounds().;
        
        BufferedImage
    }

}
