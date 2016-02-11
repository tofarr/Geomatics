package org.jg.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import org.jg.geom.Rect;
import org.jg.geom.RectBuilder;

/**
 * Node for use in spatial trees and indexes. A node may be a branch with 2
 * child nodes, or a leaf containing a number of bounds and values.
 *
 * @author tim.ofarrell
 * @param <E>
 */
public final class SpatialNode<E> implements Externalizable, Cloneable {

    static Rect[] EMPTY_BOUNDS = new Rect[0];
    static Object[] EMPTY_VALUES = new Object[0];
    SpatialNode<E> a;
    SpatialNode<E> b;

    //private static final int CAPACITY = 16;
    final RectBuilder bounds;
    Rect[] itemBounds;
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
        this.bounds = new RectBuilder();
        this.itemBounds = itemBounds.clone();
        this.itemValues = itemValues.clone();
        for (int i = 0; i < size; i++) {
            this.bounds.add(itemBounds[i]);
        }
    }

    /**
     *
     * @param a
     * @param b
     * @throws NullPointerException if a is null or b is null
     */
    public SpatialNode(SpatialNode<E> a, SpatialNode<E> b) throws NullPointerException {
        this.bounds = new RectBuilder().add(a.bounds).add(b.bounds);
        this.a = a;
        this.b = b;
        this.size = a.size + b.size;
        SpatialNode<E> n = this;
    }

    /**
     * Create a new empty spatial node
     */
    public SpatialNode() {
        this.bounds = new RectBuilder();
        this.itemBounds = EMPTY_BOUNDS;
        this.itemValues = (E[]) EMPTY_VALUES;
    }

    SpatialNode(SpatialNode<E> a, SpatialNode<E> b, RectBuilder bounds, Rect[] itemBounds, E[] itemValues, int size) {
        this.a = a;
        this.b = b;
        this.bounds = bounds;
        this.itemBounds = itemBounds;
        this.itemValues = itemValues;
        this.size = size;
    }

    /**
     * Get the bounds for this node
     *
     * @return
     */
    public Rect getBounds() {
        return bounds.build();
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
     *
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
     *
     * @param rect
     * @param processor
     * @return false if processor returned false, true otherwise
     * @throws NullPointerException if rect or processor was null
     */
    public boolean getInteracting(Rect rect, NodeProcessor<E> processor) throws NullPointerException {
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
     *
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
     * @throws NullPointerException if rect was null
     */
    public boolean contains(Rect rect, E value) throws NullPointerException {
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

    /**
     * Get the bounds of the item at the index given in this leaf
     *
     * @param index
     * @param target
     * @return target
     * @throws IllegalStateException if not a leaf node
     * @throws NullPointerException
     * @throws IndexOutOfBoundsException
     */
    public Rect getItemBounds(int index, Rect target) throws NullPointerException, IndexOutOfBoundsException {
        if (isBranch()) {
            throw new IllegalStateException("Not a leaf node!");
        }
        if (index >= size) {
            throw new IndexOutOfBoundsException(index + " is outside range [0," + size + "]");
        }
        index <<= 2;
        return target.set(itemBounds[index++], itemBounds[index++], itemBounds[index++], itemBounds[index]);
    }

    /**
     * Get the value of the item at the index given in this leaf
     *
     * @param index
     * @return target value
     * @throws IllegalStateException if not a leaf node
     * @throws NullPointerException
     * @throws IndexOutOfBoundsException
     */
    public E getItemValue(int index) throws IndexOutOfBoundsException {
        if (isBranch()) {
            throw new IllegalStateException("Not a leaf node!");
        }
        if (index >= size) {
            throw new IndexOutOfBoundsException(index + " is outside range [0," + size + "]");
        }
        return itemValues[index];
    }

    /**
     * Get the child a of this node
     *
     * @return child, or null if not a branch
     */
    public SpatialNode<E> getA() {
        return a;
    }

    /**
     * Get the child b of this node
     *
     * @return child, or null if not a branch
     */
    public SpatialNode<E> getB() {
        return b;
    }

    /**
     * Determine if this node is a branch. (Has child nodes A and B, and does
     * not directly contain items).
     *
     * @return
     */
    public boolean isBranch() {
        return (a != null);
    }

    /**
     * Determine if this node is a leaf. (Directly contains items, and does not
     * have child nodes A and B).
     *
     * @return
     */
    public boolean isLeaf() {
        return (itemBounds != null);
    }

    /**
     * Get the depth of this node - the max number of children one must go
     * through before reacing a leaf node - Typically for the same entries in a
     * node, the lower this number the more balanced the tree.
     *
     * @return
     */
    public int getDepth() {
        if (isBranch()) {
            return Math.max(a.getDepth(), b.getDepth()) + 1;
        } else {
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

    /**
     * Convert this spatial node to a string and add it to the appendable given.
     * Leaves are in the format {itemBounds:[...],itemValues:[...]} and branches
     * are in the format {a:{...},b:{...})
     *
     * @param appendable
     * @throws IllegalStateException if there was an output error
     * @throws NullPointerException if appendable was null
     */
    public void toString(Appendable appendable) throws IllegalStateException, NullPointerException {
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

    /**
     * Processor for nodes - a callback function repeatedly accepting a leaf
     * node and index as parameters
     *
     * @param <E>
     */
    public interface NodeProcessor<E> {

        /**
         * Process the entry at the index given in the leaf node given
         *
         * @param leaf
         * @param index
         * @return true if nore entries are acceptable, false if the node should
         * return no more entries (if present)
         */
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
