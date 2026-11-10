# Optional Simplified Chinese data

`common-3500.txt` contains exactly the 3,500 level-one characters of the 2013
《通用规范汉字表》, in table order. The old 1988《现代汉语常用字表》also had 3,500
characters, but its membership differs; the UI deliberately names the 2013 table.

Source: Unicode 17.0.0 `Unihan_OtherMappings.txt`, property `kTGH`, values
`2013:1` through `2013:3500`, from
<https://www.unicode.org/Public/17.0.0/ucd/Unihan.zip>.
Archive SHA-256: `f7a48b2b545acfaa77b2d607ae28747404ce02baefee16396c5d2d7a8ef34b5e`.
The 10,500-byte UTF-8 file has no BOM or trailing newline. Regenerate with
`python tools/extract-common-han.py /path/to/Unihan.zip` from this directory.

Primary explanations of the table and the mapping:

- <https://www.moe.gov.cn/srcsite/A19/s229/201310/t20131015_159487.html>
- <https://www.unicode.org/reports/tr38/#kTGH>

The optional common-character filter checks every Han code point in a candidate,
including supplementary-plane characters. It never deletes factory dictionary
entries. A user-created word bypasses the filter, so a rare personal name remains
usable. The switch is off by default.

These data and the generated phrase-prefix index belong to the downloadable
Chinese pack, not the base application's assets.

The Rime-derived dictionary frequencies and generated ranking data are
distributed under GPL-3.0-only, as specified by Rime Ice. See
`RIME-ICE-NOTICE.txt` for sources, exact input hashes, changes and corresponding
source paths, and `RIME-ICE-LICENSE.txt` for the upstream license. These two
notices are included under `licenses/` in both the plain Chinese dictionary
ZIP and the combined extension pack. The application source retains its
existing Apache-2.0 license; the Unicode character table keeps its own license.

The phrase-prefix index is generated deterministically from
`../dictionaries/zh-simplified-pinyin.csv` by `buildChinesePrefixIndex`. It retains
up to 16 continuations per prefix, only when the prefix is itself a factory word
and the continuation has a factory weight of at least 5000. Only referenced
phrases are stored. The current raw index is about 1.8 MiB; loading it retains
the packed bytes and about 0.3 MiB of record offsets instead of a large word map.

Index format V2 also stores 2,581 frequent single-character readings and their
original weights, adding about 30 KiB. A weight of at least 5800 on the existing
logarithmic scale, together with membership in the 3,500-character core table,
identifies ordinary single-character choices. They precede phrase completions;
secondary characters follow them. Learned leading choices, custom words and
fully typed exact phrases retain priority. Older V1 indexes remain readable.

Ranking prefers fewer additional Han characters. It uses the original weight
divided by the square of the added character count, with a small bonus derived
from an existing common question form. Existing `吗`, `呢`, or `吧` continuations
can appear beside their base phrase, so `你` offers both `你好` and `你好吗`.
Stored words, readings and weights remain unchanged. Each result carries the
full original reading and T9 sequence for accepting and learning the phrase.

This is a bounded offline completion index, not a sentence language model. It
does not invent phrases or search the full factory dictionary on each keypress.
Uncommon continuations remain available through ordinary Pinyin input; user
words are handled separately from this factory index.

`polyphonic-readings.tsv` records the reviewed Rime Ice source readings, raw
weights and line numbers for 375 supported BMP characters with multiple source
or legacy readings. The source-hash-verified repair tool
`tools/repair_chinese_readings.py` appends missing exact word/reading pairs while
preserving every existing row and weight. The 2026-10-28 audit restored 393
readings; the dictionary now has 482,677 pairs. No new characters or
supplementary-plane readings were introduced. See
`docs/dictionaries/pinyinWordlistReadme.txt` for scope and reproduction commands.
