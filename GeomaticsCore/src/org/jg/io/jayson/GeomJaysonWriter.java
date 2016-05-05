
package org.jg.io.jayson;

import org.jayson.JaysonException;
import org.jayson.JaysonWriter;

/**
 * Json writer which adds additional spaces making strings of 2d numbers easier to read
 * @author tofarrell
 */
public class GeomJaysonWriter extends JaysonWriter{
    
    private int space;
    
    public GeomJaysonWriter(Appendable appendable) throws NullPointerException {
        super(appendable);
    }

    @Override
    protected void writeNull() throws JaysonException {
        space = 0;
        super.writeNull();
    }

    @Override
    protected void writeBool(boolean bool) throws JaysonException {
        space = 0;
        super.writeBool(bool);
    }

    @Override
    protected void writeNum(double num) throws JaysonException {
        if(++space >= 2){
            append(' ');
            space = 0;
        }
        super.writeNum(num);
    }

    @Override
    protected void writeStr(String str) throws JaysonException {
        space = 0;
        super.writeStr(str);
    }

    @Override
    protected void writeName(String name) throws JaysonException {
        space = 0;
        super.writeName(name);
    }

    @Override
    protected void writeEndArray() throws JaysonException {
        space = 0;
        super.writeEndArray();
    }

    @Override
    protected void writeBeginArray() throws JaysonException {
        space = 0;
        super.writeBeginArray();
    }

    @Override
    protected void writeEndObject() throws JaysonException {
        space = 0;
        super.writeEndObject();
    }

    @Override
    protected void writeBeginObject() throws JaysonException {
        space = 0;
        super.writeBeginObject();
    }


    
    
}
