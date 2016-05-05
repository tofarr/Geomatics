
package org.jayson.render;

import java.lang.reflect.Type;
import org.jayson.Jayson;
import org.jayson.JsonException;
import org.jayson.JsonOutput;

/**
 *
 * @author tofarrell
 */
public class StringRender  implements JsonRender<String> {

    public static final StringRender INSTANCE = new StringRender();
    
    private StringRender(){
    }
    
    @Override
    public void render(String value, Jayson coder, JsonOutput out) throws JsonException {
        out.str(value);
    }

    public static class StringRenderFactory extends JsonRenderFactory{

        public StringRenderFactory() {
            super(LATE);
        }

        @Override
        public JsonRender getRenderFor(Type type) throws JsonException {
            return (type == String.class) ? INSTANCE : null;
        }   
    }    
}
