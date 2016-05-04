package org.jsonutil;

import java.io.IOException;
import java.util.ArrayDeque;

/**
 *
 * @author tofar
 */
public final class JsonWriter extends JsonOutput {

    private final Appendable appendable;
    private boolean whitespace;

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

    /**
     * In some cases, whitespace can serve to make things easier to read, so we explicitly allow it
     *
     * @return
     */
    public JsonWriter whitespace() {
        whitespace = true;
        return this;
    }

    public JsonWriter comment(String comment) throws JsonException {
        comment = "/* " + comment.replace("*", "* ") + " */";
        append(comment);
        return this;
    }

    private void append(char c) throws JsonException {
        try {
            appendable.append(c);
        } catch (IOException ex) {
            throw new JsonException("Error writing", ex);
        }
    }

    private void append(String str) throws JsonException {
        try {
            appendable.append(str);
        } catch (IOException ex) {
            throw new JsonException("Error writing", ex);
        }
    }

    protected void beforeValue() {
        super.beforeValue();
        if (prev != null) {
            append(',');
        }
        if (whitespace) {
            append(' ');
            whitespace = false;
        }
    }

    static String sanitize(String name) {
        if (name.length() == 0) {
            return "\"\"";
        }
        char c = name.charAt(0);
        if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'Z') || (c == '$'))) {
            return '"' + name.replace("\"", "\\\"") + '"';
        }
        for (int i = 1; i < name.length(); i++) {
            if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'Z') || (c == '$') || (c >= '0' && c <= '9'))) {
                return '"' + name.replace("\"", "\\\"") + '"';
            }
        }
        return name;
    }

}
