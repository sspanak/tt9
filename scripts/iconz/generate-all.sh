#!/bin/bash

generate_async() {
  local count=$#
  local suffix=${!count}

  # Get all arguments except the last (used as Python args)
  local args=("${@:1:count-1}")

  # Run Python with args and output to XML named after suffix
  python generate-v3.py "${args[@]}" > "ic_lang_${suffix}.xml" &
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
generate_async NotoSans-Bold.ttf 12 -0.15 123

# ABC
generate_async NotoSans-Bold.ttf ab -0.15 latin_lo
generate_async NotoSans-Bold.ttf AB -0.15 latin_up
generate_async NotoSans-Bold.ttf аб -0.15 cyrillic_lo
generate_async NotoSans-Bold.ttf АБ -0.15 cyrillic_up

# Arabic / Farsi
generate_async NotoSansArabic-Bold.ttf ع -0.05 ar
generate_async NotoSansArabic-Bold.ttf ف -0.3 fa
generate_async NotoSansArabic-Bold.ttf أﺏ -0.15 alifba

# CJK
generate_async NotoSansJP-Bold.ttf あ -0.15 hiragana
generate_async NotoSansJP-Bold.ttf ア -0.15 katakana
generate_async NotoSansJP-Bold.ttf 漢 -0.17 kanji
generate_async NotoSansKR-Bold.ttf 한 -0.17 ko
generate_async NotoSansSC-Bold.ttf 拼 -0.17 zh_pinyin

# Greek
generate_all_cases "NotoSans-Bold.ttf" "ελ" "el" -0.12 -0.12 -0.15
generate_async NotoSans-Bold.ttf αβ alfabeta_lo
generate_async NotoSans-Bold.ttf ΑΒ -0.15 alfabeta_up

# Hebrew / Yiddish
generate_async NotoSansHebrew-Bold יי -0.2 ji
generate_async NotoSansHebrew-Bold אב -0.2 he
generate_async NotoSansHebrew-Bold עב -0.17 alefbet

# Hindi
generate_async NotoSansDevanagari-Bold.ttf ह -0.05 hi
generate_async NotoSansDevanagari-Bold.ttf कख -0.1 hi_abc

# Gujarati
generate_async NotoSansGujarati-Bold.ttf ગુ -0.1 gu
generate_async NotoSansGujarati-Bold.ttf કખ -0.1 gu_abc

# Tamazight / Tifinagh
generate_async NotoSansTifinagh.ttf ⵜⵎ -0.15 tm_tifinagh
generate_async NotoSansTifinagh.ttf ⴰⴱ -0.15 tifinagh

# Thai
generate_async NotoSansThai-Bold.ttf ไท -0.15 th
generate_async NotoSansThai-Bold.ttf กข -0.15 th_abc

generate_all_cases "NotoSans-Bold.ttf" "br" "br" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "бг" "bg" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "ca" "ca" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "hr" "hr" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "cz" "cz" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "da" "da" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "nl" "nl" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "en" "en" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "ee" "et" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "su" "su" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "fr" "fr" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "de" "de" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "hn" "hn" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "mg" "mg" -0.1 -0.1 -0.15
generate_all_cases "NotoSans-Bold.ttf" "id" "id" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "ga" "ga" -0.1 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "it" "it" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "sw" "sw" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "lv" "lv" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "lt" "lt" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "no" "no" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "pl" "pl" -0.08 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "pt" "pt" -0.08 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "ro" "ro" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "ру" "ru" -0.08 -0.08 -0.15
generate_all_cases "NotoSans-Bold.ttf" "sk" "sk" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "sl" "sl" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "ср" "sr" -0.08 -0.08 -0.15
generate_all_cases "NotoSans-Bold.ttf" "es" "es" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "sv" "sv" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "tm" "tm" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "tr" "tr" -0.15 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "уk" "uk" -0.05 -0.15 -0.15
generate_all_cases "NotoSans-Bold.ttf" "vi" "vi" -0.15 -0.15 -0.15

wait
