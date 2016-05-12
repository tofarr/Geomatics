package org.om.jayson;

import org.jayson.Jayson;
import org.jayson.JaysonBuilder;
import org.jayson.JaysonConfig;
import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonOutput;
import org.jayson.JaysonType;
import org.jayson.parser.InstanceParserFactory;
import org.jayson.parser.JaysonParser;
import org.jayson.render.InstanceRenderFactory;
import org.jayson.render.JaysonRender;
import org.om.element.Element;

/**
 *
 * @author tofarrell
 */
public class OMJaysonConfig extends JaysonConfig {

    public OMJaysonConfig() {
        super(MED);
    }

    @Override
    public void configure(JaysonBuilder builder) {
        builder.addParserFactory(new InstanceParserFactory(Element.class, new JaysonParser<Element>() {
            @Override
            public Element parse(JaysonType type, Jayson coder, JaysonInput input) {
                return Element.readJson(type, input);
            }
            
        }, EARLY));
        builder.addRenderFactory(new InstanceRenderFactory(Element.class, new JaysonRender<Element>(){
            @Override
            public void render(Element value, Jayson coder, JaysonOutput out) throws JaysonException {
                if(value == null){
                    out.nul();
                }else{
                    value.toJson(out);
                }
            }
        
        }, EARLY));
    }

}
