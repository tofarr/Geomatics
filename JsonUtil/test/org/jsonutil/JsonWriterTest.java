
package org.jsonutil;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofar
 */
public class JsonWriterTest {
    
    @Test
    public void testValidOutput(){
        StringWriter str = new StringWriter();
        JsonWriter writer = new JsonWriter(str);
        try{
            writer = new JsonWriter(null);
            fail("Exception expected");
        }catch(NullPointerException ex){
        }
        writer.beginObject().name("foo").str("bar");
        writer.whitespace().name("z.a.p").beginArray().num(1).num(2.1);
        writer.whitespace().bool(true).str("three").nul().endArray().endObject();
        assertEquals("{foo:\"bar\", \"z.a.p\":[1,2.1, true,\"three\",null]}", str.toString());
    }
    
    
    @Test
    public void testComment(){
        StringWriter str = new StringWriter();
        JsonWriter writer = new JsonWriter(str);
        writer.beginObject().name("foo").str("bar");
        writer.comment("Some */ comment").endObject();
        assertEquals("{foo:\"bar\"/* Some * / comment */}", str.toString());
    }
    
    @Test
    public void testSanitize(){
        assertEquals("a1", JsonWriter.sanitize("a1"));
        assertEquals("A1", JsonWriter.sanitize("A1"));
        assertEquals("$A1", JsonWriter.sanitize("$A1"));
        assertEquals("\"1a\"", JsonWriter.sanitize("1a"));
        assertEquals("\"[a\"", JsonWriter.sanitize("[a"));
        assertEquals("\"{a\"", JsonWriter.sanitize("{a"));
        
        assertEquals("ab", JsonWriter.sanitize("ab"));
        assertEquals("a5", JsonWriter.sanitize("a5"));
        assertEquals("a$", JsonWriter.sanitize("a$"));
        assertEquals("\"a.b\"", JsonWriter.sanitize("a.b"));
        assertEquals("\"a[\"", JsonWriter.sanitize("a["));
        assertEquals("\"a{\"", JsonWriter.sanitize("a{"));
        
        assertEquals("\"\"", JsonWriter.sanitize(""));
    }
    
    @Test
    public void testAppend(){
        Writer errorWriter = new Writer(){
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                throw new IOException();
            }

            @Override
            public void flush() throws IOException {
            }

            @Override
            public void close() throws IOException {
            }
            
        };
        JsonWriter writer = new JsonWriter(errorWriter);
        try{
            writer.comment("Foo");
            fail("Exception expected");
        }catch(JsonException ex){
        }
        try{
            writer.beginArray();
            fail("Exception expected");
        }catch(JsonException ex){
        }
    }
    
    @Test
    public void testWriteRemaining(){
        String json = "{foo:1,bar:2,zap:[true,false]}";
        JsonReader reader = new JsonReader(new StringReader(json));
        StringWriter str = new StringWriter();
        JsonWriter writer = new JsonWriter(str);
        reader.next();
        writer.beginObject();
        writer.writeRemaining(reader);
        assertEquals(json, str.toString());
    }
}
