#!/bin/bash

if [ $# -lt 4 ]; then
	echo "Usage: $0 LOCALE base-dictionary-file.csv definition-file.txt frequency-file.csv"
	echo 'Removes the repeating words injects the frequencies and sorts a dictionary file. Useful, when adding new words directly to the dictionary .csv.'
	echo 'LOCALE could be any valid JS locale, for exmaple: en, en-US, etc...'
	exit 1
fi

if ! [[ -f $2 ]]; then
	echo "base-dictionary-file: '$2' does not exist"
	exit 2
fi

if ! [[ -f $3 ]]; then
	echo "definition-file: '$3' does not exist"
	exit 2
fi

if ! [[ -f $4 ]]; then
	echo "frequency-file: '$4' does not exist"
	exit 2
fi

LOCALE=$1
DICTIONARY_FILE=$2
DEFINITION_FILE=$3
FREQUENCY_FILE=$4
WORK_DIR="/tmp/TT9_$(uuidgen)"

mkdir -p $WORK_DIR \
	&& node scripts/remove-dictionary-repeating-words.js $LOCALE $DICTIONARY_FILE > $WORK_DIR/clean.txt \
	&& node scripts/inject-dictionary-frequencies.js $WORK_DIR/clean.txt $FREQUENCY_FILE $LOCALE > $WORK_DIR/freqz.txt \
	&& node scripts/sort-dictionary.js $LOCALE $WORK_DIR/freqz.txt $DEFINITION_FILE

rm -rf $WORK_DIR
