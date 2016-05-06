package org.jayson;

/**
 *
 * @author tofarrell
 */
public abstract class JaysonInput implements AutoCloseable {

    /**
     * Get next type from stream. return null when end of stream
     *
     * @return one of BEGIN_ARRAY,END_ARRAY,BEGIN_OBJECT,END_OBJECT,NAME,STRING,NUMBER,BOOLEAN,NULL
     */
    public abstract JaysonType next() throws JaysonException;

    /**
     * Get current string / name from stream
     *
     * @return
     * @throws JaysonException if current was not string / name
     */
    public abstract String str() throws JaysonException;

    /**
     * Get current number from stream
     *
     * @return
     * @throws JaysonException if current was not number
     */
    public abstract double num() throws JaysonException;

    /**
     * Get current boolean from stream
     *
     * @return
     * @throws JaysonException if current was not boolean
     */
    public abstract boolean bool() throws JaysonException;

    /**
     * Get next string from stream
     *
     * @return
     * @throws JaysonException if next was not string
     */
    public final String nextStr() throws JaysonException {
        next();
        return str();
    }

    /**
     * Get next num from stream
     *
     * @return
     * @throws JaysonException if next was not num
     */
    public final double nextNum() throws JaysonException {
        next();
        return num();
    }

    /**
     * Get next boolean from stream
     *
     * @return
     * @throws JaysonException if next was not boolean
     */
    public final boolean nextBool() throws JaysonException {
        next();
        return bool();
    }
    
    /**
     * Get next object from stream and check against expected value
     *
     * @return this
     * @throws JaysonException if next was not boolean
     */
    public final JaysonInput next(JaysonType expected) throws JaysonException {
        JaysonType type = next();
        if(type != expected){
            throw new JaysonException("Expected "+expected+" found "+type);
        }
        return this;
    }

    public void skip() {
        int level = 0;
        do {
            JaysonType type = next();
            switch (type) {
                case BEGIN_ARRAY:
                case BEGIN_OBJECT:
                    level++;
                    break;
                case END_ARRAY:
                case END_OBJECT:
                    level--;
                    break;
            }
        } while (level > 0);
    }
}
