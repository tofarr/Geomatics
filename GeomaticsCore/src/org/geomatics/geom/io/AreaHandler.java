package org.geomatics.geom.io;

import java.util.ArrayList;
import java.util.List;
import org.jayson.JaysonException;
import org.jayson.JaysonInput;
import org.jayson.JaysonOutput;
import org.jayson.JaysonType;
import org.geomatics.geom.Area;
import org.geomatics.geom.GeomFactory;
import org.geomatics.geom.Ring;

/**
 *
 * @author tofarrell
 */
public class AreaHandler extends GeomHandler<Area> {

    private final RingHandler ringHandler = new RingHandler();
    
    public AreaHandler() {
        super(Area.CODE, Area.class);
    }

    @Override
    public Area parseRemaining(GeomFactory factory, JaysonInput input) throws JaysonException {
        JaysonType type = input.next();
        Ring shell = null;
        if (type == JaysonType.BEGIN_ARRAY) { // shell
            shell = ringHandler.parseRemaining(factory, input);
        } else if (type != JaysonType.NULL) {
            throw new JaysonException("Unexpected type : " + type);
        }

        List<Area> children = new ArrayList<>();
        while (true) {
            type = input.next();
            if (type == JaysonType.END_ARRAY) {
                Area ret = factory.area(shell, children);
                return ret;
            } else if (type != JaysonType.BEGIN_ARRAY) {
                throw new JaysonException("BEGIN_ARRAY expected");
            }
            Area child = parseRemaining(factory, input);
            children.add(child);
        }
    }

    @Override
    public void renderRemaining(Area area, JaysonOutput out) throws JaysonException {
        Ring shell = area.getShell();
        if(shell == null){
            out.nul();
        }else{
            out.beginArray();
            ringHandler.renderRemaining(shell, out);
        }
        for(int i = 0; i < area.numChildren(); i++){
            out.beginArray();
            renderRemaining(area.getChild(i), out);
        }
        out.endArray();
    }
}
