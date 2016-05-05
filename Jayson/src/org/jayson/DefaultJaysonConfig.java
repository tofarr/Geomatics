package org.jayson;

import org.jayson.parser.ArrayParser;
import org.jayson.parser.BooleanParser;
import org.jayson.parser.CollectionParser;
import org.jayson.parser.ConstructorParser;
import org.jayson.parser.NumberParser;
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
public class DefaultJsonConfig extends JsonConfig {

    public DefaultJsonConfig() {
        super(EARLY);
    }

    @Override
    public void configure(JaysonBuilder builder) {
        PolymorphicMap polymorphicMap = PolymorphicMap.getInstance();
        
        //Default parsers
        builder.addParserFactory(new ArrayParser.ArrayParserFactory());
        builder.addParserFactory(new BooleanParser.BooleanParserFactory());
        builder.addParserFactory(new CollectionParser.CollectionParserFactory());
        builder.addParserFactory(new ConstructorParser.ConstructorParserFactory());
        builder.addParserFactory(new NumberParser.NumberParserFactory());
        builder.addParserFactory(new StaticMethodParser.StaticMethodParserFactory());
        builder.addParserFactory(new StringParser.StringParserFactory());
        builder.addParserFactory(new PolymorphicParser.PolymorphicParserFactory(polymorphicMap));
        
        //Default renders
        builder.addRenderFactory(new ArrayRender.ArrayRenderFactory());
        builder.addRenderFactory(new BooleanRender.BooleanRenderFactory());
        builder.addRenderFactory(new CollectionRender.CollectionRenderFactory());
        builder.addRenderFactory(new DefaultRender.DefaultRenderFactory());
        builder.addRenderFactory(new NumberRender.NumberRenderFactory());
        builder.addRenderFactory(new StringRender.StringRenderFactory());
        builder.addRenderFactory(new PolymorphicRender.PolymorphicRenderFactory(polymorphicMap));
    }
}
