#!/bin/bash

# "Usage: $0 /path/to/csv/or/txt/files /path/to/downloads"
# 'Verifies each source dictionary is processed and stored in the given downloads directory.'

OUTPUT_EXTENSION="zip"

OUTPUT_DIR=${*: -1}
INPUT_DIR=${*: -2:1}

if ! [[ -d $INPUT_DIR ]]; then
	echo "input directory '$INPUT_DIR' does not exist" >&2
	exit 2
fi

if ! [[ -d $OUTPUT_DIR ]]; then
	echo "output directory '$OUTPUT_DIR' does not exist" >&2
	exit 2
fi

error_found=0

find "$INPUT_DIR" -type f \( -name "*.csv" -o -name "*.txt" \) -print0 | while IFS= read -r -d $'\0' input_file; do
	base_name=$(basename "$input_file" | sed 's/\.[^.]*$//')
	output_file="$OUTPUT_DIR/$base_name.$OUTPUT_EXTENSION"

	if [[ ! -f "$output_file" || "$input_file" -nt "$output_file" ]]; then
		echo "Dictionary download: '$output_file' is missing or older than '$input_file'" >&2
		error_found=1
	fi
done

if [[ "$error_found" -eq 1 ]]; then
	exit 1
else
	echo "OK"
	exit 0
fi