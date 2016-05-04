package org.jsonutil;

/**
 *
 * @author tofarrell
 */
public abstract class JsonInput {

    /**
     * Get next type from stream. return null when end of stream
     *
     * @return one of BEGIN_ARRAY,END_ARRAY,BEGIN_OBJECT,END_OBJECT,NAME,STRING,NUMBER,BOOLEAN,NULL
     */
    public abstract JsonType next() throws JsonException;

    /**
     * Get current string / name from stream
     *
     * @return
     * @throws JsonException if current was not string / name
     */
    public abstract String str() throws JsonException;

    /**
     * Get current number from stream
     *
     * @return
     * @throws JsonException if current was not number
     */
    public abstract double num() throws JsonException;

    /**
     * Get current boolean from stream
     *
     * @return
     * @throws JsonException if current was not boolean
     */
    public abstract boolean bool() throws JsonException;

    /**
     * Get next string from stream
     *
     * @return
     * @throws JsonException
     * @throws JsonException if next was not string
     */
    public final String nextStr() throws JsonException, JsonException {
        next();
        return str();
    }

    /**
     * Get next num from stream
     *
     * @return
     * @throws JsonException
     * @throws JsonException if next was not num
     */
    public final double nextNum() throws JsonException, JsonException {
        next();
        return num();
    }

    /**
     * Get next boolean from stream
     *
     * @return
     * @throws JsonException
     * @throws JsonException if next was not boolean
     */
    public final boolean nextBool() throws JsonException, JsonException {
        next();
        return bool();
    }
}
