package org.geomatics.geom.io;

import org.jayson.JaysonBuilder;
import org.jayson.JaysonConfig;
import org.jayson.parser.InstanceParserFactory;
import org.jayson.render.InstanceRenderFactory;
import org.geomatics.geom.DefaultGeomFactory;
import org.geomatics.geom.Geom;
import org.geomatics.util.SpatialNodeJaysonParser.SpatialNodeParserFactory;
import org.geomatics.util.SpatialNodeJaysonRender.SpatialNodeRenderFactory;
import org.geomatics.util.Tolerance;

/**
 *
 * @author tofar
 */
public class GeomJaysonConfig extends JaysonConfig {

    public GeomJaysonConfig() {
        super(MED);
    }

    @Override
    public void configure(JaysonBuilder builder) {
        GeomJaysonifier jsonifier = new GeomJaysonifier(new DefaultGeomFactory(Tolerance.DEFAULT),
                new AreaHandler(),
                new GeoShapeHandler(),
                new LineHandler(),
                new LineSetHandler(),
                new LineStringHandler(),
                new PathHandler(),
                new PointSetHandler(),
                new RectHandler(),
                new RingHandler(),
                new VectHandler());
        builder.addParserFactory(new InstanceParserFactory(Geom.class, jsonifier, 10000));
        builder.addRenderFactory(new InstanceRenderFactory(Geom.class, jsonifier, 10000));
        builder.addParserFactory(new SpatialNodeParserFactory(10000));
        builder.addRenderFactory(new SpatialNodeRenderFactory(10000));
    }

}
