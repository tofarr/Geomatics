package org.geomatics.gv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
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
import org.geomatics.util.Tolerance;
import org.geomatics.util.TransformBuilder;
import org.geomatics.util.View;
import org.jayson.Jayson;

/**
 *
 * @author tofarrell
 */
public class GeomLayer implements RenderableObjectSource {

    private final String title;
    /**
     * layer may or may not be backed by a file
     */
    private final String file;
    /**
     * explicitly specified geometry - in cases geometry
     */
    private final Geom geom;
    /**
     * fill for geometry
     */
    private final Fill fill;
    /**
     * outline filler
     */
    private final Fill outlineFill;
    /**
     * stroke for geometry
     */
    private final Outline outline;
    /**
     * symbol for geometry
     */
    private final Renderable symbol;
    
    private Geom cachedGeom;
    

    public GeomLayer(String title, String file, Geom geom, Fill fill, Fill outlineFill, Outline outline, Renderable symbol) {
        if((file == null) == (geom == null)){
            throw new IllegalArgumentException("Must specify either file or geom, but not both!");
        }
        this.title = title;
        this.file = file;
        this.geom = geom;
        this.fill = fill;
        this.outlineFill = outlineFill;
        this.outline = outline;
        this.symbol = symbol;
    }

    public File fileObj() {
        File ret = (file == null) ? null : new File(file);
        return ret;
    }

    public Path path() {
        Path ret = (file == null) ? null : new File(file).toPath();
        return ret;
    }
    
    public Geom cachedGeom() throws IOException {
        Geom ret = cachedGeom;
        if(ret != null){
            return ret;
        }
        ret = geom;
        if(ret != null){
            cachedGeom = ret;
            return ret;
        }
        try(Reader reader = new BufferedReader(new FileReader(file))){
            Jayson jayson = Jayson.getInstance();
            ret = jayson.parse(Geom.class, reader);
            cachedGeom = ret;
            return ret;
        }
    }

    public String getTitle() {
        return title;
    }

    public String getFile() {
        return file;
    }

    public Geom getGeom() {
        return geom;
    }

    public Fill getFill() {
        return fill;
    }

    public Fill getOutlineFill() {
        return outlineFill;
    }

    public Outline getOutline() {
        return outline;
    }

    public Renderable getSymbol() {
        return symbol;
    }

    @Override
    public boolean load(View view, final RenderableObjectProcessor processor) {
        if (geom == null) {
            return true; // no geometry, so nothing to draw
        }
        Rect bounds = getPaddedBounds(view);
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

    private Rect getPaddedBounds(View view) {
        double resolution = view.getResolution();
        double padding = 0;
        if (outline != null) {
            padding = outline.getPadding() * resolution;
        }
        if (symbol != null) {
            Rect bounds = symbol.toBounds(resolution);
            padding = Math.max(padding, Math.max(bounds.getWidth(), bounds.getHeight()));
        }
        return view.getBounds().buffer(padding);
    }
}
