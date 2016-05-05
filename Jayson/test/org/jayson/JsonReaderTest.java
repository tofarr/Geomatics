package org.jayson;

import org.jayson.JaysonReader;
import org.jayson.JaysonType;
import org.jayson.JaysonException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar
 */
public class JsonReaderTest {

    @Test
    public void testValidJson_A() {
        JaysonReader reader = new JaysonReader(new StringReader("{foo:[1,2,3],bar:true,zap:{\"bang\":\"pop\",\"piff\":\"\\\"paff\\\"\"}}"));
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        assertEquals(JaysonType.NAME, reader.next());
        assertEquals("foo", reader.str());
        assertEquals(JaysonType.BEGIN_ARRAY, reader.next());
        assertEquals(1, reader.nextNum(), 0.001);
        assertEquals(2, reader.nextNum(), 0.001);
        assertEquals(3, reader.nextNum(), 0.001);
        assertEquals(JaysonType.END_ARRAY, reader.next());
        assertEquals("bar", reader.nextStr());
        assertEquals(true, reader.nextBool());
        assertEquals("zap", reader.nextStr());
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        assertEquals("bang", reader.nextStr());
        assertEquals(JaysonType.STRING, reader.next());
        assertEquals("pop", reader.str());
        assertEquals("piff", reader.nextStr());
        assertEquals("\"paff\"", reader.nextStr());
        assertEquals(JaysonType.END_OBJECT, reader.next());
        assertEquals(JaysonType.END_OBJECT, reader.next());
        assertNull(reader.next());
    }

    @Test
    public void testValidJson_Whitespace() {
        JaysonReader reader = new JaysonReader(new StringReader(" {  foo :\r\n[1, 2,\t3]  , bar:\rtrue,zap:{\"bang\"  :\t\"pop\"} } "));
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        assertEquals(JaysonType.NAME, reader.next());
        assertEquals("foo", reader.str());
        assertEquals(JaysonType.BEGIN_ARRAY, reader.next());
        assertEquals(1, reader.nextNum(), 0.001);
        assertEquals(2, reader.nextNum(), 0.001);
        assertEquals(3, reader.nextNum(), 0.001);
        assertEquals(JaysonType.END_ARRAY, reader.next());
        assertEquals("bar", reader.nextStr());
        assertEquals(true, reader.nextBool());
        assertEquals("zap", reader.nextStr());
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        assertEquals("bang", reader.nextStr());
        assertEquals(JaysonType.STRING, reader.next());
        assertEquals("pop", reader.str());
        assertEquals(JaysonType.END_OBJECT, reader.next());
        assertEquals(JaysonType.END_OBJECT, reader.next());
        assertNull(reader.next());
    }

    @Test
    public void testWrongBracketOrder_A() {
        JaysonReader reader = new JaysonReader(new StringReader("{foo:[2,3}]"));
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        assertEquals(JaysonType.NAME, reader.next());
        assertEquals("foo", reader.str());
        assertEquals(JaysonType.BEGIN_ARRAY, reader.next());
        assertEquals(2, reader.nextNum(), 0.001);
        assertEquals(3, reader.nextNum(), 0.001);
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testWrongBracketOrder_B() {
        JaysonReader reader = new JaysonReader(new StringReader("[{foo:true]}"));
        assertEquals(JaysonType.BEGIN_ARRAY, reader.next());
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        assertTrue(reader.nextBool());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testObjectMissingComma() {
        JaysonReader reader = new JaysonReader(new StringReader("{foo:true\"bar\":false}"));
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        try {
            assertTrue(reader.nextBool());
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testStringArray() {
        JaysonReader reader = new JaysonReader(new StringReader("[\"foo\",\"bar\"]"));
        assertEquals(JaysonType.BEGIN_ARRAY, reader.next());
        assertEquals("foo", reader.nextStr());
        assertEquals("bar", reader.nextStr());
        assertEquals(JaysonType.END_ARRAY, reader.next());
    }

    @Test
    public void testUNknownUncommentedString() {
        JaysonReader reader = new JaysonReader(new StringReader("[trueb]"));
        assertEquals(JaysonType.BEGIN_ARRAY, reader.next());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testUncommentedStrings() {
        JaysonReader reader = new JaysonReader(new StringReader("[true,false,null,10,9.6,1.0E+24]"));
        assertEquals(JaysonType.BEGIN_ARRAY, reader.next());
        assertTrue(reader.nextBool());
        assertFalse(reader.nextBool());
        assertEquals(JaysonType.NULL, reader.next());
        assertEquals(10, reader.nextNum(), 0.01);
        assertEquals(9.6, reader.nextNum(), 0.01);
        assertEquals(1.0E+24, reader.nextNum(), 0.01);
        assertEquals(JaysonType.END_ARRAY, reader.next());
    }

    @Test
    public void testArrayLeadingComma() {
        JaysonReader reader = new JaysonReader(new StringReader("[,2,3]"));
        assertEquals(JaysonType.BEGIN_ARRAY, reader.next());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testObjectLeadingComma() {
        JaysonReader reader = new JaysonReader(new StringReader("{,foo:2}"));
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testArrayTrailingComma() {
        JaysonReader reader = new JaysonReader(new StringReader("[2,3,]"));
        assertEquals(JaysonType.BEGIN_ARRAY, reader.next());
        assertEquals(2, reader.nextNum(), 0.001);
        assertEquals(3, reader.nextNum(), 0.001);
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testObjectTrialingComma() {
        JaysonReader reader = new JaysonReader(new StringReader("{foo:2,}"));
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        assertEquals(2, reader.nextNum(), 0.001);
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testObjectNoColonComma() {
        JaysonReader reader = new JaysonReader(new StringReader("{foo,2}"));
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testObjectCommaAfterColon() {
        JaysonReader reader = new JaysonReader(new StringReader("{foo:,2}"));
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testDoubleComma() {
        JaysonReader reader = new JaysonReader(new StringReader("[1,,2]"));
        assertEquals(JaysonType.BEGIN_ARRAY, reader.next());
        assertEquals(1, reader.nextNum(), 0.01);
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testTopLevelComma() {
        JaysonReader reader = new JaysonReader(new StringReader("10,4"));
        assertEquals(10, reader.nextNum(), 0.001);
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }
    
    @Test
    public void testTopLevelNum() {
        JaysonReader reader = new JaysonReader(new StringReader("10.4"));
        assertEquals(10.4, reader.nextNum(), 0.001);
        assertNull(reader.next());
    }

    @Test
    public void testTopLevelBool() {
        JaysonReader reader = new JaysonReader(new StringReader("false"));
        assertFalse(reader.nextBool());
        assertNull(reader.next());
    }

    @Test
    public void testTopLevelNull() {
        JaysonReader reader = new JaysonReader(new StringReader("null"));
        assertEquals(JaysonType.NULL, reader.next());
        assertNull(reader.next());
    }

    @Test
    public void testTopLevelStr() {
        JaysonReader reader = new JaysonReader(new StringReader("\"\\\"ping\\\"\""));
        assertEquals("\"ping\"", reader.nextStr());
        assertNull(reader.next());
    }

    @Test
    public void testUnexpectedEOF_A() {
        JaysonReader reader = new JaysonReader(new StringReader("{foo:2"));
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testUnexpectedEOF_B() {
        JaysonReader reader = new JaysonReader(new StringReader("{foo"));
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testUnexpectedEOF_C() {
        JaysonReader reader = new JaysonReader(new StringReader("[tru"));
        assertEquals(JaysonType.BEGIN_ARRAY, reader.next());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testUnexpectedEOF_D() {
        JaysonReader reader = new JaysonReader(new StringReader("tru"));
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testUnexpectedEOF_E() {
        JaysonReader reader = new JaysonReader(new StringReader("\"pin"));
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testUnexpectedEOF_F() {
        JaysonReader reader = new JaysonReader(new StringReader("\""));
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testUnexpectedEOF_G() {
        JaysonReader reader = new JaysonReader(new StringReader("\"\\"));
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testUnexpectedEOF_H() {
        JaysonReader reader = new JaysonReader(new StringReader("/"));
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testNextStr() {
        JaysonReader reader = new JaysonReader(new StringReader("{foo:\"bar\"}"));
        try {
            reader.nextStr();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testNextNum() {
        JaysonReader reader = new JaysonReader(new StringReader("{foo:\"bar\"}"));
        reader.next();
        reader.next();
        try {
            reader.nextNum();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testNextBool() {
        JaysonReader reader = new JaysonReader(new StringReader("{foo:\"bar\"}"));
        try {
            reader.nextBool();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testCommentDoubleSlash() {
        JaysonReader reader = new JaysonReader(new StringReader("{\"foo\": // This is a comment and will be ignored\r\n\"bar\"}"));
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        assertEquals("bar", reader.nextStr());
        assertEquals(JaysonType.END_OBJECT, reader.next());
    }

    @Test
    public void testCommentDoubleSlashEOF() {
        JaysonReader reader = new JaysonReader(new StringReader("{\"foo\": // This is a comment and will be ignored"));
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testComment() {
        JaysonReader reader = new JaysonReader(new StringReader("{\"foo\": /* This is a comment / * and will be ignored */ \"bar\"}"));
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        assertEquals("bar", reader.nextStr());
        assertEquals(JaysonType.END_OBJECT, reader.next());
    }

    @Test
    public void testCommentEOF() {
        JaysonReader reader = new JaysonReader(new StringReader("{\"foo\": /* This is a comment and will be ignored"));
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testCommentEOF_B() {
        JaysonReader reader = new JaysonReader(new StringReader("{\"foo\": /* This is a comment and will be ignored*"));
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testCommentInvalid() {
        JaysonReader reader = new JaysonReader(new StringReader("{\"foo\": /Z"));
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testKeylessObj() {
        JaysonReader reader = new JaysonReader(new StringReader("{{"));
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testKeylessArray() {
        JaysonReader reader = new JaysonReader(new StringReader("{["));
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testColonInArray() {
        JaysonReader reader = new JaysonReader(new StringReader("[:"));
        assertEquals(JaysonType.BEGIN_ARRAY, reader.next());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testColonWithoutKey() {
        JaysonReader reader = new JaysonReader(new StringReader("{:"));
        assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testWeirdUncommented() {
        try(JaysonReader reader = new JaysonReader(new StringReader("{$type+4.0:true}"))){
            assertEquals(JaysonType.BEGIN_OBJECT, reader.next());
            assertEquals("$type+4.0", reader.nextStr());
            assertTrue(reader.nextBool());
            assertEquals(JaysonType.END_OBJECT, reader.next());
        }
    }

    @Test
    public void testIOException() {
        JaysonReader reader = new JaysonReader(new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                throw new IOException();
            }

            @Override
            public void close() throws IOException {
            }

        });
        try {
            reader.next();
            fail("Exception expected");
        } catch (JaysonException ex) {
            assertNotNull(ex.getMessage());
        }

    }
}
