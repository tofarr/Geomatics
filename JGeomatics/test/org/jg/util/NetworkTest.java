package org.jg.util;

import java.util.ArrayList;
import org.jg.geom.Vect;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tofarrell
 */
public class NetworkTest {
  
    @Test
    public void testExplicitIntersections(){
        Network network = new Network();
        network.addAllLinks(new VectList(0,15, 65,15, 65,65, 15,65, 15,0, 25,0, 25,55, 55,55, 55,25, 0,25, 0,15));
        
        network.explicitIntersections(Tolerance.DEFAULT);
        
        VectList vertices = network.getVects(new VectList());
        for(int i = 0; i < vertices.size(); i++){
            Vect vertex = vertices.getVect(i);
            VectList links = new VectList();
            network.getLinks(vertex, links);
            System.out.println(vertex + " : "+links);
        }
        
        ArrayList<VectList> results = new ArrayList<>();
        network.extractLines(results, true);
        System.out.println(results);
        
        String wkt = "MULTILINESTRING"+results.toString().replace(",", " ").replace("  ", ", ").replace("[", "(").replace("]",")");
        fail("Test case is prototype "+wkt);
    }
    
}
