package org.jg.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayDeque;
import org.jg.geom.GeomIOException;

/**
 * Simple Json reader
 *
 * @author tofar
 */
public class JsonReader {

    public enum Type {
        BEGIN_OBJECT,
        END_OBJECT,
        BEGIN_ARRAY,
        END_ARRAY,
        NAME,
        STRING,
        NUMBER,
        BOOLEAN,
        NULL,
        COLON,
        COMMA,
    }
    private final PushbackReader reader;
    private final ArrayDeque<Type> parents;
    private Type parent;
    private Type prev;
    private StringBuilder str;
    private double num;
    private boolean bool;

    public JsonReader(Reader reader) throws NullPointerException, GeomIOException {
        this.reader = new PushbackReader(reader);
        this.parents = new ArrayDeque<>();
    }

    public Type next() {
        try {
            while (true) {
                int c = nextValidChar();
                switch (c) {
                    case '[':
                        if ((parent == Type.BEGIN_OBJECT) && (prev != Type.COLON)) {
                            throw new GeomIOException("Values within objects must have a key!");
                        }
                        parents.push(parent);
                        parent = Type.BEGIN_ARRAY;
                        prev = null;
                        return Type.BEGIN_ARRAY;
                    case ']':
                        if (parent != Type.BEGIN_ARRAY) {
                            throw new GeomIOException("Attempting to close array when parent is " + parent);
                        } else if (prev == Type.COMMA) {
                            throw new GeomIOException("Trailing comma in array!");
                        }
                        parent = parents.pop();
                        return Type.END_ARRAY;
                    case '{':
                        if ((parent == Type.BEGIN_OBJECT) && (prev != Type.COLON)) {
                            throw new GeomIOException("Values within objects must have a key!");
                        }
                        parents.push(parent);
                        parent = Type.BEGIN_OBJECT;
                        prev = null;
                        return Type.BEGIN_OBJECT;
                    case '}':
                        if (parent != Type.BEGIN_OBJECT) {
                            throw new GeomIOException("Attempting to close object when parent is " + parent);
                        } else if (prev == Type.COMMA) {
                            throw new GeomIOException("Trailing comma in array!");
                        }
                        parent = parents.pop();
                        return Type.END_OBJECT;
                    case ':':
                        if (parent != Type.BEGIN_OBJECT) {
                            throw new GeomIOException("Cannot specify key value pairs in type " + parent);
                        } else if (prev != Type.NAME) {
                            throw new GeomIOException("Values within objects must have a key!");
                        }
                        prev = Type.COLON;
                        break;
                    case ',':
                        if (parent == null) {
                            throw new GeomIOException("Comma outside object!");
                        } else if (prev == null) {
                            throw new GeomIOException("Comma is first element in " + parent);
                        } else if (prev == Type.NAME) {
                            throw new GeomIOException("Comma following name!");
                        } else if (prev == Type.COLON) {
                            throw new GeomIOException("Comma following colon!");
                        } else if (prev == Type.COMMA) {
                            throw new GeomIOException("Comma outside object!");
                        }
                        prev = Type.COMMA;
                        break;
                    case '"':
                        readCommentedString('"');
                        if ((parent == Type.BEGIN_OBJECT) && (prev == null || prev == Type.COMMA)) {
                            prev = Type.NAME;
                            return prev;
                        } else {
                            prev = Type.STRING;
                            return prev;
                        }
                    default:
                        //could be name, or could be boolean number or null
                        if ((parent == Type.BEGIN_OBJECT) && (prev == null || prev == Type.COMMA)) {
                            readUncommentedString();
                            prev = Type.NAME;
                            return prev;
                        }
                        readUncommentedString();
                        String value = str.toString();
                        switch (value) {
                            case "true":
                                bool = true;
                                prev = Type.BOOLEAN;
                                return prev;
                            case "false":
                                bool = false;
                                prev = Type.BOOLEAN;
                                return prev;
                            case "null":
                                prev = Type.NULL;
                                return prev;
                            default: // number
                                num = Double.parseDouble(value);
                                prev = Type.NUMBER;
                                return prev;
                        }
                }
            }
        } catch (IOException ex) {
            throw new GeomIOException("Error reading Json", ex);
        }
    }

    private void readUncommentedString() throws IOException {
        str.setLength(0);
        while (true) {
            int c = reader.read();
            if (c < 0) {
                throw new EOFException();
            } else if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c == '$') || (c == '.')) {
                str.append(c);
            } else {
                reader.unread(c);
                return;
            }
        }
    }

    private void readCommentedString(int endChar) throws IOException {
        str.setLength(0);
        while (true) {
            int c = reader.read();
            if (c < 0) {
                throw new EOFException();
            } else if (c == endChar) {
                return;
            } else if (c == '\\') {
                c = reader.read();
                if (c < 0) {
                    throw new EOFException();
                }
            }
            str.append(c);
        }
    }

    private int nextValidChar() throws IOException {
        while (true) {
            int c = reader.read();
            if (c < 0) {
                throw new EOFException();
            }
            if (Character.isWhitespace(c)) {
                continue;
            }
            if (c == '/') { // a comment
                c = reader.read();
                if (c < 0) {
                    throw new EOFException();
                }
                switch (c) {
                    case '*':
                        while (true) {
                            c = reader.read();
                            if (c < 0) {
                                throw new EOFException();
                            }
                            if (c == '*') {
                                c = reader.read();
                                if (c < 0) {
                                    throw new EOFException();
                                }
                                if (c == '/') {
                                    break;
                                }
                            }
                        }
                        break;
                    case '/':
                        while (true) {
                            c = reader.read();
                            if (c < 0) {
                                throw new EOFException();
                            }
                            if (c == '\n') {
                                break;
                            }
                        }
                        break;
                    default:
                        throw new IOException("Unexpected characters: /" + ((char) c));
                }
            }
            return c;
        }
    }

    public String str() {
        if (prev == Type.NAME || prev == Type.STRING) {
            return str.toString();
        } else {
            throw new IllegalStateException("Requested string when type was " + prev);
        }
    }

    public double num() {
        if (prev == Type.NUMBER) {
            return num;
        } else {
            throw new IllegalStateException("Requested string when type was " + prev);
        }
    }

    public boolean bool() {
        if (prev == Type.BOOLEAN) {
            return bool;
        } else {
            throw new IllegalStateException("Requested string when type was " + prev);
        }
    }
}
