package org.jayson;

import org.jayson.parser.ArrayParser;
import org.jayson.parser.BooleanParser;
import org.jayson.parser.CollectionParser;
import org.jayson.parser.ConstructorParser;
import org.jayson.parser.NumberParser;
import org.jayson.parser.ObjectParser;
import org.jayson.parser.SimpleMapParser;
import org.jayson.parser.StaticMethodParser;
import org.jayson.parser.StringParser;
import org.jayson.poly.PolymorphicMap;
import org.jayson.poly.PolymorphicParser;
import org.jayson.poly.PolymorphicRender;
import org.jayson.render.ArrayRender;
import org.jayson.render.BooleanRender;
import org.jayson.render.CollectionRender;
import org.jayson.render.DefaultRender;
import org.jayson.render.NumberRender;
import org.jayson.render.StringRender;

/**
 *
 * @author tofarrell
 */
public class DefaultJaysonConfig extends JaysonConfig {

    public DefaultJaysonConfig() {
        super(MED);
    }

    @Override
    public void configure(JaysonBuilder builder) {
        PolymorphicMap polymorphicMap = PolymorphicMap.getInstance();
        
        //Default parsers
        builder.addParserFactory(new PolymorphicParser.PolymorphicParserFactory(10000, polymorphicMap));
        builder.addParserFactory(new ArrayParser.ArrayParserFactory(20000));
        builder.addParserFactory(new BooleanParser.BooleanParserFactory(30000));
        builder.addParserFactory(new NumberParser.NumberParserFactory(40000));
        builder.addParserFactory(new StringParser.StringParserFactory(50000));
        builder.addParserFactory(new CollectionParser.CollectionParserFactory(60000, polymorphicMap));
        builder.addParserFactory(new SimpleMapParser.MapParserFactory(70000, polymorphicMap));
        builder.addParserFactory(new ConstructorParser.ConstructorParserFactory(80000));
        builder.addParserFactory(new StaticMethodParser.StaticMethodParserFactory(90000));
        builder.addParserFactory(new ObjectParser.ObjectParserFactory(100000, polymorphicMap));
        
        //Default renders
        builder.addRenderFactory(new PolymorphicRender.PolymorphicRenderFactory(10000, polymorphicMap));
        builder.addRenderFactory(new ArrayRender.ArrayRenderFactory(20000));
        builder.addRenderFactory(new BooleanRender.BooleanRenderFactory(30000));
        builder.addRenderFactory(new NumberRender.NumberRenderFactory(40000));
        builder.addRenderFactory(new StringRender.StringRenderFactory(50000));
        builder.addRenderFactory(new CollectionRender.CollectionRenderFactory(60000));
        builder.addRenderFactory(new DefaultRender.DefaultRenderFactory(70000));
        
    }
}
