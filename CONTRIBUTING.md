# Traditional T9 Contribution Guide
If you would like to contribute to the project by fixing a bug, adding a new language or something else, read below.

## Getting Started
- If you are about to write code, you need to setup your development environment. See the [Building](#building) section.
- If you would like to add a new language, follow the [New Language](#adding-a-new-language) section.
- Finally, see what is the actual [contribution process](#contribution-process) and how to get your code merged.

## Building
The recommended way of building is using Android Studio. The project is compatible with Android Studio Ladybug | 2024.2.1.

### Building a Debug .apk
If you have not configured Android Studio yet, follow [the official manual](https://developer.android.com/training/basics/firstapp), then follow the simple steps below to get the project running.

- _Import the project in Android Studio._
- _Prevent the "Default Activity not found" issue._ If Android Studio fails to identify the default Activity, you must configure it not to launch anything after installing, otherwise it will fail with "Default Activity not found" or a similar message. To do so:
    - Open "Edit Configurations..." (Press Shift 3 times and select it from the command list)
    - Go to "General" tab.
    - Change "Launch Options" to "Nothing"
    - Hit "OK"

_You can find more info in this [Github issue](https://github.com/android/input-samples/issues/18)._

_Since this is an IME, it makes little sense to open the configuration screen every time you test on a real device. It may be practical keep "Launch Options" set to "Nothing" at all times, not only when you encounter the error above._

That's it! Now you should be able to deploy and debug the app on your device.

### Building a Release .apk
The project is configured to build an unsigned release variant by default.

- Select the "release" variant from Android Studio options (`Build` -> `Select Build Variant...`)
- `Build` -> `Rebuild Project`. After that, just ignore all warnings until you get to the end of the process.
- Find the `.apk` in the generated 'build/' folder.

_Note that it may not be possible to install an unsigned `.apk` on newer versions of Android. You must either manually sign it or build a signed one instead._

### Building a Signed .apk
Make sure you have a signing key. If you don't have one, follow the [official manual](https://developer.android.com/studio/publish/app-signing#sign-apk).

- Select `Build` -> `Generate Signed Bundle / APK...`.
- Select `APK` and proceed to the next screen.
- Enter your key details (or create a new one) and continue to the next screen.
- Choose the "Release" variant, then click `Finish` to start building.
- Android Studio will tell you where the `.apk` is, but if it does not, try looking for it in the `release/` folder.

## Adding a New Language
To support a new language one needs to:

- Find a suitable dictionary and add it to the `app/languages/dictionaries/` folder. Two file formats are supported, [see below](#dictionary-formats).
- Do not forget to include the dictionary license (or readme) file in the `docs/` folder.
- Create a new `.yml` file in `app/languages/definitions/` and define the language properties.
  - `locale` contains the language and the country codes (e.g. "en-US", "es-AR", "it-IT"). Refer to the list of [supported locales in Java](https://www.oracle.com/java/technologies/javase/jdk17-suported-locales.html#modules).
  - `dictionaryFile` is the name of the dictionary in `app/languages/dictionaries/` folder.
  - `layout` contains the letters and punctuation marks associated with each key.
    - For 0-key `[SPECIAL]`, will be fine in most languages, but you could define your own set of special characters, for example: `[@, #, $]`.
    - For 1-key, you could use `[PUNCTUATION]` and have standard English/computer punctuation; or `[PUNCTUATION_FR]` that includes the French quotation marks: `«`, `»`; or `[PUNCTUATION_DE]` that includes the German quotation marks: `„`, `“`. And if the language has extra punctuation marks, like Spanish, you could complement the list like this: `[PUNCTUATION, ¡, ¿]`. Or you could define your own list, like for 0-key.
    - Keys 2 through 9, just contain the possible letters.
  - `abcString` _(optional)_. A custom string to display in ABC mode. By default, the first three letters on 2-key are used (e.g. "ABC" or "АБВ"). Set this if the first letters of the alphabet are _not_ on 2-key, like in Hebrew, or if a different string makes more sense.
  - `currency` _(optional)_. A string representing the currency related to that language, for example: `₪`, `₩`, `﷼`, etc. The character will be displayed in position 3 between the factory currency characters.
  - `hasSpaceBetweenWords` _(optional)_ set to `no` when the language does not use spaces between words. For example: Thai, Chinese, Japanese, Korean, and so on. The default is `yes`.
  - `hasUpperCase` _(optional)_ set to `no` when the language has no upper- and lowercase letters. For example: Arabic, Hebrew, East Asian languages, and so on. The default is `yes`.
  - `name` _(optional)_ is automatically generated and equals the native name of the language (e.g. "English", "Deutsch", "Українська"). However, sometimes, the automatically selected name may be ambiguous. For example, both Portuguese in Portugal and Brazil will default to "Português", so assigning "Português brasileiro" would make it clear it's the language used in Brazil.
  -`numerals` _(optional)_ can be used to set a custom list of numerals. The list must contain exactly 10 characters equivalent to the digits from 0 to 9. For example, in Arabic you could use: `numerals: [٠,١,٢,٣,٤,٥,٦,٧,٨,٩]`.
  - `sounds` _(mandatory for non-alphabetic languages)_ is an array of elements in the format: `[sound,digits]`. It is used for East Asian or other languages where there are thousands of different characters, that can not be described in the layout property. `sounds` contains all possible vowel and consonant sounds and their respective digit combinations. There must be no repeating sounds. If a single Latin letter stands for a sound, the letter must be capital. If more than one letter is necessary to represent the sound, the first letter must be capital, while the rest must be small. For example, "A", "P", "Wo", "Ei", "Dd". The sounds are then used in the dictionary format with phonetic transcriptions. See `Korean.yml` and the respective dictionary file for an example.

### Dictionary Formats

#### TXT Containing a Simple Wordlist
The most basic format is just a list of words where each word is on a new line.

Constraints:
- No single lowercase letters. They will be added automatically.
- No repeating words.
- No digits or garbage characters as part of the words.
- The words must consist only of the letters definied in the respective YML definition file.

_The constraints will be verified automatically upon building._

Example:
```
word
another
third
...
```

#### CSV Containing Words and Frequencies
The second accepted format is CSV containing the word and its frequency on each row.

Constraints:
- No header.
- The separator is `TAB`.
- The frequency is optional. If missing, it is assumed to be 0.
- The frequency must be an integer between 0 and 255, when present.

_The TXT format constraints listed above also apply._

Example:
```
word 35
another 49
frequenciless
fourth
fifth   3
...
```

#### CSV Containing Words and Phonetic Transcriptions
The third accepted format is suitable for East Asian and other languages with many different characters. Each character or word has a phonetic representation using Latin letters. Frequencies are not applicable in this format.

Constraints:
- No header.
- The separator is `TAB`.
- The first element is the language character or word
- The second element is the phonetic representation with Latin letters. It must be a combination of the `sounds` in the respective YAML definition.

Example definition:
```yaml
# ...
- sounds:
  - [We,123]
  - [Tt,221]
  - [Wo,48]
```

Example dictionary:
```csv
다 WeWo
줘 TtWoWe
와 Wo
```

Using the above example, when the user types "221-48-123", it will result in: "줘".

See `Korean.yml` and `ko-utf8.csv` for more examples.

## Merging Languages
Traditional T9 does not officially support typing in more than one language simultaneously, and it will not provide such a possibility in the future. But if you are interested in a do-it-yourself solution, you can achieve it by merging the dictionaries for two (or more) languages into a new language. Follow the steps in [this discussion](https://github.com/sspanak/tt9/issues/713) about merging Czech and English. Just keep in mind: a) there can be no repeating words; b) it is not recommended to go over 2 million words, especially on low-end devices. Normal performance is not guaranteed beyond that limit.

## Translating the UI
To translate Traditional T9 menus and messages in your language, add: `res/values-your-lang/strings.xml`. Then use the Android Studio translation editor. It is very handy.

Alternatively, if you don't have Android Studio, you could just use `res/values/strings.xml` as a reference and translate all strings in your file, skipping the ones that have the `translatable="false"` attribute.

## Contribution Process

### Before you start
- Find the issue you are interested in or create a new one.
- Assign it to yourself, to indicate you are working on it. This will prevent someone else, who is unaware that you are working, to pick it up and do the same thing.

### After you are done
- Ensure there are no building errors or warnings, and the code works properly.
- Clean up any commented or unused code.
- Rebase with the latest `master`. Fix any conflicts, if necessary.
- Open a pull request and link the issue you are solving.
- If any review discussions begin, you may need to do some extra improvements.
- Have in mind, some PRs may be rejected, for example, if you have used third-party code or images without an appropriate license, or if your changes are targeting a very specific phone and breaking functionality on another one. It may be rejected due to other reasons, too.
- Once all discussions have been resolved, your PR is ready for merging. Congratulations and thank you for the contribution!