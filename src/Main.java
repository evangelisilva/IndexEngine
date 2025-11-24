

import bench.Benchmarks;
import btree.BPlusTree;
import btree.BFTree;
// import lsm.LSMTree;

public class Main {

    public static void main(String[] args) throws Exception {

        long ops = 100_000;
        long cache = 4096;

        System.out.println("=== BPlusTree Benchmarks ===");
        Benchmarks.seqInsertBPT(new BPlusTree("bpt_seq.db", cache), ops);
        Benchmarks.randInsertBPT(new BPlusTree("bpt_rand.db", cache), ops);
        Benchmarks.readBPT(new BPlusTree("bpt_read.db", cache), ops);
        Benchmarks.mixedBPT(new BPlusTree("bpt_mix.db", cache), ops);

        System.out.println("\n=== BFTree Benchmarks ===");
        Benchmarks.seqInsertBFT(new BFTree("bft_seq.db", cache), ops);
        Benchmarks.randInsertBFT(new BFTree("bft_rand.db", cache), ops);
        Benchmarks.readBFT(new BFTree("bft_read.db", cache), ops);
        Benchmarks.mixedBFT(new BFTree("bft_mix.db", cache), ops);

        System.out.println("\n=== LSMTree Benchmarks (optional) ===");
        // Only enable these if you implement LSMTree
        // Benchmarks.seqInsertLSM(new LSMTree("lsm_seq.db", cache), ops);
        // Benchmarks.randInsertLSM(new LSMTree("lsm_rand.db", cache), ops);
        // Benchmarks.readLSM(new LSMTree("lsm_read.db", cache), ops);
        // Benchmarks.mixedLSM(new LSMTree("lsm_mix.db", cache), ops);
    }
}

