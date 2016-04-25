package org.jg.geom;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.jg.util.RTree;
import org.jg.util.SpatialNode;
import org.jg.util.SpatialNode.NodeProcessor;
import org.jg.util.Tolerance;
import org.jg.util.VectList;
import org.jg.util.VectMap;
import org.jg.util.VectMap.VectMapProcessor;
import org.jg.util.VectSet;

/**
 *
 * @author tim.ofarrell
 */
public final class Network implements Serializable, Cloneable {

    final VectMap<VectList> map;
    int numLinks;
    SpatialNode<Line> cachedLinks;

    public Network() {
        map = new VectMap<>();
    }
    
    public static Network valueOf(Tolerance accuracy, Linearizer linearizer, Geom a, Geom b){
        Network network = new Network();
        a.addTo(network, linearizer, accuracy);
        b.addTo(network, linearizer, accuracy);
        network.explicitIntersections(accuracy);
        network.snap(accuracy);
        return network;
    }
    
    public static Network valueOf(Tolerance accuracy, Linearizer linearizer, Geom... geoms){
        Network network = new Network();
        for(Geom geom : geoms){
            geom.addTo(network, linearizer, accuracy);
        }
        network.explicitIntersections(accuracy);
        network.snap(accuracy);
        return network;
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

    public void getLink(double x, double y, int index, VectBuilder target) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        VectList links = map.get(x, y);
        if (links == null) {
            throw new IllegalArgumentException("Unknown vertex : " + Vect.valueOf(x, y));
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
        cachedLinks = null;
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

    public boolean addLink(Line line) throws NullPointerException {
        return addLinkInternal(line.ax, line.ay, line.bx, line.by);
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

    public boolean removeLink(Line line) throws NullPointerException {
        return removeLinkInternal(line.ax, line.ay, line.bx, line.by);
    }

    boolean removeLinkInternal(double ax, double ay, double bx, double by) {
        if(ax == -45 && ay == -35 && bx == -39.99999999999999 && by == -35){
            System.out.println("ZOOP");
        }
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

    public void removeAllLinks(VectList vects) {
        int index = vects.size() - 1;
        double bx = vects.getX(index);
        double by = vects.getY(index);
        while (--index >= 0) {
            double ax = vects.getX(index);
            double ay = vects.getY(index);
            removeLinkInternal(ax, ay, bx, by);
            bx = ax;
            by = ay;
        }
    }

    //does nothing if points are the same
    public boolean toggleLink(Vect a, Vect b) throws NullPointerException {
        return toggleLinkInternal(a.x, a.y, b.x, b.y);
    }

    //does nothing if points are the same
    public boolean toggleLink(VectBuilder a, VectBuilder b) throws NullPointerException {
        return toggleLinkInternal(a.getX(), a.getY(), b.getX(), b.getY());
    }

    public boolean toggleLink(Line line) throws NullPointerException {
        return toggleLinkInternal(line.ax, line.ay, line.bx, line.by);
    }

    //does nothing if points are the same
    public boolean toggleLink(double ax, double ay, double bx, double by) throws IllegalArgumentException {
        Vect.check(ax, ay);
        Vect.check(bx, by);
        return toggleLinkInternal(ax, ay, bx, by);
    }
    

    public Network toggleAllLinks(VectList links) {
        if (links.size() <= 1) {
            return this;
        }
        double ax = links.getX(0);
        double ay = links.getY(0);
        for (int i = 1; i < links.size(); i++) {
            double bx = links.getX(i);
            double by = links.getY(i);
            toggleLinkInternal(ax, ay, bx, by);
            ax = bx;
            ay = by;
        }
        return this;
    }


    //does nothing if points are the same
    boolean toggleLinkInternal(double ax, double ay, double bx, double by) {
        if (hasLink(ax, ay, bx, by)) {
            removeLinkInternal(ax, ay, bx, by);
            return true;
        } else {
            addLinkInternal(ax, ay, bx, by);
            return false;
        }
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

    public SpatialNode<Line> getLinks() {
        SpatialNode<Line> ret = cachedLinks;
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
            RTree tree = new RTree<Line>(bounds, links);
            ret = tree.getRoot();
            cachedLinks = ret;
        }
        return ret;
    }

    public boolean forEachLink(final LinkProcessor processor) throws NullPointerException {
        return map.forEach(new VectMapProcessor<VectList>() {
            @Override
            public boolean process(double ax, double ay, VectList links) {
                for(int i = links.size(); i-- > 0;){
                    double bx = links.getX(i);
                    double by = links.getY(i);
                    if(Vect.compare(ax, ay, bx, by) < 0){
                        if(!processor.process(ax, ay, bx, by)){
                            return false;
                        }
                    }
                }
                return true;
            }

        });
    }

    public boolean forEachVertex(final VertexProcessor processor) throws NullPointerException {
        return map.forEach(new VectMapProcessor<VectList>() {
            @Override
            public boolean process(double x, double y, VectList links) {
                return processor.process(x, y, links.size());
            }

        });
    }

    public boolean forInteractingLinks(Rect rect, Tolerance accuracy, NodeProcessor<Line> processor) throws NullPointerException {
        return getLinks().forInteracting(rect, accuracy, processor);
    }

    public boolean forOverlappingLinks(Rect rect, NodeProcessor<Line> processor) throws NullPointerException {
        return getLinks().forOverlapping(rect, processor);
    }

    public boolean pointTouchesLine(Vect vect, Tolerance tolerance) {
        return pointTouchesLineInternal(vect.x, vect.y, getLinks(), tolerance);
    }

    boolean pointTouchesLineInternal(double x, double y, SpatialNode<Line> node, Tolerance tolerance) {
        if(Relation.isDisjoint(node.relate(x, y, tolerance))){
            return false;
        }
        if (node.isBranch()) {
            return pointTouchesLineInternal(x, y, node.getA(), tolerance)
                    || pointTouchesLineInternal(x, y, node.getB(), tolerance);
        } else {
            double tol = tolerance.tolerance * tolerance.tolerance;
            for (int i = node.size(); i-- > 0;) {
                Line line = node.getItemValue(i);
                double distSq = Line.distSegVectSq(line.ax, line.ay, line.bx, line.by, x, y);
                if (distSq <= tol) {
                    return true;
                }
            }
            return false;
        }
    }

    //Make all points of self intersection explicit
    public Network explicitIntersections(Tolerance tolerance) {
        return explicitIntersectionsWith(this, tolerance);
    }

    public Network explicitIntersectionsWith(Network other, Tolerance tolerance) {
        return explicitIntersectionsWith(other.getLinks(), tolerance);
    }

    public Network explicitIntersectionsWith(final SpatialNode<Line> otherLinks, final Tolerance tolerance) {
        SpatialNode<Line> links = getLinks();
        final IntersectionFinder finder = new IntersectionFinder(tolerance);
        links.forEach(new NodeProcessor<Line>() {
            @Override
            public boolean process(Rect bounds, Line value) {
                finder.reset(value);
                otherLinks.forInteracting(bounds, tolerance, finder);
                VectList intersections = finder.intersections;
                if (intersections.size() > 0) {
                    double ax = value.ax;
                    double ay = value.ay;
                    double cx = value.bx;
                    double cy = value.by;
                    removeLinkInternal(ax, ay, cx, cy);

                    //intersections.sort(); // since a < b, sorting always puts in correct order - ORDER IS NOT ALWAYS CORRECT DUE TO ROUNDING ERRORS!!!
                    intersections.sortByDist(ax, ay);

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
    
    public Network removeColinearPoints(final Tolerance tolerance){
        final VectList toRemove = new VectList();
        final double tolSq = tolerance.tolerance * tolerance.tolerance;
        map.forEach(new VectMapProcessor<VectList>(){
            @Override
            public boolean process(double x, double y, VectList links) {
                if(links.size() == 2){
                    double ax = links.getX(0);
                    double ay = links.getY(0);
                    double bx = links.getX(1);
                    double by = links.getY(1);
                    if(Line.distLineVectSq(ax, ay, bx, by, x, y) <= tolSq){
                        toRemove.add(x, y);
                    }
                }
                return true;
            }
        });
        for(int n = toRemove.size(); n-- > 0;){
            double x = toRemove.getX(n);
            double y = toRemove.getY(n);
            VectList links = map.get(x, y);
            double ax = links.getX(0);
            double ay = links.getY(0);
            double bx = links.getX(1);
            double by = links.getY(1);
            removeVertexInternal(x, y);
            addLinkInternal(ax, ay, bx, by);
        }
        return this;
    }
    

    public Network snap(final Tolerance tolerance) {
        final ArrayList<Snap> snapList = new ArrayList<>();
        Snap[] snaps = new Snap[10];
        while(true){
            snapList.clear();
            map.forEach(new VectMapProcessor<VectList>(){

                final VectBuilder workingVect = new VectBuilder();
                final SpatialNode<Line> links = getLinks();

                @Override
                public boolean process(double x, double y, VectList links) {
                    calculateSnapsForPoint(x, y, cachedLinks, tolerance, workingVect, snapList);
                    return true;
                }
            });
            calculateSnapsForPoints(tolerance, snapList);
            if(snapList.isEmpty()){
                return this;
            }
            snaps = snapList.toArray(snaps);
            Arrays.sort(snaps, 0, snapList.size());
            for(int i = 0; i < snapList.size(); i++){
                snaps[i].snap(this);
            }
        }
    }
        
    void calculateSnapsForPoints(Tolerance tolerance, Collection<Snap> results){
        int size = map.size();
        int a = size - 1;
        if (a <= 0) {
            return;
        }
        VectList vects = getVects(new VectList());
        final double tolSq = tolerance.tolerance * tolerance.tolerance;
        while (a-- > 0) {
            double ax = vects.getX(a);
            double ay = vects.getY(a);
            int b = a;
            while (++b < size) {
                double bx = vects.getX(b);
                double by = vects.getY(b);
                if (!tolerance.match(ax, bx)) {
                    break;
                }
                double distSq = Vect.distSq(ax, ay, bx, by);
                if(distSq <= tolSq){
                    results.add(new Snap(ax, ay, bx, by, distSq));
                }
            }
        }
    }
    
    void calculateSnapsForPoint(double x, double y, SpatialNode<Line> node, Tolerance accuracy, VectBuilder workingVect, Collection<Snap> result){
        if(Relation.isDisjoint(node.relate(x, y, accuracy))){
            return;
        }
        if(node.isBranch()){
            calculateSnapsForPoint(x, y, node.getA(), accuracy, workingVect, result);
            calculateSnapsForPoint(x, y, node.getB(), accuracy, workingVect, result);
            return;
        }
        double tolSq = accuracy.tolerance * accuracy.tolerance;
        for(int i = node.size(); i-- > 0;){
            Line line = node.getItemValue(i);
            if((line.ax == x) && (line.ay == y)){
                continue; // if line.a == point, skip
            }else if((line.bx == x) && (line.by == y)){
                continue; // if line.b == point, skip
            }
            double distSq = Line.distSegVectSq(line.ax, line.ay, line.bx, line.by, x, y);
            if(distSq <= tolSq){
                result.add(new Snap(x, y, line.ax, line.ay, line.bx, line.by, distSq));
            }
        }
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

    public void extractHangLines(final Collection<VectList> results) {
        map.forEach(new VectMapProcessor<VectList>() {
            @Override
            public boolean process(double x, double y, VectList value) {
                if (value.size() == 1) {
                    VectList hangLine = new VectList();
                    followLine(x, y, value.getX(0), value.getY(0), hangLine);
                    if(hangLine.isOrdered()){
                        results.add(hangLine);
                        return true;
                    }
                    int max = hangLine.size()-1;
                    if(map.get(hangLine.getX(max), hangLine.getY(max)).size() != 1){
                        results.add(hangLine);
                    }
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
        if (!(obj instanceof Network)) {
            return false;
        }
        final Network network = (Network) obj;
        if (network.numVects() != numVects()) {
            return false;
        }
        return map.forEach(new VectMapProcessor<VectList>() {
            @Override
            public boolean process(double x, double y, VectList a) {
                VectList b = network.map.get(x, y);
                return a.equals(b);
            }

        });
    }

    @Override
    public int hashCode() {
        int hash = 7;
        VectList vects = getVects(new VectList());
        VectBuilder vect = new VectBuilder();
        for (int i = 0; i < vects.size(); i++) {
            vects.getVect(i, vect);
            hash = 59 * hash + vect.hashCode();
            VectList links = map.get(vect);
            hash = 59 * hash + links.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        toString(str);
        return str.toString();
    }

//    public Geom toGeom(Tolerance accuracy){
//        final List<Geom> geoms = new ArrayList<>();
//        Area area = Area.valueOf(this, accuracy);
//        int numVectsInRings;
//        if(ringSet == null){
//            numVectsInRings = 0;
//        }else{
//            numVectsInRings = ringSet.numVects();
//            geoms.add(ringSet);
//        }
//        if(numVectsInRings < map.size()){
//            final VectList points = new VectList();
//            map.forEach(new VectMapProcessor<VectList>(){
//                final VectList lineString = new VectList();
//                @Override
//                public boolean process(double x, double y, VectList value) {
//                    switch(value.size()){
//                        case 0:
//                            points.add(x, y);
//                            break;
//                        case 1:
//                            lineString.clear();
//                            followLine(x, y, value.getX(0), value.getY(0), lineString);
//                            int end = lineString.size()-1;
//                            double endX = lineString.getX(end);
//                            double endY = lineString.getY(end);
//                            if((map.get(endX, endY).size() != 1) || Vect.compare(x, y, endX, endY) < 0){
//                                if(!lineString.isOrdered()){
//                                    lineString.reverse();
//                                }
//                                if(lineString.size() == 2){
//                                    geoms.add(new Line(lineString.getX(0), lineString.getY(0), lineString.getX(1), lineString.getY(1)));
//                                }else{
//                                    geoms.add(new LineString(lineString.clone()));
//                                }
//                            }
//                    }
//                    return true;
//                }
//            
//            });
//            if(!points.isEmpty()){
//                if(points.size() == 1){
//                    geoms.add(points.getVect(0));
//                }else{
//                    geoms.add(new MultiPoint(points));
//                }
//            }
//        }
//        switch(geoms.size()){
//            case 0:
//                return null;
//            case 1:
//                return geoms.get(0);
//            default:
//                Geom[] ret = geoms.toArray(new Geom[geoms.size()]);
//                Arrays.sort(ret, Geom.COMPARATOR);
//                return new GeomSet(ret);
//        }
//    }
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

    /**
     * Write this network to the output given
     *
     * @param out
     * @throws IOException if out was null
     * @throws NullPointerException if out was null
     */
    public void write(DataOutput out) throws IOException, NullPointerException {
        ArrayList<VectList> linesAndPoints = new ArrayList<>();
        extractLines(linesAndPoints, true);
        out.writeInt(linesAndPoints.size());
        for (int i = 0; i < linesAndPoints.size(); i++) {
            linesAndPoints.get(i).write(out);
        }
    }

    /**
     * Set the vertices and links from the input given
     *
     * @param in
     * @return this
     * @throws IOException if there was an error
     * @throws NullPointerException if in was null
     */
    public static Network read(DataInput in) throws IOException, NullPointerException {
        Network ret = new Network();
        int numLineStrings = in.readInt();
        for (int i = numLineStrings; i-- > 0;) {
            VectList links = VectList.read(in);
            ret.addAllLinks(links);
        }
        return ret;
    }

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
        network.cachedLinks = cachedLinks;
        return network;
    }

    public boolean checkConsistency() {
        return map.forEach(new VectMapProcessor<VectList>() {
            @Override
            public boolean process(double x, double y, VectList links) {
                for (int i = 0; i < links.size(); i++) {
                    Vect link = links.getVect(i);
                    VectList backLinks = map.get(link);
                    if (backLinks.indexOf(x, y, 0) < 0) {
                        return false;
                    }
                }
                return true;
            }

        });
    }
    
    public boolean hasHangLines(){
        return !map.forEach(new VectMapProcessor<VectList>(){
            @Override
            public boolean process(double x, double y, VectList links) {
                return (links.size() != 1);
            }
        
        });
    }
    
    public void removeHangLines(){
        map.forEach(new VectMapProcessor<VectList>(){
            @Override
            public boolean process(double x, double y, VectList links) {
                if (links.size() == 0) {
                    map.remove(x, y);
                }
                while (links.size() == 1) {
                    double mx = links.getX(0);
                    double my = links.getY(0);
                    removeVertexInternal(x, y);
                    links = map.get(mx, my);
                    x = mx;
                    y = my;
                }

                return true;
            }
        });
    }
    
//    
//    void addLinesWithRelationInternal(final Geom geom, final Tolerance accuracy, final Relate relate, final Network result){
//        map.forEach(new VectMapProcessor<VectList>(){
//            
//            VectBuilder workingVect = new VectBuilder();
//            
//            @Override
//            public boolean process(double ax, double ay, VectList links) {
//                for(int i = links.size(); i-- > 0;){
//                    double bx = links.getX(i);
//                    double by = links.getY(i);
//                    if(Vect.compare(ax, ay, bx, by) < 0){
//                        double mx = (ax + bx) / 2;
//                        double my = (ay + by) / 2;
//                        workingVect.set(mx, my);
//                        if(geom.relate(workingVect, accuracy) == relate){
//                            result.addLinkInternal(ax, ay, bx, by);
//                        }
//                    }
//                }
//                return true;
//            }
//        });
//    }
//
//    public Network removeWithRelation(Geom geom, Tolerance flatness, Tolerance accuracy, Relate relate) {
//        Network other = new Network();
//        geom.addTo(other, accuracy, accuracy);
//        explicitIntersectionsWith(other, accuracy);
//        if (relate == Relate.TOUCH) {
//            return removeTouchingInternal(geom, accuracy, new VectBuilder());
//        } else {
//            return removeInsideOrOutsideInternal(geom, accuracy, relate, new VectBuilder());
//        }
//    }
//
//    Network removeInsideOrOutsideInternal(final Geom geom, final Tolerance accuracy, final Relate relate, final VectBuilder workingVect) {
//        map.forEach(new VectMapProcessor<VectList>() {
//            @Override
//            public boolean process(double x, double y, VectList links) {
//                workingVect.set(x, y);
//                Relate result = geom.relate(workingVect, accuracy);
//                if (result == relate) {
//                    removeVertexInternal(x, y);
//                } else if (result == Relate.TOUCH) {
//                    for (int i = links.size(); i-- > 0;) {
//                        double bx = links.getX(i);
//                        double by = links.getY(i);
//                        if (Vect.compare(x, y, bx, by) < 0) { // filter here avoids checking twice
//                            workingVect.set((x + bx) / 2, (y + by) / 2); // set to mid point on link
//                            if (geom.relate(workingVect, accuracy) == relate) {
//                                removeLinkInternal(x, y, bx, by);
//                            }
//                        }
//                    }
//                }
//                return true;
//            }
//        });
//        return this;
//    }
//
//    Network removeTouchingInternal(final Geom geom, final Tolerance accuracy, final VectBuilder workingVect) {
//        map.forEach(new VectMapProcessor<VectList>() {
//            @Override
//            public boolean process(double x, double y, VectList links) {
//                workingVect.set(x, y);
//                
//                //remove any vertex where all links are touching? 
//                //NO THIS WONT WORK.
//                //TOO TIRED TO SORT NOW!
//                
//                if (geom.relate(workingVect, accuracy) == Relate.TOUCH) { // if the vect is touching, we may have more work to do
//                    //Remove any touching links
//                    for (int i = links.size(); i-- > 0;) {
//                        double bx = links.getX(i);
//                        double by = links.getY(i);
//                        workingVect.set((x + bx) / 2, (y + by) / 2); // set to mid point on link
//                        if (geom.relate(workingVect, accuracy) == Relate.TOUCH) {
//                            removeLinkInternal(x, y, bx, by);
//                        }
//                    }
//                    if (links.isEmpty()) { // if point is unlinked remove it
//                        removeVertexInternal(x, y);
//                    }
//                }
//                return true;
//            }
//        });
//        return this;
//    }

    void mergeInternal(Network other) {
        other.map.forEach(new VectMapProcessor<VectList>() {
            @Override
            public boolean process(double ax, double ay, VectList links) {
                for (int i = links.size(); i-- > 0;) {
                    double bx = links.getX(i);
                    double by = links.getY(i);
                    if (Vect.compare(ax, ay, bx, by) < 0) {
                        toggleLinkInternal(ax, ay, bx, by);
                    }
                }
                return true;
            }
        });
    }

    public String toWkt() {
        final VectList points = new VectList();
        forEachVertex(new VertexProcessor() {
            @Override
            public boolean process(double x, double y, int numLinks) {
                if (numLinks == 0) {
                    points.add(x, y);
                }
                return true;
            }

        });
        StringBuilder str = new StringBuilder();
        if (points.size() == 0) {
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
        } else if (points.size() == map.size()) {
            str.append("MULTIPOINT(");
            for (int i = 0; i < points.size(); i++) {
                if (i != 0) {
                    str.append(", ");
                }
                str.append(Vect.ordToStr(points.getX(i))).append(' ').append(Vect.ordToStr(points.getY(i)));
            }
        } else {
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

    public interface VertexProcessor {

        public boolean process(double x, double y, int numLinks);
    }
    
    public interface LinkProcessor{
     
        public boolean process(double ax, double ay, double bx, double by);
    }

    static class Snap implements Comparable<Snap> {

        final double ax;
        final double ay;
        final double bx;
        final double by;
        final double cx;
        final double cy;
        final double distSq;

        Snap(double ax, double ay, double bx, double by, double distSq) {
            this.ax = ax;
            this.ay = ay;
            this.bx = bx;
            this.by = by;
            this.cx = this.cy = Double.NaN;
            this.distSq = distSq;
        }

        Snap(double ax, double ay, double bx, double by, double cx, double cy, double distSq) {
            this.ax = ax;
            this.ay = ay;
            this.bx = bx;
            this.by = by;
            this.cx = cx;
            this.cy = cy;
            this.distSq = distSq;
        }
        
        boolean snap(Network network){
            if(!network.hasVect(ax, ay)){
                return false;
            }
            if(isSnapToPoint()){
                VectList links = network.map.get(bx, by);
                if(links == null){
                    return false;
                }
                for(int i = links.size(); i-- > 0;){
                    network.addLinkInternal(ax, ay, links.getX(i), links.getY(i));
                }
                network.removeVertexInternal(bx, by);
            }else{
                if(!network.removeLinkInternal(bx, by, cx, cy)){
                    return false;
                }
                network.addLinkInternal(ax, ay, bx, by);
                network.addLinkInternal(ax, ay, cx, cy);
            }
            return true;
        }
        
        boolean isSnapToPoint(){
            return Double.isNaN(cx);
        }

        @Override
        public int compareTo(Snap other) {
            int c = (isSnapToPoint() ? 0: 1) - (other.isSnapToPoint() ? 0 : 1);
            return (c == 0) ? Double.compare(distSq, other.distSq) : c;
        }

    }
}
