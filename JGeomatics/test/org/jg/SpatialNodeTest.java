package org.jg;

import org.jg.SpatialNode;
import org.jg.Rect;
import org.jg.SpatialNode.NodeProcessor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tim.ofarrell
 */
public class SpatialNodeTest {

    @Test
    public void testConstructor_A() {
        Rect[] itemBounds = new Rect[]{
            new Rect(0, 0, 50, 50),
            new Rect(0, 50, 50, 100),
            new Rect(50, 0, 100, 50),
            new Rect(50, 50, 100, 100)};
        String[] itemValues = new String[]{"A", "B", "C", "D"};
        SpatialNode<String> node = new SpatialNode<>(itemBounds, itemValues);
        assertEquals(node.size, 4);
        itemValues[2] = "C";
        assertTrue(node.contains(itemBounds[2], "C"));
        assertTrue(node.containsValue("C"));
        assertFalse(node.contains(itemBounds[2], "E"));
        assertFalse(node.containsValue("E"));
        itemBounds[0].reset();
        try {
            node = new SpatialNode<>(itemBounds, itemValues);
            fail("Exception expected!");
        } catch (IllegalArgumentException ex) {
        }
        try {
            node = new SpatialNode<>(itemBounds, new String[]{"A", "B", "C"});
            fail("Exception expected!");
        } catch (IllegalArgumentException ex) {
        }
        try {
            node = new SpatialNode<>(itemBounds, new String[]{"A", "B", "C", "D", "E"});
            fail("Exception expected!");
        } catch (IllegalArgumentException ex) {
        }
        itemBounds[0] = null;
        try {
            node = new SpatialNode<>(itemBounds, itemValues);
            fail("Exception expected!");
        } catch (NullPointerException ex) {
        }
        try {
            node = new SpatialNode<>((Rect[])null, itemValues);
            fail("Exception expected!");
        } catch (NullPointerException ex) {
        }
        assertTrue(node.contains(new Rect(0, 0, 50, 50), "A"));
        assertTrue(node.containsValue("A"));

    }

    SpatialNode<String> createABC() {
        SpatialNode<String> a = new SpatialNode<>(new Rect[]{new Rect(0, 0, 30, 40), new Rect(0, 40, 30, 80)}, new String[]{"A", "B"});
        SpatialNode<String> b = new SpatialNode<>(new Rect[]{new Rect(30, 0, 60, 40), new Rect(30, 40, 60, 80), new Rect(20, 30, 40, 50)}, new String[]{"C", "D", "E"});
        SpatialNode<String> c = new SpatialNode(a, b);
        return c;
    }

    @Test
    public void testConstructor_B() {
        SpatialNode<String> a = new SpatialNode<>(new Rect[]{new Rect(0, 0, 30, 40), new Rect(0, 40, 30, 80)}, new String[]{"A", "B"});
        SpatialNode<String> b = new SpatialNode<>(new Rect[]{new Rect(30, 0, 60, 40), new Rect(30, 40, 60, 80), new Rect(20, 30, 40, 50)}, new String[]{"C", "D", "E"});
        assertEquals(2, a.size);
        SpatialNode<String> c = new SpatialNode(a, b);
        try {
            c = new SpatialNode<>(new SpatialNode(), null);
            fail("Exception expected!");
        } catch (NullPointerException ex) {
        }
        try {
            c = new SpatialNode<>(null, new SpatialNode());
            fail("Exception expected!");
        } catch (NullPointerException ex) {
        }
        assertEquals(a, c.a);
        assertEquals(b, c.b);
        assertEquals(5, c.size());
        assertEquals(new Rect(0, 0, 60, 80), c.getBounds(new Rect()));
    }

    
    @Test
    public void testConstructor_C() {
        double[] itemBounds = new double[]{
            0, 0, 50, 50,
            0, 50, 50, 100,
            50, 0, 100, 50,
            50, 50, 100, 100};
        String[] itemValues = new String[]{"A", "B", "C", "D"};
        SpatialNode<String> node = new SpatialNode<>(itemBounds, itemValues);
        assertEquals(node.size, 4);
        itemValues[2] = "C";
        assertTrue(node.contains(new Rect(50, 0, 100, 50), "C"));
        assertTrue(node.containsValue("C"));
        assertFalse(node.contains(new Rect(50, 0, 100, 50), "E"));
        assertFalse(node.containsValue("E"));
        try {
            node = new SpatialNode<>(itemBounds, new String[]{"A", "B", "C"});
            fail("Exception expected!");
        } catch (IllegalArgumentException ex) {
        }
        try {
            node = new SpatialNode<>(itemBounds, new String[]{"A", "B", "C", "D", "E"});
            fail("Exception expected!");
        } catch (IllegalArgumentException ex) {
        }
        itemBounds[0] = Double.POSITIVE_INFINITY;
        try {
            node = new SpatialNode<>(itemBounds, itemValues);
            fail("Exception expected!");
        } catch (IllegalArgumentException ex) {
        }
        try {
            node = new SpatialNode<>((double[])null, itemValues);
            fail("Exception expected!");
        } catch (NullPointerException ex) {
        }
        assertTrue(node.contains(new Rect(0, 0, 50, 50), "A"));
        assertTrue(node.containsValue("A"));

    }

    
    @Test
    public void testGetBounds() {
        SpatialNode<String> c = createABC();
        assertEquals(new Rect(0, 0, 60, 80), c.getBounds(new Rect()));
        assertEquals(new Rect(0, 0, 30, 80), c.a.getBounds(new Rect()));
        assertEquals(new Rect(20, 0, 60, 80), c.b.getBounds(new Rect()));
    }

    @Test
    public void testSize_0args() {
        SpatialNode<String> c = createABC();
        assertEquals(5, c.size());
        assertEquals(2, c.a.size());
        assertEquals(3, c.b.size());
    }

    @Test
    public void testSizeInterracting() {
        SpatialNode<String> c = createABC();
        assertEquals(5, c.sizeInteracting(new Rect(0, 0, 60, 80)));
        assertEquals(5, c.sizeInteracting(new Rect(-10, -20, 70, 90)));
        assertEquals(5, c.sizeInteracting(new Rect(20, 30, 40, 50)));
        assertEquals(2, c.sizeInteracting(new Rect(0, 0, 60, 10)));
        assertEquals(2, c.sizeInteracting(new Rect(0, 0, 10, 80)));
        assertEquals(2, c.sizeInteracting(new Rect(0, 0, 60, 0)));
        assertEquals(2, c.sizeInteracting(new Rect(0, 0, 0, 80)));
        assertEquals(1, c.sizeInteracting(new Rect(60, 80, 70, 90)));
        assertEquals(0, c.sizeInteracting(new Rect(61, 80, 70, 90)));
    }

    @Test
    public void testSizeOverlapping() {
        SpatialNode<String> c = createABC();
        assertEquals(5, c.sizeOverlapping(new Rect(0, 0, 60, 80)));
        assertEquals(5, c.sizeOverlapping(new Rect(-10, -20, 70, 90)));
        assertEquals(5, c.sizeOverlapping(new Rect(20, 30, 40, 50)));
        assertEquals(2, c.sizeOverlapping(new Rect(0, 0, 60, 10)));
        assertEquals(2, c.sizeOverlapping(new Rect(0, 0, 10, 80)));
        assertEquals(0, c.sizeOverlapping(new Rect(0, 0, 60, 0)));
        assertEquals(0, c.sizeOverlapping(new Rect(0, 0, 0, 80)));
        assertEquals(0, c.sizeOverlapping(new Rect(60, 80, 70, 90)));
        assertEquals(0, c.sizeOverlapping(new Rect(61, 80, 70, 90)));
    }

    @Test
    public void testIsEmpty() {
        assertTrue(new SpatialNode<>(new Rect[0], new String[0]).isEmpty());
        SpatialNode<String> c = createABC();
        assertFalse(c.isEmpty());
        assertFalse(c.a.isEmpty());
        assertFalse(c.b.isEmpty());

        assertFalse(c.isEmpty(new Rect(0, 0, 60, 80)));
        assertFalse(c.isEmpty(new Rect(-10, -20, 70, 90)));
        assertFalse(c.isEmpty(new Rect(20, 30, 40, 50)));
        assertFalse(c.isEmpty(new Rect(0, 0, 60, 10)));
        assertFalse(c.isEmpty(new Rect(0, 0, 10, 80)));
        assertTrue(c.isEmpty(new Rect(0, 0, 60, 0)));
        assertTrue(c.isEmpty(new Rect(0, 0, 0, 80)));
        assertTrue(c.isEmpty(new Rect(60, 80, 70, 90)));
        assertTrue(c.isEmpty(new Rect(61, 80, 70, 90)));

        assertFalse(c.isEmpty(new Rect(0, 0, 29, 80)));
        assertFalse(c.isEmpty(new Rect(31, 0, 60, 80)));

        SpatialNode<String> a = new SpatialNode<>();
        a.bounds.set(10, 20, 30, 40);
        assertTrue(a.isEmpty(new Rect(0, 0, 50, 60)));

        a = new SpatialNode<>(new Rect[]{new Rect(0, 0, 20, 20), new Rect(40, 40, 60, 60)}, new String[]{"A", "B"});
        SpatialNode<String> b = new SpatialNode<>(new Rect[]{new Rect(60, 60, 80, 80), new Rect(80, 80, 100, 100)}, new String[]{"C", "D"});
        c = new SpatialNode(a, b);
        assertTrue(c.isEmpty(new Rect(1, 21, 2, 22)));
        assertTrue(c.isEmpty(new Rect(1, 20, 2, 21)));
        assertFalse(c.isEmpty(new Rect(1, 19, 2, 22)));
        assertTrue(c.isEmpty(new Rect(58, 38, 59, 39)));
        assertTrue(c.isEmpty(new Rect(58, 39, 59, 40)));
        assertFalse(c.isEmpty(new Rect(58, 38, 59, 41)));       
    }

    @Test
    public void testIsDisjoint() {
        SpatialNode<String> c = createABC();
        assertFalse(c.isDisjoint(new Rect(0, 0, 60, 80)));
        assertFalse(c.isDisjoint(new Rect(-10, -20, 70, 90)));
        assertFalse(c.isDisjoint(new Rect(20, 30, 40, 50)));
        assertFalse(c.isDisjoint(new Rect(0, 0, 60, 10)));
        assertFalse(c.isDisjoint(new Rect(0, 0, 10, 80)));
        assertFalse(c.isDisjoint(new Rect(0, 0, 60, 0)));
        assertFalse(c.isDisjoint(new Rect(0, 0, 0, 80)));
        assertFalse(c.isDisjoint(new Rect(60, 80, 70, 90)));
        assertTrue(c.isDisjoint(new Rect(61, 80, 70, 90)));

        assertFalse(c.isDisjoint(new Rect(0, 0, 29, 80)));
        assertFalse(c.isDisjoint(new Rect(31, 0, 60, 80)));

        SpatialNode<String> a = new SpatialNode<>();
        a.bounds.set(10, 20, 30, 40);
        assertTrue(a.isDisjoint(new Rect(0, 0, 50, 60)));

        a = new SpatialNode<>(new Rect[]{new Rect(0, 0, 20, 20), new Rect(40, 40, 60, 60)}, new String[]{"A", "B"});
        SpatialNode<String> b = new SpatialNode<>(new Rect[]{new Rect(60, 60, 80, 80), new Rect(80, 80, 100, 100)}, new String[]{"C", "D"});
        c = new SpatialNode(a, b);
        assertTrue(c.isDisjoint(new Rect(1, 21, 2, 22)));
        assertFalse(c.isDisjoint(new Rect(1, 19, 2, 22)));
        assertTrue(c.isDisjoint(new Rect(58, 38, 59, 39)));
        assertFalse(c.isDisjoint(new Rect(58, 38, 59, 41)));
    }

    @Test
    public void testGet_SpatialNodeNodeProcessor() {
        SpatialNode<String> c = createABC();
        final Map<Rect, String> map = new HashMap();
        map.put(new Rect(0, 0, 30, 40), "A");
        map.put(new Rect(0, 40, 30, 80), "B");
        map.put(new Rect(30, 0, 60, 40), "C");
        map.put(new Rect(30, 40, 60, 80), "D");
        map.put(new Rect(20, 30, 40, 50), "E");
        assertTrue(c.get(new NodeProcessor<String>() {

            @Override
            public boolean process(SpatialNode<String> leaf, int index) {
                Rect bounds = leaf.getItemBounds(index, new Rect());
                String value = leaf.getItemValue(index);
                assertEquals(map.remove(bounds), value);
                return true;
            }

        }));
        assertTrue(map.isEmpty());
        assertFalse(c.get(new NodeProcessor<String>() {

            boolean done;

            @Override
            public boolean process(SpatialNode<String> leaf, int index) {
                if (done) {
                    fail("Done");
                }
                done = true;
                return false;
            }

        }));
        assertFalse(c.get(new NodeProcessor<String>() {

            int count;

            @Override
            public boolean process(SpatialNode<String> leaf, int index) {
                return (count++ < 3);
            }

        }));
        try {
            c.get(null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testGetInteracting() {
        SpatialNode<String> c = createABC();
        assertTrue(c.getInteracting(new Rect(70, 80, 90, 100), new NodeProcessor<String>() {
            @Override
            public boolean process(SpatialNode<String> leaf, int index) {
                throw new IllegalStateException("Execution flow should not reach this point!");
            }
        }));
        final Map<Rect, String> map = new HashMap();
        map.put(new Rect(0, 0, 30, 40), "A");
        map.put(new Rect(0, 40, 30, 80), "B");
        map.put(new Rect(30, 0, 60, 40), "C");
        map.put(new Rect(30, 40, 60, 80), "D");
        map.put(new Rect(20, 30, 40, 50), "E");
        assertTrue(c.getInteracting(new Rect(0, 0, 30, 80), new NodeProcessor<String>() {

            @Override
            public boolean process(SpatialNode<String> leaf, int index) {
                Rect bounds = leaf.getItemBounds(index, new Rect());
                String value = leaf.getItemValue(index);
                assertEquals(map.remove(bounds), value);
                return true;
            }

        }));
        assertTrue(map.isEmpty());

        map.put(new Rect(30, 0, 60, 40), "C");
        map.put(new Rect(30, 40, 60, 80), "D");
        assertFalse(c.getInteracting(new Rect(31, 0, 60, 80), new NodeProcessor<String>() {

            @Override
            public boolean process(SpatialNode<String> leaf, int index) {
                Rect bounds = leaf.getItemBounds(index, new Rect());
                String value = leaf.getItemValue(index);
                return (map.remove(bounds) != null);
            }

        }));
        assertTrue(map.isEmpty());

        assertFalse(c.getInteracting(new Rect(0, 0, 30, 80), new NodeProcessor<String>() {

            boolean done;

            @Override
            public boolean process(SpatialNode<String> leaf, int index) {
                if (done) {
                    fail("Done");
                }
                done = true;
                return false;
            }

        }));
        try {
            c.getInteracting(null, new NodeProcessor<String>() {

                @Override
                public boolean process(SpatialNode<String> leaf, int index) {
                    throw new IllegalStateException();
                }

            });
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            c.getInteracting(new Rect(0, 0, 30, 80), null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testGetInteracting2() {
        SpatialNode<String> a = new SpatialNode<>(new Rect[]{new Rect(0, 0, 20, 20), new Rect(80, 80, 100, 100)}, new String[]{"A", "B"});
        a.getInteracting(new Rect(40, 50, 60, 70), new NodeProcessor<String>() {

            @Override
            public boolean process(SpatialNode<String> leaf, int index) {
                throw new IllegalStateException("Should not get here");
            }

        });
    }

    
    @Test
    public void testGetOverlapping() {
        SpatialNode<String> c = createABC();
        assertTrue(c.getOverlapping(new Rect(70, 80, 90, 100), new NodeProcessor<String>() {
            @Override
            public boolean process(SpatialNode<String> leaf, int index) {
                throw new IllegalStateException("Execution flow should not reach this point!");
            }
        }));
        final Map<Rect, String> map = new HashMap();
        map.put(new Rect(0, 0, 30, 40), "A");
        map.put(new Rect(0, 40, 30, 80), "B");
        map.put(new Rect(20, 30, 40, 50), "E");
        assertTrue(c.getOverlapping(new Rect(0, 0, 30, 80), new NodeProcessor<String>() {

            @Override
            public boolean process(SpatialNode<String> leaf, int index) {
                Rect bounds = leaf.getItemBounds(index, new Rect());
                String value = leaf.getItemValue(index);
                assertEquals(map.remove(bounds), value);
                return true;
            }

        }));
        assertTrue(map.isEmpty());

        map.put(new Rect(30, 0, 60, 40), "C");
        map.put(new Rect(30, 40, 60, 80), "D");
        assertFalse(c.getOverlapping(new Rect(31, 0, 60, 80), new NodeProcessor<String>() {

            @Override
            public boolean process(SpatialNode<String> leaf, int index) {
                Rect bounds = leaf.getItemBounds(index, new Rect());
                String value = leaf.getItemValue(index);
                return (map.remove(bounds) != null);
            }

        }));
        assertTrue(map.isEmpty());

        assertFalse(c.getOverlapping(new Rect(0, 0, 30, 80), new NodeProcessor<String>() {

            boolean done;

            @Override
            public boolean process(SpatialNode<String> leaf, int index) {
                if (done) {
                    fail("Done");
                }
                done = true;
                return false;
            }

        }));
        try {
            c.getOverlapping(null, new NodeProcessor<String>() {

                @Override
                public boolean process(SpatialNode<String> leaf, int index) {
                    throw new IllegalStateException();
                }

            });
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        try {
            c.getOverlapping(new Rect(0, 0, 30, 80), null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testGetOverlapping2() {
        SpatialNode<String> a = new SpatialNode<>(new Rect[]{new Rect(0, 0, 20, 20), new Rect(80, 80, 100, 100)}, new String[]{"A", "B"});
        a.getOverlapping(new Rect(40, 50, 60, 70), new NodeProcessor<String>() {

            @Override
            public boolean process(SpatialNode<String> leaf, int index) {
                throw new IllegalStateException("Should not get here");
            }

        });
    }
    
    @Test
    public void testContains() {
        SpatialNode<String> c = createABC();
        assertTrue(c.contains(new Rect(0, 0, 30, 40), "A"));
        assertFalse(c.contains(new Rect(0, 0, 30, 41), "A"));
        assertFalse(c.contains(new Rect(0, 0, 31, 41), "A"));
        assertFalse(c.contains(new Rect(0, -1, 31, 41), "A"));
        assertFalse(c.contains(new Rect(-1, -1, 31, 41), "A"));
        assertFalse(c.contains(new Rect(0, 40, 30, 80), "A"));
        assertTrue(c.contains(new Rect(30, 40, 60, 80), "D"));
        
        assertFalse(c.contains(new Rect(0, 0, 30, 40), "B"));
        assertFalse(c.a.contains(new Rect(0, 0, 30, 39), "A"));
        assertFalse(c.a.contains(new Rect(0, 0, 29, 40), "A"));
        assertFalse(c.a.contains(new Rect(0, 1, 30, 40), "A"));
        assertFalse(c.a.contains(new Rect(1, 0, 30, 40), "A"));
        
    }

    @Test
    public void testContainsValue() {
        SpatialNode<String> c = createABC();
        assertTrue(c.containsValue("A"));
        assertTrue(c.containsValue("B"));
        assertTrue(c.containsValue("C"));
        assertTrue(c.containsValue("D"));
        assertTrue(c.containsValue("E"));
        assertFalse(c.containsValue("F"));
    }

    @Test
    public void testGetItemBounds() {
        SpatialNode<String> c = createABC();
        try {
            c.getItemBounds(0, new Rect());
            fail("Exception expected!");
        } catch (IllegalStateException ex) {
        }
        try {
            c.a.getItemBounds(2, new Rect());
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }

        try {
            c.a.getItemBounds(0, null);
            fail("Exception expected");
        } catch (NullPointerException ex) {
        }
        assertEquals(new Rect(0, 0, 30, 40), c.a.getItemBounds(0, new Rect()));
        assertEquals(new Rect(0, 40, 30, 80), c.a.getItemBounds(1, new Rect()));
        assertEquals(new Rect(30, 0, 60, 40), c.b.getItemBounds(0, new Rect()));
        assertEquals(new Rect(30, 40, 60, 80), c.b.getItemBounds(1, new Rect()));
        assertEquals(new Rect(20, 30, 40, 50), c.b.getItemBounds(2, new Rect()));
        Rect rect = new Rect();
        assertSame(rect, c.b.getItemBounds(2, rect));
    }

    @Test
    public void testGetItemValue() {
        SpatialNode<String> c = createABC();
        try {
            c.getItemValue(0);
            fail("Exception expected!");
        } catch (IllegalStateException ex) {
        }
        try {
            c.a.getItemValue(2);
            fail("Exception expected");
        } catch (IndexOutOfBoundsException ex) {
        }
        assertEquals("A", c.a.getItemValue(0));
        assertEquals("B", c.a.getItemValue(1));
        assertEquals("C", c.b.getItemValue(0));
        assertEquals("D", c.b.getItemValue(1));
        assertEquals("E", c.b.getItemValue(2));
    }

    @Test
    public void testGetA() {
        SpatialNode<Integer> a = new SpatialNode<>();
        SpatialNode<Integer> b = new SpatialNode<>();
        SpatialNode<Integer> c = new SpatialNode<>(a, b);
        assertSame(a, c.getA());
        assertNull(a.getA());
        assertNull(b.getA());
    }

    @Test
    public void testGetB() {
        SpatialNode<Integer> a = new SpatialNode<>();
        SpatialNode<Integer> b = new SpatialNode<>();
        SpatialNode<Integer> c = new SpatialNode<>(a, b);
        assertSame(b, c.getB());
        assertNull(a.getB());
        assertNull(b.getB());
    }

    @Test
    public void testIsBranch() {
        SpatialNode<String> c = createABC();
        assertTrue(c.isBranch());
        assertFalse(c.a.isBranch());
        assertFalse(c.b.isBranch());
    }

    @Test
    public void testIsLeaf() {
        SpatialNode<String> c = createABC();
        assertFalse(c.isLeaf());
        assertTrue(c.a.isLeaf());
        assertTrue(c.b.isLeaf());
    }
    
    @Test
    public void testGetDepth() {
        SpatialNode<String> c = createABC();
        assertEquals(2, c.getDepth());
        assertEquals(1, c.a.getDepth());
        assertEquals(1, c.b.getDepth());
    }

    @Test
    public void testExternalize() throws Exception {
        SpatialNode<String> a = createABC();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bout)) {
            out.writeObject(a);
        }
        SpatialNode<String> b;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()))) {
            b = (SpatialNode<String>) in.readObject();
        }
        assertEquals(a, b);
    }

    @Test
    public void testHashCode() {
        SpatialNode<String> a = createABC();
        SpatialNode<String> b = createABC();
        assertEquals(a.hashCode(), b.hashCode());
        assertFalse(a.hashCode() == new SpatialNode<String>().hashCode());
    }

    @Test
    public void testEquals() {
        SpatialNode<String> a = createABC();
        SpatialNode<String> b = createABC();
        assertEquals(a, b);
        assertFalse(a.equals(new SpatialNode<String>()));
        assertFalse(a.equals(""));
        a = new SpatialNode<>(new Rect[]{new Rect(0,0,100,100)}, new String[]{"A"});
        b = new SpatialNode<>(new Rect[]{new Rect(0,0,100,50),new Rect(0,50,100,100)}, new String[]{"A","B"});
        assertFalse(a.equals(b));
        a = new SpatialNode<>(new Rect[]{new Rect(0,0,100,100)}, new String[]{"A"});
        b = new SpatialNode<>(new Rect[]{new Rect(0,0,100,50)}, new String[]{"A"});
        assertFalse(a.equals(b));
        a = new SpatialNode<>(new Rect[]{new Rect(0,0,100,50),new Rect(0,50,100,100)}, new String[]{"A","B"});
        b = new SpatialNode<>(new SpatialNode<>(new Rect[]{new Rect(0,0,100,50)}, new String[]{"A"}),
                new SpatialNode<>(new Rect[]{new Rect(0,50,100,100)}, new String[]{"B"}));
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        a = new SpatialNode<>(new Rect[]{new Rect(0,0,100,50),new Rect(0,50,100,100)}, new String[]{"A","B"});
        b = new SpatialNode<>(new Rect[]{new Rect(0,0,50,100),new Rect(50,0,100,100)}, new String[]{"A","B"});
        assertFalse(a.equals(b));
        a = new SpatialNode<>(new Rect[]{new Rect(0,0,100,50),new Rect(0,50,100,100)}, new String[]{"A","B"});
        b = new SpatialNode<>(new Rect[]{new Rect(0,0,100,50),new Rect(0,50,100,100)}, new String[]{"A","C"});
        assertFalse(a.equals(b));
        a = new SpatialNode<>(new SpatialNode<>(new Rect[]{new Rect(0,0,100,50)}, new String[]{"A"}),
                new SpatialNode<>(new Rect[]{new Rect(0,50,100,100)}, new String[]{"B"}));
        b = new SpatialNode<>(new SpatialNode<>(new Rect[]{new Rect(0,0,100,50)}, new String[]{"A"}),
                new SpatialNode<>(new Rect[]{new Rect(0,50,100,100)}, new String[]{"C"}));
        assertFalse(a.equals(b));
        b = new SpatialNode<>(new SpatialNode<>(new Rect[]{new Rect(0,0,100,50)}, new String[]{"C"}),
                new SpatialNode<>(new Rect[]{new Rect(0,50,100,100)}, new String[]{"B"}));
        assertFalse(a.equals(b));
    }

    @Test
    public void testToString_0args() {
        assertEquals("{a:{itemBounds:[0,0,30,40,0,40,30,80],itemValues:[A,B]},b:{itemBounds:[30,0,60,40,30,40,60,80,20,30,40,50],itemValues:[C,D,E]}}", createABC().toString());
    }

    @Test
    public void testToString_Appendable() {
        StringBuilder str = new StringBuilder();
        createABC().toString(str);
        assertEquals("{a:{itemBounds:[0,0,30,40,0,40,30,80],itemValues:[A,B]},b:{itemBounds:[30,0,60,40,30,40,60,80,20,30,40,50],itemValues:[C,D,E]}}", str.toString());
        try {
            createABC().toString(new Appendable() {

                @Override
                public Appendable append(CharSequence csq) throws IOException {
                    throw new IOException();
                }

                @Override
                public Appendable append(CharSequence csq, int start, int end) throws IOException {
                    throw new IOException();
                }

                @Override
                public Appendable append(char c) throws IOException {
                    throw new IOException();
                }

            });
            fail("Excception expected");
        } catch (IllegalStateException ex) {
        }
    }

    @Test
    public void testClone() {
        SpatialNode<String> a = createABC();
        SpatialNode<String> b = a.clone();
        assertNotSame(a, b);
        assertEquals(a, b);
    }
}
