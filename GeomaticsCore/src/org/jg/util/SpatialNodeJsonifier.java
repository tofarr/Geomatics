package org.jg.util;

import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonOutput;
import org.jayson.JaysonType;
import org.jayson.parser.JaysonParser;
import org.jayson.render.JaysonRender;
import org.jg.geom.Rect;

/**
 *
 * @author tofar
 */
public class SpatialNodeJsonifier extends JaysonParser<SpatialNode> implements JaysonRender<SpatialNode> {

    @Override
    public void render(SpatialNode node, Jayson coder, JaysonOutput out) throws JaysonException {
        out.beginObject();
        if(node.isBranch()){
            out.name("a");
            render(node.getA(), coder, out);
            out.name("b");
            render(node.getB(), coder, out);
        }else{
            out.name("itemBounds").beginArray();
            for(int i = 0; i < node.size(); i++){
                coder.render(node.getItemBounds(i), out);
            }
            out.endArray();
        }
        out.endObject();
    }

    @Override
    public SpatialNode parse(JaysonType type, Jayson coder, JaysonInput input) throws JaysonException {
        if(type != JaysonType.BEGIN_OBJECT){
            throw new JaysonException("Expected BEGIN_OBJECT found "+type);
        }
        SpatialNode a = null;
        SpatialNode b = null;
        Rect[] itemBounds = null;
        Object[] values = null;
        while(true){
            type = input.next();
            switch(type){
                case NAME:
                    String name = input.str();
                    switch(name){
                        case "a":
                            a = parse(coder, input);
                            break;
                        case "b":
                            b = parse(coder, input);
                            break;
                        case "itemBounds":
                            itemBounds = coder.parse(Rect[].class, input);
                            break;
                        case "values":
                            
                    } NEED NODE TYPE HERE!
                case END_OBJECT:
                    
            }
        }
        
    }

}