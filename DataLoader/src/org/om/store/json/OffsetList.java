package org.om.store.json;

/**
 *
 * @author tofar
 */
class OffsetList {

    private long[] offsets;
    private int count;

    OffsetList() {
        this(32);
    }

    OffsetList(int initialSize) {
        offsets = new long[initialSize];
    }

    void add(long offset) {
        if (count == offsets.length) {
            long[] off = new long[offsets.length << 2];
            System.arraycopy(offsets, 0, off, 0, offsets.length);
            offsets = off;
        }
        offsets[count++] = offset;
    }

    public int numOffsets() {
        return count;
    }

    public long offset(int index) {
        return offsets[index];
    }

    public long lastOffset() {
        return offsets[count - 1];
    }
}
