import sys
import os
import argparse
from collections import defaultdict

def print_error(message):
    print(message, file=sys.stderr)

def parse_args():
    parser = argparse.ArgumentParser(
        description="Normalizes the frequencies in a dictionary with transcriptions."
    )
    parser.add_argument(
        "word_list",
        help="Path to the word list file (e.g., WORD-LIST.txt)"
    )
    return parser.parse_args()

def validate_file(file_path):
    if not os.path.isfile(file_path):
        print_error(f'Failure! Could not find word list file "{file_path}".')
        sys.exit(2)

def load_entries(file_path):
    with open(file_path, encoding='utf-8') as f:
        lines = [line.strip() for line in f if line.strip()]

    entries = []
    for line_num, line in enumerate(lines, start=1):
        parts = line.split('\t')
        if len(parts) < 2:
            print_error(f"Malformed line {line_num}: '{line}' (expected at least 2 tab-separated fields)")
            sys.exit(3)

        chinese, latin = parts[:2]
        number = None
        if len(parts) > 2:
            try:
                number = int(parts[2])
            except ValueError:
                print_error(f"Malformed line {line_num}: '{line}' (third field must be an integer if present)")
                sys.exit(3)

        entries.append({'chinese': chinese, 'latin': latin, 'number': number})

    return entries

def group_entries(entries):
    groups = defaultdict(list)
    for entry in entries:
        groups[entry['latin']].append(entry)
    return groups

def normalize_frequencies(groups):
    sorted_entries = []
    for group in groups.values():
        with_numbers = [e for e in group if e['number'] is not None]
        without_numbers = [e for e in group if e['number'] is None]

        with_numbers.sort(key=lambda e: e['number'], reverse=True)

        for rank, entry in enumerate(with_numbers, start=1):
            entry['number'] = str(len(with_numbers) - rank + 1)

        sorted_entries.extend(with_numbers)
        sorted_entries.extend(without_numbers)

    return sorted_entries

def print_entries(entries):
    for e in entries:
        parts = [e['chinese'], e['latin']]
        if e['number'] is not None:
            parts.append(e['number'])
        print('\t'.join(parts))

def main():
    args = parse_args()
    validate_file(args.word_list)
    entries = load_entries(args.word_list)
    groups = group_entries(entries)
    sorted_entries = normalize_frequencies(groups)
    print_entries(sorted_entries)

if __name__ == "__main__":
    main()
