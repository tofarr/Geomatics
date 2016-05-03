package org.jg.io.json;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayDeque;

/**
 *
 * @author tofar
 */
public final class JsonWriter {

    private final Appendable appendable;
    private final ArrayDeque<JsonType> parents;
    private JsonType parent;
    private JsonType prev;
    private boolean whitespace;

    public JsonWriter(Appendable appendable) throws NullPointerException {
        if (appendable == null) {
            throw new NullPointerException();
        }
        this.appendable = appendable;
        this.parents = new ArrayDeque<>();
    }

    public JsonWriter beginObject() throws JsonException, IllegalStateException {
        beforeValue();
        parents.push(parent);
        parent = JsonType.BEGIN_OBJECT;
        prev = null;
        return append('{');
    }

    public JsonWriter endObject() throws JsonException, IllegalStateException {
        if (parent != JsonType.BEGIN_OBJECT) {
            throw new IllegalStateException("Cannot end object!");
        }
        if (prev == JsonType.NAME) {
            throw new IllegalStateException("Cannot add key without value!");
        }
        prev = JsonType.END_OBJECT;
        parent = parents.pop();
        return append('}');
    }

    public JsonWriter beginArray() throws JsonException, IllegalStateException {
        beforeValue();
        parents.push(parent);
        parent = JsonType.BEGIN_ARRAY;
        prev = null;
        return append('[');
    }

    public JsonWriter endArray() throws JsonException, IllegalStateException {
        if (parent != JsonType.BEGIN_ARRAY) {
            throw new IllegalStateException("Cannot end array!");
        }
        prev = JsonType.END_ARRAY;
        parent = parents.pop();
        return append(']');
    }

    public JsonWriter name(String name) throws JsonException, NullPointerException, IllegalStateException {
        if (parent != JsonType.BEGIN_OBJECT) {
            throw new IllegalStateException("Cannot add key outside object!");
        }
        if (prev == JsonType.NAME) {
            throw new IllegalStateException("Cannot add key without value!");
        }
        name = sanitize(name);
        prev = JsonType.NAME;
        return append(name);
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

    public JsonWriter str(String str) throws JsonException {
        beforeValue();
        str = str.replace("\"", "\\\"");
        prev = JsonType.STRING;
        return append(str);
    }

    public JsonWriter num(double num) throws JsonException {
        String ret = Double.toString(num);
        if (ret.endsWith(".0")) {
            ret = ret.substring(0, ret.length() - 2);
        }
        beforeValue();
        prev = JsonType.NUMBER;
        return append(ret);
    }

    public JsonWriter bool(boolean bool) throws JsonException {
        beforeValue();
        prev = JsonType.BOOLEAN;
        return append(Boolean.toString(bool));
    }

    /**
     * In some cases, whitespace can serve to make things easier to read, so
     * we explicitly allow it
     * @return 
     */
    public JsonWriter whitespace() {
        whitespace = true;
        return this;
    }

    public JsonWriter comment(String comment) throws JsonException {
        comment = comment.replace("*", "* ");
        return append(comment);
    }

    private JsonWriter append(char c) throws JsonException {
        try {
            appendable.append(c);
            return this;
        } catch (IOException ex) {
            throw new JsonException("Error writing", ex);
        }
    }

    private JsonWriter append(String str) throws JsonException {
        try {
            appendable.append(str);
            return this;
        } catch (IOException ex) {
            throw new JsonException("Error writing", ex);
        }
    }

    private void beforeValue() {
        if (parent == JsonType.BEGIN_OBJECT && (prev != null) && (prev != JsonType.NAME)) {
            throw new IllegalStateException("Cannot add value without key!");
        }
        if (prev != null) {
            append(',');
        }
        if (whitespace) {
            append(' ');
            whitespace = false;
        }
    }
}
