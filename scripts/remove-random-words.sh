#!/bin/bash

if [ $# -ne 7 ]; then
	echo "Usage: $0 <locale> <all-words.txt> <courpus-words-with-frequencies.txt> <bad-combinations.txt> <output-file.txt> <vowels-list> <unpopular-max-length>"
	echo "Example (Polish): $0 pl-PL pl.txt pl-corpus.txt pl-bad-combinations.txt pl-reduced.txt aąeęijoóuy 13"
	exit 1
fi

LOCALE="$1"
ORIGINAL_WORDS="$2"
CORPUS_WORDS="$3"
BAD_COMB_FILE="$4"
OUTPUT_FILE="$5"
VOWELS="$6"
UNPOPULAR_MAX_LENGTH="$7"

if ! [[ -f "$ORIGINAL_WORDS" ]]; then
	echo "All words file: '$ORIGINAL_WORDS' does not exist"
	exit 2
fi

if ! [[ -f "$CORPUS_WORDS" ]]; then
	echo "Corpus words file: '$CORPUS_WORDS' does not exist"
	exit 2
fi

if ! [[ -f "$BAD_COMB_FILE" ]]; then
	echo "Bad letter combinations file: '$BAD_COMB_FILE' does not exist"
	exit 2
fi

BAD_LETTER_COMBINATIONS=$(paste -sd'|' "$BAD_COMB_FILE")


sed -E 's/^[^\t]+\t[12]$//g' "$CORPUS_WORDS" | grep . | sed -E 's/[\t0-9]+$//g' > __tmp__popular.txt &
grep -Ev "(.)\1{2,}" "$ORIGINAL_WORDS" | grep -Ev "($BAD_LETTER_COMBINATIONS)" | grep -Ev "[$VOWELS]{3,}" | sort -u | uniq > __tmp__reduced.txt &
wait

node ~/src/tt9/scripts/remove-foreign-words.js --blacklist "$LOCALE" __tmp__reduced.txt "$LOCALE" __tmp__popular.txt | grep -Ev "(.)\1{2,}" > __tmp__unpopular.txt
node ~/src/tt9/scripts/remove-foreign-words.js --blacklist "$LOCALE" __tmp__reduced.txt "$LOCALE" __tmp__unpopular.txt > __tmp__popular_reduced.txt &
awk -v maxlen="$UNPOPULAR_MAX_LENGTH" 'length($0) <= maxlen' __tmp__unpopular.txt > __tmp__unpopular_reduced.txt &
wait

cat __tmp__popular_reduced.txt __tmp__unpopular_reduced.txt | sort -u | uniq > $OUTPUT_FILE

rm -f __tmp__*.txt
