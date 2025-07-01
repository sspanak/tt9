#!/bin/bash

# Usage: ./compare_inner_txt.sh /path/to/dir https://base-url

LOCAL_DIR="$1"
BASE_URL="$2"

if [[ -z "$LOCAL_DIR" || -z "$BASE_URL" ]]; then
    echo "Usage: $0 /path/to/dir https://base-url"
    exit 1
fi

if [[ ! -d "$LOCAL_DIR" ]]; then
    echo "Error: Directory '$LOCAL_DIR' does not exist."
    exit 2
fi

cd "$LOCAL_DIR" || exit 3

for zip_file in *.zip; do
    [[ -e "$zip_file" ]] || continue  # skip if no .zip files present

    echo "Comparing local and remote '$zip_file'"

    # Extract local .txt file to temp
    LOCAL_TMP=$(mktemp)
    LOCAL_TXT=$(unzip -Z1 "$zip_file" | grep '\.txt$')
    
    if [[ -z "$LOCAL_TXT" ]]; then
        echo "Error: No .txt file found inside $zip_file"
        rm -f "$LOCAL_TMP"
        continue
    fi

    unzip -p "$zip_file" "$LOCAL_TXT" > "$LOCAL_TMP"

    # Download remote .zip and extract .txt
    REMOTE_TMP_ZIP=$(mktemp)
    REMOTE_TMP_TXT=$(mktemp)

    if ! curl -fsSL "${BASE_URL}/${zip_file}" -o "$REMOTE_TMP_ZIP"; then
        echo "Failed to download: ${BASE_URL}/${zip_file}"
        rm -f "$LOCAL_TMP" "$REMOTE_TMP_ZIP" "$REMOTE_TMP_TXT"
        continue
    fi

    REMOTE_TXT=$(unzip -Z1 "$REMOTE_TMP_ZIP" | grep '\.txt$')

    if [[ -z "$REMOTE_TXT" ]]; then
        echo "Error: No .txt file found inside remote $zip_file"
        rm -f "$LOCAL_TMP" "$REMOTE_TMP_ZIP" "$REMOTE_TMP_TXT"
        continue
    fi

    unzip -p "$REMOTE_TMP_ZIP" "$REMOTE_TXT" > "$REMOTE_TMP_TXT"

    # Compare text files
    if diff -q "$LOCAL_TMP" "$REMOTE_TMP_TXT" > /dev/null; then
        echo "✔ Text files are identical for $zip_file"
    else
        echo "❌ Text files differ in $zip_file"
    fi

    # Clean up
    rm -f "$LOCAL_TMP" "$REMOTE_TMP_ZIP" "$REMOTE_TMP_TXT"
    echo

done
