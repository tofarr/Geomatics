package org.jg.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.jg.geom.Vect;
import org.jg.geom.VectBuilder;

/**
 *
 * @author tofar_000
 * @param <E>
 */
public final class VectMap<E> implements Serializable, Cloneable {

    static final int MAX_JUMPS = 4;
    static final int INITIAL_CAPACITY = 16;
    final int maxJumps;
    double[] ords;
    E[] values;
    int size;
    boolean copyOnEdit;
    int version;

    /**
     * Create a new instance of VectMap
     *
     * @param initialCapacity initial table size
     * @param maxJumps max jumps before a rehash occurs
     * @throws IllegalArgumentException if initialCapacity or maxJumps < 1
     */
    public VectMap(int initialCapacity, int maxJumps) throws IllegalArgumentException {
        if (initialCapacity <= 1) {
            throw new IllegalArgumentException("Invalid initialCapacity : " + initialCapacity);
        }
        if (maxJumps < 1) {
            throw new IllegalArgumentException("Invalid maxJumps : " + maxJumps);
        }
        initialCapacity = Integer.highestOneBit(initialCapacity - 1) << 1; // make sure capacity is a power of 2
        this.maxJumps = maxJumps;
        this.ords = new double[initialCapacity << 1];
        this.values = (E[]) new Object[initialCapacity];
        Arrays.fill(ords, Double.NaN); // All entries must initially be NaN
    }

    /**
     * Create a new instance of VectMap
     *
     * @param initialCapacity initial table size
     * @throws IllegalArgumentException if initialCapacity < 1
     */
    public VectMap(int initialCapacity) {
        this(initialCapacity, MAX_JUMPS);
    }

    /**
     * Create a new instanceof VectMap
     */
    public VectMap() {
        this(INITIAL_CAPACITY);
    }

    VectMap(int maxJumps, double[] ords, E[] values, int size, boolean copyOnEdit) {
        this.maxJumps = maxJumps;
        this.ords = ords;
        this.values = values;
        this.size = size;
        this.copyOnEdit = copyOnEdit;
    }

    /**
     * Get the number of vector mappings
     *
     * @return
     */
    public int size() {
        return size;
    }

    /**
     * Determine if empty (contains no vectors)
     *
     * @return
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Determine if map contains the vector given
     *
     * @param vect
     * @return
     * @throws NullPointerException if vect was null
     */
    public boolean containsKey(Vect vect) {
        return containsInternal(vect.x, vect.y);
    }

    /**
     * Determine if map contains the vector given
     *
     * @param x
     * @param y
     * @return
     * @throws IllegalArgumentException if x or y was infinite or NaN
     */
    public boolean containsKey(double x, double y) {
        Vect.check(x, y);
        return containsInternal(x, y);
    }

    boolean containsInternal(double x, double y) {
        int index = VectSet.hashIndex(x, y, ords);
        for (int s = maxJumps; s-- > 0;) {
            if ((ords[index] == x) && (ords[index + 1] == y)) {
                return true;
            }
            index = (index + 2) % ords.length;
        }
        return false;
    }

    /**
     * Get the value mapped from the vector given
     *
     * @param vect
     * @return value or null if no such mapping
     * @throws NullPointerException if vect was null
     */
    public E get(Vect vect) throws NullPointerException {
        return getInternal(vect.x, vect.y);
    }
    
    /**
     * Get the value mapped from the vector given
     *
     * @param vect
     * @return value or null if no such mapping
     * @throws NullPointerException if vect was null
     */
    public E get(VectBuilder vect) throws NullPointerException {
        return getInternal(vect.getX(), vect.getY());
    }

    /**
     * Get the value mapped from the vector given
     *
     * @param x
     * @param y
     * @return value or null if no such mapping
     * @throws IllegalArgumentException if x or y was infinite or NaN
     */
    public E get(double x, double y) {
        Vect.check(x, y);
        return getInternal(x, y);
    }

    E getInternal(double x, double y) {
        int index = VectSet.hashIndex(x, y, ords);
        for (int s = maxJumps; s-- > 0;) {
            if ((ords[index] == x) && (ords[index + 1] == y)) {
                return values[index >> 1];
            }
            index = (index + 2) % ords.length;
        }
        return null;
    }

    /**
     * Add / Update the mapping given
     *
     * @param vect
     * @param value
     * @return this
     * @throws NullPointerException if vect was null
     */
    public VectMap put(Vect vect, E value) throws NullPointerException {
        return putInternal(vect.x, vect.y, value);
    }

    /**
     * Add / Update the mapping given
     *
     * @param x
     * @param y
     * @param value
     * @return this
     * @throws IllegalArgumentException if x or y was infinite r NaN
     */
    public VectMap put(double x, double y, E value) throws IllegalArgumentException {
        Vect.check(x, y);
        return putInternal(x, y, value);
    }

    /**
     * Add / Update all the mappings given
     *
     * @param vects
     * @return this
     * @throws NullPointerException if vects was null
     */
    public VectMap putAll(VectMap vects) {
        if(vects.isEmpty() || (vects == this)){
            return this;
        }
        vects.forEach(new VectMapProcessor<E>(){
            @Override
            public boolean process(double x, double y, E value) {
                putInternal(x, y, value);
                return true;
            }        
        });
        return this;
    }

    /**
     * Add / Update all the mappings given
     *
     * @param vects
     * @return this
     * @throws NullPointerException if vects was null or a vector key was null
     */
    public VectMap putAll(Map<Vect, E> vects) throws NullPointerException {
        for (Entry<Vect, E> entry : vects.entrySet()) {
            Vect key = entry.getKey();
            putInternal(key.x, key.y, entry.getValue());
        }
        return this;
    }

    VectMap putInternal(double x, double y, E value) {
        version++;
        beforeUpdate();
        int capacity = ords.length;
        while (true) {
            switch (putMapping(x, y, value, ords, values, maxJumps)) {
                case 0:
                    return this; // exists
                case -1:
                    capacity = capacity << 1;
                    rehash(capacity);
                    break;
                default: // case 1
                    size++;
                    return this;
            }
        }
    }

    /**
     * Remove the mapping from vector given from this
     *
     * @param x
     * @param y
     * @return value stored under the mapping
     * @throws IllegalArgumentException if x or y was infinite or NaN
     */
    public E remove(double x, double y) throws IllegalArgumentException {
        Vect.check(x, y);
        return removeInternal(x, y);
    }

    /**
     * Remove the mapping from vector given from this
     *
     * @param vect
     * @return value stored under the mapping
     * @throws NullPointerException if vect was null
     */
    public E remove(Vect vect) {
        return removeInternal(vect.getX(), vect.getY());
    }

    E removeInternal(double x, double y) {
        beforeUpdate();
        int index = VectSet.hashIndex(x, y, ords);
        for (int s = maxJumps; s-- > 0;) {
            if ((ords[index] == x) && (ords[index + 1] == y)) {
                ords[index] = Double.NaN;
                size--;
                return values[index >> 1];
            }
            index = (index + 2) % ords.length;
        }
        return null;
    }

    /**
     * Remove all entries from this map
     */
    public void clear() {
        if (size != 0) {
            if (copyOnEdit) {
                ords = new double[ords.length];
                values = (E[]) new Object[ords.length >> 1];
                copyOnEdit = false;
            }
            Arrays.fill(ords, Double.NaN);
            size = 0;
        }
    }

    public boolean forEach(VectMapProcessor<E> processor) {
        int index = 0;
        final int storedVersion = version;
        while (true) {
            if (index >= ords.length) {
                return true;
            }
            if (Double.isNaN(ords[index])) {
                index += 2;
            } else {
                E value = values[index >> 1];
                if (!processor.process(ords[index++], ords[index++], value)) {
                    return false;
                }else if(storedVersion != version){
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    /**
     * Get a list of all keys
     *
     * @param target
     * @return target
     * @throws NullPointerException if target was null
     */
    public VectList keyList(VectList target) throws NullPointerException {
        int index = 0;
        while (true) {
            if (index >= ords.length) {
                return target;
            }
            if (!Double.isNaN(ords[index])) {
                target.addInternal(ords[index++], ords[index++]);
            } else {
                index += 2;
            }
        }
    }

    /**
     * Get a set of all keys
     *
     * @param target
     * @return target
     * @throws NullPointerException if target was null
     */
    public VectSet keySet(VectSet target) throws NullPointerException {
        int index = 0;
        while (true) {
            if (index >= ords.length) {
                return target;
            }
            if (!Double.isNaN(ords[index])) {
                target.addInternal(ords[index++], ords[index++]);
            } else {
                index += 2;
            }
        }
    }

    @Override
    public int hashCode() {
        VectList list = keyList(new VectList(size));
        list.sort();
        int hash = 3;
        for (int i = list.size(); i-- > 0;) {
            hash = 41 * hash + Vect.hashCode(list.getX(i), list.getY(i));
            hash = 41 * hash + Objects.hashCode(values[i]);
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VectMap) {
            VectMap<E> other = (VectMap) obj;
            if (size == other.size) {
                VectList keys = keyList(new VectList());
                keys.sort();
                VectList otherKeys = other.keyList(new VectList());
                otherKeys.sort();
                if (keys.equals(otherKeys)) {
                    for (int i = keys.size(); i-- > 0;) {
                        double x = keys.getX(i);
                        double y = keys.getY(i);
                        if (!Objects.equals(getInternal(x, y), other.getInternal(x, y))) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        if (size == 0) {
            return "[]";
        } else if (size < 50) {
            StringBuilder str = new StringBuilder();
            toString(str);
            return str.toString();
        } else {
            return "{size:" + size + "}";
        }
    }

    /**
     * Get a string representing this set in the format [x0,y0, x1,y1, ...
     * xn,yn]
     *
     * @param appendable
     * @throws IllegalStateException
     */
    public void toString(Appendable appendable) throws IllegalStateException {
        VectList list = keyList(new VectList(size));
        list.sort();
        try {
            appendable.append("[");
            for (int i = 0; i < list.size(); i++) {
                if (i != 0) {
                    appendable.append(", ");
                }
                appendable.append(Vect.ordToStr(list.getX(i)))
                        .append(',')
                        .append(Vect.ordToStr(list.getY(i)))
                        .append(',')
                        .append(Objects.toString(getInternal(list.getX(i), list.getY(i))));
            }
            appendable.append("]");
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public VectMap clone() {
        VectMap ret = new VectMap(maxJumps, ords, values, size, true);
        copyOnEdit = true;
        return ret;
    }

    void rehash(int capacity) {
        double[] newOrds = new double[capacity];
        E[] newValues = (E[]) new Object[capacity];
        Arrays.fill(newOrds, Double.NaN);
        int index = ords.length;
        while (index > 0) {
            double y = ords[--index];
            double x = ords[--index];
            if (!Double.isNaN(x)) {
                putMapping(x, y, values[index >> 1], newOrds, newValues, maxJumps);
            }
        }
        ords = newOrds;
        values = newValues;
    }

    static <E> int putMapping(double x, double y, E value, double[] ords, E[] values, int maxJumps) {
        int index = VectSet.hashIndex(x, y, ords);
        for (int s = maxJumps; s-- > 0;) {
            int next = index + 1;
            if (Double.isNaN(ords[index])) {
                ords[index] = x;
                ords[next] = y;
                values[index >> 1] = value;
                return 1;
            } else if ((ords[index] == x) && (ords[next] == y)) {
                values[index >> 1] = value;
                return 0;
            }
            index = (index + 2) % ords.length;
        }
        return -1;
    }

    void beforeUpdate() {
        if (copyOnEdit) {
            ords = ords.clone();
            values = values.clone();
            copyOnEdit = false;
        }
    }

    public interface VectMapProcessor<E> {

        boolean process(double x, double y, E value);
    }
}
