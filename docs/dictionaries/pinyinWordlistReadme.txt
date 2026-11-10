Simplified Chinese Pinyin word list by Vkiki2233:
Source: https://github.com/sspanak/tt9/issues/260

Previous frequency sources (before the 2026-10-27 refresh):
Simplified Chinese frequency list 1 by Cai & Brysbaert
Source: http://crr.ugent.be/programs-data/subtitle-frequencies/subtlex-ch
Version: 20231108150220
Reference: Cai, Q., & Brysbaert, M. (2010). SUBTLEX-CH: Chinese Word and Character Frequencies Based on Film Subtitles. Plos ONE, 5(6), e10729

Simplified Chinese frequency list 2 by HanziCraft
Source: https://hanzicraft.com/lists/frequency
Version 2025-03-19

Additional readings: 说 SHUO, 行 XING, 重 ZHONG, 绿 LV (2026-10-27).
Source: https://github.com/iDvel/rime-ice/blob/main/cn_dicts/8105.dict.yaml
Snapshot version: 2025-02-21; SHA-256: b208cd1e443b720e69a6cffe3a1dca35edbb191090e418baa40b8b3a985482ee
Source lines: 5839, 6963, 8297, 4054; raw weights: 12000460, 2756706, 1900814, 245485.
Existing readings are retained, including SHUI, HANG, CHONG and LU.
The existing V spelling for ü is retained (NV, LV, NVE, LVE).

Factory frequency refresh (2026-10-27): exact word-and-Pinyin matches use Rime Ice 8105.dict.yaml and base.dict.yaml, version 2025-02-21.
Additional source: https://github.com/iDvel/rime-ice/blob/main/cn_dicts/base.dict.yaml
base.dict.yaml SHA-256: 1f49260adb871baadbfd8aed11e6c32e68d7decf8a828b76c267c76da1d61d09
Weights use round(9999 * log(1 + source_weight) / log(1 + 76938354)); source zero or no exact match means zero.
This preserves the source priorities across all Pinyin groups rather than assigning ranks within each group. Rime's curated weights are not pure corpus counts.
463977 of 482284 readings match the sources; 463530 have positive weights, up from 61432. Unmatched readings remain available.
Examples: 你好 NIHAO 7002 > 米糕 MIGAO 4796; 你 NI 7801 > 谧 MI 4934.
All word-and-reading pairs and their row order are preserved. No source dictionaries or additional runtime dependencies are bundled.
These are factory defaults; personal learning remains managed by the existing input method.

Polyphonic-reading completeness repair (2026-10-28, issue #903):
The same pinned Rime Ice 8105.dict.yaml snapshot was audited by exact
(character, Pinyin) pair against all existing factory BMP characters. Its 8,341
active readings for 7,944 supported characters are now present. This appends 393
missing readings for 372 characters, including 长 ZHANG, 乐 YUE, 得 DEI,
单 DAN/SHAN, 着 ZHE/ZHUO and 嗯 ENG. All 482,284 preceding rows, their order,
and their weights are unchanged; the dictionary now has 482,677 pairs.
No new characters or supplementary-plane readings are added. Commented-out
source readings remain excluded. Three active source readings have weight zero
(了 LIAO, 鿎 TA, 育 YO); they remain typeable rather than being discarded.
The existing logarithmic formula gives the added readings their factory weights;
390 are positive, bringing the total to 463,920 positive and 18,757 zero weights.
This is completeness against the reviewed snapshot for supported BMP characters,
not a claim that it contains every Mandarin reading or regional pronunciation.

app/languages/chinese/polyphonic-readings.tsv records all 772 reviewed source
readings of the 375 supported characters with multiple source or legacy
readings, together with their raw weights and exact source line numbers.
The source-backed repair is repeatable and idempotent:
  python tools/repair_chinese_readings.py /path/to/8105.dict.yaml
  python tools/repair_chinese_readings.py /path/to/8105.dict.yaml --check
The tool verifies the source SHA-256 before writing. It never deduplicates by
character or changes an existing word/reading pair. Its regression suite is:
  python tools/test_repair_chinese_readings.py
The production parser, digit converter, sorter and ZIP writer are also exercised
by tools/test_chinese_reading_conversion.groovy (arguments: repository root and
an output directory), using groovy.ui.GroovyMain with the Gradle lib/* classpath.
That fixture verifies all 772 reviewed pairs survive conversion exactly once.

Rime Ice specifies GPL-3.0-only for its curated dictionary data. The Rime-derived
frequencies and generated ranking data are distributed under those terms; the
TT9 application source retains its existing Apache-2.0 license. Original list
provenance and all retained readings remain credited above. See:
  app/languages/chinese/RIME-ICE-NOTICE.txt
  app/languages/chinese/RIME-ICE-LICENSE.txt
Both notices accompany plain and combined Chinese download ZIPs under licenses/.
The matching TT9 source tree/patch contains the CSV and deterministic build
scripts needed to modify and regenerate these data.

==========

Traditional Chinese with Pinyin uses the same dictionary as the Bopomofo variant, just the transcriptions were converted using: https://github.com/logicmason/pinyin-zhuyin
