package org.jayson;

import java.io.StringReader;
import java.util.ArrayList;

/**
 *
 * @author tofarrell
 */
public class JaysonBuffer extends JaysonOutput {

    private final ArrayList<JaysonType> types;
    private final ArrayList<Object> values;

    public JaysonBuffer() {
        types = new ArrayList<>();
        values = new ArrayList<>();
    }

    public JaysonBuffer(JaysonInput input) throws JaysonException{
        this();
        this.copyRemaining(input);
    }
      
    public JaysonBuffer(String json) throws JaysonException{
        this(new JaysonReader(new StringReader(json)));
    }
        
    @Override
    protected void writeBeginObject() throws JaysonException {
        types.add(JaysonType.BEGIN_OBJECT);
    }

    @Override
    protected void writeEndObject() throws JaysonException {
        types.add(JaysonType.END_OBJECT);
    }

    @Override
    protected void writeBeginArray() throws JaysonException {
        types.add(JaysonType.BEGIN_ARRAY);
    }

    @Override
    protected void writeEndArray() throws JaysonException {
        types.add(JaysonType.END_ARRAY);
    }

    @Override
    protected void writeName(String name) throws JaysonException {
        types.add(JaysonType.NAME);
        values.add(name);
    }

    @Override
    protected void writeStr(String str) throws JaysonException {
        types.add(JaysonType.STRING);
        values.add(str);
    }

    @Override
    protected void writeNum(double num) throws JaysonException {
        types.add(JaysonType.NUMBER);
        values.add(num);
    }

    @Override
    protected void writeBool(boolean bool) throws JaysonException {
        types.add(JaysonType.BOOLEAN);
        values.add(bool);
    }

    @Override
    protected void writeNull() throws JaysonException {
        types.add(JaysonType.NULL);
    }

    public JaysonInput getInput() {
        return new JaysonInput() {
            int typeIndex = -1;
            int valueIndex = -1;

            @Override
            public JaysonType next() throws JaysonException {
                typeIndex++;
                if(typeIndex >= types.size()){
                    if(parent != JaysonType.NULL){
                        throw new JaysonException("Unexpected end of stream!");
                    }
                    return null;
                }
                JaysonType type = types.get(typeIndex);
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
            public String str() throws JaysonException {
                JaysonType type = types.get(typeIndex);
                if ((type != JaysonType.STRING) && (type != JaysonType.NAME)) {
                    throw new JaysonException("Expected STRING, found "+type);
                }
                return (String) values.get(valueIndex);
            }

            @Override
            public double num() throws JaysonException {
                if (types.get(typeIndex) != JaysonType.NUMBER) {
                    throw new JaysonException("Expected NUMBER, found "+types.get(typeIndex));
                }
                return (Double) values.get(valueIndex);
            }

            @Override
            public boolean bool() throws JaysonException {
                if (types.get(typeIndex) != JaysonType.BOOLEAN) {
                    throw new JaysonException("Expected STRING, found "+types.get(typeIndex));
                }
                return (Boolean) values.get(valueIndex);
            }

            @Override
            public void close() {
            }
        };
    }

    public JaysonInput getInputAt(String key, int level) {
        JaysonInput input = getInput();
        JaysonType type;
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

    public String findFirstStr(String key, int level) throws JaysonException {
        JaysonInput input = getInputAt(key, level);
        if (input == null) {
            throw new JaysonException("Not found : " + key);
        }
        input.next();
        return input.str();
    }

    public double findFirstNum(String key, int level) throws JaysonException {
        JaysonInput input = getInputAt(key, level);
        if (input == null) {
            throw new JaysonException("Not found : " + key);
        }
        input.next();
        return input.num();
    }

    public boolean findFirstBool(String key, int level) throws JaysonException {
        JaysonInput input = getInputAt(key, level);
        if (input == null) {
            throw new JaysonException("Not found : " + key);
        }
        input.next();
        return input.bool();
    }

    @Override
    public void close() {
    }

    public JaysonBuffer clear(){
        types.clear();
        values.clear();
        prev = null;
        parent = JaysonType.NULL;
        parents.clear();
        return this;
    }
}
