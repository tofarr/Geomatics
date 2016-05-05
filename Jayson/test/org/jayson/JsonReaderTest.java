package org.jayson;

import org.jayson.JsonReader;
import org.jayson.JsonType;
import org.jayson.JsonException;
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
        JsonReader reader = new JsonReader(new StringReader("{foo:[1,2,3],bar:true,zap:{\"bang\":\"pop\",\"piff\":\"\\\"paff\\\"\"}}"));
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        assertEquals(JsonType.NAME, reader.next());
        assertEquals("foo", reader.str());
        assertEquals(JsonType.BEGIN_ARRAY, reader.next());
        assertEquals(1, reader.nextNum(), 0.001);
        assertEquals(2, reader.nextNum(), 0.001);
        assertEquals(3, reader.nextNum(), 0.001);
        assertEquals(JsonType.END_ARRAY, reader.next());
        assertEquals("bar", reader.nextStr());
        assertEquals(true, reader.nextBool());
        assertEquals("zap", reader.nextStr());
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        assertEquals("bang", reader.nextStr());
        assertEquals(JsonType.STRING, reader.next());
        assertEquals("pop", reader.str());
        assertEquals("piff", reader.nextStr());
        assertEquals("\"paff\"", reader.nextStr());
        assertEquals(JsonType.END_OBJECT, reader.next());
        assertEquals(JsonType.END_OBJECT, reader.next());
        assertNull(reader.next());
    }

    @Test
    public void testValidJson_Whitespace() {
        JsonReader reader = new JsonReader(new StringReader(" {  foo :\r\n[1, 2,\t3]  , bar:\rtrue,zap:{\"bang\"  :\t\"pop\"} } "));
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        assertEquals(JsonType.NAME, reader.next());
        assertEquals("foo", reader.str());
        assertEquals(JsonType.BEGIN_ARRAY, reader.next());
        assertEquals(1, reader.nextNum(), 0.001);
        assertEquals(2, reader.nextNum(), 0.001);
        assertEquals(3, reader.nextNum(), 0.001);
        assertEquals(JsonType.END_ARRAY, reader.next());
        assertEquals("bar", reader.nextStr());
        assertEquals(true, reader.nextBool());
        assertEquals("zap", reader.nextStr());
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        assertEquals("bang", reader.nextStr());
        assertEquals(JsonType.STRING, reader.next());
        assertEquals("pop", reader.str());
        assertEquals(JsonType.END_OBJECT, reader.next());
        assertEquals(JsonType.END_OBJECT, reader.next());
        assertNull(reader.next());
    }

    @Test
    public void testWrongBracketOrder_A() {
        JsonReader reader = new JsonReader(new StringReader("{foo:[2,3}]"));
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        assertEquals(JsonType.NAME, reader.next());
        assertEquals("foo", reader.str());
        assertEquals(JsonType.BEGIN_ARRAY, reader.next());
        assertEquals(2, reader.nextNum(), 0.001);
        assertEquals(3, reader.nextNum(), 0.001);
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testWrongBracketOrder_B() {
        JsonReader reader = new JsonReader(new StringReader("[{foo:true]}"));
        assertEquals(JsonType.BEGIN_ARRAY, reader.next());
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        assertTrue(reader.nextBool());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testObjectMissingComma() {
        JsonReader reader = new JsonReader(new StringReader("{foo:true\"bar\":false}"));
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        try {
            assertTrue(reader.nextBool());
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testStringArray() {
        JsonReader reader = new JsonReader(new StringReader("[\"foo\",\"bar\"]"));
        assertEquals(JsonType.BEGIN_ARRAY, reader.next());
        assertEquals("foo", reader.nextStr());
        assertEquals("bar", reader.nextStr());
        assertEquals(JsonType.END_ARRAY, reader.next());
    }

    @Test
    public void testUNknownUncommentedString() {
        JsonReader reader = new JsonReader(new StringReader("[trueb]"));
        assertEquals(JsonType.BEGIN_ARRAY, reader.next());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testUncommentedStrings() {
        JsonReader reader = new JsonReader(new StringReader("[true,false,null,10,9.6,1.0E+24]"));
        assertEquals(JsonType.BEGIN_ARRAY, reader.next());
        assertTrue(reader.nextBool());
        assertFalse(reader.nextBool());
        assertEquals(JsonType.NULL, reader.next());
        assertEquals(10, reader.nextNum(), 0.01);
        assertEquals(9.6, reader.nextNum(), 0.01);
        assertEquals(1.0E+24, reader.nextNum(), 0.01);
        assertEquals(JsonType.END_ARRAY, reader.next());
    }

    @Test
    public void testArrayLeadingComma() {
        JsonReader reader = new JsonReader(new StringReader("[,2,3]"));
        assertEquals(JsonType.BEGIN_ARRAY, reader.next());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testObjectLeadingComma() {
        JsonReader reader = new JsonReader(new StringReader("{,foo:2}"));
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testArrayTrailingComma() {
        JsonReader reader = new JsonReader(new StringReader("[2,3,]"));
        assertEquals(JsonType.BEGIN_ARRAY, reader.next());
        assertEquals(2, reader.nextNum(), 0.001);
        assertEquals(3, reader.nextNum(), 0.001);
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testObjectTrialingComma() {
        JsonReader reader = new JsonReader(new StringReader("{foo:2,}"));
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        assertEquals(2, reader.nextNum(), 0.001);
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testObjectNoColonComma() {
        JsonReader reader = new JsonReader(new StringReader("{foo,2}"));
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testObjectCommaAfterColon() {
        JsonReader reader = new JsonReader(new StringReader("{foo:,2}"));
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testDoubleComma() {
        JsonReader reader = new JsonReader(new StringReader("[1,,2]"));
        assertEquals(JsonType.BEGIN_ARRAY, reader.next());
        assertEquals(1, reader.nextNum(), 0.01);
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testTopLevelComma() {
        JsonReader reader = new JsonReader(new StringReader("10,4"));
        assertEquals(10, reader.nextNum(), 0.001);
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }
    
    @Test
    public void testTopLevelNum() {
        JsonReader reader = new JsonReader(new StringReader("10.4"));
        assertEquals(10.4, reader.nextNum(), 0.001);
        assertNull(reader.next());
    }

    @Test
    public void testTopLevelBool() {
        JsonReader reader = new JsonReader(new StringReader("false"));
        assertFalse(reader.nextBool());
        assertNull(reader.next());
    }

    @Test
    public void testTopLevelNull() {
        JsonReader reader = new JsonReader(new StringReader("null"));
        assertEquals(JsonType.NULL, reader.next());
        assertNull(reader.next());
    }

    @Test
    public void testTopLevelStr() {
        JsonReader reader = new JsonReader(new StringReader("\"\\\"ping\\\"\""));
        assertEquals("\"ping\"", reader.nextStr());
        assertNull(reader.next());
    }

    @Test
    public void testUnexpectedEOF_A() {
        JsonReader reader = new JsonReader(new StringReader("{foo:2"));
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testUnexpectedEOF_B() {
        JsonReader reader = new JsonReader(new StringReader("{foo"));
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testUnexpectedEOF_C() {
        JsonReader reader = new JsonReader(new StringReader("[tru"));
        assertEquals(JsonType.BEGIN_ARRAY, reader.next());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testUnexpectedEOF_D() {
        JsonReader reader = new JsonReader(new StringReader("tru"));
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testUnexpectedEOF_E() {
        JsonReader reader = new JsonReader(new StringReader("\"pin"));
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testUnexpectedEOF_F() {
        JsonReader reader = new JsonReader(new StringReader("\""));
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testUnexpectedEOF_G() {
        JsonReader reader = new JsonReader(new StringReader("\"\\"));
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testUnexpectedEOF_H() {
        JsonReader reader = new JsonReader(new StringReader("/"));
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testNextStr() {
        JsonReader reader = new JsonReader(new StringReader("{foo:\"bar\"}"));
        try {
            reader.nextStr();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testNextNum() {
        JsonReader reader = new JsonReader(new StringReader("{foo:\"bar\"}"));
        reader.next();
        reader.next();
        try {
            reader.nextNum();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testNextBool() {
        JsonReader reader = new JsonReader(new StringReader("{foo:\"bar\"}"));
        try {
            reader.nextBool();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testCommentDoubleSlash() {
        JsonReader reader = new JsonReader(new StringReader("{\"foo\": // This is a comment and will be ignored\r\n\"bar\"}"));
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        assertEquals("bar", reader.nextStr());
        assertEquals(JsonType.END_OBJECT, reader.next());
    }

    @Test
    public void testCommentDoubleSlashEOF() {
        JsonReader reader = new JsonReader(new StringReader("{\"foo\": // This is a comment and will be ignored"));
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testComment() {
        JsonReader reader = new JsonReader(new StringReader("{\"foo\": /* This is a comment / * and will be ignored */ \"bar\"}"));
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        assertEquals("bar", reader.nextStr());
        assertEquals(JsonType.END_OBJECT, reader.next());
    }

    @Test
    public void testCommentEOF() {
        JsonReader reader = new JsonReader(new StringReader("{\"foo\": /* This is a comment and will be ignored"));
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testCommentEOF_B() {
        JsonReader reader = new JsonReader(new StringReader("{\"foo\": /* This is a comment and will be ignored*"));
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testCommentInvalid() {
        JsonReader reader = new JsonReader(new StringReader("{\"foo\": /Z"));
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        assertEquals("foo", reader.nextStr());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testKeylessObj() {
        JsonReader reader = new JsonReader(new StringReader("{{"));
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testKeylessArray() {
        JsonReader reader = new JsonReader(new StringReader("{["));
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testColonInArray() {
        JsonReader reader = new JsonReader(new StringReader("[:"));
        assertEquals(JsonType.BEGIN_ARRAY, reader.next());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testColonWithoutKey() {
        JsonReader reader = new JsonReader(new StringReader("{:"));
        assertEquals(JsonType.BEGIN_OBJECT, reader.next());
        try {
            reader.next();
            fail("Exception expected");
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void testWeirdUncommented() {
        try(JsonReader reader = new JsonReader(new StringReader("{$type+4.0:true}"))){
            assertEquals(JsonType.BEGIN_OBJECT, reader.next());
            assertEquals("$type+4.0", reader.nextStr());
            assertTrue(reader.nextBool());
            assertEquals(JsonType.END_OBJECT, reader.next());
        }
    }

    @Test
    public void testIOException() {
        JsonReader reader = new JsonReader(new Reader() {
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
        } catch (JsonException ex) {
            assertNotNull(ex.getMessage());
        }

    }
}
