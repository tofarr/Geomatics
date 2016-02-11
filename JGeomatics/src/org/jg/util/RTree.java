package org.jg;

import org.jg.SpatialNode.NodeProcessor;
import java.util.ArrayDeque;
import java.util.Objects;

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
        this.root = new SpatialNode(null, null, new Rect(),
                new double[INITIAL_CAPACITY << 2], (E[]) new Object[INITIAL_CAPACITY],
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
        root.itemBounds = new double[node.size << 2];
        root.itemValues = (E[]) new Object[node.size];
        node.get(new NodeProcessor<E>() {

            int rootIndex;

            @Override
            public boolean process(SpatialNode<E> leaf, int index) {
                System.arraycopy(leaf.itemBounds, index << 2, root.itemBounds, rootIndex << 2, 4);
                root.itemValues[rootIndex++] = leaf.itemValues[index];
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
        root = new SpatialNode<>(itemBounds, itemValues);
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
    public RTree(double[] itemBounds, E[] itemValues) throws NullPointerException, IllegalArgumentException {
        root = new SpatialNode<>(itemBounds, itemValues);
        optimise();
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
            node.bounds.union(itemBounds, node.bounds);
            node.size++;
            node = getBestCandidateForAdd(itemBounds.minX, itemBounds.minY, itemBounds.maxX, itemBounds.maxY, node.a, node.b);
        }
        if (node.size == node.itemValues.length) {
            int newCapacity = node.size * 3 / 2 + 1;
            double[] newItemBounds = new double[newCapacity << 2];
            System.arraycopy(node.itemBounds, 0, newItemBounds, 0, node.size << 2);
            E[] newItemValues = (E[]) new Object[newCapacity];
            System.arraycopy(node.itemValues, 0, newItemValues, 0, node.size);
            node.itemBounds = newItemBounds;
            node.itemValues = newItemValues;
        }
        int j = node.size << 2;
        node.itemBounds[j++] = itemBounds.minX;
        node.itemBounds[j++] = itemBounds.minY;
        node.itemBounds[j++] = itemBounds.maxX;
        node.itemBounds[j] = itemBounds.maxY;
        node.itemValues[node.size++] = itemValue;
        node.bounds.union(itemBounds);
        if (node.size >= SPLIT_SIZE) {
            trySplit(node);
        }
        return this;
    }

    static <E> SpatialNode<E> getBestCandidateForAdd(double minX, double minY, double maxX, double maxY, SpatialNode<E> a, SpatialNode<E> b) {
        //get expansion size
        double ea = expansionSize(minX, minY, maxX, maxY, a.bounds);
        double eb = expansionSize(minX, minY, maxX, maxY, b.bounds);
        if (ea < eb) {
            return a;
        } else if (ea > eb) {
            return b;
        } else if (ea == 0) { // within both - choose closer
            double dax = maxX - a.bounds.minX;
            double dbx = b.bounds.maxX - minX;
            double day = maxY - a.bounds.minY;
            double dby = b.bounds.maxY - minY;
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

    static double expansionSize(double minX, double minY, double maxX, double maxY, Rect bounds) {
        double w = Math.max(maxX, bounds.maxX) - Math.min(minX, bounds.minX);
        double h = Math.max(maxY, bounds.maxY) - Math.min(minY, bounds.minY);
        double a = (w * h);
        return a - bounds.getArea();
    }

    static <E> void trySplit(SpatialNode<E> node) {
        Rect bounds = node.bounds;
        double width = bounds.getWidth();
        double height = bounds.getHeight();
        if ((width == 0) && (height == 0)) {
            return;
        }
        SpatialNode<E> xa = new SpatialNode<>(null, null, new Rect(bounds.getMinX(), bounds.getMinY(), bounds.getCx(), bounds.getMaxY()), new double[node.size << 2], (E[]) new Object[node.size], 0);
        SpatialNode<E> xb = new SpatialNode<>(null, null, new Rect(bounds.getCx(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY()), new double[node.size << 2], (E[]) new Object[node.size], 0);
        SpatialNode<E> ya = new SpatialNode<>(null, null, new Rect(bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getCy()), new double[node.size << 2], (E[]) new Object[node.size], 0);
        SpatialNode<E> yb = new SpatialNode<>(null, null, new Rect(bounds.getMinX(), bounds.getCy(), bounds.getMaxX(), bounds.getMaxY()), new double[node.size << 2], (E[]) new Object[node.size], 0);

        double[] itemBounds = node.itemBounds;
        E[] itemValues = node.itemValues;
        for (int i = node.size, j = node.size << 2; i-- > 0;) {
            double maxY = itemBounds[--j];
            double maxX = itemBounds[--j];
            double minY = itemBounds[--j];
            double minX = itemBounds[--j];
            E itemValue = itemValues[i];
            SpatialNode<E> n = getBestCandidateForAdd(minX, minY, maxX, maxY, xa, xb);
            System.arraycopy(itemBounds, j, n.itemBounds, n.size << 2, 4);
            n.itemValues[n.size++] = itemValue;
            n = getBestCandidateForAdd(minX, minY, maxX, maxY, ya, yb);
            System.arraycopy(itemBounds, j, n.itemBounds, n.size << 2, 4);
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
        xa.bounds.unionAll(xa.itemBounds, 0, xa.size << 2);
        xb.bounds.unionAll(xb.itemBounds, 0, xb.size << 2);
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
        tree.root.get(new NodeProcessor<E>() {
            final Rect rect = new Rect();

            @Override
            public boolean process(SpatialNode<E> leaf, int index) {
                leaf.getItemBounds(index, rect);
                add(rect, leaf.getItemValue(index));
                return true;
            }

        });
        return tree;
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
        if (!node.bounds.contains(bounds)) {
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
            double[] itemBounds = node.itemBounds;
            E[] itemValues = node.itemValues;
            for (int i = node.size, j = node.size << 2; i-- > 0;) {
                double maxY = itemBounds[--j];
                double maxX = itemBounds[--j];
                double minY = itemBounds[--j];
                double minX = itemBounds[--j];
                if ((minX == bounds.minX)
                        && (minY == bounds.minY)
                        && (maxX == bounds.maxX)
                        && (maxY == bounds.maxY)
                        && Objects.equals(itemValues[i], itemValue)) {
                    System.arraycopy(itemBounds, j + 4, itemBounds, j, (node.size << 2) - (j + 4));
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
        if (bounds.contains(node.bounds)) {
            if (node.isBranch()) {
                node.a = node.b = null;
                node.itemBounds = new double[INITIAL_CAPACITY << 2];
                node.itemValues = (E[]) new Object[INITIAL_CAPACITY];
            }
            int ret = node.size;
            node.bounds.reset();
            node.size = 0;
            return ret;
        } else if (!bounds.isOverlapping(node.bounds)) {
            return 0;
        } else if (node.isBranch()) {
            int ret = removeOverlapping(node.a, bounds) + removeOverlapping(node.b, bounds);
            if (ret != 0) {
                node.bounds.reset().union(node.a.bounds).union(node.b.bounds);
                node.size = node.a.size + node.b.size;
                tryMerge(node);
            }
            return ret;
        } else {
            double[] itemBounds = node.itemBounds;
            E[] itemValues = node.itemValues;
            double[] newItemBounds = new double[node.itemBounds.length];
            E[] newItemValues = (E[]) new Object[node.itemValues.length];
            int m = 0;
            int n = 0;
            for (int i = 0, j = 0; i < node.size; i++) {
                double minX = itemBounds[j++];
                double minY = itemBounds[j++];
                double maxX = itemBounds[j++];
                double maxY = itemBounds[j++];
                if (!Rect.overlaps(minX, minY, maxX, maxY, bounds.minX, bounds.minY, bounds.maxX, bounds.maxY)) {
                    newItemBounds[m++] = minX;
                    newItemBounds[m++] = minY;
                    newItemBounds[m++] = maxX;
                    newItemBounds[m++] = maxY;
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
            node.bounds.reset().unionAll(newItemBounds, 0, m);
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
        if (bounds.contains(node.bounds)) {
            if (node.isBranch()) {
                node.a = node.b = null;
                node.itemBounds = new double[INITIAL_CAPACITY << 2];
                node.itemValues = (E[]) new Object[INITIAL_CAPACITY];
            }
            int ret = node.size;
            node.bounds.reset();
            node.size = 0;
            return ret;
        } else if (bounds.isDisjoint(node.bounds)) {
            return 0;
        } else if (node.isBranch()) {
            int ret = removeInteracting(node.a, bounds) + removeInteracting(node.b, bounds);
            if (ret != 0) {
                node.bounds.reset().union(node.a.bounds).union(node.b.bounds);
                node.size = node.a.size + node.b.size;
                tryMerge(node);
            }
            return ret;
        } else {
            double[] itemBounds = node.itemBounds;
            E[] itemValues = node.itemValues;
            double[] newItemBounds = new double[node.itemBounds.length];
            E[] newItemValues = (E[]) new Object[node.itemValues.length];
            int m = 0;
            int n = 0;
            for (int i = 0, j = 0; i < node.size; i++) {
                double minX = itemBounds[j++];
                double minY = itemBounds[j++];
                double maxX = itemBounds[j++];
                double maxY = itemBounds[j++];
                if (Rect.disjoint(minX, minY, maxX, maxY, bounds.minX, bounds.minY, bounds.maxX, bounds.maxY)) {
                    newItemBounds[m++] = minX;
                    newItemBounds[m++] = minY;
                    newItemBounds[m++] = maxX;
                    newItemBounds[m++] = maxY;
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
            node.bounds.reset().unionAll(newItemBounds, 0, m);
            return ret;
        }
    }

    static <E> void tryMerge(SpatialNode<E> node) {
        if (node.size <= SPLIT_SIZE) {
            node.itemBounds = new double[INITIAL_CAPACITY << 2];
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
            System.arraycopy(toMerge.itemBounds, 0, target.itemBounds, index << 2, toMerge.size << 2);
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
            root.itemBounds = new double[root.size << 2];
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
     * @param target
     * @return target
     * @throws NullPointerException if target was null
     */
    public Rect getBounds(Rect target) throws NullPointerException {
        return root.getBounds(target);
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
     * Iterate over all entries in this tree
     *
     * @return iterator
     */
    public Iter iterator() {
        return new Iter(root);
    }

    /**
     * Process all entries in this node
     *
     * @param processor
     * @return
     * @throws NullPointerException if processor was null
     */
    public boolean get(NodeProcessor<E> processor) throws NullPointerException {
        return root.get(processor);
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
        return root.getInteracting(rect, processor);
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
        return root.getOverlapping(rect, processor);
    }

    /**
     * Iterator over tree
     *
     * @param <E>
     */
    public static final class Iter<E> {

        final ArrayDeque<SpatialNode<E>> stack;
        SpatialNode<E> node;
        int index;

        Iter(SpatialNode<E> node) {
            this.stack = new ArrayDeque<>();
            this.node = firstLeaf(node);
            this.index = -1;
        }

        SpatialNode<E> firstLeaf(SpatialNode<E> node) {
            while (node.isBranch()) {
                stack.push(node.b);
                node = node.a;
            }
            return node;
        }

        SpatialNode<E> nextLeaf() {
            if (stack.isEmpty()) {
                return null;
            }
            SpatialNode<E> n = stack.pop();
            n = firstLeaf(n);
            return n;
        }

        /**
         * Move to next entry
         *
         * @return true if move occured, false otherwise
         */
        public boolean next() {
            while (true) {
                if (node == null) {
                    return false;
                }
                index++;
                if (index < node.size) {
                    return true;
                }
                SpatialNode<E> nextLeaf = nextLeaf();
                node = nextLeaf;
                index = -1;
                if (nextLeaf == null) {
                    return false;
                }

            }
        }

        /**
         * Get bounds of current entry
         *
         * @param target target in which to place bounds
         * @return target
         * @throws IndexOutOfBoundsException if iterator is beyond end of
         * collection
         * @throws NullPointerException if target was null
         */
        public Rect getBounds(Rect target) throws IndexOutOfBoundsException, NullPointerException {
            if (node == null) {
                throw new IndexOutOfBoundsException();
            }
            double[] bounds = node.itemBounds;
            int b = index << 2;
            return target.set(bounds[b++], bounds[b++], bounds[b++], bounds[b]);
        }

        /**
         * Get value of current entry
         *
         * @return
         * @throws IndexOutOfBoundsException if iterator is beyond end of
         * collection
         */
        public E getValue() throws IndexOutOfBoundsException {
            if (node == null) {
                throw new IndexOutOfBoundsException();
            }
            return node.itemValues[index];
        }
    }
}
