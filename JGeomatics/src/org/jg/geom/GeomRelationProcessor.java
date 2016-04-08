package org.jg.geom;

import org.jg.util.Tolerance;
import org.jg.util.VectList;
import org.jg.util.VectMap.VectMapProcessor;

/**
 * Processor for geometry relations
 *
 * @author tofarrell
 */
public final class GeomRelationProcessor implements VectMapProcessor<VectList> {

    private final Tolerance accuracy;
    private final VectBuilder workingVect;
    private Geom a;
    private Geom b;
    private int relation;

    public GeomRelationProcessor(Tolerance accuracy) {
        this.accuracy = accuracy;
        workingVect = new VectBuilder();
    }

    public GeomRelationProcessor(Tolerance accuracy, Geom a, Geom b) {
        this(accuracy);
        reset(a, b);
    }

    public static int relate(Geom a, Geom b, Tolerance flatness, Tolerance accuracy) throws NullPointerException {
        if(a.getBounds().relate(b.getBounds(), accuracy) == Relation.DISJOINT){
            return Relation.DISJOINT; // Short cut - if disjoint, then cant possibly overlap
        }
        Network network = Network.valueOf(accuracy, flatness, a, b);
        return relate(a, b, network, accuracy);
    }

    static int relate(Geom a, Geom b, Network network, Tolerance accuracy) throws NullPointerException {
        GeomRelationProcessor processor = new GeomRelationProcessor(accuracy, a, b);
        network.map.forEach(processor);
        return processor.relation;
    }

    public void reset(Geom a, Geom b) {
        this.a = a;
        this.b = b;
        relation = Relation.NULL;
    }

    public Tolerance getAccuracy() {
        return accuracy;
    }

    public Geom getA() {
        return a;
    }

    public Geom getB() {
        return b;
    }

    public int getRelation() {
        return relation;
    }

    @Override
    public boolean process(double x, double y, VectList links) {
        workingVect.set(x, y);
        process();
        
        for(int i = links.size(); i-- > 0;){
            double mx = (x + links.getX(i)) / 2;
            double my = (y + links.getY(i)) / 2;
            workingVect.set(mx, my);
            process();
        }
        
        return (relation != Relation.ALL); // If already all relations, no need to check further
    }
    
    void process(){
        int relateA = a.relate(workingVect, accuracy);
        int relateB = Relation.swap(b.relate(workingVect, accuracy));
        
        //Considered a touch only if touching both
        boolean touchA = Relation.isTouch(relateA);
        boolean touchB = Relation.isTouch(relateB);
        if(touchA){
           if(!touchB){ 
                relateA ^= Relation.TOUCH;
           }
        }else if(touchB){
            relateB ^= Relation.TOUCH;
        }
        
        //Apply relation
        relation |= relateA;
        relation |= relateB;
    }

}
