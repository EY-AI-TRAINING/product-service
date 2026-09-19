#!/usr/bin/env python3
"""Summarize a JaCoCo jacoco.csv into package and class coverage tables."""

from __future__ import annotations

import csv
import sys
from collections import defaultdict
from pathlib import Path

METRICS = (
    "INSTRUCTION",
    "BRANCH",
    "LINE",
    "COMPLEXITY",
    "METHOD",
)


def pct(covered: int, missed: int) -> float:
    total = covered + missed
    if total == 0:
        return 100.0
    return 100.0 * covered / total


def fmt_pct(covered: int, missed: int) -> str:
    return f"{pct(covered, missed):.1f}%"


def add_row(bucket: dict, row: dict) -> None:
    for metric in METRICS:
        bucket[f"{metric}_COVERED"] += int(row[f"{metric}_COVERED"])
        bucket[f"{metric}_MISSED"] += int(row[f"{metric}_MISSED"])


def empty_bucket() -> dict[str, int]:
    return {f"{m}_{side}": 0 for m in METRICS for side in ("COVERED", "MISSED")}


def print_totals(label: str, b: dict[str, int]) -> None:
    print(f"{label}")
    print(
        f"  instruction={fmt_pct(b['INSTRUCTION_COVERED'], b['INSTRUCTION_MISSED'])} "
        f"({b['INSTRUCTION_COVERED']}/{b['INSTRUCTION_COVERED'] + b['INSTRUCTION_MISSED']})"
    )
    print(
        f"  branch={fmt_pct(b['BRANCH_COVERED'], b['BRANCH_MISSED'])} "
        f"({b['BRANCH_COVERED']}/{b['BRANCH_COVERED'] + b['BRANCH_MISSED']})"
    )
    print(
        f"  line={fmt_pct(b['LINE_COVERED'], b['LINE_MISSED'])} "
        f"({b['LINE_COVERED']}/{b['LINE_COVERED'] + b['LINE_MISSED']}) "
        f"missed_lines={b['LINE_MISSED']}"
    )
    print(
        f"  method={fmt_pct(b['METHOD_COVERED'], b['METHOD_MISSED'])} "
        f"complexity={fmt_pct(b['COMPLEXITY_COVERED'], b['COMPLEXITY_MISSED'])}"
    )


def main() -> int:
    if len(sys.argv) != 2:
        print("Usage: summarize-jacoco.py <path-to-jacoco.csv>", file=sys.stderr)
        return 2

    csv_path = Path(sys.argv[1])
    if not csv_path.is_file():
        print(f"Missing JaCoCo CSV: {csv_path}", file=sys.stderr)
        return 1

    packages: dict[str, dict[str, int]] = defaultdict(empty_bucket)
    classes: list[tuple[str, str, dict]] = []
    overall = empty_bucket()

    with csv_path.open(newline="") as handle:
        reader = csv.DictReader(handle)
        for row in reader:
            package = row["PACKAGE"]
            class_name = row["CLASS"]
            bucket = empty_bucket()
            add_row(bucket, row)
            add_row(packages[package], row)
            add_row(overall, row)
            classes.append((package, class_name, bucket))

    print("== OVERALL ==")
    print_totals("all production classes", overall)
    print()

    print("== PACKAGES ==")
    for package in sorted(packages):
        print_totals(package, packages[package])
    print()

    print("== CLASSES (lowest line coverage first) ==")
    ranked = sorted(
        classes,
        key=lambda item: (
            pct(item[2]["LINE_COVERED"], item[2]["LINE_MISSED"]),
            -item[2]["LINE_MISSED"],
            item[0],
            item[1],
        ),
    )
    for package, class_name, bucket in ranked:
        line_pct = pct(bucket["LINE_COVERED"], bucket["LINE_MISSED"])
        flag = " BELOW_80" if line_pct < 80.0 else ""
        print(
            f"{package}.{class_name} "
            f"line={fmt_pct(bucket['LINE_COVERED'], bucket['LINE_MISSED'])} "
            f"missed_lines={bucket['LINE_MISSED']} "
            f"branch={fmt_pct(bucket['BRANCH_COVERED'], bucket['BRANCH_MISSED'])} "
            f"missed_branches={bucket['BRANCH_MISSED']}"
            f"{flag}"
        )

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
