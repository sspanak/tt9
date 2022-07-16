# Traditional T9
TT9 is an IME (Input Method Editor) for Android devices with hardware keypad. It supports multiple languages and predictive text typing. _NOTE: TT9 is not usable on touchscreen-only devices._

This is a clone of the [original project](https://github.com/Clam-/TraditionalT9) by Clam-.

## Building
The recommended way of building is using Android Studio. As the of time of writing this, the current version is Android Studio Chipmunk | 2021.2.1 Patch 1.

### Building a Debug .apk
If you have not configure Android Studio yet, follow [the official manual](https://developer.android.com/training/basics/firstapp), then follow the simple steps below to get the project running.

- _Import the project in Android Studio._
- _Prevent the "Default Activity not found" issue._ The app does not have a default view or a launcher icon. For this reason, you must configure Android Studio not to launch anything after installing, otherwise it will fail with "Default Activity not found" or a similar message. To do so:
    - Open "Edit Configurations..." (Press Shift 3 times and select it from the command list)
    - Go to "General" tab.
    - Change "Launch Options" to "Nothing"
    - Hit "OK"

That's it! Now you should be able to deploy and debug the app on your device.

You can find more info in this [Github issue](https://github.com/android/input-samples/issues/18).

### Building a Release .apk
The project is configured to build an unsigned release variant by default. You just need to select the "release" variant from Android Studio options, then `Build -> Rebuild Project`. After that, just ignore all warnings until you get to the end of the process. You will find the `.apk` in the generated 'build/' folder.

### Building a Signed .apk
- Make sure you have a signing key. If you don't have one, follow the [official manual](https://developer.android.com/studio/publish/app-signing#sign-apk).
- In `build.gradle` find the `signingConfigs` and `buildTypes` sections and uncomment them.
- Set properly the environment variables listed in `signingConfigs.release` _(You may need to restart Android Studio after that)_. Alternatively, you may simply type the actual key path, alias and passwords there. **Just make sure not to commit them!**
- Build the project normally. Android Studio should show you where it has generated the signed `.apk` file. If not, look for it in the `build/` folder.

## Adding a new language
To support a new language one needs to:

- Modify CharMap.java
- New Map needs to be created with the characters to be encountered in addWord or in the user dictionary when loaded.
- New character array needs to be added for characters that are to cycle on each number press.
- New array needs to be made to tell where the capital letters start in that array.
- modify LangHelper.java
- Add status icons
    - Create proper icons for each mode (e.g. "Ab", "En", "12") and each screen size. The font must be Roboto Lt at an adequate size to fit the icon square with minimum padding. Text must be white and the background must be transparent as per the [official Android guide](https://android-doc.github.io/guide/practices/ui_guidelines/icon_design_status_bar.html). To simplify the process, you could use Android Studio. It has a built-in icon generator accessible by right-cicking on "drawable" folder -> New -> Image Asset. Then choose "Icon Type": "Notification Icons", "Asset Type": Text, "Trim": No, "Padding": 0%.
    - Add new entry in ICONMAP
- Add new LANGUAGE enum entry e.g. FR(3,5) (index, id) Where index is index in arrays like LOCALES, and id is the identifier used in the database and such. The latter should never change unless database update is done.
- Make sure new id matches const.xml
- Add the LOCALE of the language in the LOCALES Locale array
- Add translations for arrays.xml and strings.xml in to new files in the appropriate locale folder (e.g. res/values-de/arrays.xml.) AndroidStudio has a cute/nice Translation Editor which might be handy.
    - Edit the base arrays.xml file to add the new language. (pref_lang_titles, pref_lang_values)
    - Exclude translatable="false" items from the new locale arrays.xml file.
    - Also make sure the new language is added to pref_loaduserdictdesc (base non-locale strings.xml)
- Find a suitable dictionary and add it to assets

That should be it? I hope.

## Using the app
TODO: Initial config, loading a dictionary...
#### Configuration Options
TODO...
#### Hotkeys
See [the original manual](https://github.com/Clam-/TraditionalT9/wiki/Hotkeys).
#### Key Remapping
See [the original manual](https://github.com/Clam-/TraditionalT9/wiki/Key-remapping).

## Word Lists
Here is detailed information and licenses about the word lists used:
- [Bulgarian word list](docs/bgWordlistReadme.txt)
- [English word list](docs/enWordlistReadme.txt)
- [French word list](docs/frWordlistReadme.txt)
- [German word list](docs/deWordlistReadme.txt)
- [Russian word list](docs/ruWordlistReadme.txt)
- [Ukrainian word list](docs/ukWordlistReadme.txt)

## License
- The source code, the logo image and the icons are licensed under the conditions described in [LICENSE.txt](LICENSE.txt).
- "Roboto" font is under [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
- The word lists / dictionaries are licensed under the licenses provided in the [respective readme files](#word-lists), where applicable.