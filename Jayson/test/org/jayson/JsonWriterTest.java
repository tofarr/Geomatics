
package org.jayson;

import org.jayson.JaysonReader;
import org.jayson.JaysonWriter;
import org.jayson.JaysonException;
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
        JaysonWriter writer = new JaysonWriter(str);
        try{
            writer = new JaysonWriter(null);
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
        try(JaysonWriter writer = new JaysonWriter(str)){
            writer.beginObject().name("foo").str("bar");
            writer.comment("Some */ comment").endObject();
            assertEquals("{foo:\"bar\"/* Some * / comment */}", str.toString());
        }
    }
    
    @Test
    public void testSanitize(){
        assertEquals("a1", JaysonWriter.sanitize("a1"));
        assertEquals("A1", JaysonWriter.sanitize("A1"));
        assertEquals("$A1", JaysonWriter.sanitize("$A1"));
        assertEquals("\"1a\"", JaysonWriter.sanitize("1a"));
        assertEquals("\"[a\"", JaysonWriter.sanitize("[a"));
        assertEquals("\"{a\"", JaysonWriter.sanitize("{a"));
        
        assertEquals("ab", JaysonWriter.sanitize("ab"));
        assertEquals("a5", JaysonWriter.sanitize("a5"));
        assertEquals("a$", JaysonWriter.sanitize("a$"));
        assertEquals("\"a.b\"", JaysonWriter.sanitize("a.b"));
        assertEquals("\"a[\"", JaysonWriter.sanitize("a["));
        assertEquals("\"a{\"", JaysonWriter.sanitize("a{"));
        
        assertEquals("\"\"", JaysonWriter.sanitize(""));
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
        try(JaysonWriter writer = new JaysonWriter(errorWriter)){
            try{
                writer.comment("Foo");
                fail("Exception expected");
            }catch(JaysonException ex){
            }
            try{
                writer.beginArray();
                fail("Exception expected");
            }catch(JaysonException ex){
            }
        }
    }
    
    @Test
    public void testWriteRemaining(){
        String json = "{foo:1,bar:{bang:\"pop\"},zap:[true,false,null]}";
        JaysonReader reader = new JaysonReader(new StringReader(json));
        StringWriter str = new StringWriter();
        JaysonWriter writer = new JaysonWriter(str);
        reader.next();
        writer.beginObject();
        writer.copyRemaining(reader);
        assertEquals(json, str.toString());
    }
    
    @Test
    public void testBadEndObject(){
        StringWriter str = new StringWriter();
        JaysonWriter writer = new JaysonWriter(str);
        try{
            writer.endObject();
            fail("Exception expected!");
        }catch(JaysonException ex){
        }
        writer.beginObject().name("foo");
        try{
            writer.endObject();
            fail("Exception expected!");
        }catch(JaysonException ex){
        }
    }
    
    @Test
    public void testBadEndArray(){
        StringWriter str = new StringWriter();
        JaysonWriter writer = new JaysonWriter(str);
        try{
            writer.endArray();
            fail("Exception expected!");
        }catch(JaysonException ex){
        }
    }
    
    @Test
    public void testBadName(){
        StringWriter str = new StringWriter();
        JaysonWriter writer = new JaysonWriter(str);
        try{
            writer.name("foo");
            fail("Exception expected!");
        }catch(JaysonException ex){
        }
        writer.beginObject().name("foo");
        try{
            writer.name("bar");
            fail("Exception expected!");
        }catch(JaysonException ex){
        }
    }
    
    @Test
    public void teetBadValue(){
        StringWriter str = new StringWriter();
        JaysonWriter writer = new JaysonWriter(str);
        writer.beginObject();
        try{
            writer.str("foo");
            fail("Exception expected!");
        }catch(JaysonException ex){
        }
    }
    
    @Test
    public void testWriteRemainingFail(){
        String json = "{foo:1}";
        JaysonReader reader = new JaysonReader(new StringReader(json));
        StringWriter str = new StringWriter();
        JaysonWriter writer = new JaysonWriter(str);
        writer.beginArray();
        try{
            writer.copyRemaining(reader);
            fail("Exception expected");
        }catch(JaysonException ex){
        }
    }
    
}
