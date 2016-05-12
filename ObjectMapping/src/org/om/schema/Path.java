package org.om.schema;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 *
 * @author tofarrell
 */
public class Path {

    private static final Pattern pattern = Pattern.compile("[\\d$]+");
    public static final char SEPARATOR = '/';
    public static final Path ROOT = new Path(null, "");

    final Path parent;
    final String name;

    private Path(Path parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public Path add(String name) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Invalid path element : " + name);
        }
        for (int i = name.length(); i-- > 0;) {
            char c = name.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (c == '$') || (c == '_'))) {
                throw new IllegalArgumentException("Invalid path element : " + name);
            }
        }
        return new Path(this, name);
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        toString(str);
        return str.toString();
    }

    public void toString(Appendable appendable) {
        try {
            if (parent != null) {
                parent.toString(appendable);
                appendable.append(SEPARATOR);
            }
            appendable.append(name);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public int size() {
        return 1 + ((parent == null) ? 0 : parent.size());
    }
}
