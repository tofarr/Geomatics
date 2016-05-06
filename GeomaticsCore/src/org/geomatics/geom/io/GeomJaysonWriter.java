package org.geomatics.geom.io;

import org.jayson.JaysonException;
import org.jayson.JaysonWriter;

/**
 *
 * @author tofar
 */
public class GeomJaysonWriter extends JaysonWriter {

    private int nums;

    public GeomJaysonWriter(Appendable appendable) throws NullPointerException {
        super(appendable);
    }

    @Override
    protected void writeNull() throws JaysonException {
        nums = 0;
        super.writeNull();
    }

    @Override
    protected void writeBool(boolean bool) throws JaysonException {
        nums = 0;
        super.writeBool(bool);
    }

    @Override
    protected void writeNum(double num) throws JaysonException {
        if(++nums > 2){
            append(' ');
            nums = 1;
        }
        super.writeNum(num);
    }

    @Override
    protected void writeStr(String str) throws JaysonException {
        nums = 0;
        super.writeStr(str);
    }

    @Override
    protected void writeName(String name) throws JaysonException {
        nums = 0;
        super.writeName(name);
    }

    @Override
    protected void writeEndArray() throws JaysonException {
        nums = 0;
        super.writeEndArray();
    }

    @Override
    protected void writeBeginArray() throws JaysonException {
        nums = 0;
        super.writeBeginArray();
    }

    @Override
    protected void writeEndObject() throws JaysonException {
        nums = 0;
        super.writeEndObject();
    }

    @Override
    protected void writeBeginObject() throws JaysonException {
        nums = 0;
        super.writeBeginObject();
    }

}
