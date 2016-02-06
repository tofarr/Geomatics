package org.jg;

import org.jg.SpatialNode.NodeProcessor;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author tim.ofarrell
 */
public class Network implements Externalizable, Cloneable {
    
    private VectMap<VectList> map;
    private int numLinks;

    public int numVects() {
        return map.size();
    }

    public int numLinks() {
        return numLinks;
    }

    public boolean hasVect(Vect vect) throws NullPointerException {
        return map.containsKey(vect);
    }

    public boolean hasLink(Vect a, Vect b) throws NullPointerException {
        VectList links = map.get(a);
        return (links != null) && (links.indexOf(b, 0) >= 0);
    }

    //Returns -1 if vect does not exist
    public int numLinks(Vect vect) throws NullPointerException {
        VectList links = map.get(vect);
        return (links == null) ? -1 : links.size();
    }

    public boolean getLinks(Vect vect, VectList target) throws NullPointerException {
        VectList links = map.get(vect);
        if (links == null) {
            return false;
        }
        target.addAll(links);
        return true;
    }

    public boolean nextCW(Vect vect, Vect link, Vect target) throws NullPointerException {
        VectList links = map.get(vect);
        if (links != null) {
            int index = links.indexOf(link, 0);
            if (index >= 0) {
                index++;
                if (index == links.size()) {
                    index = 0;
                }
                links.get(index, target);
                return true;
            }
        }
        return false;
    }

    public boolean nextCCW(Vect vect, Vect link, Vect target) throws NullPointerException, IllegalArgumentException {
        VectList links = map.get(vect);
        if (links != null) {
            int index = links.indexOf(link, 0);
            if (index >= 0) {
                if (index == 0) {
                    index = links.size();
                }
                index--;
                links.get(index, target);
                return true;
            }
        }
        return false;
    }

    public boolean addVertex(Vect vect) throws NullPointerException {
        VectList links = map.get(vect);
        if (links == null) {
            links = new VectList(2);
            map.put(new Vect(vect), links);
            return true;
        }
        return false;
    }

    public boolean removeVertex(Vect vect) throws NullPointerException {
        VectList links = map.remove(vect);
        if (links == null) {
            return false;
        }
        Vect link = new Vect();
        for (int i = 0; i < links.size(); i++) {
            links.get(i, link);
            VectList backLinks = map.get(link);
            int index = backLinks.indexOf(vect, 0);
            backLinks.remove(index);
        }
        numLinks -= links.size();
        return true;

    }

    //does nothing if points are the same
    public boolean addLink(Vect a, Vect b) throws NullPointerException {
        if (a.equals(b)) {
            return false;
        }
        VectList links = map.get(a);
        if (links == null) {
            links = new VectList(2);
            links.add(b);
            map.put(new Vect(a), links);
        } else {
            int index = links.indexOf(b, 0);
            if (index >= 0) {
                return false; // link already exists
            }
            insertLink(a, b, links);
        }
        links = map.get(b);
        if (links == null) {
            links = new VectList(2);
            links.add(a);
            map.put(new Vect(b), links);
        } else {
            insertLink(b, a, links);
        }
        numLinks++;
        return true;
    }

    private static void insertLink(Vect origin, Vect toInsert, VectList links) {
        int min = 0;
        int max = links.size();
        double ox = origin.getX();
        double oy = origin.getY();
        double ndx = toInsert.getX() - ox;
        double ndy = toInsert.getY() - oy;
        while (min < max) {
            int mid = (min + max) / 2;
            double vdx = links.getX(mid) - ox;
            double vdy = links.getY(mid) - oy;
            if (compare(ndx, ndy, vdx, vdy) < 0) {
                max = mid;
            } else {
                min = mid + 1;
            }
        }
        links.insert(min, toInsert);
    }

    static int compare(double idx, double idy, double jdx, double jdy) {
        if ((idx == jdx) && (idy == jdy)) {
            return 0;
        }
        if (idx >= 0) {
            if (jdx < 0) {
                return 1;
            } else {
                return Double.compare((idy / idx), (jdy / jdx));
            }
        } else {
            if (jdx >= 0) {
                return -1;
            } else {
                return Double.compare((idy / idx), (jdy / jdx));
            }
        }
    }

    public boolean removeLink(Vect a, Vect b) throws NullPointerException {
        VectList links = map.get(a);
        if (links == null) {
            return false;
        }
        int index = links.indexOf(b, 0);
        if (index < 0) {
            return false;
        }
        links.remove(index);
        numLinks--;
        return true;
    }

    public Network clear() {
        map.clear();
        numLinks = 0;
        return this;
    }

    public Network addAllVects(VectList vects) {
        Vect vect = new Vect();
        for (int i = 0; i < vects.size(); i++) {
            vects.get(i, vect);
            addVertex(vect);
        }
        return this;
    }

    public Network addAllLinks(VectList links) {
        if (links.size() <= 1) {
            return this;
        }
        Vect a = new Vect();
        links.get(0, a);
        Vect b = new Vect();
        for (int i = 1; i < links.size(); i++) {
            links.get(i, b);
            addLink(a, b);
            Vect c = a;
            a = b;
            b = c;
        }
        return this;
    }

    //Get all vectors, sorted from min to max
    public VectList getVects(VectList target) {
        map.keyList(target);
        target.sort();
        return target;

    }

    public RTree<Line> getLines(RTree<Line> target) {
        Vect a = new Vect();
        Vect b = new Vect();
        for(VectMap<VectList>.Iter iter = map.iterator(); iter.next();){
            iter.getVect(a);
            VectList vects = iter.getValue();
            for (int i = 0; i < vects.size(); i++) {
                vects.get(i, b);
                if(a.compareTo(b) < 0){
                    Rect rect = new Rect().union(a).union(b);
                    Line line = new Line(a, b);
                    target.add(rect, line);
                }
            }
        }
        return target;
    }

    //modifications during iteration are not permitted
    public Iter iterator() {
        return new Iter();
    }

    //Make all points of self intersection explicit
    public Network explicitIntersections(Tolerance tolerance) {
        final RTree<Line> lines = getLines(new RTree<Line>());
        final IntersectionFinder finder = new IntersectionFinder(tolerance);
        lines.root.get(new NodeProcessor<Line>() {

            final Rect rect = new Rect();
            
            @Override
            public boolean process(SpatialNode<Line> leaf, int index) {
                Line value = leaf.getItemValue(index);
                leaf.getItemBounds(index, rect);
                finder.i = value;
                lines.root.getInteracting(rect, finder);
                return true;
            }

        });
        return this;
    }
    
    public Network snap(Tolerance tolerance){
        int size = map.size();
        int i = size - 1;
        if(i <= 0){
            return this;
        }
        VectList vects = getVects(new VectList());
        Vect a = new Vect();
        Vect b = new Vect();
        vects.get(i, a);
        while(i-- > 0){
            vects.get(i, a);
            int j = i;
            while(++j < size){
                vects.get(j, b);
                if(!tolerance.match(a.getX(), b.getX())){
                    break;
                }
                if(a.match(b, tolerance)){
                    VectList links = map.get(b);
                    removeVertex(b);
                    for(int k = 0; k < links.size(); k++){
                        links.get(k, b);
                        addLink(a, b);
                    }
                }
            }
        }
        return this;
    }

    public VectList extractPoints(VectList results) {
        for (VectMap<VectList>.Iter iter = map.iterator(); iter.next();) {
            if (iter.getValue().isEmpty()) {
                results.addInternal(iter.getX(), iter.getY());
            }
        }
        return results;
    }

    public Collection<LineString> extractLines(Collection<LineString> results) {
        Set<Line> done = new HashSet<>();
        Line line = new Line();
        Vect origin = new Vect();
        Vect a = new Vect();
        Vect b = new Vect();
        Vect c = new Vect();
        for (VectMap<VectList>.Iter iter = map.iterator(); iter.next();) {
            iter.getVect(origin);
            VectList links = iter.getValue();
            if (links.size() != 2) { // a nexus
                for (int i = 0; i < links.size(); i++) {
                    a.set(origin);
                    links.get(i, b);
                    line.set(a, b).normalize();
                    if (done.contains(line)) {
                        continue;
                    }
                    done.add(line.clone());
                    VectList vects = followLine(a, b, c, new VectList().add(a));
                    results.add(new LineString(vects));
                    int s = vects.size();
                    done.add(new Line(vects.getX(s - 2), vects.getY(s - 2), vects.getX(s - 1), vects.getY(s - 1)).normalize());
                }
            }
        }
        return results;
    }
    
    public Collection<LineString> extractHangingLines(Collection<LineString> results) {
        Set<Line> done = new HashSet<>();
        Line line = new Line();
        Vect origin = new Vect();
        Vect a = new Vect();
        Vect b = new Vect();
        Vect c = new Vect();
        for (VectMap<VectList>.Iter iter = map.iterator(); iter.next();) {
            iter.getVect(origin);
            VectList links = iter.getValue();
            if (links.size() == 1) { // a nexus
                for (int i = 0; i < links.size(); i++) {
                    a.set(origin);
                    links.get(i, b);
                    line.set(a, b).normalize();
                    if (done.contains(line)) {
                        continue;
                    }
                    done.add(line.clone());
                    VectList vects = followLine(a, b, c, new VectList().add(a));
                    results.add(new LineString(vects));
                    int s = vects.size();
                    done.add(new Line(vects.getX(s - 2), vects.getY(s - 2), vects.getX(s - 1), vects.getY(s - 1)).normalize());
                }
            }
        }
        return results;
    }
    
    public Collection<Ring> extractRings(Collection<Ring> results){
        Set<Line> visited = new HashSet<>();
        Set<Vect> currentVisited = new HashSet<>();
        VectList current = new VectList();
        VectList vects = getVects(new VectList());
        Line link = new Line();
        for(int v = 0; v < vects.size(); v++){
            Vect a = vects.get(v, new Vect()); //get vect
            VectList links = map.get(a);
            for(int i = 0; i < links.size(); i++){
                Vect b = links.get(i, new Vect());
                link.set(a, b);
                link.normalize();
                if(!visited.contains(link)){
                    visited.add(link.clone());
                    Vect c = followPath(a, b, current, currentVisited);
                    if(a.equals(c)){
                        Ring result = new Ring(current.clone());
                        if(result.getArea() > 0){
                            for(int k = current.size(); k-- > 0;){
                                visited.add(current.get(0, new Line()).normalize());
                            }
                            results.add(result);
                        }
                    }
                }
            }
        }
        return results;
    }
    
    Vect followPath(Vect a, Vect b, VectList path, Set<Vect> visited){
        path.clear();
        path.add(a);
        path.add(b);
        visited.clear();
        visited.add(a);
        visited.add(b);
        while(true){
            Vect c = new Vect();
            if(!nextCCW(a, b, c)){
                return null;
            }
            path.add(c);
            if(visited.contains(c)){
                return c;
            }
            visited.add(c);
            a = b;
            b = c;
        }
    }
    
    public RingSet extractRingSet(){
        RingSet ret = new RingSet(null);
        Collection<Ring> rings = extractRings(new ArrayList<Ring>());
        for(Ring ring : rings){
            ret.addInternal(new RingSet(ring));
        }
        if(ret.children.size() == 1){
            return ret.children.get(0);
        }
        return ret;
    }

    private VectList followLine(Vect a, Vect b, Vect c, VectList results) {
        while (true) {
            results.add(b);
            VectList links = map.get(b);
            if (links.size() != 2) {
                break;
            }
            links.get(0, c);
            if (c.equals(a)) {
                links.get(1, c);
            }
            Vect d = a;
            a = b;
            b = c;
            c = d;
        }
        results.add(b);
        return results;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Network)) {
            return false;
        }
        Network network = (Network) obj;
        if (network.numVects() != numVects()) {
            return false;
        }
        for (VectMap<VectList>.Iter iter = map.iterator(); iter.next();) {
            VectList a = iter.getValue();
            VectList b = network.map.getInternal(iter.getX(), iter.getY());
            if (!Objects.equals(a, b)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        VectList vects = getVects(new VectList());
        Vect vect = new Vect();
        for (int i = 0; i < vects.size(); i++) {
            vects.get(i, vect);
            hash = 59 * hash + vect.hashCode();
            VectList links = map.get(vect);
            hash = 59 * hash + links.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        toStringInternal(str);
        return str.toString();
    }

    void toStringInternal(Appendable appendable) {
        try {
            toString(appendable);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void toString(Appendable appendable) throws IOException {
        appendable.append('[');
        VectList vects = getVects(new VectList());
        Vect a = new Vect();
        Vect b = new Vect();
        boolean comma = false;
        for (int i = 0; i < vects.size(); i++) {
            vects.get(i, a);
            if (comma) {
                appendable.append(',');
            } else {
                comma = true;
            }
            appendable.append('[').append(Util.ordToStr(a.getX())).append(',')
                    .append(Util.ordToStr(a.getY()));
            VectList links = map.get(a);
            for (int j = 0; j < links.size(); j++) {
                links.get(j, b);
                if (a.compareTo(b) < 0) {
                    appendable.append(", ").append(Util.ordToStr(b.getX())).append(',')
                            .append(Util.ordToStr(b.getY()));
                }
            }
            appendable.append(']');
        }
        appendable.append(']');
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        //Get counts for printing first
        int linkCount = 0;
        int vertexCount = 0;
        Vect a = new Vect();
        Vect b = new Vect();
        for (VectMap<VectList>.Iter iter = map.iterator(); iter.next();) {
            iter.getVect(a);
            VectList links = iter.getValue();
            if (links.isEmpty()) {
                vertexCount++;
            } else {
                for (int i = 0; i < links.size(); i++) {
                    links.get(i, b);
                    if (a.compareTo(b) < 0) {
                        linkCount++;
                    }
                }
            }
        }

        //print links
        out.writeInt(linkCount);
        if (linkCount != 0) {
            for (VectMap<VectList>.Iter iter = map.iterator(); iter.next();) {
                iter.getVect(a);
                VectList links = iter.getValue();
                for (int i = 0; i < links.size(); i++) {
                    links.get(i, b);
                    if (a.compareTo(b) < 0) {
                        out.writeDouble(a.getX());
                        out.writeDouble(a.getY());
                        out.writeDouble(b.getX());
                        out.writeDouble(b.getY());
                    }
                }
            }
        }

        //print vertices
        out.writeInt(vertexCount);
        if (vertexCount != 0) {
            for (VectMap<VectList>.Iter iter = map.iterator(); iter.next();) {
                iter.getVect(a);
                VectList links = iter.getValue();
                for (int i = 0; i < links.size(); i++) {
                    links.get(i, b);
                    if (links.isEmpty()) {
                        out.writeDouble(a.getX());
                        out.writeDouble(a.getY());
                    }
                }
            }
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        clear();
        //Get counts for printing first
        int linkCount = in.readInt();
        Vect a = new Vect();
        Vect b = new Vect();
        for (int i = linkCount; i-- > 0;) {
            a.set(in.readDouble(), in.readDouble());
            b.set(in.readDouble(), in.readDouble());
            addLink(a, b);
        }
        int vertexCount = in.readInt();
        for (int i = vertexCount; i-- > 0;) {
            a.set(in.readDouble(), in.readDouble());
            addVertex(a);
        }
    }

    public final class Iter {

        private VectMap<VectList>.Iter iter;

        private Iter() {
            this.iter = map.iterator();
        }

        public boolean next() {
            return iter.next();
        }
        
        
        public double getX() {
            return iter.getX();
        }

        public double getY() {
            return iter.getY();
        }

        public Vect getVect(Vect target) {
            return iter.getVect(target);
        }

        public VectList getLinks(VectList links) {
            return links.addAll(iter.getValue());
        }
    }

    class IntersectionFinder implements NodeProcessor<Line> {

        final Tolerance tolerance;
        Line i;
        final Vect intersection;
        final Vect a;
        final Vect b;

        public IntersectionFinder(Tolerance tolerance) {
            this.tolerance = tolerance;
            intersection = new Vect();
            a = new Vect();
            b = new Vect();
        }

        @Override
        public boolean process(SpatialNode<Line> node, int index) {
            Line j = node.getItemValue(index);
            if (i.intersectionSeg(i, tolerance, intersection)) {
                i.getA(a);
                i.getB(b);
                removeLink(a, b);
                addLink(a, intersection);
                addLink(intersection, b);
                j.getA(a);
                j.getB(b);
                removeLink(a, b);
                addLink(a, intersection);
                addLink(intersection, b);
            }
            return true;
        }

    }

}
