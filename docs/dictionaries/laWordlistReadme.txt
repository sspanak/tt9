Classical Latin word list source:
https://github.com/Alatius/latin-macronizer

Dictionary file used for conversion:
https://raw.githubusercontent.com/Alatius/latin-macronizer/master/macrons.txt

Upstream license:
- GPL-3.0 (repository license file)

Conversion notes for TT9:
- Parsed the macronized inflected-form column (4th TAB-separated field), which includes full forms
  (e.g. finite verb tenses/persons, infinitives, participles, declined forms).
- Converted marker notation to Unicode vowels:
  - removed breve marker `^`
  - converted long-vowel marker `_` to macrons on the preceding vowel (`ā ē ī ō ū ȳ`)
- Lowercased words, removed duplicates, removed 1-letter entries.
- Kept only alphabetic entries matching `[a-zāēīōūȳ]+`.
- Output format: one word per line (`la-utf8.txt`).
