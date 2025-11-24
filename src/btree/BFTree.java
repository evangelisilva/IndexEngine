package btree;

import java.io.IOException;

public class BFTree {

    private final BPlusTree tree;
    private final MiniPageCache cache;

    // You can tune this
    private static final int DEFAULT_MINIPAGE_CAPACITY = 256;

    public BFTree(String file, long cacheBytes) throws IOException {
        this.tree = new BPlusTree(file, cacheBytes);

        this.cache = new MiniPageCache(
                cacheBytes,
                this,
                DEFAULT_MINIPAGE_CAPACITY
        );
    }

    // ============================================================
    // PUBLIC API
    // ============================================================

    public void put(long key, long value) throws IOException {

        long leafId = findLeafPageForKey(key);

        MiniPage mp = cache.getOrCreate(leafId);

        // IMPORTANT: flush BEFORE overflow
        if (mp.isFull()) {
            flushMiniPage(mp);
            mp.clear();
            cache.updateUsage();
        }

        mp.put(key, value);
        cache.updateUsage();
    }

    public Long get(long key) throws IOException {
        long leafId = findLeafPageForKey(key);

        // Check buffer first
        MiniPage mp = cache.get(leafId);
        if (mp != null && mp.contains(key)) {
            return mp.get(key);
        }

        // Fall back to full tree
        return tree.get(key);
    }

    public void close() throws IOException {
        cache.flushAll();
        tree.close();
    }

    // ============================================================
    // Leaf traversal (same logic as BPlusTree.search)
    // ============================================================

    long findLeafPageForKey(long key) throws IOException {
        Node node = tree.load(tree.rootPageId);

        while (!node.isLeaf) {
            int pos = binarySearch(node, key);
            if (pos < 0) pos = -pos - 1;
            node = tree.load(node.children[pos]);
        }

        return node.pageId;
    }

    private int binarySearch(Node node, long key) {
        int lo = 0, hi = node.keyCount - 1;

        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            long k = node.keys[mid];

            if (k == key) return mid;
            if (k < key) lo = mid + 1;
            else hi = mid - 1;
        }

        return -(lo + 1);
    }

    // ============================================================
    // FLUSH logic
    // ============================================================

    public void flushMiniPage(MiniPage mp) {
        if (mp.isEmpty()) return;

        try {
            for (int i = 0; i < mp.size(); i++) {
                long k = mp.getKey(i);
                long v = mp.getValue(i);
                tree.put(k, v);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
