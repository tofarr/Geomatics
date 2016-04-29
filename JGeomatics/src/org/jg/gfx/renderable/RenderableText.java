package org.jg.gfx.renderable;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.beans.ConstructorProperties;
import org.jg.geom.Rect;
import org.jg.geom.Vect;
import org.jg.gfx.fill.Fill;
import org.jg.gfx.font.FontSpec;
import org.jg.util.Transform;

/**
 *
 * @author tofarrell
 */
public class RenderableText implements Renderable {

    private final String text;
    private final Vect location;
    private final Fill fill;
    private final FontSpec font;

    @ConstructorProperties({"text", "location", "fill", "font"})
    public RenderableText(String text, Vect location, Fill fill, FontSpec font) {
        this.text = text;
        this.location = location;
        this.fill = fill;
        this.font = font;
    }

    public String getText() {
        return text;
    }

    public Vect getLocation() {
        return location;
    }

    public Fill getFill() {
        return fill;
    }

    public FontSpec getFont() {
        return font;
    }

    @Override
    public boolean boundsVariable() {
        return true;
    }

    @Override
    public Rect toBounds(double resolution) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        Font _font = font.toFont();
        Rectangle2D rect = _font.getStringBounds(text, frc);
        Rect ret = Rect.valueOf(location.x + (rect.getMinX() * resolution),
                location.y + (rect.getMinY() * resolution),
                location.x + (rect.getMaxX() * resolution),
                location.y + (rect.getMaxY() * resolution));
        return ret;
    }

    @Override
    public void render(Graphics2D g, Transform transform) {
        g.setPaint(fill.toPaint());
        g.setFont(font.toFont());
        Vect transformed = transform.transform(location);
        g.drawString(text, (float) transformed.x, (float) transformed.y);
    }
}