package org.jsonutil;

import java.io.StringReader;
import java.util.ArrayList;

/**
 *
 * @author tofarrell
 */
public class JsonBuffer extends JsonOutput {

    private final ArrayList<JsonType> types;
    private final ArrayList<Object> values;

    public JsonBuffer() {
        types = new ArrayList<>();
        values = new ArrayList<>();
    }

    public JsonBuffer(JsonInput input) throws JsonException{
        this();
        this.copyRemaining(input);
    }
      
    public JsonBuffer(String json) throws JsonException{
        this(new JsonReader(new StringReader(json)));
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
                typeIndex++;
                if(typeIndex >= types.size()){
                    if(parent != JsonType.NULL){
                        throw new JsonException("Unexpected end of stream!");
                    }
                    return null;
                }
                JsonType type = types.get(typeIndex);
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
            public String str() throws JsonException {
                JsonType type = types.get(typeIndex);
                if ((type != JsonType.STRING) && (type != JsonType.NAME)) {
                    throw new JsonException("Expected STRING, found "+type);
                }
                return (String) values.get(valueIndex);
            }

            @Override
            public double num() throws JsonException {
                if (types.get(typeIndex) != JsonType.NUMBER) {
                    throw new JsonException("Expected NUMBER, found "+types.get(typeIndex));
                }
                return (Double) values.get(valueIndex);
            }

            @Override
            public boolean bool() throws JsonException {
                if (types.get(typeIndex) != JsonType.BOOLEAN) {
                    throw new JsonException("Expected STRING, found "+types.get(typeIndex));
                }
                return (Boolean) values.get(valueIndex);
            }

            @Override
            public void close() {
            }
        };
    }

    public JsonInput getInputAt(String key, int level) {
        JsonInput input = getInput();
        JsonType type;
        while ((type = input.next()) != null) {
            switch(type){
                case NAME:
                    if((level == 0) && (input.str().equals(key))) {
                        return input;
                    }
                    break;
                case BEGIN_ARRAY:
                case BEGIN_OBJECT:
                    level--;
                    break;
                case END_ARRAY:
                case END_OBJECT:
                    level++;
                    break;
            }
        }
        return null;
    }

    public String findFirstStr(String key, int level) throws JsonException {
        JsonInput input = getInputAt(key, level);
        if (input == null) {
            throw new JsonException("Not found : " + key);
        }
        input.next();
        return input.str();
    }

    public double findFirstNum(String key, int level) throws JsonException {
        JsonInput input = getInputAt(key, level);
        if (input == null) {
            throw new JsonException("Not found : " + key);
        }
        input.next();
        return input.num();
    }

    public boolean findFirstBool(String key, int level) throws JsonException {
        JsonInput input = getInputAt(key, level);
        if (input == null) {
            throw new JsonException("Not found : " + key);
        }
        input.next();
        return input.bool();
    }

    @Override
    public void close() {
    }

    public JsonBuffer clear(){
        types.clear();
        values.clear();
        prev = null;
        parent = JsonType.NULL;
        parents.clear();
        return this;
    }
}
