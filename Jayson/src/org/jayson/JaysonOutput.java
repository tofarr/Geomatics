package org.jayson;

import java.util.ArrayDeque;

/**
 *
 * @author tofarrell
 */
public abstract class JaysonOutput implements AutoCloseable {

    protected final ArrayDeque<JaysonType> parents;
    protected JaysonType parent;
    protected JaysonType prev;

    public JaysonOutput() throws NullPointerException {
        this.parents = new ArrayDeque<>();
        parent = JaysonType.NULL;
    }
    
    public JaysonOutput beginObject() throws JaysonException {
        beforeValue();
        parents.push(parent);
        parent = JaysonType.BEGIN_OBJECT;
        prev = null;
        writeBeginObject();
        return this;
    }

    protected abstract void writeBeginObject() throws JaysonException;

    public JaysonOutput endObject() throws JaysonException {
        if (parent != JaysonType.BEGIN_OBJECT) {
            throw new JaysonException("Cannot end object!");
        }
        if (prev == JaysonType.NAME) {
            throw new JaysonException("Cannot add key without value!");
        }
        prev = JaysonType.END_OBJECT;
        parent = parents.pop();
        writeEndObject();
        return this;
    }

    protected abstract void writeEndObject() throws JaysonException;

    public JaysonOutput beginArray() throws JaysonException {
        beforeValue();
        parents.push(parent);
        parent = JaysonType.BEGIN_ARRAY;
        prev = null;
        writeBeginArray();
        return this;
    }

    protected abstract void writeBeginArray() throws JaysonException;

    public JaysonOutput endArray() throws JaysonException {
        if (parent != JaysonType.BEGIN_ARRAY) {
            throw new JaysonException("Cannot end array!");
        }
        prev = JaysonType.END_ARRAY;
        parent = parents.pop();
        writeEndArray();
        return this;
    }

    protected abstract void writeEndArray() throws JaysonException;

    public JaysonOutput name(String name) throws JaysonException, NullPointerException, JaysonException {
        if (parent != JaysonType.BEGIN_OBJECT) {
            throw new JaysonException("Cannot add key outside object!");
        }
        if (prev == JaysonType.NAME) {
            throw new JaysonException("Cannot add key without value!");
        }
        writeName(name);
        prev = JaysonType.NAME;
        return this;
    }

    protected abstract void writeName(String name) throws JaysonException;

    public JaysonOutput str(String str) throws JaysonException {
        beforeValue();
        prev = JaysonType.STRING;
        writeStr(str);
        return this;
    }

    protected abstract void writeStr(String name) throws JaysonException;

    public JaysonOutput num(double num) throws JaysonException {
        beforeValue();
        prev = JaysonType.NUMBER;
        writeNum(num);
        return this;
    }

    protected abstract void writeNum(double num) throws JaysonException;

    public JaysonOutput bool(boolean bool) throws JaysonException {
        beforeValue();
        prev = JaysonType.BOOLEAN;
        writeBool(bool);
        return this;

    }

    protected abstract void writeBool(boolean bool) throws JaysonException;

    public JaysonOutput nul() throws JaysonException {
        beforeValue();
        prev = JaysonType.NULL;
        writeNull();
        return this;
    }
    
    protected abstract void writeNull() throws JaysonException;
    

    protected void beforeValue() throws JaysonException {
        if (parent == JaysonType.BEGIN_OBJECT && (prev != JaysonType.NAME)) {
            throw new JaysonException("Cannot add value without key!");
        }
    }

    public final void copyRemaining(JaysonInput input) {
        int targetLevel = parents.size()-1;
        while(parents.size() != targetLevel){
            JaysonType type = input.next();
            if (type == null) {
                throw new JaysonException("Unexpected end of stream");
            }
            switch (type) {
                case BEGIN_ARRAY:
                    beginArray();
                    break;
                case BEGIN_OBJECT:
                    beginObject();
                    break;
                case BOOLEAN:
                    bool(input.bool());
                    break;
                case END_ARRAY:
                    endArray();
                    break;
                case END_OBJECT:
                    endObject();
                    break;
                case NAME:
                    name(input.str());
                    break;
                case NULL:
                    nul();
                    break;
                case NUMBER:
                    num(input.num());
                    break;
                case STRING:
                    str(input.str());
                    break;
            }
        }
    }
}
