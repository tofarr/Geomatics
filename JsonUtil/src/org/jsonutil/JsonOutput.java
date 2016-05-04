package org.jsonutil;

import java.util.ArrayDeque;

/**
 *
 * @author tofarrell
 */
public abstract class JsonOutput {

    protected final ArrayDeque<JsonType> parents;
    protected JsonType parent;
    protected JsonType prev;

    public JsonOutput() throws NullPointerException {
        this.parents = new ArrayDeque<>();
        parent = JsonType.NULL;
    }
    
    public JsonOutput beginObject() throws JsonException, IllegalStateException {
        beforeValue();
        parents.push(parent);
        parent = JsonType.BEGIN_OBJECT;
        prev = null;
        writeBeginObject();
        return this;
    }

    protected abstract void writeBeginObject() throws JsonException;

    public JsonOutput endObject() throws JsonException, IllegalStateException {
        if (parent != JsonType.BEGIN_OBJECT) {
            throw new IllegalStateException("Cannot end object!");
        }
        if (prev == JsonType.NAME) {
            throw new IllegalStateException("Cannot add key without value!");
        }
        prev = JsonType.END_OBJECT;
        parent = parents.pop();
        writeEndObject();
        return this;
    }

    protected abstract void writeEndObject() throws JsonException;

    public JsonOutput beginArray() throws JsonException, IllegalStateException {
        beforeValue();
        parents.push(parent);
        parent = JsonType.BEGIN_ARRAY;
        prev = null;
        writeBeginArray();
        return this;
    }

    protected abstract void writeBeginArray() throws JsonException;

    public JsonOutput endArray() throws JsonException, IllegalStateException {
        if (parent != JsonType.BEGIN_ARRAY) {
            throw new IllegalStateException("Cannot end array!");
        }
        prev = JsonType.END_ARRAY;
        parent = parents.pop();
        writeEndArray();
        return this;
    }

    protected abstract void writeEndArray() throws JsonException;

    public JsonOutput name(String name) throws JsonException, NullPointerException, IllegalStateException {
        if (parent != JsonType.BEGIN_OBJECT) {
            throw new IllegalStateException("Cannot add key outside object!");
        }
        if (prev == JsonType.NAME) {
            throw new IllegalStateException("Cannot add key without value!");
        }
        writeName(name);
        prev = JsonType.NAME;
        return this;
    }

    protected abstract void writeName(String name) throws JsonException;

    public JsonOutput str(String str) throws JsonException {
        beforeValue();
        prev = JsonType.STRING;
        writeStr(str);
        return this;
    }

    protected abstract void writeStr(String name) throws JsonException;

    public JsonOutput num(double num) throws JsonException {
        beforeValue();
        prev = JsonType.NUMBER;
        writeNum(num);
        return this;
    }

    protected abstract void writeNum(double num) throws JsonException;

    public JsonOutput bool(boolean bool) throws JsonException {
        beforeValue();
        prev = JsonType.BOOLEAN;
        writeBool(bool);
        return this;

    }

    protected abstract void writeBool(boolean bool) throws JsonException;

    public JsonOutput nul() throws JsonException {
        beforeValue();
        prev = JsonType.NULL;
        writeNull();
        return this;
    }
    
    protected abstract void writeNull() throws JsonException;
    

    protected void beforeValue() throws IllegalStateException, JsonException {
        if (parent == JsonType.BEGIN_OBJECT && (prev != null) && (prev != JsonType.NAME)) {
            throw new IllegalStateException("Cannot add value without key!");
        }
    }

    public void writeRemaining(JsonInput input) {
        while (!parents.isEmpty()) {
            JsonType type = input.next();
            if (type == null) {
                throw new JsonException("Unexpected end of stream");
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
