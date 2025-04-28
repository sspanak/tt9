#!/bin/bash

generate_icons() {
  font_file=$1
  lang_code=$2
  file_code=$3
  alignment=$4

  python generate-v2.py $font_file ${lang_code} ${alignment} > ic_lang_${file_code}_lo.xml
  python generate-v2.py $font_file $(echo $lang_code | awk '{print toupper($0)}') > ic_lang_${file_code}_up.xml
  python generate-v2.py $font_file $(echo $lang_code | awk '{print toupper(substr($0,1,1)) substr($0,2)}') > ic_lang_${file_code}_cp.xml
}

# 123
python generate-v2.py NotoSans-Bold.ttf 12 > ic_lang_123.xml

# ABC
python generate-v2.py NotoSans-Bold.ttf ab > ic_lang_latin_lo.xml
python generate-v2.py NotoSans-Bold.ttf AB > ic_lang_latin_up.xml
python generate-v2.py NotoSans-Bold.ttf аб > ic_lang_cyrillic_lo.xml
python generate-v2.py NotoSans-Bold.ttf АБ > ic_lang_cyrillic_up.xml

# Arabic / Farsi
python generate-v2.py NotoSansArabic-Bold.ttf ع -0.3 > ic_lang_ar.xml
python generate-v2.py NotoSansArabic-Bold.ttf ف -0.4 > ic_lang_fa.xml
python generate-v2.py NotoSansArabic-Bold.ttf أﺏ > ic_lang_alifba.xml

# CJK
python generate-v2.py NotoSansJP-Bold.ttf あ > ic_lang_hiragana.xml
python generate-v2.py NotoSansJP-Bold.ttf ア > ic_lang_katakana.xml
python generate-v2.py NotoSansJP-Bold.ttf 漢 > ic_lang_kanji.xml
python generate-v2.py NotoSansKR-Bold.ttf 한 > ic_lang_ko.xml
python generate-v2.py NotoSansSC-Bold.ttf 拼 > ic_lang_zh_pinyin.xml

# Greek
generate_icons "NotoSans-Bold.ttf" "ελ" "el"
python generate-v2.py NotoSans-Bold.ttf αβ -0.15 > ic_lang_alfabeta_lo.xml
python generate-v2.py NotoSans-Bold.ttf ΑΒ > ic_lang_alfabeta_up.xml

# Hebrew / Yiddish
python generate-v2.py NotoSansHebrew-Bold יי > ic_lang_ji.xml
python generate-v2.py NotoSansHebrew-Bold אב > ic_lang_he.xml
python generate-v2.py NotoSansHebrew-Bold עב > ic_lang_alefbet.xml

# Hindi
python generate-v2.py NotoSansDevanagari-Bold.ttf ह -0.15 > ic_lang_hi.xml
python generate-v2.py NotoSansDevanagari-Bold.ttf कख -0.175 > ic_lang_hi_abc.xml

# Gujarati
python generate-v2.py NotoSansGujarati-Bold.ttf ગુ > ic_lang_gu.xml
python generate-v2.py NotoSansGujarati-Bold.ttf કખ > ic_lang_gu_abc.xml

# Tamazight / Tifinagh
python generate-v2.py NotoSansTifinagh.ttf ⵜⵎ > ic_lang_tm_tifinagh.xml
python generate-v2.py NotoSansTifinagh.ttf ⴰⴱ > ic_lang_tifinagh.xml

# Thai
python generate-v2.py NotoSansThai-Bold.ttf ไท -0.175 > ic_lang_th.xml
python generate-v2.py NotoSansThai-Bold.ttf กข -0.275 > ic_lang_th_abc.xml

generate_icons "NotoSans-Bold.ttf" "br" "br" -0.2
generate_icons "NotoSans-Bold.ttf" "бг" "bg" -0.2
generate_icons "NotoSans-Bold.ttf" "ca" "ca" -0.3
generate_icons "NotoSans-Bold.ttf" "hr" "hr" -0.2
generate_icons "NotoSans-Bold.ttf" "cz" "cz" -0.3
generate_icons "NotoSans-Bold.ttf" "da" "da" -0.2
generate_icons "NotoSans-Bold.ttf" "nl" "nl" -0.2
generate_icons "NotoSans-Bold.ttf" "en" "en" -0.3
generate_icons "NotoSans-Bold.ttf" "et" "et" -0.25
generate_icons "NotoSans-Bold.ttf" "su" "su" -0.3
generate_icons "NotoSans-Bold.ttf" "fr" "fr" -0.2
generate_icons "NotoSans-Bold.ttf" "de" "de" -0.2
generate_icons "NotoSans-Bold.ttf" "hn" "hn" -0.2
generate_icons "NotoSans-Bold.ttf" "hu" "hu" -0.2
generate_icons "NotoSans-Bold.ttf" "id" "id" -0.2
generate_icons "NotoSans-Bold.ttf" "ga" "ga" -0.275
generate_icons "NotoSans-Bold.ttf" "it" "it" -0.2
generate_icons "NotoSans-Bold.ttf" "sw" "sw" -0.3
generate_icons "NotoSans-Bold.ttf" "lv" "lv" -0.2
generate_icons "NotoSans-Bold.ttf" "lt" "lt" -0.2
generate_icons "NotoSans-Bold.ttf" "no" "no" -0.3
generate_icons "NotoSans-Bold.ttf" "pl" "pl" -0.15
generate_icons "NotoSans-Bold.ttf" "pt" "pt" -0.2
generate_icons "NotoSans-Bold.ttf" "ro" "ro" -0.3
generate_icons "NotoSans-Bold.ttf" "ру" "ru" -0.3
generate_icons "NotoSans-Bold.ttf" "sl" "sl" -0.2
generate_icons "NotoSans-Bold.ttf" "es" "es" -0.2
generate_icons "NotoSans-Bold.ttf" "sv" "sv" -0.3
generate_icons "NotoSans-Bold.ttf" "tm" "tm" -0.25
generate_icons "NotoSans-Bold.ttf" "tr" "tr" -0.25
generate_icons "NotoSans-Bold.ttf" "уk" "uk" -0.15
generate_icons "NotoSans-Bold.ttf" "vi" "vi" -0.2
