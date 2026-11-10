# Simplified Chinese extension

The base APK keeps shared keyboard rendering, storage and a small extension contract.
`chinese/` contains the Pinyin editor, syllable rules, fuzzy matching, prefix lookup,
user-word manager and Chinese settings. Host UI translations are a resource-only
`config.zh_rCN` split, with IDs generated from that exact base build.

Build matching signed debug artifacts with:

```
./gradlew :app:testLiteDebugUnitTest :chinese:testLiteDebugUnitTest :app:packageLiteDebugChinesePack :app:lintLiteDebug
```

The main deliverables are the base APK and
`app/build/outputs/chinese-packs/liteDebug/zh-simplified-pinyin.zip`.
The ZIP starts with the normal dictionary entry, followed by the common-character
table and its Unicode license, the compact prefix index, `chinese.apk` and
`chinese-ui.apk`. Do not put this ZIP back into the base APK's assets.
Release packaging requires the same configured signing key for the base and both
splits. Android checks the package, version and signing certificate during install.

Enable Simplified Chinese, then use Languages → Chinese Input → Download Chinese
Pack. Loading Chinese from the ordinary dictionary list uses the same combined
download when its feature is missing or does not match the installed base. One
download supplies the dictionary, data and both signed splits; completion proceeds
to Android installation consent. The file importer remains available for offline
installation of a matching ZIP. Android may ask to allow installs from this source
and will ask for installation confirmation. Installing or removing splits can
restart the input method. No previous dictionary download is required to import
this ZIP. Full retains bundled plain dictionaries for its other languages and
uses Internet access for the optional Chinese download. Both flavors keep Chinese
extension code, indexes and translated host UI outside the base. Other languages
retain their existing dictionary download or bundled-asset paths.

Chinese downloads resolve an immutable release asset by installed package,
version code, flavor, build type and signing certificate. The release workflow
stamps the destination release URL into the matching base build. Schema-one
delivery metadata also binds the exact base APK SHA-256 and both split hashes;
validation happens before committing dictionary or pack changes. Missing,
incompatible and damaged downloads leave the previous dictionary/pack intact.
When a published dictionary hash is unchanged, updating just the feature files
preserves learned frequencies and user data. Local imports use a separate hash
marker: they remain usable offline, but cannot falsely authorize this fast path
for a different or older dictionary on a later online reload.
The production downloader requires HTTPS. Only debug builds allow a loopback
HTTP fixture for local QA; release networking is not weakened for tests.
See [release packaging and publishing](chinese-release.md) for the signed build,
dry-run verification and explicit publishing workflow. Earlier combined ZIPs
without delivery metadata remain supported for local import; online downloads
require the new metadata and are never redirected to a data-only dictionary.

Deleting the Chinese dictionary removes its settings and private pack data, then
asks Android to remove both optional splits. Declining that confirmation leaves
the code installed but the dictionary and Chinese profile are already removed.
The Languages entry remains available for completing a cancelled removal, even
if Chinese has been deselected. Pending state is reconciled against the actual
installed splits because native removal can kill its completion callback.
An intentionally emptied Chinese dictionary does not automatically download
again on a cold IME start; explicit download/import still reinstalls it. Removal
also clears queued installer offers and interrupted UUID staging/backup folders.
If Chinese remains enabled, the same Languages entry remains available to import
the pack again. Deselecting a language does not delete it. User words and their explicit
readings survive removal and return when the pack/dictionary is reloaded.

Settings follow their function: Languages → Chinese Input contains pack
import/update, personal words/readings, numeric candidate selection, prefix
completion, domain vocabularies, expanded-panel repeat speed, fuzzy rules and the common-character filter. Appearance → Chinese
Appearance contains candidate presentation. The single, global Interface Language
selector belongs to the host Appearance screen: System and English are always
available, and Chinese is offered only while its translation split is installed.
Deleting Chinese resets a selected Chinese UI language to System while preserving
an English preference. The global star/pound swap remains under Keypad.
Ordinary single-row repeat speed also belongs to the host Keypad screen. The
Chinese feature independently owns expanded-panel speed and all expanded layout,
packing, row numbering, separators and navigation; the host retains only a generic
`CandidatePanel`/adapter presentation contract. Without the feature, ordinary rows
continue to work and expanded-panel settings are absent. Held single-row navigation
stops at either end; ordinary short taps retain cyclic selection. Native repeat
events are rate limited without a timer that could continue after release.
Slow/Normal/Fast use minimum native-repeat intervals of 120/60/30 milliseconds in
both independently configured views. Expanded rows normally use the measured
ordinary-row height. The optional, default-off Roomy candidates setting adds
about 15% height and 2dp padding on each side. It replaces the former Compact switch;
cached normal cell/wrapper measurements keep rapid reopen cycles stable.

Seven default-off domain vocabularies ship as a 126,839-byte indexed data file in
the Chinese download, together with licenses and provenance notices. All parser,
query, ranking, activation and bounded learning code lives in the optional feature.
See [domain vocabulary sources and maintenance](chinese-domain-vocabulary.md).

The single custom-word import/export in Languages → Added Words also accepts
Chinese after the code split is installed. The Chinese word manager does not
duplicate these actions. The format is `word<TAB>462106<TAB>reading`, with one unaccented
syllable per Han character, for example `重庆<TAB>462106<TAB>chong qing`.
Reading boundaries are stored separately, so editing `CHAN'GAN` into `CHANG'AN`
does not silently lose the chosen boundary. Other languages retain their existing
two-column format and the usual import limits.

The common-character switch is off by default and uses the 2013 national table's
3,500 level-one characters; explicitly saved user words bypass it. Fuzzy rules are
also off by default. Phrase completion and expanded-panel number selection are on
by default. Completion replaces the current composition; zero-key prediction
after a committed word and partial-syllable confirmation are not implemented.

Each visual row of the expanded candidates restarts its numeric labels at 1. A
digit selects that position in the highlighted candidate's row; an absent position
does nothing. Numbered rows contain at most nine naturally sized candidates.
Common exact single characters precede prefix completions, while less frequent
characters follow them. Learned leading candidates and explicit user words retain
their priority. The prefix index also carries a small per-reading frequency table;
older version-one indexes remain readable.

The second Up enters Pinyin editing with the application's actual composing-text
cursor. Left/Right, insertion and backspace move or modify that selection. Editors
that reject composing selection retain the visible inline cursor fallback.
Incomplete spelling choices can enter editing too: `745` with selected `RI'LE`
opens only `RIL`, preserving the typed key count. Returning unchanged retains the
original explicit spelling filter; edited readings retain resolved separators so
`XI'AN` stays distinct from `XIAN`.

The GitHub/local-install channel uses Android's package installer, not loose DEX
loading. A Play distribution would need its own Play Feature Delivery integration.
