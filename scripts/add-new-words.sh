#!/bin/bash

if [ $# -lt 4 ]; then
	echo "Usage: $0 LOCALE base-dictionary-file.csv new-words-file.txt frequency-file.csv"
	echo 'Cleans up and adds new words to a dictionary file.'
	echo 'LOCALE could be any valid JS locale, for exmaple: en, en-US, etc...'
	exit 1
fi

if ! [[ -f $2 ]]; then
	echo "base-dictionary-file: '$2' does not exist"
	exit 2
fi

if ! [[ -f $3 ]]; then
	echo "new-words-file: '$3' does not exist"
	exit 2
fi

if ! [[ -f $4 ]]; then
	echo "frequency-file: '$4' does not exist"
	exit 2
fi

LOCALE=$1
DICTIONARY_FILE=$2
NEW_WORDS_FILE=$3
FREQUENCY_FILE=$4

sed -E 's/[\t0-9]+//g' $DICTIONARY_FILE > /tmp/_TT9_base.txt \
	&& node scripts/injest-words.js $NEW_WORDS_FILE > /tmp/_TT9_1.txt \
	&& node scripts/remove-foreign-words.js $LOCALE /tmp/_TT9_1.txt $LOCALE /tmp/_TT9_base.txt > /tmp/_TT9_2.txt \
	&& cp /tmp/_TT9_base.txt /tmp/_TT9_combined.txt \
	&& cat /tmp/_TT9_2.txt >> /tmp/_TT9_combined.txt \
	&& node scripts/remove-dictionary-repeating-words.js $LOCALE /tmp/_TT9_combined.txt > /tmp/_TT9_clean.txt \
	&& node scripts/inject-dictionary-frequencies.js /tmp/_TT9_clean.txt $FREQUENCY_FILE $LOCALE > /tmp/_TT9_output.txt \
	&& cat /tmp/_TT9_output.txt

rm -f /tmp/_TT9*
