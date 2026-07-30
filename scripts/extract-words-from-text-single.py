import sys
import os
import re
import time
from collections import Counter
from os.path import basename


def usage():
    print(f"Usage: e{basename(__file__)} [--freq|-f] <allowed_letters> <file>")
    sys.exit(1)


# Check and parse arguments
args = sys.argv[1:]
if not args or len(args) < 2:
    usage()

show_freq = False
if args[0] in ("--freq", "-f"):
    show_freq = True
    args = args[1:]

if len(args) != 2:
    usage()

allowed_letters = set(args[0])
file_path = args[1]

# Unicode word pattern
word_pattern = re.compile(r'\b\w+\b', re.UNICODE)
word_counts = Counter()

# Process file
try:
    file_size = os.path.getsize(file_path)
    last_reported = -1
    last_report_time = 0.0
    PROGRESS_INTERVAL = 1.0  # seconds
    with open(file_path, 'r', encoding='utf-8') as f:
        while True:
            line = f.readline()
            if not line:
                break

            for word in word_pattern.findall(line):
                if all(char in allowed_letters for char in word):
                    word_counts[word] += 1

            # Progress logging as a percentage of bytes read so far,
            # throttled to at most once per second.
            if file_size > 0:
                percent = f.tell() * 100 / file_size
            else:
                percent = 100
            now = time.monotonic()
            if percent != last_reported and now - last_report_time >= PROGRESS_INTERVAL:
                print(f"\rProgress: {percent}%", end="", file=sys.stderr, flush=True)
                last_reported = percent
                last_report_time = now
    # Always show the final 100% regardless of the time throttle.
    if last_reported != 100:
        print(f"\rProgress: 100%", end="", file=sys.stderr, flush=True)
    print(file=sys.stderr)  # newline after the final progress update
except Exception as e:
    print(f"\nError reading {file_path}: {e}", file=sys.stderr)
    sys.exit(1)

# Output
if show_freq:
    for word, count in sorted(word_counts.items()):
        print(f"{word}\t{count}")
else:
    for word in sorted(word_counts):
        print(word)
