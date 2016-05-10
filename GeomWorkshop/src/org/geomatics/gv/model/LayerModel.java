package org.geomatics.gv.model;

import java.beans.ConstructorProperties;
import org.geomatics.geom.Geom;
import org.geomatics.geom.LineString;
import org.geomatics.geom.Linearizer;
import org.geomatics.geom.Network;
import org.geomatics.geom.Rect;
import org.geomatics.geom.Relation;
import org.geomatics.geom.Vect;
import org.geomatics.gfx.fill.ColorFill;
import org.geomatics.gfx.fill.Fill;
import org.geomatics.gfx.outline.BasicOutline;
import org.geomatics.gfx.outline.Outline;
import org.geomatics.gfx.renderable.FloatingRenderable;
import org.geomatics.gfx.renderable.Renderable;
import org.geomatics.gfx.renderable.RenderableFill;
import org.geomatics.gfx.renderable.RenderableOutline;
import org.geomatics.gfx.source.RenderableObjectSource;
import org.geomatics.util.Tolerance;
import org.geomatics.util.TransformBuilder;
import org.geomatics.util.View;
import org.geomatics.util.ViewPoint;

/**
 *
 * @author tofarrell
 */
public class LayerModel implements RenderableObjectSource<Geom> {

    public static LayerModel DEFAULT;

    static {
        RenderableOutline symbol = new RenderableOutline(0,
                LineString.valueOf(Tolerance.DEFAULT, -5, -5, 5, -5, 5, 5, -5, 5, -5, -5),
                new ColorFill(0xFFFF0000),
                new BasicOutline(1));
        DEFAULT = new LayerModel("Default Layer",
                Rect.valueOf(0, 0, 100, 100).toArea(),
                null,
                new ColorFill(0xFF000000),
                new BasicOutline(1),
                symbol);
    }
    public final String title;
    public final Geom geom;
    public final Fill fill;
    public final Fill outlineFill;
    public final Outline outline;
    public final Renderable symbol;

    @ConstructorProperties({"title", "geom", "fill", "outlineFill", "outline", "symbol"})
    public LayerModel(String title, Geom geom, Fill fill, Fill outlineFill, Outline outline, Renderable symbol) {
        if (fill == null && outlineFill == null && outline == null && symbol == null) {
            throw new NullPointerException("No style specified!");
        }
        this.title = title;
        this.geom = geom;
        this.fill = fill;
        this.outlineFill = outlineFill;
        this.outline = outline;
        this.symbol = symbol;
    }

    @Override
    public boolean load(View view, RenderableObjectProcessor processor) {
        if (geom == null) {
            return true; // no geometry, so nothing to draw
        }
        Rect bounds = view.getBounds().buffer(getPadding(view.getResolution()));
        return load(bounds, processor);
    }

    @Override
    public boolean load(ViewPoint viewPoint, RenderableObjectProcessor processor) {
        if (geom == null) {
            return true; // no geometry, so nothing to draw
        }
        Rect bounds = viewPoint.getCenter().getBounds().buffer(getPadding(viewPoint.getResolution()));
        return load(bounds, processor);
    }

    private boolean load(Rect bounds, final RenderableObjectProcessor processor) {

        if (!Relation.isDisjoint(bounds.relate(geom.getBounds(), Tolerance.DEFAULT))) {
            if (fill != null) {
                if (!processor.process(new RenderableFill(0, geom, fill))) {
                    return false;
                }
            }
            if (outline != null) {
                if (!processor.process(new RenderableOutline(0, geom, outlineFill, outline))) {
                    return false;
                }
            }
            if (symbol != null) {
                Network network = new Network();
                geom.addTo(network, Linearizer.DEFAULT, Tolerance.DEFAULT);
                network.forEachVertex(new Network.VertexProcessor() {
                    TransformBuilder builder = new TransformBuilder();

                    @Override
                    public boolean process(double x, double y, int numLinks) {
                        FloatingRenderable renderable = new FloatingRenderable(0, symbol, Vect.valueOf(x, y));
                        return processor.process(renderable);
                    }
                });
            }
        }
        return true;
    }

    private double getPadding(double resolution) {
        double padding = 0;
        if (outline != null) {
            padding = outline.getPadding() * resolution;
        }
        if (symbol != null) {
            Rect bounds = symbol.toBounds(resolution);
            padding = Math.max(padding, Math.max(bounds.getWidth(), bounds.getHeight()));
        }
        return padding;
    }

    @Override
    public Geom getAttributes(long renderableId) {
        return (renderableId == 0) ? geom : null;
    }

}
