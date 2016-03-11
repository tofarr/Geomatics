package org.jg.geom;

import org.jg.util.Tolerance;
import org.jg.util.VectList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofarrell
 */
public class NetworkTest {
  
    @Test
    public void testExplicitIntersection_Loop(){
        Network network = new Network();
        network.addAllLinks(new VectList(0,15, 65,15, 65,65, 15,65, 15,0, 25,0, 25,55, 55,55, 55,25, 0,25, 0,15));
        
        network.explicitIntersections(Tolerance.DEFAULT);
        assertEquals("[[15,15, 0,15, 0,25, 15,25],[15,15, 15,0, 25,0, 25,15],[15,15, 15,25],[15,15, 25,15],[15,25, 15,65, 65,65, 65,15, 25,15],[15,25, 25,25],[25,15, 25,25],[25,25, 25,55, 55,55, 55,25, 25,25]]", network.toString());
    }
     
    @Test
    public void testExplicitIntersection_SawTeeth(){
        Network network = new Network();
        network.addAllLinks(new VectList(0, 50, 50, 100, 50, 0, 150, 100, 150, 0, 250, 100, 250, 0, 350, 100, 350, 0, 450, 100, 450, 50, 0, 50));
        network.explicitIntersections(Tolerance.DEFAULT);
        assertEquals("[[50,50, 0,50, 50,100, 50,50],[50,50, 50,0, 100,50],[50,50, 100,50],[100,50, 150,50],[100,50, 150,100, 150,50],[150,50, 150,0, 200,50],[150,50, 200,50],[200,50, 250,50],[200,50, 250,100, 250,50],[250,50, 250,0, 300,50],[250,50, 300,50],[300,50, 350,50],[300,50, 350,100, 350,50],[350,50, 350,0, 400,50],[350,50, 400,50],[400,50, 450,50, 450,100, 400,50]]", network.toString());
    }
    
    @Test
    public void testQuad(){
        assertEquals(0, Network.quad(0, -5));
        assertEquals(0, Network.quad(5, -5));
        assertEquals(1, Network.quad(5, 0));
        assertEquals(1, Network.quad(5, 5));
        assertEquals(1, Network.quad(0, 5));
        assertEquals(2, Network.quad(-5, 5));
        assertEquals(3, Network.quad(-5, 0));
        assertEquals(3, Network.quad(-5, -5));
    } 
    
    @Test
    public void testCompare(){
        assertEquals(-1, Network.compare(0, -5, 5, -5));
        assertEquals(-1, Network.compare(5, -5, 5, 0));
        assertEquals(-1, Network.compare(5, 0, 5, 5));
        assertEquals(-1, Network.compare(5, 5, 0, 5));
        assertEquals(-1, Network.compare(0, 5, -5, 5));
        assertEquals(-1, Network.compare(-5, 5, -5, 0));
        assertEquals(-1, Network.compare(-5, 0, -5, -5));
        
        assertEquals(1, Network.compare(5, -5, 0, -5));
        assertEquals(1, Network.compare(5, 0, 5, -5));
        assertEquals(1, Network.compare(5, 5, 5, 0));
        assertEquals(1, Network.compare(0, 5, 5, 5));
        assertEquals(1, Network.compare(-5, 5, 0, 5));
        assertEquals(1, Network.compare(-5, 0, -5, 5));
        assertEquals(1, Network.compare(-5, -5, -5, 0));
    }
    
    @Test
    public void testAddLink(){
        Network network = new Network();
        
        network.addLink(5, 5, 10, 5);
        network.addLink(5, 5, 0, 7);
        network.addLink(5, 5, 5, 0);
        network.addLink(5, 5, 10, 5);
        network.addLink(5, 5, 0, 3);
        network.addLink(5, 5, 10, 0);
        network.addLink(5, 5, 0, 10);
        network.addLink(5, 5, 0, 5);
        network.addLink(5, 5, 3, 0);
        network.addLink(5, 5, 0, 0);
        network.addLink(5, 5, 3, 10);
        network.addLink(5, 5, 10, 7);
        network.addLink(5, 5, 5, 10);
        network.addLink(5, 5, 10, 10);
        network.addLink(5, 5, 7, 0);
        network.addLink(5, 5, 10, 3);
        network.addLink(5, 5, 7, 10);
        
        VectList target = new VectList();
        network.getLinks(5, 5, target);
        assertEquals("[5,0, 7,0, 10,0, 10,3, 10,5, 10,7, 10,10, 7,10, 5,10, 3,10, 0,10, 0,7, 0,5, 0,3, 0,0, 3,0]", target.toString());
//        String wkt = "MULTILINESTRING"+network.toString().replace(",", " ").replace("  ", ", ").replace("[", "(").replace("]",")").replace(") (", "),(");
//        System.out.println(wkt);
    }
}
