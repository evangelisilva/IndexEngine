package wrappers;

import org.rocksdb.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

public class RocksDBWrapper {
    private static RocksDB db;
    private static LRUCache cache;
    private static Statistics stats; 
    private static final String DB_PATH = "rocksdb_data";

    public static void init(long cacheSizeBytes) {
        try {
            RocksDB.loadLibrary();

            if (db != null) {
                db.close();
                db = null;
            }

            // Delete existing DB directory
            Path dbDir = Paths.get(DB_PATH);
            if (Files.exists(dbDir)) {
                Files.walk(dbDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }

            // Create stats + cache
            stats = new Statistics();
            cache = new LRUCache(cacheSizeBytes);

            BlockBasedTableConfig tableConfig = new BlockBasedTableConfig()
                    .setBlockCache(cache)
                    .setCacheIndexAndFilterBlocks(true)
                    .setCacheIndexAndFilterBlocksWithHighPriority(true);

            Options options = new Options()
                    .setCreateIfMissing(true)
                    .setTableFormatConfig(tableConfig)
                    .setUseDirectReads(true)
                    .setUseDirectIoForFlushAndCompaction(true)
                    .setParanoidChecks(false)
                    .setUseFsync(false)
                    .setStatistics(stats);

            db = RocksDB.open(options, DB_PATH);

            // ✅ Print human-readable sizes
            double sizeKB = cacheSizeBytes / 1024.0;
            double sizeMB = sizeKB / 1024.0;
            double sizeGB = sizeMB / 1024.0;

            System.out.printf(
                "RocksDB initialized with cache = %,.0f bytes (%.2f KB, %.2f MB, %.3f GB)%n",
                (double) cacheSizeBytes, sizeKB, sizeMB, sizeGB
            );

        } catch (RocksDBException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        try {
            RocksDB.loadLibrary();

            if (db != null) {
                db.close();
                db = null;
            }

            // Delete existing DB directory
            Path dbDir = Paths.get(DB_PATH);
            if (Files.exists(dbDir)) {
                Files.walk(dbDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }

            // create a Statistics object to track hits/misses
            stats = new Statistics();

            Options options = new Options()
                    .setCreateIfMissing(true)
                    .setUseDirectReads(true)
                    .setUseDirectIoForFlushAndCompaction(true)
                    .setParanoidChecks(false)
                    .setUseFsync(false)
                    .setStatistics(stats); // attach stats tracker

            db = RocksDB.open(options, DB_PATH);

        } catch (RocksDBException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        if (db != null) db.close();
        if (cache != null) cache.close();
        if (stats != null) stats.close();
    }

    public static void sequentialWrite(int count) {
        try (WriteOptions writeOptions = new WriteOptions().setDisableWAL(true)) {
            for (int i = 1; i <= count; i++) {
                byte[] key = String.format("%016x", i).getBytes();
                db.put(writeOptions, key, key);
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    public static void pointLookup(int count, int[] samples) {
        for (int s : samples) {
            try {
                db.get(String.format("%016x", s).getBytes());
            } catch (RocksDBException ignored) {}
        }
    }

    /**
     * Compute real block cache hit ratio using RocksDB's Statistics
     */
    public static double getBlockCacheHitRatio() {
        if (stats == null) return 0.0;

        long hits = stats.getTickerCount(TickerType.BLOCK_CACHE_HIT);
        long misses = stats.getTickerCount(TickerType.BLOCK_CACHE_MISS);
        long total = hits + misses;

        return total == 0 ? 0.0 : (100.0 * hits / total);
    }
}
