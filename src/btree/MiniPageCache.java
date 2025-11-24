package btree;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MiniPageCache — buffered micro-pages from the BFTree paper.
 * Eviction is based on BYTES (not entry count).
 */
public class MiniPageCache {

    private final long maxBytes;     // total buffer pool size
    private long usedBytes = 0;      // current usage

    private final LinkedHashMap<Long, MiniPage> pages =
            new LinkedHashMap<>(16, 0.75f, true);

    private final BFTree parent;
    public final int miniPageMaxBytes;

    public MiniPageCache(long maxBytes, BFTree parent, int miniPageMaxBytes) {
        this.maxBytes = maxBytes;
        this.parent = parent;
        this.miniPageMaxBytes = miniPageMaxBytes;
    }

    // --------------------------------------------------------------
    // Get or create
    // --------------------------------------------------------------

    public MiniPage getOrCreate(long leafPageId) {
        MiniPage mp = pages.get(leafPageId);
        if (mp != null) return mp;

        // create new
        mp = new MiniPage(leafPageId, miniPageMaxBytes);
        pages.put(leafPageId, mp);

        usedBytes += mp.byteSize();
        evictIfNeeded();

        return mp;
    }

    public MiniPage get(long leafPageId) {
        return pages.get(leafPageId);
    }

    // --------------------------------------------------------------
    // Evict based on byte usage (LRU)
    // --------------------------------------------------------------

    private void evictIfNeeded() {
        while (usedBytes > maxBytes && !pages.isEmpty()) {

            Map.Entry<Long, MiniPage> oldest =
                    pages.entrySet().iterator().next();

            MiniPage mp = oldest.getValue();

            // flush changes
            parent.flushMiniPage(mp);

            usedBytes -= mp.byteSize();
            pages.remove(mp.leafPageId);
        }
    }

    // --------------------------------------------------------------
    // Recompute usage + evict if needed
    // --------------------------------------------------------------

    public void updateUsage() {

        long total = 0;

        for (MiniPage mp : pages.values()) {
            total += mp.byteSize();
        }

        usedBytes = total;
        evictIfNeeded();
    }

    // --------------------------------------------------------------
    // Flush everything on close()
    // --------------------------------------------------------------

    public void flushAll() {
        for (MiniPage mp : pages.values()) {
            parent.flushMiniPage(mp);
        }

        pages.clear();
        usedBytes = 0;
    }
}
