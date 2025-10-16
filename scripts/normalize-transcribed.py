import sys
import os
import argparse
import yaml
from collections import defaultdict

def print_error(message):
    print(message, file=sys.stderr)

# ---------- Argument Parsing ----------
def parse_args():
    parser = argparse.ArgumentParser(
        description="Normalizes the frequencies in a dictionary with transcriptions."
    )
    parser.add_argument(
        "word_list",
        help="Path to the word list file (e.g., WORD-LIST.txt)"
    )
    parser.add_argument(
        "layout_yaml",
        help="Path to the YAML file containing the layout definitions (UTF-8 encoded)"
    )
    return parser.parse_args()

# ---------- File Validation ----------
def validate_file(file_path):
    if not os.path.isfile(file_path):
        print_error(f'Failure! Could not find file "{file_path}".')
        sys.exit(2)

# ---------- YAML Layout Loading ----------
def load_layout(yaml_path):
    with open(yaml_path, encoding='utf-8') as f:
        data = yaml.safe_load(f)

    if "layout" not in data or not isinstance(data["layout"], list):
        print_error("Error: YAML file must contain a 'layout' key with a list of lists.")
        sys.exit(4)

    layout_dict = {}
    seen = set()

    for index, group in enumerate(data["layout"]):
        if not isinstance(group, list) or len(group) == 0:
            print_error(f"Error: Layout entry {index} must be a non-empty list of strings.")
            sys.exit(4)
        for symbol in group:
            if symbol in seen:
                print_error(f"Error: Duplicate symbol '{symbol}' found in layout. Aborting.")
                sys.exit(4)
            seen.add(symbol)
            layout_dict[symbol] = index

    return layout_dict

# ---------- Word List Loading ----------
def load_entries(file_path):
    with open(file_path, encoding='utf-8') as f:
        lines = [line.strip() for line in f if line.strip()]

    entries = []
    for line_num, line in enumerate(lines, start=1):
        parts = line.split('\t')
        if len(parts) < 2:
            print_error(f"Malformed line {line_num}: '{line}' (expected at least 2 tab-separated fields)")
            sys.exit(3)

        native, latin = parts[:2]
        number = None
        if len(parts) > 2:
            try:
                number = int(parts[2])
            except ValueError:
                print_error(f"Malformed line {line_num}: '{line}' (third field must be an integer if present)")
                sys.exit(3)

        entries.append({'native': native, 'latin': latin, 'number': number})

    return entries

# ---------- Grouping by Layout Index Pattern ----------
def group_entries(entries, layout_dict):
    groups = defaultdict(list)

    # Sort symbols by length (descending) for multi-letter matching (e.g., 'Zh' before 'Z')
    sorted_symbols = sorted(layout_dict.keys(), key=len, reverse=True)

    for entry in entries:
        latin = entry['latin']
        i = 0
        index_seq = []

        while i < len(latin):
            matched = False
            for symbol in sorted_symbols:
                if latin.startswith(symbol, i):
                    index_seq.append(str(layout_dict[symbol]))
                    i += len(symbol)
                    matched = True
                    break
            if not matched:
                print_error(f"Error: Unknown symbol in Latin string '{latin}' near '{latin[i:]}'")
                sys.exit(5)

        key = ''.join(index_seq)
        groups[key].append(entry)

    return groups

# ---------- Frequency Normalization ----------
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

# ---------- Output ----------
def print_entries(entries):
    for e in entries:
        parts = [e['native'], e['latin']]
        if e['number'] is not None:
            parts.append(str(e['number']))
        print('\t'.join(parts))

# ---------- Main ----------
def main():
    args = parse_args()
    validate_file(args.word_list)
    validate_file(args.layout_yaml)

    layout_dict = load_layout(args.layout_yaml)
    entries = load_entries(args.word_list)
    groups = group_entries(entries, layout_dict)
    sorted_entries = normalize_frequencies(groups)
    print_entries(sorted_entries)

if __name__ == "__main__":
    main()
