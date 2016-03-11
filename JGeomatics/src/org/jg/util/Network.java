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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jg.geom.GeomException;
import org.jg.geom.Line;
import org.jg.geom.Rect;
import org.jg.geom.RingSet;
import org.jg.geom.Vect;
import org.jg.geom.VectBuilder;
import org.jg.util.SpatialNode.NodeProcessor;
import org.jg.util.VectMap.VectMapProcessor;
import org.jg.util.VectSet.VectSetProcessor;

/**
 *
 * @author tim.ofarrell
 */
public final class Network implements Externalizable, Cloneable {

    final VectMap<VectList> map;
    int numLinks;
    RTree<Line> cachedLinks;

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
    
    //Returns -1 if vect does not exist
    public int numLinks(double x, double y) throws NullPointerException {
        VectList links = map.get(x, y);
        return (links == null) ? -1 : links.size();
    }

    public Vect minVect() {
        VectBuilder target = new VectBuilder();
        return minVect(target) ? target.build() : null;
    }

    public boolean minVect(final VectBuilder target) throws NullPointerException {
        if (map.isEmpty()) {
            return false;
        }
        target.set(Double.MAX_VALUE, Double.MAX_VALUE);
        map.forEach(new VectMapProcessor<VectList>() {
            @Override
            public boolean process(double x, double y, VectList value) {
                if (Vect.compare(x, y, target.getX(), target.getY()) < 0) {
                    target.set(x, y);
                }
                return true;
            }

        });
        return true;
    }

    public boolean getLinks(Vect vect, VectList target) throws NullPointerException {
        VectList links = map.get(vect);
        if (links == null) {
            return false;
        }
        target.addAll(links);
        return true;
    }

    public boolean getLinks(double x, double y, VectList target) throws NullPointerException, IllegalArgumentException {
        VectList links = map.get(x, y);
        if (links == null) {
            return false;
        }
        target.addAll(links);
        return true;
    }
    
    public void getLink(double x, double y, int index, VectBuilder target) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException{
        VectList links = map.get(x, y);
        if (links == null) {
            throw new IllegalArgumentException("Unknown vertex : "+Vect.valueOf(x, y));
        }
        links.getVect(index, target);
    }

    public boolean nextCW(double originX, double originY, double linkX, double linkY, VectBuilder target) throws NullPointerException, IllegalArgumentException {
        VectList links = map.get(originX, originY);
        if (links != null) {
            int index = links.indexOf(linkX, linkY, 0);
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

    public boolean nextCCW(double originX, double originY, double linkX, double linkY, VectBuilder target) throws NullPointerException, IllegalArgumentException {
        VectList links = map.get(originX, originY);
        if (links != null) {
            int index = links.indexOf(linkX, linkY, 0);
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

    public VectList vectList(VectList target) {
        return map.keyList(target);
    }

    public VectSet vectSet(VectSet target) throws NullPointerException {
        return map.keySet(target);
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

    boolean addVertexInternal(double x, double y) {
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
            insertLink(bx, by, ax, ay, links);
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
        int qi = quad(idx, idy);
        int qj = quad(jdx, jdy);
        if (qi == qj) {
            return Double.compare((idy / idx), (jdy / jdx));
        } else {
            return (qi < qj) ? -1 : 1;
        }
    }

    //upward horizontal lines are considered to be in the second quadrant - not the third!
    static int quad(double dx, double dy) {
        if (dx >= 0) { // right or vertical
            return (dy < 0) ? 0 : 1; // either bottom right or top right
        } else { // left
            return (dy > 0) ? 2 : 3;
        }
    }

    public boolean removeLink(double ax, double ay, double bx, double by) throws IllegalArgumentException {
        Vect.check(ax, ay);
        Vect.check(bx, by);
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
        for (int i = 0; i < vects.size(); i++) {
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

    public RTree<Line> getLinks(RTree<Line> target) {
        target.addAll(getLinks());
        return target;
    }

    RTree<Line> getLinks() {
        RTree<Line> ret = cachedLinks;
        if (ret == null) {
            final Rect[] bounds = new Rect[numLinks];
            final Line[] links = new Line[numLinks];
            map.forEach(new VectMapProcessor<VectList>() {

                int index = 0;

                @Override
                public boolean process(double ax, double ay, VectList value) {
                    for (int i = 0; i < value.size(); i++) {
                        double bx = value.getX(i);
                        double by = value.getY(i);
                        if (Vect.compare(ax, ay, bx, by) < 0) {
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
    
    public boolean forEachVertex(final VertexProcessor processor) throws NullPointerException{
        return map.forEach(new VectMapProcessor<VectList>(){
            @Override
            public boolean process(double x, double y, VectList value) {
                return processor.process(x, y, value.size());
            }
        
        });
    }

    public boolean forInteractingLinks(Rect rect, NodeProcessor<Line> processor) throws NullPointerException {
        return getLinks().forInteracting(rect, processor);
    }

    public boolean forOverlappingLinks(Rect rect, NodeProcessor<Line> processor) throws NullPointerException {
        return getLinks().forOverlapping(rect, processor);
    }

    //Make all points of self intersection explicit
    public Network explicitIntersections(Tolerance tolerance) {
        final RTree<Line> links = getLinks();
        final IntersectionFinder finder = new IntersectionFinder(tolerance);
        links.forEach(new NodeProcessor<Line>() {
            @Override
            public boolean process(Rect bounds, Line value) {
                finder.reset(value);
                links.forInteracting(bounds, finder);
                VectList intersections = finder.intersections;
                if (intersections.size() > 0) {
                    double ax = value.ax;
                    double ay = value.ay;
                    double cx = value.bx;
                    double cy = value.by;
                    removeLinkInternal(ax, ay, cx, cy);

                    //intersections.sort(); // since a < b, sorting always puts in correct order - ORDER IS NOT ALWAYS CORRECT DUE TO ROUNDING ERRORS!!!
                    for (int i = intersections.size(); i-- > 1;) {
                        double ix = intersections.getX(i);
                        double iy = intersections.getY(i);
                        double disq = Vect.distSq(ax, ay, ix, iy);
                        for (int j = i; j-- > 0;) {
                            double jx = intersections.getX(j);
                            double jy = intersections.getY(j);
                            double djsq = Vect.distSq(ax, ay, jx, jy);
                            if (disq < djsq) {
                                intersections.swap(i, j);
                                ix = jx;
                                iy = jy;
                                disq = djsq;
                            }
                        }
                    }

                    for (int i = intersections.size(); i-- > 0;) {
                        double bx = intersections.getX(i);
                        double by = intersections.getY(i);
                        addLinkInternal(bx, by, cx, cy);
                        cx = bx;
                        cy = by;
                    }
                    addLinkInternal(ax, ay, cx, cy);
                }
                return true;
            }

        });
        return this;
    }

    public Network snap(Tolerance tolerance) {
        int size = map.size();
        int a = size - 1;
        if (a <= 0) {
            return this;
        }
        VectList vects = getVects(new VectList());
        while (a-- > 0) {
            double ax = vects.getX(a);
            double ay = vects.getY(a);
            int b = a;
            while (++b < size) {
                double bx = vects.getX(b);
                double by = vects.getY(b);
                if (!tolerance.match(ax, by)) {
                    break;
                }
                if (tolerance.match(ax, ay, bx, by)) {
                    VectList links = map.get(bx, by);
                    removeVertexInternal(bx, by);
                    for (int k = 0; k < links.size(); k++) {
                        addLinkInternal(ax, ay, links.getX(k), links.getY(k));
                    }
                }
            }
        }
        return this;
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

    public Collection<VectList> extractLines(Collection<VectList> results, boolean includePoints) {
        VectList vects = getVects(new VectList());
        VectList result = new VectList();
        int numLinksProcessed = 0;
        for (int i = 0; i < vects.size(); i++) {
            double ax = vects.getX(i);
            double ay = vects.getY(i);
            VectList links = map.get(ax, ay);
            switch (links.size()) {
                case 0:
                    if (includePoints) {
                        results.add(new VectList().add(ax, ay));
                    }
                    break;
                case 2:
                    //this misses rings! Need to get these on second pass
                    break;
                default:
                    for (int j = 0; j < links.size(); j++) {
                        double bx = links.getX(j);
                        double by = links.getY(j);
                        result.clear();
                        followLine(ax, ay, bx, by, result);
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
        VectBuilder a = new VectBuilder();
        VectBuilder b = new VectBuilder();
        VectBuilder c = new VectBuilder();
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
                    continue; // point was less than a - continue
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

//    public boolean hasHangLines() {
//        return !map.forEach(new VectMapProcessor<VectList>() {
//            @Override
//            public boolean process(double ax, double ay, VectList value) {
//                return (value.size() != 1);
//            }
//        });
//    }
//
//    public Network removeHangLines() {
//        map.forEach(new VectMapProcessor<VectList>() {
//            @Override
//            public boolean process(double ax, double ay, VectList value) {
//                if(value.size() == 1){
//                    while(true){
//                        numLinks--;
//                        map.remove(ax, ay);
//                        double bx = value.getX(0);
//                        double by = value.getY(0);
//                        value = map.get(bx, by);
//                        switch(value.size()){
//                            case 1: // other end of a hang line
//                                map.remove(bx, by);
//                                return true;
//                            case 2: // mid point of a hang line
//                                ax = bx;
//                                ay = by;
//                            default: // nexus
//                                int index = value.indexOf(ax, ay, 0);
//                                value.remove(index);
//                                return true;
//                        }
//                    }
//                }
//                return true;
//            }
//        });
//        return this;
//    }

    public void extractHangLines(final Collection<VectList> results) {
        map.forEach(new VectMapProcessor<VectList>() {
            @Override
            public boolean process(double x, double y, VectList value) {
                if(value.size() == 1){
                    VectList hangLine = new VectList();
                    followLine(x, y, value.getX(0), value.getY(0), hangLine);
                    results.add(hangLine);
                }
                return true;
            }
        });
    }

    public VectList followLine(double ax, double ay, double bx, double by, VectList results) {
        results.add(ax, ay);
        while (true) {
            results.add(bx, by);
            VectList links = map.get(bx, by);
            if (links.size() != 2) {
                return results;
            }
            double cx = links.getX(1);
            double cy = links.getY(1);
            if ((cx == ax) && (cy == ay)) {
                cx = links.getX(0);
                cy = links.getY(0);
            }
            ax = bx;
            ay = by;
            bx = cx;
            by = cy;
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
    
    public String toWKT(){
        final VectList points = new VectList();
        forEachVertex(new VertexProcessor(){
            @Override
            public boolean process(double x, double y, int numLinks) {
                if(numLinks == 0){
                    points.add(x, y);
                }
                return true;
            }
        
        });
        StringBuilder str = new StringBuilder();
        if(points.size() == 0){
            str.append("MULTILINESTRING(");
            List<VectList> lineStrings = new ArrayList<>();
            extractLines(lineStrings, false);
            for (int i = 0; i < lineStrings.size(); i++) {
                VectList lineString = lineStrings.get(i);
                if (i != 0) {
                    str.append(',');
                }
                str.append('(');
                for (int j = 0; j < lineString.size(); j++) {
                    if (j != 0) {
                        str.append(", ");
                    }
                    str.append(Vect.ordToStr(lineString.getX(j))).append(' ').append(Vect.ordToStr(lineString.getY(j)));
                }
                str.append(')');
            }
        }else if(points.size() == map.size){
            str.append("MULTIPOINT(");
            for (int i = 0; i < points.size(); i++) {
                if (i != 0) {
                    str.append(", ");
                }
                str.append(Vect.ordToStr(points.getX(i))).append(' ').append(Vect.ordToStr(points.getY(i)));
            }
        }else{
            str.append("GEOMETRYCOLLECTION(");
            for (int i = 0; i < points.size(); i++) {
                if (i != 0) {
                    str.append(",");
                }
                str.append("POINT(").append(Vect.ordToStr(points.getX(i))).append(' ').append(Vect.ordToStr(points.getY(i))).append(')');
            }
            List<VectList> lineStrings = new ArrayList<>();
            extractLines(lineStrings, false);
            for (int i = 0; i < lineStrings.size(); i++) {
                VectList lineString = lineStrings.get(i);
                str.append(",LINESTRING(");
                for (int j = 0; j < lineString.size(); j++) {
                    if (j != 0) {
                        str.append(", ");
                    }
                    str.append(Vect.ordToStr(lineString.getX(j))).append(' ').append(Vect.ordToStr(lineString.getY(j)));
                }
                str.append(')');
            }
        }
        str.append(')');
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
    @Override
    public Network clone() {
        final Network network = new Network();
        map.forEach(new VectMapProcessor<VectList>() {
            @Override
            public boolean process(double x, double y, VectList value) {
                network.map.put(x, y, value.clone());
                return true;
            }
        });
        network.numLinks = numLinks;
        return network;
    }

    public boolean checkConsistency(){
        return map.forEach(new VectMapProcessor<VectList>(){
            @Override
            public boolean process(double x, double y, VectList links) {
                for(int i = 0; i < links.size(); i++){
                    Vect link = links.getVect(i);
                    VectList backLinks = map.get(link);
                    if(backLinks.indexOf(x, y, 0) < 0){
                        return false;
                    }
                }
                return true;
            }
        
        });
    }
    
    class IntersectionFinder implements NodeProcessor<Line> {

        final Tolerance tolerance;
        Line i;
        final VectList intersections;
        final VectBuilder workingVect;

        IntersectionFinder(Tolerance tolerance) {
            this.tolerance = tolerance;
            this.intersections = new VectList();
            this.workingVect = new VectBuilder();
        }

        void reset(Line i) {
            this.i = i;
            this.intersections.clear();
        }

        @Override
        public boolean process(Rect bounds, Line j) {
            if ((!i.equals(j)) && i.intersectionSeg(j, tolerance, workingVect)) {
                if ((Vect.compare(i.ax, i.ay, workingVect.getX(), workingVect.getY()) != 0)
                        && (Vect.compare(i.bx, i.by, workingVect.getX(), workingVect.getY()) != 0)) {
                    intersections.add(workingVect);
                }
            }
            return true;
        }
    }
    
    public interface VertexProcessor{
        public boolean process(double x, double y, int numLinks);
    }
}
