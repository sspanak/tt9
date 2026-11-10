"""Restore supported readings from the pinned Rime Ice character dictionary.

The factory dictionary identifies an entry by (word, Pinyin), never by word alone.
This repair appends missing readings without changing any existing byte, weight,
or row order. It adds no new characters and excludes supplementary-plane Han.
"""

import argparse
import hashlib
import math
import re
from collections import defaultdict
from pathlib import Path


SOURCE_SHA256 = "b208cd1e443b720e69a6cffe3a1dca35edbb191090e418baa40b8b3a985482ee"
SOURCE_MAX_WEIGHT = 76938354  # The existing factory-frequency normalization scale.
REPO = Path(__file__).resolve().parent.parent
DEFAULT_DICTIONARY = REPO / "app/languages/dictionaries/zh-simplified-pinyin.csv"
DEFAULT_MANIFEST = REPO / "app/languages/chinese/polyphonic-readings.tsv"


def factory_entries(data):
    entries = {}
    for number, line in enumerate(data.decode("utf-8").splitlines(), 1):
        fields = line.split("\t")
        if len(fields) not in (2, 3) or not fields[0] or not re.fullmatch(r"[A-Z]+", fields[1]):
            raise ValueError(f"Invalid factory entry on line {number}")
        weight = int(fields[2]) if len(fields) == 3 else 0
        if not 0 <= weight <= 9999:
            raise ValueError(f"Invalid factory weight on line {number}")
        pair = tuple(fields[:2])
        if pair in entries:
            raise ValueError(f"Duplicate factory word/reading pair on line {number}")
        entries[pair] = weight
    return entries


def source_readings(data, existing):
    characters = {word for word, _ in existing if len(word) == 1 and ord(word) <= 0xFFFF}
    readings, seen = [], set()
    in_body = False
    for number, line in enumerate(data.decode("utf-8").splitlines(), 1):
        if line == "...":
            in_body = True
            continue
        if not in_body or not line or line.startswith("#"):
            continue
        fields = line.split("\t")
        if fields[0] not in characters:
            continue
        if len(fields) != 3 or not re.fullmatch(r"[a-z]+", fields[1]) or not fields[2].isdigit():
            raise ValueError(f"Invalid source reading on line {number}")
        word, reading, weight = fields[0], fields[1].upper(), int(fields[2])
        pair = word, reading
        if pair in seen:
            raise ValueError(f"Duplicate source word/reading pair on line {number}")
        seen.add(pair)
        readings.append((word, reading, weight, number))
    if not in_body or not readings:
        raise ValueError("The source has no supported character readings")
    return readings


def factory_weight(source_weight):
    return round(9999 * math.log1p(source_weight) / math.log1p(SOURCE_MAX_WEIGHT))


def repair_dictionary(data, readings):
    existing = factory_entries(data)
    additions = [row for row in readings if tuple(row[:2]) not in existing]
    if not additions:
        return data, additions
    newline = b"\r\n" if b"\r\n" in data else b"\n"
    suffix = newline.join(f"{word}\t{reading}\t{factory_weight(weight)}".encode("utf-8")
                           for word, reading, weight, _ in additions)
    # Preserve the original file exactly, including its newline convention.
    separator = b"" if not data or data.endswith(b"\n") else newline
    return data + separator + suffix, additions


def reading_manifest(readings, existing):
    variants = defaultdict(set)
    for word, reading in existing:
        if len(word) == 1:
            variants[word].add(reading)
    for word, reading, _, _ in readings:
        variants[word].add(reading)
    lines = [
        "# Rime Ice 8105.dict.yaml; GPL-3.0-only; see RIME-ICE-NOTICE.txt.",
        f"# Source SHA-256: {SOURCE_SHA256}",
        "# Expected source readings for factory BMP characters with multiple readings.",
        "# Columns: character, Pinyin, original source weight, source line (TAB-separated).",
    ]
    lines.extend(f"{word}\t{reading}\t{weight}\t{number}"
                 for word, reading, weight, number in readings if len(variants[word]) > 1)
    return ("\n".join(lines) + "\n").encode("utf-8")


def verified_source(path):
    data = path.read_bytes()
    if hashlib.sha256(data).hexdigest() != SOURCE_SHA256:
        raise ValueError("Source SHA-256 differs from the reviewed 2025-02-21 snapshot; see the provenance notice")
    return data


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path, help="Pinned Rime Ice cn_dicts/8105.dict.yaml")
    parser.add_argument("--dictionary", type=Path, default=DEFAULT_DICTIONARY)
    parser.add_argument("--manifest", type=Path, default=DEFAULT_MANIFEST)
    parser.add_argument("--check", action="store_true", help="Verify completeness and manifest without writing files")
    arguments = parser.parse_args()
    try:
        original = arguments.dictionary.read_bytes()
        existing = factory_entries(original)
        readings = source_readings(verified_source(arguments.source), existing)
        repaired, additions = repair_dictionary(original, readings)
        manifest = reading_manifest(readings, existing)
        if arguments.check:
            if additions:
                raise ValueError(f"Missing {len(additions)} supported word/reading pairs")
            if not arguments.manifest.exists() or arguments.manifest.read_bytes() != manifest:
                raise ValueError("The reading provenance manifest needs regeneration")
        else:
            if additions:
                arguments.dictionary.write_bytes(repaired)
            arguments.manifest.write_bytes(manifest)
        print(f"{len(readings)} source readings checked; {len(additions)} pairs added; "
              f"{len(existing) + len(additions)} factory pairs; original entries preserved")
    except (OSError, ValueError) as error:
        parser.exit(1, f"{error}\n")


if __name__ == "__main__":
    main()
