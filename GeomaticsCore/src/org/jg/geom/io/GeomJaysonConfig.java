package org.jg.geom.io;

import org.jayson.JaysonBuilder;
import org.jayson.JaysonConfig;
import org.jayson.parser.InstanceParserFactory;
import org.jayson.render.InstanceRenderFactory;
import org.jg.geom.DefaultGeomFactory;
import org.jg.geom.Geom;
import org.jg.util.Tolerance;

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
    }

}
