import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

# Load CSV
df = pd.read_csv("results/bench__rocks_bp_bf__throughput_time_disklookup__2m_1m.csv")

# Sort
df = df.sort_values(by=["Zipf Skewness", "Cache size (MB)"])

# Unique values
cache_sizes = sorted(df["Cache size (MB)"].unique())
skews = sorted(df["Zipf Skewness"].unique())

# Colors
rocks_color = "#1f77b4"   # Blue
bptree_color = "#ff7f0e"  # Orange
bftree_color = "#2ca02c"  # Green

def format_cache_size(mb):
    """Convert MB to readable string (KB, MB, GB)."""
    bytes_size = mb * 1024 * 1024
    if bytes_size < 1024**2:
        return f"{bytes_size/1024:.0f} KB"
    elif bytes_size < 1024**3:
        return f"{mb:.0f} MB"
    else:
        return f"{mb/1024:.0f} GB"

# -----------------------------------
# 📊 3 SUBPLOTS (Throughput, Time per op, Disk per lookup)
# -----------------------------------
fig, axes = plt.subplots(3, 1, figsize=(16, 18))
bar_width = 0.08
x = np.arange(len(skews))

# Helper to draw one subplot
def plot_metric(ax, column_rocks, column_bp, column_bf, title, ylabel):

    for i, c in enumerate(cache_sizes):
        subset = df[df["Cache size (MB)"] == c]
        offset = (i - len(cache_sizes)/2) * (bar_width * 3)

        bars_r = ax.bar(
            x + offset - bar_width, 
            subset[column_rocks],
            width=bar_width, color=rocks_color, alpha=0.85
        )
        bars_b = ax.bar(
            x + offset, 
            subset[column_bp],
            width=bar_width, color=bptree_color, alpha=0.85
        )
        bars_f = ax.bar(
            x + offset + bar_width, 
            subset[column_bf],
            width=bar_width, color=bftree_color, alpha=0.85
        )

        # Cache-size labels
        for bar in list(bars_r) + list(bars_b) + list(bars_f):
            ax.text(
                bar.get_x() + bar.get_width()/2,
                bar.get_height(),
                format_cache_size(c),
                ha="center", va="bottom",
                fontsize=6, rotation=90
            )

    ax.set_yscale("log")
    ax.set_xlabel("Zipf Skewness")
    ax.set_ylabel(ylabel)
    ax.set_title(title)
    ax.set_xticks(x)
    ax.set_xticklabels([f"{s:.1f}" for s in skews])
    ax.grid(axis="y", linestyle="--", alpha=0.6)

# -------------------------
# Plot each metric
# -------------------------

# 1) Throughput
plot_metric(
    axes[0],
    "RocksDB Throughput (Mops/s)",
    "B+Tree Throughput (Mops/s)",
    "BfTree Throughput (Mops/s)",
    "Throughput vs Zipf Skewness (All Cache Sizes)",
    "Throughput (Mops/s)"
)

# 2) Time per operation
plot_metric(
    axes[1],
    "RocksDB Time per Op (µs/op)",
    "B+Tree Time per Op (µs/op)",
    "BfTree Time per Op (µs/op)",
    "Time per Operation vs Zipf Skewness (All Cache Sizes)",
    "Time per Op (µs)"
)

# 3) Disk accesses per lookup
plot_metric(
    axes[2],
    "RocksDB Disk/Lookup",
    "B+Tree Disk/Lookup",
    "BfTree Disk/Lookup",
    "Disk Accesses per Lookup vs Zipf Skewness (All Cache Sizes)",
    "Disk Accesses per Lookup"
)

# Add legend once at the top
axes[0].legend(["RocksDB", "B+Tree", "BF-Tree"], loc="upper left")

plt.tight_layout()
plt.savefig("plot/bench__rocks_bp_bf__throughput_time_disklookup__2m_1m.png", dpi=300)
plt.show()
