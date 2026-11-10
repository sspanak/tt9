# Chinese input verification

This is the verification record and reproduction guide for the upstream Chinese
input contribution. See [the update description](chinese-update.md) for scope and
[the extension notes](chinese-extension.md) for the package layout.

## Current #903 / #1051 follow-up results

| Check | Result |
| --- | --- |
| Host and Chinese feature unit tests | 243 passing in Full and 243 in Lite (96 host, 147 feature per flavor) |
| Python repair and release validation | 20 tests passing; pinned-source repair check reports no missing additions |
| Production dictionary conversion | All 772 reviewed polyphonic pairs occur exactly once after the real parser, sorter, digit converter and ZIP writer; license entries retained |
| Debug APK and matching combined pack | Full and Lite builds passed |
| Lite release with R8 and resource shrinking | APK and matching combined pack built with a local test signature |
| Lite debug and release lint | 0 errors, 389 warnings per variant, including missing translations for the two new English preference strings in other locales |
| API 35 Lite debug dictionary import | Production local-pack loader imported 482,677 rows; all 393 restored readings occur exactly once |
| API 35 Full debug installed engine | 30 checks passed with the actual Chinese and bundled English dictionaries |

The installed-engine checks cover 14 selected polyphonic readings, frequent Han
candidates for single keys 3 and 6, preservation of exact `E` and its explicit
filter, all ten English shortcut key sequences, and suppression when disabled,
filtered or using French. The real Pinyin mode retains uppercase `V` while
accepting `ü` and `u:` as reading-entry aliases. JVM regression checks cover
language-switch commit guards, structured-field restrictions, synthetic-candidate
learning isolation, ordinary preview/space behavior and full OK selection.

Runtime verification used only a disposable, read-only API 35 emulator. These
new changes were not installed on the Mive phone. This follow-up's R8 result is
a build check; its runtime result is from debug APKs. Earlier minified/device
results below remain historical evidence, not new coverage of these changes.

Local evidence is outside the checkout under `TT9-build-support`: the
`issues-903-1051-final-build.log`, `issues-903-1051-python-tests.log`, production
conversion output, and `issues-903-1051-runtime/report-full.json`. The earlier
Lite import report records successful Chinese checks before an English fixture
setup failure: the test had used the Chinese-only local-pack API for an English
ZIP. The final Full run uses the public loader and bundled English dictionary.

## Earlier PR #1210 results, before this follow-up

| Check | Result |
| --- | --- |
| Host and Chinese feature unit tests | 215 tests passing in Full; 215 in Lite (74 host, 141 feature) |
| Debug APK and combined Chinese pack build | Passed |
| Full debug Android lint | 0 errors, 387 warnings |
| Updated Full release with R8 and resource shrinking, including combined pack | Build passed |
| Updated Full release Android lint | 0 errors, 387 warnings |
| Lite debug Android lint | 0 errors, 387 warnings |
| Delivery packaging | Full debug, Lite debug and minified Full release built and validated against actual base/split hashes |
| Release preparation tests | 14 Python archive/inventory cases and isolated Groovy delivery-identity checks passed |
| Final Full delivery on API 35 | 404, truncation and wrong-base rollback passed; valid download, stable cancellation and cached Finish installation preserved all nine tables and functional preferences |
| Mive in-app delivery | 5/5 focused paths passed, with unchanged database tables, word frequencies and user settings |
| Final Mive provenance build | 2/2 download/install and Chinese-input paths passed; fresh nine-table and user-preference snapshots unchanged |
| Lite ordinary loading and local provenance on API 35 | Versioned online loading passed; a one-word legacy local pack stayed usable offline with no GET, then online loading restored all 482,284 factory rows |
| Final minified delivery build on API 35 | Real matching SAF import/native installation, exact APK hashes, native caret and expanded candidates passed |
| Fresh Chinese removal, API 35 debug | Delete/cancel/finish/reinstall passed; Chinese defaults reset, personal/global data retained and no automatic re-download |
| Final minified selective removal, API 35 | Real Delete Unselected/cancel/finish passed; 173,916 English rows and personal-table hashes unchanged, both splits removed, zero Chinese download requests |
| Subsequent Mive upgrade and panel/Pinyin continuation | Final matched debug APK/cache set installed; collapse → Up re-entry regression and system-caret delete/insert/convert passed; fresh nine-table and default-preference snapshots unchanged |
| Mive, Android 12, current edit/scroll combinations | 9/9 passed; three-option speed dialog also verified |
| API 35 current optional-domain engine checks | 252/252 passed, including all 103 new pairs enabled/disabled |
| API 35 current interactive speed choices and native repeats | 9/9 passed: English, Pinyin and expanded panel, three speeds each |
| Latest equal-height and Roomy layouts, API 35 debug and minified | Eight display/font/mode combinations per build; 20 rapid collapse/reopen cycles per combination |
| Latest Mive layout and repeat refinement | 5/5 focused cases passed; default rows match the ordinary 28 px row, Roomy is 33 px |
| First-layout cache regression, actual Android debug runtime | 11/11 assertions passed; the preceding layout build reproduced 10/11 failures |
| Final Mive cache-fix build | 3/3 geometry/native-caret cases passed; database and preferences unchanged |
| Final API 35 cache-fix build, debug and minified | Default/Roomy UI checks passed in both builds, 20 rapid reopen cycles each; minified native-caret return passed |
| API 35 signed-pack lifecycle, debug and minified | Real SAF/native install and split removal passed; personal words and host settings retained |
| Final catalog UI, debug English and minified Chinese | Seven compact rows, full scrollable provenance and persistent toggles passed |
| Final minified caret/panel smoke | System selection, edit/convert/reopen and numbered expanded panel passed |

Build results and runtime results are recorded separately. The minified runtime
coverage is the API 35 smoke described below; Mive used the debug artifacts.
Lint and Gradle warnings are present; this contribution does not claim a
warning-free build.

The preceding revision moved Chinese expanded layout, row numbering, separators and
navigation into the optional split, adds seven default-off domain vocabularies,
reclassifies settings and separates host single-row speed from Chinese expanded
speed. The engine and speed totals above cover this revision. The earlier device
matrices below (11 Mive, 23 API 35 debug, 6 API 35 minified combinations, 8 grid
checks and 24 engine profiles) describe the previous Pinyin revision and are kept
as historical evidence, not counted as new runs after this extraction.

Production-data JVM tests load the real Groovy-generated binary with the Java
reader and check all 2,980 source rows, 2,961 merged pairs, weights, domain masks,
per-domain isolation and prefix bounds. These run in Lite CI as well as Full.
The API 35 engine run preserves both domain preference keys and the complete
factory frequency totals, and creates/removes an isolated position-zero fixture
to verify a missing learning target cannot modify an unrelated word.

Mive's current checks cover all three edit exits, `745 → RI'LE → RIL`, appending
`3` and converting back to 日了, unchanged reading boundaries, `942 → XIA` plus
`6` resolving to `XI'AN`, held-list endpoints and release, short-tap wrapping,
expanded navigation/collapse and row-based number selection. Its six dictionary
tables, including all 482,284 word/frequency rows, 321,524 position rows, existing
learned pairs and custom-reading tables, were compared before/after and remained
identical. Numeric selection uses the actual learned leading candidate rather
than assuming a pristine dictionary always leads with 你.

Before the latest speed/layout refinement, the resource-only dialog correction
was rebuilt and linted in debug and R8.
All four base/feature DEX sets are byte-identical before and after that correction;
the final Mive APK hashes were verified, and its three speed choices were checked
again. Initial harness setup failures (a stopped IME falling back to Mive's system
keyboard, an unfocused test editor, and an unreachable spelling fixture) are kept
separately from product results. Native injected key events verify application
handling. The Mive user separately confirmed that the new held-arrow endpoints,
Pinyin-letter editing and return to Han candidates now work through physical keys;
this is a qualitative confirmation, not a timed hardware-scanner benchmark.

The subsequent speed/layout refinement replaces the Compact switch with a
default-off Roomy option. Both independent speed selectors now use minimum
native-repeat intervals of 120/60/30 ms for Slow/Normal/Fast. These are event
gates, not a promise that a device sends repeats that quickly. The API 35 fixture
sent repeats on absolute 20 ms deadlines and verified all three speeds in English,
Pinyin and expanded candidates. Mive's faster Normal setting, held endpoints,
release, short-tap wrapping, expanded collapse and native-caret return were also
checked; its previous speed selection was restored afterwards.

For both debug and minified API 35 builds, the latest geometry matrix covers
480 px at density 240 and 1080 px at density 420, system font scales 1.0 and 1.3,
and Roomy off/on. Ordinary/default expanded row heights match exactly at
33, 42, 58 and 74 px; corresponding Roomy rows are 38, 49, 67 and 86 px.
Roomy adds 2 dp padding per side, rounded to 6 or 10 extra pixels per candidate
at these densities. Each of the 16 combinations retained six fully visible rows
through 20 rapid collapse/reopen cycles. On Mive, default rows are 28 px and
Roomy rows 33 px; candidate width increases from 47 to 53 px, and both modes
passed the same 20-cycle check. These runs verify the normal-height cache as
well as modestly larger Roomy spacing, rather than a fixed grid cell size.
The first refinement's Mive preservation audit matched all six database tables and their
canonical hashes, including 482,284 words, 321,524 positions and eight learned
pairs. The only preference difference was the new, explicitly restored
Roomy=false value. Both user speed choices remained Fast; temporary test tools
were removed and the original official TT9 installation was left untouched.

The broad geometry/speed matrix preceded a final first-layout cache correction.
An isolated Android reflection probe reproduces the uncached fallback on the
preceding debug build and passes all 11 assertions on the final build, including
positive measured size without ordinary children, entirely unmeasured first
layout, wrapper padding, and reuse after old Grid/Linear layout states. The
probe changes no preferences or dictionary data. This private-method probe is
debug-only: R8 inlines the resolver, and no optimizer/keep rules were weakened
for testing. The final debug and minified builds each passed real UI geometry at
480 px, density 240 and font scale 1.0: default 33 px and Roomy 38 px, with six
fully visible rows through 20 rapid reopen cycles in each mode. The final
minified native-caret smoke moved selection 4 to 3 in XIAN, converted to 西安 and
reopened the full spelling list. Mive's final matching APK set
repeats both geometry modes and their 20-cycle checks before the preservation
audit. That final audit found zero preference differences and identical canonical
hashes for all six database tables; installed and cached split hashes matched
the final artifacts. Temporary tools were removed, both user speeds remained
Fast and Roomy remained off, with the test IME selected and its empty editor
focused. The official application was untouched.

The catalog-only revision keeps short count summaries and moves complete
licenses/provenance into a scrollable dialog. Its debug English and minified
Chinese views were checked separately, including all seven counts, full metadata
and persisted toggles. No prediction, timing, navigation or data format changed.
The final runtime base/feature/translation hashes were verified. The API 35 test
apps/files were removed, original application APK hashes and captured IME/display
settings were restored, and the emulator was shut down. Mive retains the updated
matching test build alongside its untouched original TT9 64.0 installation.

The current unit suite includes the repaired transitions: ordinary `XIAN` versus
an old editing preview, repeated edit/convert/reopen cycles, query/result/render
revision ordering, rapid second-Up queuing, stale short-reading rejection while
preserving enabled fuzzy alternatives, and finish/cancel/timeout behavior after
insertion or deletion. Full-spelling restoration is also checked in the device
operation matrices below.

## In-app delivery verification

The delivery revision adds a deterministic release download and validates its
base APK identity and both split hashes before committing changes. An unchanged
Chinese dictionary hash takes the feature-only path, preserving existing word
frequencies, learned pairs and explicit personal readings. These checks are
separate from the earlier candidate-layout matrices above.

The actual Mive Android 12 run passed five focused paths across the initial and
cancellation-fixed delivery builds: HTTP 404 and explicit retry, complete combined download
and native installation, final cancellation without another automatic prompt,
explicit Finish installation using the cached files, and Chinese/native-caret
input after installation. The final cancel/retry added no GET. Its three total
requests were one 404 and two complete downloads for the two tested base builds;
all requested only the exact deterministic asset name. The same nine Chinese and
English database table hashes and all default preferences matched throughout,
including 482,284 Chinese word rows, 321,524 positions, eight learned pairs and
the user's frequency totals. Installed/cached APKs and delivery metadata matched
the final immutable set. Temporary tools, the reverse mapping and fixture server
were removed; the install-source permission was restored to its original state.
The transient error toast was not captured, so this run claims retry behavior
and data preservation rather than observation of its exact message text.
The final local-provenance build then repeated one complete download/native
installation and Chinese-input smoke, passing both focused paths. Its fresh
nine-table snapshot and all user preferences remained identical, including
learning performed since the earlier baseline. Installed/cached APKs and the
descriptor matched this final build, and temporary services/tools/permissions
were cleaned up again.

On API 35, the initial delivery build reproduced an automatic second Android
update prompt after Cancel. The final build clears the queued offer when the
retained download activity owns consent. Its Full run rejected a 404, truncated
archive and a complete ZIP bound to the wrong base without changing any of nine
dictionary/custom/reading/meta tables, functional preferences or preceding pack
file hashes, and left no pending directories. A valid online update preserved
the same snapshot, including a real learned factory preference, explicit personal
reading and domain-learning fixture. Cancel stayed cancelled; explicit Finish
used the cached files with no additional GET before installing both splits.

Lite's ordinary Languages → Load Selected entry fetched its exact versioned Lite
asset and installed both splits through native consent. A final provenance case
imported a metadata-less, one-word local ZIP through SAF: the stored hash had the
local marker, the single word worked in the IME, and settings/IME startup made
zero GETs with the server unavailable. Ordinary online loading then fetched the
complete matching Lite ZIP, restored all 482,284 factory rows, and replaced the
local marker with the verified published hash. The final minified build passed
real matching SAF import/native installation, exact three-APK hash verification,
XIAN system-caret movement and conversion to 西安, and clean numbered expanded
rows. It was not tested against a live production HTTPS release endpoint.

Full delivery QA uses the real Full code and LITE=false, with only the ordinary
English and Chinese dictionaries bundled in its test APK. Other Full dictionaries
are omitted to keep the fixture small; Lite has no bundled word dictionaries.
This tests the Full offline/Chinese-download distinction without claiming a new
installation test of the stock all-dictionaries Full distribution. Debug online
checks use a loopback HTTP fixture permitted only by the external test manifest.
Production builds require HTTPS; minified release installation and input checks
use a matching local ZIP and do not claim a live production HTTPS release test.
No GitHub Release or signing-secret configuration is performed by these tests.

Release preparation validates actual base/feature/UI sets, complete source/license
notices and a closed checksum inventory. Fourteen Python cases reject damaged,
stale and mismatched archives, wrong tags, overwritten assets and unlisted files.
Workflow YAML, embedded Python and 19 shell blocks passed syntax checks; shell
blocks were not executed as a publication test.

## Chinese removal regression verification

A fresh API 35 emulator run revisited removal after the in-app delivery change.
It reproduced automatic re-download on a cold Chinese IME start and a missing
completion action after cancelling Android's split-removal prompt. Completing
native removal could also kill the callback before it cleared the pending flag,
leaving a stale Finish action. These are removal regressions, not failures of
the earlier download/install or candidate-operation matrices.

The corrected debug lifecycle used the real Languages deletion action and native
Cancel/confirmation. All 13 seeded Chinese preference keys and the private pack
were cleared, including two interrupted UUID staging/backup directories.
Personal-word and explicit-reading table hashes, enabled-language IDs, ordinary
candidate speed and star/pound swap were preserved. Chinese interface language
returned to System. Confirmed removal left only the base APK, and a cold Chinese
input attempt made zero download requests. Explicit reinstallation restored
482,284 factory rows plus the saved personal word/reading; Roomy was off, expanded
speed was Normal and no old domain choices or domain learning returned. A fresh
installation with no stored dictionary hash still downloaded normally.

The final debug build additionally passed completed-removal reconciliation and
stale-offer checks. An actual no-extra installer Activity finished without
opening installation UI or making a request; a headless removal with no installed
splits cleared the queued offer without launching an Activity. These state
fixtures used an external test APK, not new production debug paths. The staging
cleanup also has a host unit test that verifies unrelated files are retained.

The final Full release build was installed without debuggable mode and exercised
real SAF import and Android installation before selective removal. English stayed
selected, Chinese was unchecked, and Languages → Delete Unselected removed only
Chinese. Cancelling native consent left the splits installed and the completion
action available; after a cold start, explicitly re-enabling Chinese still made
zero download requests. Continue removal then removed both splits. All four
English tables, including 173,916 words, and both personal-word/reading tables
retained their exact hashes throughout. All Chinese preferences and pack files
were absent, while the English UI preference, Fast ordinary-row speed and
star/pound swap remained set. An external read-only instrumentation APK inspected
private state through Android/SQLite APIs; production optimizer rules and
debuggable settings were unchanged.

The destructive removal checks used the API 35 emulator. Mive was subsequently
updated from a fresh backup: the initial combined download preserved its data,
and the final matching base/split APKs and delivery cache were installed through
ADB. All nine dictionary/personal/metadata table hashes and default preferences
were identical before and after the final update. The official TT9 package was
left untouched; the test app's install-source permission was restored to Off.

This Mive run reproduced a separate panel continuation bug: after held Down/Up,
a short Up collapsed the panel, but the next Up was counted as a repeated spelling
filter and stayed on Han candidates. Resetting the legacy same-key counter at
the actual panel-to-row transition fixes that sequence. The final installed build
passed held scrolling and release, six 28-pixel rows with per-row numbering,
collapse → full spelling list → native Pinyin caret → delete/insert → Han, and
reopening the full spelling list after the edit. No test words were committed.
These are injected-key checks, not a new physical-keypad scanner test; removal
was not exercised on the user's phone.

## Reproduce the debug build

Use the repository's Gradle wrapper and its configured JDK/Android SDK. From the
repository root (`gradlew.bat` on Windows):

```sh
./gradlew :app:testLiteDebugUnitTest :chinese:testLiteDebugUnitTest \
  :app:packageLiteDebugChinesePack :app:lintLiteDebug
```

For the signed, versioned download assets and dry-run publishing inventory, see
[the release guide](chinese-release.md). The current delivery tests add eight
host JVM cases for identity, metadata bounds, wrong base/variant/signature and
payload damage. The extra five lint warnings are missing translations in the
base resource set for new download messages; Chinese translations are supplied
through the optional translation split. Existing warning totals and earlier UI
matrices below describe their respective revisions.

On Windows, a checkout made with `core.symlinks=false` can leave the tracked
`app/src/lite/java` symlink as a text file containing `../full/java`. The local
test environment maps the Lite Java source set to `app/src/full/java` through an
external test-only init script when that happens. This does not change the
repository's source layout. A normal Linux checkout/CI follows the tracked
symlink and needs no such mapping.

This exercises dictionary/parser and prediction behavior, candidate-row
numbering/navigation, Pinyin editing and selection tracking, custom-word/readings,
fuzzy matching, prefix indexing, common-character filtering, and staged-pack
rollback. Inspect the host and feature test reports under their respective
`build/reports/tests/` directories and the host lint report under
`app/build/reports/`.

Install the generated base APK from `app/build/outputs/apk/lite/debug/` on a test
device or emulator. Enable TT9 and Simplified Chinese, then import
`app/build/outputs/chinese-packs/liteDebug/zh-simplified-pinyin.zip` through
**Languages → Chinese Input → Import/update Chinese pack**. Confirm Android's
install-source and split-install prompts. The base and both splits must be from
the same build and use the same signing certificate. Split installation may
restart the input method.

## Mive Android 12 debug checks

Installed device APKs used an isolated test application ID and the Full source
set with lean test assets and Lite download behavior. The normal Full flavor's
complete bundled dictionary set was generated/validated but excluded from these
installed test APKs. The device results below validate Chinese input and optional
pack behavior; they are not an installation test of the stock all-dictionaries
Full APK. Lite unit tests were also compiled and executed separately.

The latest targeted run used ADB-injected Android `KeyEvent` input on the actual
Mive. This reaches Android's input method key handling, including repeat events;
it does not exercise the physical keypad scanner. Earlier human-operated keypad
checks and raw input-event capture establish separate physical-key evidence.
They are not a fresh physical-key retest of every latest change.

Verified targeted cases:

- Compose, commit, then delete the last character of 你好 (`64426`), 中国
  (`94664486`), 我们 (`96636`), 北京 (`2345464`), and 上海 (`74264424`).
- For ambiguous `943943`, accept 这些 or explicitly select 谢谢 with Right and
  commit it. Both spellings share the T9 sequence; 谢谢 is not required to be the
  first candidate.
- Expand candidates, move to another row, use that row's repeated number labels,
  and verify that an absent number leaves selection/composition unchanged.
- Rapid Up/Down and repeated arrows preserve the compact separators; the held
  arrow cases settle after release. A short Up from the first row collapses.
- Edit `XIAN` after the committed prefix 你好. Left/Backspace changes the
  composition to 你好XIN with the real selection inside the composing span;
  inserting the missing key restores 你好XIAN. Conversion produces 你好西安 while
  retaining the prefix. Editor selection/composition offsets were captured.
- Cancel Pinyin editing; enter numeric and password fields; confirm Chinese with
  Pound, insert the existing Chinese U+3000 space when idle, switch to numeric
  mode with Star, and switch between English and Chinese with held Star.
- Pass all 11 final operation combinations, including repeated spelling/edit/Han
  cycles. Explicitly selecting `XIAO` produces 小; selecting `XIAN` produces 先,
  with the corresponding distinct Han candidate lists.

Language switching above is the explicit key action. Changing an editor language
hint on a reused input connection was not verified as automatic language
switching and is not claimed here.

During fixture development, clearing a field and calling `restartInput` within
150 ms of a composing write could trigger TT9's existing automatic
composing-disable compatibility heuristic. The cached field could then show
candidates while the editor stayed blank. The final combination run used fresh
input sessions and passed; this contribution does not claim a product fix for
that pre-existing restart heuristic.

The final Mive preservation audit matched all 482,284 word/position pairs against
the pretest backup without reloading the dictionary. Only five identified
test-created frequency promotions were reverted; user learning and existing
preferences were preserved. Final installed APK hashes matched the tested set.

Local evidence is kept outside the repository under the QA workspace's
`global-qa-mive/` directory: `normal-typing-cases-verified.json`,
`thanks-committed.json`, `prefixed-native-caret-verification.json`,
`held-arrows.json`, `mixed-field-keypad-checks-verified.json`,
`reading-to-han-mapping.json`, `qa-final-report.json`, row-state captures, and
screenshots. Device backups and private preferences are not release assets.

## API 35 debug and minified runtime results

The final Android 15 emulator report records 23/23 debug operation combinations,
6/6 minified release combinations, 8/8 candidate-grid/repeat checks, and 24/24
installed-engine profiles passing. Checks included full spelling-list restoration,
rapid transitions, held phonetic arrows, second-row numeric selection, natural
candidate widths, and the absence of labels or reserved number space when numeric
selection is disabled. Normal multiword typing, backspace, numeric/password
fields, and mixed input also passed.

The final minified build loaded the reflected Chinese settings and word-manager
entry points, passed custom-word creation/reading-edit/deletion, and displayed
explicit Chinese and system UI language selections. Its installed APK hashes
matched the final immutable base/feature/UI set. No unresolved product failures
were reported. The emulator's original state was restored and shutdown verified.

Normal document-picker import and Android split-install consent were exercised
on the earlier debug build before the Pinyin-only transition fixes. The final
code upgrade used `adb install-multiple -r` with matching three-APK sets; the final
runtime results do not imply a fresh document-picker installation of that last
set.

Full personal-word import/export and invalid-import atomic rollback, plus
dictionary/profile/split deletion and offline reload with personal readings
preserved, passed in the earlier unchanged-path verification. Those complete
lifecycle cases were not freshly repeated in the final operation matrix.

Evidence is outside the repository in `global-qa-api35/qa-final-report.json` and
its referenced captures. Earlier lifecycle evidence is separately recorded in
`api35-split-feature-qa/qa-final-complete.json`. Injected Android key events do not
validate a manufacturer's physical keypad scanner.

## Reproduce a minified release smoke test

Use a disposable test environment. Keep any test keystore, passwords, and signing
init script outside the checkout. Configure the base release signing config with
a local key; the feature and generated UI split must use that same key. For
example, save this init script outside the repository and supply the named
environment variables through the local shell or CI secret store:

```groovy
gradle.beforeProject { project ->
    project.plugins.withId('com.android.application') {
        project.androidComponents.finalizeDsl { android ->
            def keyStore = System.getenv('TT9_SMOKE_KEYSTORE')
            def storePass = System.getenv('TT9_SMOKE_STORE_PASSWORD')
            def keyAlias = System.getenv('TT9_SMOKE_KEY_ALIAS')
            def keyPass = System.getenv('TT9_SMOKE_KEY_PASSWORD') ?: storePass
            if (!keyStore || !storePass || !keyAlias || !keyPass) {
                throw new GradleException('Missing local smoke signing configuration')
            }
            def signing = android.signingConfigs.create('localSmoke')
            signing.storeFile = new File(keyStore)
            signing.storePassword = storePass
            signing.keyAlias = keyAlias
            signing.keyPassword = keyPass
            android.buildTypes.release.signingConfig = signing
        }
    }
}
```

Build using that script's absolute path:

```sh
./gradlew -I /absolute/path/to/local-smoke.init.gradle \
  :app:packageLiteReleaseChinesePack :app:lintLiteRelease
```

The existing `release` build type enables R8 and resource shrinking. The Full
flavor equivalents (`packageFullReleaseChinesePack`, `lintFullRelease`) also
exercise the offline flavor. Keep test signing material and test-signed artifacts
out of upstream distribution; publishing uses the upstream signing process.

Install the matching release base and ZIP through the same user-facing import
path. Verify the following on the minified build:

1. Start the base without the feature installed, open the Chinese bootstrap import
   screen, and install both signed splits using Android's confirmation.
2. Open Languages → Chinese Input and word management, plus Appearance → Chinese
   Appearance. Change the host Appearance → Interface Language preference and
   check that Chinese is offered only with the translation split installed.
   Confirm that reflected feature entry points and resources load, and that
   import/export appears only in Languages → Added Words.
3. Repeat candidate expansion, current-row number selection, and held-arrow
   release checks. Cycle Han → spellings → Pinyin edit → Han → spellings, checking
   the whole alternative list after each cycle. Include rapid Up/Up, immediate
   insert/delete → Down/confirm → Up, no-match input, and Back cancellation.
   Expand candidates, hold Down then Up to reach the first row, release, and
   press Up to collapse. The next Up must open the full spelling list; another
   Up must edit Pinyin, and Down must return to Han candidates.
   Restart input while editing and confirm that the old cursor override does not
   survive; separate fixture-triggered composing-disable behavior as noted above.
4. Add/edit/export/import a word with an explicit reading; test fuzzy and
   common-character switches; confirm ordinary English/numeric/password input.
5. Cancel a split update, resume pending installation from the same Chinese
   settings screen, and verify that a mismatched version/signature is rejected by
   Android. Remove the dictionary, inspect profile/data cleanup, confirm native
   split removal, and reimport while preserving personal words/readings.

Record APK/ZIP hashes, application ID/version, Android version, signer match,
test outcomes, and crash-log results. Preserve the exact base/split set used;
debug runtime results cannot substitute for this R8 runtime smoke test.

## STAR firmware compatibility and Help follow-up

The optional host service is disabled by default. On the Mive AT-M120 (Android 12), enable **Keypad → System * key compatibility**, then explicitly enable that service in Android Accessibility settings. Only key filtering is declared (`canRetrieveWindowContent=false`). The setting remains configured while Android temporarily rebinds the service; the bound-service list is not used as proof that the user revoked access.

Validation for the debug `full` test application, versionCode 1665:

- Full and Lite host/Chinese unit suites: 245 passing per flavor; Full debug lint has no errors.
- Mive physical password-field STAR hold: 3.599 seconds recorded, language switching worked, and the ringer remained NORMAL. The device owner also confirmed the result.
- Mive virtual-hardware keyboard path (`uinput`, through InputReader, accessibility filtering and native policy): 19 scenario groups passed. They cover ordinary text, email, ordinary/visible/web passwords, number/PIN/phone restrictions, confirmed `@` plus Backspace in five text-field types, short/rapid/canceled STAR, star/pound swap, the disabled option and behavior outside an editor. Virtual hardware testing does not replace keypad-scanner evidence.
- API 35 emulator: 29 additional direct-callback scenarios passed, including focus cancellation, disabling mid-hold, enabling mid-stream and unrelated-key/IME passthrough. These are callback tests, not physical-routing tests. Its original IME/accessibility state and temporary-package cleanup were verified.
- Actual Help preference opened the test application's own offline document on Mive; its external Installation Guide launched the configured Via browser. The emulator also rendered the offline contents anchor and launched its default browser.

Keep a virtual keyboard connected through each entire composing sequence. Removing it after every candidate movement can restart an IME. Span-only composing changes need a live editor snapshot rather than a stale TextWatcher record. Use Generic key-layout scan codes for virtual digits; the Mive Generic layout maps STAR/POUND but does not contain the dedicated Linux numeric digit entries used by its built-in vendor keypad.

The final debug APK was freshly packaged to remove unused incremental ZIP space. Its 15,483,701 bytes are 10,502 bytes above the previous installed base. On that final build, with remembered ABC mode and optional alphabetic prediction disabled, a Chinese-hinted field showed the single-row Han candidates and expanded numbered rows in Suggestion list only layout; the touch function-key row stayed hidden. The ordinary ABC display option is retained; no remembered-mode preference is rewritten. Nine dictionary/personal-word/learning tables were unchanged against the fresh pre-update snapshot after testing. This is debug runtime coverage; no new R8 runtime claim is made for this follow-up.
