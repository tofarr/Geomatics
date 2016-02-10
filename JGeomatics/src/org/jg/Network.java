package org.jg;

import org.jg.RingSet;
import org.jg.Ring;
import org.jg.LineString;
import java.io.DataInput;
import java.io.DataOutput;
import org.jg.SpatialNode.NodeProcessor;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author tim.ofarrell
 */
public final class Network implements Externalizable, Cloneable {

    private final VectMap<VectList> map;
    private int numLinks;

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
        } else if (jdx >= 0) {
            return -1;
        } else {
            return Double.compare((idy / idx), (jdy / jdx));
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
        links = map.get(b);
        index = links.indexOf(a, 0);
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

    public RTree<Line> getLinks() {
        double[] itemBounds = new double[numLinks << 2];
        Line[] itemValues = new Line[numLinks];
        int boundIndex = 0;
        int valueIndex = 0;
        Rect itemBound = new Rect();
        Vect a = new Vect();
        Vect b = new Vect();
        for (VectMap<VectList>.Iter iter = map.iterator(); iter.next();) {
            iter.getVect(a);
            VectList vects = iter.getValue();
            for (int i = 0; i < vects.size(); i++) {
                vects.get(i, b);
                if (a.compareTo(b) < 0) {
                    itemBound.reset().union(a).union(b);
                    itemBounds[boundIndex++] = itemBound.minX;
                    itemBounds[boundIndex++] = itemBound.minY;
                    itemBounds[boundIndex++] = itemBound.maxX;
                    itemBounds[boundIndex++] = itemBound.maxY;
                    itemValues[valueIndex++] = new Line(a, b);
                }
            }
        }
        RTree<Line> ret = new RTree<>(itemBounds, itemValues);
        return ret;
    }

    //modifications during iteration are not permitted
    public Iter iterator() {
        return new Iter();
    }

    //Make all points of self intersection explicit
    public Network explicitIntersections(Tolerance tolerance) {
        final RTree<Line> lines = getLinks();
        final IntersectionFinder finder = new IntersectionFinder(tolerance);
        lines.root.get(new NodeProcessor<Line>() {

            final Rect rect = new Rect();

            @Override
            public boolean process(SpatialNode<Line> leaf, int index) {
                Line value = leaf.getItemValue(index);
                leaf.getItemBounds(index, rect);
                finder.reset(value);
                lines.root.getInteracting(rect, finder);
                VectList intersections = finder.intersections;
                if (intersections.size() > 0) {
                    Vect a = finder.a;
                    Vect b = finder.b;
                    Vect n = finder.intersection;
                    removeLink(a, b);
                    intersections.sort();
                    if (Vect.compare(value.ax, value.ay, value.bx, value.by) > 0) {
                        intersections.reverse();
                    }
                    for (int i = intersections.size(); i-- > 0;) {
                        intersections.get(i, n);
                        addLink(n, b);
                        Vect c = b;
                        b = n;
                        n = c;
                    }
                    addLink(a, b);
                }
                return true;
            }

        });
        return this;
    }

    public Network snap(Tolerance tolerance) {
        int size = map.size();
        int i = size - 1;
        if (i <= 0) {
            return this;
        }
        VectList vects = getVects(new VectList());
        Vect a = new Vect();
        Vect b = new Vect();
        vects.get(i, a);
        while (i-- > 0) {
            vects.get(i, a);
            int j = i;
            while (++j < size) {
                vects.get(j, b);
                if (!tolerance.match(a.getX(), b.getX())) {
                    break;
                }
                if (a.match(b, tolerance)) {
                    VectList links = map.get(b);
                    removeVertex(b);
                    for (int k = 0; k < links.size(); k++) {
                        links.get(k, b);
                        addLink(a, b);
                    }
                }
            }
        }
        return this;
    }

    public Collection<VectList> extractLines(Collection<VectList> results, boolean includePoints) {
        VectList vects = getVects(new VectList());
        HashSet<Line> done = new HashSet<>();
        Vect a = new Vect();
        Vect b = new Vect();
        Vect c = new Vect();
        Line testLine = new Line();
        for (int i = 0; i < vects.size(); i++) {
            vects.get(i, a);
            VectList links = map.get(a);
            switch (links.size()) {
                case 0:
                    if(includePoints){
                        results.add(new VectList().add(a));
                    }
                    break;
                case 2:
                    //this misses rings! Need to get these on second pass
                    break;
                default:
                    for (int j = 0; j < links.size(); j++) {
                        links.get(j, b);
                        testLine.set(a, b).normalize();
                        if (!done.contains(testLine)) {
                            VectList result = followLine(a, b, c, new VectList().add(a));
                            for (int k = result.size() - 1; k-- > 0;) {
                                done.add(result.get(k, new Line()).normalize());
                            }
                            results.add(result);
                        }
                    }
            }
        }
        if (done.size() == (vects.size() - 1)) {
            return results;
        }
        Vect d = new Vect();
        for (int i = 0; i < vects.size(); i++) {
            vects.get(i, a);
            VectList links = map.get(a);
            if (links.size() == 2) {
                links.get(0, c);
                testLine.set(a, c).normalize();
                if (!done.contains(testLine)) {
                    VectList result = new VectList().add(a).add(c);
                    b.set(a);
                    while (!c.equals(a)) {
                        links = map.get(c);
                        links.get(1, d);
                        if (d.equals(b)) {
                            links.get(0, d);
                        }
                        result.add(d);
                        Vect e = b; // rotate
                        b = c;
                        c = d;
                        d = e;
                    }
                }
            }
        }
        
        return results;
    }

    public VectList extractPoints(VectList results) {
        for (VectMap<VectList>.Iter iter = map.iterator(); iter.next();) {
            if (iter.getValue().isEmpty()) {
                results.addInternal(iter.getX(), iter.getY());
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

    public Collection<Ring> extractRings(Collection<Ring> results) {
        Set<Line> visited = new HashSet<>();
        Set<Vect> currentVisited = new HashSet<>();
        VectList current = new VectList();
        VectList vects = getVects(new VectList());
        Line link = new Line();
        for (int v = 0; v < vects.size(); v++) {
            Vect a = vects.get(v, new Vect()); //get vect
            VectList links = map.get(a);
            for (int i = 0; i < links.size(); i++) {
                Vect b = links.get(i, new Vect());
                link.set(a, b);
                link.normalize();
                if (!visited.contains(link)) {
                    visited.add(link.clone());
                    Vect c = followPath(a, b, current, currentVisited);
                    if (a.equals(c)) {
                        Ring result = new Ring(current.clone());
                        if (result.getArea() > 0) {
                            for (int k = current.size(); k-- > 0;) {
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

    Vect followPath(Vect a, Vect b, VectList path, Set<Vect> visited) {
        path.clear();
        path.add(a);
        path.add(b);
        visited.clear();
        visited.add(a);
        visited.add(b);
        while (true) {
            Vect c = new Vect();
            if (!nextCCW(a, b, c)) {
                return null;
            }
            path.add(c);
            if (visited.contains(c)) {
                return c;
            }
            visited.add(c);
            a = b;
            b = c;
        }
    }

    public RingSet extractRingSet() {
        RingSet ret = new RingSet(null);
        Collection<Ring> rings = extractRings(new ArrayList<Ring>());
        for (Ring ring : rings) {
            ret.addInternal(new RingSet(ring));
        }
        if (ret.children.size() == 1) {
            return ret.children.get(0);
        }
        return ret;
    }

    private VectList followLine(Vect a, Vect b, Vect c, VectList results) {
        while (true) {
            results.add(b);
            VectList links = map.get(b);
            if (links.size() != 2) {
                return results;
            }
            links.get(1, c);
            if (c.equals(a)) {
                links.get(0, c);
            }
            Vect d = a;
            a = b;
            b = c;
            c = d;
        }
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
        ArrayList<VectList> linesAndPoints = new ArrayList<>();
        extractLines(linesAndPoints, true);
        appendable.append('[');
        boolean comma = false;
        for (int i = 0; i < linesAndPoints.size(); i++) {
            if (comma) {
                appendable.append(',');
            } else {
                comma = true;
            }
            linesAndPoints.get(i).toString(appendable);
        }
        appendable.append(']');
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
        ArrayList<VectList> linesAndPoints = new ArrayList<>();
        extractLines(linesAndPoints, true);
        out.writeInt(linesAndPoints.size());
        for (int i = 0; i < linesAndPoints.size(); i++) {
            linesAndPoints.get(i).writeData(out);
        }
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
        clear();
        int numLineStrings = in.readInt();
        for (int i = numLineStrings; i-- > 0;) {
            VectList links = VectList.read(in);
            addAllLinks(links);
        }
        return this;
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
        final Vect a;
        final Vect b;
        final Vect intersection;
        final VectList intersections;

        IntersectionFinder(Tolerance tolerance) {
            this.tolerance = tolerance;
            this.a = new Vect();
            this.b = new Vect();
            this.intersection = new Vect();
            this.intersections = new VectList();
        }

        void reset(Line i) {
            this.i = i;
            this.i.getA(a);
            this.i.getB(b);
            this.intersections.clear();
        }

        @Override
        public boolean process(SpatialNode<Line> node, int index) {
            Line j = node.getItemValue(index);
            if ((!i.equals(j)) && i.intersectionSeg(j, tolerance, intersection)) {
                if (!(a.equals(intersection) || b.equals(intersection))) {
                    intersections.add(intersection);
                }
            }
            return true;
        }

    }

    @Override
    public Network clone() {
        Network network = new Network();
        Vect vect = new Vect();
        for(VectMap<VectList>.Iter iter = map.iterator(); iter.next();){
            iter.getVect(vect);
            network.map.put(vect, iter.getValue().clone());
        }
        network.numLinks = numLinks;
        return network;
    }
    
    
}
