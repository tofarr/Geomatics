/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jg.geom;

import org.jg.util.Tolerance;
import org.jg.util.VectList;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 *
 * @author tofar
 */
public class AdHocTest {
    
    @Test
    public void testLess(){
        Area a = Area.valueOf(Tolerance.DEFAULT, -65,-55, 65,-55, 65,55, -65,55, -65,-55);
        
        Area b = new Area(new Ring(new VectList(-65,-50, -64.90392640201615,-50.97545161008064, -64.61939766255644,-51.91341716182544, -64.15734806151274,-52.77785116509801, -63.535533905932745,-53.53553390593274, -62.77785116509802,-54.15734806151272, -61.91341716182546,-54.61939766255642, -60.97545161008065,-54.90392640201614, -60.00000000000001,-54.999999999999986, 60,-55, 60.97545161008064,-54.90392640201615, 61.913417161825436,-54.61939766255642, 62.777851165097985,-54.15734806151271, 63.535533905932695,-53.53553390593272, 64.15734806151266,-52.777851165098, 64.61939766255637,-51.91341716182544, 64.90392640201608,-50.97545161008065, 64.99999999999993,-50.000000000000014, 65,50, 64.90392640201615,50.97545161008064, 64.61939766255644,51.91341716182544, 64.15734806151274,52.77785116509801, 63.535533905932745,53.53553390593274, 62.77785116509802,54.15734806151272, 61.91341716182546,54.61939766255642, 60.97545161008065,54.90392640201614, 60.00000000000001,54.999999999999986, -60,55, -60.97545161008064,54.90392640201615, -61.913417161825436,54.61939766255642, -62.777851165097985,54.15734806151271, -63.535533905932695,53.53553390593272, -64.15734806151266,52.777851165098, -64.61939766255637,51.91341716182544, -64.90392640201608,50.97545161008065, -64.99999999999993,50.000000000000014, -65,-50), null),
                new Area(new Ring(new VectList(-45,-35, -39.99999999999999,-35, -40.97545161008063,-34.903926402016154, -41.91341716182544,-34.61939766255644, -42.77785116509801,-34.157348061512735, -43.53553390593274,-33.535533905932745, -44.15734806151273,-32.777851165098014, -44.61939766255644,-31.91341716182545, -44.903926402016154,-30.97545161008064, -45,-30, -45,-35), null)));

        //Vect vect = Vect.valueOf(-62.5, -54.99999999999999);
        //int relate = b.relate(vect, Tolerance.DEFAULT); // should be touch, not inside
        //System.out.println(relate);
        
        
        //VectRelationProcessor processor = new VectRelationProcessor(Tolerance.DEFAULT, -62.5, -54.99999999999999);
        //Line line = new Line(64.99999999999993, -50.000000000000014, 65, 50);
        //processor.process(line.getBounds(), line);
        //System.out.println(processor.getRelation());
        
        Area c = a.less(b, Tolerance.DEFAULT);
        String wkt = c.toGeoShape().toWkt();
        fail("No assertions "+wkt);
    }
}
