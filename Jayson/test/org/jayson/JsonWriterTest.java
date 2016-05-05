
package org.jayson;

import org.jayson.JsonReader;
import org.jayson.JsonWriter;
import org.jayson.JsonException;
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
        writer.beginObject().name("foo").str("bar")
            .name("z.a.p").beginArray().num(1).num(2.1)
            .bool(true).str("three").nul().endArray().endObject();
        assertEquals("{foo:\"bar\",\"z.a.p\":[1,2.1,true,\"three\",null]}", str.toString());
    }
    
    
    @Test
    public void testComment(){
        StringBuilder str = new StringBuilder();
        try(JsonWriter writer = new JsonWriter(str)){
            writer.beginObject().name("foo").str("bar");
            writer.comment("Some */ comment").endObject();
            assertEquals("{foo:\"bar\"/* Some * / comment */}", str.toString());
        }
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
        try(JsonWriter writer = new JsonWriter(errorWriter)){
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
    }
    
    @Test
    public void testWriteRemaining(){
        String json = "{foo:1,bar:{bang:\"pop\"},zap:[true,false,null]}";
        JsonReader reader = new JsonReader(new StringReader(json));
        StringWriter str = new StringWriter();
        JsonWriter writer = new JsonWriter(str);
        reader.next();
        writer.beginObject();
        writer.copyRemaining(reader);
        assertEquals(json, str.toString());
    }
    
    @Test
    public void testBadEndObject(){
        StringWriter str = new StringWriter();
        JsonWriter writer = new JsonWriter(str);
        try{
            writer.endObject();
            fail("Exception expected!");
        }catch(JsonException ex){
        }
        writer.beginObject().name("foo");
        try{
            writer.endObject();
            fail("Exception expected!");
        }catch(JsonException ex){
        }
    }
    
    @Test
    public void testBadEndArray(){
        StringWriter str = new StringWriter();
        JsonWriter writer = new JsonWriter(str);
        try{
            writer.endArray();
            fail("Exception expected!");
        }catch(JsonException ex){
        }
    }
    
    @Test
    public void testBadName(){
        StringWriter str = new StringWriter();
        JsonWriter writer = new JsonWriter(str);
        try{
            writer.name("foo");
            fail("Exception expected!");
        }catch(JsonException ex){
        }
        writer.beginObject().name("foo");
        try{
            writer.name("bar");
            fail("Exception expected!");
        }catch(JsonException ex){
        }
    }
    
    @Test
    public void teetBadValue(){
        StringWriter str = new StringWriter();
        JsonWriter writer = new JsonWriter(str);
        writer.beginObject();
        try{
            writer.str("foo");
            fail("Exception expected!");
        }catch(JsonException ex){
        }
    }
    
    @Test
    public void testWriteRemainingFail(){
        String json = "{foo:1}";
        JsonReader reader = new JsonReader(new StringReader(json));
        StringWriter str = new StringWriter();
        JsonWriter writer = new JsonWriter(str);
        writer.beginArray();
        try{
            writer.copyRemaining(reader);
            fail("Exception expected");
        }catch(JsonException ex){
        }
    }
    
}
