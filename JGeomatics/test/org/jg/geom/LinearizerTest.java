/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jg.geom;

import org.jg.util.VectList;
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
public class LinearizerTest {
    
    @Test
    public void testGetSegmentsPerQuadrant() {
        Linearizer linearizer = new Linearizer(0.1);
        assertEquals(0, linearizer.getSegmentsPerQuadrant().intValue());
        linearizer = new Linearizer(6);
        assertEquals(6, linearizer.getSegmentsPerQuadrant().intValue());
    }

    @Test
    public void testConstructor() {
        Linearizer linearizer = new Linearizer(0.1);
        assertEquals(0.1, linearizer.getFlatness(), 0.0001);
        assertNull(linearizer.getSegmentsPerQuadrant());
        linearizer = new Linearizer(6);
        assertNull(linearizer.getFlatness());
        assertEquals(6, linearizer.getSegmentsPerQuadrant().intValue());
    }

    @Test
    public void testLinearizeArc_A(){
        Linearizer linearizer = new Linearizer(0.1);
        VectList result = new VectList();
        linearizer.linearizeSegment(0, 0, 0, 2, 2, 0, result);
        result.add(0, 0).add(0, 2);
        Rect bounds = result.getBounds();
        assertEquals(-2, bounds.minX, 0.1);
        assertEquals(-2, bounds.minY, 0.1);
        assertEquals(2, bounds.maxX, 0.1);
        assertEquals(2, bounds.maxY, 0.1);
        assertEquals(Math.PI * 3, Ring.getArea(result), 0.1);
        assertEquals(Math.PI * 3 + 4, LineString.getLength(result), 0.2);
        
        result.clear().add(0, 0);
        linearizer.linearizeSegment(0, 0, 2, 0, 0, 2, result);
        result.add(0, 0);
        bounds = result.getBounds();
        assertEquals(0, bounds.minX, 0.1);
        assertEquals(0, bounds.minY, 0.1);
        assertEquals(2, bounds.maxX, 0.1);
        assertEquals(2, bounds.maxY, 0.1);
        assertEquals(Math.PI, Ring.getArea(result), 0.1);
        assertEquals(Math.PI + 4, LineString.getLength(result), 0.2);
        
        result.clear();
        linearizer = new Linearizer(1.0);
        linearizer.linearizeSegment(0, 0, 2, 0, 0, 2, result);
        assertEquals(new VectList(0, 0), result);
        
        result.clear();
        linearizer = new Linearizer(0.1);
        linearizer.linearizeArc(0, 0, Math.PI/2, 0, 2, result);
        result.add(0, 0).add(0, 2);
        bounds = result.getBounds();
        assertEquals(-2, bounds.minX, 0.1);
        assertEquals(-2, bounds.minY, 0.1);
        assertEquals(2, bounds.maxX, 0.1);
        assertEquals(2, bounds.maxY, 0.1);
        assertEquals(Math.PI * 3, Ring.getArea(result), 0.1);
        assertEquals(Math.PI * 3 + 4, LineString.getLength(result), 0.2);
        
        result.clear().add(0, 0);
        linearizer.linearizeArc(0, 0, 0, Math.PI/2, 2, result);
        result.add(0, 0);
        bounds = result.getBounds();
        assertEquals(0, bounds.minX, 0.1);
        assertEquals(0, bounds.minY, 0.1);
        assertEquals(2, bounds.maxX, 0.1);
        assertEquals(2, bounds.maxY, 0.1);
        assertEquals(Math.PI, Ring.getArea(result), 0.1);
        assertEquals(Math.PI + 4, LineString.getLength(result), 0.2);
    }
    

    @Test
    public void testLinearizeArc_B() {
        VectList result = new VectList();
        Linearizer linearizer = Linearizer.DEFAULT;
        linearizer.linearizeArc(0, 0, 0, Math.PI/2, 10, result);
        assertEquals(8, result.size());
        result.clear();linearizer = new Linearizer(4);
        linearizer.linearizeArc(0, 0, 0, Math.PI/2, 10, result);
        assertEquals(4, result.size());
    }
}
