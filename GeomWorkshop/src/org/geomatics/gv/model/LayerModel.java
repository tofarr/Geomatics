package org.geomatics.gv.model;

import org.geomatics.geom.Geom;
import org.geomatics.gfx.fill.Fill;
import org.geomatics.gfx.outline.Outline;
import org.geomatics.gfx.renderable.Renderable;

/**
 *
 * @author tofarrell
 */
public class LayerModel {

    public final String title;
    public final Geom geom;
    public final Fill fill;
    public final Fill outlineFill;
    public final Outline outline;
    public final Renderable symbol;

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

}
