package org.geomatics.io.wkt;

import java.io.EOFException;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import org.geomatics.geom.io.GeomIOException;

/**
 *
 * @author tofar
 */
public class WktInput implements AutoCloseable {

    private final PushbackReader reader;
    private final StringBuilder token;

    public WktInput(Reader reader) {
        this.reader = new PushbackReader(reader);
        this.token = new StringBuilder();
    }

    public String currentToken(){
        return token.toString();
    }
    
    /**
     * Get next char. Sequences of multiple white space chars are converted to
     * be single white space chars
     */
    public String nextToken() throws GeomIOException {
        try {
            token.setLength(0);
            while (true) {
                int i = reader.read();
                if (i <= 0) {
                    throw new EOFException();
                }
                char c = (char) i;
                if (((c >= 'A') && (c <= 'Z'))
                        || ((c >= 'a') && (c <= 'z'))
                        || ((c >= '0') && (c <= '9'))
                        || (c == '+')
                        || (c == '-')
                        || (c == '.')) {
                    token.append(c);
                } else if (Character.isWhitespace(c)) {
                    if (token.length() > 0) {
                        return token.toString(); // return only if token is not empty
                    }
                } else if (token.length() > 0) {
                    reader.unread(i);
                    return token.toString();
                } else {
                    return Character.toString(c);
                }
            }
        } catch (IOException ex) {
            throw new GeomIOException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

}
