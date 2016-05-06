package org.geomatics.gv.modelold;

import org.geomatics.geom.Geom;
import org.geomatics.geom.Linearizer;
import org.geomatics.geom.Network;
import org.geomatics.geom.Network.VertexProcessor;
import org.geomatics.geom.Rect;
import org.geomatics.geom.Relation;
import org.geomatics.geom.Vect;
import org.geomatics.gfx.fill.Fill;
import org.geomatics.gfx.outline.Outline;
import org.geomatics.gfx.renderable.FloatingRenderable;
import org.geomatics.gfx.renderable.Renderable;
import org.geomatics.gfx.renderable.RenderableFill;
import org.geomatics.gfx.renderable.RenderableOutline;
import org.geomatics.gfx.source.RenderableObjectSource;
import org.geomatics.gfx.source.RenderableObjectSource.RenderableObjectProcessor;
import org.geomatics.util.Tolerance;
import org.geomatics.util.TransformBuilder;
import org.geomatics.util.View;
import org.geomatics.util.ViewPoint;

/**
 *
 * @author tofarrell
 */
public class GeomLayerz implements RenderableObjectSource<Geom> {

    private final Geom geom;
    private final Fill fill;
    private final Fill outlineFill;
    private final Outline outline;
    private final Renderable symbol;

    public GeomLayerz(Geom geom, Fill fill, Fill outlineFill, Outline outline, Renderable symbol) {
        if ((fill == null) && (outlineFill == null) && (outline == null) && (symbol == null)) {
            throw new NullPointerException("Must define at least one visual element!");
        }
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
                network.forEachVertex(new VertexProcessor() {
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
