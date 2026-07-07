#!/bin/bash

if [ $# -lt 7 ]; then
	echo "Usage: $0 <raw-word-list.txt> <output.txt> <suffixes.txt> <hunspell.aff> <hunspell.dic> <allowed-lowercase-char-list> <allowed-uppercase-char-list> [--skip-fix-text-case]"
	echo
	echo "Optional arguments:"
	echo "  --skip-fix-text-case  Skip the lowerUPPER/UPPERlower filtering and text-case fixing using Hunspell"
	echo
	echo "Example (Slovak, no need of whitelist filter):"
	echo "  $0 sk-raw.txt sk-filtered.txt /dev/null sk_SK.aff sk_SK.dic aáäbcčdďeéfghiíjklĺľmnňoóôpqrŕsštťuúvwxyýzž AÁÄBCČDĎEÉFGHIÍJKLĹĽMNŇOÓÔPQRŔSŠTŤUÚVWXYÝZŽ"
	echo
	echo "Example (Slovak, with whitelist filter):"
	echo "  $0 sk-raw.txt sk-filtered.txt sk-suffix.txt sk_SK.aff sk_SK.dic aáäbcčdďeéfghiíjklĺľmnňoóôpqrŕsštťuúvwxyýzž AÁÄBCČDĎEÉFGHIÍJKLĹĽMNŇOÓÔPQRŔSŠTŤUÚVWXYÝZŽ"
	

	echo "Parameters are: $#"

	exit 1
fi

DICTIONARY_FILE=$1
if ! [[ -f $DICTIONARY_FILE ]]; then
	echo "base-dictionary-file: '$DICTIONARY_FILE' does not exist"
	exit 2
fi

SUFFIXES_FILE=$3

AFF_FILE=$4
if ! [[ -f "$AFF_FILE" ]]; then
	echo ".aff file: '$AFF_FILE' does not exist"
	exit 2
fi

DIC_FILE=$5
if ! [[ -f "$DIC_FILE" ]]; then
	echo ".dic file: '$DIC_FILE' does not exist"
	exit 2
fi

OUTPUT_FILE=$2
ALLOWED_LOWERCASE_CHARS=$6
ALLOWED_UPPERCASE_CHARS=$7

SKIP_FIX_TEXT_CASE=0
if [ "$8" == "--skip-fix-text-case" ]; then
	SKIP_FIX_TEXT_CASE=1
fi

WORK_DIR="/tmp/TT9_$(uuidgen)"
SCRIPT_DIR="$(dirname "$0")"

if ! [[ -d $SCRIPT_DIR/venv ]]; then
	python -m venv $SCRIPT_DIR/venv && source $SCRIPT_DIR/venv/bin/activate && pip install -r $SCRIPT_DIR/requirements.txt
fi


generate_words() {
	CLEAN_WORDS=$1
	OUTPUT=$2
	DICTIONARY=${AFF_FILE::-4}

	if ! [[ -f "$SUFFIXES_FILE" ]]; then
		echo "Suffixes file: '$SUFFIXES_FILE' does not exist. Skipping extra word generation."
		cp $CLEAN_WORDS $OUTPUT
		return
	fi

	printf "Extracting valid words for generating new ones... " && hunspell -i UTF-8 -G -d "$DICTIONARY" $CLEAN_WORDS | sort -u | uniq > $WORK_DIR/generation-stems.txt && echo "OK" \
	&& python $SCRIPT_DIR/generate-words-from-suffixes.py $WORK_DIR/generation-stems.txt $SUFFIXES_FILE $WORK_DIR/generated-raw.txt \
	&& printf "Validating generated words with Hunspell... " && hunspell -i UTF-8 -G -d "$DICTIONARY" $WORK_DIR/generated-raw.txt > $WORK_DIR/generated-valid.txt && echo "OK" \
	&& printf "Merging generated and input words... " && cat $CLEAN_WORDS $WORK_DIR/generated-valid.txt | sort -u | uniq > $OUTPUT && echo "OK"
}


fix_camel_case() {
	INPUT=$1
	OUTPUT=$2

	if [ "$SKIP_FIX_TEXT_CASE" -eq 1 ]; then
		echo "Skipping lowerUPPER correction."
		echo "Skipping UPPERlower correction."
		cp $INPUT $OUTPUT
		return
	fi

	printf "Removing lowerUPPER... " && grep -vE "[$ALLOWED_LOWERCASE_CHARS][$ALLOWED_UPPERCASE_CHARS]" $INPUT > $WORK_DIR/no_low_up.txt && echo "OK" \
	&& printf "Removing UPPERlower... " && grep -vE "[$ALLOWED_UPPERCASE_CHARS]{2,}[$ALLOWED_LOWERCASE_CHARS]" $WORK_DIR/no_low_up.txt > $OUTPUT && echo "OK"
}


fix_text_case() {
	INPUT=$1
	OUTPUT=$2

	if [ "$SKIP_FIX_TEXT_CASE" -eq 1 ]; then
		echo "Skipping text case correction with Hunspell."
		cp $INPUT $OUTPUT
		return
	fi

	echo "Preparing to fix the text case." && source $SCRIPT_DIR/venv/bin/activate && python $SCRIPT_DIR/fix-text-case.py $INPUT $OUTPUT --aff "$AFF_FILE" --dic "$DIC_FILE"
}


# remove Roman numerals: ^(M{0,3})(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$


date
mkdir -p $WORK_DIR \
	&& printf "Removing foreign letters... " && grep --text -E "^[$ALLOWED_LOWERCASE_CHARS$ALLOWED_UPPERCASE_CHARS]+$" $DICTIONARY_FILE > $WORK_DIR/noforeign.txt && echo "OK" \
	&& printf "Removing frequencies and duplicates... " && sed -E 's/[\t0-9]+//g' $WORK_DIR/noforeign.txt | sort | uniq > $WORK_DIR/nofreq_norepeat.txt && echo "OK" \
	&& fix_camel_case $WORK_DIR/nofreq_norepeat.txt $WORK_DIR/no_up_low.txt \
	&& printf "Removing single chars... " && grep -vE "^.$" $WORK_DIR/no_up_low.txt > $WORK_DIR/no_single.txt && echo "OK" \
	&& printf "Removing words with repeeeeaaaated letters... " && grep -vE "(.)\1{2,}" $WORK_DIR/no_single.txt | grep -vE "^(.)\1$" | sort | uniq > $WORK_DIR/no_multi.txt && echo "OK" \
	&& generate_words $WORK_DIR/no_multi.txt $WORK_DIR/generated.txt \
	&& fix_text_case $WORK_DIR/generated.txt $WORK_DIR/text_case.txt \
	&& INITIAL_COUNT=$(wc -l < "$DICTIONARY_FILE") && FINAL_COUNT=$(wc -l < "$WORK_DIR/text_case.txt") && echo "Word count: $INITIAL_COUNT -> $FINAL_COUNT" \
	&& mv $WORK_DIR/text_case.txt "$OUTPUT_FILE"

rm -rf $WORK_DIR
date