import argparse
import os
from multiprocessing import Pool, cpu_count
from collections import defaultdict

def load_stem_buckets(whitelist_path):
    buckets = defaultdict(set)
    with open(whitelist_path, 'r', encoding='utf-8', errors='replace') as f:
        for line in f:
            word = line.strip()
            if '�' in word:
                continue
            word_lc = word.lower()
            first_char = word_lc[0]
            buckets[first_char].add(word_lc)
    return dict(buckets)

def load_unique_words(full_list_path):
    words = set()
    with open(full_list_path, 'r', encoding='utf-8', errors='replace') as f:
        for line in f:
            word = line.strip()
            if '�' in word:
                continue
            words.add(word)
    return words

def load_known_suffixes(suffix_file_path):
    suffixes = set()
    with open(suffix_file_path, 'r', encoding='utf-8', errors='replace') as f:
        for line in f:
            suffix = line.strip()
            if suffix:
                suffixes.add(suffix)
    return suffixes

def match_word(word, buckets, known_suffixes):
    """Return all valid combinations: base word and word+suffix if found in stems."""
    word_lc = word.lower()
    first_char = word_lc[0]
    possible_stems = buckets.get(first_char, set())

    matches = []

    if word_lc in possible_stems:
        matches.append(word)

    for suffix in known_suffixes:
        compound_word = word_lc + suffix
        if compound_word in possible_stems:
            matches.append(compound_word)

    return matches

def filter_words_parallel(all_words, stem_buckets, known_suffixes, num_workers):
    args = [(word, stem_buckets, known_suffixes) for word in all_words]
    with Pool(processes=num_workers) as pool:
        results = pool.starmap(match_word, args)

    matched_words = set()
    for match_list in results:
        matched_words.update(match_list)
    return matched_words

def main():
    parser = argparse.ArgumentParser(description="Filter given words by a stem whitelist. The list of suffixes is used to generate more variants of the valid words.")
    parser.add_argument("whitelist", help="Path to the whitelist file (with valid words)")
    parser.add_argument("full_list", help="Path to the full list of words to filter")
    parser.add_argument("suffix_file", help="Path to the file containing known suffixes")
    parser.add_argument("output", help="Path to save the filtered output")
    args = parser.parse_args()

    if not os.path.exists(args.whitelist):
        print(f"Whitelist file not found: {args.whitelist}")
        return
    if not os.path.exists(args.full_list):
        print(f"Full word list not found: {args.full_list}")
        return
    if not os.path.exists(args.suffix_file):
        print(f"Suffix file not found: {args.suffix_file}")
        return

    stem_buckets = load_stem_buckets(args.whitelist)
    print(f"Loaded {sum(len(s) for s in stem_buckets.values())} valid stems across {len(stem_buckets)} buckets.")

    all_words = load_unique_words(args.full_list)
    print(f"Loaded {len(all_words)} candidate words.")

    known_suffixes = load_known_suffixes(args.suffix_file)
    print(f"Loaded {len(known_suffixes)} known suffixes.")

    workers = cpu_count()
    print(f"Filtering using {workers} threads...", end=' ')
    filtered = filter_words_parallel(all_words, stem_buckets, known_suffixes, workers)
    print(f"OK. Matched {len(filtered)} words.")

    with open(args.output, 'w', encoding='utf-8') as f:
        for word in sorted(filtered):
            f.write(word + '\n')

if __name__ == "__main__":
    main()
