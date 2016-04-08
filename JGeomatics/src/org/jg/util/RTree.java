package org.jg.util;

import java.util.Arrays;
import java.util.Objects;
import org.jg.geom.Rect;
import org.jg.geom.RectBuilder;
import org.jg.geom.Relation;
import org.jg.util.SpatialNode.NodeProcessor;

/**
 * Class for spatially indexing data for rapid searching. Spatially, queries are
 * performed by bounding box and may result in zero or more results. Typically,
 * the more edits applied to an index, the less efficient it is, so indexes
 * should be reoptimised after batchs of edits.
 *
 * @author tofar_000
 * @param <E>
 */
public final class RTree<E> {

    static final int INITIAL_CAPACITY = 16;
    static final int SPLIT_SIZE = 10;
    final SpatialNode<E> root;

    /**
     * Create a new empty RTree instance
     */
    public RTree() {
        this.root = new SpatialNode(null, null, new RectBuilder(),
                new Rect[INITIAL_CAPACITY], (E[]) new Object[INITIAL_CAPACITY],
                0);
    }

    /**
     * Create a new RTree instance based on the node given
     *
     * @param node
     * @throws NullPointerException if node was null
     */
    public RTree(SpatialNode<E> node) throws NullPointerException {
        root = new SpatialNode<>();
        root.bounds.set(node.bounds);
        root.size = node.size;
        root.itemBounds = new Rect[node.size];
        root.itemValues = (E[]) new Object[node.size];
        node.forEach(new NodeProcessor<E>() {

            int rootIndex;

            @Override
            public boolean process(Rect bounds, E value) {
                root.itemBounds[rootIndex] = bounds;
                root.itemValues[rootIndex] = value;
                rootIndex++;
                return true;
            }

        });
        optimise();
    }

    /**
     * Create a new optimised RTree instance based on the values given
     *
     * @param itemBounds
     * @param itemValues
     * @throws NullPointerException if itemBounds or itemValues was null
     * @throws IllegalArgumentException if a bounds was invalid or the number of
     * bounds did not match the number of values
     */
    public RTree(Rect[] itemBounds, E[] itemValues) throws NullPointerException, IllegalArgumentException {
        if(itemBounds.length < SPLIT_SIZE){
            root = new SpatialNode<>(itemBounds, itemValues);
        }else{
            if (itemBounds.length != itemValues.length) {
                throw new IllegalArgumentException("Different numbers of arguments : " + itemBounds.length + "->" + itemValues.length);
            }
            RectBuilder bounds = new RectBuilder().addRects(itemBounds);
            root = new SpatialNode<E>(null, null, bounds, itemBounds, itemValues, itemBounds.length);
            optimise();
        }
    }

    /**
     * Add the value given at the location given
     *
     * @param itemBounds
     * @param itemValue
     * @return this
     * @throws NullPointerException if itemBounds was null
     */
    public RTree<E> add(Rect itemBounds, E itemValue) throws NullPointerException {
        SpatialNode<E> node = root;
        while (node.isBranch()) {
            node.bounds.add(itemBounds);
            node.size++;
            node = getBestCandidateForAdd(itemBounds, node.a, node.b);
        }
        if (node.size == node.itemValues.length) {
            int newCapacity = node.size * 3 / 2 + 1;
            node.itemBounds = Arrays.copyOf(node.itemBounds, newCapacity);
            node.itemValues = Arrays.copyOf(node.itemValues, newCapacity);
        }
        node.itemBounds[node.size] = itemBounds;
        node.itemValues[node.size++] = itemValue;
        node.bounds.add(itemBounds);
        if (node.size >= SPLIT_SIZE) {
            trySplit(node);
        }
        return this;
    }

    static <E> SpatialNode<E> getBestCandidateForAdd(Rect bounds, SpatialNode<E> a, SpatialNode<E> b) {
        //get expansion size
        double ea = expansionSize(bounds, a.bounds);
        double eb = expansionSize(bounds, b.bounds);
        if (ea < eb) {
            return a;
        } else if (ea > eb) {
            return b;
        } else if (ea == 0) { // within both - choose closer
            double dax = bounds.maxX - a.bounds.getMinX();
            double dbx = b.bounds.getMaxX() - bounds.minX;
            double day = bounds.maxY - a.bounds.getMinY();
            double dby = b.bounds.getMaxY() - bounds.minY;
            double dx = Math.abs(dax - dbx);
            double dy = Math.abs(day - dby);
            if (dx > dy) {
                return (dax < dbx) ? a : b;
            } else {
                return (day < dby) ? a : b;
            }
        } else {
            return b;
        }
    }

    static double expansionSize(Rect a, RectBuilder b) {
        double w = Math.max(a.maxX, b.getMaxX()) - Math.min(a.minX, b.getMinX());
        double h = Math.max(a.maxY, b.getMaxY()) - Math.min(a.minY, b.getMinY());
        double area = (w * h);
        return area - b.getArea();
    }

    static <E> void trySplit(SpatialNode<E> node) {
        RectBuilder bounds = node.bounds;
        double width = bounds.getWidth();
        double height = bounds.getHeight();
        if ((width == 0) && (height == 0)) {
            return;
        }
        SpatialNode<E> xa = new SpatialNode<>(null, null, new RectBuilder(bounds.getMinX(), bounds.getMinY(), bounds.getCx(), bounds.getMaxY()), new Rect[node.size], (E[]) new Object[node.size], 0);
        SpatialNode<E> xb = new SpatialNode<>(null, null, new RectBuilder(bounds.getCx(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY()), new Rect[node.size], (E[]) new Object[node.size], 0);
        SpatialNode<E> ya = new SpatialNode<>(null, null, new RectBuilder(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getCy()), new Rect[node.size], (E[]) new Object[node.size], 0);
        SpatialNode<E> yb = new SpatialNode<>(null, null, new RectBuilder(bounds.getMinX(), bounds.getCy(), bounds.getMaxX(), bounds.getMaxY()), new Rect[node.size], (E[]) new Object[node.size], 0);

        Rect[] itemBounds = node.itemBounds;
        E[] itemValues = node.itemValues;
        for (int i = node.size; i-- > 0;) {
            Rect itemBound = itemBounds[i];
            E itemValue = itemValues[i];
            SpatialNode<E> n = getBestCandidateForAdd(itemBound, xa, xb);
            n.itemBounds[n.size] = itemBound;
            n.itemValues[n.size++] = itemValue;
            n = getBestCandidateForAdd(itemBound, ya, yb);
            n.itemBounds[n.size] = itemBound;
            n.itemValues[n.size++] = itemValue;
        }
        int diffA = Math.abs(xa.size - xb.size);
        int diffC = Math.abs(ya.size - yb.size);
        if (diffA > diffC) {
            xa = ya;
            xb = yb;
        }
        if ((xa.size == 0) || (xb.size == 0)) {
            return; // could not split
        }
        xa.bounds.addRects(xa.itemBounds, 0, xa.size);
        xb.bounds.addRects(xb.itemBounds, 0, xb.size);
        node.a = xa;
        node.b = xb;
        node.itemBounds = null;
        node.itemValues = null;
    }

    /**
     * Add the mappings from the tree given
     *
     * @param tree
     * @return this
     * @throws NullPointerException if tree was null
     * @throws IllegalArgumentException if tree == this
     */
    public RTree<E> addAll(final RTree<E> tree) throws NullPointerException, IllegalArgumentException {
        if (this == tree) {
            throw new IllegalArgumentException("Cannot add to self!");
        }
        if(isEmpty()){
            SpatialNode<E> node = tree.root;
            root.bounds.set(node.bounds);
            root.size = node.size;
            root.itemBounds = new Rect[node.size];
            root.itemValues = (E[]) new Object[node.size];
            node.forEach(new NodeProcessor<E>() {

                int rootIndex;

                @Override
                public boolean process(Rect bounds, E value) {
                    root.itemBounds[rootIndex] = bounds;
                    root.itemValues[rootIndex] = value;
                    rootIndex++;
                    return true;
                }

            });
            optimise();
        }else{
            tree.root.forEach(new NodeProcessor<E>() {

                @Override
                public boolean process(Rect bounds, E value) {
                    add(bounds, value);
                    return true;
                }

            });
        }
        return this;
    }

    /**
     * Add the value given at the location given
     *
     * @param itemBounds
     * @param itemValue
     * @return true if removed, false otherwise
     * @throws NullPointerException if itemBounds was null
     */
    public boolean remove(Rect itemBounds, E itemValue) throws NullPointerException {
        return remove(root, itemBounds, itemValue);
    }

    static <E> boolean remove(SpatialNode<E> node, Rect bounds, E itemValue) {
        int relate = bounds.relate(node.bounds, Tolerance.ZERO);
        if (Relation.isOutside(relate)) {
            return false;
        } else if (node.isBranch()) {
            boolean ret = remove(node.a, bounds, itemValue)
                    || remove(node.b, bounds, itemValue);
            if (ret) {
                node.size--;
                tryMerge(node);
            }
            return ret;
        } else {
            Rect[] itemBounds = node.itemBounds;
            E[] itemValues = node.itemValues;
            for (int i = node.size; i-- > 0;) {
                if(bounds.equals(itemBounds[i])
                        && Objects.equals(itemValues[i], itemValue)) {
                    System.arraycopy(itemBounds, i + 1, itemBounds, i, node.size - (i + 1));
                    System.arraycopy(itemValues, i + 1, itemValues, i, node.size - (i + 1));
                    node.size--;
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Remove all mappings overlapping with the bounds given
     *
     * @param bounds
     * @return number of removed items
     * @throws NullPointerException bounds was null
     */
    public int removeOverlapping(Rect bounds) throws NullPointerException {
        return removeOverlapping(root, bounds);
    }

    static <E> int removeOverlapping(SpatialNode<E> node, Rect bounds) {
        int relate = bounds.relate(node.bounds, Tolerance.ZERO);
        if (!Relation.isOutside(relate)) {
            if (node.isBranch()) {
                node.a = node.b = null;
                node.itemBounds = new Rect[INITIAL_CAPACITY];
                node.itemValues = (E[]) new Object[INITIAL_CAPACITY];
            }
            int ret = node.size;
            node.bounds.reset();
            node.size = 0;
            return ret;
        } else if (!Relation.isInside(relate)) {
            return 0;
        } else if (node.isBranch()) {
            int ret = removeOverlapping(node.a, bounds) + removeOverlapping(node.b, bounds);
            if (ret != 0) {
                node.bounds.reset().add(node.a.bounds).add(node.b.bounds);
                node.size = node.a.size + node.b.size;
                tryMerge(node);
            }
            return ret;
        } else {
            Rect[] itemBounds = node.itemBounds;
            E[] itemValues = node.itemValues;
            Rect[] newItemBounds = new Rect[node.itemBounds.length];
            E[] newItemValues = (E[]) new Object[node.itemValues.length];
            int n = 0;
            for (int i = 0; i < node.size; i++) {
                if(!Relation.isInside(bounds.relate(itemBounds[i], Tolerance.ZERO))){
                    newItemBounds[n] = itemBounds[i];
                    newItemValues[n++] = itemValues[i];
                }
            }
            if (n == node.size) {
                return 0;
            }
            int ret = node.size - n;
            node.itemBounds = newItemBounds;
            node.itemValues = newItemValues;
            node.size = n;
            node.bounds.reset().addRects(newItemBounds, 0, n);
            return ret;
        }
    }

    /**
     * Remove all mappings interacting with the bounds given
     *
     * @param bounds
     * @return number of removed items
     * @throws NullPointerException bounds was null
     */
    public int removeInteracting(Rect bounds) throws NullPointerException {
        return removeInteracting(root, bounds);
    }

    static <E> int removeInteracting(SpatialNode<E> node, Rect bounds) {
        if (!Relation.isOutside(bounds.relate(node.bounds, Tolerance.ZERO))) {
            if (node.isBranch()) {
                node.a = node.b = null;
                node.itemBounds = new Rect[INITIAL_CAPACITY];
                node.itemValues = (E[]) new Object[INITIAL_CAPACITY];
            }
            int ret = node.size;
            node.bounds.reset();
            node.size = 0;
            return ret;
        } else if (Relation.isDisjoint(bounds.relate(node.bounds, Tolerance.ZERO))) {
            return 0;
        } else if (node.isBranch()) {
            int ret = removeInteracting(node.a, bounds) + removeInteracting(node.b, bounds);
            if (ret != 0) {
                node.bounds.reset().add(node.a.bounds).add(node.b.bounds);
                node.size = node.a.size + node.b.size;
                tryMerge(node);
            }
            return ret;
        } else {
            Rect[] itemBounds = node.itemBounds;
            E[] itemValues = node.itemValues;
            Rect[] newItemBounds = new Rect[node.itemBounds.length];
            E[] newItemValues = (E[]) new Object[node.itemValues.length];
            int n = 0;
            for (int i = 0; i < node.size; i++) {
                Rect itemBound = itemBounds[i];
                if (Relation.isDisjoint(itemBound.relate(bounds, Tolerance.ZERO))) {
                    newItemBounds[n] = itemBound;
                    newItemValues[n++] = itemValues[i];
                }
            }
            if (n == node.size) {
                return 0;
            }
            int ret = node.size - n;
            node.itemBounds = newItemBounds;
            node.itemValues = newItemValues;
            node.size = n;
            node.bounds.reset().addRects(newItemBounds, 0, n);
            return ret;
        }
    }

    static <E> void tryMerge(SpatialNode<E> node) {
        if (node.size <= SPLIT_SIZE) {
            node.itemBounds = new Rect[INITIAL_CAPACITY];
            node.itemValues = (E[]) new Object[INITIAL_CAPACITY];
            int index = mergeNode(0, node.a, node);
            mergeNode(index, node.b, node);
            node.a = node.b = null;
        } else if (node.a.size == 0) {
            node.a = node.b.a;
            node.b = node.b.b;
            node.itemBounds = node.b.itemBounds;
            node.itemValues = node.b.itemValues;
        } else if (node.b.size == 0) {
            node.b = node.a.b;
            node.a = node.a.a;
            node.itemBounds = node.a.itemBounds;
            node.itemValues = node.a.itemValues;
        }
    }

    static <E> int mergeNode(int index, SpatialNode<E> toMerge, SpatialNode<E> target) {
        if (toMerge.isBranch()) {
            index = mergeNode(index, toMerge.a, target);
            index = mergeNode(index, toMerge.b, target);
            return index;
        } else {
            System.arraycopy(toMerge.itemBounds, 0, target.itemBounds, index, toMerge.size);
            System.arraycopy(toMerge.itemValues, 0, target.itemValues, index, toMerge.size);
            index += toMerge.size;
            return index;
        }
    }

    /**
     * Attempt to improve the quality of this index. Generally a good idea to
     * call this after a lot of data has been added piecemeal.
     */
    public void optimise() {
        if (!root.isLeaf()) {
            root.itemBounds = new Rect[root.size];
            root.itemValues = (E[]) new Object[root.size];
            mergeNode(0, root, root); // turn into a leaf.
            root.a = root.b = null;
        }
        tryRecursiveSplit(root); //recursively subdivide while size > SPLIT_SIZE
    }

    static <E> void tryRecursiveSplit(SpatialNode<E> node) {
        if (node.size >= SPLIT_SIZE) {
            trySplit(node);
            if (node.isBranch()) {
                tryRecursiveSplit(node.a);
                tryRecursiveSplit(node.b);
            }
        }
    }

    /**
     * Get the root node for this RTree
     *
     * @return the root node
     */
    public SpatialNode<E> getRoot() {
        return root;
    }

    /**
     * Get the bounds of this tree
     *
     * @return target
     * @throws NullPointerException if target was null
     */
    public Rect getBounds() throws NullPointerException {
        return root.getBounds();
    }

    /**
     * Get the number of entries stored in this collection
     *
     * @return
     */
    public int size() {
        return root.size();
    }

    /**
     * Get the number of entries stored in this collection intersecting (Sharing
     * internal area or touching) with the bounds given
     *
     * @param bounds
     * @return the number of interacting entries
     * @throws NullPointerException if bounds was null
     */
    public int sizeInteracting(Rect bounds) throws NullPointerException {
        return root.sizeInteracting(bounds);
    }

    /**
     * Get the number of entries stored in this collection overlapping (Sharing
     * internal area) with the bounds given.
     *
     * @param bounds
     * @return the number of overlapping entries
     * @throws NullPointerException if bounds was null
     */
    public int sizeOverlapping(Rect bounds) throws NullPointerException {
        return root.sizeOverlapping(bounds);
    }

    /**
     * Determine if this tree is empty
     *
     * @return
     */
    public boolean isEmpty() {
        return root.isEmpty();
    }

    /**
     * Determine if the region given overlaps. (Shares internal area with) any
     * entries in this tree
     *
     * @param bounds
     * @return true if bounds overlaps with no entries, false otherwise.
     * @throws NullPointerException if bounds was null
     */
    public boolean isEmpty(Rect bounds) throws NullPointerException {
        return root.isEmpty(bounds);
    }

    /**
     * Determines if the bounds given is disjoint from (is outside of and does
     * not touch) all entries in this tree
     *
     * @param bounds
     * @return
     * @throws NullPointerException if bounds was null
     */
    public boolean isDisjoint(Rect bounds) throws NullPointerException {
        return root.isDisjoint(bounds);
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
        return root.contains(rect, value);
    }

    /**
     * Determine whether this collection contains the value given
     *
     * @param value
     * @return
     */
    public boolean containsValue(E value) {
        return root.containsValue(value);
    }

    /**
     * Process all entries in this node
     *
     * @param processor
     * @return
     * @throws NullPointerException if processor was null
     */
    public boolean forEach(NodeProcessor<E> processor) throws NullPointerException {
        return root.forEach(processor);
    }

    /**
     * Get any entries not disjoint from the rect given
     *
     * @param rect
     * @param processor
     * @return false if processor returned false, true otherwise
     * @throws NullPointerException if rect or processor was null
     */
    public boolean forInteracting(Rect rect, NodeProcessor<E> processor) throws NullPointerException {
        return root.forInteracting(rect, processor);
    }

    /**
     * Get any entries overlapping from the rect given
     *
     * @param rect
     * @param processor
     * @return false if processor returned false, true otherwise
     * @throws NullPointerException if rect or processor was null
     */
    public boolean forOverlapping(Rect rect, NodeProcessor<E> processor) throws NullPointerException {
        return root.forOverlapping(rect, processor);
    }
}
