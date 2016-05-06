package org.geomatics.geom;

import org.geomatics.geom.Relation;
import org.geomatics.geom.Line;
import org.geomatics.geom.VectRelationProcessor;
import org.geomatics.util.Tolerance;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofarrell
 */
public class VectRelationProcessorTest {

    @Test
    public void testReset() {
        VectRelationProcessor processor = new VectRelationProcessor(Tolerance.DEFAULT, 1, 2);
        assertEquals(1, processor.getX(), 0.00001);
        assertEquals(2, processor.getY(), 0.00001);
        processor.reset(3, 4);
        assertEquals(3, processor.getX(), 0.00001);
        assertEquals(4, processor.getY(), 0.00001);
        processor = new VectRelationProcessor(Tolerance.DEFAULT);
        assertEquals(0, processor.getX(), 0.00001);
        assertEquals(0, processor.getY(), 0.00001);
        processor.reset(5, 7);
        assertEquals(5, processor.getX(), 0.00001);
        assertEquals(7, processor.getY(), 0.00001);
    }

    @Test
    public void testHorizontal() {
        VectRelationProcessor processor = new VectRelationProcessor(Tolerance.DEFAULT, 3, 7);
        Line line = Line.valueOf(0, 5, 6, 5);
        assertTrue(processor.process(line.getBounds(), line));
        assertEquals(Relation.B_OUTSIDE_A, processor.getRelation());
        
        line = Line.valueOf(0, 7, 2, 7);
        assertTrue(processor.process(line.getBounds(), line));
        
        line = Line.valueOf(4, 7, 6, 7);
        assertTrue(processor.process(line.getBounds(), line));
        
        line = Line.valueOf(2, 7, 0, 7);
        assertTrue(processor.process(line.getBounds(), line));
        
        line = Line.valueOf(6, 7, 4, 7);
        assertTrue(processor.process(line.getBounds(), line));
        
        line = Line.valueOf(0, 7, 6, 7);
        assertFalse(processor.process(line.getBounds(), line));
        
        processor.reset(3, 7);
        line = Line.valueOf(6, 7, 0, 7);
        assertFalse(processor.process(line.getBounds(), line));
    }
    
    @Test
    public void testProcess() {
        VectRelationProcessor processor = new VectRelationProcessor(Tolerance.DEFAULT, 3, 7);
        //Line line = Line.valueOf(5, 4, 8, 8);
        Line line = Line.valueOf(7, 4, 12, 6.9999);
        assertTrue(processor.process(line.getBounds(), line));
        assertEquals(Relation.B_INSIDE_A, processor.getRelation());
        
        line = Line.valueOf(-1, 4, -6, 7.0001);
        assertTrue(processor.process(line.getBounds(), line));
        assertEquals(Relation.B_INSIDE_A, processor.getRelation());
        
        line = Line.valueOf(7, 4, 12, 7);
        assertTrue(processor.process(line.getBounds(), line));
        assertEquals(Relation.B_INSIDE_A, processor.getRelation());
        assertTrue(processor.process(line.getBounds(), line));
        assertEquals(Relation.B_INSIDE_A, processor.getRelation());
        
        assertTrue(processor.process(line.getBounds(), line));
        
        line = Line.valueOf(7, 10, 12, 7);
        assertTrue(processor.process(line.getBounds(), line));
        assertEquals(Relation.B_OUTSIDE_A, processor.getRelation());
        
        processor = new VectRelationProcessor(Tolerance.DEFAULT, 3, 7);
        assertTrue(processor.process(line.getBounds(), line));
        line = Line.valueOf(7, 4, 12, 7);
        assertTrue(processor.process(line.getBounds(), line));
        assertEquals(Relation.B_INSIDE_A, processor.getRelation());
    }
}
