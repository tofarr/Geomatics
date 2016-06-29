package org.om.store.key;

import java.beans.ConstructorProperties;
import java.beans.Transient;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;
import org.om.element.Element;
import org.om.element.ElementType;
import org.om.element.NumElement;

/**
 *
 * @author tofar
 */
public class AtomicLongKeyGenerator implements KeyGenerator, Serializable {

    private final AtomicLong seq;

    public AtomicLongKeyGenerator() {
        seq = new AtomicLong(System.currentTimeMillis());
    }

    @ConstructorProperties({"value"})
    public AtomicLongKeyGenerator(long value) {
        seq = new AtomicLong(value);
    }

    public long getValue() {
        return seq.get();
    }

    @Override
    @Transient
    public ElementType getType() {
        return ElementType.NUMBER;
    }

    @Override
    public NumElement createKey() {
        long key = seq.incrementAndGet();
        return NumElement.valueOf(key);
    }

}
