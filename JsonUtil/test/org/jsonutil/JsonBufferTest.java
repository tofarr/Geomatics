package org.jsonutil;

import java.io.StringWriter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofarrell
 */
public class JsonBufferTest {

    @Test
    public void testJsonBuffer() {
        JsonBuffer buffer = new JsonBuffer();
        buffer.beginObject().name("foo").num(12.3).name("bar").bool(true).name("zap").nul().name("ba-ng")
                .beginArray().bool(false).endArray().name("one").num(1).name("str").str("strValue").endObject();
        StringWriter str = new StringWriter();
        JsonWriter writer = new JsonWriter(str);
        writer.beginObject();
        JsonInput input = buffer.getInput();
        input.next();
        writer.copyRemaining(input);
        assertEquals("{foo:12.3,bar:true,zap:null,\"ba-ng\":[false],one:1,str:\"strValue\"}", str.toString());
    }

    @Test
    public void testBadStr() {
        JsonBuffer buffer = new JsonBuffer();
        buffer.num(12.3);
        JsonInput input = buffer.getInput();
        try {
            input.nextStr();
            fail("Exception expected");
        } catch (JsonException ex) {
        }
    }

    @Test
    public void testBadNum() {
        JsonBuffer buffer = new JsonBuffer();
        buffer.str("10");
        JsonInput input = buffer.getInput();
        try {
            input.nextNum();
            fail("Exception expected");
        } catch (JsonException ex) {
        }
    }

    @Test
    public void testBadBool() throws Exception {
        JsonBuffer buffer = new JsonBuffer();
        buffer.str("false");
        try (JsonInput input = buffer.getInput()) {
            try {
                input.nextBool();
                fail("Exception expected");
            } catch (JsonException ex) {
            }
        }
    }

    @Test
    public void testFindFirst() throws Exception {
        try(JsonBuffer buffer = new JsonBuffer("{a:[{b:\"bValue\",c:\"cValue\"},{d:10,e:false}]}")){
            assertEquals("bValue", buffer.findFirstStr("b", 3));
            assertEquals("cValue", buffer.findFirstStr("c", 3));
            assertEquals(10.0, buffer.findFirstNum("d", 3), 0.0001);
            assertFalse(buffer.findFirstBool("e", 3));
            try{
                buffer.findFirstStr("b", 1);
                fail("Exception expected");
            }catch(JsonException ex){
            }
            try{
                buffer.findFirstNum("f", 1);
                fail("Exception expected");
            }catch(JsonException ex){
            }
            try{
                buffer.findFirstBool("f", 1);
                fail("Exception expected");
            }catch(JsonException ex){
            }
            buffer.clear().beginObject().name("a").bool(true); // unclosed
            try{
                buffer.findFirstStr("b", 1);
                fail("Exception expected");
            }catch(JsonException ex){
            }
        }
    }
}
