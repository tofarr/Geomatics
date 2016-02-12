package org.jg.geom;

/**
 *
 * @author tofar_000
 */
public class RingSet {

    final Ring ring;
    final List<RingSet> children;

    public RingSet(Ring ring, List<RingSet> children) {
        this.ring = ring;
        this.children = children;
    }

    public RingSet(Ring ring) {
        this(ring, new ArrayList<RingSet>());
    }

    public void addInternal(RingSet child) {

    }
}