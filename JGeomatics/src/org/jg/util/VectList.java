package org.jg.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import org.jg.geom.GeomException;
import org.jg.geom.Line;
import org.jg.geom.Rect;
import org.jg.geom.RectBuilder;
import org.jg.geom.Vect;
import org.jg.geom.VectBuilder;

/**
 * Packed list of vectors
 * @author tofar_000
 */
public final class VectList implements Serializable, Cloneable, Iterable<Vect>, Comparable<VectList> {

    static final int DEFAULT_INITIAL_CAPACITY = 64;
    private double[] ords;
    private int size;
    private Rect cachedRect;
    private boolean copyOnEdit;

    /**
     * Create a new VectList with the DEFAULT initial capacity
     */
    public VectList() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Create a new VectList populated with the ordinates given
     *
     * @param ords ordinates
     * @throws NullPointerException if ords was null
     * @throws IllegalArgumentException if an ordinate was n
     */
    public VectList(double... ords) throws NullPointerException, IllegalArgumentException {
        if ((ords.length & 1) == 1) {
            throw new IllegalArgumentException("Number of ordinates must be even : " + ords.length);
        }
        for (int i = 0; i < ords.length; i++) {
            Vect.check(ords[i], "Invalid ordinate : {0}");
        }
        this.ords = (ords.length == 0) ? new double[DEFAULT_INITIAL_CAPACITY] : ords.clone();
        this.size = ords.length >> 1;
    }

    /**
     * Create a new VectList with the initial capacity given
     *
     * @param initialCapacity
     * @throws IllegalArgumentException if the initial capacity was <= 0
     */
    public VectList(int initialCapacity) throws IllegalArgumentException {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Invalid capacity : " + initialCapacity);
        }
        this.ords = new double[initialCapacity << 1];
    }

    VectList(double[] ords, int size, boolean copyOnEdit) {
        this.ords = ords;
        this.size = size;
        this.copyOnEdit = copyOnEdit;
    }

    /**
     * Get the number of vectors in this list
     *
     * @return
     */
    public int size() {
        return size;
    }

    /**
     * Determine whether there are any vectors stored in this object
     *
     * @return
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Check index >= 0 && index < size
     *
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    private void checkIndex(int index) throws IndexOutOfBoundsException {
        if ((index < 0) || (index >= size)) {
            throw new IndexOutOfBoundsException("Index " + index + " is outside bounds [0," + size + ']');
        }
    }

    /**
     * Check index >= 0 && index <= size
     *
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    private void checkLength(int len) throws IndexOutOfBoundsException {
        if ((len < 0) || (len > size)) {
            throw new IndexOutOfBoundsException("Index " + len + " is outside bounds [0," + size + ']');
        }
    }

    /**
     * Get the vector at the index given
     * @param index
     * @return vector
     * @throws IndexOutOfBoundsException if index was out of bounds
     */
    public Vect getVect(int index) throws IndexOutOfBoundsException {
        checkIndex(index);
        int ordIndex = index << 1;
        return Vect.valueOf(ords[ordIndex], ords[++ordIndex]);
    }

    /**
     * Get the vector at the index given
     *
     * @param index
     * @param target target vector
     * @return target
     * @throws IndexOutOfBoundsException if index is out of bounds
     * @throws NullPointerException if target was null
     */
    public VectBuilder getVect(int index, VectBuilder target) throws IndexOutOfBoundsException, NullPointerException {
        checkIndex(index);
        int ordIndex = 0 + (index << 1);
        target.set(ords[ordIndex], ords[++ordIndex]);
        return target;
    }

    /**
     * Get the line at the index given
     *
     * @param index
     * @return target
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public Line getLine(int index) throws IndexOutOfBoundsException {
        if ((index < 0) || ((index + 1) >= size)) {
            throw new IndexOutOfBoundsException(index + " is not within range [0," + (size - 1) + "]");
        }
        index <<= 1;
        return Line.valueOf(ords[index++], ords[index++], ords[index++], ords[index]);
    }

    /**
     * Get x value at the index given
     *
     * @param index
     * @return
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public double getX(int index) throws IndexOutOfBoundsException {
        checkIndex(index);
        int ordIndex = (index << 1);
        return ords[ordIndex];
    }

    /**
     * Get y value at the index given
     *
     * @param index
     * @return
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public double getY(int index) {
        checkIndex(index);
        int ordIndex = (index << 1) | 1;
        return ords[ordIndex];
    }

    /**
     * Get the bounds of all vectors in this list
     *
     * @return bounds
     */
    public Rect getBounds() {
        Rect ret = cachedRect;
        if (ret == null) {
            RectBuilder builder = new RectBuilder();
            builder.addAll(ords, 0, size);
            ret = builder.build();
            cachedRect = ret;
        }
        return ret;
    }

    /**
     * Get the first index of the vector given in this list, after the index
     * given (inclusive)
     *
     * @param x
     * @param y
     * @param fromIndex
     * @return the first index, or -1 if not found
     * @throws IndexOutOfBoundsException if fromIndex was out of bounds
     * @throws IllegalArgumentException if an ordinate was infinite or NaN
     */
    public int indexOf(double x, double y, int fromIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        checkLength(fromIndex);
        fromIndex = (fromIndex << 1);
        int endIndex = (size << 1);
        for (int i = fromIndex; i < endIndex; i+=2) {
            if ((ords[i] == x) && (ords[i+1] == y)) {
                return i >> 1;
            }
        }
        return -1;
    }

    /**
     * Get the first index of the vector given in this list, after the index
     * given (inclusive)
     *
     * @param vect
     * @param fromIndex
     * @return the first index, or -1 if not found
     * @throws IndexOutOfBoundsException if fromIndex was out of bounds
     * @throws NullPointerException if vect was null
     */
    public int indexOf(Vect vect, int fromIndex) throws IndexOutOfBoundsException, NullPointerException {
        return indexOf(vect.x, vect.y, fromIndex);
    }

    /**
     * Get the last index of the vector given in this list, before the index
     * given (exclusive)
     *
     * @param x
     * @param y
     * @param toIndex
     * @return the first index, or -1 if not found
     * @throws IndexOutOfBoundsException if from index was out of bounds
     * @throws IllegalArgumentException if an ordinate was infinite of NaN
     */
    public int lastIndexOf(double x, double y, int toIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        checkLength(toIndex);
        toIndex = (toIndex << 1);
        for (int i = toIndex; i > 0;) {
            i--;
            int j = i-1;
            if ((ords[i] == y) && (ords[j] == x)) {
                return j >> 1;
            }
            i = j;
        }
        return -1;
    }

    /**
     * Get the last index of the vector given in this list, before the index
     * given (exclusive)
     *
     * @param vect
     * @param toIndex
     * @return the first index, or -1 if not found
     * @throws IndexOutOfBoundsException if from index was out of bounds
     * @throws NullPointerException if vect was null
     */
    public int lastIndexOf(Vect vect, int toIndex) throws IndexOutOfBoundsException, NullPointerException {
        return lastIndexOf(vect.x, vect.y, toIndex);
    }

    /**
     * Copy ordinates from this list to the destination array given
     *
     * @param srcIndex index within this of the first vector from which to copy
     * @param dst destination array
     * @param dstIndex offset within destination array
     * @param numVects number of vectors to copy
     * @throws IndexOutOfBoundsException if an index was out of bounds
     * @throws NullPointerException if dst was null
     */
    public void getOrds(int srcIndex, double[] dst, int dstIndex, int numVects) throws IndexOutOfBoundsException, NullPointerException {
        checkIndex(srcIndex);
        checkLength(srcIndex + numVects);
        srcIndex = (srcIndex << 1);
        int numOrds = numVects << 1;
        System.arraycopy(ords, srcIndex, dst, dstIndex, numOrds);
    }

    /**
     * Transform this VectList using the matrix given
     *
     * @param transform
     * @throws NullPointerException if matrix or target was null
     */
    public void transform(Transform transform) throws NullPointerException {
        if (transform.mode != Transform.NO_OP) {
            ensureSize(size);
            transform.transformOrds(ords, 0, ords, 0, size);
            cachedRect = null;
        }
    }

    /**
     * Replace the ords buffer with the one of the size given
     *
     * @oaran newSize,
     */
    void replaceBuffer(int newSize) {
        double[] newOrds = new double[newSize];
        System.arraycopy(ords, 0, newOrds, 0, size << 1);
        this.ords = newOrds;
    }

    /**
     * Create iterator over items in the list modifications during iteration are
     * permitted - insertions may cause iterators to lose place
     *
     * @return iterator
     */
    @Override
    public Iterator<Vect> iterator() {
        return new VectListIterator(0);
    }

    /**
     * Create iterator over items in the list modifications during iteration are
     * permitted - insertions may cause iterators to lose place
     *
     * @param nextIndex the index of the vector focused on after a call to next
     * @return iterator
     */
    public Iterator<Vect> iterator(int nextIndex) {
        checkIndex(nextIndex);
        return new VectListIterator(nextIndex);
    }

    /**
     * Add a vector to the end of this list
     *
     * @param vect
     * @return this
     * @throws NullPointerException if vect was null
     */
    public VectList add(Vect vect) throws NullPointerException {
        return addInternal(vect.x, vect.y);
    }

    /**
     * Add a vector to the end of this list
     *
     * @param vect
     * @return this
     * @throws NullPointerException if vect was null
     */
    public VectList add(VectBuilder vect) throws NullPointerException {
        return addInternal(vect.getX(), vect.getY());
    }

    /**
     * Add a vector to the end of this list
     *
     * @param x
     * @param y
     * @return this
     * @throws IllegalArgumentException if x or y was infinite or NaN
     */
    public VectList add(double x, double y) throws IllegalArgumentException {
        Vect.check(x, y);
        return addInternal(x, y);
    }

    /**
     * Add a vector to the end of this list
     *
     * @param vects
     * @param index
     * @return this
     * @throws IndexOutOfBoundsException if index was out of bounds in vects
     */
    public VectList add(VectList vects, int index) throws IndexOutOfBoundsException {
        vects.checkIndex(index);
        index <<= 1;
        return addInternal(vects.ords[index], vects.ords[++index]);
    }

    VectList addInternal(double x, double y) {
        ensureSize(size + 1);
        int ordIndex = size << 1;
        ords[ordIndex] = x;
        ords[++ordIndex] = y;
        size++;
        cachedRect = null;
        return this;
    }

    /**
     * Remove the vector at the index given
     *
     * @param index
     * @return this
     * @throws IndexOutOfBoundsException if index was out of bounds
     */
    public VectList remove(int index) throws IndexOutOfBoundsException {
        checkIndex(index);
        ensureSize(size - 1);
        int ordIndex = (index << 1);
        System.arraycopy(ords, ordIndex + 2, ords, ordIndex, (size << 1) - (ordIndex + 2));
        size--;
        cachedRect = null;
        return this;
    }

    /**
     * Remove the range of vectors at the index given
     *
     * @param index
     * @param length
     * @return this
     * @throws IndexOutOfBoundsException if index was out of bounds
     */
    public VectList removeAll(int index, int length) throws IndexOutOfBoundsException {
        if(length < 0){
            index += length;
            length = -length;
        }
        checkIndex(index);
        checkLength(index + length);
        ensureSize(size - length);
        int fromIndex = (index+length) << 1;
        int toIndex = (index << 1);
        System.arraycopy(ords, fromIndex, ords, toIndex, (size << 1) - fromIndex);
        size -= length;
        cachedRect = null;
        return this;
    }
    
    /**
     * Set the vector at the index given to the value given
     *
     * @param index
     * @param vect
     * @return this
     * @throws IndexOutOfBoundsException if index < 0 or index >= size
     * @throws NullPointerException if vect was null
     */
    public VectList set(int index, Vect vect) throws IndexOutOfBoundsException, NullPointerException {
        checkIndex(index);
        ensureSize(size);
        int ordIndex = (index << 1);
        ords[ordIndex] = vect.getX();
        ords[++ordIndex] = vect.getY();
        cachedRect = null;
        return this;
    }

    /**
     * Insert the vector given at the index given, increasing the index of later
     * vectors by 1
     *
     * @param index
     * @param vect
     * @return this
     * @throws IndexOutOfBoundsException if index < 0 or index > size
     * @throws NullPointerException if vect was null
     */
    public VectList insert(int index, Vect vect) throws IndexOutOfBoundsException, NullPointerException {
        return insertInternal(index, vect.x, vect.y);
    }

    /**
     * Insert the vector given at the index given, increasing the index of later
     * vectors by 1
     *
     * @param index
     * @param x
     * @param y
     * @return this
     * @throws IndexOutOfBoundsException if index < 0 or index > size
     * @throws IllegalArgumentException if x or y was infinite or NaN
     */
    public VectList insert(int index, double x, double y) throws IndexOutOfBoundsException, IllegalArgumentException {
        Vect.check(x, y);
        return insertInternal(index, x, y);
    }

    VectList insertInternal(int index, double x, double y) throws IndexOutOfBoundsException {
        if (index == size) {
            return add(x, y);
        }
        ensureSize(size + 1);
        int ordIndex = (index << 1);
        System.arraycopy(ords, ordIndex, ords, ordIndex + 2, (size << 1) - ordIndex);
        ords[ordIndex] = x;
        ords[++ordIndex] = y;
        size++;
        cachedRect = null;
        return this;
    }

    /**
     * Remove all vectors
     *
     * @return this
     */
    public VectList clear() {
        size = 0;
        cachedRect = null;
        return this;
    }

    /**
     * Add all the vectors given to the end of this list
     *
     * @param vects
     * @return this
     * @throws NullPointerException if vects was null or a vector was null
     */
    public VectList addAll(Iterable<Vect> vects) throws NullPointerException {
        if (vects instanceof VectList) {
            return addAll((VectList) vects);
        }
        int oldSize = size;
        try {
            for (Vect vect : vects) {
                add(vect);
            }
        } catch (NullPointerException ex) {
            size = oldSize; // reset size if there was an error
            throw ex;
        }
        return this;
    }

    /**
     * Add all the vectors given to this list
     *
     * @param vects
     * @return this
     * @throws NullPointerException if vects was null or a vector was null
     */
    public VectList addAll(Vect... vects) throws NullPointerException {
        int oldSize = size;
        try {
            for (Vect vect : vects) {
                add(vect);
            }
        } catch (NullPointerException ex) {
            size = oldSize; // reset size if there was an error
            throw ex;
        }
        return this;
    }

    /**
     * Add all the vectors given to the end of this list
     *
     * @param vects
     * @return this
     * @throws NullPointerException if vects was null
     */
    public VectList addAll(VectList vects) throws NullPointerException {
        if (size == 0) {
            ords = vects.ords;
            size = vects.size;
            copyOnEdit = true;
            cachedRect = vects.cachedRect;
            vects.copyOnEdit = true;
            return this;
        }
        return addAllInternal(vects.ords, 0, vects.size);
    }

    /**
     * Add the range of vectors given to this list
     *
     * @param vects
     * @param index index within array
     * @param numVects
     * @return this
     * @throws NullPointerException if vects was null
     * @throws IndexOutOfBoundsException if an index was out of bounds
     */
    public VectList addAll(VectList vects, int index, int numVects) throws NullPointerException, IndexOutOfBoundsException {
        vects.checkIndex(index);
        vects.checkLength(numVects);
        return addAllInternal(vects.ords, index << 1, numVects);
    }

    /**
     * Add all the vectors given to this list
     *
     * @param ords
     * @param startIndex index of first ordinate in ords
     * @param numVects number of vectors to add
     * @return this.
     * @throws NullPointerException if ords was null
     * @throws IllegalArgumentException if an ordinate was infinite or NaN
     * @throws IndexOutOfBoundsException if an index was out of bounds
     */
    public VectList addAll(double[] ords, int startIndex, int numVects) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        for (int i = startIndex + (numVects * 2); --i >= startIndex;) {
            Vect.check(ords[i], "Invalid ordinate {0}");
        }
        if (numVects < 0) {
            throw new IllegalArgumentException("Number of vects must be positive : " + numVects);
        }
        return addAllInternal(ords, startIndex, numVects);
    }

    VectList addAllInternal(double[] ords, int startIndex, int numVects) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        int numOrds = numVects << 1;
        ensureSize(size + numVects);
        int dstIndex = (this.size << 1);
        System.arraycopy(ords, startIndex, this.ords, dstIndex, numOrds);
        size += numVects;
        cachedRect = null;
        return this;
    }

    /**
     *
     * @param ords
     * @return this.
     * @throws IllegalArgumentException if number of ordinates was odd or an
     * ordinate was infinite or NaN
     * @throws NullPointerException if ords was null
     */
    public VectList addAll(double... ords) throws IllegalArgumentException, NullPointerException {
        if ((ords.length & 1) == 1) {
            throw new IllegalArgumentException("Invalid number of ordinates : " + ords.length);
        }
        return addAll(ords, 0, ords.length >> 1);
    }

    /**
     * Swap the vectors at the ordinates given
     *
     * @param i
     * @param j
     * @return this
     */
    public VectList swap(int i, int j) {
        checkIndex(i);
        checkIndex(j);
        ensureSize(size);
        i <<= 1;
        j <<= 1;
        double tmp = ords[i];
        ords[i] = ords[j];
        ords[j] = tmp;
        i++;
        j++;
        tmp = ords[i];
        ords[i] = ords[j];
        ords[j] = tmp;
        return this;
    }

    /**
     * Revverse the order of vectors in this list
     *
     */
    public void reverse() {
        ensureSize(size);
        int min = 0;
        int max = size - 1;
        while (min < max) {
            swap(min++, max--);
        }
    }

    /**
     * Ensure that the vect list can store at least the number of vertices given
     *
     * @param required
     * @return true if a buffer copy occured, false otherwise
     */
    boolean ensureSize(int required) {
        required = Math.abs(required << 1);
        int capacity = ords.length;
        int newCapacity = capacity;
        while (newCapacity < required) {
            newCapacity = newCapacity * 3 / 2 + 1;
        }
        if ((newCapacity != capacity) || copyOnEdit) {
            if ((newCapacity & 1) == 1) {
                newCapacity++;
            }
            replaceBuffer(newCapacity);
            copyOnEdit = false;
            return true;
        }
        return false;
    }

    /**
     * Sort the vectors in this list by x and then y
     *
     * @return this
     */
    public VectList sort() {
        if (size > 1) {
            sort(this, 0, size);
        }
        return this;
    }

    static void sort(VectList vects, int min, int max) {
        if (min < max) {
            //int p = partition(vects, min, max, a, b);
            //int pivot = 

            int a = (min + max) / 2;
            double ax = vects.getX(a);
            double ay = vects.getY(a);
            int b = a + 1;

            //check items before partition
            for (int i = min; i < a; i++) {
                double ix = vects.getX(i);
                double iy = vects.getY(i);
                if (Vect.compare(ix, iy, ax, ay) >= 0) {
                    if ((a - 1) != i) {
                        vects.swap((a - 1), a);
                    }
                    vects.swap(i, a);
                    i--;
                    a--;
                }
            }

            //Check items after partition
            for (int i = b; i < max; i++) {
                double ix = vects.getX(i);
                double iy = vects.getY(i);
                if (Vect.compare(ax, ay, ix, iy) >= 0) {
                    if ((a + 1) != i) {
                        vects.swap(a, (a + 1));
                    }
                    vects.swap(a, i);
                    a++;
                }
            }

            sort(vects, min, a);
            sort(vects, a + 1, max);
        }
    }

    /**
     * Determine whether this list is ordered such that the start is less than
     * the end
     *
     * @return
     */
    public boolean isOrdered() {
        int min = 0;
        int max = size << 1;
        while (min < max) {
            double ax = ords[min++];
            double ay = ords[min++];
            double by = ords[--max];
            double bx = ords[--max];
            int c = Vect.compare(ax, ay, bx, by);
            if (c < 0) {
                return true;
            }else if(c > 0){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VectList) {
            VectList vectList = (VectList) obj;
            return equalsRange(vectList.ords, 0, vectList.size);
        } else {
            return false;
        }
    }

    /**
     * Determine if this path equals the path given. Paths are equal if they
     * have all the same points in the same order
     *
     * @param ords
     * @param startIndex
     * @param size
     * @return
     */
    public boolean equalsRange(double[] ords, int startIndex, int size) throws NullPointerException, IndexOutOfBoundsException {
        if (size != this.size) {
            return false;
        }
        int numOrds = size << 1;
        for (int i = 0; i < numOrds; i++) {
            if (ords[startIndex + i] != this.ords[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        int endIndex = (size << 1);
        for (int i = 0; i < endIndex; i++) {
            hash = 79 * hash + Vect.hash(ords[i]);
        }
        return hash;
    }

    @Override
    public int compareTo(VectList o) {
        //First we test to determine if one list has a point at an index which is less
        //than the other lists corresponding point
        int max = Math.min(size, o.size) << 1;
        for(int i = 0; i < max; i++){
            if(ords[i] < o.ords[i]){
                return -1;
            }else if(ords[i] > o.ords[i]){
                return 1;
            }
        }
        //Otherwise we return the shortest list
        if(size < o.size){
            return -1;
        }else if(size > o.size){
            return 1;
        }else{
            return 0;
        }
    }
    
    /**
     * Read a VectList from to the DataInput given
     *
     * @param in
     * @return a VectList
     * @throws NullPointerException if in was null
     * @throws IllegalArgumentException if the stream contained infinite or NaN ordinates
     * @throws GeomException if there was an IO error
     */
    public static VectList read(DataInput in) throws NullPointerException, IllegalArgumentException, GeomException{
        try {
            int size = in.readInt();
            int max = size << 1;
            double[] ords = new double[max];
            for(int i = 0; i < max; i++){
                double d = in.readDouble();
                Vect.check(d, "Invalid ordinate {0}");
                ords[i] = d;
            }
            return new VectList(ords, size, false);
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
            out.writeInt(size);
            for(int i = 0, max = size << 1; i < max; i++){
                out.writeDouble(ords[i]);
            }
        } catch (IOException ex) {
            throw new GeomException("Error writing VectList", ex);
        }
    }
    
    /**
     * Get an RTree containing all lines in this list
     * @return
     */
    public RTree<Line> toLineIndex(){
        int s = size - 1;
        Rect[] bounds = new Rect[s];
        Line[] lines = new Line[s];
        for(int i = 0; i < s; i++){
            Line line = getLine(i);
            bounds[i] = line.getBounds();
            lines[i] = line;
        }
        return new RTree<Line>(bounds, lines);
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * Convert this list to a string
     * @param summarize true if shortened format may be used, false otherwise
     * @return string representation of this list
     */
    public String toString(boolean summarize) {
        StringBuilder str = new StringBuilder();
        if (summarize && (size > 50)) { //Large list - just print summary
            str.append("{size:").append(size).append(", bounds:");
            Rect.toString(getBounds(), str);
            str.append("}");
        } else {
            toString(str);
        }
        return str.toString();
    }
    
    /**
     * Get a string representing this path in the format [x0,y0, x1,y1, ...
     * xn,yn]
     *
     * @param appendable
     * @throws GeomException
     */
    public void toString(Appendable appendable) throws GeomException {
        try {
            appendable.append('[');
            boolean comma = false;
            int endIndex = (size << 1);
            for (int i = 0; i < endIndex;) {
                if (comma) {
                    appendable.append(", ");
                } else {
                    comma = true;
                }
                appendable.append(Vect.ordToStr(ords[i++])).append(',').append(Vect.ordToStr(ords[i++]));
            }
            appendable.append(']');
        } catch (IOException ex) {
            throw new GeomException("Error writing", ex);
        }
    }

    @Override
    public VectList clone() {
        VectList ret = new VectList(ords, size, true);
        copyOnEdit = true;
        return ret;
    }

    class VectListIterator implements Iterator<Vect> {

        int index;

        VectListIterator(int index) {
            this.index = index;
        }

        @Override
        public boolean hasNext() {
            return (index < size);
        }

        @Override
        public Vect next() {
            return getVect(index++);
        }

        @Override
        public void remove() {
            VectList.this.remove(index - 1);
            index--;
        }

    }
}
