#!/bin/bash

generate_async() {
  local count=$#
  local suffix=${!count}

  # Get all arguments except the last (used as Python args)
  local args=("${@:1:count-1}")

  # Run Python with args and output to XML named after suffix
  python generate-v2.py "${args[@]}" > "ic_lang_${suffix}.xml" &
}

generate_all_cases() {
  font_file=$1
  lang_code=$2
  file_code=$3
  alignment_lo=$4
  alignment_cp=$5
  alignment_up=$6

  generate_async $font_file ${lang_code} ${alignment_lo} ${file_code}_lo
  generate_async $font_file $(echo $lang_code | awk '{print toupper($0)}') ${alignment_up} ${file_code}_up
  generate_async $font_file $(echo $lang_code | awk '{print toupper(substr($0,1,1)) substr($0,2)}') ${alignment_cp} ${file_code}_cp
}

# 123
generate_async NotoSans-Bold.ttf 12 -0.12 123

# ABC
generate_async NotoSans-Bold.ttf ab -0.08 latin_lo
generate_async NotoSans-Bold.ttf AB -0.12 latin_up
generate_async NotoSans-Bold.ttf аб -0.08 cyrillic_lo
generate_async NotoSans-Bold.ttf АБ -0.12 cyrillic_up

# Arabic / Farsi
generate_async NotoSansArabic-Bold.ttf ع -0.17 ar
generate_async NotoSansArabic-Bold.ttf ف -0.3 fa
generate_async NotoSansArabic-Bold.ttf أﺏ -0.14 alifba

# CJK
generate_async NotoSansJP-Bold.ttf あ -0.13 hiragana
generate_async NotoSansJP-Bold.ttf ア -0.15 katakana
generate_async NotoSansJP-Bold.ttf 漢 -0.11 kanji
generate_async NotoSansKR-Bold.ttf 한 -0.15 ko
generate_async NotoSansSC-Bold.ttf 拼 -0.125 zh_pinyin

# Greek
generate_all_cases "NotoSans-Bold.ttf" "ελ" "el" -0.075 -0.075 -0.12
generate_async NotoSans-Bold.ttf αβ alfabeta_lo
generate_async NotoSans-Bold.ttf ΑΒ -0.12 alfabeta_up

# Hebrew / Yiddish
generate_async NotoSansHebrew-Bold יי -0.15 ji
generate_async NotoSansHebrew-Bold אב -0.18 he
generate_async NotoSansHebrew-Bold עב -0.13 alefbet

# Hindi
generate_async NotoSansDevanagari-Bold.ttf ह -0.03 hi
generate_async NotoSansDevanagari-Bold.ttf कख -0.08 hi_abc

# Gujarati
generate_async NotoSansGujarati-Bold.ttf ગુ gu
generate_async NotoSansGujarati-Bold.ttf કખ gu_abc

# Tamazight / Tifinagh
generate_async NotoSansTifinagh.ttf ⵜⵎ -0.12 tm_tifinagh
generate_async NotoSansTifinagh.ttf ⴰⴱ -0.12 tifinagh

# Thai
generate_async NotoSansThai-Bold.ttf ไท -0.05 th
generate_async NotoSansThai-Bold.ttf กข -0.15 th_abc

generate_all_cases "NotoSans-Bold.ttf" "br" "br" -0.12 -0.14 -0.14
generate_all_cases "NotoSans-Bold.ttf" "бг" "bg" -0.08 -0.12 -0.13
generate_all_cases "NotoSans-Bold.ttf" "ca" "ca" -0.16 -0.12 -0.12
generate_all_cases "NotoSans-Bold.ttf" "hr" "hr" -0.1 -0.12 -0.12
generate_all_cases "NotoSans-Bold.ttf" "cz" "cz" -0.15 -0.11 -0.11
generate_all_cases "NotoSans-Bold.ttf" "da" "da" -0.1 -0.12 -0.13
generate_all_cases "NotoSans-Bold.ttf" "nl" "nl" -0.1 -0.11 -0.12
generate_all_cases "NotoSans-Bold.ttf" "en" "en" -0.16 -0.12 -0.12
generate_all_cases "NotoSans-Bold.ttf" "et" "et" -0.14 -0.12 -0.12
generate_all_cases "NotoSans-Bold.ttf" "su" "su" -0.15 -0.11 -0.11
generate_all_cases "NotoSans-Bold.ttf" "fr" "fr" -0.11 -0.12 -0.12
generate_all_cases "NotoSans-Bold.ttf" "de" "de" -0.1 -0.12 -0.13
generate_all_cases "NotoSans-Bold.ttf" "hn" "hn" -0.1 -0.12 -0.12
generate_all_cases "NotoSans-Bold.ttf" "mg" "mg" -0.12 -0.06 -0.13
generate_all_cases "NotoSans-Bold.ttf" "id" "id" -0.1 -0.1 -0.12
generate_all_cases "NotoSans-Bold.ttf" "ga" "ga" -0.1 -0.12 -0.12
generate_all_cases "NotoSans-Bold.ttf" "it" "it" -0.1 -0.11 -0.12
generate_all_cases "NotoSans-Bold.ttf" "sw" "sw" -0.15 -0.12 -0.12
generate_all_cases "NotoSans-Bold.ttf" "lv" "lv" -0.11 -0.13 -0.12
generate_all_cases "NotoSans-Bold.ttf" "lt" "lt" -0.1 -0.12 -0.12
generate_all_cases "NotoSans-Bold.ttf" "no" "no" -0.15 -0.12 -0.11
generate_all_cases "NotoSans-Bold.ttf" "pl" "pl" 0 -0.11 -0.12
generate_all_cases "NotoSans-Bold.ttf" "pt" "pt" -0.07 -0.12 -0.12
generate_all_cases "NotoSans-Bold.ttf" "ro" "ro" -0.15 -0.11 -0.11
generate_all_cases "NotoSans-Bold.ttf" "ру" "ru" -0.1 -0.04 -0.1
generate_all_cases "NotoSans-Bold.ttf" "sl" "sl" -0.1 -0.1 -0.12
generate_all_cases "NotoSans-Bold.ttf" "es" "es" -0.16 -0.12 -0.12
generate_all_cases "NotoSans-Bold.ttf" "sv" "sv" -0.16 -0.12 -0.12
generate_all_cases "NotoSans-Bold.ttf" "tm" "tm" -0.12 -0.12 -0.12
generate_all_cases "NotoSans-Bold.ttf" "tr" "tr" -0.12 -0.12 -0.12
generate_all_cases "NotoSans-Bold.ttf" "уk" "uk" 0 -0.09 -0.11
generate_all_cases "NotoSans-Bold.ttf" "vi" "vi" -0.12 -0.12 -0.14

wait
