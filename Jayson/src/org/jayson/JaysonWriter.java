package org.jayson;

import java.io.IOException;

/**
 *
 * @author tofar
 */
public class JaysonWriter extends JaysonOutput {

    private final Appendable appendable;

    public JaysonWriter(Appendable appendable) throws NullPointerException {
        if (appendable == null) {
            throw new NullPointerException();
        }
        this.appendable = appendable;
    }

    @Override
    protected void writeBeginObject() throws JaysonException {
        append('{');
    }

    @Override
    protected void writeEndObject() throws JaysonException {
        append('}');
    }

    @Override
    protected void writeBeginArray() throws JaysonException {
        append('[');
    }

    @Override
    protected void writeEndArray() throws JaysonException {
        append(']');
    }

    @Override
    protected void writeName(String name) throws JaysonException {
        if(prev != null){
            append(',');
        }
        name = sanitize(name);
        append(name);
    }

    @Override
    protected void writeStr(String str) throws JaysonException {
        str = '\"' + str.replace("\"", "\\\"") + '\"';
        append(str);
    }

    @Override
    protected void writeNum(double num) throws JaysonException {
        String str = Double.toString(num);
        if (str.endsWith(".0")) {
            str = str.substring(0, str.length() - 2);
        }
        append(str);
    }

    @Override
    protected void writeBool(boolean bool) throws JaysonException {
        append(Boolean.toString(bool));
    }

    @Override
    protected void writeNull() throws JaysonException {
        append("null");
    }

    public JaysonWriter comment(String comment) throws JaysonException {
        comment = "/* " + comment.replace("*", "* ") + " */";
        append(comment);
        return this;
    }

    protected void append(char c) throws JaysonException {
        try {
            appendable.append(c);
        } catch (IOException ex) {
            throw new JaysonException("Error writing", ex);
        }
    }

    protected void append(String str) throws JaysonException {
        try {
            appendable.append(str);
        } catch (IOException ex) {
            throw new JaysonException("Error writing", ex);
        }
    }

    protected void beforeValue() {
        super.beforeValue();
        if (prev != null) {
            append((prev == JaysonType.NAME) ? ':' : ',');
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
    public void close() throws JaysonException {
        try {
            if(appendable instanceof AutoCloseable){
                ((AutoCloseable)appendable).close();
            }
        } catch (Exception ex) {
            throw new JaysonException("Error closing", ex);
        }
    }
}
