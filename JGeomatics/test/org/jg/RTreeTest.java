package org.jg;

import org.jg.RTree;
import org.jg.util.SpatialNode;
import org.jg.Rect;
import org.jg.RTree.Iter;
import org.jg.util.SpatialNode.NodeProcessor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tim.ofarrell
 */
public class RTreeTest {
    
    private final RTree<String> staticTree;
    
    public RTreeTest(){
        staticTree = new RTree<>();
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 50; j++) {
                Rect bounds = new Rect(i, j, i + 1, j + 1);
                String value = i + "_" + j;
                staticTree.add(bounds, value);
            }
        }
    }

    @Test
    public void testConstructor_A(){
        int depth = staticTree.getRoot().getDepth();
        RTree<String> tree = new RTree<>(staticTree.getRoot());
        assertEquals(depth, staticTree.getRoot().getDepth());
        assertTrue(tree.getRoot().getDepth() < depth);
        checkIntegrity(tree.getRoot());
        checkIntegrity(staticTree.getRoot());
        assertEquals(2500, tree.size());
        assertEquals(2500, staticTree.size());
    }
    
    @Test
    public void testConstructor_B(){
        RTree<String> tree = new RTree<>(new Rect[]{new Rect(0, 0, 4, 1),new Rect(3, 0, 7, 1),new Rect(6, 0, 10, 1)}, new String[]{"A","B","C"});
        assertEquals(1, tree.getRoot().getDepth());
        assertEquals(3, tree.size());
        Map<Rect, String> expected = new HashMap<>();
        expected.put(new Rect(0, 0, 4, 1), "A");
        expected.put(new Rect(3, 0, 7, 1), "B");
        expected.put(new Rect(6, 0, 10, 1), "C");
        Iter iter = tree.iterator();
        while(iter.next()){
            assertEquals(expected.remove(iter.getBounds(new Rect())), iter.getValue());
        }
    }
    
    @Test
    public void testConstructor_C(){
        RTree<String> tree = new RTree<>(new double[]{0, 0, 4, 1, 3, 0, 7, 1, 6, 0, 10, 1}, new String[]{"A","B","C"});
        assertEquals(1, tree.getRoot().getDepth());
        assertEquals(3, tree.size());
        Map<Rect, String> expected = new HashMap<>();
        expected.put(new Rect(0, 0, 4, 1), "A");
        expected.put(new Rect(3, 0, 7, 1), "B");
        expected.put(new Rect(6, 0, 10, 1), "C");
        Iter iter = tree.iterator();
        while(iter.next()){
            assertEquals(expected.remove(iter.getBounds(new Rect())), iter.getValue());
        }
    }
    
    @Test
    public void testAddBranchSingleSplit() {
        RTree<Integer> tree = new RTree<>();
        Map<Rect, Integer> map = new HashMap<>();
        for (int j = 0; j < 10; j++) {
            Rect bounds = new Rect(0, j, 1, j + 1);
            map.put(bounds, j);
            tree.add(bounds, j);
        }
        Iter<Integer> iter = tree.iterator();
        while (iter.next()) {
            Rect rect = iter.getBounds(new Rect());
            assertEquals(map.remove(rect), iter.getValue());
        }
        assertTrue(map.isEmpty());
        assertEquals(10, tree.size());
        SpatialNode<Integer> node = tree.getRoot();
        assertEquals(10, node.size());
        assertEquals(new Rect(0, 0, 1, 10), node.bounds);
        assertTrue(node.isBranch());
        assertEquals(new Rect(0, 0, 1, 5), node.a.bounds);
        assertEquals(new Rect(0, 5, 1, 10), node.b.bounds);
    }

    @Test
    public void testAddBranchMultiSplit() {
        RTree<Integer> tree = new RTree<>();
        Map<Rect, Integer> map = new HashMap<>();
        for (int i = 0; i < 20; i++) {
            Rect bounds = new Rect(i, 0, i + 1, 1);
            map.put(bounds, i);
            tree.add(bounds, i);
        }
        Iter<Integer> iter = tree.iterator();
        while (iter.next()) {
            Rect rect = iter.getBounds(new Rect());
            assertEquals(map.remove(rect), iter.getValue());
        }
        assertTrue(map.isEmpty());
        assertEquals(20, tree.size());
        SpatialNode<Integer> node = tree.getRoot();
        assertEquals(20, node.size());
        assertEquals(new Rect(0, 0, 20, 1), node.bounds);
        assertTrue(node.isBranch());
    }

    @Test
    public void testAddMany() {
        RTree<String> tree = new RTree<>();
        Map<Rect, String> map = new HashMap<>();
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 50; j++) {
                Rect bounds = new Rect(i, j, i + 1, j + 1);
                String value = i + "_" + j;
                map.put(bounds, value);
                tree.add(bounds, value);
            }
        }
        Iter<String> iter = tree.iterator();
        while (iter.next()) {
            Rect rect = iter.getBounds(new Rect());
            assertEquals(map.remove(rect), iter.getValue());
        }
        assertTrue(map.isEmpty());
        assertEquals(2500, tree.size());
        checkIntegrity(tree.getRoot());
    }

    static void checkIntegrity(SpatialNode<String> node) {
        if (node.isBranch()) {
            checkIntegrity(node.a);
            checkIntegrity(node.b);
            assertEquals(node.size, node.a.size + node.b.size);
            assertTrue(node.bounds.contains(node.a.bounds));
            assertTrue(node.bounds.contains(node.b.bounds));
        }
    }

    @Test
    public void testAddAll() {
        RTree<Integer> tree = new RTree<>();
        Map<Rect, Integer> map = new HashMap<>();
        for (int i = 0; i < 20; i++) {
            Rect bounds = new Rect(i, 0, i + 1, 1);
            map.put(bounds, i);
            tree.add(bounds, i);
        }
        tree = new RTree<Integer>().addAll(tree);
        Iter<Integer> iter = tree.iterator();
        while (iter.next()) {
            Rect rect = iter.getBounds(new Rect());
            assertEquals(map.remove(rect), iter.getValue());
        }
        assertTrue(map.isEmpty());
        assertEquals(20, tree.size());
        SpatialNode<Integer> node = tree.getRoot();
        assertEquals(20, node.size());
        assertEquals(new Rect(0, 0, 20, 1), node.bounds);
        assertTrue(node.isBranch());
        try {
            tree.addAll(tree);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
    }

    @Test
    public void testAddSame() {
        RTree<Integer> tree = new RTree<>();
        Set<Integer> values = new HashSet<>();
        Rect bounds = new Rect(1, 3, 7, 13);
        for (int i = 0; i < 20; i++) {
            tree.add(bounds, i);
            values.add(i);
        }
        assertTrue(tree.getRoot().isLeaf());
        assertEquals(20, tree.size());
        for (int i = 0; i < 20; i++) {
            assertEquals(bounds, tree.getRoot().getItemBounds(i, new Rect()));
            assertTrue(values.remove(tree.getRoot().getItemValue(i)));
        }
    }

    @Test
    public void testAddNoArea() {
        RTree<Integer> tree = new RTree<>();
        Set<Integer> values = new HashSet<>();
        Rect bounds = new Rect(1, 3, 7, 3);
        for (int i = 0; i < 20; i++) {
            tree.add(bounds, i);
            values.add(i);
        }
        assertTrue(tree.getRoot().isLeaf());
        assertEquals(20, tree.size());
        for (int i = 0; i < 20; i++) {
            assertEquals(bounds, tree.getRoot().getItemBounds(i, new Rect()));
            assertTrue(values.remove(tree.getRoot().getItemValue(i)));
        }

        tree = new RTree<>();
        values = new HashSet<>();
        bounds = new Rect(1, 3, 1, 13);
        for (int i = 0; i < 20; i++) {
            tree.add(bounds, i);
            values.add(i);
        }
        assertTrue(tree.getRoot().isLeaf());
        assertEquals(20, tree.size());
        for (int i = 0; i < 20; i++) {
            assertEquals(bounds, tree.getRoot().getItemBounds(i, new Rect()));
            assertTrue(values.remove(tree.getRoot().getItemValue(i)));
        }
    }

    @Test
    public void testRemove_A() {
        RTree<String> tree = new RTree<>();
        Map<Rect, String> map = new HashMap<>();
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 50; j++) {
                Rect bounds = new Rect(i, j, i + 1, j + 1);
                String value = i + "_" + j;
                map.put(bounds, value);
                tree.add(bounds, value);
            }
        }
        assertFalse(tree.isEmpty());
        int expectedSize = tree.size();
        for (Entry<Rect, String> entry : map.entrySet()) {
            boolean removed = tree.remove(entry.getKey(), entry.getValue());
            assertTrue(removed);
            expectedSize--;
            assertEquals(expectedSize, tree.size());
        }
        checkIntegrity(tree.getRoot());
        assertTrue(tree.isEmpty());
    }

    @Test
    public void testRemoveSingle() {
        RTree<Integer> tree = new RTree<>();
        Map<Rect, Integer> map = new HashMap<>();
        for (int i = 0; i < 20; i++) {
            Rect bounds = new Rect(i, 0, i + 1, 1);
            if ((i < 5) || (i >= 15)) {
                map.put(bounds, i);
            }
            tree.add(bounds, i);
        }
        assertEquals(10, tree.removeOverlapping(new Rect(5, 0, 15, 1)));
        Iter<Integer> iter = tree.iterator();
        while (iter.next()) {
            Rect rect = iter.getBounds(new Rect());
            assertEquals(map.remove(rect), iter.getValue());
        }
        assertTrue(map.isEmpty());
        assertEquals(10, tree.size());

        assertFalse(tree.remove(new Rect(3, 0, 4, 1), -3));
        assertFalse(tree.remove(new Rect(3, 0, 4, 0.9), 3));
        assertFalse(tree.remove(new Rect(3, 0, 3.9, 1), 3));
        assertFalse(tree.remove(new Rect(3, 0.1, 4, 1), 3));
        assertFalse(tree.remove(new Rect(3.1, 0, 4, 1), 3));
    }

    @Test
    public void testRemoveContained() {
        RTree<Integer> tree = new RTree<>();
        for (int i = 0; i < 20; i++) {
            Rect bounds = new Rect(i, 0, i + 1, 1);
            tree.add(bounds, i);
        }
        assertEquals(20, tree.removeOverlapping(new Rect(0, 0, 20, 1)));
        tree = new RTree<>();
        for (int i = 0; i < 20; i++) {
            Rect bounds = new Rect(i, 0, i + 1, 1);
            tree.add(bounds, i);
        }
        assertEquals(20, tree.removeInteracting(new Rect(0, 0, 20, 1)));
        tree = new RTree<>();
        for (int i = 0; i < 9; i++) {
            Rect bounds = new Rect(i, 0, i + 1, 1);
            tree.add(bounds, i);
        }
        assertEquals(9, tree.removeInteracting(new Rect(0, 0, 20, 1)));
    }

    @Test
    public void testRemoveMany() {
        RTree<String> tree = new RTree<>();
        Map<Rect, String> map = new HashMap<>();
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 50; j++) {
                Rect bounds = new Rect(i, j, i + 1, j + 1);
                String value = i + "_" + j;
                if (i > 1 && i < 49 && j > 1 && j < 49) {
                    map.put(bounds, value);
                }
                tree.add(bounds, value);
            }
        }
        assertEquals(2116, tree.removeOverlapping(new Rect(2, 2, 48, 48)));
        assertEquals(188, tree.removeInteracting(new Rect(2, 2, 48, 48)));
        assertEquals(196, tree.size());
        assertEquals(new Rect(0, 0, 50, 50), tree.getBounds(new Rect()));
    }

    @Test
    public void testSize() {
        assertEquals(2304, staticTree.sizeOverlapping(new Rect(1, 1, 49, 49)));
        assertEquals(2500, staticTree.sizeInteracting(new Rect(1, 1, 49, 49)));
    }

    @Test
    public void testTrySplit() {
        Rect rect = new Rect(1, 3, 1, 3);
        Rect[] itemBounds = new Rect[10];
        Integer[] itemValues = new Integer[10];
        for (int i = 0; i < 10; i++) {
            itemBounds[i] = rect;
        }
        SpatialNode<Integer> node = new SpatialNode<>(itemBounds, itemValues);
        RTree.trySplit(node);
        assertFalse(node.isBranch());
        RTree.tryRecursiveSplit(node);
        assertFalse(node.isBranch());

        node.bounds.set(1, 3, 2, 3);
        RTree.trySplit(node);
        assertFalse(node.isBranch());

        node.bounds.set(1, 3, 1, 4);
        RTree.trySplit(node);
        assertFalse(node.isBranch());

        node.bounds.set(0, 3, 1, 3);
        RTree.trySplit(node);
        assertFalse(node.isBranch());
    }

    @Test
    public void testIsEmpty() {
        RTree<String> tree = new RTree<>();
        assertTrue(tree.isEmpty());
        assertFalse(staticTree.isEmpty(new Rect(0, 0, 50, 50)));
        assertTrue(staticTree.isEmpty(new Rect(0, 50, 1, 51)));
        assertTrue(staticTree.isEmpty(new Rect(50, 0, 51, 1)));
        assertFalse(staticTree.isEmpty(new Rect(23, 29, 24, 30)));
        assertFalse(staticTree.isEmpty(new Rect(23, 29, 25, 31)));
        assertFalse(staticTree.isEmpty(new Rect(23.1, 29.1, 23.9, 29.9)));
    }

    @Test
    public void testIsDisjoint() {
        assertFalse(staticTree.isDisjoint(new Rect(0, 0, 50, 50)));
        assertTrue(staticTree.isDisjoint(new Rect(0, 51, 1, 52)));
        assertFalse(staticTree.isDisjoint(new Rect(0, 50, 1, 51)));
        assertTrue(staticTree.isDisjoint(new Rect(51, 0, 52, 1)));
        assertFalse(staticTree.isDisjoint(new Rect(50, 0, 51, 1)));
        assertFalse(staticTree.isDisjoint(new Rect(23, 29, 24, 30)));
        assertFalse(staticTree.isDisjoint(new Rect(23, 29, 25, 31)));
        assertFalse(staticTree.isDisjoint(new Rect(23.1, 29.1, 23.9, 29.9)));
    }

    @Test
    public void testContains() {
        for(int i = 0; i < 50; i++){
            for(int j = 0; j < 50; j++){
                assertTrue(staticTree.contains(new Rect(i, j, i+1, j+1), i+"_"+j));
            }
        }
        assertFalse(staticTree.contains(new Rect(0, 0, 1, 1), "0_1"));
        assertFalse(staticTree.contains(new Rect(0, 0, 1, 0.9), "0_0"));
        assertFalse(staticTree.contains(new Rect(0, 0, 0.9, 1), "0_0"));
        assertFalse(staticTree.contains(new Rect(0, 0.1, 1, 1), "0_0"));
        assertFalse(staticTree.contains(new Rect(0.1, 0, 1, 1), "0_0"));
    }

    @Test
    public void testContainsValue() {
        for(int i = 0; i < 50; i++){
            for(int j = 0; j < 50; j++){
                assertTrue(staticTree.containsValue(i+"_"+j));
            }
        }
        assertFalse(staticTree.containsValue("0_50"));
        assertFalse(staticTree.containsValue("50_0"));
    }
    
    @Test
    public void testOptimise() {
        RTree<String> tree = new RTree<>();
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 50; j++) {
                Rect bounds = new Rect(i, j, i + 1, j + 1);
                String value = i + "_" + j;
                tree.add(bounds, value);
            }
        }
        int depth = tree.getRoot().getDepth();
        tree.optimise();
        int optimisedDepth = tree.getRoot().getDepth();
        assertTrue(optimisedDepth < depth);
        assertEquals(new Rect(0,0,50,50), tree.getBounds(new Rect()));
        assertEquals(2500, tree.size());
        checkIntegrity(tree.getRoot());
    }

    @Test
    public void testIterator_0args() {
        RTree<Integer> tree = new RTree<>();
        assertFalse(tree.iterator().next());
        Map<Rect, Integer> map = new HashMap<>();
        for (int j = 0; j < 10; j++) {
            Rect bounds = new Rect(0, j, 1, j + 1);
            map.put(bounds, j);
            tree.add(bounds, j);
        }
        tree.optimise();
        Iter<Integer> iter = tree.iterator();
        while(iter.next()){
            Rect rect = iter.getBounds(new Rect());
            assertEquals(map.remove(rect), iter.getValue());
        }
        assertTrue(map.isEmpty());
        assertFalse(iter.next());
        try{
            iter.getBounds(new Rect());
            fail("Exception expected!");
        }catch(IndexOutOfBoundsException ex){
        }
        try{
            iter.getValue();
            fail("Exception expected!");
        }catch(IndexOutOfBoundsException ex){
        }
        iter = tree.iterator();
        try{
            iter.getBounds(new Rect());
            fail("Exception expected!");
        }catch(IndexOutOfBoundsException ex){
        }
        try{
            iter.getValue();
            fail("Exception expected!");
        }catch(IndexOutOfBoundsException ex){
        }
        iter.next();
        try{
            iter.getBounds(null);
            fail("Exception expected!");
        }catch(NullPointerException ex){
        }
    }

    @Test
    public void testGet() {
        RTree<Integer> tree = new RTree<>();
        assertTrue(tree.get(new NodeProcessor<Integer>(){
            @Override
            public boolean process(SpatialNode<Integer> leaf, int index){
                throw new IllegalStateException();
            }
        }));

        final Map<Rect, String> map = new HashMap<>();
        for (int i = 1; i <= 7; i++) {
            for (int j = 2; j <= 13; j++) {
                map.put(new Rect(i, j, i + 1, j + 1), i + "_" + j);
            }
        }
        final Rect bounds = new Rect();
        assertTrue(staticTree.getInteracting(new Rect(2, 3, 7, 13), new NodeProcessor<String>() {

            @Override
            public boolean process(SpatialNode<String> leaf, int index) {
                assertEquals(map.remove(leaf.getItemBounds(index, bounds)), leaf.getItemValue(index));
                return true;
            }

        }));
        assertTrue(map.isEmpty());
        
        for (int i = 2; i < 7; i++) {
            for (int j = 3; j < 13; j++) {
                map.put(new Rect(i, j, i + 1, j + 1), i + "_" + j);
            }
        }
        assertTrue(staticTree.getOverlapping(new Rect(2, 3, 7, 13), new NodeProcessor<String>() {

            @Override
            public boolean process(SpatialNode<String> leaf, int index) {
                assertEquals(map.remove(leaf.getItemBounds(index, bounds)), leaf.getItemValue(index));
                return true;
            }

        }));
        assertTrue(map.isEmpty());
    }
    
    @Test
    public void testGetBestCandidateForAdd(){
        SpatialNode<Integer> a = new SpatialNode<>();
        SpatialNode<Integer> b = new SpatialNode<>();
        a.bounds.set(0, 0, 20, 10);
        b.bounds.set(10, 0, 30, 10);
        assertSame(a, RTree.getBestCandidateForAdd(0, 0, 10, 10, a, b));
        assertSame(b, RTree.getBestCandidateForAdd(20, 0, 30, 10, a, b));
    }
}
