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
- _Prevent the "Default Activity not found" issue._ The app does not have a default view or a launcher icon. For this reason, you must configure Android Studio not to launch anything after installing, otherwise it will fail with "Default Activity not found" or a similar message. To do so:
    - Open "Edit Configurations..." (Press Shift 3 times and select it from the command list)
    - Go to "General" tab.
    - Change "Launch Options" to "Nothing"
    - Hit "OK"

That's it! Now you should be able to deploy and debug the app on your device.

You can find more info in this [Github issue](https://github.com/android/input-samples/issues/18).

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

- Add status icons
    - Create a proper icon for each screen size. The icon needs to contain the abbreviation of the language. (e.g. "En" for "English").
    - The font must be Roboto Lt at an adequate size to fit the icon square with minimum padding.
    - The text must be white and the background must be transparent as per the [official Android guide](https://android-doc.github.io/guide/practices/ui_guidelines/icon_design_status_bar.html).
    - To simplify the process, you could use Android Studio. It has a built-in icon generator accessible by right-cicking on "drawable" folder -> New -> Image Asset. Then choose "Icon Type": "Notification Icons", "Asset Type": Text, "Trim": No, "Padding": 0%.
- Find a suitable dictionary and add it to `assets` folder. Two file formats are supported, [see below](#dictionary-formats).
- Do not forget to include the dictionary license (or readme) file in the `docs/` folder.
- Create a new language class in `languages/definitions/`. Make sure to set all properties.
  - `ID` must be the next available number.
  - Set `isPunctuationPartOfWords` to `true`, if you need to use the 1-key for typing words, such as: `it's`, `a'tje` or `п'ят`. Otherwise, it would not be possible to type them, nor will they appear as suggestions. `false` will allow faster typing when apostrophes or other punctuation are not part of the words.
- Add the new language to the list in `LanguageCollection.java`. You only need to add it in one place, in the constructor. Please, be nice and maintain the alphabetical order.


### Dictionary Formats

#### TXT Containing a Simple Wordlist
The most basic format is just a list of words where each word is on a new line.

Constraints:
- No single lowercase letters. The application will add them automatically.
- No repeating words.
- No digits or garbadge characters as part of the words.

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

## Adding Support for Keys
TT9 allows assigning hotkeys for performing different functions. If your phone has a special key that does not appear on the Hotkey configuration screen, you can easily add support for it.

- Find [preferences/helpers/Hotkeys.java](io/github/sspanak/tt9/preferences/helpers/Hotkeys.java).
- In the file, find the `generateList()` function.
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