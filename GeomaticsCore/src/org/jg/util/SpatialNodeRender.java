package org.jg.util;

import org.jayson.Jayson;
import org.jayson.JaysonException;
import org.jayson.JaysonOutput;
import org.jayson.render.JaysonRender;

/**
 *
 * @author tofar
 */
public class SpatialNodeRender implements JaysonRender<SpatialNode> {

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

}
