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
     * @throws JaysonException
     * @throws JaysonException if next was not string
     */
    public final String nextStr() throws JaysonException, JaysonException {
        next();
        return str();
    }

    /**
     * Get next num from stream
     *
     * @return
     * @throws JaysonException
     * @throws JaysonException if next was not num
     */
    public final double nextNum() throws JaysonException, JaysonException {
        next();
        return num();
    }

    /**
     * Get next boolean from stream
     *
     * @return
     * @throws JaysonException
     * @throws JaysonException if next was not boolean
     */
    public final boolean nextBool() throws JaysonException, JaysonException {
        next();
        return bool();
    }
}