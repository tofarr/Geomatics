package org.jsonutil;

import java.io.IOException;

/**
 *
 * @author tofar
 */
public class JsonWriter extends JsonOutput {

    private final Appendable appendable;

    public JsonWriter(Appendable appendable) throws NullPointerException {
        if (appendable == null) {
            throw new NullPointerException();
        }
        this.appendable = appendable;
    }

    @Override
    protected void writeBeginObject() throws JsonException {
        append('{');
    }

    @Override
    protected void writeEndObject() throws JsonException {
        append('}');
    }

    @Override
    protected void writeBeginArray() throws JsonException {
        append('[');
    }

    @Override
    protected void writeEndArray() throws JsonException {
        append(']');
    }

    @Override
    protected void writeName(String name) throws JsonException {
        if(prev != null){
            append(',');
        }
        name = sanitize(name);
        append(name);
    }

    @Override
    protected void writeStr(String str) throws JsonException {
        str = '\"' + str.replace("\"", "\\\"") + '\"';
        append(str);
    }

    @Override
    protected void writeNum(double num) throws JsonException {
        String str = Double.toString(num);
        if (str.endsWith(".0")) {
            str = str.substring(0, str.length() - 2);
        }
        append(str);
    }

    @Override
    protected void writeBool(boolean bool) throws JsonException {
        append(Boolean.toString(bool));
    }

    @Override
    protected void writeNull() throws JsonException {
        append("null");
    }

    public JsonWriter comment(String comment) throws JsonException {
        comment = "/* " + comment.replace("*", "* ") + " */";
        append(comment);
        return this;
    }

    protected void append(char c) throws JsonException {
        try {
            appendable.append(c);
        } catch (IOException ex) {
            throw new JsonException("Error writing", ex);
        }
    }

    protected void append(String str) throws JsonException {
        try {
            appendable.append(str);
        } catch (IOException ex) {
            throw new JsonException("Error writing", ex);
        }
    }

    protected void beforeValue() {
        super.beforeValue();
        if (prev != null) {
            append((prev == JsonType.NAME) ? ':' : ',');
        }
    }

    static String sanitize(String name) {
        if (name.length() == 0) {
            return "\"\"";
        }
        char c = name.charAt(0);
        if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c == '$'))) {
            return '"' + name.replace("\"", "\\\"") + '"';
        }
        for (int i = 1; i < name.length(); i++) {
            c = name.charAt(i);
            if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c == '$') || (c >= '0' && c <= '9'))) {
                return '"' + name.replace("\"", "\\\"") + '"';
            }
        }
        return name;
    }

    @Override
    public void close() throws JsonException {
        try {
            if(appendable instanceof AutoCloseable){
                ((AutoCloseable)appendable).close();
            }
        } catch (Exception ex) {
            throw new JsonException("Error closing", ex);
        }
    }
}
