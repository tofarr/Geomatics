package org.jg.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.jg.geom.Rect;
import org.jg.geom.RectBuilder;
import org.jg.geom.Relation;
import org.jg.util.SpatialNode.NodeProcessor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar
 */
public class RTreeTest {
    
    private final RTree<String> staticTree;
    
    public RTreeTest(){
        staticTree = new RTree<>();
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 50; j++) {
                Rect bounds = Rect.valueOf(i, j, i + 1, j + 1);
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
        RTree<String> tree = new RTree<>(new Rect[]{Rect.valueOf(0, 0, 4, 1),Rect.valueOf(3, 0, 7, 1),Rect.valueOf(6, 0, 10, 1)}, new String[]{"A","B","C"});
        assertEquals(1, tree.getRoot().getDepth());
        assertEquals(3, tree.size());
        final Map<Rect, String> expected = new HashMap<>();
        expected.put(Rect.valueOf(0, 0, 4, 1), "A");
        expected.put(Rect.valueOf(3, 0, 7, 1), "B");
        expected.put(Rect.valueOf(6, 0, 10, 1), "C");
        assertTrue(tree.forEach(new NodeProcessor<String>(){
            @Override
            public boolean process(Rect bounds, String value) {
                assertEquals(expected.remove(bounds), value);
                return true;
            }
        
        }));
        try{
            new RTree<>(new Rect[20], new String[21]);
            fail("Exception expected");
        }catch(IllegalArgumentException ex){
        }
    }
    
    @Test
    public void testAddBranchSingleSplit() {
        RTree<Integer> tree = new RTree<>();
        final Map<Rect, Integer> map = new HashMap<>();
        for (int j = 0; j < 10; j++) {
            Rect bounds = Rect.valueOf(0, j, 1, j + 1);
            map.put(bounds, j);
            tree.add(bounds, j);
        }
        assertTrue(tree.forEach(new NodeProcessor<Integer>(){
            @Override
            public boolean process(Rect bounds, Integer value) {
                assertEquals(map.remove(bounds), value);
                return true;
            }
        
        }));
        assertTrue(map.isEmpty());
        assertEquals(10, tree.size());
        SpatialNode<Integer> node = tree.getRoot();
        assertEquals(10, node.size());
        assertEquals(new RectBuilder(0, 0, 1, 10), node.bounds);
        assertTrue(node.isBranch());
        assertEquals(new RectBuilder(0, 0, 1, 5), node.a.bounds);
        assertEquals(new RectBuilder(0, 5, 1, 10), node.b.bounds);
    }

    @Test
    public void testAddBranchMultiSplit() {
        RTree<Integer> tree = new RTree<>();
        final Map<Rect, Integer> map = new HashMap<>();
        for (int i = 0; i < 20; i++) {
            Rect bounds = Rect.valueOf(i, 0, i + 1, 1);
            map.put(bounds, i);
            tree.add(bounds, i);
        }
        assertTrue(tree.forEach(new NodeProcessor<Integer>(){
            @Override
            public boolean process(Rect bounds, Integer value) {
                assertEquals(map.remove(bounds), value);
                return true;
            }
        
        }));
        assertTrue(map.isEmpty());
        assertEquals(20, tree.size());
        SpatialNode<Integer> node = tree.getRoot();
        assertEquals(20, node.size());
        assertEquals(new RectBuilder(0, 0, 20, 1), node.bounds);
        assertTrue(node.isBranch());
    }

    @Test
    public void testAddMany() {
        RTree<String> tree = new RTree<>();
        final Map<Rect, String> map = new HashMap<>();
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 50; j++) {
                Rect bounds = Rect.valueOf(i, j, i + 1, j + 1);
                String value = i + "_" + j;
                map.put(bounds, value);
                tree.add(bounds, value);
            }
        }
        assertTrue(tree.forEach(new NodeProcessor<String>(){
            @Override
            public boolean process(Rect bounds, String value) {
                assertEquals(map.remove(bounds), value);
                return true;
            }
        
        }));
        assertTrue(map.isEmpty());
        assertEquals(2500, tree.size());
        checkIntegrity(tree.getRoot());
    }

    static void checkIntegrity(SpatialNode<String> node) {
        if (node.isBranch()) {
            checkIntegrity(node.a);
            checkIntegrity(node.b);
            assertEquals(node.size, node.a.size + node.b.size);
            assertFalse(Relation.isOutside(node.bounds.build().relate(node.a.bounds, Tolerance.ZERO)));
            assertFalse(Relation.isOutside(node.bounds.build().relate(node.b.bounds, Tolerance.ZERO)));
        }
    }

    @Test
    public void testAddAll() {
        RTree<Integer> tree = new RTree<>();
        final Map<Rect, Integer> map = new HashMap<>();
        for (int i = 0; i < 20; i++) {
            Rect bounds = Rect.valueOf(i, 0, i + 1, 1);
            map.put(bounds, i);
            tree.add(bounds, i);
        }
        tree = new RTree<Integer>().addAll(tree);
        assertTrue(tree.forEach(new NodeProcessor<Integer>(){
            @Override
            public boolean process(Rect bounds, Integer value) {
                assertEquals(map.remove(bounds), value);
                return true;
            }
        
        }));
        assertTrue(map.isEmpty());
        assertEquals(20, tree.size());
        SpatialNode<Integer> node = tree.getRoot();
        assertEquals(20, node.size());
        assertEquals(new RectBuilder(0, 0, 20, 1), node.bounds);
        assertTrue(node.isBranch());
        try {
            tree.addAll(tree);
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
        }
        RTree a = new RTree<>();
        a.add(Rect.valueOf(0, 0, 10, 10), 1);
        a.add(Rect.valueOf(10, 0, 20, 10), 2);
        RTree<Integer> b = new RTree<>();
        b.add(Rect.valueOf(20, 0, 30, 10), 3);
        b.add(Rect.valueOf(30, 0, 40, 10), 4);
        b.addAll(a);
        assertEquals("{itemBounds:[[20,0,30,10],[30,0,40,10],[0,0,10,10],[10,0,20,10]],itemValues:[3,4,1,2]}", b.root.toString());
    }

    @Test
    public void testAddSame() {
        RTree<Integer> tree = new RTree<>();
        Set<Integer> values = new HashSet<>();
        Rect bounds = Rect.valueOf(1, 3, 7, 13);
        for (int i = 0; i < 20; i++) {
            tree.add(bounds, i);
            values.add(i);
        }
        assertTrue(tree.getRoot().isLeaf());
        assertEquals(20, tree.size());
        for (int i = 0; i < 20; i++) {
            assertEquals(bounds, tree.getRoot().getItemBounds(i));
            assertTrue(values.remove(tree.getRoot().getItemValue(i)));
        }
    }

    @Test
    public void testAddNoArea() {
        RTree<Integer> tree = new RTree<>();
        Set<Integer> values = new HashSet<>();
        Rect bounds = Rect.valueOf(1, 3, 7, 3);
        for (int i = 0; i < 20; i++) {
            tree.add(bounds, i);
            values.add(i);
        }
        assertTrue(tree.getRoot().isLeaf());
        assertEquals(20, tree.size());
        for (int i = 0; i < 20; i++) {
            assertEquals(bounds, tree.getRoot().getItemBounds(i));
            assertTrue(values.remove(tree.getRoot().getItemValue(i)));
        }

        tree = new RTree<>();
        values = new HashSet<>();
        bounds = Rect.valueOf(1, 3, 1, 13);
        for (int i = 0; i < 20; i++) {
            tree.add(bounds, i);
            values.add(i);
        }
        assertTrue(tree.getRoot().isLeaf());
        assertEquals(20, tree.size());
        for (int i = 0; i < 20; i++) {
            assertEquals(bounds, tree.getRoot().getItemBounds(i));
            assertTrue(values.remove(tree.getRoot().getItemValue(i)));
        }
    }

    @Test
    public void testRemove_A() {
        RTree<String> tree = new RTree<>();
        Map<Rect, String> map = new HashMap<>();
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 50; j++) {
                Rect bounds = Rect.valueOf(i, j, i + 1, j + 1);
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
        final Map<Rect, Integer> map = new HashMap<>();
        for (int i = 0; i < 20; i++) {
            Rect bounds = Rect.valueOf(i, 0, i + 1, 1);
            if ((i < 5) || (i >= 15)) {
                map.put(bounds, i);
            }
            tree.add(bounds, i);
        }
        assertEquals(10, tree.removeOverlapping(Rect.valueOf(5, 0, 15, 1)));
        
        assertTrue(tree.forEach(new NodeProcessor<Integer>(){
            @Override
            public boolean process(Rect bounds, Integer value) {
                assertEquals(map.remove(bounds), value);
                return true;
            }
        
        }));
        assertTrue(map.isEmpty());
        assertEquals(10, tree.size());

        assertFalse(tree.remove(Rect.valueOf(3, 0, 4, 1), -3));
        assertFalse(tree.remove(Rect.valueOf(3, 0, 4, 0.9), 3));
        assertFalse(tree.remove(Rect.valueOf(3, 0, 3.9, 1), 3));
        assertFalse(tree.remove(Rect.valueOf(3, 0.1, 4, 1), 3));
        assertFalse(tree.remove(Rect.valueOf(3.1, 0, 4, 1), 3));
    }

    @Test
    public void testRemoveContained() {
        RTree<Integer> tree = new RTree<>();
        for (int i = 0; i < 20; i++) {
            Rect bounds = Rect.valueOf(i, 0, i + 1, 1);
            tree.add(bounds, i);
        }
        assertEquals(20, tree.removeOverlapping(Rect.valueOf(0, 0, 20, 1)));
        tree = new RTree<>();
        for (int i = 0; i < 20; i++) {
            Rect bounds = Rect.valueOf(i, 0, i + 1, 1);
            tree.add(bounds, i);
        }
        assertEquals(20, tree.removeInteracting(Rect.valueOf(0, 0, 20, 1)));
        tree = new RTree<>();
        for (int i = 0; i < 9; i++) {
            Rect bounds = Rect.valueOf(i, 0, i + 1, 1);
            tree.add(bounds, i);
        }
        assertEquals(9, tree.removeInteracting(Rect.valueOf(0, 0, 20, 1)));
    }

    @Test
    public void testRemoveMany() {
        RTree<String> tree = new RTree<>();
        Map<Rect, String> map = new HashMap<>();
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 50; j++) {
                Rect bounds = Rect.valueOf(i, j, i + 1, j + 1);
                String value = i + "_" + j;
                if (i > 1 && i < 49 && j > 1 && j < 49) {
                    map.put(bounds, value);
                }
                tree.add(bounds, value);
            }
        }
        assertEquals(2116, tree.removeOverlapping(Rect.valueOf(2, 2, 48, 48)));
        assertEquals(188, tree.removeInteracting(Rect.valueOf(2, 2, 48, 48)));
        assertEquals(196, tree.size());
        assertEquals(Rect.valueOf(0, 0, 50, 50), tree.getBounds());
    }

    @Test
    public void testSize() {
        assertEquals(2304, staticTree.sizeOverlapping(Rect.valueOf(1, 1, 49, 49)));
        assertEquals(2500, staticTree.sizeInteracting(Rect.valueOf(1, 1, 49, 49)));
    }

    @Test
    public void testTrySplit() {
        Rect rect = Rect.valueOf(1, 3, 1, 3);
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
        assertFalse(staticTree.isEmpty(Rect.valueOf(0, 0, 50, 50)));
        assertTrue(staticTree.isEmpty(Rect.valueOf(0, 50, 1, 51)));
        assertTrue(staticTree.isEmpty(Rect.valueOf(50, 0, 51, 1)));
        assertFalse(staticTree.isEmpty(Rect.valueOf(23, 29, 24, 30)));
        assertFalse(staticTree.isEmpty(Rect.valueOf(23, 29, 25, 31)));
        assertFalse(staticTree.isEmpty(Rect.valueOf(23.1, 29.1, 23.9, 29.9)));
    }

    @Test
    public void testIsDisjoint() {
        assertFalse(staticTree.isDisjoint(Rect.valueOf(0, 0, 50, 50)));
        assertTrue(staticTree.isDisjoint(Rect.valueOf(0, 51, 1, 52)));
        assertFalse(staticTree.isDisjoint(Rect.valueOf(0, 50, 1, 51)));
        assertTrue(staticTree.isDisjoint(Rect.valueOf(51, 0, 52, 1)));
        assertFalse(staticTree.isDisjoint(Rect.valueOf(50, 0, 51, 1)));
        assertFalse(staticTree.isDisjoint(Rect.valueOf(23, 29, 24, 30)));
        assertFalse(staticTree.isDisjoint(Rect.valueOf(23, 29, 25, 31)));
        assertFalse(staticTree.isDisjoint(Rect.valueOf(23.1, 29.1, 23.9, 29.9)));
    }

    @Test
    public void testContains() {
        for(int i = 0; i < 50; i++){
            for(int j = 0; j < 50; j++){
                assertTrue(staticTree.contains(Rect.valueOf(i, j, i+1, j+1), i+"_"+j));
            }
        }
        assertFalse(staticTree.contains(Rect.valueOf(0, 0, 1, 1), "0_1"));
        assertFalse(staticTree.contains(Rect.valueOf(0, 0, 1, 0.9), "0_0"));
        assertFalse(staticTree.contains(Rect.valueOf(0, 0, 0.9, 1), "0_0"));
        assertFalse(staticTree.contains(Rect.valueOf(0, 0.1, 1, 1), "0_0"));
        assertFalse(staticTree.contains(Rect.valueOf(0.1, 0, 1, 1), "0_0"));
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
                Rect bounds = Rect.valueOf(i, j, i + 1, j + 1);
                String value = i + "_" + j;
                tree.add(bounds, value);
            }
        }
        int depth = tree.getRoot().getDepth();
        tree.optimise();
        int optimisedDepth = tree.getRoot().getDepth();
        assertTrue(optimisedDepth < depth);
        assertEquals(Rect.valueOf(0,0,50,50), tree.getBounds());
        assertEquals(2500, tree.size());
        checkIntegrity(tree.getRoot());
    }

    @Test
    public void testForEach() {
        RTree<Integer> tree = new RTree<>();
        assertTrue(tree.forEach(new NodeProcessor<Integer>(){
            @Override
            public boolean process(Rect bounds, Integer value){
                throw new IllegalStateException();
            }
        }));

        final Map<Rect, String> map = new HashMap<>();
        for (int i = 1; i <= 7; i++) {
            for (int j = 2; j <= 13; j++) {
                map.put(Rect.valueOf(i, j, i + 1, j + 1), i + "_" + j);
            }
        }
        assertTrue(staticTree.forInteracting(Rect.valueOf(2, 3, 7, 13), new NodeProcessor<String>() {

            @Override
            public boolean process(Rect bounds, String value) {
                assertEquals(map.remove(bounds), value);
                return true;
            }

        }));
        assertTrue(map.isEmpty());
        
        for (int i = 2; i < 7; i++) {
            for (int j = 3; j < 13; j++) {
                map.put(Rect.valueOf(i, j, i + 1, j + 1), i + "_" + j);
            }
        }
        assertTrue(staticTree.forOverlapping(Rect.valueOf(2, 3, 7, 13), new NodeProcessor<String>() {

            @Override
            public boolean process(Rect bounds, String value) {
                assertEquals(map.remove(bounds), value);
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
        assertSame(a, RTree.getBestCandidateForAdd(Rect.valueOf(0, 0, 10, 10), a, b));
        assertSame(b, RTree.getBestCandidateForAdd(Rect.valueOf(20, 0, 30, 10), a, b));
    }    
}
