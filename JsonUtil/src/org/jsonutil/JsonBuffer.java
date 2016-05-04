package org.jsonutil;

import java.util.ArrayList;

/**
 *
 * @author tofarrell
 */
public class JsonBuffer extends JsonOutput {

    private final ArrayList<JsonType> types;
    private final ArrayList<Object> values;

    public JsonBuffer() throws NullPointerException {
        types = new ArrayList<>();
        values = new ArrayList<>();
    }

    @Override
    protected void writeBeginObject() throws JsonException {
        types.add(JsonType.BEGIN_OBJECT);
    }

    @Override
    protected void writeEndObject() throws JsonException {
        types.add(JsonType.END_OBJECT);
    }

    @Override
    protected void writeBeginArray() throws JsonException {
        types.add(JsonType.BEGIN_ARRAY);
    }

    @Override
    protected void writeEndArray() throws JsonException {
        types.add(JsonType.END_ARRAY);
    }

    @Override
    protected void writeName(String name) throws JsonException {
        types.add(JsonType.NAME);
        values.add(name);
    }

    @Override
    protected void writeStr(String str) throws JsonException {
        types.add(JsonType.STRING);
        values.add(str);
    }

    @Override
    protected void writeNum(double num) throws JsonException {
        types.add(JsonType.NUMBER);
        values.add(num);
    }

    @Override
    protected void writeBool(boolean bool) throws JsonException {
        types.add(JsonType.BOOLEAN);
        values.add(bool);
    }

    @Override
    protected void writeNull() throws JsonException {
        types.add(JsonType.NULL);
    }

    public JsonInput getInput() {
        return new JsonInput() {
            int typeIndex = -1;
            int valueIndex = -1;

            @Override
            public JsonType next() throws JsonException {
                JsonType type = types.get(++typeIndex);
                switch (type) {
                    case BOOLEAN:
                    case NAME:
                    case NUMBER:
                    case STRING:
                        valueIndex++;
                }
                return type;
            }

            @Override
            public String str() throws IllegalStateException {
                if (types.get(typeIndex) != JsonType.STRING) {
                    throw new IllegalStateException();
                }
                return (String) values.get(valueIndex);
            }

            @Override
            public double num() throws IllegalStateException {
                if (types.get(typeIndex) != JsonType.NUMBER) {
                    throw new IllegalStateException();
                }
                return (Double) values.get(valueIndex);
            }

            @Override
            public boolean bool() throws IllegalStateException {
                if (types.get(typeIndex) != JsonType.BOOLEAN) {
                    throw new IllegalStateException();
                }
                return (Boolean) values.get(valueIndex);
            }

        };
    }

    public JsonInput getInputAt(String key) {
        JsonInput input = getInput();
        JsonType type;
        while ((type = input.next()) != null) {
            if (type == JsonType.NAME) {
                if (input.str().equals(key)) {
                    return input;
                }
            }
        }
        return null;
    }

    public String findFirstStr(String key) throws JsonException {
        JsonInput input = getInputAt(key);
        if (input == null) {
            throw new JsonException("Not found : " + key);
        }
        input.next();
        return input.str();
    }

    public double findFirstNum(String key) throws JsonException {
        JsonInput input = getInputAt(key);
        if (input == null) {
            throw new JsonException("Not found : " + key);
        }
        input.next();
        return input.num();
    }

    public boolean findFirstBool(String key) throws JsonException {
        JsonInput input = getInputAt(key);
        if (input == null) {
            throw new JsonException("Not found : " + key);
        }
        input.next();
        return input.bool();
    }

}
