package wrappers;

import btree.BFTree;

public class BFTreeWrapper {

    public BFTree tree;

    /** Initialize a B+Tree with given cache size (in bytes) */
    public void init(String path, long cacheSizeBytes) throws Exception {
        double sizeKB = cacheSizeBytes / 1024.0;
        double sizeMB = sizeKB / 1024.0;
        double sizeGB = sizeMB / 1024.0;

        System.out.printf(
            "Bf-Tree initialized with cache = %,.0f bytes (%.2f KB, %.2f MB, %.3f GB)%n",
            (double) cacheSizeBytes, sizeKB, sizeMB, sizeGB
        );

        tree = new BFTree(path, cacheSizeBytes);
    }

    /** Sequential writes (populate database) */
    public void write(int count) throws Exception {
        for (long i = 0; i < count; i++) {
            tree.put(i, i);
        }
    }

    /** Point lookups */
    public void lookup(int[] samples) throws Exception {
        for (int s : samples) {
            tree.get((long) s);
        }
    }

    public long getDiskAccesses() { return tree.tree.getDiskReads(); }
    public void resetStats() { tree.resetStats(); }

    /** Close the B+Tree */
    public void close() throws Exception {
        if (tree != null) tree.close();
    }

    /** Get cache hit ratio (%) */
    // public double getCacheHitRatio() {
    //     long hits = tree.getCacheHits();
    //     long misses = tree.getCacheMisses();
    //     long total = hits + misses;
    //     return total == 0 ? 0.0 : (100.0 * hits / total);
    // }
}
