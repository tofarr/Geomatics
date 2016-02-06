package org.jg;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

/**
 * Node for use in spatial trees and indexes
 *
 * @author tim.ofarrell
 * @param <E>
 */
public final class SpatialNode<E> implements Externalizable, Cloneable {

    static double[] EMPTY_BOUNDS = new double[0];
    static Object[] EMPTY_VALUES = new Object[0];
    SpatialNode<E> a;
    SpatialNode<E> b;

    //private static final int CAPACITY = 16;
    final Rect bounds;
    double[] itemBounds;
    E[] itemValues;
    int size;

    /**
     *
     * @param itemBounds
     * @param itemValues
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public SpatialNode(Rect[] itemBounds, E[] itemValues) throws NullPointerException, IllegalArgumentException {
        if (itemBounds.length != itemValues.length) {
            throw new IllegalArgumentException("Different numbers of arguments : " + itemBounds.length + "->" + itemValues.length);
        }
        this.size = itemBounds.length;
        this.bounds = new Rect();
        this.itemBounds = new double[size << 2];
        this.itemValues = itemValues.clone();
        int j = 0;
        for (int i = 0; i < size; i++) {
            Rect itemBound = itemBounds[i];
            if (!itemBound.isValid()) {
                throw new IllegalArgumentException("Invalid item bounds : " + itemBound);
            }
            this.bounds.union(itemBound);
            this.itemBounds[j++] = itemBound.minX;
            this.itemBounds[j++] = itemBound.minY;
            this.itemBounds[j++] = itemBound.maxX;
            this.itemBounds[j++] = itemBound.maxY;
        }
    }
    
    /**
     *
     * @param itemBounds
     * @param itemValues
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public SpatialNode(double[] itemBounds, E[] itemValues) throws NullPointerException, IllegalArgumentException {
        if (itemBounds.length != (itemValues.length<<2)) {
            throw new IllegalArgumentException("Different numbers of arguments : " + itemBounds.length + "->" + itemValues.length);
        }
        this.size = itemValues.length;
        this.bounds = new Rect();
        this.itemBounds = itemBounds.clone();
        this.itemValues = itemValues.clone();
        int j = 0;
        for(int i = itemBounds.length; i-- > 0;){
            Util.check(itemBounds[i], "Invalid ordinate : {0}");
        }
        for (int i = 0; i < itemBounds.length;) {
            double minX = itemBounds[i++];
            double minY = itemBounds[i++];
            double maxX = itemBounds[i++];
            double maxY = itemBounds[i++];
            this.bounds.union(minX, minY).union(maxY, maxY);
        }
    }

    /**
     *
     * @param a
     * @param b
     * @throws NullPointerException if a is null or b is null
     */
    public SpatialNode(SpatialNode<E> a, SpatialNode<E> b) throws NullPointerException {
        this.bounds = new Rect().union(a.bounds).union(b.bounds);
        this.a = a;
        this.b = b;
        this.size = a.size + b.size;
        SpatialNode<E> n = this;
    }

    public SpatialNode() {
        this.bounds = new Rect();
        this.itemBounds = EMPTY_BOUNDS;
        this.itemValues = (E[]) EMPTY_VALUES;
    }

    SpatialNode(SpatialNode<E> a, SpatialNode<E> b, Rect bounds, double[] itemBounds, E[] itemValues, int size) {
        this.a = a;
        this.b = b;
        this.bounds = bounds;
        this.itemBounds = itemBounds;
        this.itemValues = itemValues;
        this.size = size;
    }

    /**
     *
     * @param target
     * @return
     * @throws NullPointerException
     */
    public Rect getBounds(Rect target) throws NullPointerException {
        return target.set(bounds);
    }

    /**
     * Get the number of entries
     *
     * @return
     */
    public int size() {
        return size;
    }

    /**
     * Get the number of entries which are not disjoint with the region given
     *
     * @param rect
     * @return
     * @throws NullPointerException if rect was null
     */
    public int sizeInteracting(Rect rect) throws NullPointerException {
        if (rect.contains(bounds)) {
            return size;
        } else if (rect.isDisjoint(bounds)) {
            return 0;
        } else if (isBranch()) {
            return a.sizeInteracting(rect) + b.sizeInteracting(rect);
        } else {
            int ret = 0;
            for (int i = 0, max = size << 2; i < max;) {
                if (!Rect.disjoint(itemBounds[i++], itemBounds[i++], itemBounds[i++], itemBounds[i++],
                        rect.minX, rect.minY, rect.maxX, rect.maxY)) {
                    ret++;
                }
            }
            return ret;
        }
    }
    
    /**
     * Get the number of entries which are not disjoint with the region given
     *
     * @param rect
     * @return
     * @throws NullPointerException if rect was null
     */
    public int sizeOverlapping(Rect rect) throws NullPointerException {
        if (rect.contains(bounds)) {
            return size;
        } else if (rect.isDisjoint(bounds)) {
            return 0;
        } else if (isBranch()) {
            return a.sizeOverlapping(rect) + b.sizeOverlapping(rect);
        } else {
            int ret = 0;
            for (int i = 0, max = size << 2; i < max;) {
                if (Rect.overlaps(itemBounds[i++], itemBounds[i++], itemBounds[i++], itemBounds[i++],
                        rect.minX, rect.minY, rect.maxX, rect.maxY)) {
                    ret++;
                }
            }
            return ret;
        }
    }
    
    /**
     * Determine if this node is empty
     *
     * @return
     */
    public boolean isEmpty() {
        return (size == 0);
    }

    /**
     * Determine if the region given overlaps any entries in this tree
     *
     * @param rect
     * @return
     * @throws NullPointerException if rect was null
     */
    public boolean isEmpty(Rect rect) throws NullPointerException {
        if (rect.contains(bounds)) {
            return size == 0;
        } else if (rect.isDisjoint(bounds)) {
            return true;
        } else if (isBranch()) {
            return a.isEmpty(rect) && b.isEmpty(rect);
        } else {
            for (int i = 0, max = size << 2; i < max;) {
                if (Rect.overlaps(itemBounds[i++], itemBounds[i++], itemBounds[i++], itemBounds[i++],
                        rect.minX, rect.minY, rect.maxX, rect.maxY)) {
                    return false;
                }
            }
            return true;
        }
    }
    
    /**
     * Determine if the region given is disjoint all entries in this tree
     *
     * @param rect
     * @return
     * @throws NullPointerException if rect was null
     */
    public boolean isDisjoint(Rect rect) throws NullPointerException {
        if (rect.contains(bounds)) {
            return size == 0;
        } else if (rect.isDisjoint(bounds)) {
            return true;
        } else if (isBranch()) {
            return a.isDisjoint(rect) && b.isDisjoint(rect);
        } else {
            for (int i = 0, max = size << 2; i < max;) {
                if (!Rect.disjoint(itemBounds[i++], itemBounds[i++], itemBounds[i++], itemBounds[i++],
                        rect.minX, rect.minY, rect.maxX, rect.maxY)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Process all entries in this node
     * @param processor
     * @return
     * @throws NullPointerException if processor was null
     */
    public boolean get(NodeProcessor<E> processor) throws NullPointerException {
        if (isBranch()) {
            return a.get(processor) && b.get(processor);
        } else {
            for (int i = 0; i < size; i++) {
                if (!processor.process(this, i)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Get any entries not disjoint from the rect given
     * @param rect
     * @param processor
     * @return false if processor returned false, true otherwise
     * @throws NullPointerException if rect or processor was null
     */
    public boolean getInteracting(Rect rect, NodeProcessor<E> processor) throws NullPointerException{
        if (rect.contains(bounds)) {
            return get(processor);
        } else if (rect.isDisjoint(bounds)) {
            return true;
        } else if (isBranch()) {
            return a.getInteracting(rect, processor) && b.getInteracting(rect, processor);
        } else {
            for (int i = 0, j = 0, max = size << 2; i < size; i++) {
                if (!Rect.disjoint(itemBounds[j++], itemBounds[j++], itemBounds[j++], itemBounds[j++],
                        rect.minX, rect.minY, rect.maxX, rect.maxY)) {
                    if (!processor.process(this, i)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /**
     * Get any entries overlapping from the rect given
     * @param rect
     * @param processor
     * @return false if processor returned false, true otherwise
     * @throws NullPointerException if rect or processor was null
     */
    public boolean getOverlapping(Rect rect, NodeProcessor<E> processor) throws NullPointerException {
        if (rect.contains(bounds)) {
            return get(processor);
        } else if (rect.isDisjoint(bounds)) {
            return true;
        } else if (isBranch()) {
            return a.getOverlapping(rect, processor) && b.getOverlapping(rect, processor);
        } else {
            for (int i = 0, j = 0, max = size << 2; i < size; i++) {
                if (Rect.overlaps(itemBounds[j++], itemBounds[j++], itemBounds[j++], itemBounds[j++],
                        rect.minX, rect.minY, rect.maxX, rect.maxY)) {
                    if (!processor.process(this, i)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /**
     * Determine whether this collection contains the entry given
     *
     * @param rect
     * @param value
     * @return
     */
    public boolean contains(Rect rect, E value) {
        if (!bounds.contains(rect)) {
            return false;
        } else if (isBranch()) {
            return a.contains(rect, value) || b.contains(rect, value);
        } else {
            for (int i = 0, j = 0, max = size << 2; i < size; i++, j += 4) {
                if ((itemBounds[j] == rect.minX)
                        && (itemBounds[j + 1] == rect.minY)
                        && (itemBounds[j + 2] == rect.maxX)
                        && (itemBounds[j + 3] == rect.maxY)
                        && Objects.equals(itemValues[i], value)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Determine whether this collection contains the value given
     *
     * @param value
     * @return
     */
    public boolean containsValue(E value) {
        if (isBranch()) {
            return a.containsValue(value) || b.containsValue(value);
        }
        for (int i = size; i-- > 0;) {
            if (Objects.equals(itemValues[i], value)) {
                return true;
            }
        }
        return false;
    }

    public Rect getItemBounds(int index, Rect target) throws NullPointerException, IndexOutOfBoundsException {
        if (index >= size) {
            throw new IndexOutOfBoundsException(index + " is outside range [0," + size + "]");
        }
        index <<= 2;
        return target.set(itemBounds[index++], itemBounds[index++], itemBounds[index++], itemBounds[index]);
    }

    public E getItemValue(int index) throws IndexOutOfBoundsException {
        if (index >= size) {
            throw new IndexOutOfBoundsException(index + " is outside range [0," + size + "]");
        }
        return itemValues[index];
    }

    public SpatialNode<E> getA() {
        return a;
    }

    public SpatialNode<E> getB() {
        return b;
    }

    public boolean isBranch() {
        return (a != null);
    }

    public boolean isLeaf() {
        return (itemBounds != null);
    }
    
    public int getDepth(){
        if(isBranch()){
            return Math.max(a.getDepth(), b.getDepth()) + 1;
        }else{
            return 1;
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        if (isBranch()) {
            out.writeBoolean(true);
            a.writeExternal(out);
            b.writeExternal(out);
        } else {
            out.writeBoolean(false);
            out.writeInt(size);
            for (int i = 0, max = size << 2; i < max; i++) {
                out.writeDouble(itemBounds[i]);
            }
            for (int i = 0; i < size; i++) {
                out.writeObject(itemValues[i]);
            }
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        if (in.readBoolean()) {
            a = new SpatialNode();
            b = new SpatialNode();
            a.readExternal(in);
            b.readExternal(in);
            bounds.reset().union(a.bounds).union(b.bounds);
            size = a.size + b.size;
            itemBounds = null;
            itemValues = null;
        } else {
            size = in.readInt();
            itemBounds = new double[size << 2];
            itemValues = (E[]) new Object[size];
            for (int i = 0; i < itemBounds.length; i++) {
                itemBounds[i] = in.readDouble();
            }
            for (int i = 0; i < size; i++) {
                itemValues[i] = (E) in.readObject();
            }
            bounds.reset().unionAll(itemBounds, 0, itemBounds.length);
            a = b = null;
        }
    }

    @Override
    public int hashCode() {
        HashCodeProcessor processor = new HashCodeProcessor();
        get(processor);
        return processor.hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SpatialNode) {
            SpatialNode tree = (SpatialNode) obj;
            if (bounds.equals(tree.bounds) && (size == tree.size)) {
                if (isBranch()) {
                    if (tree.isBranch()) {
                        return a.equals(tree.a) && b.equals(tree.b);
                    }
                } else if (!tree.isBranch()) {
                    for (int i = size << 2; i-- > 0;) {
                        if (itemBounds[i] != tree.itemBounds[i]) {
                            return false;
                        }
                    }
                    for (int i = size; i-- > 0;) {
                        if (!Objects.equals(itemValues[i], tree.itemValues[i])) {
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
        StringBuilder str = new StringBuilder();
        toString(str);
        return str.toString();
    }

    public void toString(Appendable appendable) throws IllegalStateException {
        try {
            appendable.append('{');
            if (isBranch()) {
                appendable.append("a:");
                a.toString(appendable);
                appendable.append(",b:");
                b.toString(appendable);
            } else {
                appendable.append("itemBounds:[");
                int max = size << 2;
                for (int i = 0; i < max; i++) {
                    if (i != 0) {
                        appendable.append(',');
                    }
                    appendable.append(Util.ordToStr(itemBounds[i]));
                }
                appendable.append("],itemValues:[");
                for (int i = 0; i < size; i++) {
                    if (i != 0) {
                        appendable.append(',');
                    }
                    appendable.append(Objects.toString(itemValues[i]));
                }
                appendable.append(']');
            }
            appendable.append('}');
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public SpatialNode<E> clone() {
        if (isBranch()) {
            return new SpatialNode<>(a.clone(), b.clone());
        } else {
            return new SpatialNode<>(null, null, new Rect(bounds), itemBounds.clone(), itemValues.clone(), size);
        }
    }

    public interface NodeProcessor<E> {

        public boolean process(SpatialNode<E> leaf, int index);
    }

    static class HashCodeProcessor<E> implements NodeProcessor<E> {

        int hash = 5;

        @Override
        public boolean process(SpatialNode<E> leaf, int index) {
            int j = index << 2;
            hash = 79 * hash + Util.hash(leaf.itemBounds[j++]);
            hash = 79 * hash + Util.hash(leaf.itemBounds[j++]);
            hash = 79 * hash + Util.hash(leaf.itemBounds[j++]);
            hash = 79 * hash + Util.hash(leaf.itemBounds[j]);
            hash = 79 * hash + Objects.hashCode(leaf.itemValues[index]);
            return true;
        }
    }

}
