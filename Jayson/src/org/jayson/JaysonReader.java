package org.jayson;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayDeque;

/**
 * Simple streaming json reader
 *
 * @author tofar
 */
public class JsonReader extends JsonInput {

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
    private final StringBuilder str;
    private JsonType parent;
    private ReaderType prev;
    private double num;
    private boolean bool;

    public JsonReader(Reader reader) throws NullPointerException, JsonException {
        this.reader = new PushbackReader(reader);
        this.parents = new ArrayDeque<>();
        parent = JsonType.NULL;
        str = new StringBuilder();
    }

    /**
     * Get next type from stream - return null when end of stream reached
     *
     * @return one of BEGIN_ARRAY,END_ARRAY,BEGIN_OBJECT,END_OBJECT,NAME,STRING,NUMBER,BOOLEAN,NULL
     */
    @Override
    public JsonType next() {
        try {
            while (true) {
                int c = nextValidChar();
                if(c < 0){
                    if(parents.isEmpty()){
                        return null;
                    }else{
                        throw new JsonException("Unexpected end of stream!");
                    }
                }
                switch (c) {
                    case '[':
                        if ((parent == JsonType.BEGIN_OBJECT) && (prev != ReaderType.COLON)) {
                            throw new JsonException("Values within objects must have a key!");
                        }
                        parents.push(parent);
                        parent = JsonType.BEGIN_ARRAY;
                        prev = null;
                        return JsonType.BEGIN_ARRAY;
                    case ']':
                        if (parent != JsonType.BEGIN_ARRAY) {
                            throw new JsonException("Attempting to close array when parent is " + parent);
                        } else if (prev == ReaderType.COMMA) {
                            throw new JsonException("Trailing comma in array!");
                        }
                        parent = parents.pop();
                        return JsonType.END_ARRAY;
                    case '{':
                        if ((parent == JsonType.BEGIN_OBJECT) && (prev != ReaderType.COLON)) {
                            throw new JsonException("Values within objects must have a key!");
                        }
                        parents.push(parent);
                        parent = JsonType.BEGIN_OBJECT;
                        prev = null;
                        return JsonType.BEGIN_OBJECT;
                    case '}':
                        if (parent != JsonType.BEGIN_OBJECT) {
                            throw new JsonException("Attempting to close object when parent is " + parent);
                        } else if (prev == ReaderType.COMMA) {
                            throw new JsonException("Trailing comma in object!");
                        }
                        parent = parents.pop();
                        return JsonType.END_OBJECT;
                    case ':':
                        if (parent != JsonType.BEGIN_OBJECT) {
                            throw new JsonException("Cannot specify key value pairs in type " + parent);
                        } else if (prev != ReaderType.NAME) {
                            throw new JsonException("Values within objects must have a key!");
                        }
                        prev = ReaderType.COLON;
                        break;
                    case ',':
                        if (parent == JsonType.NULL) {
                            throw new JsonException("Comma outside object!");
                        } else if (prev == null) {
                            throw new JsonException("Comma is first element in " + parent);
                        } else if (prev == ReaderType.NAME) {
                            throw new JsonException("Comma following name!");
                        } else if (prev == ReaderType.COLON) {
                            throw new JsonException("Comma following colon!");
                        } else if (prev == ReaderType.COMMA) {
                            throw new JsonException("Comma outside object!");
                        }
                        prev = ReaderType.COMMA;
                        break;
                    case '"':
                        if((prev != null) && (prev != ReaderType.COLON) && (prev != ReaderType.COMMA)){
                            throw new JsonException("Found STRING when expected COMMA");
                        }
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
                            readUncommentedString(c);
                            prev = ReaderType.NAME;
                            return JsonType.NAME;
                        }
                        readUncommentedString(c);
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
                                try{
                                    num = Double.parseDouble(value);
                                }catch(NumberFormatException ex){
                                    throw new JsonException("Error parsing number "+value, ex);
                                }
                                prev = ReaderType.NUMBER;
                                return JsonType.NUMBER;
                        }
                }
            }
        } catch (IOException ex) {
            throw new JsonException("Error reading Json", ex);
        }
    }

    private void readUncommentedString(int c) throws IOException {
        str.setLength(0);
        while (true) {
            if (c < 0) {
                if(parent == JsonType.NULL){
                    return;
                }
                throw new JsonException("Unexpected end of stream!");
            } else if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c == '$') || (c == '.') || (c == '+')) {
                str.append((char)c);
            } else {
                reader.unread(c);
                return;
            }
            c = reader.read();
        }
    }

    private void readCommentedString(int endChar) throws IOException {
        str.setLength(0);
        while (true) {
            int c = reader.read();
            if (c < 0) {
                throw new JsonException("Unexpected end of stream!");
            } else if (c == endChar) {
                return;
            } else if (c == '\\') {
                c = reader.read();
                if (c < 0) {
                    throw new JsonException("Unexpected end of stream!");
                }
            }
            str.append((char)c);
        }
    }

    private int nextValidChar() throws IOException {
        while (true) {
            int c = reader.read();
            if (c < 0) {
                return c;
            }
            if (Character.isWhitespace(c)) {
                continue;
            }
            if (c != '/') { // a comment
                return c;
            }
            c = reader.read();
            if (c < 0) {
                throw new JsonException("Unexpected end of stream!");
            }
            switch (c) {
                case '*':
                    while (true) {
                        c = reader.read();
                        if (c < 0) {
                            throw new JsonException("Unexpected end of stream!");
                        }
                        if (c == '*') {
                            c = reader.read();
                            if (c < 0) {
                                throw new JsonException("Unexpected end of stream!");
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
                            return c;
                        }
                        if (c == '\n') {
                            break;
                        }
                    }
                    break;
                default:
                    throw new JsonException("Unexpected characters: /" + ((char) c));
            }
        }
    }

    /**
     * Get current string / name from stream
     *
     * @return
     * @throws JsonException if current was not string / name
     */
    @Override
    public String str() throws JsonException {
        if (prev == ReaderType.NAME || prev == ReaderType.STRING) {
            return str.toString();
        } else {
            throw new JsonException("Expected STRING found " + prev);
        }
    }

    /**
     * Get current number from stream
     *
     * @return
     * @throws JsonException if current was not number
     */
    @Override
    public double num() throws JsonException {
        if (prev == ReaderType.NUMBER) {
            return num;
        } else {
            throw new JsonException("Expected NUMBER found " + prev);
        }
    }

    /**
     * Get current boolean from stream
     *
     * @return
     * @throws JsonException if currrent was not boolean
     */
    @Override
    public boolean bool() throws JsonException {
        if (prev == ReaderType.BOOLEAN) {
            return bool;
        } else {
            throw new JsonException("Expected BOOLEAN found " + prev);
        }
    }

    @Override
    public void close() throws JsonException {
        try {
            reader.close();
        } catch (IOException ex) {
            throw new JsonException("Error closing", ex);
        }
    }
}
