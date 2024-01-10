#!/bin/bash

if [ $# -lt 4 ]; then
	echo "Usage: $0 LOCALE base-dictionary-file.csv new-words-file.txt frequency-file.csv [ignore-split-list.txt]"
	echo 'Cleans up and adds new words to a dictionary file. Optionally, it could skip splitting the words from "ignore-split-list.txt"'
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
IGNORE_SPLIT_LIST_FILE=$5
WORK_DIR="/tmp/TT9_$(uuidgen)"

mkdir -p $WORK_DIR && \
sed -E 's/[\t0-9]+//g' $DICTIONARY_FILE > $WORK_DIR/_TT9_base.txt \
	&& node scripts/injest-words.js $NEW_WORDS_FILE $IGNORE_SPLIT_LIST_FILE > $WORK_DIR/_TT9_1.txt \
	&& node scripts/remove-foreign-words.js $LOCALE $WORK_DIR/_TT9_1.txt $LOCALE $WORK_DIR/_TT9_base.txt > $WORK_DIR/_TT9_2.txt \
	&& cp $WORK_DIR/_TT9_base.txt $WORK_DIR/_TT9_combined.txt \
	&& echo >> $WORK_DIR/_TT9_combined.txt \
	&& cat $WORK_DIR/_TT9_2.txt >> $WORK_DIR/_TT9_combined.txt \
	&& node scripts/remove-dictionary-repeating-words.js $LOCALE $WORK_DIR/_TT9_combined.txt > $WORK_DIR/_TT9_clean.txt \
	&& node scripts/inject-dictionary-frequencies.js $WORK_DIR/_TT9_clean.txt $FREQUENCY_FILE $LOCALE > $WORK_DIR/_TT9_output.txt \
	&& cat $WORK_DIR/_TT9_output.txt

rm -rf $WORK_DIR
