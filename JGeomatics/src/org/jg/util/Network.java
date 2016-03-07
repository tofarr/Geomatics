package org.jg.util;

import org.jg.geom.Ring;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.jg.geom.GeomException;
import org.jg.geom.Line;
import org.jg.geom.Rect;
import org.jg.geom.RingSet;
import org.jg.geom.Vect;
import org.jg.geom.VectBuilder;
import org.jg.util.SpatialNode.NodeProcessor;
import org.jg.util.VectMap.VectMapProcessor;

/**
 *
 * @author tim.ofarrell
 */
public final class Network implements Externalizable, Cloneable {

    private final VectMap<VectList> map;
    private int numLinks;
    private RTree<Line> cachedLinks;

    public Network() {
        map = new VectMap<>();
    }

    public int numVects() {
        return map.size();
    }

    public int numLinks() {
        return numLinks;
    }

    public boolean hasVect(Vect vect) throws NullPointerException {
        return map.containsKey(vect);
    }

    public boolean hasVect(double x, double y) throws IllegalArgumentException {
        return map.containsKey(x, y);
    }

    public boolean hasLink(Vect a, Vect b) throws NullPointerException {
        VectList links = map.get(a);
        return (links != null) && (links.indexOf(b, 0) >= 0);
    }

    public boolean hasLink(double ax, double ay, double bx, double by) throws IllegalArgumentException {
        VectList links = map.get(ax, ay);
        return (links != null) && (links.indexOf(bx, by, 0) >= 0);
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

    public boolean nextCW(Vect vect, Vect link, VectBuilder target) throws NullPointerException {
        VectList links = map.get(vect);
        if (links != null) {
            int index = links.indexOf(link, 0);
            if (index >= 0) {
                index++;
                if (index == links.size()) {
                    index = 0;
                }
                links.getVect(index, target);
                return true;
            }
        }
        return false;
    }

    public boolean nextCCW(Vect vect, Vect link, VectBuilder target) throws NullPointerException, IllegalArgumentException {
        VectList links = map.get(vect);
        if (links != null) {
            int index = links.indexOf(link, 0);
            if (index >= 0) {
                if (index == 0) {
                    index = links.size();
                }
                index--;
                links.getVect(index, target);
                return true;
            }
        }
        return false;
    }

    public VectList vectList(VectList target) {
        return map.keyList(target);
    }

    public VectSet vectSet(VectSet target) throws NullPointerException {
        return map.keySet(target);
    }

    public VectList extractPoints(final VectList target) throws NullPointerException {
        map.forEach(new VectMapProcessor<VectList>() {
            @Override
            public boolean process(double x, double y, VectList value) {
                if (value.isEmpty()) {
                    target.add(x, y);
                }
                return true;
            }
        });
        return target;
    }

    public List<VectList> extractLines(boolean includePnts, List<VectList> target) throws NullPointerException {
        final VectList allVectsSorted = getVects(new VectList(map.size()));
        allVectsSorted.sort();
        VectList path = new VectList();
        for (int i = 0; i < allVectsSorted.size(); i++) {
            double ax = allVectsSorted.getX(i);
            double ay = allVectsSorted.getY(i);
            VectList links = map.get(ax, ay);
            switch (links.size()) {
                case 0:
                    if (includePnts) {
                        VectList vects = new VectList(1);
                        vects.addInternal(ax, ay);
                        target.add(vects);
                    }
                    break;
                case 2: {
                    double bx = links.getX(0);
                    double by = links.getY(0);
                    double cx = links.getX(1);
                    double cy = links.getY(1);
                    if ((Vect.compare(ax, ay, bx, by) < 0) && (Vect.compare(ax, ay, cx, cy) < 0)) {
                        path.clear();
                        followLineString(ax, ay, bx, by, path);
                        if ((ax == path.getX(path.size() - 1)) && (ay == path.getY(path.size() - 1))) {
                            target.add(path);
                            path = new VectList();
                        }
                    }
                    break; // internal link. Do nothing
                }
                default: {
                    path.clear();
                    followLineString(ax, ay, links.getX(0), links.getY(0), path);
                    double bx = links.getX(links.size() - 1);
                    double by = links.getY(links.size() - 1);
                    if (Vect.compare(ax, ay, bx, by) <= 0) {
                        target.add(path);
                        path = new VectList();
                    }
                }
            }
        }
        return target;
    }

    void followLineString(double sx, double sy, double bx, double by, VectList result) {
        double ax = sx;
        double ay = sy;
        result.addInternal(ax, ay);
        while (true) {
            result.addInternal(bx, by);
            VectList links = map.get(bx, by);
            if ((links.size() != 2) || ((sx == bx) && (sy == by))) {
                return;
            }
            double cx = links.getX(0);
            double cy = links.getY(0);
            if ((cx == ax) && (cy == ay)) {
                cx = links.getX(1);
                cy = links.getY(1);
            }
            ax = bx;
            ay = by;
            bx = cx;
            by = cy;
        }
    }

    public boolean addVertex(Vect vect) throws NullPointerException {
        return addVertexInternal(vect.x, vect.y);
    }

    public boolean addVertex(double x, double y) throws IllegalArgumentException {
        Vect.check(x, y);
        return addVertexInternal(x, y);
    }

    public boolean addVertex(VectBuilder vect) throws NullPointerException {
        return addVertexInternal(vect.getX(), vect.getY());
    }
    
    boolean addVertexInternal(double x, double y){
        VectList links = map.get(x, y);
        if (links == null) {
            links = new VectList(2);
            map.put(x, y, links);
            return true;
        }
        return false;   
    }

    public boolean removeVertex(double x, double y) throws IllegalArgumentException {
        Vect.check(x, y);
        return removeVertexInternal(x, y);
    }

    public boolean removeVertex(Vect vect) throws NullPointerException {
        return removeVertexInternal(vect.x, vect.y);
    }
    
    public boolean removeVertex(VectBuilder vect) throws NullPointerException {
        return removeVertexInternal(vect.getX(), vect.getY());
    }

    boolean removeVertexInternal(double x, double y) {
        VectList links = map.remove(x, y);
        if (links == null) {
            return false;
        }
        for (int i = 0; i < links.size(); i++) {
            double bx = links.getX(i);
            double by = links.getY(i);
            VectList backLinks = map.get(bx, by);
            int index = backLinks.indexOf(x, y, 0);
            backLinks.remove(index);
        }
        numLinks -= links.size();
        return true;
    }

    //does nothing if points are the same
    public boolean addLink(Vect a, Vect b) throws NullPointerException {
        return addLinkInternal(a.x, a.y, b.x, b.y);
    }

//does nothing if points are the same
    public boolean addLink(VectBuilder a, VectBuilder b) throws NullPointerException {
        return addLinkInternal(a.getX(), a.getY(), b.getX(), b.getY());
    }

    //does nothing if points are the same
    public boolean addLink(double ax, double ay, double bx, double by) throws IllegalArgumentException {
        Vect.check(ax, ay);
        Vect.check(bx, by);
        return addLinkInternal(ax, ay, bx, by);
    }

    //does nothing if points are the same
    public boolean addLinkInternal(double ax, double ay, double bx, double by) {
        if ((ax == bx) && (ay == by)) {
            return false;
        }
        VectList links = map.get(ax, ay);
        if (links == null) {
            links = new VectList(2);
            links.add(bx, by);
            map.put(ax, ay, links);
        } else {
            int index = links.indexOf(bx, by, 0);
            if (index >= 0) {
                return false; // link already exists
            }
            insertLink(ax, ay, bx, by, links);
        }
        links = map.get(bx, by);
        if (links == null) {
            links = new VectList(2);
            links.add(ax, ay);
            map.put(bx, by, links);
        } else {
            insertLink(bx, by, ax, by, links);
        }
        numLinks++;
        cachedLinks = null;
        return true;
    }

    private static void insertLink(double ox, double oy, double tx, double ty, VectList links) {
        int min = 0;
        int max = links.size();
        double ndx = tx - ox;
        double ndy = ty - oy;
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
        links.insert(min, tx, ty);
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
        } else if (jdx >= 0) {
            return -1;
        } else {
            return Double.compare((idy / idx), (jdy / jdx));
        }
    }

    public boolean removeLink(double ax, double ay, double bx, double by) throws IllegalArgumentException {
        return removeLinkInternal(ax, ay, bx, by);
    }

    public boolean removeLink(Vect a, Vect b) throws NullPointerException {
        return removeLink(a.x, a.y, b.x, b.y);
    }

    boolean removeLinkInternal(double ax, double ay, double bx, double by) {
        VectList links = map.get(ax, ay);
        if (links == null) {
            return false;
        }
        int index = links.indexOf(bx, by, 0);
        if (index < 0) {
            return false;
        }
        links.remove(index);
        links = map.get(bx, by);
        index = links.indexOf(ax, ay, 0);
        links.remove(index);
        numLinks--;
        cachedLinks = null;
        return true;
    }

    public Network clear() {
        map.clear();
        numLinks = 0;
        cachedLinks = null;
        return this;
    }

    public Network addAllVertices(VectList vects) {
        for(int i = 0; i < vects.size(); i++){
            addVertexInternal(vects.getX(i), vects.getY(i));
        }
        return this;
    }

    public Network addAllLinks(VectList links) {
        if (links.size() <= 1) {
            return this;
        }
        double ax = links.getX(0);
        double ay = links.getY(0);
        for (int i = 1; i < links.size(); i++) {
            double bx = links.getX(i);
            double by = links.getY(i);
            addLinkInternal(ax, ay, bx, by);
            ax = bx;
            ay = by;
        }
        return this;
    }

    //Get all vectors, sorted from min to max
    public VectList getVects(VectList target) {
        map.keyList(target);
        target.sort();
        return target;

    }

    RTree<Line> getLinks() {
        RTree<Line> ret = cachedLinks;
        if(ret == null){
            final Rect[] bounds = new Rect[numLinks];
            final Line[] links = new Line[numLinks];
            map.forEach(new VectMapProcessor<VectList>(){
                
                int index = 0;
                
                @Override
                public boolean process(double ax, double ay, VectList value) {
                    for(int i = 0; i < value.size(); i++){
                        double bx = value.getX(i);
                        double by = value.getY(i);
                        if(Vect.compare(ax, ay, bx, by) < 0){
                            bounds[index] = Rect.valueOf(ax, ay, bx, by);
                            links[index] = Line.valueOf(ax, ay, bx, by);
                            index++;
                        }
                    }
                    return true;
                }
            
            });
            ret = new RTree<Line>(bounds, links);
            cachedLinks = ret;
        }
        return ret;
    }

    public boolean forEachLink(NodeProcessor<Line> processor) throws NullPointerException {
        return getLinks().forEach(processor);
    }

    public boolean forInteractingLinks(Rect rect, NodeProcessor<Line> processor) throws NullPointerException {
        return getLinks().forInteracting(rect, processor);
    }

    public boolean forOverlappingLinks(Rect rect, NodeProcessor<Line> processor) throws NullPointerException {
        return getLinks().forOverlapping(rect, processor);
    }
    

    //modifications during iteration are not permitted
//    public Iter iterator() {
//        return new Iter();
//    }
    //Make all points of self intersection explicit
    public Network explicitIntersections(Tolerance tolerance) {
        throw new UnsupportedOperationException();
//        final RTree<Line> lines = getLinks();
//        final IntersectionFinder finder = new IntersectionFinder(tolerance);
//        lines.root.get(new NodeProcessor<Line>() {
//
//            final Rect rect = new Rect();
//
//            @Override
//            public boolean process(SpatialNode<Line> leaf, int index) {
//                Line value = leaf.getItemValue(index);
//                leaf.getItemBounds(index, rect);
//                finder.reset(value);
//                lines.root.getInteracting(rect, finder);
//                VectList intersections = finder.intersections;
//                if (intersections.size() > 0) {
//                    Vect a = finder.a;
//                    Vect b = finder.b;
//                    Vect n = finder.intersection;
//                    removeLink(a, b);
//                    intersections.sort();
//                    if (Vect.compare(value.ax, value.ay, value.bx, value.by) > 0) {
//                        intersections.reverse();
//                    }
//                    for (int i = intersections.size(); i-- > 0;) {
//                        intersections.get(i, n);
//                        addLink(n, b);
//                        Vect c = b;
//                        b = n;
//                        n = c;
//                    }
//                    addLink(a, b);
//                }
//                return true;
//            }
//
//        });
//        return this;
    }

    public Network snap(Tolerance tolerance) {
        throw new UnsupportedOperationException();
//        int size = map.size();
//        int i = size - 1;
//        if (i <= 0) {
//            return this;
//        }
//        VectList vects = getVects(new VectList());
//        Vect a = new Vect();
//        Vect b = new Vect();
//        vects.get(i, a);
//        while (i-- > 0) {
//            vects.get(i, a);
//            int j = i;
//            while (++j < size) {
//                vects.get(j, b);
//                if (!tolerance.match(a.getX(), b.getX())) {
//                    break;
//                }
//                if (a.match(b, tolerance)) {
//                    VectList links = map.get(b);
//                    removeVertex(b);
//                    for (int k = 0; k < links.size(); k++) {
//                        links.get(k, b);
//                        addLink(a, b);
//                    }
//                }
//            }
//        }
//        return this;
    }

    public Collection<VectList> extractLines(Collection<VectList> results, boolean includePoints) {
        VectList vects = getVects(new VectList());
        VectBuilder a = new VectBuilder();
        VectBuilder b = new VectBuilder();
        VectBuilder c = new VectBuilder();
        VectList result = new VectList();
        int numLinksProcessed = 0;
        for (int i = 0; i < vects.size(); i++) {
            vects.getVect(i, a);
            VectList links = map.get(a.getX(), a.getY());
            switch (links.size()) {
                case 0:
                    if (includePoints) {
                        results.add(new VectList().add(a.getX(), a.getY()));
                    }
                    break;
                case 2:
                    //this misses rings! Need to get these on second pass
                    break;
                default:
                    for (int j = 0; j < links.size(); j++) {
                        links.getVect(j, b);
                        result.clear().add(a);
                        followLine(a, b, c, result);
                        if (result.isOrdered()) {
                            results.add(result.clone());
                            numLinksProcessed += (result.size() - 1);
                        }
                    }
            }
        }
        if (numLinksProcessed == numLinks) { // if there were no rings, then we are done
            return results;
        }

        //Process rings in second pass
        VectBuilder d = new VectBuilder();
        for (int i = 0; i < vects.size(); i++) {
            vects.getVect(i, a);
            VectList links = map.get(vects.getX(i), vects.getY(i));
            if (links.size() == 2) { // If there are 2 links, then this could be part of an unconnected ring

                links.getVect(0, b);
                links.getVect(1, c);

                //if both linked vertices are greater than the current one, then this may be the start point
                //of an unconnected linear ring. All points on the ring will be greater than a
                if ((a.compareTo(b) > 0) || (a.compareTo(c) > 0)) {
                    break; // point was less than a - continue
                }

                //to begin, we pick the direction with the lower dydx
                if (Vect.dydxTo(a.getX(), a.getY(), b.getX(), b.getY()) < Vect.dydxTo(a.getX(), a.getY(), c.getX(), c.getY())) {
                    c.set(b);
                }

                b.set(a);

                //follow line, copying into result. If we come across a point which is less than
                // a or has more than 2 links, discontinue.
                result.clear().add(b);
                while (true) {
                    result.add(c);
                    links = map.get(c);
                    if (links.size() != 2) {
                        break; // this is not a ring!
                    }
                    links.getVect(0, d);
                    if (d.equals(b)) {
                        links.getVect(1, d);
                    }
                    int cmp = d.compareTo(a);
                    if (cmp < 0) {
                        break; // found a point before origin of ring.
                    } else if (cmp == 0) { // cycled back to origin - we have a ring!
                        result.add(a);
                        results.add(result.clone());
                        break;
                    }
                    VectBuilder t = b;
                    b = c;
                    c = d;
                    d = t;
                }
            }
        }

        return results;
    }

    public Collection<Ring> extractRings(Collection<Ring> results) {
        throw new UnsupportedOperationException();
//        Set<Line> visited = new HashSet<>();
//        Set<Vect> currentVisited = new HashSet<>();
//        VectList current = new VectList();
//        VectList vects = getVects(new VectList());
//        Line link = new Line();
//        for (int v = 0; v < vects.size(); v++) {
//            Vect a = vects.get(v, new Vect()); //get vect
//            VectList links = map.get(a);
//            for (int i = 0; i < links.size(); i++) {
//                Vect b = links.get(i, new Vect());
//                link.set(a, b);
//                link.normalize();
//                if (!visited.contains(link)) {
//                    visited.add(link.clone());
//                    Vect c = followPath(a, b, current, currentVisited);
//                    if (a.equals(c)) {
//                        Ring result = new Ring(current.clone());
//                        if (result.getArea() > 0) {
//                            for (int k = current.size(); k-- > 0;) {
//                                visited.add(current.get(0, new Line()).normalize());
//                            }
//                            results.add(result);
//                        }
//                    }
//                }
//            }
//        }
//        return results;
    }

    public RingSet extractRingSet() {
        throw new UnsupportedOperationException();
//        RingSet ret = new RingSet(null);
//        Collection<Ring> rings = extractRings(new ArrayList<Ring>());
//        for (Ring ring : rings) {
//            ret.addInternal(new RingSet(ring));
//        }
//        if (ret.children.size() == 1) {
//            return ret.children.get(0);
//        }
//        return ret;
    }

    private VectList followLine(VectBuilder a, VectBuilder b, VectBuilder c, VectList results) {
        while (true) {
            results.add(b.getX(), b.getY());
            VectList links = map.get(b.getX(), b.getY());
            if (links.size() != 2) {
                return results;
            }
            links.getVect(1, c);
            if (c.equals(a)) {
                links.getVect(0, c);
            }
            VectBuilder d = a;
            a = b;
            b = c;
            c = d;
        }
    }

    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException();
//        if (!(obj instanceof Network)) {
//            return false;
//        }
//        Network network = (Network) obj;
//        if (network.numVects() != numVects()) {
//            return false;
//        }
//        for (VectMap<VectList>.Iter iter = map.iterator(); iter.next();) {
//            VectList a = iter.getValue();
//            VectList b = network.map.getInternal(iter.getX(), iter.getY());
//            if (!Objects.equals(a, b)) {
//                return false;
//            }
//        }
//        return true;
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
//        int hash = 7;
//        VectList vects = getVects(new VectList());
//        Vect vect = new Vect();
//        for (int i = 0; i < vects.size(); i++) {
//            vects.get(i, vect);
//            hash = 59 * hash + vect.hashCode();
//            VectList links = map.get(vect);
//            hash = 59 * hash + links.hashCode();
//        }
//        return hash;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        toString(str);
        return str.toString();
    }

    public void toString(Appendable appendable) throws GeomException {
        try {
            ArrayList<VectList> linesAndPointList = new ArrayList<>();
            extractLines(linesAndPointList, true);
            VectList[] linesAndPoints = linesAndPointList.toArray(new VectList[linesAndPointList.size()]);
            Arrays.sort(linesAndPoints);
            appendable.append('[');
            boolean comma = false;
            for (int i = 0; i < linesAndPoints.length; i++) {
                if (comma) {
                    appendable.append(',');
                } else {
                    comma = true;
                }
                linesAndPoints[i].toString(appendable);
            }
            appendable.append(']');
        } catch (IOException ex) {
            throw new GeomException("Error writing network", ex);
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        writeData(out);
    }

    /**
     * Write this network to the output given
     *
     * @param out
     * @throws IOException if out was null
     * @throws NullPointerException if out was null
     */
    public void writeData(DataOutput out) throws IOException, NullPointerException {
        throw new UnsupportedOperationException();
//        ArrayList<VectList> linesAndPoints = new ArrayList<>();
//        extractLines(linesAndPoints, true);
//        out.writeInt(linesAndPoints.size());
//        for (int i = 0; i < linesAndPoints.size(); i++) {
//            linesAndPoints.get(i).writeData(out);
//        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        readData(in);
    }

    /**
     * Set the vertices and links from the input given
     *
     * @param in
     * @return this
     * @throws IOException if there was an error
     * @throws NullPointerException if in was null
     */
    public Network readData(DataInput in) throws IOException, NullPointerException {
        throw new UnsupportedOperationException();
//        clear();
//        int numLineStrings = in.readInt();
//        for (int i = numLineStrings; i-- > 0;) {
//            VectList links = VectList.read(in);
//            addAllLinks(links);
//        }
//        return this;
    }

    /**
     * Read vertices and links from the input given
     *
     * @param in
     * @return a line
     * @throws IOException if there was an error
     * @throws NullPointerException if in was null
     */
    public static Network read(DataInput in) throws IOException, NullPointerException {
        return new Network().readData(in);
    }

//    public final class Iter {
//
//        private VectMap<VectList>.Iter iter;
//
//        private Iter() {
//            this.iter = map.iterator();
//        }
//
//        public boolean next() {
//            return iter.next();
//        }
//
//        public double getX() {
//            return iter.getX();
//        }
//
//        public double getY() {
//            return iter.getY();
//        }
//
//        public Vect getVect(Vect target) {
//            return iter.getVect(target);
//        }
//
//        public VectList getLinks(VectList links) {
//            return links.addAll(iter.getValue());
//        }
//    }
//
//    class IntersectionFinder implements NodeProcessor<Line> {
//
//        final Tolerance tolerance;
//        Line i;
//        final Vect a;
//        final Vect b;
//        final Vect intersection;
//        final VectList intersections;
//
//        IntersectionFinder(Tolerance tolerance) {
//            this.tolerance = tolerance;
//            this.a = new Vect();
//            this.b = new Vect();
//            this.intersection = new Vect();
//            this.intersections = new VectList();
//        }
//
//        void reset(Line i) {
//            this.i = i;
//            this.i.getA(a);
//            this.i.getB(b);
//            this.intersections.clear();
//        }
//
//        @Override
//        public boolean process(SpatialNode<Line> node, int index) {
//            Line j = node.getItemValue(index);
//            if ((!i.equals(j)) && i.intersectionSeg(j, tolerance, intersection)) {
//                if (!(a.equals(intersection) || b.equals(intersection))) {
//                    intersections.add(intersection);
//                }
//            }
//            return true;
//        }
//
//    }
//
//    @Override
//    public Network clone() {
//        Network network = new Network();
//        Vect vect = new Vect();
//        for(VectMap<VectList>.Iter iter = map.iterator(); iter.next();){
//            iter.getVect(vect);
//            network.map.put(vect, iter.getValue().clone());
//        }
//        network.numLinks = numLinks;
//        return network;
//    }
}
