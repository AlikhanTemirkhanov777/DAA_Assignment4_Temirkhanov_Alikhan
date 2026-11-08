#!/usr/bin/env python3
"""Create assignment plots from the measured rows in results/results.csv."""
import csv
import statistics
from collections import defaultdict
from pathlib import Path

import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parents[1]
CSV_PATH = ROOT / "results" / "results.csv"
PLOT_DIR = ROOT / "results" / "plots"


def load_rows():
    with CSV_PATH.open(newline="", encoding="utf-8") as handle:
        rows = list(csv.DictReader(handle))
    if not rows:
        raise SystemExit(f"No experiment rows found in {CSV_PATH}")
    required = {"algorithm", "input", "n", "m", "run", "time_ms", "ops", "extra_memory_ints"}
    missing = required.difference(rows[0])
    if missing:
        raise SystemExit(f"Missing CSV columns: {sorted(missing)}")
    for row in rows:
        for key in ("n", "m", "run"):
            row[key] = int(row[key])
        for key in ("time_ms", "ops", "extra_memory_ints"):
            row[key] = float(row[key]) if key == "time_ms" else int(row[key])
    return rows


def median_by(rows, algorithm, input_type, metric):
    grouped = defaultdict(list)
    for row in rows:
        if row["algorithm"] == algorithm and row["input"] == input_type:
            grouped[row["n"]].append(row[metric])
    return sorted((n, statistics.median(values)) for n, values in grouped.items())


def save_line_plot(series, title, xlabel, ylabel, filename, log_y=False):
    fig, ax = plt.subplots(figsize=(8.4, 5.2))
    for label, points, marker in series:
        if points:
            xs, ys = zip(*points)
            ax.plot(xs, ys, marker=marker, linewidth=2, markersize=6, label=label)
    if log_y:
        ax.set_yscale("log")
    ax.set_title(title)
    ax.set_xlabel(xlabel)
    ax.set_ylabel(ylabel)
    ax.grid(True, which="both", alpha=0.3)
    ax.legend()
    fig.tight_layout()
    fig.savefig(PLOT_DIR / filename, dpi=180)
    plt.close(fig)


def main():
    PLOT_DIR.mkdir(parents=True, exist_ok=True)
    rows = load_rows()

    save_line_plot(
        [
            ("Table", median_by(rows, "Table", "WorstCase", "ops"), "o"),
            ("TwoRows", median_by(rows, "TwoRows", "WorstCase", "ops"), "s"),
        ],
        "Edit Distance: DP states evaluated (Worst Case)",
        "String length n (characters)", "DP states evaluated (count)",
        "dp_ops.png", log_y=True,
    )
    save_line_plot(
        [
            ("Full table", median_by(rows, "Table", "WorstCase", "extra_memory_ints"), "o"),
            ("Two rows", median_by(rows, "TwoRows", "WorstCase", "extra_memory_ints"), "s"),
        ],
        "Edit Distance: auxiliary integer storage (Worst Case)",
        "String length n (characters)", "Auxiliary integer cells (count)",
        "dp_memory.png", log_y=True,
    )
    save_line_plot(
        [("Recursive", median_by(rows, "Recursive", "WorstCase", "ops"), "o")],
        "Recursive Edit Distance: recursive calls (Worst Case)",
        "String length n (characters)", "Recursive calls (count)",
        "recursive_ops.png", log_y=True,
    )
    save_line_plot(
        [
            ("Naive", median_by(rows, "Naive", "WorstCase", "ops"), "o"),
            ("KMP (including LPS construction)", median_by(rows, "KMP", "WorstCase", "ops"), "s"),
        ],
        "String Search: character comparisons (Worst Case)",
        "Text length n (characters)", "Character comparisons (count)",
        "search_ops.png", log_y=True,
    )
    save_line_plot(
        [
            ("Naive", median_by(rows, "Naive", "WorstCase", "time_ms"), "o"),
            ("KMP", median_by(rows, "KMP", "WorstCase", "time_ms"), "s"),
        ],
        "String Search: median runtime (Worst Case)",
        "Text length n (characters)", "Median runtime (milliseconds)",
        "search_time.png",
    )
    print(f"Created 5 plots in {PLOT_DIR}")


if __name__ == "__main__":
    main()
