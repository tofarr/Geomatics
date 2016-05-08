package org.geomatics.gfx.io;

import org.geomatics.gfx.fill.ColorFill;
import org.geomatics.gfx.fill.Fill;
import org.geomatics.gfx.font.BasicFontSpec;
import org.geomatics.gfx.font.FontSpec;
import org.geomatics.gfx.img.ImgSpec;
import org.geomatics.gfx.img.RenderableImgSpec;
import org.geomatics.gfx.img.UrlImgSpec;
import org.geomatics.gfx.outline.BasicOutline;
import org.geomatics.gfx.outline.Outline;
import org.geomatics.gfx.renderable.FixedImg;
import org.geomatics.gfx.renderable.FloatingImg;
import org.geomatics.gfx.renderable.FloatingRenderable;
import org.geomatics.gfx.renderable.Renderable;
import org.geomatics.gfx.renderable.RenderableFill;
import org.geomatics.gfx.renderable.RenderableOutline;
import org.geomatics.gfx.renderable.RenderableText;
import org.geomatics.gfx.renderable.TransformedRenderable;
import org.geomatics.gfx.source.CompoundRenderableObjectSource;
import org.geomatics.gfx.source.RenderableObjectSource;
import org.geomatics.gfx.source.SimpleRenderableObjectSource;
import org.jayson.poly.PolymorphicConfig;
import org.jayson.poly.PolymorphicMapBuilder;

/**
 *
 * @author tofar
 */
public class GfxPolymorphicConfig extends PolymorphicConfig {

    public GfxPolymorphicConfig() {
        super(MED);
    }

    @Override
    public void configure(PolymorphicMapBuilder builder) {
        builder.getClassMap(Fill.class).add(ColorFill.class);
        builder.getClassMap(FontSpec.class).add(BasicFontSpec.class);
        builder.getClassMap(ImgSpec.class).add(RenderableImgSpec.class).add(UrlImgSpec.class);
        builder.getClassMap(Outline.class).add(BasicOutline.class);
        builder.getClassMap(Renderable.class)
                .add(FixedImg.class)
                .add(FloatingImg.class)
                .add(FloatingRenderable.class)
                .add(RenderableFill.class)
                .add(RenderableOutline.class)
                .add(RenderableText.class)
                .add(TransformedRenderable.class);
        builder.getClassMap(RenderableObjectSource.class)
                .add(SimpleRenderableObjectSource.class)
                .add(CompoundRenderableObjectSource.class);
    }

}
