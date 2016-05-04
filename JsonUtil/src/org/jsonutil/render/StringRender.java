
package org.jsonutil.render;

import java.lang.reflect.Type;
import org.jsonutil.JsonCoder;
import org.jsonutil.JsonException;
import org.jsonutil.JsonOutput;

/**
 *
 * @author tofarrell
 */
public class StringRender  implements JsonRender<String> {

    public static final StringRender INSTANCE = new StringRender();
    
    private StringRender(){
    }
    
    @Override
    public void render(String value, JsonCoder coder, JsonOutput out) throws JsonException {
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
