
package org.jayson.render;

import java.lang.reflect.Type;
import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonOutput;

/**
 *
 * @author tofarrell
 */
public class StringRender  implements JaysonRender<String> {

    public static final StringRender INSTANCE = new StringRender();
    
    private StringRender(){
    }
    
    @Override
    public void render(String value, Jayson coder, JaysonOutput out) throws JaysonException {
        out.str(value);
    }

    public static class StringRenderFactory extends JaysonRenderFactory{

        public StringRenderFactory(int priority){
            super(priority);
        }

        @Override
        public JaysonRender getRenderFor(Type type) throws JaysonException {
            return (type == String.class) ? INSTANCE : null;
        }   
    }    
}
