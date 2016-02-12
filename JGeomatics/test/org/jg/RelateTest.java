/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jg;

import org.jg.util.Relate;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tim.ofarrell
 */
public class RelateTest {

    @Test
    public void testValues() {
        assertEquals(3, Relate.values().length);
    }

    @Test
    public void testValueOf() {
        assertEquals(Relate.INSIDE, Relate.valueOf("INSIDE"));
        assertEquals(Relate.OUTSIDE, Relate.valueOf("OUTSIDE"));
        assertEquals(Relate.TOUCH, Relate.valueOf("TOUCH"));
    }
    
}
