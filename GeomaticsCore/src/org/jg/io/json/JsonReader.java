package org.jg.io.json;

import java.io.EOFException;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayDeque;
import org.jg.geom.GeomIOException;

/**
 * Simple streaming json reader
 *
 * @author tofar
 */
public class JsonReader {

    private enum ReaderType {
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
    private final ArrayDeque<JsonType> parents;
    private JsonType parent;
    private ReaderType prev;
    private StringBuilder str;
    private double num;
    private boolean bool;

    public JsonReader(Reader reader) throws NullPointerException, GeomIOException {
        this.reader = new PushbackReader(reader);
        this.parents = new ArrayDeque<>();
    }

    /**
     * Get next type from stream
     *
     * @return one of BEGIN_ARRAY,END_ARRAY,BEGIN_OBJECT,END_OBJECT,NAME,STRING,NUMBER,BOOLEAN,NULL
     */
    public JsonType next() {
        try {
            while (true) {
                int c = nextValidChar();
                switch (c) {
                    case '[':
                        if ((parent == JsonType.BEGIN_OBJECT) && (prev != ReaderType.COLON)) {
                            throw new GeomIOException("Values within objects must have a key!");
                        }
                        parents.push(parent);
                        parent = JsonType.BEGIN_ARRAY;
                        prev = null;
                        return JsonType.BEGIN_ARRAY;
                    case ']':
                        if (parent != JsonType.BEGIN_ARRAY) {
                            throw new GeomIOException("Attempting to close array when parent is " + parent);
                        } else if (prev == ReaderType.COMMA) {
                            throw new GeomIOException("Trailing comma in array!");
                        }
                        parent = parents.pop();
                        return JsonType.END_ARRAY;
                    case '{':
                        if ((parent == JsonType.BEGIN_OBJECT) && (prev != ReaderType.COLON)) {
                            throw new GeomIOException("Values within objects must have a key!");
                        }
                        parents.push(parent);
                        parent = JsonType.BEGIN_OBJECT;
                        prev = null;
                        return JsonType.BEGIN_OBJECT;
                    case '}':
                        if (parent != JsonType.BEGIN_OBJECT) {
                            throw new GeomIOException("Attempting to close object when parent is " + parent);
                        } else if (prev == ReaderType.COMMA) {
                            throw new GeomIOException("Trailing comma in array!");
                        }
                        parent = parents.pop();
                        return JsonType.END_OBJECT;
                    case ':':
                        if (parent != JsonType.BEGIN_OBJECT) {
                            throw new GeomIOException("Cannot specify key value pairs in type " + parent);
                        } else if (prev != ReaderType.NAME) {
                            throw new GeomIOException("Values within objects must have a key!");
                        }
                        prev = ReaderType.COLON;
                        break;
                    case ',':
                        if (parent == null) {
                            throw new GeomIOException("Comma outside object!");
                        } else if (prev == null) {
                            throw new GeomIOException("Comma is first element in " + parent);
                        } else if (prev == ReaderType.NAME) {
                            throw new GeomIOException("Comma following name!");
                        } else if (prev == ReaderType.COLON) {
                            throw new GeomIOException("Comma following colon!");
                        } else if (prev == ReaderType.COMMA) {
                            throw new GeomIOException("Comma outside object!");
                        }
                        prev = ReaderType.COMMA;
                        break;
                    case '"':
                        readCommentedString('"');
                        if ((parent == JsonType.BEGIN_OBJECT) && (prev == null || prev == ReaderType.COMMA)) {
                            prev = ReaderType.NAME;
                            return JsonType.NAME;
                        } else {
                            prev = ReaderType.STRING;
                            return JsonType.STRING;
                        }
                    default:
                        //could be name, or could be boolean number or null
                        if ((parent == JsonType.BEGIN_OBJECT) && (prev == null || prev == ReaderType.COMMA)) {
                            readUncommentedString();
                            prev = ReaderType.NAME;
                            return JsonType.NAME;
                        }
                        readUncommentedString();
                        String value = str.toString();
                        switch (value) {
                            case "true":
                                bool = true;
                                prev = ReaderType.BOOLEAN;
                                return JsonType.BOOLEAN;
                            case "false":
                                bool = false;
                                prev = ReaderType.BOOLEAN;
                                return JsonType.BOOLEAN;
                            case "null":
                                prev = ReaderType.NULL;
                                return JsonType.NULL;
                            default: // number
                                num = Double.parseDouble(value);
                                prev = ReaderType.NUMBER;
                                return JsonType.NUMBER;
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

    /**
     * Get current string / name from stream
     *
     * @return
     * @throws IllegalStateException if current was not string / name
     */
    public String str() throws IllegalStateException {
        if (prev == ReaderType.NAME || prev == ReaderType.STRING) {
            return str.toString();
        } else {
            throw new IllegalStateException("Requested string when type was " + prev);
        }
    }

    /**
     * Get current number from stream
     *
     * @return
     * @throws IllegalStateException if current was not number
     */
    public double num() throws IllegalStateException {
        if (prev == ReaderType.NUMBER) {
            return num;
        } else {
            throw new IllegalStateException("Requested string when type was " + prev);
        }
    }

    /**
     * Get current boolean from stream
     *
     * @return
     * @throws IllegalStateException if currrent was not boolean
     */
    public boolean bool() throws IllegalStateException {
        if (prev == ReaderType.BOOLEAN) {
            return bool;
        } else {
            throw new IllegalStateException("Requested string when type was " + prev);
        }
    }

    public String nextStr() {
        next();
        return str();
    }

    public double nextNum() {
        next();
        return num();
    }

    public boolean nextBool() {
        next();
        return bool();
    }
}
