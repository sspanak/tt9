import argparse
import os
from multiprocessing import Pool, cpu_count, Manager
from collections import defaultdict
import hunspell

def load_unique_words(full_list_path):
    words = set()
    with open(full_list_path, 'r', encoding='utf-8', errors='replace') as f:
        for line in f:
            word = line.strip()
            if '�' in word:
                continue
            words.add(word.lower())
    return words

def init_hunspell_worker(aff_path, dic_path):
    global hobj, dictionary_words
    hobj = hunspell.HunSpell(dic_path, aff_path)
    with open(dic_path, "r") as f:
        dictionary_words = set({
            line.split('/')[0].strip()
            for line in f
            if not line.startswith('#')
        })

def fix_word_text_case(word):
    word_lower = word.lower()
    for variant in [word, word_lower, word.capitalize(), word.upper()]:
        if variant in dictionary_words:
            return variant

    for suggestion in hobj.suggest(word_lower):
        if suggestion.lower() == word_lower:
            return suggestion

    return word

def run_hunspell_batch(words, aff_path, dic_path, num_workers):
    with Pool(
        processes=num_workers,
        initializer=init_hunspell_worker,
        initargs=(aff_path, dic_path)
    ) as pool:
        return pool.map(fix_word_text_case, words, chunksize=1)

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

    print(f"Fixing text case using hunspell...", end=" ")
    corrected_words = run_hunspell_batch(all_words, args.aff, args.dic, cpu_count())
    print("OK")

    with open(args.output, 'w', encoding='utf-8') as f:
        for word in sorted(corrected_words):
            f.write(word + '\n')

if __name__ == "__main__":
    main()
