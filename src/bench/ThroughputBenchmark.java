package bench;

import wrappers.RocksDBWrapper;
import wrappers.BPlusTreeWrapper;
import wrappers.BFTreeWrapper;

import org.apache.commons.math3.distribution.ZipfDistribution;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.FileWriter;

public class ThroughputBenchmark {

    private static final int WRITE_COUNT = 2_000_000;
    private static final int LOOKUP_COUNT = 1_000_000;
    private static final int WARMUP_COUNT = 50_000;
    private static final int RUNS = 1;

    private static final long[] CACHE_SIZES = {
            2L * 1024 * 1024,
            20L * 1024 * 1024,
            200L * 1024 * 1024,
            2L * 1024 * 1024 * 1024
    };

    private static final double[] ZIPF_EXPONENTS = {0.1, 0.5, 1.0, 2.0};
    private static final String OUTPUT_CSV = "combined_benchmark_new_2.csv";

    public static void main(String[] args) throws Exception {
        System.out.println("Benchmark LSMTree (RocksDB), B+Tree and BfTree");

        try (FileWriter writer = new FileWriter(OUTPUT_CSV)) {

            // ---- CSV HEADER ----
            writer.write(
                "Cache size (MB),Zipf Skewness,"
                + "RocksDB Throughput (Mops/s),B+Tree Throughput (Mops/s),BfTree Throughput (Mops/s),"
                + "RocksDB Time per Op (µs/op),B+Tree Time per Op (µs/op),BfTree Time per Op (µs/op)\n"
            );

            RandomGenerator rng = new JDKRandomGenerator();
            rng.setSeed(42L);

            for (long cacheSize : CACHE_SIZES) {
                for (double zipfExp : ZIPF_EXPONENTS) {

                    System.out.printf(">>> Cache: %.2f MB | Zipf: %.2f%n",
                            cacheSize / (1024.0 * 1024.0), zipfExp);

                    double totalRocksThroughput = 0.0;
                    double totalBPTreeThroughput = 0.0;
                    double totalBfTreeThroughput = 0.0;
                    double totalRocksTimePerOp = 0.0;
                    double totalBPTreeTimePerOp = 0.0;
                    double totalBfTreeTimePerOp = 0.0;

                    for (int run = 1; run <= RUNS; run++) {
                        System.out.printf("  Run #%d/%d%n", run, RUNS);

                        // Prepare Zipf samples
                        ZipfDistribution zipfLookup = new ZipfDistribution(rng, WRITE_COUNT, zipfExp);
                        int[] lookupSamples = new int[LOOKUP_COUNT];
                        for (int i = 0; i < LOOKUP_COUNT; i++)
                            lookupSamples[i] = zipfLookup.sample();

                        ZipfDistribution zipfWarmup = new ZipfDistribution(rng, WRITE_COUNT, zipfExp);
                        int[] warmupSamples = new int[WARMUP_COUNT];
                        for (int i = 0; i < WARMUP_COUNT; i++)
                            warmupSamples[i] = zipfWarmup.sample();

                        // ===== RocksDB =====
                        RocksDBWrapper.init(cacheSize);
                        RocksDBWrapper.sequentialWrite(WRITE_COUNT);
                        RocksDBWrapper.pointLookup(WARMUP_COUNT, warmupSamples);

                        long startLookupR = System.nanoTime();
                        RocksDBWrapper.pointLookup(LOOKUP_COUNT, lookupSamples);
                        long endLookupR = System.nanoTime();

                        double rocksSeconds = (endLookupR - startLookupR) / 1e9;
                        double rocksThroughput = (LOOKUP_COUNT / rocksSeconds) / 1e6; // Mops/s
                        double rocksTimePerOp = (rocksSeconds * 1e6) / LOOKUP_COUNT;  // µs/op

                        RocksDBWrapper.close();

                        totalRocksThroughput += rocksThroughput;
                        totalRocksTimePerOp += rocksTimePerOp;

                        // ===== B+Tree =====
                        BPlusTreeWrapper bptree = new BPlusTreeWrapper();
                        bptree.init("bptree.db", cacheSize);
                        bptree.write(WRITE_COUNT);
                        bptree.lookup(warmupSamples);

                        long startLookupB = System.nanoTime();
                        bptree.lookup(lookupSamples);
                        long endLookupB = System.nanoTime();

                        double bptreeSeconds = (endLookupB - startLookupB) / 1e9;
                        double bptreeThroughput = (LOOKUP_COUNT / bptreeSeconds) / 1e6; // Mops/s
                        double bptreeTimePerOp = (bptreeSeconds * 1e6) / LOOKUP_COUNT;  // µs/op

                        bptree.close();

                        totalBPTreeThroughput += bptreeThroughput;
                        totalBPTreeTimePerOp += bptreeTimePerOp;

                        // ===== B+Tree =====
                        BFTreeWrapper bftree = new BFTreeWrapper();
                        bftree.init("bftree.db", cacheSize);
                        bftree.write(WRITE_COUNT);
                        bftree.lookup(warmupSamples);

                        long startLookupF = System.nanoTime();
                        bftree.lookup(lookupSamples);
                        long endLookupF = System.nanoTime();

                        double bftreeSeconds = (endLookupF - startLookupF) / 1e9;
                        double bftreeThroughput = (LOOKUP_COUNT / bftreeSeconds) / 1e6; // Mops/s
                        double bftreeTimePerOp = (bftreeSeconds * 1e6) / LOOKUP_COUNT;  // µs/op

                        bftree.close();

                        totalBfTreeThroughput += bftreeThroughput;
                        totalBfTreeTimePerOp += bftreeTimePerOp;

                        System.out.printf(
                            "    RocksDB: %.3f Mops/s (%.3f µs/op),  B+Tree: %.3f Mops/s (%.3f µs/op),  Bf-Tree: %.3f Mops/s (%.3f µs/op)%n",
                            rocksThroughput, rocksTimePerOp,
                            bptreeThroughput, bptreeTimePerOp,
                            bftreeThroughput, bftreeTimePerOp
                        );
                    }

                    // ---- Averages ----
                    double avgRocksThroughput = totalRocksThroughput / RUNS;
                    double avgBPTreeThroughput = totalBPTreeThroughput / RUNS;
                    double avgBfTreeThroughput = totalBfTreeThroughput / RUNS;
                    double avgRocksTimePerOp = totalRocksTimePerOp / RUNS;
                    double avgBPTreeTimePerOp = totalBPTreeTimePerOp / RUNS;
                    double avgBfTreeTimePerOp = totalBfTreeTimePerOp / RUNS;

                    // ---- CSV WRITE ----
                    writer.write(String.format(
                            "%.3f,%.2f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f\n",
                            cacheSize / (1024.0 * 1024.0),
                            zipfExp,
                            avgRocksThroughput,
                            avgBPTreeThroughput,
                            avgBfTreeThroughput,
                            avgRocksTimePerOp,
                            avgBPTreeTimePerOp,
                            avgBfTreeTimePerOp
                    ));
                    writer.flush();

                    // ---- PRINT SUMMARY ----
                    System.out.printf(
                        ">>> Averages: RocksDB %.3f Mops/s (%.3f µs/op), "
                        + "B+Tree %.3f Mops/s (%.3f µs/op), Bf-Tree %.3f Mops/s (%.3f µs/op)%n%n",
                        avgRocksThroughput, avgRocksTimePerOp,
                        avgBPTreeThroughput, avgBPTreeTimePerOp,
                        avgBfTreeThroughput, avgBfTreeTimePerOp
                    );
                }
            }
        }

        System.out.println("\nBenchmark complete. Results written to " + OUTPUT_CSV);
    }
}
