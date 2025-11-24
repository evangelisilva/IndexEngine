package btree;

/**
 * MiniPage = in-memory micro leaf page from the BFTree paper.
 * - Sorted key/value arrays (same physical layout as a leaf page)
 * - Capacity enforced by BYTE SIZE, not number of entries
 * - MiniPage size is typically 64B–4KB
 * - Once full, flushed into the actual leaf page on disk
 */
public class MiniPage {

    public final long leafPageId;

    // Physical layout exactly mimics B+Tree leaf pages:
    private long[] keys;
    private long[] values;

    // How many keys currently stored
    private int keyCount = 0;

    // Maximum allowed byte size (64–4096 bytes)
    private final int maxBytes;

    // Track actual bytes used
    private int usedBytes = 0;

    // --- Constants ---
    private static final int KEY_SIZE = 8;
    private static final int VALUE_SIZE = 8;
    private static final int ENTRY_SIZE = KEY_SIZE + VALUE_SIZE;
    private static final int BASE_OVERHEAD = 16; // metadata overhead, small constant

    public MiniPage(long leafPageId, int maxBytes) {
        this.leafPageId = leafPageId;
        this.maxBytes = maxBytes;

        // Initially allow at most (maxBytes / entrySize) entries
        int initialCapacity = Math.max(4, maxBytes / ENTRY_SIZE);
        this.keys = new long[initialCapacity];
        this.values = new long[initialCapacity];
    }

    // --------------------------------------------------------------
    // PUBLIC API
    // --------------------------------------------------------------

    public boolean isFull() {
        return (usedBytes + ENTRY_SIZE + BASE_OVERHEAD) > maxBytes;
    }

    public void put(long key, long value) {
        int pos = binarySearch(key);

        // Update existing
        if (pos >= 0) {
            values[pos] = value;
            return;
        }

        int insertPos = -pos - 1;

        // If full → caller will flush, so overflow is not allowed here
        if (isFull()) {
            throw new IllegalStateException("MiniPage overflow");
        }

        // Expand array if needed
        ensureCapacity(keyCount + 1);

        // Shift existing keys
        System.arraycopy(keys, insertPos, keys, insertPos + 1, keyCount - insertPos);
        System.arraycopy(values, insertPos, values, insertPos + 1, keyCount - insertPos);

        keys[insertPos] = key;
        values[insertPos] = value;

        keyCount++;
        usedBytes += ENTRY_SIZE;
    }

    public boolean contains(long key) {
        return binarySearch(key) >= 0;
    }

    public Long get(long key) {
        int pos = binarySearch(key);
        return (pos >= 0) ? values[pos] : null;
    }

    public int size() {
        return keyCount;
    }

    public long getKey(int i) {
        return keys[i];
    }

    public long getValue(int i) {
        return values[i];
    }

    public int byteSize() {
        return usedBytes + BASE_OVERHEAD;
    }

    public boolean isEmpty() {
        return keyCount == 0;
    }

    public void clear() {
        keyCount = 0;
        usedBytes = 0;
    }

    // --------------------------------------------------------------
    // INTERNALS
    // --------------------------------------------------------------

    private int binarySearch(long key) {
        int lo = 0, hi = keyCount - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            long k = keys[mid];

            if (k == key) return mid;
            if (k < key) lo = mid + 1;
            else hi = mid - 1;
        }

        return -(lo + 1);
    }

    private void ensureCapacity(int needed) {
        if (needed <= keys.length) return;

        int newCap = Math.max(keys.length * 2, needed);
        long[] newKeys = new long[newCap];
        long[] newVals = new long[newCap];

        System.arraycopy(keys, 0, newKeys, 0, keyCount);
        System.arraycopy(values, 0, newVals, 0, keyCount);

        keys = newKeys;
        values = newVals;
    }
}
