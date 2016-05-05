package org.jayson;

import java.beans.ConstructorProperties;
import java.beans.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jayson.parser.StaticFactory;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofarrell
 */
public class JaysonTest {

    @Test
    public void testParseStringArray() {
        String json = "[\"foo\",\"bar\"]";
        Jayson jayson = Jayson.getInstance();
        String[] array = jayson.parse(String[].class, json);
        assertTrue(Arrays.equals(new String[]{"foo", "bar"}, array));
    }

    @Test
    public void testParseIntArray() {
        Jayson jayson = Jayson.getInstance();

        String a = "[1,2,3]";
        int[] aResult = jayson.parse(int[].class, a);
        assertTrue(Arrays.equals(new int[]{1, 2, 3}, aResult));

        String b = "[1,null,3]";
        try {
            jayson.parse(int[].class, b);
            fail("Exception expected");
        } catch (JaysonException ex) {
        }
    }

    @Test
    public void testParseArrayList() {
        Jayson jayson = Jayson.getInstance();

        String a = "[1,2,3]";
        int[] aResult = jayson.parse(int[].class, a);
        assertTrue(Arrays.equals(new int[]{1, 2, 3}, aResult));

        String b = "[1,null,3]";
        try {
            jayson.parse(int[].class, b);
            fail("Exception expected");
        } catch (JaysonException ex) {
        }
    }

    @Test
    public void testBeanIO() {
        Jayson jayson = Jayson.getInstance();

        String a = "{id:1,title:\"A\",children:[{id:2,title:\"B\"},{id:3,title:\"C\",nonExistantProperty:[1,\"foo\",{}]}]}";
        Bean aResult = jayson.parse(Bean.class, a);
        Bean expected = new Bean(1, "A", new ArrayList<>(Arrays.asList(
                new Bean(2, "B", null),
                new Bean(3, "C", null)
        )));
        assertEquals(expected, aResult);
        String b = jayson.renderStr(aResult);
        assertEquals("{children:[{id:2,title:\"B\"},{id:3,title:\"C\"}],id:1,title:\"A\"}", b);
    }

    public static class Bean {

        private long id;
        private String title;
        private ArrayList<Bean> children;

        public Bean(long id, String title, ArrayList<Bean> children) {
            this.id = id;
            this.title = title;
            this.children = children;
        }

        public Bean() {
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public ArrayList<Bean> getChildren() {
            return children;
        }

        public void setChildren(ArrayList<Bean> children) {
            this.children = children;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 31 * hash + (int) (this.id ^ (this.id >>> 32));
            hash = 31 * hash + Objects.hashCode(this.title);
            hash = 31 * hash + Objects.hashCode(this.children);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Bean other = (Bean) obj;
            if (this.id != other.id) {
                return false;
            }
            if (!Objects.equals(this.title, other.title)) {
                return false;
            }
            if (!Objects.equals(this.children, other.children)) {
                return false;
            }
            return true;
        }

    }

    @Test
    public void testImmutableIO() {
        Jayson jayson = Jayson.getInstance();

        String a = "{id:1,title:\"A\",children:[{id:2,title:\"B\"},{id:3,title:\"C\",nonExistantProperty:[1,\"foo\",{}]}]}";
        Immutable aResult = jayson.parse(Immutable.class, a);
        Immutable expected = new Immutable(1, "A", Arrays.asList(
                new Immutable(2, "B", null),
                new Immutable(3, "C", null)
        ));
        assertEquals(expected, aResult);
        String b = jayson.renderStr(aResult);
        assertEquals("{id:1,children:[{id:2,title:\"B\"},{id:3,title:\"C\"}],title:\"A\"}", b);
    }

    public static final class Immutable {

        public final long id;
        public final String title;
        private final List<Immutable> children;

        @ConstructorProperties({"id", "title", "children"})
        public Immutable(long id, String title, List<Immutable> children) {
            this.id = id;
            this.title = title;
            this.children = (children == null) ? null : Collections.unmodifiableList(new ArrayList<>(children));
        }

        public String getTitle() {
            return title;
        }

        public List<Immutable> getChildren() {
            return children;
        }

        @Transient
        public int getNumChildren() {
            return children.size();
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 79 * hash + (int) (this.id ^ (this.id >>> 32));
            hash = 79 * hash + Objects.hashCode(this.title);
            hash = 79 * hash + Objects.hashCode(this.children);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Immutable other = (Immutable) obj;
            if (this.id != other.id) {
                return false;
            }
            if (!Objects.equals(this.title, other.title)) {
                return false;
            }
            if (!Objects.equals(this.children, other.children)) {
                return false;
            }
            return true;
        }

    }

    @Test
    public void testStaticInitialized() {
        Jayson jayson = Jayson.getInstance();

        String a = "{id:1,title:\"A\",nonExistantProperty:[1,\"foo\",{}]}]}";
        StaticInitialized aResult = jayson.parse(StaticInitialized.class, a);
        StaticInitialized expected = StaticInitialized.valueOf(1, "A");
        assertEquals(expected, aResult);
        String b = jayson.renderStr(aResult);
        assertEquals("{id:1,title:\"A\"}", b);
    }

    public static final class StaticInitialized {

        private final long id;
        private String title;

        private StaticInitialized(long id) {
            this.id = id;
        }

        @StaticFactory({"id", "title"})
        public static StaticInitialized valueOf(long id, String title) {
            StaticInitialized ret = new StaticInitialized(id);
            ret.setTitle(title);
            return ret;
        }

        public long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 43 * hash + (int) (this.id ^ (this.id >>> 32));
            hash = 43 * hash + Objects.hashCode(this.title);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final StaticInitialized other = (StaticInitialized) obj;
            if (this.id != other.id) {
                return false;
            }
            if (!Objects.equals(this.title, other.title)) {
                return false;
            }
            return true;
        }

    }
}
