#!/bin/bash

if [ $# -lt 8 ]; then
	echo "Usage: $0 <raw-word-list.txt> <output.txt> <whitelist.txt> <suffixes.txt> <hunspell.aff> <hunspell.dic> <allowed-lowercase-char-list> <allowed-uppercase-char-list>"
	echo
	echo "Example (Slovak, no need of whitelist filter):"
	echo "  $0 sk-raw.txt sk-filtered.txt /dev/null /dev/null sk_SK.aff sk_SK.dic aáäbcčdďeéfghiíjklĺľmnňoóôpqrŕsštťuúvwxyýzž AÁÄBCČDĎEÉFGHIÍJKLĹĽMNŇOÓÔPQRŔSŠTŤUÚVWXYÝZŽ"
	echo
	echo "Example (Slovak, with whitelist filter):"
	echo "  $0 sk-raw.txt sk-filtered.txt cc-100+uni-leipzig.txt sk-suffix.txt sk_SK.aff sk_SK.dic aáäbcčdďeéfghiíjklĺľmnňoóôpqrŕsštťuúvwxyýzž AÁÄBCČDĎEÉFGHIÍJKLĹĽMNŇOÓÔPQRŔSŠTŤUÚVWXYÝZŽ"

	exit 1
fi

DICTIONARY_FILE=$1
if ! [[ -f $DICTIONARY_FILE ]]; then
	echo "base-dictionary-file: '$DICTIONARY_FILE' does not exist"
	exit 2
fi

SUFFIXES_FILE=$4


AFF_FILE=$5
if ! [[ -f "$AFF_FILE" ]]; then
    echo "whitelist file: '$AFF_FILE' does not exist"
    exit 2
fi

DIC_FILE=$6
if ! [[ -f "$DIC_FILE" ]]; then
    echo "whitelist file: '$DIC_FILE' does not exist"
    exit 2
fi

OUTPUT_FILE=$2
WHITELIST_FILE=$3
ALLOWED_LOWERCASE_CHARS=$7
ALLOWED_UPPERCASE_CHARS=$8
WORK_DIR="/tmp/TT9_$(uuidgen)"
SCRIPT_DIR="$(dirname "$0")"

if ! [[ -d $SCRIPT_DIR/venv ]]; then
	python -m venv $SCRIPT_DIR/venv && source $SCRIPT_DIR/venv/bin/activate && pip install -r $SCRIPT_DIR/requirements.txt
fi


whitelist_filter() {
	CLEAN_WORDS=$1
	OUTPUT=$2
	DICTIONARY=${AFF_FILE::-4}

	if ! [[ -f "$WHITELIST_FILE" ]]; then
	    echo "Whitelist file: '$WHITELIST_FILE' does not exist. Skipping filtering."
	    cp $CLEAN_WORDS $OUTPUT
	    return
	fi

	if ! [[ -f "$SUFFIXES_FILE" ]]; then
	    echo "Suffixes file: '$SUFFIXES_FILE' does not exist. Skipping filtering."
	    cp $CLEAN_WORDS $OUTPUT
	    return
	fi

	echo "Preparing to filter words using Hunspell and whitelist file $WHITELIST_FILE." \
		&& cp $WHITELIST_FILE $WORK_DIR/white.txt \
		&& printf "Enriching the whitelist with "good" Hunspell from our list... " && hunspell -i UTF-8 -G -d "$DICTIONARY" "$CLEAN_WORDS" >> $WORK_DIR/white.txt && echo "OK" \
		&& printf "Sorting the good words... " && cat $WORK_DIR/white.txt | sort | uniq > $WORK_DIR/white-clean.txt && echo "OK" \
		&& python $SCRIPT_DIR/whitelist-filter.py $WORK_DIR/white-clean.txt $CLEAN_WORDS $SUFFIXES_FILE $OUTPUT
}

# remove Roman numerals: ^(M{0,3})(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$

date
mkdir -p $WORK_DIR \
	&& printf "Removing foreign letters... " && grep --text -E "^[$ALLOWED_LOWERCASE_CHARS$ALLOWED_UPPERCASE_CHARS]+$" $DICTIONARY_FILE > $WORK_DIR/noforeign.txt && echo "OK" \
	&& printf "Removing frequencies and duplicates... " && sed -E 's/[\t0-9]+//g' $WORK_DIR/noforeign.txt | sort | uniq > $WORK_DIR/nofreq_norepeat.txt && echo "OK" \
	&& printf "Removing lowerUPPER... " && grep -vE "[$ALLOWED_LOWERCASE_CHARS][$ALLOWED_UPPERCASE_CHARS]" $WORK_DIR/nofreq_norepeat.txt > $WORK_DIR/no_low_up.txt && echo "OK" \
	&& printf "Removing UPPERlower... " && grep -vE "[$ALLOWED_UPPERCASE_CHARS]{2,}[$ALLOWED_LOWERCASE_CHARS]" $WORK_DIR/no_low_up.txt > $WORK_DIR/no_up_low.txt && echo "OK" \
	&& printf "Removing single chars... " && grep -vE "^.$" $WORK_DIR/no_up_low.txt > $WORK_DIR/no_single.txt && echo "OK" \
	&& printf "Removing words with repeeeeaaaated letters... " && grep -vE "(.)\1{2,}" $WORK_DIR/no_single.txt | grep -vE "^(.)\1$" | sort | uniq > $WORK_DIR/no_multi.txt && echo "OK" \
	&& whitelist_filter $WORK_DIR/no_multi.txt $WORK_DIR/whitelisted.txt \
	&& echo "Preparing to fix the text case." && source $SCRIPT_DIR/venv/bin/activate && python $SCRIPT_DIR/fix-text-case.py $WORK_DIR/whitelisted.txt $WORK_DIR/text_case.txt --aff "$AFF_FILE" --dic "$DIC_FILE" \
	&& INITIAL_COUNT=$(wc -l < "$DICTIONARY_FILE") && FINAL_COUNT=$(wc -l < "$WORK_DIR/text_case.txt") && echo "Word count: $INITIAL_COUNT -> $FINAL_COUNT" \
	&& mv $WORK_DIR/text_case.txt "$OUTPUT_FILE"

rm -rf $WORK_DIR
date