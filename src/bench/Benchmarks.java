package bench;

import btree.BPlusTree;
import btree.BFTree;
// import lsm.LSMTree;

// import java.io.IOException;
import java.util.Random;

public class Benchmarks {

    private static final Random rnd = new Random(1234);

    // ============================================================
    // Helpers
    // ============================================================

    private static long[] randomKeys(long n, long bound) {
        long[] arr = new long[(int) n];
        for (int i = 0; i < n; i++) {
            arr[i] = rnd.nextInt((int) bound);
        }
        return arr;
    }

    private static void print(String label, long ms, long ops) {
        double opsPerSec = (ops * 1000.0) / ms;
        System.out.printf("%-25s %8d ms   (%8.1f ops/sec)\n", label, ms, opsPerSec);
    }

    // ============================================================
    // B+TREE TESTS
    // ============================================================

    public static void seqInsertBPT(BPlusTree tree, long ops) throws Exception {
        long start = System.currentTimeMillis();
        for (long i = 0; i < ops; i++) tree.put(i, i);
        long end = System.currentTimeMillis();
        print("BPlusTree: sequential", end - start, ops);
        tree.close();
    }

    public static void randInsertBPT(BPlusTree tree, long ops) throws Exception {
        long[] keys = randomKeys(ops, ops * 10);
        long start = System.currentTimeMillis();
        for (int i = 0; i < ops; i++) tree.put(keys[i], i);
        long end = System.currentTimeMillis();
        print("BPlusTree: random", end - start, ops);
        tree.close();
    }

    public static void readBPT(BPlusTree tree, long ops) throws Exception {
        long[] keys = randomKeys(ops, ops * 10);
        for (int i = 0; i < ops; i++) tree.put(keys[i], i);

        long start = System.currentTimeMillis();
        for (int i = 0; i < ops; i++) tree.get(keys[i]);
        long end = System.currentTimeMillis();

        print("BPlusTree: read", end - start, ops);
        tree.close();
    }

    // ============================================================
    // BFTREE TESTS
    // ============================================================

    public static void seqInsertBFT(BFTree tree, long ops) throws Exception {
        long start = System.currentTimeMillis();
        for (long i = 0; i < ops; i++) tree.put(i, i);
        long end = System.currentTimeMillis();
        print("BFTree: sequential", end - start, ops);
        tree.close();
    }

    public static void randInsertBFT(BFTree tree, long ops) throws Exception {
        long[] keys = randomKeys(ops, ops * 10);
        long start = System.currentTimeMillis();
        for (int i = 0; i < ops; i++) tree.put(keys[i], i);
        long end = System.currentTimeMillis();
        print("BFTree: random", end - start, ops);
        tree.close();
    }

    public static void readBFT(BFTree tree, long ops) throws Exception {
        long[] keys = randomKeys(ops, ops * 10);
        for (int i = 0; i < ops; i++) tree.put(keys[i], i);

        long start = System.currentTimeMillis();
        for (int i = 0; i < ops; i++) tree.get(keys[i]);
        long end = System.currentTimeMillis();

        print("BFTree: read", end - start, ops);
        tree.close();
    }

    // ============================================================
    // MIXED WORKLOADS (50/50)
    // ============================================================

    public static void mixedBPT(BPlusTree tree, long ops) throws Exception {
        long[] keys = randomKeys(ops, ops * 10);

        long start = System.currentTimeMillis();
        for (int i = 0; i < ops; i++) {
            if ((i & 1) == 0) tree.put(keys[i], i);
            else tree.get(keys[i]);
        }
        long end = System.currentTimeMillis();
        print("BPlusTree: mixed 50/50", end - start, ops);
        tree.close();
    }

    public static void mixedBFT(BFTree tree, long ops) throws Exception {
        long[] keys = randomKeys(ops, ops * 10);

        long start = System.currentTimeMillis();
        for (int i = 0; i < ops; i++) {
            if ((i & 1) == 0) tree.put(keys[i], i);
            else tree.get(keys[i]);
        }
        long end = System.currentTimeMillis();
        print("BFTree: mixed 50/50", end - start, ops);
        tree.close();
    }

    // ============================================================
    // LSM TREE TESTS (if you add it)
    // ============================================================

    // public static void seqInsertLSM(LSMTree tree, long ops) throws Exception {
    //     long start = System.currentTimeMillis();
    //     for (long i = 0; i < ops; i++) tree.put(i, i);
    //     long end = System.currentTimeMillis();
    //     print("LSMTree: sequential", end - start, ops);
    //     tree.close();
    // }

    // public static void randInsertLSM(LSMTree tree, long ops) throws Exception {
    //     long[] keys = randomKeys(ops, ops * 10);
    //     long start = System.currentTimeMillis();
    //     for (int i = 0; i < ops; i++) tree.put(keys[i], i);
    //     long end = System.currentTimeMillis();
    //     print("LSMTree: random", end - start, ops);
    //     tree.close();
    // }

    // public static void readLSM(LSMTree tree, long ops) throws Exception {
    //     long[] keys = randomKeys(ops, ops * 10);
    //     for (int i = 0; i < ops; i++) tree.put(keys[i], i);

    //     long start = System.currentTimeMillis();
    //     for (int i = 0; i < ops; i++) tree.get(keys[i]);
    //     long end = System.currentTimeMillis();

    //     print("LSMTree: read", end - start, ops);
    //     tree.close();
    // }

    // public static void mixedLSM(LSMTree tree, long ops) throws Exception {
    //     long[] keys = randomKeys(ops, ops * 10);

    //     long start = System.currentTimeMillis();
    //     for (int i = 0; i < ops; i++) {
    //         if ((i & 1) == 0) tree.put(keys[i], i);
    //         else tree.get(keys[i]);
    //     }
    //     long end = System.currentTimeMillis();
    //     print("LSMTree: mixed 50/50", end - start, ops);
    //     tree.close();
    // }
}
