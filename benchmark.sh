#!/bin/bash
javac -cp "lib/*" -d bin $(find src -name "*.java")

java \
  --enable-native-access=ALL-UNNAMED \
  -cp "bin:lib/*" \
  bench.ThroughputBenchmark
