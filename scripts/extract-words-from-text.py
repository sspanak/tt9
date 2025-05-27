import sys
import re
from collections import Counter
from os.path import basename

def usage():
    print(f"Usage: e{basename(__file__)} [--freq|-f] <allowed_letters> <file1> [file2 ...]")
    sys.exit(1)

# Check and parse arguments
args = sys.argv[1:]
if not args or len(args) < 2:
    usage()

show_freq = False
if args[0] in ("--freq", "-f"):
    show_freq = True
    args = args[1:]

if len(args) < 2:
    usage()

allowed_letters = set(args[0])
file_paths = args[1:]

# Unicode word pattern
word_pattern = re.compile(r'\b\w+\b', re.UNICODE)
word_counts = Counter()

# Process files
for path in file_paths:
    try:
        with open(path, 'r', encoding='utf-8') as f:
            for line in f:
                for word in word_pattern.findall(line):
                    if all(char in allowed_letters for char in word):
                        word_counts[word] += 1
    except Exception as e:
        print(f"Error reading {path}: {e}", file=sys.stderr)

# Output
if show_freq:
    for word, count in sorted(word_counts.items()):
        print(f"{word}\t{count}")
else:
    for word in sorted(word_counts):
        print(word)
