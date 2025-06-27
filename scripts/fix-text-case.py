import argparse
import os
import time
from multiprocessing import Pool, cpu_count, Manager
from collections import defaultdict
import hunspell

def load_unique_words(full_list_path):
    words = dict()
    with open(full_list_path, 'r', encoding='utf-8', errors='replace') as f:
        for line in f:
            word = line.strip()
            if '�' in word:
                continue

            word_lower = word.lower()
            if word_lower not in words or words[word_lower] == word_lower:
                words[word_lower] = word

    return words.values()

def init_hunspell_worker(aff_path, dic_path):
    global hobj, hunspell_stems
    hobj = hunspell.HunSpell(dic_path, aff_path)
    with open(dic_path, "r") as f:
        hunspell_stems = set({
            line.split('/')[0].strip()
            for line in f
            if not line.startswith('#')
        })

def fix_word_text_case(word):
    word_lower = word.lower()

    # check for direct matches to avoid expensive calls to HunSpell.suggest()
    if word_lower != word and word_lower in hunspell_stems:
        return word_lower

    if word in hunspell_stems:
        return word

    # name -> Name
    hunspell_variants = hobj.suggest(word_lower)
    for variant in hunspell_variants:
        if word_lower != variant and word_lower == variant.lower():
            return variant

    # if it can be either lowercase or uppercase, then we want to keep the lowercase
    if word_lower in hunspell_variants:
        return word_lower

    # if it is an unknown word, keep it as-is
    return word

def print_progress(current, total, start_time, interval):
    if current % interval == 0 or current == total:
        avg_time = (time.time() - start_time) / current
        remaining_time = (total - current) * avg_time
        HH, rem = divmod(int(remaining_time), 3600)
        MM, SS = divmod(rem, 60)
        print(f"\rFixing text case using hunspell... {current}/{total}, Remaining: {HH:02}:{MM:02}:{SS:02}", end=" ")


def run_hunspell_batch(words, aff_path, dic_path, num_workers):
    total = len(words)
    start_time = time.time()

    with Pool(
        processes=num_workers,
        initializer=init_hunspell_worker,
        initargs=(aff_path, dic_path)
    ) as pool:
        for i, correct_word in enumerate (pool.imap_unordered(fix_word_text_case, words), 1):
            print_progress(i, total, start_time, 300)
            yield correct_word


def main():
    parser = argparse.ArgumentParser(description="Correct the text case of a word list using Hunspell.")
    parser.add_argument("word_list", help="Path to the full list of words.")
    parser.add_argument("output", help="Path to save the corrected words.")
    parser.add_argument("--aff", required=True, help="Path to Hunspell .aff file.")
    parser.add_argument("--dic", required=True, help="Path to Hunspell .dic file.")
    args = parser.parse_args()

    if not os.path.exists(args.word_list):
        print(f"Full word list not found: {args.word_list}")
        return
    if not os.path.exists(args.aff):
        print(f"Hunspell .aff file not found: {args.aff}")
        return
    if not os.path.exists(args.dic):
        print(f"Hunspell .dic file not found: {args.dic}")
        return

    all_words = load_unique_words(args.word_list)
    print(f"Loaded {len(all_words)} candidate words.")

    corrected_words = run_hunspell_batch(all_words, args.aff, args.dic, cpu_count())

    with open(args.output, 'w', encoding='utf-8') as f:
        for word in sorted(corrected_words):
            f.write(word + '\n')

    print(" ") # clear the '\r'

if __name__ == "__main__":
    main()
