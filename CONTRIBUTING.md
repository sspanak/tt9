# Traditional T9 Contribution Guide
If you would like to contribute to the project by fixing a bug, adding a new language or something else, read below.


## Getting Started
- If you are about to write code, you need to setup your development environment. See the [Building](#building) section.
- If you would like to add a new language, follow the [New Language](#adding-a-new-language) section.
- Finally, see what is the actual [contribution process](#contribution-process) and how to get your code merged.


## Building
The recommended way of building is using Android Studio. As the of time of writing this, the current version is: Android Studio Dolphin | 2021.3.1.

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

- Find a suitable dictionary and add it to the `assets/` folder. Two file formats are supported, [see below](#dictionary-formats).
- Do not forget to include the dictionary license (or readme) file in the `docs/` folder.
- Create a new language class in `languages/definitions/` and define its properties.
  - `locale` contains the language and the country codes (e.g. "en-US", "es-AR", "it-IT"). Refer to the list of [supported locales in Java](https://www.oracle.com/java/technologies/javase/jdk8-jre8-suported-locales.html#util-text).
  - `dictionaryFile` is the name of the dictionary in `assets/` folder.
  - `characterMap` contains the letters and punctuation marks associated with each key.
  - `abcString` _(optional)_. A custom string to display in ABC mode. By default, the first three letters on 2-key are used (e.g. "ABC" or "АБВ"). Set this if the first letters of the alphabet are _not_ on 2-key, like in Hebrew, or if a different string makes more sense.
  - `hasUpperCase` _(optional)_ set to `false` when the language has no upper- and lowercase letters. For example: Arabic, Hebrew, East Asian languages, and so on. The default is `true`.
  - `name` _(optional)_ is automatically generated and equals the native name of the language (e.g. "English", "Deutsch", "Українська"). However, sometimes, the automatically selected name may be ambiguous. For example, both Portuguese in Portugal and Brazil will default to "Português", so assigning "Português brasileiro" would make it clear it's the language used in Brazil.
- Finally, add the new language to the list in `LanguageCollection.java`. You only need to add it in one place, in the constructor. Please, be nice and maintain the alphabetical order.


### Dictionary Formats

#### TXT Containing a Simple Wordlist
The most basic format is just a list of words where each word is on a new line.

Constraints:
- No single lowercase letters. They will be added automatically.
- No repeating words.
- No digits or garbage characters as part of the words.

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
- The frequency must be a non-negative integer, when present.

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

## Translating the UI
To translate Traditional T9 menus and messages in your language, add: `res/values-your-lang/strings.xml`. Then use the Android Studio translation editor. It is very handy.

Alternatively, if you don't have Android Studio, you could just use `res/values/strings.xml` as a reference and translate all strings in your file, skipping the ones that have the `translatable="false"` attribute.

## Adding Support for New Hardware Keys (Hotkeys)
TT9 allows assigning hotkeys for performing different functions. If your phone has a special key that does not appear on the Hotkey configuration screen, you can easily add support for it.

- In `preferences/helpers/Hotkeys.java`, find the `generateList()` function.
- Add the new key there. The order of adding is used when displaying the dropdown options.
- Optionally, you can translate the name of the key in different languages in the `res/values-XX/strings.xml` files.

 _You can find the key codes [in the Android docs](https://developer.android.com/reference/android/view/KeyEvent)._

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