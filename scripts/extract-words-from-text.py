#!/usr/bin/env python3

import sys
import re
import os
import time
from collections import Counter
from concurrent.futures import ProcessPoolExecutor
from itertools import islice
from os.path import basename

WORD_PATTERN = re.compile(r"\b\w+\b", re.UNICODE)
CHUNK_SIZE = 10000  # lines per task
MAX_PENDING = os.cpu_count() * 2


def usage():
    print(
        f"Usage: {basename(__file__)} [--freq|-f] <allowed_letters> <input_file> <output_file>",
        file=sys.stderr,
    )
    sys.exit(1)


def process_chunk(args):
    """Process a chunk of lines and return a Counter."""
    lines, allowed_letters = args

    counts = Counter()
    for line in lines:
        for word in WORD_PATTERN.findall(line):
            if all(c in allowed_letters for c in word):
                counts[word] += 1
    return counts


def chunked(fileobj, size):
    """Yield lists containing up to `size` lines."""
    while True:
        chunk = list(islice(fileobj, size))
        if not chunk:
            return
        yield chunk


def main():
    args = sys.argv[1:]

    if len(args) < 3:
        usage()

    show_freq = False
    if args[0] in ("--freq", "-f"):
        show_freq = True
        args = args[1:]

    if len(args) != 3:
        usage()

    allowed_letters = frozenset(args[0])
    input_file = args[1]
    output_file = args[2]

    total_counts = Counter()
    total_size = os.path.getsize(input_file)

    try:
        with open(input_file, "r", encoding="utf-8") as f, ProcessPoolExecutor() as executor:

            pending = []
            done_bytes = 0
            last_print = time.monotonic()


            for chunk in chunked(f, CHUNK_SIZE):
                chunk_bytes = sum(len(line.encode("utf-8")) for line in chunk)
                future = executor.submit(process_chunk, (chunk, allowed_letters))
                pending.append((future, chunk_bytes))

                if len(pending) >= MAX_PENDING:
                    future, chunk_bytes = pending.pop(0)
                    total_counts.update(future.result())
                    done_bytes += chunk_bytes

                    now = time.monotonic()
                    if now - last_print >= 1.0:
                        last_print = now
                        print(f"\rProcessing: {done_bytes * 100 / total_size:3.3f}%", end="", file=sys.stderr, flush=True)

            while pending:
                future, chunk_bytes = pending.pop(0)
                total_counts.update(future.result())
                done_bytes += chunk_bytes
                print(f"\rProcessing: {done_bytes * 100 / total_size:3.3f}%", end="", file=sys.stderr, flush=True)

        with open(output_file, "w", encoding="utf-8") as out:
            if show_freq:
                for word, count in sorted(total_counts.items()):
                    out.write(f"{word}\t{count}\n")
            else:
                for word in sorted(total_counts):
                    out.write(f"{word}\n")

    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    print(file=sys.stderr)
    main()
