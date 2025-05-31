import argparse
import os
from multiprocessing import Pool, cpu_count
from collections import defaultdict

def load_unique_words(word_list_path):
    words = set()
    with open(word_list_path, 'r', encoding='utf-8', errors='replace') as f:
        for line in f:
            word = line.strip()
            if '�' in word:
                continue
            words.add(word.lower())
    return words

def load_known_suffixes(suffix_file_path):
    suffixes = set()
    with open(suffix_file_path, 'r', encoding='utf-8', errors='replace') as f:
        for line in f:
            suffix = line.strip()
            if suffix:
                suffixes.add(suffix)
    return suffixes

def generate_from_args(args):
    word, suffixes = args
    return {word + suffix for suffix in suffixes}

def generate_words(words, suffixes, num_workers):
    new_words = set()
    with Pool(processes=num_workers) as pool:
        for result in pool.imap_unordered(generate_from_args, ((word, suffixes) for word in words)):
            new_words.update(result)
    return new_words

def main():
    parser = argparse.ArgumentParser(description="Naively generate new words using a list of stems and a list of suffixes. Note that, you will have to clean up the invalid words after that.")
    parser.add_argument("word_list", help="Path to the full list of words to filter")
    parser.add_argument("suffix_file", help="Path to the file containing known suffixes")
    parser.add_argument("output", help="Path to save the filtered output")
    args = parser.parse_args()

    if not os.path.exists(args.word_list):
        print(f"Full word list not found: {args.word_list}")
        return
    if not os.path.exists(args.suffix_file):
        print(f"Suffix file not found: {args.suffix_file}")
        return

    print("Generating new words...", end=' ')

    all_words = load_unique_words(args.word_list)
    known_suffixes = load_known_suffixes(args.suffix_file)

    print(f"\rGenerating new words out of {len(all_words)} stems and {len(known_suffixes)} suffixes...", end=' ')
    generated = generate_words(all_words, known_suffixes, cpu_count())
    print(f"OK ({len(generated) - len(all_words)} new words)")

    with open(args.output, 'w', encoding='utf-8') as f:
        for word in generated:
            f.write(word + '\n')

if __name__ == "__main__":
    main()
