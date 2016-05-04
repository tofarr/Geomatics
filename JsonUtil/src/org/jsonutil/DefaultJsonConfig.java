package org.jsonutil;

import org.jsonutil.parser.ArrayParser;
import org.jsonutil.parser.BooleanParser;
import org.jsonutil.parser.CollectionParser;
import org.jsonutil.parser.ConstructorParser;
import org.jsonutil.parser.NumberParser;
import org.jsonutil.parser.StaticMethodParser;
import org.jsonutil.parser.StringParser;
import org.jsonutil.poly.PolymorphicMap;
import org.jsonutil.poly.PolymorphicParser;
import org.jsonutil.poly.PolymorphicRender;
import org.jsonutil.render.ArrayRender;
import org.jsonutil.render.BooleanRender;
import org.jsonutil.render.CollectionRender;
import org.jsonutil.render.DefaultRender;
import org.jsonutil.render.NumberRender;
import org.jsonutil.render.StringRender;

/**
 *
 * @author tofarrell
 */
public class DefaultJsonConfig extends JsonConfig {

    public DefaultJsonConfig() {
        super(EARLY);
    }

    @Override
    public void configure(JsonCoderBuilder builder) {
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
