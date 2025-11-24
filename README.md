## A Research Framework for Comparing B+Tree, BF-Tree, and LSM (RocksDB) Index Structures

### 🧩 Overview

**IndexEngine** is a research-grade benchmarking framework for evaluating three fundamental index structures used in modern storage systems:

- **B+Tree** — Classic disk-backed balanced tree.  
- **BF-Tree** — Write-optimized B+Tree variant using *MiniPages* as an in-memory write buffer.  
- **RocksDB (LSM-Tree)** — State-of-the-art log-structured merge tree.

The framework measures:

- **Throughput (Mops/s)**  
- **Latency (µs/op)**  
- **Disk Accesses per Lookup** (cache-miss metric)  
- Effects of **cache size** and **Zipfian skew**

All results are exported as clean CSV files for downstream analysis.
