package org.jayson;

import org.jayson.PrettyPrintJsonWriter;
import java.io.StringWriter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofarrell
 */
public class PrettyPrintJsonWriterTest {
 
    @Test
    public void testValidOutput(){
        StringWriter str = new StringWriter();
        PrettyPrintJsonWriter writer = new PrettyPrintJsonWriter(str);
        writer.beginObject().name("foo").str("bar");
        writer.comment("Lorem ipsum dolor sit amet")
            .name("z.a.p").beginArray().num(1).num(2.1)
            .bool(true).str("three").nul().endArray().endObject();
        assertEquals("{\r\n\tfoo:\"bar\"\r\n\t/* Lorem ipsum dolor sit amet */,\r\n\t\"z.a.p\":[1,2.1,true,\"three\",null]\r\n}", str.toString());
    }
    
}
