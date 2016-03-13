package org.jg.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import org.jg.geom.GeomException;
import org.jg.geom.Vect;
import org.jg.geom.VectBuilder;

/**
 * Set of vectors backed by an open hash table
 *
 * @author tofar_000
 */
public final class VectSet implements Serializable, Cloneable, Iterable<Vect> {

    static final int MAX_JUMPS = 4;
    static final int INITIAL_CAPACITY = 16;
    final int maxJumps;
    double[] ords;
    int size;
    transient boolean copyOnEdit;
    int version;

    /**
     * Create a new instance of VectSet
     *
     * @param initialCapacity initial table size
     * @param maxJumps max jumps before a rehash occurs
     * @throws IllegalArgumentException if initialCapacity or maxJumps < 1
     */
    public VectSet(int initialCapacity, int maxJumps) throws IllegalArgumentException {
        if (initialCapacity <= 1) {
            throw new IllegalArgumentException("Invalid initialCapacity : " + initialCapacity);
        }
        if (maxJumps < 1) {
            throw new IllegalArgumentException("Invalid maxJumps : " + maxJumps);
        }
        initialCapacity = Integer.highestOneBit(initialCapacity - 1) << 1; // make sure capacity is a power of 2
        this.maxJumps = maxJumps;
        this.ords = new double[initialCapacity << 1];
        Arrays.fill(ords, Double.NaN); // All entries must initially be NaN
    }

    /**
     * Create a new instance of VectSet
     *
     * @param initialCapacity initial table size
     * @throws IllegalArgumentException if initialCapacity < 1
     */
    public VectSet(int initialCapacity) throws IllegalArgumentException {
        this(initialCapacity, MAX_JUMPS);
    }

    /**
     * Create a new instance of VectSet
     */
    public VectSet() {
        this(INITIAL_CAPACITY);
    }

    /**
     *
     * @param other
     */
    public VectSet(VectSet other) {
        this.maxJumps = other.maxJumps;
        this.ords = other.ords;
        this.copyOnEdit = true;
        other.copyOnEdit = true;
        this.size = other.size;
    }

    /**
     * Get the number of vectors
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
     * Determine if set contains the vector given
     *
     * @param vect
     * @return
     * @throws NullPointerException if vect was null
     */
    public boolean contains(Vect vect) throws NullPointerException {
        return containsInternal(vect.x, vect.y);
    }
    
    /**
     * Determine if set contains the vector given
     *
     * @param vect
     * @return
     * @throws NullPointerException if vect was null
     */
    public boolean contains(VectBuilder vect) throws NullPointerException {
        return containsInternal(vect.getX(), vect.getY());
    }

    /**
     * Determine if set contains the vector given
     *
     * @param x
     * @param y
     * @return
     * @throws IllegalArgumentException if x or y was infinite or NaN
     */
    public boolean contains(double x, double y) throws IllegalArgumentException {
        Vect.check(x, y);
        return containsInternal(x, y);
    }
    
    boolean containsInternal(double x, double y) {
        int index = hashIndex(x, y, ords);
        for (int s = maxJumps; s-- > 0;) {
            if ((ords[index] == x) && (ords[index + 1] == y)) {
                return true;
            }
            index = (index + 2) % ords.length;
        }
        return false;
    }

    /**
     * Add the vector given to this set
     *
     * @param vect
     * @return this
     * @throws NullPointerException if vect was null
     */
    public VectSet add(Vect vect) throws NullPointerException {
        return addInternal(vect.x, vect.y);
    } 
    
    /**
     * Add the vector given to this set
     *
     * @param vect
     * @return this
     * @throws NullPointerException if vect was null
     */
    public VectSet add(VectBuilder vect) throws NullPointerException {
        return addInternal(vect.getX(), vect.getY());
    }

    /**
     * Add the vector given to this set
     *
     * @param x
     * @param y
     * @return this
     * @throws IllegalArgumentException if x or y was infinite or NaN
     */
    public VectSet add(double x, double y) throws IllegalArgumentException {
        Vect.check(x, y);
        return addInternal(x, y);
    }

    /**
     * Add the vectors given to this set
     *
     * @param vects
     * @return this
     * @throws NullPointerException if vects was null
     */
    public VectSet addAll(VectList vects) throws NullPointerException {
        for (int i = vects.size(); i-- > 0;) {
            addInternal(vects.getX(i), vects.getY(i));
        }
        return this;
    }

    /**
     * Add the vectors given to this set
     *
     * @param vects
     * @return this
     * @throws NullPointerException if vects was null
     */
    public VectSet addAll(VectSet vects) throws NullPointerException {
        if (vects.isEmpty() || (vects == this)) {
            return this;
        } else if (isEmpty()) {
            this.ords = vects.ords;
            this.copyOnEdit = true;
            vects.copyOnEdit = true;
            this.size = vects.size;
            return this;
        } else {
            vects.forEach(new VectSetProcessor() {
                @Override
                public boolean process(double x, double y) {
                    addInternal(x, y);
                    return true;
                }
            });
            return this;
        }
    }

    /**
     * Add the vectors given to this set
     *
     * @param vects
     * @return this
     * @throws NullPointerException if vects or a vector within it was null
     */
    public VectSet addAll(Iterable<Vect> vects) throws NullPointerException {
        for (Vect vect : vects) {
            add(vect);
        }
        return this;
    }

    VectSet addInternal(double x, double y) {
        beforeUpdate();
        int capacity = ords.length;
        while (true) {
            switch (addToOrds(x, y, ords, maxJumps)) {
                case 0:
                    return this; // exists
                case -1:
                    capacity = capacity << 1;
                    double[] newOrds = rehash(ords, maxJumps, capacity);
                    ords = newOrds;
                    break;
                default: // case 1
                    size++;
                    version++;
                    return this;
            }
        }
    }

    /**
     * Remove the vector given from this set
     *
     * @param x
     * @param y
     * @return true if present and removed, false otherwise
     * @throws IllegalArgumentException if x or y was infinite or NaN
     */
    public boolean remove(double x, double y) throws IllegalArgumentException {
        Vect.check(x, y);
        return removeInternal(x, y);
    }

    /**
     * Remove the vector given from this set
     *
     * @param vect
     * @return true if present and removed, false otherwise
     * @throws NullPointerException if vect was null
     */
    public boolean remove(Vect vect) throws NullPointerException {
        return removeInternal(vect.x, vect.y);
    }

    boolean removeInternal(double x, double y) {
        beforeUpdate();
        int index = hashIndex(x, y, ords);
        for (int s = maxJumps; s-- > 0;) {
            if ((ords[index] == x) && (ords[index + 1] == y)) {
                ords[index] = Double.NaN;
                size--;
                return true;
            }
            index = (index + 2) % ords.length;
        }
        return false;
    }

    /**
     * Remove all entries from this set
     */
    public VectSet clear() {
        if (size != 0) {
            if (copyOnEdit) {
                ords = new double[ords.length];
                copyOnEdit = false;
            }
            Arrays.fill(ords, Double.NaN);
            size = 0;
        }
        return this;
    }

    public boolean forEach(VectSetProcessor processor) {
        int index = 0;
        final int storedVersion = version;
        while (true) {
            if (index >= ords.length) {
                return true;
            }
            if (Double.isNaN(ords[index])) {
                index += 2;
            } else if(storedVersion != version){
                throw new ConcurrentModificationException();
            } else if (!processor.process(ords[index++], ords[index++])) {
                return false;
            }
        }
    }

    @Override
    public Iterator<Vect> iterator() {
        return new VectSetIterator();
    }

    /**
     * Add this set to the target list given
     *
     * @param target
     * @return target
     * @throws NullPointerException if target was null
     */
    public VectList toList(VectList target) throws NullPointerException {
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
     * Read a VectSet from to the DataInput given
     *
     * @param in
     * @return a VectSet
     * @throws NullPointerException if in was null
     * @throws IllegalArgumentException if the stream contained infinite or NaN ordinates
     * @throws GeomException if there was an IO error
     */
    public static VectSet read(DataInput in) throws NullPointerException, IllegalArgumentException, GeomException{
        try {
            VectSet ret = new VectSet(in.readInt());
            int size = in.readInt();
            for(int i = 0; i < size; i++){
                ret.add(in.readDouble(), in.readDouble());
            }
            return ret;
        } catch (IOException ex) {
            throw new GeomException("Error reading VectList", ex);
        }
    }
    
    /**
     * Write this VectList to the DataOutput given
     *
     * @param out
     * @throws NullPointerException if out was null
     * @throws GeomException if there was an IO error
     */
    public void write(DataOutput out) throws NullPointerException, GeomException{
        try {
            int i = 0;
            int max = ords.length;
            out.writeInt(max);
            out.writeInt(size);
            while(i < max){
                double x = ords[i++];
                double y = ords[i++];
                if(!Double.isNaN(x)){
                    out.writeDouble(x);
                    out.writeDouble(y);
                }
            }
        } catch (IOException ex) {
            throw new GeomException("Error writing VectList", ex);
        }
    }

    @Override
    public int hashCode() {
        VectList list = toList(new VectList(size));
        list.sort();
        int hash = 3;
        hash = 41 * hash + list.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VectSet) {
            VectSet other = (VectSet) obj;
            if (size == other.size) {
                VectList list = toList(new VectList(size));
                list.sort();
                VectList otherList = toList(new VectList(other.size));
                otherList.sort();
                return list.equals(otherList);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        VectList list = toList(new VectList(size));
        list.sort();
        return list.toString();
    }

    /**
     * Get a string representing this set in the format [x0,y0, x1,y1, ...
     * xn,yn]
     *
     * @param appendable
     * @throws IllegalStateException
     */
    public void toString(Appendable appendable) throws IllegalStateException {
        VectList list = toList(new VectList(size));
        list.sort();
        list.toString(appendable);
    }

    @Override
    public VectSet clone() {
        return new VectSet(this);
    }

    static double[] rehash(double[] ords, int maxJumps, int capacity) {
        double[] newOrds = new double[capacity];
        Arrays.fill(newOrds, Double.NaN);
        int index = ords.length;
        while (index > 0) {
            double y = ords[--index];
            double x = ords[--index];
            if (!Double.isNaN(x)) {
                int added = addToOrds(x, y, newOrds, maxJumps);
                THIS CAN MOST CERTAINLY FAIL - SEE MAP
                //if (added == -1) { // only adds bits to address space, so this can never fail
                //    return null;
                //}
            }
        }
        return newOrds;
    }

    static int hashIndex(double x, double y, double[] ords) {
        int hash = Vect.hashCode(x, y);
        int ret = (hash & ((ords.length >> 1) - 1)) << 1;
        return ret;
    }

    static int addToOrds(double x, double y, double[] ords, int maxJumps) {
        int index = hashIndex(x, y, ords);
        for (int s = maxJumps; s-- > 0;) {
            int next = index + 1;
            if (Double.isNaN(ords[index])) {
                ords[index] = x;
                ords[next] = y;
                return 1;
            } else if ((ords[index] == x) && (ords[next] == y)) {
                return 0;
            }
            index = (index + 2) % ords.length;
        }
        return -1;
    }

    void beforeUpdate() {
        if (copyOnEdit) {
            ords = ords.clone();
            copyOnEdit = false;
        }
    }

    final class VectSetIterator implements Iterator<Vect> {

        final int storedVersion;
        int prevIndex;
        int nextIndex;

        VectSetIterator() {
            storedVersion = version;
            prevIndex = -1;
            nextIndex = nextIndex();
        }

        @Override
        public boolean hasNext() {
            return nextIndex != -1;
        }

        @Override
        public Vect next() throws ConcurrentModificationException {
            if (storedVersion != version) {
                throw new ConcurrentModificationException();
            }
            prevIndex = nextIndex;
            Vect ret = Vect.valueOf(ords[nextIndex++], ords[nextIndex++]);
            nextIndex = nextIndex();
            return ret;
        }

        int nextIndex() {
            for (int i = nextIndex; i < ords.length; i += 2) {
                if (!Double.isNaN(ords[i])) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public void remove() {
            if(!Double.isNaN(ords[prevIndex])){
                ords[prevIndex] = Double.NaN;
                size--;
            }else{
                throw new IllegalStateException("Already Removed");
            }
        }
    }

    public interface VectSetProcessor {

        boolean process(double x, double y);
    }
}
