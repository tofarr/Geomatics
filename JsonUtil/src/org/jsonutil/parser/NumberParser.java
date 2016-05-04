package org.jsonutil.parser;

import java.lang.reflect.Type;
import org.jsonutil.JsonCoder;
import org.jsonutil.JsonException;
import org.jsonutil.JsonInput;
import org.jsonutil.JsonType;

/**
 *
 * @author tofarrell
 * @param <E>
 */
public abstract class NumberParser<E extends Number> extends JsonParser<E> {

    protected NumberParser() {
    }

    @Override
    public E parse(JsonType type, JsonCoder coder, JsonInput input) {
        switch (type) {
            case NULL:
                return null;
            case STRING:
                try {
                    return fromStr(input.str());
                } catch (NumberFormatException ex) {
                    throw new JsonException("Error initializing number number", ex);
                }
            case NUMBER:
                return fromNum(input.num());
            default:
                throw new JsonException("Expected NUMBER, found " + type);
        }
    }

    abstract E fromNum(double num);

    abstract E fromStr(String str) throws NumberFormatException;

    public static final NumberParser<Integer> INTEGER = new NumberParser<Integer>() {
        @Override
        Integer fromNum(double num) {
            return (int) num;
        }

        @Override
        Integer fromStr(String str) {
            return Integer.valueOf(str);
        }
    };

    public static final NumberParser<Long> LONG = new NumberParser<Long>() {
        @Override
        Long fromNum(double num) {
            return (long) num;
        }

        @Override
        Long fromStr(String str) {
            return Long.valueOf(str);
        }
    };

    public static final NumberParser<Double> DOUBLE = new NumberParser<Double>() {
        @Override
        Double fromNum(double num) {
            return num;
        }

        @Override
        Double fromStr(String str) {
            return Double.valueOf(str);
        }
    };

    public static final NumberParser<Float> FLOAT = new NumberParser<Float>() {
        @Override
        Float fromNum(double num) {
            return (float) num;
        }

        @Override
        Float fromStr(String str) {
            return Float.valueOf(str);
        }
    };

    public static final NumberParser<Short> SHORT = new NumberParser<Short>() {
        @Override
        Short fromNum(double num) {
            return (short) num;
        }

        @Override
        Short fromStr(String str) {
            return Short.valueOf(str);
        }
    };

    public static final NumberParser<Byte> BYTE = new NumberParser<Byte>() {
        @Override
        Byte fromNum(double num) {
            return (byte) num;
        }

        @Override
        Byte fromStr(String str) {
            return Byte.valueOf(str);
        }
    };

    public static class NumberParserFactory extends JsonParserFactory {

        public NumberParserFactory() {
            super(LATE);
        }

        @Override
        public JsonParser getParserFor(Type type) {
            if (type == byte.class || type == Byte.class) {
                return BYTE;
            } else if (type == double.class || type == Double.class) {
                return DOUBLE;
            } else if (type == float.class || type == Float.class) {
                return FLOAT;
            } else if (type == int.class || type == Integer.class) {
                return INTEGER;
            } else if (type == long.class || type == Long.class) {
                return LONG;
            } else if (type == short.class || type == Short.class) {
                return SHORT;
            } else {
                return null;
            }
        }
    }
}
